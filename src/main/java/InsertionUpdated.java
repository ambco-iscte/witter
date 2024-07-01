public class InsertionUpdated {

    public static void sort(Comparable[] arr) {
        int loops = 0;
        for (int i = 0; i < arr.length; i++) {
            Comparable aux = arr[i];
            int j;
            for (j = i; j > 0 && aux.compareTo(arr[j - 1]) < 0; j--) {
                arr[j] = arr[j - 1];
                loops++;
            }
            arr[j] = aux;
            loops++;
        }
        System.out.println("Loops: " + loops);
    }

    public static void main(String[] args) {
        sort(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        sort(new Integer[] { 7, 3, 2, 1, 5, 6, 10, 8, 9, 4 });
        sort(new Double[] { 7.32, 3.14, 2.14, 1.93, 5.99, 6.74, 10.21, 8.84, 9.26, 4.56 });
        sort(new String[] { "sorting", "algorithms", "are", "really", "very", "cool" });
        sort(new Integer[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 });
    }
}