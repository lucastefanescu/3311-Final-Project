package ca.yorku.eecs;

import com.sun.net.httpserver.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class AppTest extends TestCase {
    private static neo4jDB db;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigorous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = new neo4jDB("bolt://localhost:7687", "neo4j", "123456");
    }

    @Override
    protected void tearDown() throws Exception {
        db.closeDB();
        super.tearDown();
    }

    public void testAddActorPass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/addActor", new AddActor(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Keanu Reeves");
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addActor", requestBody.toString());
        new AddActor(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testAddActorFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/addActor", new AddActor(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing actorId
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Keanu Reeves");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addActor", requestBody.toString());
        new AddActor(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testAddMoviePass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/addMovie", new AddMovie(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Matrix");
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addMovie", requestBody.toString());
        new AddMovie(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testAddMovieFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/addMovie", new AddMovie(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing movieId
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Matrix");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addMovie", requestBody.toString());
        new AddMovie(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testAddRelationshipPass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/addRelationship", new AddRelationship(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addRelationship", requestBody.toString());
        new AddRelationship(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testAddRelationshipFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/addRelationship", new AddRelationship(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing movieId
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("PUT", "/addRelationship", requestBody.toString());
        new AddRelationship(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testComputeBaconNumberPass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/computeBaconNumber", new ComputeBaconNumber(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconNumber", requestBody.toString());
        new ComputeBaconNumber(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testComputeBaconNumberFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/computeBaconNumber", new ComputeBaconNumber(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing actorId
        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconNumber", requestBody.toString());
        new ComputeBaconNumber(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testComputeBaconPathPass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/computeBaconPath", new ComputeBaconPath(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconPath", requestBody.toString());
        new ComputeBaconPath(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testComputeBaconPathFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/computeBaconPath", new ComputeBaconPath(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing actorId
        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/computeBaconPath", requestBody.toString());
        new ComputeBaconPath(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testGetActorPass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/getActor", new GetActor(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getActor", requestBody.toString());
        new GetActor(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testGetActorFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/getActor", new GetActor(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing actorId
        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getActor", requestBody.toString());
        new GetActor(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testGetMoviePass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/getMovie", new GetMovie(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getMovie", requestBody.toString());
        new GetMovie(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testGetMovieFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/getMovie", new GetMovie(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing movieId
        JSONObject requestBody = new JSONObject();

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/getMovie", requestBody.toString());
        new GetMovie(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
    }

    public void testHasRelationshipPass() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/hasRelationship", new HasRelationship(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "1234");
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/hasRelationship", requestBody.toString());
        new HasRelationship(db).handle(exchange);

        assertEquals(200, exchange.getResponseCode());

        server.stop(0);
    }

    public void testHasRelationshipFail() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/hasRelationship", new HasRelationship(db));
        server.setExecutor(null); // creates a default executor
        server.start();

        // Create a JSON request body with missing actorId
        JSONObject requestBody = new JSONObject();
        requestBody.put("movieId", "5678");

        HttpExchangeMock exchange = new HttpExchangeMock("GET", "/hasRelationship", requestBody.toString());
        new HasRelationship(db).handle(exchange);

        assertEquals(400, exchange.getResponseCode());

        server.stop(0);
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
        public Object getAttribute(String s) {
            return null;
        }

        @Override
        public void setAttribute(String s, Object o) {
        }

        @Override
        public void setStreams(InputStream inputStream, OutputStream outputStream) {
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }
}
