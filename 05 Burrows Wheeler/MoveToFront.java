import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class MoveToFront {
    private static class SequencedChars{
        private final char[] chars;

        SequencedChars(int size){
            chars = new char[size];

            for(int i = 0; i < R; ++i){
                chars[i] = (char)(i);
            }
        }

        private int insert(char c){
            int index = 0;
            while (chars[index] != c) ++index;
            System.arraycopy(chars, 0, chars, 1, index);
            chars[0] = c;

            return index;
        }

        private char get(int index){
            char c = chars[index];
            System.arraycopy(chars, 0, chars, 1, index);
            chars[0] = c;
            return c;
        }
    }

    private static final int R = 256; // extended ASCII

    // apply move-to-front encoding, reading from standard input and writing to standard output
    public static void encode(){
        SequencedChars chars = new SequencedChars(R);

        char c = StdIn.readChar();
        int pos = chars.insert(c);
        StdOut.print(pos);

        while(!StdIn.isEmpty()){
            c = StdIn.readChar();
            pos = chars.insert(c);
            StdOut.print(' ');
            StdOut.print(pos);
        }
    }

    // apply move-to-front decoding, reading from standard input and writing to standard output
    public static void decode(){
        SequencedChars chars = new SequencedChars(R);

        int index = StdIn.readInt();
        char c = chars.get(index);
        StdOut.print(c);

        while(!StdIn.isEmpty()){
            index = StdIn.readInt();
            c = chars.get(index);
            StdOut.print(' ');
            StdOut.print(c);
        }
    }

    // if args[0] is "-", apply move-to-front encoding
    // if args[0] is "+", apply move-to-front decoding
    public static void main(String[] args){
        if (args.length < 1) throw new IllegalArgumentException("Usage : java MoveToFront (-|+)");

        if (args[0].equals("-")) {
            encode();
        }
        else if (args[0].equals("+")) {
            decode();
        }
        else {
            throw new IllegalArgumentException("Usage : java MoveToFront (-|+)");
        }
    }
}