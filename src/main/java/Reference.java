public class IntegerInsertionSortReference {

   public static int[] sorted(int[] a) {
       int[] b = new int[a.length];
       for (int i = 0; i < a.length; i++)
           for (int j = i; j > 0 && a[j] < a[j - 1]; j--) {
               int t = a[i];
               a[i] = a[j];
               a[j] = t;
           }
       return b;
   }
}
