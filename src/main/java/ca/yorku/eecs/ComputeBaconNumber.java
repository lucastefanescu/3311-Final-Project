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

public class ComputeBaconNumber implements HttpHandler {
    private final Driver driver;

    public ComputeBaconNumber(neo4jDB db){
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
        } catch (IOException | JSONException e) {
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange exchange) throws JSONException, IOException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("actorId")){
                exchange.sendResponseHeaders(400, -1);
            }else if(!actorExists(httpReqDeserialized.getString("actorId"))){
                exchange.sendResponseHeaders(404, -1);
            }else{
                String baconNumber = findBaconPath(httpReqDeserialized.getString("actorId"));
                if(baconNumber.isEmpty()) {
                    exchange.sendResponseHeaders(404, -1);
                }else{
                    byte[] baconPathAsByteArray = baconNumber.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, baconPathAsByteArray.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(baconPathAsByteArray);
                    outputStream.close();
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private boolean actorExists(String actorId) {
        String query = "MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a";
        Map<String, Object> map = Collections.singletonMap("actorId", actorId);

        try(Session session = driver.session()){
            StatementResult result = session.run(query, map);
            if(result.hasNext()){
                return true;
            }else{
                return false;
            }
        }
    }

    private String findBaconPath(String actorId){
        String query = "MATCH (bacon:Actor {actorId: 'nm0000102'}), (actor:Actor {actorId: $actorId})\n" +
                "WITH bacon, actor\n" +
                "OPTIONAL MATCH p=(bacon)-[:ACTED_IN*]-(actor)\n" +
                "WHERE bacon <> actor\n" +
                "RETURN CASE \n" +
                "           WHEN $actorId = 'nm0000102' THEN 0\n" +
                "           WHEN p IS NULL THEN -1\n" +
                "           ELSE length(p)/2 \n" +
                "       END AS baconNumber";

        try(Session session = driver.session()){
            Map<String, Object> map = Collections.singletonMap("actorId", actorId);
            StatementResult result = session.run(query, map);
            if(result.hasNext()){
                Record record = result.next();
                if(record.get("baconNumber").asInt() == -1){
                    return "";
                }else{
                    JSONObject jsonobject = new JSONObject();
                    jsonobject.put("baconNumber", record.get("baconNumber").asInt());
                    return jsonobject.toString();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "An error occurred while processing the JSON request";
    }
}
