public class Reference {

    /*
    @Test(new int[] { 1, 2, 3, 4, 5 })
    @Test(new int[] { 1, 2 })
    @CountLoopIterations(1)
    @CountRecordAllocations(0)
    @CountArrayAllocations(0)
    #* TODO: Array read accesses go here when implemented *#
    @CountArrayWriteAccesses(0)
    #* TODO: memory usage goes here when weird UnboundType error is fixed *#
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
