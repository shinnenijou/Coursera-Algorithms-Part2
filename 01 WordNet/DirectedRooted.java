import edu.princeton.cs.algs4.Digraph;

public class DirectedRooted {
    private int rootCandidate;
    private Digraph G;
    private boolean isRooted;
    private boolean[] marked;

    private void dfs(int v) {
        if (!isRooted) return;
        marked[v] = true;

        // may be root
        if (G.outdegree(v) == 0) {
            if (rootCandidate == -1) rootCandidate = v;
            else if (rootCandidate != v) isRooted = false;
            return;
        }

        for (int w : G.adj(v)) {
            if (marked[w]) continue;
            dfs(w);
        }
    }

    DirectedRooted(Digraph digraph) {
        if (digraph == null)
            throw new NullPointerException("digraph is null");

        if (digraph.V() == 0) {
            isRooted = false;
            return;
        }

        rootCandidate = -1;
        G = digraph;
        marked = new boolean[digraph.V()];
        isRooted = true;

        for (int v = 0; v < digraph.V(); v++) {
            if (marked[v]) continue;
            dfs(v);
        }
    }

    public boolean isRooted() {
        return isRooted;
    }
}
