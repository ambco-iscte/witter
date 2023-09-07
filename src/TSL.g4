grammar TSL; // Test Specification Language

specification: NEWLINE* (annotation (NEWLINE* annotation)*)? NEWLINE* EOF;

annotation:
    '@Test' TEST_ARGUMENTS #testCaseAnnotation
    | '@CountLoopIterations' ('(' margin=INTEGER ')')? #countLoopIterations
    | '@CheckObjectAllocations' #countObjectAllocations
    | '@CheckArrayAllocations' #countArrayAllocations
    | '@CountArrayReads' ('(' margin=INTEGER ')')? #countArrayReadAccesses
    | '@CountArrayWrites' ('(' margin=INTEGER ')')? #countArrayWriteAccesses
    | '@CountMemoryUsage' ('(' margin=INTEGER ')')? #countMemoryUsage
    | '@TrackArgumentStates' #trackVariableStates
    | '@CheckSideEffects' #checkParameterImmutability
    | '@CountRecursiveCalls' ('(' margin=INTEGER ')')? #countRecursiveCalls
    ;

TEST_ARGUMENTS: '(' ( ~('(' | ')') | TEST_ARGUMENTS )* ')';

INTEGER: ('-')?[0-9]+;

NEWLINE: '\n' | '\r' | '\r\n';

IGNORE: (WHITESPACE | COMMENT) -> skip;

fragment WHITESPACE: (' ' | '\t')+;

fragment COMMENT: NEWLINE* '#*' .+? '*#' NEWLINE*;