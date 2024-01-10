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
        return stack[size];
    }

    public int size() {
        return size;
    }
}
