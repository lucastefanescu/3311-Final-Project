package ca.yorku.eecs;


import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class neo4jDB {
    private final Driver driver;

    public neo4jDB(String url, String user, String password){
        driver = GraphDatabase.driver(url, AuthTokens.basic(user, password));
    }

    public void closeDB(){
        driver.close();
    }

    public Driver getDriver(){
        return this.driver;
    }
}
