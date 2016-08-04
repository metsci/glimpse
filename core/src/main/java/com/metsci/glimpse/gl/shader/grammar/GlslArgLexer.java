// $ANTLR 3.5.2 core/src/main/resources/shader/antlr/GlslArg.g 2016-08-03 12:09:15

package com.metsci.glimpse.gl.shader.grammar;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class GlslArgLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__65=65;
	public static final int ATTRIBUTE=4;
	public static final int BOOL=5;
	public static final int BREAK=6;
	public static final int BVEC2=7;
	public static final int BVEC3=8;
	public static final int BVEC4=9;
	public static final int COMMENT=10;
	public static final int CONST=11;
	public static final int CONTINUE=12;
	public static final int DIRECTIVE=13;
	public static final int DISCARD=14;
	public static final int DO=15;
	public static final int ELSE=16;
	public static final int FALSE=17;
	public static final int FLOAT=18;
	public static final int FOR=19;
	public static final int HIGH_PRECISION=20;
	public static final int IDENTIFIER=21;
	public static final int IF=22;
	public static final int IN=23;
	public static final int INOUT=24;
	public static final int INT=25;
	public static final int INVARIANT=26;
	public static final int ISAMPLER1D=27;
	public static final int ISAMPLER2D=28;
	public static final int IVEC2=29;
	public static final int IVEC3=30;
	public static final int IVEC4=31;
	public static final int LBRACKET=32;
	public static final int LCURLY=33;
	public static final int LOW_PRECISION=34;
	public static final int LPAREN=35;
	public static final int MAT2=36;
	public static final int MAT3=37;
	public static final int MAT4=38;
	public static final int MEDIUM_PRECISION=39;
	public static final int MULTILINE_COMMENT=40;
	public static final int OUT=41;
	public static final int PRECISION=42;
	public static final int RBRACKET=43;
	public static final int RCURLY=44;
	public static final int RETURN=45;
	public static final int RPAREN=46;
	public static final int SAMPLER1D=47;
	public static final int SAMPLER1DARRAY=48;
	public static final int SAMPLER2D=49;
	public static final int SAMPLER2DARRAY=50;
	public static final int SAMPLERCUBE=51;
	public static final int SEMI=52;
	public static final int STRUCT=53;
	public static final int TRUE=54;
	public static final int UNIFORM=55;
	public static final int USAMPLER1D=56;
	public static final int USAMPLER2D=57;
	public static final int VARYING=58;
	public static final int VEC2=59;
	public static final int VEC3=60;
	public static final int VEC4=61;
	public static final int VOID=62;
	public static final int WHILE=63;
	public static final int WHITESPACE=64;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public GlslArgLexer() {} 
	public GlslArgLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public GlslArgLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "core/src/main/resources/shader/antlr/GlslArg.g"; }

	// $ANTLR start "T__65"
	public final void mT__65() throws RecognitionException {
		try {
			int _type = T__65;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:11:7: ( 'main' )
			// core/src/main/resources/shader/antlr/GlslArg.g:11:9: 'main'
			{
			match("main"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__65"

	// $ANTLR start "LCURLY"
	public final void mLCURLY() throws RecognitionException {
		try {
			int _type = LCURLY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:104:18: ( '{' )
			// core/src/main/resources/shader/antlr/GlslArg.g:104:20: '{'
			{
			match('{'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LCURLY"

	// $ANTLR start "RCURLY"
	public final void mRCURLY() throws RecognitionException {
		try {
			int _type = RCURLY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:105:18: ( '}' )
			// core/src/main/resources/shader/antlr/GlslArg.g:105:20: '}'
			{
			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RCURLY"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:106:18: ( '(' )
			// core/src/main/resources/shader/antlr/GlslArg.g:106:20: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:107:18: ( ')' )
			// core/src/main/resources/shader/antlr/GlslArg.g:107:20: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RPAREN"

	// $ANTLR start "LBRACKET"
	public final void mLBRACKET() throws RecognitionException {
		try {
			int _type = LBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:108:18: ( '[' )
			// core/src/main/resources/shader/antlr/GlslArg.g:108:20: '['
			{
			match('['); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACKET"

	// $ANTLR start "RBRACKET"
	public final void mRBRACKET() throws RecognitionException {
		try {
			int _type = RBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:109:18: ( ']' )
			// core/src/main/resources/shader/antlr/GlslArg.g:109:20: ']'
			{
			match(']'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACKET"

	// $ANTLR start "SEMI"
	public final void mSEMI() throws RecognitionException {
		try {
			int _type = SEMI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:110:18: ( ';' )
			// core/src/main/resources/shader/antlr/GlslArg.g:110:20: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SEMI"

	// $ANTLR start "ATTRIBUTE"
	public final void mATTRIBUTE() throws RecognitionException {
		try {
			int _type = ATTRIBUTE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:112:18: ( 'attribute' )
			// core/src/main/resources/shader/antlr/GlslArg.g:112:20: 'attribute'
			{
			match("attribute"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ATTRIBUTE"

	// $ANTLR start "BOOL"
	public final void mBOOL() throws RecognitionException {
		try {
			int _type = BOOL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:113:18: ( 'bool' )
			// core/src/main/resources/shader/antlr/GlslArg.g:113:20: 'bool'
			{
			match("bool"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOOL"

	// $ANTLR start "BREAK"
	public final void mBREAK() throws RecognitionException {
		try {
			int _type = BREAK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:114:18: ( 'break' )
			// core/src/main/resources/shader/antlr/GlslArg.g:114:20: 'break'
			{
			match("break"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BREAK"

	// $ANTLR start "BVEC2"
	public final void mBVEC2() throws RecognitionException {
		try {
			int _type = BVEC2;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:115:18: ( 'bvec2' )
			// core/src/main/resources/shader/antlr/GlslArg.g:115:20: 'bvec2'
			{
			match("bvec2"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BVEC2"

	// $ANTLR start "BVEC3"
	public final void mBVEC3() throws RecognitionException {
		try {
			int _type = BVEC3;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:116:18: ( 'bvec3' )
			// core/src/main/resources/shader/antlr/GlslArg.g:116:20: 'bvec3'
			{
			match("bvec3"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BVEC3"

	// $ANTLR start "BVEC4"
	public final void mBVEC4() throws RecognitionException {
		try {
			int _type = BVEC4;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:117:18: ( 'bvec4' )
			// core/src/main/resources/shader/antlr/GlslArg.g:117:20: 'bvec4'
			{
			match("bvec4"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BVEC4"

	// $ANTLR start "CONST"
	public final void mCONST() throws RecognitionException {
		try {
			int _type = CONST;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:118:18: ( 'const' )
			// core/src/main/resources/shader/antlr/GlslArg.g:118:20: 'const'
			{
			match("const"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONST"

	// $ANTLR start "CONTINUE"
	public final void mCONTINUE() throws RecognitionException {
		try {
			int _type = CONTINUE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:119:18: ( 'continue' )
			// core/src/main/resources/shader/antlr/GlslArg.g:119:20: 'continue'
			{
			match("continue"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONTINUE"

	// $ANTLR start "DISCARD"
	public final void mDISCARD() throws RecognitionException {
		try {
			int _type = DISCARD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:120:18: ( 'discard' )
			// core/src/main/resources/shader/antlr/GlslArg.g:120:20: 'discard'
			{
			match("discard"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DISCARD"

	// $ANTLR start "DO"
	public final void mDO() throws RecognitionException {
		try {
			int _type = DO;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:121:18: ( 'do' )
			// core/src/main/resources/shader/antlr/GlslArg.g:121:20: 'do'
			{
			match("do"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DO"

	// $ANTLR start "ELSE"
	public final void mELSE() throws RecognitionException {
		try {
			int _type = ELSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:122:18: ( 'else' )
			// core/src/main/resources/shader/antlr/GlslArg.g:122:20: 'else'
			{
			match("else"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ELSE"

	// $ANTLR start "FALSE"
	public final void mFALSE() throws RecognitionException {
		try {
			int _type = FALSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:123:18: ( 'false' )
			// core/src/main/resources/shader/antlr/GlslArg.g:123:20: 'false'
			{
			match("false"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FALSE"

	// $ANTLR start "FLOAT"
	public final void mFLOAT() throws RecognitionException {
		try {
			int _type = FLOAT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:124:18: ( 'float' )
			// core/src/main/resources/shader/antlr/GlslArg.g:124:20: 'float'
			{
			match("float"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FLOAT"

	// $ANTLR start "FOR"
	public final void mFOR() throws RecognitionException {
		try {
			int _type = FOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:125:18: ( 'for' )
			// core/src/main/resources/shader/antlr/GlslArg.g:125:20: 'for'
			{
			match("for"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FOR"

	// $ANTLR start "HIGH_PRECISION"
	public final void mHIGH_PRECISION() throws RecognitionException {
		try {
			int _type = HIGH_PRECISION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:126:18: ( 'highp' )
			// core/src/main/resources/shader/antlr/GlslArg.g:126:20: 'highp'
			{
			match("highp"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HIGH_PRECISION"

	// $ANTLR start "IF"
	public final void mIF() throws RecognitionException {
		try {
			int _type = IF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:127:18: ( 'if' )
			// core/src/main/resources/shader/antlr/GlslArg.g:127:20: 'if'
			{
			match("if"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IF"

	// $ANTLR start "IN"
	public final void mIN() throws RecognitionException {
		try {
			int _type = IN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:128:18: ( 'in' )
			// core/src/main/resources/shader/antlr/GlslArg.g:128:20: 'in'
			{
			match("in"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IN"

	// $ANTLR start "INOUT"
	public final void mINOUT() throws RecognitionException {
		try {
			int _type = INOUT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:129:18: ( 'inout' )
			// core/src/main/resources/shader/antlr/GlslArg.g:129:20: 'inout'
			{
			match("inout"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INOUT"

	// $ANTLR start "INT"
	public final void mINT() throws RecognitionException {
		try {
			int _type = INT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:130:18: ( 'int' )
			// core/src/main/resources/shader/antlr/GlslArg.g:130:20: 'int'
			{
			match("int"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INT"

	// $ANTLR start "INVARIANT"
	public final void mINVARIANT() throws RecognitionException {
		try {
			int _type = INVARIANT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:131:18: ( 'invariant' )
			// core/src/main/resources/shader/antlr/GlslArg.g:131:20: 'invariant'
			{
			match("invariant"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INVARIANT"

	// $ANTLR start "IVEC2"
	public final void mIVEC2() throws RecognitionException {
		try {
			int _type = IVEC2;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:132:18: ( 'ivec2' )
			// core/src/main/resources/shader/antlr/GlslArg.g:132:20: 'ivec2'
			{
			match("ivec2"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IVEC2"

	// $ANTLR start "IVEC3"
	public final void mIVEC3() throws RecognitionException {
		try {
			int _type = IVEC3;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:133:18: ( 'ivec3' )
			// core/src/main/resources/shader/antlr/GlslArg.g:133:20: 'ivec3'
			{
			match("ivec3"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IVEC3"

	// $ANTLR start "IVEC4"
	public final void mIVEC4() throws RecognitionException {
		try {
			int _type = IVEC4;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:134:18: ( 'ivec4' )
			// core/src/main/resources/shader/antlr/GlslArg.g:134:20: 'ivec4'
			{
			match("ivec4"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IVEC4"

	// $ANTLR start "LOW_PRECISION"
	public final void mLOW_PRECISION() throws RecognitionException {
		try {
			int _type = LOW_PRECISION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:135:18: ( 'lowp' )
			// core/src/main/resources/shader/antlr/GlslArg.g:135:20: 'lowp'
			{
			match("lowp"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LOW_PRECISION"

	// $ANTLR start "MAT2"
	public final void mMAT2() throws RecognitionException {
		try {
			int _type = MAT2;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:136:18: ( 'mat2' )
			// core/src/main/resources/shader/antlr/GlslArg.g:136:20: 'mat2'
			{
			match("mat2"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MAT2"

	// $ANTLR start "MAT3"
	public final void mMAT3() throws RecognitionException {
		try {
			int _type = MAT3;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:137:18: ( 'mat3' )
			// core/src/main/resources/shader/antlr/GlslArg.g:137:20: 'mat3'
			{
			match("mat3"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MAT3"

	// $ANTLR start "MAT4"
	public final void mMAT4() throws RecognitionException {
		try {
			int _type = MAT4;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:138:18: ( 'mat4' )
			// core/src/main/resources/shader/antlr/GlslArg.g:138:20: 'mat4'
			{
			match("mat4"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MAT4"

	// $ANTLR start "MEDIUM_PRECISION"
	public final void mMEDIUM_PRECISION() throws RecognitionException {
		try {
			int _type = MEDIUM_PRECISION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:139:18: ( 'mediump' )
			// core/src/main/resources/shader/antlr/GlslArg.g:139:20: 'mediump'
			{
			match("mediump"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MEDIUM_PRECISION"

	// $ANTLR start "OUT"
	public final void mOUT() throws RecognitionException {
		try {
			int _type = OUT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:140:18: ( 'out' )
			// core/src/main/resources/shader/antlr/GlslArg.g:140:20: 'out'
			{
			match("out"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OUT"

	// $ANTLR start "PRECISION"
	public final void mPRECISION() throws RecognitionException {
		try {
			int _type = PRECISION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:141:18: ( 'precision' )
			// core/src/main/resources/shader/antlr/GlslArg.g:141:20: 'precision'
			{
			match("precision"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PRECISION"

	// $ANTLR start "RETURN"
	public final void mRETURN() throws RecognitionException {
		try {
			int _type = RETURN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:142:18: ( 'return' )
			// core/src/main/resources/shader/antlr/GlslArg.g:142:20: 'return'
			{
			match("return"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RETURN"

	// $ANTLR start "SAMPLER2D"
	public final void mSAMPLER2D() throws RecognitionException {
		try {
			int _type = SAMPLER2D;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:143:18: ( 'sampler2D' )
			// core/src/main/resources/shader/antlr/GlslArg.g:143:20: 'sampler2D'
			{
			match("sampler2D"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SAMPLER2D"

	// $ANTLR start "ISAMPLER2D"
	public final void mISAMPLER2D() throws RecognitionException {
		try {
			int _type = ISAMPLER2D;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:144:18: ( 'isampler2D' )
			// core/src/main/resources/shader/antlr/GlslArg.g:144:20: 'isampler2D'
			{
			match("isampler2D"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ISAMPLER2D"

	// $ANTLR start "USAMPLER2D"
	public final void mUSAMPLER2D() throws RecognitionException {
		try {
			int _type = USAMPLER2D;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:145:18: ( 'usampler2D' )
			// core/src/main/resources/shader/antlr/GlslArg.g:145:20: 'usampler2D'
			{
			match("usampler2D"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "USAMPLER2D"

	// $ANTLR start "SAMPLER1D"
	public final void mSAMPLER1D() throws RecognitionException {
		try {
			int _type = SAMPLER1D;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:146:18: ( 'sampler1D' )
			// core/src/main/resources/shader/antlr/GlslArg.g:146:20: 'sampler1D'
			{
			match("sampler1D"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SAMPLER1D"

	// $ANTLR start "ISAMPLER1D"
	public final void mISAMPLER1D() throws RecognitionException {
		try {
			int _type = ISAMPLER1D;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:147:18: ( 'isampler1D' )
			// core/src/main/resources/shader/antlr/GlslArg.g:147:20: 'isampler1D'
			{
			match("isampler1D"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ISAMPLER1D"

	// $ANTLR start "USAMPLER1D"
	public final void mUSAMPLER1D() throws RecognitionException {
		try {
			int _type = USAMPLER1D;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:148:18: ( 'usampler1D' )
			// core/src/main/resources/shader/antlr/GlslArg.g:148:20: 'usampler1D'
			{
			match("usampler1D"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "USAMPLER1D"

	// $ANTLR start "SAMPLERCUBE"
	public final void mSAMPLERCUBE() throws RecognitionException {
		try {
			int _type = SAMPLERCUBE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:149:18: ( 'samplerCube' )
			// core/src/main/resources/shader/antlr/GlslArg.g:149:20: 'samplerCube'
			{
			match("samplerCube"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SAMPLERCUBE"

	// $ANTLR start "SAMPLER1DARRAY"
	public final void mSAMPLER1DARRAY() throws RecognitionException {
		try {
			int _type = SAMPLER1DARRAY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:150:23: ( 'sampler1DArray' )
			// core/src/main/resources/shader/antlr/GlslArg.g:150:25: 'sampler1DArray'
			{
			match("sampler1DArray"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SAMPLER1DARRAY"

	// $ANTLR start "SAMPLER2DARRAY"
	public final void mSAMPLER2DARRAY() throws RecognitionException {
		try {
			int _type = SAMPLER2DARRAY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:151:23: ( 'sampler2DArray' )
			// core/src/main/resources/shader/antlr/GlslArg.g:151:25: 'sampler2DArray'
			{
			match("sampler2DArray"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SAMPLER2DARRAY"

	// $ANTLR start "STRUCT"
	public final void mSTRUCT() throws RecognitionException {
		try {
			int _type = STRUCT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:152:18: ( 'struct' )
			// core/src/main/resources/shader/antlr/GlslArg.g:152:20: 'struct'
			{
			match("struct"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRUCT"

	// $ANTLR start "TRUE"
	public final void mTRUE() throws RecognitionException {
		try {
			int _type = TRUE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:153:18: ( 'true' )
			// core/src/main/resources/shader/antlr/GlslArg.g:153:20: 'true'
			{
			match("true"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TRUE"

	// $ANTLR start "UNIFORM"
	public final void mUNIFORM() throws RecognitionException {
		try {
			int _type = UNIFORM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:154:18: ( 'uniform' )
			// core/src/main/resources/shader/antlr/GlslArg.g:154:20: 'uniform'
			{
			match("uniform"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNIFORM"

	// $ANTLR start "VARYING"
	public final void mVARYING() throws RecognitionException {
		try {
			int _type = VARYING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:155:18: ( 'varying' )
			// core/src/main/resources/shader/antlr/GlslArg.g:155:20: 'varying'
			{
			match("varying"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VARYING"

	// $ANTLR start "VEC2"
	public final void mVEC2() throws RecognitionException {
		try {
			int _type = VEC2;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:156:18: ( 'vec2' )
			// core/src/main/resources/shader/antlr/GlslArg.g:156:20: 'vec2'
			{
			match("vec2"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VEC2"

	// $ANTLR start "VEC3"
	public final void mVEC3() throws RecognitionException {
		try {
			int _type = VEC3;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:157:18: ( 'vec3' )
			// core/src/main/resources/shader/antlr/GlslArg.g:157:20: 'vec3'
			{
			match("vec3"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VEC3"

	// $ANTLR start "VEC4"
	public final void mVEC4() throws RecognitionException {
		try {
			int _type = VEC4;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:158:18: ( 'vec4' )
			// core/src/main/resources/shader/antlr/GlslArg.g:158:20: 'vec4'
			{
			match("vec4"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VEC4"

	// $ANTLR start "VOID"
	public final void mVOID() throws RecognitionException {
		try {
			int _type = VOID;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:159:18: ( 'void' )
			// core/src/main/resources/shader/antlr/GlslArg.g:159:20: 'void'
			{
			match("void"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VOID"

	// $ANTLR start "WHILE"
	public final void mWHILE() throws RecognitionException {
		try {
			int _type = WHILE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:160:18: ( 'while' )
			// core/src/main/resources/shader/antlr/GlslArg.g:160:20: 'while'
			{
			match("while"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WHILE"

	// $ANTLR start "IDENTIFIER"
	public final void mIDENTIFIER() throws RecognitionException {
		try {
			int _type = IDENTIFIER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:163:3: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
			// core/src/main/resources/shader/antlr/GlslArg.g:163:5: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// core/src/main/resources/shader/antlr/GlslArg.g:163:28: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop1;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IDENTIFIER"

	// $ANTLR start "WHITESPACE"
	public final void mWHITESPACE() throws RecognitionException {
		try {
			int _type = WHITESPACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:168:3: ( ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' ) )
			// core/src/main/resources/shader/antlr/GlslArg.g:168:5: ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' )
			{
			if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WHITESPACE"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:173:3: ( '//' (~ ( '\\n' | '\\r' ) )* )
			// core/src/main/resources/shader/antlr/GlslArg.g:173:5: '//' (~ ( '\\n' | '\\r' ) )*
			{
			match("//"); 

			// core/src/main/resources/shader/antlr/GlslArg.g:173:10: (~ ( '\\n' | '\\r' ) )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '\u0000' && LA2_0 <= '\t')||(LA2_0 >= '\u000B' && LA2_0 <= '\f')||(LA2_0 >= '\u000E' && LA2_0 <= '\uFFFF')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop2;
				}
			}

			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "DIRECTIVE"
	public final void mDIRECTIVE() throws RecognitionException {
		try {
			int _type = DIRECTIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:178:3: ( '#' (~ ( '\\n' | '\\r' ) )* )
			// core/src/main/resources/shader/antlr/GlslArg.g:178:5: '#' (~ ( '\\n' | '\\r' ) )*
			{
			match('#'); 
			// core/src/main/resources/shader/antlr/GlslArg.g:178:10: (~ ( '\\n' | '\\r' ) )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '\u0000' && LA3_0 <= '\t')||(LA3_0 >= '\u000B' && LA3_0 <= '\f')||(LA3_0 >= '\u000E' && LA3_0 <= '\uFFFF')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop3;
				}
			}

			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIRECTIVE"

	// $ANTLR start "MULTILINE_COMMENT"
	public final void mMULTILINE_COMMENT() throws RecognitionException {
		try {
			int _type = MULTILINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// core/src/main/resources/shader/antlr/GlslArg.g:183:3: ( '/*' ( options {greedy=false; } : . )* '*/' )
			// core/src/main/resources/shader/antlr/GlslArg.g:183:5: '/*' ( options {greedy=false; } : . )* '*/'
			{
			match("/*"); 

			// core/src/main/resources/shader/antlr/GlslArg.g:183:10: ( options {greedy=false; } : . )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0=='*') ) {
					int LA4_1 = input.LA(2);
					if ( (LA4_1=='/') ) {
						alt4=2;
					}
					else if ( ((LA4_1 >= '\u0000' && LA4_1 <= '.')||(LA4_1 >= '0' && LA4_1 <= '\uFFFF')) ) {
						alt4=1;
					}

				}
				else if ( ((LA4_0 >= '\u0000' && LA4_0 <= ')')||(LA4_0 >= '+' && LA4_0 <= '\uFFFF')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:183:38: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop4;
				}
			}

			match("*/"); 

			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MULTILINE_COMMENT"

	@Override
	public void mTokens() throws RecognitionException {
		// core/src/main/resources/shader/antlr/GlslArg.g:1:8: ( T__65 | LCURLY | RCURLY | LPAREN | RPAREN | LBRACKET | RBRACKET | SEMI | ATTRIBUTE | BOOL | BREAK | BVEC2 | BVEC3 | BVEC4 | CONST | CONTINUE | DISCARD | DO | ELSE | FALSE | FLOAT | FOR | HIGH_PRECISION | IF | IN | INOUT | INT | INVARIANT | IVEC2 | IVEC3 | IVEC4 | LOW_PRECISION | MAT2 | MAT3 | MAT4 | MEDIUM_PRECISION | OUT | PRECISION | RETURN | SAMPLER2D | ISAMPLER2D | USAMPLER2D | SAMPLER1D | ISAMPLER1D | USAMPLER1D | SAMPLERCUBE | SAMPLER1DARRAY | SAMPLER2DARRAY | STRUCT | TRUE | UNIFORM | VARYING | VEC2 | VEC3 | VEC4 | VOID | WHILE | IDENTIFIER | WHITESPACE | COMMENT | DIRECTIVE | MULTILINE_COMMENT )
		int alt5=62;
		alt5 = dfa5.predict(input);
		switch (alt5) {
			case 1 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:10: T__65
				{
				mT__65(); 

				}
				break;
			case 2 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:16: LCURLY
				{
				mLCURLY(); 

				}
				break;
			case 3 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:23: RCURLY
				{
				mRCURLY(); 

				}
				break;
			case 4 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:30: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 5 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:37: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 6 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:44: LBRACKET
				{
				mLBRACKET(); 

				}
				break;
			case 7 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:53: RBRACKET
				{
				mRBRACKET(); 

				}
				break;
			case 8 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:62: SEMI
				{
				mSEMI(); 

				}
				break;
			case 9 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:67: ATTRIBUTE
				{
				mATTRIBUTE(); 

				}
				break;
			case 10 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:77: BOOL
				{
				mBOOL(); 

				}
				break;
			case 11 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:82: BREAK
				{
				mBREAK(); 

				}
				break;
			case 12 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:88: BVEC2
				{
				mBVEC2(); 

				}
				break;
			case 13 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:94: BVEC3
				{
				mBVEC3(); 

				}
				break;
			case 14 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:100: BVEC4
				{
				mBVEC4(); 

				}
				break;
			case 15 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:106: CONST
				{
				mCONST(); 

				}
				break;
			case 16 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:112: CONTINUE
				{
				mCONTINUE(); 

				}
				break;
			case 17 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:121: DISCARD
				{
				mDISCARD(); 

				}
				break;
			case 18 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:129: DO
				{
				mDO(); 

				}
				break;
			case 19 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:132: ELSE
				{
				mELSE(); 

				}
				break;
			case 20 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:137: FALSE
				{
				mFALSE(); 

				}
				break;
			case 21 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:143: FLOAT
				{
				mFLOAT(); 

				}
				break;
			case 22 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:149: FOR
				{
				mFOR(); 

				}
				break;
			case 23 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:153: HIGH_PRECISION
				{
				mHIGH_PRECISION(); 

				}
				break;
			case 24 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:168: IF
				{
				mIF(); 

				}
				break;
			case 25 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:171: IN
				{
				mIN(); 

				}
				break;
			case 26 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:174: INOUT
				{
				mINOUT(); 

				}
				break;
			case 27 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:180: INT
				{
				mINT(); 

				}
				break;
			case 28 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:184: INVARIANT
				{
				mINVARIANT(); 

				}
				break;
			case 29 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:194: IVEC2
				{
				mIVEC2(); 

				}
				break;
			case 30 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:200: IVEC3
				{
				mIVEC3(); 

				}
				break;
			case 31 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:206: IVEC4
				{
				mIVEC4(); 

				}
				break;
			case 32 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:212: LOW_PRECISION
				{
				mLOW_PRECISION(); 

				}
				break;
			case 33 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:226: MAT2
				{
				mMAT2(); 

				}
				break;
			case 34 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:231: MAT3
				{
				mMAT3(); 

				}
				break;
			case 35 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:236: MAT4
				{
				mMAT4(); 

				}
				break;
			case 36 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:241: MEDIUM_PRECISION
				{
				mMEDIUM_PRECISION(); 

				}
				break;
			case 37 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:258: OUT
				{
				mOUT(); 

				}
				break;
			case 38 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:262: PRECISION
				{
				mPRECISION(); 

				}
				break;
			case 39 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:272: RETURN
				{
				mRETURN(); 

				}
				break;
			case 40 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:279: SAMPLER2D
				{
				mSAMPLER2D(); 

				}
				break;
			case 41 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:289: ISAMPLER2D
				{
				mISAMPLER2D(); 

				}
				break;
			case 42 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:300: USAMPLER2D
				{
				mUSAMPLER2D(); 

				}
				break;
			case 43 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:311: SAMPLER1D
				{
				mSAMPLER1D(); 

				}
				break;
			case 44 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:321: ISAMPLER1D
				{
				mISAMPLER1D(); 

				}
				break;
			case 45 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:332: USAMPLER1D
				{
				mUSAMPLER1D(); 

				}
				break;
			case 46 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:343: SAMPLERCUBE
				{
				mSAMPLERCUBE(); 

				}
				break;
			case 47 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:355: SAMPLER1DARRAY
				{
				mSAMPLER1DARRAY(); 

				}
				break;
			case 48 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:370: SAMPLER2DARRAY
				{
				mSAMPLER2DARRAY(); 

				}
				break;
			case 49 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:385: STRUCT
				{
				mSTRUCT(); 

				}
				break;
			case 50 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:392: TRUE
				{
				mTRUE(); 

				}
				break;
			case 51 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:397: UNIFORM
				{
				mUNIFORM(); 

				}
				break;
			case 52 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:405: VARYING
				{
				mVARYING(); 

				}
				break;
			case 53 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:413: VEC2
				{
				mVEC2(); 

				}
				break;
			case 54 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:418: VEC3
				{
				mVEC3(); 

				}
				break;
			case 55 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:423: VEC4
				{
				mVEC4(); 

				}
				break;
			case 56 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:428: VOID
				{
				mVOID(); 

				}
				break;
			case 57 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:433: WHILE
				{
				mWHILE(); 

				}
				break;
			case 58 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:439: IDENTIFIER
				{
				mIDENTIFIER(); 

				}
				break;
			case 59 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:450: WHITESPACE
				{
				mWHITESPACE(); 

				}
				break;
			case 60 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:461: COMMENT
				{
				mCOMMENT(); 

				}
				break;
			case 61 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:469: DIRECTIVE
				{
				mDIRECTIVE(); 

				}
				break;
			case 62 :
				// core/src/main/resources/shader/antlr/GlslArg.g:1:479: MULTILINE_COMMENT
				{
				mMULTILINE_COMMENT(); 

				}
				break;

		}
	}


	protected DFA5 dfa5 = new DFA5(this);
	static final String DFA5_eotS =
		"\1\uffff\1\32\7\uffff\21\32\4\uffff\10\32\1\110\5\32\1\116\1\122\17\32"+
		"\2\uffff\11\32\1\uffff\3\32\1\161\1\32\1\uffff\1\32\1\164\1\32\1\uffff"+
		"\3\32\1\171\13\32\1\u0087\1\u0088\1\u0089\1\u008a\2\32\1\u008d\5\32\1"+
		"\u0095\2\32\1\uffff\2\32\1\uffff\3\32\1\u009f\1\uffff\6\32\1\u00a6\1\32"+
		"\1\u00a8\1\u00a9\1\u00aa\1\u00ab\1\32\4\uffff\2\32\1\uffff\1\u00af\1\u00b0"+
		"\1\u00b1\1\u00b2\1\u00b3\2\32\1\uffff\1\u00b6\1\u00b7\1\u00b8\1\u00b9"+
		"\1\32\1\u00bb\1\u00bc\1\u00bd\1\32\1\uffff\6\32\1\uffff\1\32\4\uffff\1"+
		"\u00c6\2\32\5\uffff\2\32\4\uffff\1\32\3\uffff\2\32\1\u00ce\1\32\1\u00d0"+
		"\3\32\1\uffff\1\u00d4\2\32\1\u00d7\3\32\1\uffff\1\32\1\uffff\1\32\1\u00df"+
		"\1\u00e0\1\uffff\1\32\1\u00e2\1\uffff\7\32\2\uffff\1\u00ec\1\uffff\1\u00ed"+
		"\2\32\1\u00f0\1\u00f2\1\u00f4\3\32\2\uffff\1\u00f8\1\u00f9\1\uffff\1\32"+
		"\1\uffff\1\32\1\uffff\1\32\1\u00fd\1\u00fe\2\uffff\2\32\1\u0101\2\uffff"+
		"\2\32\1\uffff\2\32\1\u0106\1\u0107\2\uffff";
	static final String DFA5_eofS =
		"\u0108\uffff";
	static final String DFA5_minS =
		"\1\11\1\141\7\uffff\1\164\2\157\1\151\1\154\1\141\1\151\1\146\1\157\1"+
		"\165\1\162\1\145\1\141\1\156\1\162\1\141\1\150\2\uffff\1\52\1\uffff\1"+
		"\151\1\144\1\164\1\157\2\145\1\156\1\163\1\60\1\163\1\154\1\157\1\162"+
		"\1\147\2\60\1\145\1\141\1\167\1\164\1\145\1\164\1\155\1\162\1\141\1\151"+
		"\1\165\1\162\1\143\2\151\2\uffff\1\156\1\62\1\151\1\162\1\154\1\141\1"+
		"\143\1\163\1\143\1\uffff\1\145\1\163\1\141\1\60\1\150\1\uffff\1\165\1"+
		"\60\1\141\1\uffff\1\143\1\155\1\160\1\60\1\143\1\165\1\160\1\165\1\155"+
		"\1\146\1\145\1\171\1\62\1\144\1\154\4\60\1\165\1\151\1\60\1\153\1\62\1"+
		"\164\1\151\1\141\1\60\1\145\1\164\1\uffff\1\160\1\164\1\uffff\1\162\1"+
		"\62\1\160\1\60\1\uffff\1\151\1\162\1\154\1\143\1\160\1\157\1\60\1\151"+
		"\4\60\1\145\4\uffff\1\155\1\142\1\uffff\5\60\1\156\1\162\1\uffff\4\60"+
		"\1\151\3\60\1\154\1\uffff\1\163\1\156\1\145\1\164\1\154\1\162\1\uffff"+
		"\1\156\4\uffff\1\60\1\160\1\165\5\uffff\1\165\1\144\4\uffff\1\141\3\uffff"+
		"\1\145\1\151\1\60\1\162\1\60\1\145\1\155\1\147\1\uffff\1\60\1\164\1\145"+
		"\1\60\1\156\1\162\1\157\1\uffff\1\61\1\uffff\1\162\2\60\1\uffff\1\145"+
		"\1\60\1\uffff\1\164\1\61\1\156\2\104\1\165\1\61\2\uffff\1\60\1\uffff\1"+
		"\60\2\104\3\60\1\142\2\104\2\uffff\2\60\1\uffff\1\162\1\uffff\1\162\1"+
		"\uffff\1\145\2\60\2\uffff\2\162\1\60\2\uffff\2\141\1\uffff\2\171\2\60"+
		"\2\uffff";
	static final String DFA5_maxS =
		"\1\175\1\145\7\uffff\1\164\1\166\2\157\1\154\1\157\1\151\1\166\1\157\1"+
		"\165\1\162\1\145\1\164\1\163\1\162\1\157\1\150\2\uffff\1\57\1\uffff\1"+
		"\164\1\144\1\164\1\157\2\145\1\156\1\163\1\172\1\163\1\154\1\157\1\162"+
		"\1\147\2\172\1\145\1\141\1\167\1\164\1\145\1\164\1\155\1\162\1\141\1\151"+
		"\1\165\1\162\1\143\2\151\2\uffff\1\156\1\64\1\151\1\162\1\154\1\141\1"+
		"\143\1\164\1\143\1\uffff\1\145\1\163\1\141\1\172\1\150\1\uffff\1\165\1"+
		"\172\1\141\1\uffff\1\143\1\155\1\160\1\172\1\143\1\165\1\160\1\165\1\155"+
		"\1\146\1\145\1\171\1\64\1\144\1\154\4\172\1\165\1\151\1\172\1\153\1\64"+
		"\1\164\1\151\1\141\1\172\1\145\1\164\1\uffff\1\160\1\164\1\uffff\1\162"+
		"\1\64\1\160\1\172\1\uffff\1\151\1\162\1\154\1\143\1\160\1\157\1\172\1"+
		"\151\4\172\1\145\4\uffff\1\155\1\142\1\uffff\5\172\1\156\1\162\1\uffff"+
		"\4\172\1\151\3\172\1\154\1\uffff\1\163\1\156\1\145\1\164\1\154\1\162\1"+
		"\uffff\1\156\4\uffff\1\172\1\160\1\165\5\uffff\1\165\1\144\4\uffff\1\141"+
		"\3\uffff\1\145\1\151\1\172\1\162\1\172\1\145\1\155\1\147\1\uffff\1\172"+
		"\1\164\1\145\1\172\1\156\1\162\1\157\1\uffff\1\103\1\uffff\1\162\2\172"+
		"\1\uffff\1\145\1\172\1\uffff\1\164\1\62\1\156\2\104\1\165\1\62\2\uffff"+
		"\1\172\1\uffff\1\172\2\104\3\172\1\142\2\104\2\uffff\2\172\1\uffff\1\162"+
		"\1\uffff\1\162\1\uffff\1\145\2\172\2\uffff\2\162\1\172\2\uffff\2\141\1"+
		"\uffff\2\171\2\172\2\uffff";
	static final String DFA5_acceptS =
		"\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\21\uffff\1\72\1\73\1\uffff\1\75"+
		"\37\uffff\1\74\1\76\11\uffff\1\22\5\uffff\1\30\3\uffff\1\31\36\uffff\1"+
		"\26\2\uffff\1\33\4\uffff\1\45\15\uffff\1\1\1\41\1\42\1\43\2\uffff\1\12"+
		"\7\uffff\1\23\11\uffff\1\40\6\uffff\1\62\1\uffff\1\65\1\66\1\67\1\70\3"+
		"\uffff\1\13\1\14\1\15\1\16\1\17\2\uffff\1\24\1\25\1\27\1\32\1\uffff\1"+
		"\35\1\36\1\37\10\uffff\1\71\7\uffff\1\47\1\uffff\1\61\3\uffff\1\44\2\uffff"+
		"\1\21\7\uffff\1\63\1\64\1\uffff\1\20\11\uffff\1\11\1\34\2\uffff\1\46\1"+
		"\uffff\1\50\1\uffff\1\53\3\uffff\1\51\1\54\3\uffff\1\52\1\55\2\uffff\1"+
		"\56\4\uffff\1\60\1\57";
	static final String DFA5_specialS =
		"\u0108\uffff}>";
	static final String[] DFA5_transitionS = {
			"\2\33\1\uffff\2\33\22\uffff\1\33\2\uffff\1\35\4\uffff\1\4\1\5\5\uffff"+
			"\1\34\13\uffff\1\10\5\uffff\32\32\1\6\1\uffff\1\7\1\uffff\1\32\1\uffff"+
			"\1\11\1\12\1\13\1\14\1\15\1\16\1\32\1\17\1\20\2\32\1\21\1\1\1\32\1\22"+
			"\1\23\1\32\1\24\1\25\1\27\1\26\1\30\1\31\3\32\1\2\1\uffff\1\3",
			"\1\36\3\uffff\1\37",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\40",
			"\1\41\2\uffff\1\42\3\uffff\1\43",
			"\1\44",
			"\1\45\5\uffff\1\46",
			"\1\47",
			"\1\50\12\uffff\1\51\2\uffff\1\52",
			"\1\53",
			"\1\54\7\uffff\1\55\4\uffff\1\57\2\uffff\1\56",
			"\1\60",
			"\1\61",
			"\1\62",
			"\1\63",
			"\1\64\22\uffff\1\65",
			"\1\67\4\uffff\1\66",
			"\1\70",
			"\1\71\3\uffff\1\72\11\uffff\1\73",
			"\1\74",
			"",
			"",
			"\1\76\4\uffff\1\75",
			"",
			"\1\77\12\uffff\1\100",
			"\1\101",
			"\1\102",
			"\1\103",
			"\1\104",
			"\1\105",
			"\1\106",
			"\1\107",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\111",
			"\1\112",
			"\1\113",
			"\1\114",
			"\1\115",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\16\32\1\117\4\32\1\120\1\32"+
			"\1\121\4\32",
			"\1\123",
			"\1\124",
			"\1\125",
			"\1\126",
			"\1\127",
			"\1\130",
			"\1\131",
			"\1\132",
			"\1\133",
			"\1\134",
			"\1\135",
			"\1\136",
			"\1\137",
			"\1\140",
			"\1\141",
			"",
			"",
			"\1\142",
			"\1\143\1\144\1\145",
			"\1\146",
			"\1\147",
			"\1\150",
			"\1\151",
			"\1\152",
			"\1\153\1\154",
			"\1\155",
			"",
			"\1\156",
			"\1\157",
			"\1\160",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\162",
			"",
			"\1\163",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\165",
			"",
			"\1\166",
			"\1\167",
			"\1\170",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\172",
			"\1\173",
			"\1\174",
			"\1\175",
			"\1\176",
			"\1\177",
			"\1\u0080",
			"\1\u0081",
			"\1\u0082\1\u0083\1\u0084",
			"\1\u0085",
			"\1\u0086",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u008b",
			"\1\u008c",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u008e",
			"\1\u008f\1\u0090\1\u0091",
			"\1\u0092",
			"\1\u0093",
			"\1\u0094",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u0096",
			"\1\u0097",
			"",
			"\1\u0098",
			"\1\u0099",
			"",
			"\1\u009a",
			"\1\u009b\1\u009c\1\u009d",
			"\1\u009e",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"\1\u00a0",
			"\1\u00a1",
			"\1\u00a2",
			"\1\u00a3",
			"\1\u00a4",
			"\1\u00a5",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00a7",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00ac",
			"",
			"",
			"",
			"",
			"\1\u00ad",
			"\1\u00ae",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00b4",
			"\1\u00b5",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00ba",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00be",
			"",
			"\1\u00bf",
			"\1\u00c0",
			"\1\u00c1",
			"\1\u00c2",
			"\1\u00c3",
			"\1\u00c4",
			"",
			"\1\u00c5",
			"",
			"",
			"",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00c7",
			"\1\u00c8",
			"",
			"",
			"",
			"",
			"",
			"\1\u00c9",
			"\1\u00ca",
			"",
			"",
			"",
			"",
			"\1\u00cb",
			"",
			"",
			"",
			"\1\u00cc",
			"\1\u00cd",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00cf",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00d1",
			"\1\u00d2",
			"\1\u00d3",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00d5",
			"\1\u00d6",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00d8",
			"\1\u00d9",
			"\1\u00da",
			"",
			"\1\u00dc\1\u00db\20\uffff\1\u00dd",
			"",
			"\1\u00de",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"\1\u00e1",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"\1\u00e3",
			"\1\u00e5\1\u00e4",
			"\1\u00e6",
			"\1\u00e7",
			"\1\u00e8",
			"\1\u00e9",
			"\1\u00eb\1\u00ea",
			"",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00ee",
			"\1\u00ef",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\1\u00f1\31\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\1\u00f3\31\32\4\uffff\1\32\1\uffff\32\32",
			"\1\u00f5",
			"\1\u00f6",
			"\1\u00f7",
			"",
			"",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"\1\u00fa",
			"",
			"\1\u00fb",
			"",
			"\1\u00fc",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"",
			"\1\u00ff",
			"\1\u0100",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			"",
			"\1\u0102",
			"\1\u0103",
			"",
			"\1\u0104",
			"\1\u0105",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
			"",
			""
	};

	static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
	static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
	static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
	static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
	static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
	static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
	static final short[][] DFA5_transition;

	static {
		int numStates = DFA5_transitionS.length;
		DFA5_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
		}
	}

	protected class DFA5 extends DFA {

		public DFA5(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 5;
			this.eot = DFA5_eot;
			this.eof = DFA5_eof;
			this.min = DFA5_min;
			this.max = DFA5_max;
			this.accept = DFA5_accept;
			this.special = DFA5_special;
			this.transition = DFA5_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__65 | LCURLY | RCURLY | LPAREN | RPAREN | LBRACKET | RBRACKET | SEMI | ATTRIBUTE | BOOL | BREAK | BVEC2 | BVEC3 | BVEC4 | CONST | CONTINUE | DISCARD | DO | ELSE | FALSE | FLOAT | FOR | HIGH_PRECISION | IF | IN | INOUT | INT | INVARIANT | IVEC2 | IVEC3 | IVEC4 | LOW_PRECISION | MAT2 | MAT3 | MAT4 | MEDIUM_PRECISION | OUT | PRECISION | RETURN | SAMPLER2D | ISAMPLER2D | USAMPLER2D | SAMPLER1D | ISAMPLER1D | USAMPLER1D | SAMPLERCUBE | SAMPLER1DARRAY | SAMPLER2DARRAY | STRUCT | TRUE | UNIFORM | VARYING | VEC2 | VEC3 | VEC4 | VOID | WHILE | IDENTIFIER | WHITESPACE | COMMENT | DIRECTIVE | MULTILINE_COMMENT );";
		}
	}

}
