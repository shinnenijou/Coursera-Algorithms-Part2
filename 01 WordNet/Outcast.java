import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;

public class Outcast {
    private final WordNet wordNet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordNet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        int[] distance = new int[nouns.length];
        Arrays.fill(distance, 0);

        for (int i = 0; i < nouns.length; i++) {
            for (int j = 0; j < nouns.length; j++) {
                if (i == j) continue;
                distance[i] += wordNet.distance(nouns[i], nouns[j]);
            }
        }

        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < nouns.length; i++) {
            if (distance[i] > max) {
                max = distance[i];
                maxIndex = i;
            }
        }

        return nouns[maxIndex];
    }

    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}