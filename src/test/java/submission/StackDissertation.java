package submission;

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
        int s = 0;
        for (int i = 0; i < stack.length; i++)
            if (stack[i] != 0) s += 1;
        return s;
    }
}
