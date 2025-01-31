import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;


public class IndexedMinPQ<Key extends Comparable<Key>, Value extends Comparable<Value>> {
    ArrayList<Node> nodes;

    private class Node {
        Key key;
        Value value;

        Node(Key key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    private int parent(int index){
        return index >> 1;
    }

    private int leftChild(int index){
        return index << 1;
    }

    // compare ith element with jth element
    private boolean less(int i, int j) {
        return nodes.get(i).key.compareTo(nodes.get(j).key) < 0;
    }

    // swap ith element with jth element
    private void swap(int i, int j) {
        Node temp = nodes.get(i);
        nodes.set(i, nodes.get(j));
        nodes.set(j, temp);
    }

    private void swim(int k) {
        while (k > 1 && less(k, parent(k))) {
            swap(k, parent(k));
            k = parent(k);
        }
    }

    private void sink(int k) {
        while (leftChild(k) <= size()){
            int j = leftChild(k);
            if (j < size() && less(j + 1, j)) j++;
            if (less(k, j)) break;
            swap(k, j);
            k = j;
        }
    }

    IndexedMinPQ() {
        nodes = new ArrayList<>();
        nodes.add(null);
    }

    public int size() {
        return nodes.size() - 1;
    }

    public void insert(Key key, Value value) {
        nodes.add(new Node(key, value));
        swim(size());
    }

    public Value pop(){
        if (size() == 0) throw new IllegalStateException();
        Node node = nodes.get(1);
        swap(1, size());
        nodes.remove(size());
        sink(1);
        return node.value;
    }

    public void decreaseKey(Key newKey, Value value) {
        for (int k = 1; k <= size(); k++) {
            if (nodes.get(k).value.compareTo(value) == 0){
                nodes.get(k).key = newKey;
                swim(k);
                break;
            }
        }
    }

    public void increaseKey(Key newKey, Value value) {
        for (int k = 1; k <= size(); k++) {
            if (nodes.get(k).value.compareTo(value) == 0){
                nodes.get(k).key = newKey;
                sink(k);
                break;
            }
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 1; i <= size(); i++) {
            s.append(nodes.get(i).key.toString());
            s.append(" ");
        }

        s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

    public static void main(String[] args) {
        IndexedMinPQ<String, Integer> queue = new IndexedMinPQ<>();

        queue.insert("p", 0);
        queue.insert("o", 1);
        queue.insert("b", 2);
        queue.insert("u", 3);
        queue.insert("a", 4);
        queue.insert("t", 5);
        queue.insert("r", 6);
        queue.insert("y", 7);
        StdOut.println(queue);

        queue.increaseKey("z", 4);
        StdOut.println(queue);

        queue.decreaseKey("a", 4);
        StdOut.println(queue);

        StdOut.println(queue.pop());
        StdOut.println(queue);
        StdOut.println(queue.pop());
        StdOut.println(queue);
        StdOut.println(queue.pop());
        StdOut.println(queue);
        StdOut.println(queue.pop());
        StdOut.println(queue);
    }
}
