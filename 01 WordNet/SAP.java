import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;

import edu.princeton.cs.algs4.Queue;

import java.util.ArrayList;
import java.util.Arrays;


public class SAP {
    private static final boolean UNVISITED = false;
    private static final boolean VISITED = true;

    // helper class to return two value from function call
    private static class Pair {
        public int first;
        public int second;

        public Pair(int x, int y) {
            this.first = x;
            this.second = y;
        }
    }

    private Digraph copy(Digraph digraph) {
        Digraph newDigraph = new Digraph(digraph.V());
        for (int v = 0; v < digraph.V(); v++){
            for (int w : digraph.adj(v)){
                newDigraph.addEdge(v, w);
            }
        }

        return newDigraph;
    }

    private final Digraph G;
    private final boolean[] marked;
    private final Queue<Integer> queue;
    private final int[] distToA;
    private final int[] distToB;

    // helper container to transfer parameter
    private final ArrayList<Integer> listA;
    private final ArrayList<Integer> listB;

    private void validateVertex(Integer v) {
        if (v == null || v < 0 || v >= G.V())
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (G.V() - 1));
    }

    private void bfs(Iterable<Integer> initSet, int[] distTo) {
        Arrays.fill(distTo, -1);
        Arrays.fill(marked, UNVISITED);
        while (!queue.isEmpty()) {
            queue.dequeue();
        }

        for (int v : initSet) {
            queue.enqueue(v);
            distTo[v] = 0;
        }

        while (!queue.isEmpty()) {
            int v = queue.dequeue();
            marked[v] = VISITED;

            for (int w : G.adj(v)) {
                if (marked[w] == VISITED) continue;
                distTo[w] = distTo[v] + 1;
                queue.enqueue(w);
            }
        }
    }

    private Pair ancestorImpl(Iterable<Integer> setA, Iterable<Integer> setB) {
        for (Integer v : setA) {
            validateVertex(v);
        }

        for (Integer v : setB) {
            validateVertex(v);
        }

        bfs(setA, distToA);
        bfs(setB, distToB);

        Pair p = new Pair(-1, Integer.MAX_VALUE);

        for (int i = 0; i < distToA.length; i++) {
            if (distToA[i] > -1 && distToB[i] > -1 && distToA[i] + distToB[i] < p.second) {
                p.first = i;
                p.second = distToA[i] + distToB[i];
            }
        }

        if (p.second == Integer.MAX_VALUE) p.second = -1;

        return p;
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph graph) {
        if (graph == null) throw new IllegalArgumentException("graph == null");

        G = copy(graph);
        marked = new boolean[G.V()];
        distToA = new int[G.V()];
        distToB = new int[G.V()];
        queue = new Queue<>();
        listA = new ArrayList<>();
        listB = new ArrayList<>();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        listA.clear();
        listA.add(v);
        listB.clear();
        listB.add(w);
        return ancestorImpl(listA, listB).second;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        listA.clear();
        listA.add(v);
        listB.clear();
        listB.add(w);
        return ancestorImpl(listA, listB).first;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> setA, Iterable<Integer> setB) {
        return ancestorImpl(setA, setB).second;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> setA, Iterable<Integer> setB) {
        return ancestorImpl(setA, setB).first;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        StdOut.println(G);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
