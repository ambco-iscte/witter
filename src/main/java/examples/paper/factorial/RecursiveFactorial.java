package examples.paper.factorial;

public class RecursiveFactorial {

    /*
    @Test(5)
    @CountRecursiveCalls(0)
    */
    public static int factorial(int n) {
        if (n == 0) return 1;
        else return n * factorial(n - 1);
    }
}
