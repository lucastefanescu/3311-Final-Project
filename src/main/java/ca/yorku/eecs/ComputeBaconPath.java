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
import java.util.List;
import java.util.Map;

public class ComputeBaconPath implements HttpHandler {
    private Driver driver;

    public ComputeBaconPath(neo4jDB db){
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
        } catch (IOException e) {
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try{
            String body = Utils.convert(exchange.getRequestBody());
            JSONObject httpReqDeserialized = new JSONObject(body);

            if(!httpReqDeserialized.has("actorId")){
                exchange.sendResponseHeaders(400, -1);
            }else if(!actorExists(httpReqDeserialized.getString("actorId"))){
                exchange.sendResponseHeaders(404, -1);
            }else{
                String baconPath = retrieveBaconPath(httpReqDeserialized.getString("actorId"));
                if(baconPath.isEmpty()){
                    exchange.sendResponseHeaders(404, -1);
                }else{
                    byte[] baconPathAsArray = baconPath.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, baconPathAsArray.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(baconPathAsArray);
                    outputStream.close();
                }
            }
        } catch (IOException | JSONException e) {
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }

    private String retrieveBaconPath(String actorId) throws JSONException {
        if(actorId.equals("nm0000102")){
            JSONObject jsonObject = new JSONObject().put("baconPath", "nm0000102");
            return jsonObject.toString();
        }
        String query = "MATCH (bacon:Actor {actorId: 'nm0000102'}), (actor:Actor {actorId: $actorId})\n" +
                "OPTIONAL MATCH p=shortestPath((actor)-[:ACTED_IN*]-(bacon))\n" +
                "RETURN \n" +
                "    [n IN nodes(p) |\n" +
                "        CASE\n" +
                "            WHEN n:Actor THEN n.actorId\n" +
                "            WHEN n:Movie THEN n.movieId\n" +
                "            ELSE null\n" +
                "        END\n" +
                "    ] AS baconPath,\n" +
                "    p IS NOT NULL AS pathExists";

        try(Session session = driver.session()){
            Map<String, Object> map = Collections.singletonMap("actorId", actorId);
            StatementResult result = session.run(query, map);
            if(result.hasNext()){
                Record record = result.next();
                if (record.get("pathExists").asBoolean()) {
                    List<Object> pathList = record.get("baconPath").asList();
                    JSONObject jsonObject = new JSONObject().put("baconPath", new JSONArray(pathList));
                    return jsonObject.toString();
                }else{
                    return "";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "An error occurred while processing the JSON request";
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
}
