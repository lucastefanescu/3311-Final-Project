package ca.yorku.eecs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AppTest extends TestCase {
    private AddActor addActor;
    private AddMovie addMovie;
    private AddRelationship addRelationship;
    private Driver mockDriver;
    private HttpExchange mockExchange;
    private Session mockSession;
    private neo4jDB mockDb;

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
        mockDriver = mock(Driver.class);
        mockSession = mock(Session.class);
        when(mockDriver.session()).thenReturn(mockSession);

        mockDb = mock(neo4jDB.class);
        when(mockDb.getDriver()).thenReturn(mockDriver);

        addActor = new AddActor(mockDb);
        addMovie = new AddMovie(mockDb);
        addRelationship = new AddRelationship(mockDb);
        mockExchange = mock(HttpExchange.class);
    }

    public void testAddActorPass() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "John Doe");
        requestBody.put("actorId", "12345");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        when(mockSession.run(anyString(), anyMap())).thenReturn(mock(Result.class));

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(200), anyLong());

        // Call the handle method
        addActor.handle(mockExchange);

        // Verify the response status is 200
        verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
    }

    public void testAddActorFail() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "John Doe");
        requestBody.put("actorId", "12345");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        Result mockResult = mock(Result.class);
        when(mockResult.hasNext()).thenReturn(true);  // Simulate actor already exists
        when(mockSession.run(anyString(), anyMap())).thenReturn(mockResult);

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(400), anyLong());

        // Call the handle method
        addActor.handle(mockExchange);

        // Verify the response status is 400
        verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
    }

    public void testAddMoviePass() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Some Movie");
        requestBody.put("movieId", "67890");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        when(mockSession.run(anyString(), anyMap())).thenReturn(mock(Result.class));

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(200), anyLong());

        // Call the handle method
        addMovie.handle(mockExchange);

        // Verify the response status is 200
        verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
    }

    public void testAddMovieFail() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Some Movie");
        requestBody.put("movieId", "67890");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        Result mockResult = mock(Result.class);
        when(mockResult.hasNext()).thenReturn(true);  // Simulate movie already exists
        when(mockSession.run(anyString(), anyMap())).thenReturn(mockResult);

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(400), anyLong());

        // Call the handle method
        addMovie.handle(mockExchange);

        // Verify the response status is 400
        verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
    }

    public void testAddRelationshipPass() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "12345");
        requestBody.put("movieId", "67890");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        Result mockEmptyResult = mock(Result.class);
        when(mockEmptyResult.hasNext()).thenReturn(false);  // Simulate no existing relationship

        // Ensure actor and movie exist
        Result mockActorResult = mock(Result.class);
        when(mockActorResult.hasNext()).thenReturn(true);
        Result mockMovieResult = mock(Result.class);
        when(mockMovieResult.hasNext()).thenReturn(true);

        // Mock specific queries to ensure actor and movie exist and no existing relationship
        when(mockSession.run(eq("MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]->(m:Movie {movieId: $movieId}) RETURN r"), anyMap())).thenReturn(mockEmptyResult);
        when(mockSession.run(eq("MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a"), anyMap())).thenReturn(mockActorResult);
        when(mockSession.run(eq("MATCH (m:Movie) WHERE m.movieId = $movieId RETURN m"), anyMap())).thenReturn(mockMovieResult);

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(200), anyLong());

        // Call the handle method
        addRelationship.handle(mockExchange);

        // Verify the response status is 200
        verify(mockExchange).sendResponseHeaders(eq(200), anyLong());

        // Verify the relationshipExists method was called
        verify(mockSession).run(eq("MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]-(m:Movie {movieId: $movieId}) RETURN r"), anyMap());
        // Verify the actorExists method was called
        verify(mockSession).run(eq("MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a"), anyMap());
        // Verify the movieExists method was called
        verify(mockSession).run(eq("MATCH (m:Movie) WHERE m.movieId = $movieId RETURN m"), anyMap());
    }

    public void testAddRelationshipFail() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "12345");
        requestBody.put("movieId", "67890");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        Result mockResult = mock(Result.class);
        when(mockResult.hasNext()).thenReturn(true);  // Simulate relationship already exists
        when(mockSession.run(anyString(), anyMap())).thenReturn(mockResult);

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(400), anyLong());

        // Call the handle method
        addRelationship.handle(mockExchange);

        // Verify the response status is 400
        verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
    }

    public void testAddRelationshipActorNotFound() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "12345");
        requestBody.put("movieId", "67890");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        Result mockEmptyResult = mock(Result.class);
        when(mockEmptyResult.hasNext()).thenReturn(false);  // Simulate actor does not exist
        Result mockMovieResult = mock(Result.class);
        when(mockMovieResult.hasNext()).thenReturn(true);  // Simulate movie exists

        when(mockSession.run(eq("MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a"), anyMap())).thenReturn(mockEmptyResult);
        when(mockSession.run(eq("MATCH (m:Movie) WHERE m.movieId = $movieId RETURN m"), anyMap())).thenReturn(mockMovieResult);
        when(mockSession.run(anyString(), anyMap())).thenReturn(mockEmptyResult); // For other queries

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(404), anyLong());

        // Call the handle method
        addRelationship.handle(mockExchange);

        // Verify the response status is 404
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
    }

    public void testAddRelationshipMovieNotFound() throws IOException, JSONException {
        // Mock the HTTP exchange
        when(mockExchange.getRequestMethod()).thenReturn("PUT");
        JSONObject requestBody = new JSONObject();
        requestBody.put("actorId", "12345");
        requestBody.put("movieId", "67890");
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock the database session
        Result mockEmptyResult = mock(Result.class);
        when(mockEmptyResult.hasNext()).thenReturn(false);  // Simulate movie does not exist
        Result mockActorResult = mock(Result.class);
        when(mockActorResult.hasNext()).thenReturn(true);  // Simulate actor exists

        when(mockSession.run(eq("MATCH (a:Actor) WHERE a.actorId = $actorId RETURN a"), anyMap())).thenReturn(mockActorResult);
        when(mockSession.run(eq("MATCH (m:Movie) WHERE m.movieId = $movieId RETURN m"), anyMap())).thenReturn(mockEmptyResult);
        when(mockSession.run(anyString(), anyMap())).thenReturn(mockEmptyResult); // For other queries

        // Mock the response headers
        doNothing().when(mockExchange).sendResponseHeaders(eq(404), anyLong());

        // Call the handle method
        addRelationship.handle(mockExchange);

        // Verify the response status is 404
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
    }
}
