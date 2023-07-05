// Generated from C:/Users/afons/IdeaProjects/whitebox-proof-of-concept/src\TSL.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TSLParser}.
 */
public interface TSLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TSLParser#specification}.
	 * @param ctx the parse tree
	 */
	void enterSpecification(TSLParser.SpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TSLParser#specification}.
	 * @param ctx the parse tree
	 */
	void exitSpecification(TSLParser.SpecificationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code testCaseAnnotation}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterTestCaseAnnotation(TSLParser.TestCaseAnnotationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code testCaseAnnotation}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitTestCaseAnnotation(TSLParser.TestCaseAnnotationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countLoopIterations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCountLoopIterations(TSLParser.CountLoopIterationsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countLoopIterations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCountLoopIterations(TSLParser.CountLoopIterationsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countObjectAllocations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCountObjectAllocations(TSLParser.CountObjectAllocationsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countObjectAllocations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCountObjectAllocations(TSLParser.CountObjectAllocationsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countArrayAllocations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCountArrayAllocations(TSLParser.CountArrayAllocationsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countArrayAllocations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCountArrayAllocations(TSLParser.CountArrayAllocationsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countArrayReadAccesses}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCountArrayReadAccesses(TSLParser.CountArrayReadAccessesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countArrayReadAccesses}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCountArrayReadAccesses(TSLParser.CountArrayReadAccessesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countArrayWriteAccesses}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCountArrayWriteAccesses(TSLParser.CountArrayWriteAccessesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countArrayWriteAccesses}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCountArrayWriteAccesses(TSLParser.CountArrayWriteAccessesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countMemoryUsage}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCountMemoryUsage(TSLParser.CountMemoryUsageContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countMemoryUsage}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCountMemoryUsage(TSLParser.CountMemoryUsageContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trackVariableStates}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterTrackVariableStates(TSLParser.TrackVariableStatesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trackVariableStates}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitTrackVariableStates(TSLParser.TrackVariableStatesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code checkParameterImmutability}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterCheckParameterImmutability(TSLParser.CheckParameterImmutabilityContext ctx);
	/**
	 * Exit a parse tree produced by the {@code checkParameterImmutability}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitCheckParameterImmutability(TSLParser.CheckParameterImmutabilityContext ctx);
}