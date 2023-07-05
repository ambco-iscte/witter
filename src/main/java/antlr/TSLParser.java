// Generated from C:/Users/afons/IdeaProjects/whitebox-proof-of-concept/src\TSL.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class TSLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, TEST_ARGUMENTS=11, INTEGER=12, NEWLINE=13, IGNORE=14;
	public static final int
		RULE_specification = 0, RULE_annotation = 1;
	private static String[] makeRuleNames() {
		return new String[] {
			"specification", "annotation"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'@Test'", "'@CountLoopIterations('", "')'", "'@CountRecordAllocations('", 
			"'@CountArrayAllocations('", "'@CountArrayReadAccesses('", "'@CountArrayWriteAccesses('", 
			"'@CountMemoryUsage('", "'@TrackVariableStates()'", "'@CheckParameterImmutability()'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "TEST_ARGUMENTS", 
			"INTEGER", "NEWLINE", "IGNORE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "TSL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TSLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SpecificationContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(TSLParser.EOF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(TSLParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(TSLParser.NEWLINE, i);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public SpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitSpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitSpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecificationContext specification() throws RecognitionException {
		SpecificationContext _localctx = new SpecificationContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_specification);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(7);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(4);
					match(NEWLINE);
					}
					} 
				}
				setState(9);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(23);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2038L) != 0)) {
				{
				setState(10);
				annotation();
				setState(20);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(14);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(11);
							match(NEWLINE);
							}
							}
							setState(16);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(17);
						annotation();
						}
						} 
					}
					setState(22);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				}
			}

			setState(28);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(25);
				match(NEWLINE);
				}
				}
				setState(30);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(31);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationContext extends ParserRuleContext {
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
	 
		public AnnotationContext() { }
		public void copyFrom(AnnotationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountArrayAllocationsContext extends AnnotationContext {
		public Token margin;
		public TerminalNode INTEGER() { return getToken(TSLParser.INTEGER, 0); }
		public CountArrayAllocationsContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCountArrayAllocations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCountArrayAllocations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCountArrayAllocations(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountLoopIterationsContext extends AnnotationContext {
		public Token margin;
		public TerminalNode INTEGER() { return getToken(TSLParser.INTEGER, 0); }
		public CountLoopIterationsContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCountLoopIterations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCountLoopIterations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCountLoopIterations(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountMemoryUsageContext extends AnnotationContext {
		public Token margin;
		public TerminalNode INTEGER() { return getToken(TSLParser.INTEGER, 0); }
		public CountMemoryUsageContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCountMemoryUsage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCountMemoryUsage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCountMemoryUsage(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CheckParameterImmutabilityContext extends AnnotationContext {
		public CheckParameterImmutabilityContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCheckParameterImmutability(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCheckParameterImmutability(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCheckParameterImmutability(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountArrayWriteAccessesContext extends AnnotationContext {
		public Token margin;
		public TerminalNode INTEGER() { return getToken(TSLParser.INTEGER, 0); }
		public CountArrayWriteAccessesContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCountArrayWriteAccesses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCountArrayWriteAccesses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCountArrayWriteAccesses(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TrackVariableStatesContext extends AnnotationContext {
		public TrackVariableStatesContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterTrackVariableStates(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitTrackVariableStates(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitTrackVariableStates(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountObjectAllocationsContext extends AnnotationContext {
		public Token margin;
		public TerminalNode INTEGER() { return getToken(TSLParser.INTEGER, 0); }
		public CountObjectAllocationsContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCountObjectAllocations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCountObjectAllocations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCountObjectAllocations(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TestCaseAnnotationContext extends AnnotationContext {
		public TerminalNode TEST_ARGUMENTS() { return getToken(TSLParser.TEST_ARGUMENTS, 0); }
		public TestCaseAnnotationContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterTestCaseAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitTestCaseAnnotation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitTestCaseAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountArrayReadAccessesContext extends AnnotationContext {
		public Token margin;
		public TerminalNode INTEGER() { return getToken(TSLParser.INTEGER, 0); }
		public CountArrayReadAccessesContext(AnnotationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).enterCountArrayReadAccesses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TSLListener ) ((TSLListener)listener).exitCountArrayReadAccesses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TSLVisitor ) return ((TSLVisitor<? extends T>)visitor).visitCountArrayReadAccesses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_annotation);
		try {
			setState(55);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				_localctx = new TestCaseAnnotationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(33);
				match(T__0);
				setState(34);
				match(TEST_ARGUMENTS);
				}
				break;
			case T__1:
				_localctx = new CountLoopIterationsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(35);
				match(T__1);
				setState(36);
				((CountLoopIterationsContext)_localctx).margin = match(INTEGER);
				setState(37);
				match(T__2);
				}
				break;
			case T__3:
				_localctx = new CountObjectAllocationsContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(38);
				match(T__3);
				setState(39);
				((CountObjectAllocationsContext)_localctx).margin = match(INTEGER);
				setState(40);
				match(T__2);
				}
				break;
			case T__4:
				_localctx = new CountArrayAllocationsContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(41);
				match(T__4);
				setState(42);
				((CountArrayAllocationsContext)_localctx).margin = match(INTEGER);
				setState(43);
				match(T__2);
				}
				break;
			case T__5:
				_localctx = new CountArrayReadAccessesContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(44);
				match(T__5);
				setState(45);
				((CountArrayReadAccessesContext)_localctx).margin = match(INTEGER);
				setState(46);
				match(T__2);
				}
				break;
			case T__6:
				_localctx = new CountArrayWriteAccessesContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(47);
				match(T__6);
				setState(48);
				((CountArrayWriteAccessesContext)_localctx).margin = match(INTEGER);
				setState(49);
				match(T__2);
				}
				break;
			case T__7:
				_localctx = new CountMemoryUsageContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(50);
				match(T__7);
				setState(51);
				((CountMemoryUsageContext)_localctx).margin = match(INTEGER);
				setState(52);
				match(T__2);
				}
				break;
			case T__8:
				_localctx = new TrackVariableStatesContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(53);
				match(T__8);
				}
				break;
			case T__9:
				_localctx = new CheckParameterImmutabilityContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(54);
				match(T__9);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u000e:\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0001"+
		"\u0000\u0005\u0000\u0006\b\u0000\n\u0000\f\u0000\t\t\u0000\u0001\u0000"+
		"\u0001\u0000\u0005\u0000\r\b\u0000\n\u0000\f\u0000\u0010\t\u0000\u0001"+
		"\u0000\u0005\u0000\u0013\b\u0000\n\u0000\f\u0000\u0016\t\u0000\u0003\u0000"+
		"\u0018\b\u0000\u0001\u0000\u0005\u0000\u001b\b\u0000\n\u0000\f\u0000\u001e"+
		"\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u00018\b\u0001\u0001\u0001\u0000\u0000\u0002\u0000\u0002"+
		"\u0000\u0000D\u0000\u0007\u0001\u0000\u0000\u0000\u00027\u0001\u0000\u0000"+
		"\u0000\u0004\u0006\u0005\r\u0000\u0000\u0005\u0004\u0001\u0000\u0000\u0000"+
		"\u0006\t\u0001\u0000\u0000\u0000\u0007\u0005\u0001\u0000\u0000\u0000\u0007"+
		"\b\u0001\u0000\u0000\u0000\b\u0017\u0001\u0000\u0000\u0000\t\u0007\u0001"+
		"\u0000\u0000\u0000\n\u0014\u0003\u0002\u0001\u0000\u000b\r\u0005\r\u0000"+
		"\u0000\f\u000b\u0001\u0000\u0000\u0000\r\u0010\u0001\u0000\u0000\u0000"+
		"\u000e\f\u0001\u0000\u0000\u0000\u000e\u000f\u0001\u0000\u0000\u0000\u000f"+
		"\u0011\u0001\u0000\u0000\u0000\u0010\u000e\u0001\u0000\u0000\u0000\u0011"+
		"\u0013\u0003\u0002\u0001\u0000\u0012\u000e\u0001\u0000\u0000\u0000\u0013"+
		"\u0016\u0001\u0000\u0000\u0000\u0014\u0012\u0001\u0000\u0000\u0000\u0014"+
		"\u0015\u0001\u0000\u0000\u0000\u0015\u0018\u0001\u0000\u0000\u0000\u0016"+
		"\u0014\u0001\u0000\u0000\u0000\u0017\n\u0001\u0000\u0000\u0000\u0017\u0018"+
		"\u0001\u0000\u0000\u0000\u0018\u001c\u0001\u0000\u0000\u0000\u0019\u001b"+
		"\u0005\r\u0000\u0000\u001a\u0019\u0001\u0000\u0000\u0000\u001b\u001e\u0001"+
		"\u0000\u0000\u0000\u001c\u001a\u0001\u0000\u0000\u0000\u001c\u001d\u0001"+
		"\u0000\u0000\u0000\u001d\u001f\u0001\u0000\u0000\u0000\u001e\u001c\u0001"+
		"\u0000\u0000\u0000\u001f \u0005\u0000\u0000\u0001 \u0001\u0001\u0000\u0000"+
		"\u0000!\"\u0005\u0001\u0000\u0000\"8\u0005\u000b\u0000\u0000#$\u0005\u0002"+
		"\u0000\u0000$%\u0005\f\u0000\u0000%8\u0005\u0003\u0000\u0000&\'\u0005"+
		"\u0004\u0000\u0000\'(\u0005\f\u0000\u0000(8\u0005\u0003\u0000\u0000)*"+
		"\u0005\u0005\u0000\u0000*+\u0005\f\u0000\u0000+8\u0005\u0003\u0000\u0000"+
		",-\u0005\u0006\u0000\u0000-.\u0005\f\u0000\u0000.8\u0005\u0003\u0000\u0000"+
		"/0\u0005\u0007\u0000\u000001\u0005\f\u0000\u000018\u0005\u0003\u0000\u0000"+
		"23\u0005\b\u0000\u000034\u0005\f\u0000\u000048\u0005\u0003\u0000\u0000"+
		"58\u0005\t\u0000\u000068\u0005\n\u0000\u00007!\u0001\u0000\u0000\u0000"+
		"7#\u0001\u0000\u0000\u00007&\u0001\u0000\u0000\u00007)\u0001\u0000\u0000"+
		"\u00007,\u0001\u0000\u0000\u00007/\u0001\u0000\u0000\u000072\u0001\u0000"+
		"\u0000\u000075\u0001\u0000\u0000\u000076\u0001\u0000\u0000\u00008\u0003"+
		"\u0001\u0000\u0000\u0000\u0006\u0007\u000e\u0014\u0017\u001c7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}