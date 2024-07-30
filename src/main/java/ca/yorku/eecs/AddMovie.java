package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
    TODO: only add movies if they aren't in the db
 */
public class AddMovie implements HttpHandler {
    private final Driver driver;
    private String movieId;
    private String name;

    public AddMovie(neo4jDB db){
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

            if(!httpReqDeserialized.has("name") || !httpReqDeserialized.has("movieId") ||
                    movieExists(httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(400, -1);
            }else{
                name = httpReqDeserialized.getString("name");
                movieId = httpReqDeserialized.getString("movieId");
                int status_code = addMovie(name, movieId);
                exchange.sendResponseHeaders(status_code, -1);
            }
        }catch(IOException | JSONException e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public int addMovie(String name, String movieId){
        String query = "CREATE (m:Movie {name: $name, movieId: $movieId}) RETURN m";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("movieId", movieId);

        try(Session session = driver.session()){
            session.executeWrite(tx -> tx.run(query, parameters).consume());
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
            Result result = session.run(query, map);
            if(result.hasNext()){
                return true;
            }else{
                return false;
            }
        }
    }
}
