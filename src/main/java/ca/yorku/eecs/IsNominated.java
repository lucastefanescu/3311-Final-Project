package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Record;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class IsNominated implements HttpHandler {
    private final Driver driver;
    private String movieId;

    public IsNominated(neo4jDB db){
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
                return;
            }else if(!movieExists(httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(404, -1);
                return;
            }else{
                movieId = httpReqDeserialized.getString("movieId");
                String movie = checkNomination(movieId);
                
                if (movie == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                } 
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
            StatementResult result = session.run(query, map);
            if(result.hasNext()){
                return true;
            }else{
                return false;
            }
        }
    }
    public String checkNomination(String movieId){
        String query = "MATCH (m:Movie {movieId: $movieId}) " +
                "RETURN {movieId: m.movieId, name: m.name, nominated: m.nominated} AS result";

        Map map = Collections.singletonMap("movieId", movieId);

        try (Session session = driver.session()) {
            StatementResult result = session.run(query, map);

            if (result.hasNext()) {
                Record record = result.next();
                JSONObject jsonResult = new JSONObject(record.get("result").asMap());
                if (jsonResult.isNull("nominated")) {
                    return null;
                }
                return jsonResult.toString();
            }
        }
        return "An error occurred while processing the JSON request";
    }
}
