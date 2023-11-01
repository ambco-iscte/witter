package pt.iscte.witter.examples.paper.sorting;

public class InsertionSort {

    /*
    @Test(new int[] { 5, 4, 3, 2, 1 })
    @CountArrayReads
    @CountArrayWrites
    @CheckSideEffects
    */
    public static void sort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            for (int j = i; j > 0; j--) {
                if (a[j] >= a[j - 1])
                    break;
                int tmp = a[j];
                a[j] = a[j - 1];
                a[j - 1] = tmp;
            }
        }
    }
}
