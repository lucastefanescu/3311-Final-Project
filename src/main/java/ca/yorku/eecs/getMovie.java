package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class getMovie implements HttpHandler {
    private final Driver driver;
    private String movieId;

    public getMovie(neo4jDB db){
        driver = db.getDriver();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(exchange.getRequestMethod().equals("GET")){
                handleGet(exchange);
            }else{
                exchange.sendResponseHeaders(400, -1);
            }
        }catch(IOException | JSONException e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, JSONException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("movieId")){
                exchange.sendResponseHeaders(400, -1);
            }else if(!movieExists(httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(404, -1);
            }else{
                movieId = httpReqDeserialized.getString("movieId");
                String movie = retrieveMovie(movieId);
                byte[] movieAsByteArray = movie.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, movieAsByteArray.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(movieAsByteArray);
                outputStream.close();
            }
        } catch (IOException | JSONException e) {
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public boolean movieExists(String movieId){
        String query = "MATCH (m:Movie) WHERE m.movieId = $movieId RETURN m";

        Map<String, Object> map = Collections.singletonMap("movieId", movieId);

        try(Session session = driver.session()){
            Result result = session.run(query, map);
            if(result.hasNext()){
                return true;
            }else{
                return false;
            }
        }
    }
    public String retrieveMovie(String movieId){
        String query = "MATCH (m:Movie {movieId: $movieId}) " +
                "OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor) " +
                "RETURN {movieId: m.movieId, name: m.name, actors: collect(a.name)} AS result";

        Map map = Collections.singletonMap("movieId", movieId);

        try (Session session = driver.session()) {
            Result result = session.run(query, map);

            if (result.hasNext()) {
                System.out.println("hello");
                Record record = result.next();
                JSONObject jsonResult = new JSONObject(record.get("result").asMap());
                return jsonResult.toString();
            }
        }
        return "An error occurred while processing the JSON request";
    }
}
