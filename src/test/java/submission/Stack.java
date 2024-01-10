package submission;

public class Stack {

    private final int[] stack;
    private int size;

    public Stack(int capacity) {
        stack = new int[capacity];
        size = 0;
    }

    public void push(int item) {
        stack[size] = item;
    }

    public int pop() {
        return stack[size];
    }

    public int size() {
        return size;
    }
}
