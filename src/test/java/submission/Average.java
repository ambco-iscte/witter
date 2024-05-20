package submission;

public class Average {

    public static double average(double[] a) {
        double sum = 0.0;
        for (int i = 1; i < a.length; i++) sum += a[i];
        return sum / a.length;
    }
}
