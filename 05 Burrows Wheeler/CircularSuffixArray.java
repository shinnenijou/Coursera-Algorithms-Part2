import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;

public class CircularSuffixArray {

    private static class StringWrapper {
        public final String s;
        public final int originIndex;

        StringWrapper(String s, int index){
            this.s = s;
            this.originIndex = index;
        }

        public char charAt(int index){
            return s.charAt(index);
        }
    }

    private final int R = 256; // extended ASCII
    private final String[] originSuffixes; // maybe not need
    private final StringWrapper[] nodes;

    // circular suffix array of s
    public CircularSuffixArray(String s) {
        if (s == null) {
            throw new IllegalArgumentException("s cannot be null");
        }

        final int N = s.length();

        originSuffixes = new String[N];
        nodes = new StringWrapper[N];

        StringBuilder builder = new StringBuilder(s);

        for (int i = 0; i < N; ++i){
            char c = builder.charAt(0);
            originSuffixes[i] = builder.toString();
            builder.deleteCharAt(0);
            builder.append(c);
        }

        for (int i = 0; i < N; ++i){
            nodes[i] = new StringWrapper(originSuffixes[i], i);
        }

        // aux array
        StringWrapper[] aux = new StringWrapper[N];
        int[] count = new int[R + 1];
        int CHAR_OFFSET = R / 2;

        // LSD sort
        for (int i = N - 1; i >= 0; --i){
            Arrays.fill(count, 0);

            for (int j = 0; j < N; ++j){
                char c = nodes[j].charAt(i);
                ++count[c + 1 + CHAR_OFFSET];
            }

            for (int j = 1; j < count.length; ++j){
                count[j] += count[j - 1];
            }

            for (int j = 0; j < N; ++j){
                char c = nodes[j].charAt(i);
                aux[count[c + CHAR_OFFSET]++] = nodes[j];
            }

            for (int j = 0; j < N; ++j){
                nodes[j] = aux[j];
            }
        }
    }

    // length of s
    public int length() {
        return nodes.length;
    }

    // returns index of ith sorted suffix
    public int index(int i) {
        validateIndex(i);
        return nodes[i].originIndex;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("Length: ").append(length()).append("\n");
        String format = "%" + (length() + 2) + "s";

        for (int i = 0; i < length(); i++) {
            s.append(String.format(format, originSuffixes[i]));
            s.append(String.format(format, nodes[i].s));
            s.append(String.format("%5d", nodes[i].originIndex));
            s.append('\n');
        }

        return s.toString();
    }

    private void validateIndex(int i) {
        if (i < 0 || i >= length())
            throw new IndexOutOfBoundsException("Index out of bounds: [0, " + length() + ")");
    }

    // unit testing (required)
    public static void main(String[] args) {
        In file = new In(args[0]);
        String s = file.readAll();
        CircularSuffixArray suffixArray = new CircularSuffixArray(s);
        StdOut.println("length: " + suffixArray.length());
        StdOut.println(suffixArray);
    }
}
