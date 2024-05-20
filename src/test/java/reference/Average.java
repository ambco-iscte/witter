package reference;

public class Average {

    public static double average(double[] a) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) sum += a[i];
        return sum / a.length;
    }
}
