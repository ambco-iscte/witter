grammar TSL; // Test Specification Language

specification: NEWLINE* (annotation (NEWLINE* annotation)*)? NEWLINE* EOF;

annotation:
    '@Test' TEST_ARGUMENTS #testCaseAnnotation
    | '@CountLoopIterations(' margin=INTEGER ')' #countLoopIterations
    | '@CountRecordAllocations(' margin=INTEGER ')' #countObjectAllocations
    | '@CountArrayAllocations(' margin=INTEGER ')' #countArrayAllocations
    | '@CountArrayReadAccesses(' margin=INTEGER ')' #countArrayReadAccesses
    | '@CountArrayWriteAccesses(' margin=INTEGER ')' #countArrayWriteAccesses
    | '@CountMemoryUsage(' margin=INTEGER ')' #countMemoryUsage
    | '@TrackArgumentStates()' #trackVariableStates
    | '@CheckParameterMutability()' #checkParameterImmutability
    | '@CountRecursiveCalls(' margin=INTEGER ')' #countRecursiveCalls
    ;

TEST_ARGUMENTS: '(' ( ~('(' | ')') | TEST_ARGUMENTS )* ')';

INTEGER: ('-')?[0-9]+;

NEWLINE: '\n' | '\r' | '\r\n';

IGNORE: (WHITESPACE | COMMENT) -> skip;

fragment WHITESPACE: (' ' | '\t')+;

fragment COMMENT: NEWLINE* '#*' .+? '*#' NEWLINE*;