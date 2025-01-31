import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;

import edu.princeton.cs.algs4.Queue;

import java.util.ArrayList;
import java.util.Arrays;


public class SAP {
    private static final char UNVISITED = 0;
    private static final char PASS1VISITED = 1;
    private static final char PASS2VISITED = 2;

    // helper class to return two value from function call
    private static class Pair {
        public int first;
        public int second;

        public Pair(int x, int y) {
            this.first = x;
            this.second = y;
        }
    }

    private final Digraph G;
    private final char[] marked;
    private final int[] distTo;
    private final Queue<Integer> queue;

    // help container to transfer parameter
    private final ArrayList<Integer> listA;
    private final ArrayList<Integer> listB;

    private void validateVertex(int v) {
        if (v < 0 || v >= G.V())
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (G.V() - 1));
    }

    // init before every query
    private void init() {
        Arrays.fill(marked, UNVISITED);

        Arrays.fill(distTo, -1);

        while (!queue.isEmpty()) {
            queue.dequeue();
        }

        listA.clear();
        listB.clear();
    }

    private void bfs1pass(Iterable<Integer> initSet) {
        for (int v : initSet) {
            queue.enqueue(v);
            distTo[v] = 0;
        }

        while (!queue.isEmpty()) {
            int v = queue.dequeue();
            marked[v] = PASS1VISITED;

            for (int w : G.adj(v)) {
                if (marked[w] == PASS1VISITED) continue;
                distTo[w] = distTo[v] + 1;
                queue.enqueue(w);
            }
        }
    }

    private Pair bfs2pass(Iterable<Integer> initSet) {
        for (int v : initSet) {
            // special case: ancestor is in B, leading to overwrite distTo[v] incorrectly
            if (marked[v] == PASS1VISITED) {
                return new Pair(v, distTo[v]);
            }
            queue.enqueue(v);
            distTo[v] = 0;
        }

        while (!queue.isEmpty()) {
            int v = queue.dequeue();
            marked[v] = PASS2VISITED;

            for (int w : G.adj(v)) {
                if (marked[w] == PASS2VISITED) continue;
                if (marked[w] == PASS1VISITED) {
                    return new Pair(w, distTo[w] + distTo[v] + 1);
                }
                distTo[w] = distTo[v] + 1;
                queue.enqueue(w);
            }
        }

        return new Pair(-1, -1);
    }

    private Pair ancestorImpl(Iterable<Integer> A, Iterable<Integer> B) {
        for (int v : A) {
            validateVertex(v);
        }

        for (int v : B) {
            validateVertex(v);
        }

        bfs1pass(A);
        return bfs2pass(B);
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph graph) {
        if (graph == null) throw new IllegalArgumentException("graph == null");

        G = graph;
        marked = new char[G.V()];
        distTo = new int[G.V()];
        queue = new Queue<>();
        listA = new ArrayList<>();
        listB = new ArrayList<>();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        init();
        listA.add(v);
        listB.add(w);
        return ancestorImpl(listA, listB).second;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        init();
        listA.add(v);
        listB.add(w);
        return ancestorImpl(listA, listB).first;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> A, Iterable<Integer> B) {
        init();
        return ancestorImpl(A, B).second;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> A, Iterable<Integer> B) {
        init();
        return ancestorImpl(A, B).first;
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
