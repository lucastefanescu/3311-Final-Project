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

public class GetHighestRating implements HttpHandler {
    private final Driver driver;
    private String actorId;

    public GetHighestRating(neo4jDB db){
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

            if(!httpReqDeserialized.has("actorId")){
                exchange.sendResponseHeaders(400, -1);
            }else if(!actorExists(httpReqDeserialized.getString("actorId"))){
                exchange.sendResponseHeaders(404, -1);
            }else{
                actorId = httpReqDeserialized.getString("actorId");
                String actor = retrieveHighestRatedMovie(actorId);
                byte[] actorAsByteArray = actor.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, actorAsByteArray.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(actorAsByteArray);
                outputStream.close();
            }
        }catch(JSONException | IOException e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
    private boolean actorExists(String actorId) {
        String query = "MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a";
        Map<String, Object> map = Collections.singletonMap("actorId", actorId);

        try(Session session = driver.session()){
            StatementResult result = session.run(query, map);
            return result.hasNext();
        }
    }
    public String retrieveHighestRatedMovie(String actorId){
        String query = "MATCH (a:Actor {actorId: $actorId})-[:ACTED_IN]->(m:Movie) " +
                "RETURN a.actorId AS actorId, m.name AS name, MAX(m.rating) AS rating "; 

        Map map = Collections.singletonMap("actorId", actorId);

        try (Session session = driver.session()) {
            StatementResult result = session.run(query, map);

            if (result.hasNext()) {
                Record record = result.next();
                JSONObject jsonResult = new JSONObject();
                jsonResult.put("actorId", record.get("actorId").asString());
                jsonResult.put("name", record.get("name").asString());
                jsonResult.put("rating", record.get("rating").isNull() ? 0 : record.get("rating").asString());
                return jsonResult.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "An error occurred while processing the JSON request";
    }
}
