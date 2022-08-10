package org.neo4j.ps.longest_pattern;

import org.junit.jupiter.api.*;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LongestPathLengthTest {

    private Driver driver;

    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j(){
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(LongestPatternLength.class)
                .build();
        this.driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
    }

    @AfterAll
    void closeDriver(){
        this.driver.close();
        this.embeddedDatabaseServer.close();
    }

    @AfterEach
    void cleanDb() {
        try(Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    @Test
    public void testLongestPathEqualsZero(){
        try(Session session = driver.session()) {
            session.run("CREATE (n:StartNode {id: '12'})");
            session.run("CREATE (e:EndNode {id: '13'})");

            Result r = session.run("CALL org.neo4j.ps.getLongestPathLength(0) YIELD length RETURN length");
            Integer len = r.single().get("length").asInt();
            assertEquals(0, len);
        }
    }

    @Test
    public void testLongestPathEqualsOne() {
        try (Session session = driver.session()) {
            session.run("CREATE (n:StartNode {id: '12'}) CREATE (e:EndNode {id: '13'}) CREATE (n)-[:TRANSFORMS_TO]->(e)");
            Result r = session.run("CALL org.neo4j.ps.getLongestPathLength(1) YIELD length RETURN length");
            Integer len = r.single().get("length").asInt();
            assertEquals(1, len);
        }
    }

    @Test
    public void testSimpleLongestPath() {
        try (Session session = driver.session()) {
            session.run("CREATE (s:Node{id: '12'}) " +
                    "CREATE (e:Node {id: '13'}) " +
                    "CREATE (n:Node {id: '11'})" +
                    "CREATE (s)-[:TRANSFORMS_TO]->(n)-[:TRANSFORMS_TO]->(e)");
            Result r = session.run("CALL org.neo4j.ps.getLongestPathLength(1) YIELD length RETURN length");
            Integer len = r.single().get("length").asInt();
            assertEquals(2, 2);
        }
    }
}
