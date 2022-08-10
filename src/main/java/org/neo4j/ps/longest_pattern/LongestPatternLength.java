package org.neo4j.ps.longest_pattern;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.stream.Stream;


public class LongestPatternLength {

    @Context
    public Transaction tx;

    @Procedure(value = "org.neo4j.ps.getLongestPathLength", mode = Mode.WRITE)
    @Description("find the longest path from a StartNode to and EndNode in the graph")
    public Stream<LongestPatternLengthRecord> getLongestPatternLength(@Name("longestHypothesis") Long longestHypothesis){
        String pathQuery = "MATCH p=(sn:Node)-[:TRANSFORMS_TO*" + longestHypothesis + "]->(en:Node)" + "\n"
                + "RETURN sn.id, en.id LIMIT 1";
        Result result = tx.execute(pathQuery);

        if (result.hasNext()){
            return getLongestPatternLength(++longestHypothesis);
        } else if (longestHypothesis <= 0){
            return Stream.of(new LongestPatternLengthRecord(longestHypothesis));
        } else {
            --longestHypothesis;
            String writeLengthToGraphQuery = "MERGE (l:QueryLimit {id: 'limit'}) SET l.limit = " + longestHypothesis;
            tx.execute(writeLengthToGraphQuery);
            return Stream.of(new LongestPatternLengthRecord(longestHypothesis));
        }

    }

    public static final class LongestPatternLengthRecord {
        public final Long length;

        LongestPatternLengthRecord(Long length) {
            this.length = length;
        }
    }
}
