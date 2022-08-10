package org.neo4j.ps.longest_pattern;

import java.util.*;
import java.util.stream.Stream;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;

import static org.neo4j.graphdb.Direction.OUTGOING;

public class LongestPathLengthAlt {

    @Context
    public Transaction tx;

    @Procedure(value = "org.neo4j.ps.getLongestPathLengthAlt")
    @Description("Get the longest path between any node of label A and any node of label B")
    public Stream<Integer> getLongestPathLength(@Name("fromLabel") String from, @Name("toLabel") String to) {

        Label fromLabel = Label.label(from);
        Label toLabel = Label.label(to);
        Label allLabel = Label.label("Node");
        ResourceIterator<Node> fromNodes = tx.findNodes(fromLabel);
        ResourceIterator<Node> toNodes = tx.findNodes(toLabel);

        return new ArrayList<Integer>().stream();

    }

    private LongestPath getLongestPathFromNode(Node n){

        Set<String> topologicalSortVisited = new HashSet<>();
        Stack<Node> topologicalSort = new Stack<>();
        constructTopologicalSort(n, topologicalSortVisited, topologicalSort);

        RelationshipType transformsTo = RelationshipType.withName("TRANSFORMS_TO");

        Set<String> visited = new HashSet<>();
        Map<String, Integer> distances = new HashMap<>();

        String startNodeId = getNodeId(n);
        distances.put(startNodeId, 0);

        while (!(topologicalSort.isEmpty())){
            Node current = topologicalSort.pop();
            String currentNodeId = getNodeId(current);
            Integer currentDistance = distances.get(currentNodeId);
            if (currentDistance != null){
                Iterable<Relationship> rels = current.getRelationships(OUTGOING, transformsTo);
                for (Relationship rel : rels){
                    Node adjacent = rel.getEndNode();
                    String adjacentNodeId = getNodeId(adjacent);
                    Integer adjacentNodeDistance = distances.get(adjacentNodeId);
                    if (adjacentNodeDistance != null ) {
                        Integer potentialNewDistance = currentDistance + 1;
                        if (adjacentNodeDistance < potentialNewDistance){
                            distances.put(adjacentNodeId, potentialNewDistance);
                        }
                    }
                }
            }
        }

        distances.entrySet().stream().max((a, b) -> a.getValue() > b.getValue() ? 1 : -1).get().getKey();

        return new LongestPath(1, "placeholder");
    }

    private Iterable<Relationship> getOutgoingRelationships(Node n) {
        RelationshipType transformsTo = RelationshipType.withName("TRANSFORMS_TO");
        return n.getRelationships(OUTGOING, transformsTo);
    }

    private void constructTopologicalSort(Node start, Set<String> visited, Stack<Node> topSort){
        String nodeId = getNodeId(start);
        visited.add(nodeId);
        RelationshipType transformsTo = RelationshipType.withName("TRANSFORMS_TO");
        Iterable<Relationship> outgoingRelationships = start.getRelationships(OUTGOING, transformsTo);

        for (Relationship r : outgoingRelationships){
            Node adjacent = r.getEndNode();
            String adjacentNodeId = getNodeId(adjacent);
            if (!visited.contains(adjacentNodeId)){
                constructTopologicalSort(adjacent, visited, topSort);
            }
        }

        topSort.push(start);
    }

    private String getNodeId(Node n){
        Map<String, Object> nodeIdMap = n.getProperties("id");
        return (String) nodeIdMap.get("id");
    }

    private class LongestPath {

        private Integer length;
        private String terminalNodeId;

        LongestPath(Integer length, String terminalNodeId) {
            this.length = length;
            this.terminalNodeId = terminalNodeId;
        }

        String getTerminalNodeId(){
            return this.terminalNodeId;
        }

        Integer getLength(){
            return this.length;
        }
    }
}
