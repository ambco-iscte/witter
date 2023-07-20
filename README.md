# Witter: A Library for White-Box Testing of Java Code

## What is Witter?
**Witter** is a software testing library that aims to provide
a straightforward way for software testers to run white-box
tests of Java source code. The library thus provides the tools
for a software tester to analyse the execution of a proposed
implementation against an accepted solution to verify that
the code not only produces correct results, but also verifies
certain efficiency or design pattern restrictions.

<br>

## Functionalities
Witter provides a Kotlin library that allows the definition
of annotated reference solutions and their execution and
comparison against proposed implementations.

Witter currently supports the dynamic measurement of the
following metrics relating to the execution of procedures:
1. Loop iterations;
2. Record (object) allocations;
3. Array allocations;
4. Array read and write accesses;
5. Memory usage (in bytes);
6. Recursive calls;
7. Argument mutability;
8. Tracking argument variable states, that is, the states
   that each argument variable takes as the procedure
   executes.
   
Naturally, the outputs of functions are also measured, to
   allow for regular black-box testing.

One can define the intended test cases for a given application
by providing Witter with a reference solution written in Java
and annotated with comments that detail the different inputs
to test and the metrics that should be measured during test
execution. Each procedure is thus annotated with a comment
following Witter’s Test Specification Language (TSL) syntax, which is based on Java’s usual annotation syntax. The
test specification relying on a purely textual comment is
intended to simplify the process by removing the need to
import external modules, while also giving us more freedom
in handling the content of the provided annotations.
TSL’s main annotation is the @Test annotation, which
contains the arguments that should be passed when executing the procedure for a single test case. The arguments are
enclosed in parentheses and follow Java’s usual syntax.
Metrics 1-6 are associated with the testing of any given procedure by including their respective annotations in the 
procedure’s test specification comment. Each of these annotations
should be parametrised with an integer value that denotes
an acceptable margin of deviation of the tested implementation from the reference solution when it comes to 
each metric. For example, the annotation @CountLoopIterations(3)
would measure the number of loop iterations on execution
of a given procedure, and a solution is considered acceptable if that number lies within 3 iterations of the reference
procedure. The annotations for metrics 7 and 8 do not require the inclusion of a margin value, thus being simply
`@CheckParameterMutability` and `@TrackArgumentStates`, respectively.

<br>

## Examples
### Example 1 - Test specification
Consider we wish to assess an implementation of the binary search algorithm on an array of 
integers. We thus provide Witter with an annotated reference solution that defines two 
test cases and additionally compares the number of loop iterations between the reference 
and submitted solutions. For the sake of showcasing the intended functionality, 
assume that the submitted solution is instead implemented using a linear search algorithm.

**Listing 1.** Reference solution.
```java
/*
@Test(new int[] { 1, 2, 3, 4, 5, 6 }, 6)
@Test(new int[] { 1, 2, 3 }, 3)
@CountLoopIterations(0)
 */
public static int search(int[] a, int e) {
    int l = 0;
    int r = a.length - 1;
    while (l <= r) {
        int m = l + (r - l) / 2;
        if (a[m] == e)
            return m;
        if (a[m] < e)
            l = m + 1;
        else
            r = m - 1;
    }
    return -1;
}
```

**Listing 2.** Submitted solution.
```java
public static int search(int[] a, int e) {
    for (int i = 0; i < a.length; i++) {
        if (a[i] == e)
            return i;
    }
    return -1;
}
```

We then execute the testing process, which gives a collection of results, grouped by each 
executed procedure (in this example, only one procedure is considered):
```
[search]: Incorrect count of loop iterations for invocation search([1, 2, 3, 4, 5, 6, 7], 1):
          should have measured 3 +- 0 loop iterations, but measured 1.

[search]: Incorrect count of loop iterations for invocation search([1, 2, 3], 3): should 
          have measured 2 +- 0 loop iterations, but measured 3.
```

### Example 2 - Test execution
Witter provides a Tester class that, given an annotated reference solution as described above, and the solution that
one wishes to assess, automatically handles the execution
of the test cases and the collection of the desired metrics,
outputting information of the results for each executed procedure.
```kotlin
val tester = Tester(reference = File("AnnotatedReferenceSolution.java"))

// Map associating each procedure to a list of result information
val results = tester.execute(file = File("StudentSubmission.java"))
```