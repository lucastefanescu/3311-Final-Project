package ca.yorku.eecs;


import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class neo4jDB {
    private final Driver driver;
    public static Config config;

    public neo4jDB(String url, String user, String password){
        config = Config.builder().withoutEncryption().build();
        driver = GraphDatabase.driver(url, AuthTokens.basic(user, password), config);
    }

    public void closeDB(){
        driver.close();
    }

    public Driver getDriver(){
        return this.driver;
    }
}
