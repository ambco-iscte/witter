package submission;

public class IterativeFactorial {

    public static int factorial(int n) {
        int f = 1;
        for (int i = 0; i <= n; i++)
            f *= i; // i starts at 0, f = 0 always
        int[] a = new int[10];
        double[] b = new double[10];
        return f;
    }
}
