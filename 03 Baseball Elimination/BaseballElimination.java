import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.SeparateChainingHashST;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Bag;

import java.util.Arrays;

public class BaseballElimination {
    private static class FlowEdge {
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

    private static class FlowNetwork {
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
            if (v < 0 || v >= V)
                throw new IllegalArgumentException("vertex " + v + " must be between 0 and " + (V - 1));
        }
    }

    private static class FordFulkerson {
        private final boolean[] marked;
        private int value;

        public FordFulkerson(FlowNetwork G, int s, int t) {
            FlowNetwork graph = new FlowNetwork(G);
            marked = new boolean[graph.V()];
            FlowEdge[] edgeTo = new FlowEdge[graph.V()];
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

        private boolean hasAugmentingPath(FlowNetwork G, FlowEdge[] edgeTo, int source, int target) {
            Arrays.fill(marked, false);
            Arrays.fill(edgeTo, null);

            Queue<Integer> queue = new Queue<>();
            queue.enqueue(source);
            marked[source] = true;

            // augmenting path strategy: shortest (BFS)
            while (!queue.isEmpty()) {
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

    private static int source(FlowNetwork graph) {
        return graph.V() - 2;
    }

    private static int target(FlowNetwork graph) {
        return graph.V() - 1;
    }

    private final SeparateChainingHashST<String, Integer> teams;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] schedule;
    private int maxWin;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In file = new In(filename);
        int teamNum = Integer.parseInt(file.readLine());

        teams = new SeparateChainingHashST<>();
        wins = new int[teamNum];
        losses = new int[teamNum];
        remaining = new int[teamNum];
        schedule = new int[teamNum][teamNum];
        maxWin = 0;

        for (int i = 0; i < teamNum && file.hasNextLine(); ++i) {
            String line = file.readLine();
            String[] tokens = line.strip().split("\\s+");

            // name win loss remain [schedule...]
            if (tokens.length < teamNum + 4) {
                throw new IllegalArgumentException("Invalid format in input file");
            }

            if (teams.contains(tokens[0])) {
                throw new IllegalArgumentException("Team " + tokens[0] + " already exists");
            }

            teams.put(tokens[0], i);
            wins[i] = Integer.parseInt(tokens[1]);
            losses[i] = Integer.parseInt(tokens[2]);
            remaining[i] = Integer.parseInt(tokens[3]);

            for (int j = 0; j < teamNum; ++j) {
                schedule[i][j] = Integer.parseInt(tokens[4 + j]);
            }

            if (wins[i] > wins[maxWin]) {
                maxWin = i;
            }
        }
    }

    // number of teams
    public int numberOfTeams() {
        return teams.size();
    }

    // all teams
    public Iterable<String> teams() {
        return teams.keys();
    }

    // number of wins for given team
    public int wins(String team) {
        validateTeam(team);
        return wins[teams.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        validateTeam(team);
        return losses[teams.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        validateTeam(team);
        return remaining[teams.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        validateTeam(team1);
        validateTeam(team2);
        return schedule[teams.get(team1)][teams.get(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        validateTeam(team);

        int x = teams.get(team);

        if (wins[x] + remaining[x] < wins[maxWin]) {
            return true;
        }

        FlowNetwork graph = createFlowNetwork(team);
        FordFulkerson solution = new FordFulkerson(graph, source(graph), target(graph));

        for (String s : teams.keys()) {
            int i = teams.get(s);

            // x is always false since no any edge connect to it
            if (solution.inCut(i)) return true;
        }

        return false;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        validateTeam(team);

        int x = teams.get(team);

        // trivial elimination
        if (wins[x] + remaining[x] < wins[maxWin]) {
            Bag<String> bag = new Bag<>();

            for (String s : teams.keys()) {
                if (teams.get(s) == maxWin) {
                    bag.add(s);
                    break;
                }
            }

            return bag;
        }

        FlowNetwork graph = createFlowNetwork(team);
        FordFulkerson solution = new FordFulkerson(graph, source(graph), target(graph));

        Bag<String> bag = new Bag<>();

        for (String s : teams.keys()) {
            int i = teams.get(s);
            if (!solution.inCut(i)) continue;
            bag.add(s);
        }

        if (bag.isEmpty()) return null;

        return bag;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String team : teams.keys()) {
            int i = teams.get(team);
            sb.append(String.format("%10s", team));
            sb.append(String.format("%4d", wins[i]));
            sb.append(String.format("%4d", losses[i]));
            sb.append(String.format("%4d", remaining[i]));

            for (int j = 0; j < teams.size(); ++j) {
                sb.append(String.format("%2d", schedule[i][j]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private void validateTeam(String team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException("Team " + team + " does not exist");
        }
    }

    private FlowNetwork createFlowNetwork(String team) {
        // 1 (source) + 1 (target) + games (exclude team x) + teams (exclude team x)
        int x = teams.get(team);

        // number of remaining games
        int gameNum = (teams.size() - 1) * (teams.size() - 1) - teams.size() + 1;

        // [0, teams.size()) -> teams include team x for convenience
        // [teams.size(), graph.V() - 2) -> games
        FlowNetwork graph = new FlowNetwork(1 + 1 + teams.size() + gameNum);

        int v = teams.size(); // current game vertex

        for (int i = 0; i < teams.size(); ++i) {
            if (i == x) continue;

            graph.addEdge(new FlowEdge(i, target(graph), wins[x] + remaining[x] - wins[i]));

            for (int j = 0; j < teams.size(); ++j) {
                if (j == x) continue;
                if (i == j) continue;

                graph.addEdge(new FlowEdge(source(graph), v, schedule[i][j]));
                graph.addEdge(new FlowEdge(v, i, Integer.MAX_VALUE));
                graph.addEdge(new FlowEdge(v, j, Integer.MAX_VALUE));
                v++;
            }
        }

        return graph;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);

        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}


