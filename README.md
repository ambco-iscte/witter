<div align="center">

# Witter
**Witter** is a software testing library that allows programming educators to define white-box
tests for Java source code. Witter analyzes the execution of a method against a reference
solution, to verify that the code not only produces correct results but is also in
accordance with a desired algorithm behaviour.

[Specifying reference solutions](#specifying-reference-solutions) •
[Testing arbitrary solutions](#testing-arbitrary-solutions) •
[Examples](#examples)

</div>

<br>

## Specifying reference solutions
One can define the test cases for a given exercise by writing
a reference solution in a Java method, annotated with a
header comment that defines the different test inputs and
the metrics that should be measured during test execution.
The content of the comments has to obey Witter’s Test Specification
Language (TSL), whose syntax is similar to Java’s
annotation syntax:
```java
/*
@Test ({1 , 2, 3, 4, 5})
@Test ({2 , 4, 6})
@CountLoopIterations
@CountArrayReads
@CheckSideEffects
*/
public static int sum( int [] a) { 
    ... 
}
```

Witter currently supports the following runtime metrics.

| **Metric**         | **Annotation**                      | **Verification**                                                                                  |
|--------------------|-------------------------------------|---------------------------------------------------------------------------------------------------|
| Return values      | @Test(_[...args]_)                  | Return value is equal to reference solution. Multiple annotations can be used.                    |
| Side effects       | @CheckSideEffects                   | Side effects on arguments (presence and absence) are the same to those of the reference solution. |
| Loop iterations    | @CountLoopIterations(_[threshold]_) | Total number of loop iterations matches the one of the reference solution.                        |
| Array allocations  | @CheckArrayAllocations              | The array allocations match those of the reference solution (component types and lengths).        |
| Array reads        | @CountArrayReads(_[threshold]_)     | The number of array read accesses is the same as in the reference solution.                       |
| Array writes       | @CountArrayWrites(_[threshold]_)    | The number of array write accesses is the same as in the reference solution.                      |
| Object allocations | @CheckObjectAllocations             | The number of object allocations and their types match those of the reference solution.           |
| Recursive calls    | @CountRecursiveCalls(_[threshold]_) | The number of recursive calls matches the one of the reference solution.                          |

<br>

## Testing arbitrary solutions
As Witter is designed for third-party integration, it provides
a form of executing the tests programmatically. Tests are executed providing an annotated reference
solution as described, and a solution that one wishes to assess:
```java
Test test = new Test("ReferenceSolution.java")
        
List<TestResult> results = test.execute("Solution.java")
```

The test results consist of a list of feedback
items for each aspect defined in the test specification,
holding the following information:
- a flag indicating success or failure;
- which kind of metric has failed (recall Table 1);
- the location of code elements involved in the failed tests (e.g., procedure, parameters, loop structures);
- a human-readable descriptive feedback message.

<br>

## Examples
### Factorial (recursive)
Reference solution with recursion:
```java
/*
@Test (5)
@CountRecursiveCalls (1)
*/
static int factorial(int n) {
    if (n == 0) return 1;
    else return n * factorial (n - 1);
}
```

Solution under testing (iterative, with a defect):
```java
static int factorial(int n) {
    int f = 1;
    for (int i = 0; i <= n; i++)
        f *= i; // i starts at 0, f always 0
    return f;
}
```

Witter test results (black-box and white-box fail):
```
[fail] factorial(5)
    Expected result: 120
    Found: 0

[fail] factorial(5)
    Expected recursive calls: 4 (± 1)
    Found: 0
```

<br>

### Binary search (iterative)
Reference solution using binary search:
```java
/*
@Test ({1 , 2, 3, 4, 5, 6, 7}, 1)
@Test ({1 , 3, 7, 9, 11, 13, 17, 19} , 18)
@CountLoopIterations
@CheckSideEffects
*/
static int binarySearch (int [] a, int e) {
    int l = 0;
    int r = a. length - 1;
    while (l <= r) {
        int m = l + (r - l) / 2;
        if (a[m] == e) return m;
        if (a[m] < e) l = m + 1;
        else r = m - 1;
    }
    return -1;
}
```

Solution under testing (performing linear search):
```java
static int binarySearch (int [] a, int e) {
    for (int i = 0; i < a. length ; i++)
        if (a[i] == e) return i;
    return -1;
}
```

Witter test results (black-box pass, white-box fail):
```
[pass] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected result: 0 

[fail] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected loop iterations: 3 
	Found: 1

[pass] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected side effects: false 

[pass] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected result: -1 

[fail] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected loop iterations: 4 
	Found: 8

[pass] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected side effects: false 
```

<br>

### Insertion sort (procedure)
Reference solution performing insertion sort:
```java
/*
@Test ({5 , 4, 3, 2, 1})
@CountArrayReads
@CountArrayWrites
@CheckSideEffects
*/
static void sort(int[] a) {
    for (int i = 1; i < a. length; i++) {
        for (int j = i; j > 0; j--) {
            if (a[j] >= a[j - 1]) break;
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }
}
```

Solution under testing (performing selection sort):
```java
static void sort(int[] a) {
    for (int i = 0; i < a. length - 1; i++) {
        int min = i;
        for (int j = i + 1; j < a. length ; j++)
            if (a[j] < a[min]) min = j;
        int tmp = a[i];
        a[i] = a[min];
        a[min] = tmp;
    }
}
```

Witter test results (black-box pass, white-box fail):
```
[fail] sort([5, 4, 3, 2, 1])
	Expected array reads: 40 
	Found: 28

[fail] sort([5, 4, 3, 2, 1])
	Expected array writes: 20 
	Found: 8

[pass] sort([5, 4, 3, 2, 1])
	Expected side effects: false 
```