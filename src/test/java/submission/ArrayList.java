package submission;

public class ArrayList {
    private String[] lst;

    public ArrayList(int capacity) {
        this.lst = new String[capacity];
    }

    public void add(String item) {
        int index = 0;
        while (lst[index] != null) index ++;
        lst[index] = item;
    }

    public int size() {
        int size = 0;
        while (lst[size] != null) size++;
        return size;
    }

    public boolean isEmpty() { return size() == 0; }
}
