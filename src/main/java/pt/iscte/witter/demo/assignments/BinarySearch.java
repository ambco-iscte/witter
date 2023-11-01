package pt.iscte.witter.demo.assignments;

public class BinarySearch {

    /*
    @Test(new int[] { 1, 2, 3, 4, 5, 6, 7 }, 1)
    @Test(new int[] { 1, 3, 7, 9, 11, 13, 17, 19 }, 18)
    @CountLoopIterations
    @CheckSideEffects
     */
    public static int search(int[] a, int e) {
        int l = 0;
        int r = a.length - 1;

        while (l <= r) {
            int m = l + (r - l) / 2;

            if (a[m] == e)
                return m;

            if (a[m] < e)
                l = m + 1;
            else
                r = m - 1;
        }

        return -1;
    }
}
