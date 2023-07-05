public class Subject {

    public static int sum(int[] a) {
        int s = 0;
        int b[] = new int[10];
        for (int i = 0; i < a.length; i++)
            s += a[i];
        for (int j = 0; j < a.length; j++)
            s += 0;
        return s;
    }
}
