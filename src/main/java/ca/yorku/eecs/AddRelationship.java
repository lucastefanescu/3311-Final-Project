package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddRelationship implements HttpHandler {
    private final Driver driver;
    private String movieId;
    private String actorId;

    public AddRelationship(neo4jDB db){
        driver = db.getDriver();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(exchange.getRequestMethod().equals("PUT")){
                handlePut(exchange);
            }else{
                exchange.sendResponseHeaders(400, -1);
            }
        }catch (IOException e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public void handlePut(HttpExchange exchange) throws IOException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("actorId") || !httpReqDeserialized.has("movieId")
                    || relationshipExists(httpReqDeserialized.getString("actorId"), httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(400, -1);
            }else if(!actorExists(httpReqDeserialized.getString("actorId")) || !movieExists(httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(404, -1);
            }else{
                actorId = httpReqDeserialized.getString("actorId");
                movieId = httpReqDeserialized.getString("movieId");

                int status_code = addRelationship(actorId, movieId);
                exchange.sendResponseHeaders(status_code, -1);
            }
        } catch (JSONException e) {
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        } catch (IOException e) {
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    public int addRelationship(String actorId, String movieId){
        String query = "MATCH (a:Actor), (b:Movie) " +
                "WHERE a.actorId = $actorId AND b.movieId = $movieId " +
                "MERGE (a)-[r:ACTED_IN]->(b)" +
                "RETURN type(r)";

        Map<String, Object> map = new HashMap<>();
        map.put("movieId", movieId);
        map.put("actorId", actorId);

        try(Session session = driver.session()){
            session.executeWrite(tx -> tx.run(query, map).consume());
            return 200;
        }catch(Exception e){
            e.printStackTrace();
            return 500;
        }
    }
    public boolean relationshipExists(String actorId, String movieId){
        String query = "MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]-(m:Movie {movieId: $movieId}) RETURN r";

        Map<String, Object> map = new HashMap<>();
        map.put("movieId", movieId);
        map.put("actorId", actorId);

        try(Session session = driver.session()){
            Result result = session.run(query, map);
            if(result.hasNext()){
                return true;
            }else{
                return false;
            }
        }
    }
    private boolean actorExists(String actorId) {
        String query = "MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a";
        Map<String, Object> map = Collections.singletonMap("actorId", actorId);

        try(Session session = driver.session()){
            Result result = session.run(query, map);
            if(result.hasNext()){
                return true;
            }else{
                return false;
            }
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
