package examples.binarysearch;

public class LinearSearch {

    public static int search(int[] a, int e) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == e)
                return i;
        }
        return -1;
    }
}
