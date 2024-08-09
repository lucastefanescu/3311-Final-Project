package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddActor implements HttpHandler {

    private String actorName;
    private String actorId;
    private final Driver driver;

    public AddActor(neo4jDB db){
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
        }catch (Exception e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("name") || !httpReqDeserialized.has("actorId")
                    || actorExists(httpReqDeserialized.getString("actorId"))){
                exchange.sendResponseHeaders(400, -1);
            }else{
                actorName = httpReqDeserialized.getString("name");
                actorId = httpReqDeserialized.getString("actorId");

                int status_code = addActor(actorName, actorId);

                exchange.sendResponseHeaders(status_code, -1);
            }
        }catch (JSONException e) {
            e.printStackTrace();
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

    public int addActor(String actorName, String actorId){

        String query = "CREATE (a:Actor {name: $name, actorId: $actorId}) RETURN a";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", actorName);
        parameters.put("actorId", actorId);

        try(Session session = driver.session()){
            session.run(query, parameters);
            return 200;
        }catch(Exception e){
            e.printStackTrace();
            return 500;
        }
    }
}
