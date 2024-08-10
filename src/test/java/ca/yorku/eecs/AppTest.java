package ca.yorku.eecs;

import com.sun.net.httpserver.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class AppTest extends TestCase {
    private static neo4jDB db;
    private HttpServer server;

    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = new neo4jDB("bolt://localhost:7687", "neo4j", "12345678");
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.setExecutor(null);
        server.start();
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("Tearing down the test and closing the Neo4j driver...");
        server.stop(0);
        db.closeDB();
        super.tearDown();
    }
    public void deleteAllNodes(){
        String query = "MATCH(n) DETACH DELETE(n)";
        try(Session session = db.getDriver().session()){
            StatementResult result = session.run(query);
        }
    }
    public void testAddActorPass() throws Exception {
        server.createContext("/addActor", new AddActor(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Keanu Reeves");
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addActor", requestBody.toString());
        new AddActor(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testAddActorFail() throws Exception {
        server.createContext("/addActor", new AddActor(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Keanu Reeves");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addActor", requestBody.toString());
        new AddActor(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testAddMoviePass() throws Exception {
        server.createContext("/addMovie", new AddMovie(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Matrix");
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addMovie", requestBody.toString());
        new AddMovie(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testAddMovieFail() throws Exception {
        server.createContext("/addMovie", new AddMovie(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Matrix");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addMovie", requestBody.toString());
        new AddMovie(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testAddRelationshipPass() throws Exception {
        new AddActor(db).addActor("Keanu Reeves", "1234");
        new AddMovie(db).addMovie("The Matrix", "5678");

        server.createContext("/addRelationship", new AddRelationship(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addRelationship", requestBody.toString());
        new AddRelationship(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
    }

    public void testAddRelationshipFail() throws Exception {
        server.createContext("/addRelationship", new AddRelationship(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addRelationship", requestBody.toString());
        new AddRelationship(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testComputeBaconNumberPass() throws Exception {
        new AddActor(db).addActor("Kevin Bacon", "nm0000102");
        server.createContext("/computeBaconNumber", new ComputeBaconNumber(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "nm0000102");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconNumber", requestBody.toString());
        new ComputeBaconNumber(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testComputeBaconNumberFail() throws Exception {
        server.createContext("/computeBaconNumber", new ComputeBaconNumber(db));

        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconNumber", requestBody.toString());
        new ComputeBaconNumber(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testComputeBaconPathPass() throws Exception {
        new AddActor(db).addActor("Kevin Bacon", "nm0000102");
        server.createContext("/computeBaconPath", new ComputeBaconPath(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "nm0000102");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconPath", requestBody.toString());
        new ComputeBaconPath(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testComputeBaconPathFail() throws Exception {
        server.createContext("/computeBaconPath", new ComputeBaconPath(db));

        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconPath", requestBody.toString());
        new ComputeBaconPath(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testGetActorPass() throws Exception {
        new AddActor(db).addActor("Keanu Reeves", "1234");
        server.createContext("/getActor", new GetActor(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getActor", requestBody.toString());
        new GetActor(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testGetActorFail() throws Exception {
        server.createContext("/getActor", new GetActor(db));

        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getActor", requestBody.toString());
        new GetActor(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testGetMoviePass() throws Exception {
        new AddMovie(db).addMovie("The Matrix", "5678");
        server.createContext("/getMovie", new GetMovie(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getMovie", requestBody.toString());
        new GetMovie(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testGetMovieFail() throws Exception {

        server.createContext("/getMovie", new GetMovie(db));

        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getMovie", requestBody.toString());
        new GetMovie(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testHasRelationshipPass() throws Exception {
        new AddActor(db).addActor("Keanu Reeves", "1234");
        new AddMovie(db).addMovie("The Matrix", "5678");
        new AddRelationship(db).addRelationship("1234", "5678");

        server.createContext("/addRelationship", new AddRelationship(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/hasRelationship", requestBody.toString());
        new HasRelationship(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        deleteAllNodes();
    }

    public void testHasRelationshipFail() throws Exception {
        server.createContext("/hasRelationship", new HasRelationship(db));

        JSONObject requestBody = new JSONObject();
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/hasRelationship", requestBody.toString());
        new HasRelationship(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());
        deleteAllNodes();
    }

    private class HttpExchangeMock extends HttpExchange {
        private final String requestMethod;
        private final String requestURI;
        private final String requestBody;
        private int responseCode;
        private final Headers requestHeaders;
        private final Headers responseHeaders;
        private final ByteArrayOutputStream responseBody;

        public HttpExchangeMock(String requestMethod, String requestURI, String requestBody) {
            this.requestMethod = requestMethod;
            this.requestURI = requestURI;
            this.requestBody = requestBody;
            this.requestHeaders = new Headers();
            this.responseHeaders = new Headers();
            this.responseBody = new ByteArrayOutputStream();
        }

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return URI.create(requestURI);
        }

        @Override
        public String getRequestMethod() {
            return requestMethod;
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getRequestBody() {
            return new ByteArrayInputStream(requestBody.getBytes());
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            this.responseCode = rCode;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {

        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {

        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }
}