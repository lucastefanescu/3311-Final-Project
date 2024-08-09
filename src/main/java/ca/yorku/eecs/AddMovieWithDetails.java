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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class AddMovieWithDetails implements HttpHandler {
    private final Driver driver;
    private String movieId;
    private String name;
    private String rating;
    private String genre;
    private String nominated;

    public AddMovieWithDetails(neo4jDB db){
        driver = db.getDriver();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
                handlePut(exchange);
            }else{
                exchange.sendResponseHeaders(400, -1);
            }
        }catch (Exception e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public void handlePut(HttpExchange exchange) throws IOException, JSONException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("name") || !httpReqDeserialized.has("movieId") || !httpReqDeserialized.has("rating") || !httpReqDeserialized.has("genre") ||
                    movieExists(httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(400, -1);
            }else{
                name = httpReqDeserialized.getString("name");
                movieId = httpReqDeserialized.getString("movieId"); 
                genre = httpReqDeserialized.getString("genre");
                if (!httpReqDeserialized.has("rating")) {
                	rating = "0";
                }
                else {
                	rating = httpReqDeserialized.getString("rating");
                }
                if (!httpReqDeserialized.has("nominated")) {
                	nominated = "false";
                }
                else {
                	nominated = httpReqDeserialized.getString("nominated");
                }
                int status_code = addMovie(name, movieId, rating, genre, nominated);
                exchange.sendResponseHeaders(status_code, -1);
            }
        }catch(IOException | JSONException e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public int addMovie(String name, String movieId, String rating, String genre, String nominated){
        String query = "CREATE (m:Movie {name: $name, movieId: $movieId, rating: $rating, genre: $genre, nominated: $nominated}) RETURN m";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("movieId", movieId);
        parameters.put("rating", rating);
        parameters.put("genre", genre);
        parameters.put("nominated", nominated);

        try(Session session = driver.session()){
            session.run(query, parameters);
            return 200;
        }catch(Exception e){
            e.printStackTrace();
            return 500;
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
}
