package ca.yorku.eecs;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

public class getActor implements HttpHandler {
    private final Driver driver;
    private String actorId;

    public getActor(neo4jDB db){
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
                String actor = retrieveActor(actorId);
                byte[] actorAsByteArray = actor.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, actorAsByteArray.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(actorAsByteArray);
                outputStream.close();
            }
        }catch(JSONException | IOException e){
            exchange.sendResponseHeaders(500, -1);
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
    public String retrieveActor(String actorId){
        String query = "MATCH (a:Actor {actorId: $actorId})-[:ACTED_IN]->(m:Movie) " +
                "RETURN {actorId: a.actorId, name: a.name, movies: collect(m.name)} " +
                "AS result";

        Map map = Collections.singletonMap("actorId", actorId);

        try(Session session = driver.session()){
            Result result = session.run(query, map);
            Record record = result.next();
            JSONObject jsonResult = new JSONObject(record.get("result").asMap());
            return jsonResult.toString();
        }
    }
}
