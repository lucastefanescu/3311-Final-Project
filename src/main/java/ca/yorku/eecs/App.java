package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        neo4jDB db = new neo4jDB("bolt://localhost:7687", "neo4j", "12345678");

        server.createContext("/api/v1/addActor", new AddActor(db));
        server.createContext("/api/v1/addMovie", new AddMovie(db));
        server.createContext("/api/v1/addRelationship", new AddRelationship(db));
        server.createContext("/api/v1/getActor", new getActor(db));
        server.createContext("/api/v1/getMovie", new getMovie(db));

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
