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

    private static Digraph copy(Digraph digraph) {
        Digraph newDigraph = new Digraph(digraph.V());
        for (int v = 0; v < digraph.V(); v++) {
            for (int w : digraph.adj(v)) {
                newDigraph.addEdge(v, w);
            }
        }

        return newDigraph;
    }

    private static void clean(int[] distTo, ArrayList<Integer> cleaner) {
        for (Integer v : cleaner){
            distTo[v] = -1;
        }

        cleaner.clear();
    }

    private final Digraph G;
    private final boolean[] marked;
    private final Queue<Integer> queue;
    private final int[] distToA;
    private final int[] distToB;

    // helper container to transfer parameter
    private final ArrayList<Integer> listA;
    private final ArrayList<Integer> listB;

    // helper container to re-initialize dist array
    private final ArrayList<Integer> cleanerA;
    private final ArrayList<Integer> cleanerB;

    private void validateVertexSet(Iterable<Integer> set) {
        if (set == null) {
            throw new IllegalArgumentException("vertex set is null");
        }

        for (Integer v : set) {
            if (v == null || v < 0 || v >= G.V()){
                throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (G.V() - 1));
            }
        }
    }

    private void bfs(Iterable<Integer> initSet, int[] distTo, ArrayList<Integer> cleaner) {
        for (int v : initSet) {
            queue.enqueue(v);
            marked[v] = VISITED;
            distTo[v] = 0;
            cleaner.add(v);
        }

        while (!queue.isEmpty()) {
            int v = queue.dequeue();

            for (int w : G.adj(v)) {
                if (marked[w] == VISITED) continue;
                marked[w] = VISITED;
                distTo[w] = distTo[v] + 1;
                cleaner.add(w);
                queue.enqueue(w);
            }
        }

        for (int v : cleaner) {
            marked[v] = UNVISITED;
        }
    }

    private Pair ancestorImpl(Iterable<Integer> setA, Iterable<Integer> setB) {
        validateVertexSet(setA);
        validateVertexSet(setB);

        bfs(setA, distToA, cleanerA);
        bfs(setB, distToB, cleanerB);

        Pair p = new Pair(-1, Integer.MAX_VALUE);

        for (int v: cleanerA) {
            if (distToA[v] > -1 && distToB[v] > -1 && distToA[v] + distToB[v] < p.second) {
                p.first = v;
                p.second = distToA[v] + distToB[v];
            }
        }

        for (int v: cleanerB) {
            if (distToA[v] > -1 && distToB[v] > -1 && distToA[v] + distToB[v] < p.second) {
                p.first = v;
                p.second = distToA[v] + distToB[v];
            }
        }

        clean(distToA, cleanerA);
        clean(distToB, cleanerB);

        if (p.second == Integer.MAX_VALUE) p.second = -1;

        return p;
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph graph) {
        if (graph == null) throw new IllegalArgumentException("graph == null");

        G = copy(graph);
        marked = new boolean[G.V()];
        distToA = new int[G.V()];
        Arrays.fill(distToA, -1);
        distToB = new int[G.V()];
        Arrays.fill(distToB, -1);
        queue = new Queue<>();
        listA = new ArrayList<>();
        listB = new ArrayList<>();
        cleanerA = new ArrayList<>();
        cleanerB = new ArrayList<>();
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
