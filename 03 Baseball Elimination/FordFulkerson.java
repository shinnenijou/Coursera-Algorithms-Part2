import java.util.Arrays;
import edu.princeton.cs.algs4.Queue;

public class FordFulkerson {
    private final boolean[] marked;
    private int value;
    private final FlowEdge[] edgeTo;

    public FordFulkerson(FlowNetwork G, int s, int t) {
        FlowNetwork graph = new FlowNetwork(G);
        marked = new boolean[graph.V()];
        edgeTo = new FlowEdge[graph.V()];
        value = 0;

        while (hasAugmentingPath(graph, edgeTo, s, t)) {
            int bottleneck = Integer.MAX_VALUE;

            for (int v = t; v != s; v = edgeTo[v].other(v)) {
                bottleneck = Math.min(edgeTo[v].residualCapacityTo(v), bottleneck);
            }

            for (int v = t; v != s; v = edgeTo[v].other(v)) {
                edgeTo[v].addResidualCapacityTo(v, bottleneck);
            }

            value += bottleneck;
        }
    }

    public boolean inCut(int v){
        return marked[v];
    }

    public int value(){
        return value;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();

        for (int v = 0; v < marked.length; v++){
            s.append("vertex ").append(v).append(": ").append(marked[v]).append("\n");
        }

        s.append("Total value: ").append(value).append("\n");

        return s.toString();
    }

    private boolean hasAugmentingPath(FlowNetwork G, FlowEdge[] edgeTo, int source, int target){
        Arrays.fill(marked, false);
        Arrays.fill(edgeTo, null);

        Queue<Integer> queue = new Queue<>();
        queue.enqueue(source);
        marked[source] = true;

        // augmenting path strategy: shortest (BFS)
        while (!queue.isEmpty()){
            int v = queue.dequeue();

            for (FlowEdge e : G.adj(v)) {
                int w = e.other(v);
                if (marked[w] || e.residualCapacityTo(w) <= 0) continue;
                marked[w] = true;
                edgeTo[w] = e;
                queue.enqueue(w);
            }
        }

        return marked[target];
    }
}
