package submission;

public class StackDissertation {

    private int[] st;
    private int size;

    public StackDissertation(int capacity) {
        st = new int[capacity];
    }

    public void push(int item) {
        st[size] = item;
        size += 1;
    }

    public int size() {
        int s = 0;
        for (int i = 0; i < st.length; i++)
            if (st[i] != 0) s += 1;
        return s;
    }
}
