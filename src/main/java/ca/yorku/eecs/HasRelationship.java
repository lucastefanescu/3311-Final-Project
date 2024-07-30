package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Record;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HasRelationship implements HttpHandler {

    private final Driver driver;

    public HasRelationship(neo4jDB db){
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
        }catch(Exception e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }

    public void handleGet(HttpExchange exchange){
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("movieId") || !httpReqDeserialized.has("actorId")){
                exchange.sendResponseHeaders(400, -1);
            }else if(!actorExists(httpReqDeserialized.getString("actorId"))
                    || !movieExists(httpReqDeserialized.getString("movieId"))){
                exchange.sendResponseHeaders(404, -1);
            }else{
                String movieId = httpReqDeserialized.getString("movieId");
                String actorId = httpReqDeserialized.getString("actorId");
                String relationship = retrieveRelationship(movieId, actorId);
                byte[] relationshipAsByteArray = relationship.getBytes(StandardCharsets.UTF_8);
                OutputStream outputStream = exchange.getResponseBody();
                exchange.sendResponseHeaders(200, relationshipAsByteArray.length);
                outputStream.write(relationshipAsByteArray);
                outputStream.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    public String retrieveRelationship(String movieId, String actorId){
        String query = "MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId})" +
                "OPTIONAL MATCH (a)-[r:ACTED_IN]->(m) " +
                "RETURN {actorId: a.actorId, movieId: m.movieId, hasRelationship: r IS NOT NULL} AS result";

        try(Session session = driver.session()){
            Map<String, Object> map = new HashMap<>();
            map.put("movieId", movieId);
            map.put("actorId", actorId);

            Result result = session.run(query, map);
            if(result.hasNext()){
                Record record = result.next();
                JSONObject jsonObject = new JSONObject(record.get("result").asMap());
                return jsonObject.toString();
            }else{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("movieId", movieId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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
}
