package examples.factorial;

public class RecursiveFactorial {

    /*
    @Test(5)
    @CountRecursiveCalls(1)
    @CheckObjectAllocations
    @CheckArrayAllocations
    */
    public static int factorial(int n) {
        if (n == 1) return 1;
        else return n * factorial(n - 1);
    }
}
