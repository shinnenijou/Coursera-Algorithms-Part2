import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.SeparateChainingHashST;
import edu.princeton.cs.algs4.DirectedCycle;

import java.util.ArrayList;

public class WordNet {

    // noun -> vertex indexes(notice that noun may appear in multiple synset)
    private final SeparateChainingHashST<String, Bag<Integer>> nouns;

    // vertex index -> synset(i.e., vertex in graph)
    private final ArrayList<SET<String>> synsets;

    // Graph data structure
    private final Digraph digraph;

    // SAP(Shortest Ancestral Path) implementation
    private final SAP sap;

    // define vertex
    private void readSynsets(String filename) {
        In file = new In(filename);

        while (!file.isEmpty()) {
            String line = file.readLine();
            String[] fields = line.split(",");

            int index = Integer.parseInt(fields[0]);
            assert index == synsets.size();

            String[] words = fields[1].split("\\s");
            SET<String> set = new SET<>();

            for (String word : words) {
                // add to synsets
                set.add(word);

                // add to st
                if (!nouns.contains(word)) {
                    nouns.put(word, new Bag<>());
                }
                nouns.get(word).add(index);
            }

            synsets.add(set);
        }
    }

    private void readHypernyms(String filename) {
        In file = new In(filename);

        while (!file.isEmpty()) {
            String line = file.readLine();
            String[] fields = line.split(",");

            int v = Integer.parseInt(fields[0]);

            for (int i = 1; i < fields.length; i++) {
                digraph.addEdge(v, Integer.parseInt(fields[i]));
            }
        }
    }

    public String toString() {
        return digraph.toString();
    }

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null) {
            throw new IllegalArgumentException("synsets is null");
        }

        if (hypernyms == null) {
            throw new IllegalArgumentException("hypernyms is null");
        }

        this.nouns = new SeparateChainingHashST<>();

        this.synsets = new ArrayList<>();
        readSynsets(synsets);

        digraph = new Digraph(this.synsets.size());
        readHypernyms(hypernyms);

        // check acyclic
        DirectedCycle dc = new DirectedCycle(digraph);
        if (dc.hasCycle()) {
            throw new IllegalArgumentException("cycle found");
        }

        // check rooted
        DirectedRooted dr = new DirectedRooted(digraph);
        if (!dr.isRooted()) {
            throw new IllegalArgumentException("not rooted");
        }

        sap = new SAP(digraph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns.keys();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException("word is null");
        }

        return nouns.contains(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA)) {
            throw new IllegalArgumentException("Not WordNet noun: " + nounA);
        }

        if (!isNoun(nounB)) {
            throw new IllegalArgumentException("Not WordNet noun: " + nounB);
        }

        return sap.length(nouns.get(nounA), nouns.get(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA)) {
            throw new IllegalArgumentException("Not WordNet noun: " + nounA);
        }

        if (!isNoun(nounB)) {
            throw new IllegalArgumentException("Not WordNet noun: " + nounB);
        }

        int ancestor = sap.ancestor(nouns.get(nounA), nouns.get(nounB));
        SET<String> set = synsets.get(ancestor);

        StringBuilder s = new StringBuilder();
        for (String word : set) {
            s.append(word).append(" ");
        }
        s.deleteCharAt(s.length() - 1);

        return s.toString();
    }

    // do unit testing of this class
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: java WordNet <synsets> <hypernyms>");
        }

        WordNet wn = new WordNet(args[0], args[1]);
        StdOut.println(wn);

        for (String noun : wn.nouns()) {
            wn.isNoun(noun);

            for (String thatNoun : wn.nouns()) {
                StdOut.println("Distance between " + noun + " and " + thatNoun + ": " + wn.distance(noun, thatNoun));
                StdOut.println("SAP between " + noun + " and " + thatNoun + ": " + wn.sap(noun, thatNoun));
            }
        }
    }
}