package reference;

import java.util.Arrays;

public class HeapSorting {
    
    public static void sort(Comparable[] a) {
        int N = a.length;
        
        heapify(a, N);
        
        sortdown(a, N);
    }
    
    public static void heapify(Comparable[] a, int N) {
        for(int k = N/2; k >= 1; k--) {
            sink(a, k, N);
        }
    }
    
    public static void sortdown(Comparable[] a, int N) {
        while(N > 1) {
            exchange(a, 0, N - 1);
            N--;
            sink(a, 1, N);
        }
    }
    
    public static void sink(Comparable[] a, int k, int N) {
        while( 2*k <= N) {
            int j = 2*k;
            
            if(j < N && less(a, j - 1, j)) {
                j++;
            }
            
            if(!less(a, k - 1, j - 1)) {
                break;
            }
            exchange(a, k - 1, j - 1);
            k = j;
        }
    }
    
    public static int parent(int k) {
        return k/2;
    }
    
    public static int left(int k) {
        return 2*k;
    }
    
    public static int right(int k) {
        return 2*k + 1;
    }
    
    private static void exchange(Comparable[] a, int i, int j) {
        Comparable aux = a[i];
        a[i] = a[j];
        a[j] = aux;
    }
    
    private static boolean less(Comparable[] a, int i, int j) {
        return a[i].compareTo(a[j]) < 0;
    }
}