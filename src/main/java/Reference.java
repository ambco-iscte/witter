public class Reference {

    /*
    @Test(new int[] { 1, 2, 3, 4, 5 })
    @Test(new int[] { 1, 2 })
    @CountLoopIterations(1)
    @CountRecordAllocations(0)
    @CountArrayAllocations(0)
    @CountArrayReadAccesses(0)
    @CountArrayWriteAccesses(0)
    @CountMemoryUsage(0)
    @TrackVariableStates()
    @CheckParameterImmutability()
     */
    public static int sum(int[] a) {
       int s = 0;
       for (int i = 0; i < a.length; i++)
           s += a[i];
       return s;
    }
}
