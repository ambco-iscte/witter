package examples;

public class Subject {

    public static int sum(int[] a) {
        int[] b = new int[10];

        // -------------------------------
        int s = 0;
        for (int i = 0; i < a.length; i++)
            s += a[i] + a[i];
        // -------------------------------

        for (int j = 0; j < a.length; j++)
            s += 0;
        a = b;

        a[1] = 3;

        return s;
    }
}
