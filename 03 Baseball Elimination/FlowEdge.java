public class FlowEdge {
    private final int from;
    private final int to;
    private final int capacity;
    private int flow;

    public FlowEdge(int v, int w, int capacity) {
        this.from = v;
        this.to = w;
        this.capacity = capacity;
        this.flow = 0;
    }

    public FlowEdge(FlowEdge e) {
        this.from = e.from;
        this.to = e.to;
        this.capacity = e.capacity;
        this.flow = 0;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public int other(int v) {
        if (v == from) return to;
        else if (v == to) return from;
        else throw new IllegalArgumentException();
    }

    public int capacity() {
        return capacity;
    }

    public int flow() {
        return flow;
    }

    public int residualCapacityTo(int v) {
        if (v == to) return capacity - flow;
        else if (v == from) return flow;
        else throw new IllegalArgumentException();
    }

    public void addResidualCapacityTo(int v, int delta) {
        if (v == to) flow += delta;
        else if (v == from) flow -= delta;
        else throw new IllegalArgumentException();
    }
}
