package reference;

public class Stack {

    private final int[] stack;
    private int size;

    public Stack(int capacity) {
        stack = new int[capacity];
        size = 0;
    }

    public void push(int item) {
        stack[size] = item;
        size += 1;
    }

    public int pop() {
        size -= 1;
        int elem = stack[size];
        stack[size] = 0;
        return elem;
    }

    public int size() {
        return size;
    }
}
