package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
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

public class ListMovies implements HttpHandler {
    private final Driver driver;
    private String genre;

    public ListMovies(neo4jDB db){
        driver = db.getDriver();
    }
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(exchange.getRequestMethod().equals("GET")){
                handleGet(exchange);
            }else{
                exchange.sendResponseHeaders(400, -1);
            }
        }catch (Exception e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public void handleGet(HttpExchange exchange) throws JSONException, IOException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("genre")){
                exchange.sendResponseHeaders(400, -1);
            }else{
                genre = httpReqDeserialized.getString("genre");
                String movies = retrieveMoviesByGenre(genre);
                byte[] moviesAsByteArray = movies.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, moviesAsByteArray.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(moviesAsByteArray);
                outputStream.close();
            }
        }catch(JSONException | IOException e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public String retrieveMoviesByGenre(String genre){
    	String query = "MATCH (m:Movie {genre: $genre}) " +
                "RETURN {movieId: m.movieId, name: m.name} AS result";
        Map<String, Object> map = Collections.singletonMap("genre", genre);

        try (Session session = driver.session()) {
            StatementResult result = session.run(query, map);
            JSONArray moviesArray = new JSONArray();

            while (result.hasNext()) {
                Record record = result.next();
                JSONObject movie = new JSONObject(record.get("result").asMap());
                moviesArray.put(movie); // put all movie objects into array
            }
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("genre", genre);
            jsonResult.put("movies", moviesArray);
            
            return jsonResult.toString();          
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "An error occurred while processing the JSON request";
    }
}
