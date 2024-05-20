package reference;

public class StackDissertation {

    private int[] stack;
    private int size;

    public StackDissertation(int capacity) {
        stack = new int[capacity];
    }

    public void push(int item) {
        stack[size] = item;
        size += 1;
    }

    public int size() {
        return size;
    }
}
