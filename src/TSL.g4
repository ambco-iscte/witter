grammar TSL; // Test Specification Language

specification: (annotation (annotation)*)? EOF;

annotation:
    '@Test' '(' args=(TEST_ARGUMENTS | INTEGER) ')' #testCaseAnnotation
    | '@CountLoopIterations' ('(' margin=INTEGER ')')? #countLoopIterations
    | '@CheckObjectAllocations' #countObjectAllocations
    | '@CheckArrayAllocations' #countArrayAllocations
    | '@CountArrayReads' ('(' margin=INTEGER ')')? #countArrayReadAccesses
    | '@CountArrayWrites' ('(' margin=INTEGER ')')? #countArrayWriteAccesses
    | '@CountMemoryUsage' ('(' margin=INTEGER ')')? #countMemoryUsage
    | '@TrackArgumentStates(' parameterID=IDENTIFIER ')' #trackArgumentStates
    | '@CheckSideEffects' #checkParameterImmutability
    | '@CountRecursiveCalls' ('(' margin=INTEGER ')')? #countRecursiveCalls
    ;

INTEGER: [0-9]+;

IDENTIFIER: [a-zA-Z_$][a-zA-Z0-9_$]*; // Variable identifier

TEST_ARGUMENTS: TEST_ARGUMENT (TEST_ARGUMENT)*; // Modified to remove left-recursion
TEST_ARGUMENT: ~('(' | ')' | '\n' | '\r');

IGNORE: (WHITESPACE | COMMENT | NEWLINE) -> skip;

fragment NEWLINE: '\n' | '\r' | '\r\n';

fragment WHITESPACE: (' ' | '\t')+;

fragment COMMENT: '#*' .+? '*#';