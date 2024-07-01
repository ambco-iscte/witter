package reference;

public class ArrayList {
    private String[] lst;
    private int size;

    public ArrayList(int capacity) {
        this.lst = new String[capacity];
    }

    public void add(String item) { lst[size++] = item; }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }
}
