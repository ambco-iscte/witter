package submission;

public class SelectionSort {

    public static void sort(int[] a) {
        int N = a.length;
        for (int i = 0; i < N - 1; i++) {
            int min = i;
            for (int j = i + 1; j < N; j++)
                if (a[j] < a[min])
                    min = j;

            int tmp = a[i];
            a[i] = a[min];
            a[min] = tmp;
        }
    }
}
