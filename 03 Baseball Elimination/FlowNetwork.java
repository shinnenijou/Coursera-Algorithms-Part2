import edu.princeton.cs.algs4.Bag;

public class FlowNetwork {
    private final int V;
    private int E;
    private final Bag<FlowEdge>[] vertices;

    public FlowNetwork(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices is negative");
        this.V = V;
        this.E = 0;
        this.vertices = (Bag<FlowEdge>[]) new Bag[V];

        for (int i = 0; i < V; i++) {
            vertices[i] = new Bag<>();
        }
    }

    public FlowNetwork(FlowNetwork other) {
        this.V = other.V;
        this.E = other.E;
        this.vertices = (Bag<FlowEdge>[]) new Bag[this.V];

        for (int i = 0; i < this.V; i++) {
            this.vertices[i] = new Bag<>();
        }

        for (int v = 0; v < this.V; v++) {
            for (FlowEdge e : other.vertices[v]) {
                FlowEdge edge = new FlowEdge(e);
                E++;
                this.vertices[edge.to()].add(edge);
                this.vertices[edge.from()].add(edge);
            }
        }
    }

    public int V() {
        return V;
    }

    public int E() {
        return E;
    }

    public void addEdge(FlowEdge edge) {
        validateVertex(edge.from());
        validateVertex(edge.to());
        E++;
        vertices[edge.to()].add(edge);
        vertices[edge.from()].add(edge);
    }

    public Iterable<FlowEdge> adj(int v) {
        validateVertex(v);
        return vertices[v];
    }

    public Iterable<FlowEdge> edges() {
        Bag<FlowEdge> bag = new Bag<>();

        for (int i = 0; i < V; i++) {
            for (FlowEdge e : adj(i)) {
                bag.add(e);
            }
        }

        return bag;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V) throw new IllegalArgumentException("vertex " + v + " must be between 0 and " + (V - 1));
    }
}
