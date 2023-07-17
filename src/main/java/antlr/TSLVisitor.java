// Generated from C:/Users/afons/IdeaProjects/whitebox-proof-of-concept/src\TSL.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TSLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TSLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TSLParser#specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecification(TSLParser.SpecificationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code testCaseAnnotation}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTestCaseAnnotation(TSLParser.TestCaseAnnotationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countLoopIterations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountLoopIterations(TSLParser.CountLoopIterationsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countObjectAllocations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountObjectAllocations(TSLParser.CountObjectAllocationsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countArrayAllocations}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountArrayAllocations(TSLParser.CountArrayAllocationsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countArrayReadAccesses}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountArrayReadAccesses(TSLParser.CountArrayReadAccessesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countArrayWriteAccesses}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountArrayWriteAccesses(TSLParser.CountArrayWriteAccessesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countMemoryUsage}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountMemoryUsage(TSLParser.CountMemoryUsageContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trackVariableStates}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrackVariableStates(TSLParser.TrackVariableStatesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code checkParameterImmutability}
	 * labeled alternative in {@link TSLParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckParameterImmutability(TSLParser.CheckParameterImmutabilityContext ctx);
}