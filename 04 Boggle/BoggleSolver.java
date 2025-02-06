import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.Stopwatch;

public class BoggleSolver {
    private static String filterQu(String s) {
        return s.replace(QU, QuReplacement);
    }

    private static String recoverQu(String s) {
        return s.replace(QuReplacement, QU);
    }

    private static char filterQu(char c) {
        return c == Q ? QuReplacement.charAt(0) : c;
    }

    private static class Node {
        static public final int R = 26;
        public final char letter;
        public final Node[] children;
        public final int length; //Qu will change the actual length so track a string length from root to current node
        public boolean active;

        Node(char letter, int length) {
            this.letter = letter;
            this.active = false;
            this.children = new Node[26 + 1];
            this.length = length;
        }
    }

    private static final int[] SCORE = {0, 0, 0, 1, 1, 2, 3, 5, 11};
    private static final CharSequence QuReplacement = new String(new char[]{'Z' + 1});
    private static final CharSequence QU = "QU";
    private static final char Q = 'Q';
    private static final int MIN_LENGTH = 3;

    private final Node root;

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        if (dictionary == null) throw new IllegalArgumentException();

        root = new Node('\0', 0);

        for (String s : dictionary) {
            if (s == null) throw new IllegalArgumentException();
            String word = filterQu(s);
            int index = word.charAt(0) - 'A';
            root.children[index] = insert(root.children[index], word, 0, root.length);
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        SET<String> words = new SET<>();
        StringBuilder s = new StringBuilder();
        boolean[] onStack = new boolean[board.cols() * board.rows()];

        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                char firstLetter = filterQu(board.getLetter(row, col));
                getAllValidWords(onStack, board, row, col, s, root.children[firstLetter - 'A'], words);
            }
        }

        SET<String> finalWords = new SET<>();
        for (String word : words) {
            finalWords.add(recoverQu(word));
        }

        return finalWords;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word == null) throw new IllegalArgumentException();
        if (word.isEmpty()) return 0;

        String searchWord = filterQu(word);
        int childIndex = searchWord.charAt(0) - 'A';
        Node target = find(root.children[childIndex], searchWord, 0);

        if (target == null) return 0;

        if (word.length() > SCORE.length - 1) {
            return SCORE[SCORE.length - 1];
        }

        return SCORE[word.length()];
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        SET<String> words = new SET<>();

        for (int i = 0; i < root.children.length; i++) {
            traverse(root.children[i], s, words);
        }

        s.delete(0, s.length());

        for (String word : words) {
            s.append(recoverQu(word)).append('\n');
        }

        return s.toString();
    }

    private void traverse(Node root, StringBuilder s, SET<String> words) {
        if (root == null) return;
        s.append(root.letter);

        if (root.active) {
            words.add(s.toString());
        }

        for (Node child : root.children) {
            traverse(child, s, words);
        }

        s.deleteCharAt(s.length() - 1);
    }

    private Node insert(Node root, String word, int begin, int length) {
        if (begin >= word.length()) return null;

        if (root == null) {
            int currentLength = word.charAt(begin) == QuReplacement.charAt(0) ? length + 2 : length + 1;
            root = new Node(word.charAt(begin), currentLength);
        }

        if (begin + 1 >= word.length()) {
            root.active = true;
            return root;
        }

        int childIndex = word.charAt(begin + 1) - 'A';
        root.children[childIndex] = insert(root.children[childIndex], word, begin + 1, root.length);
        return root;
    }

    private Node find(Node root, String word, int begin) {
        if (root == null) return null;

        if (begin == word.length() - 1) {
            return root.active ? root : null;
        }

        int childIndex = word.charAt(begin + 1) - 'A';
        return find(root.children[childIndex], word, begin + 1);
    }

    // DFS trie and board in lockstep
    private void getAllValidWords(boolean[] onStack, BoggleBoard board, int row, int col, StringBuilder s, Node root, SET<String> words) {
        if (root == null) return;
        if (onStack[row * board.cols() + col]) return;

        char letter = root.letter;
        s.append(letter);
        onStack[row * board.cols() + col] = true;

        if (root.active && root.length >= MIN_LENGTH) {
            words.add(s.toString());
        }

        char nextLetter;

        if (row > 0) {
            // up-left
            if (col > 0) {
                nextLetter = filterQu(board.getLetter(row - 1, col - 1));
                getAllValidWords(onStack, board, row - 1, col - 1, s, root.children[nextLetter - 'A'], words);
            }

            // up
            nextLetter = filterQu(board.getLetter(row - 1, col));
            getAllValidWords(onStack, board, row - 1, col, s, root.children[nextLetter - 'A'], words);

            // up-right
            if (col + 1 < board.cols()) {
                nextLetter = filterQu(board.getLetter(row - 1, col + 1));
                getAllValidWords(onStack, board, row - 1, col + 1, s, root.children[nextLetter - 'A'], words);
            }
        }

        // left
        if (col > 0) {
            nextLetter = filterQu(board.getLetter(row, col - 1));
            getAllValidWords(onStack, board, row, col - 1, s, root.children[nextLetter - 'A'], words);
        }

        // right
        if (col + 1 < board.cols()) {
            nextLetter = filterQu(board.getLetter(row, col + 1));
            getAllValidWords(onStack, board, row, col + 1, s, root.children[nextLetter - 'A'], words);
        }

        if (row + 1 < board.rows()) {
            // down-left
            if (col > 0) {
                nextLetter = filterQu(board.getLetter(row + 1, col - 1));
                getAllValidWords(onStack, board, row + 1, col - 1, s, root.children[nextLetter - 'A'], words);
            }

            // down
            nextLetter = filterQu(board.getLetter(row + 1, col));
            getAllValidWords(onStack, board, row + 1, col, s, root.children[nextLetter - 'A'], words);

            // down-right
            if (col + 1 < board.cols()) {
                nextLetter = filterQu(board.getLetter(row + 1, col + 1));
                getAllValidWords(onStack, board, row + 1, col + 1, s, root.children[nextLetter - 'A'], words);
            }
        }

        onStack[row * board.cols() + col] = false;
        s.deleteCharAt(s.length() - 1);
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);

        // timing
        Stopwatch stopwatch = new Stopwatch();
        int count = 0;

        while (true) {
            if (stopwatch.elapsedTime() > 1) {
                break;
            }

            count++;
            solver.getAllValidWords(new BoggleBoard());
        }

        StdOut.println("Ran on random Hasbro board: " + count);
    }
}