import edu.princeton.cs.algs4.Queue;

import java.util.Arrays;

public class MyFordFulkerson {
    private final boolean[] marked;
    private int value;

    public MyFordFulkerson(MyFlowNetwork G, int s, int t) {
        MyFlowNetwork graph = new MyFlowNetwork(G);
        marked = new boolean[graph.V()];
        MyFlowEdge[] edgeTo = new MyFlowEdge[graph.V()];
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

    public boolean inCut(int v) {
        validateVertex(v);
        return marked[v];
    }

    public int value() {
        return value;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int v = 0; v < marked.length; v++) {
            s.append("vertex ").append(v).append(": ").append(marked[v]).append("\n");
        }

        s.append("Total value: ").append(value).append("\n");

        return s.toString();
    }

    private boolean hasAugmentingPath(MyFlowNetwork G, MyFlowEdge[] edgeTo, int source, int target) {
        Arrays.fill(marked, false);
        Arrays.fill(edgeTo, null);

        Queue<Integer> queue = new Queue<>();
        queue.enqueue(source);
        marked[source] = true;

        // augmenting path strategy: shortest (BFS)
        while (!queue.isEmpty()) {
            int v = queue.dequeue();

            for (MyFlowEdge e : G.adj(v)) {
                int w = e.other(v);
                if (marked[w] || e.residualCapacityTo(w) <= 0) continue;
                marked[w] = true;
                edgeTo[w] = e;
                queue.enqueue(w);
            }
        }

        return marked[target];
    }

    private void validateVertex(int v) {
        if (v < 0 || v > marked.length) {
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (marked.length - 1));
        }
    }
}
