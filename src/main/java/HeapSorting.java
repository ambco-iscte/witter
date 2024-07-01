import java.util.Arrays;

public class HeapSorting { // Updated

	private static int arrayReads = 0;

	public static void sort(Comparable[] a) {
		arrayReads = 0;
		int N = a.length - 1;
		heapify(a, N);
		sortdown(a, N);
		System.out.println("Array reads: " + arrayReads);
	}
	
	private static void heapify(Comparable[] a, int N) {
		for (int k = parent(N); k >= 0; k--)
			sink(a, k, N);
	}
	
	private static void sortdown(Comparable[] a, int N) {
		while (N > 0) {
			exchange(a, 0, N);
			N--;
			sink(a, 0, N);
		}
	}
	
	// Parent of node k
	private static int parent(int k) {
		return k / 2;
	}
	
	// Left child of node k
	private static int left(int k) {
		return 2 * k + 1;
	}
	
	// Right child of node k
	private static int right(int k) {
		return left(k) + 1;
	}
	
	private static void sink(Comparable[] a, int k, int N) {
		while (left(k) <= N) {
			int biggestChild = left(k);
			if (right(k) <= N && less(a, left(k), right(k)))
				biggestChild = right(k);
			
			if (!less(a, k, biggestChild)) break;
			exchange(a, k, biggestChild);
			k = biggestChild;
		}
	}
	
	private static boolean less(Comparable[] a, int i, int j) {
		arrayReads += 2;
		return a[i].compareTo(a[j]) <= 0;
	}
	
	private static void exchange(Object[] a, int i, int j) {
		Object swap = a[i];
		a[i] = a[j];
		a[j] = swap;
		arrayReads += 2;
	}

	public static void main(String[] args) {
		sort(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
		sort(new Integer[] { 7, 3, 2, 1, 5, 6, 10, 8, 9, 4 });
		sort(new Double[] { 7.32, 3.14, 2.14, 1.93, 5.99, 6.74, 10.21, 8.84, 9.26, 4.56 });
		sort(new String[] { "sorting", "algorithms", "are", "really", "very", "cool" });
		sort(new Integer[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 });
	}
}
