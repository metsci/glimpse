// $ANTLR 3.5.2 core/src/main/resources/shader/antlr/GlslArg.g 2016-08-03 12:09:15

package com.metsci.glimpse.gl.shader.grammar;

import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderArgType;
import com.metsci.glimpse.gl.shader.ShaderArgInOut;
import com.metsci.glimpse.gl.shader.ShaderArgQualifier;

import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class GlslArgParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ATTRIBUTE", "BOOL", "BREAK", 
		"BVEC2", "BVEC3", "BVEC4", "COMMENT", "CONST", "CONTINUE", "DIRECTIVE", 
		"DISCARD", "DO", "ELSE", "FALSE", "FLOAT", "FOR", "HIGH_PRECISION", "IDENTIFIER", 
		"IF", "IN", "INOUT", "INT", "INVARIANT", "ISAMPLER1D", "ISAMPLER2D", "IVEC2", 
		"IVEC3", "IVEC4", "LBRACKET", "LCURLY", "LOW_PRECISION", "LPAREN", "MAT2", 
		"MAT3", "MAT4", "MEDIUM_PRECISION", "MULTILINE_COMMENT", "OUT", "PRECISION", 
		"RBRACKET", "RCURLY", "RETURN", "RPAREN", "SAMPLER1D", "SAMPLER1DARRAY", 
		"SAMPLER2D", "SAMPLER2DARRAY", "SAMPLERCUBE", "SEMI", "STRUCT", "TRUE", 
		"UNIFORM", "USAMPLER1D", "USAMPLER2D", "VARYING", "VEC2", "VEC3", "VEC4", 
		"VOID", "WHILE", "WHITESPACE", "'main'"
	};
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
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public GlslArgParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public GlslArgParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return GlslArgParser.tokenNames; }
	@Override public String getGrammarFileName() { return "core/src/main/resources/shader/antlr/GlslArg.g"; }


	public static class shader_return extends ParserRuleReturnScope {
		public List<ShaderArg> result;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "shader"
	// core/src/main/resources/shader/antlr/GlslArg.g:28:1: shader returns [List<ShaderArg> result] : ( parameter )* VOID ! 'main' ! LPAREN ! ( ( VOID )? ) ! RPAREN ! LCURLY !;
	public final GlslArgParser.shader_return shader() throws RecognitionException {
		GlslArgParser.shader_return retval = new GlslArgParser.shader_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token VOID2=null;
		Token string_literal3=null;
		Token LPAREN4=null;
		Token VOID5=null;
		Token RPAREN6=null;
		Token LCURLY7=null;
		ParserRuleReturnScope parameter1 =null;

		CommonTree VOID2_tree=null;
		CommonTree string_literal3_tree=null;
		CommonTree LPAREN4_tree=null;
		CommonTree VOID5_tree=null;
		CommonTree RPAREN6_tree=null;
		CommonTree LCURLY7_tree=null;


		  List<ShaderArg> result = new ArrayList<ShaderArg>();

		try {
			// core/src/main/resources/shader/antlr/GlslArg.g:33:3: ( ( parameter )* VOID ! 'main' ! LPAREN ! ( ( VOID )? ) ! RPAREN ! LCURLY !)
			// core/src/main/resources/shader/antlr/GlslArg.g:33:5: ( parameter )* VOID ! 'main' ! LPAREN ! ( ( VOID )? ) ! RPAREN ! LCURLY !
			{
			root_0 = (CommonTree)adaptor.nil();


			// core/src/main/resources/shader/antlr/GlslArg.g:33:5: ( parameter )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==VOID) ) {
					int LA1_1 = input.LA(2);
					if ( (LA1_1==IDENTIFIER) ) {
						alt1=1;
					}

				}
				else if ( ((LA1_0 >= ATTRIBUTE && LA1_0 <= BOOL)||(LA1_0 >= BVEC2 && LA1_0 <= BVEC4)||LA1_0==CONST||LA1_0==FLOAT||(LA1_0 >= IN && LA1_0 <= IVEC4)||(LA1_0 >= MAT2 && LA1_0 <= MAT4)||LA1_0==OUT||(LA1_0 >= SAMPLER1D && LA1_0 <= SAMPLERCUBE)||(LA1_0 >= UNIFORM && LA1_0 <= VEC4)) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:33:7: parameter
					{
					pushFollow(FOLLOW_parameter_in_shader71);
					parameter1=parameter();
					state._fsp--;

					adaptor.addChild(root_0, parameter1.getTree());

					 result.add( (parameter1!=null?((GlslArgParser.parameter_return)parameter1).result:null) ); 
					}
					break;

				default :
					break loop1;
				}
			}

			VOID2=(Token)match(input,VOID,FOLLOW_VOID_in_shader78); 
			string_literal3=(Token)match(input,65,FOLLOW_65_in_shader81); 
			LPAREN4=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_shader84); 
			// core/src/main/resources/shader/antlr/GlslArg.g:33:79: ( ( VOID )? )
			// core/src/main/resources/shader/antlr/GlslArg.g:33:80: ( VOID )?
			{
			// core/src/main/resources/shader/antlr/GlslArg.g:33:80: ( VOID )?
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==VOID) ) {
				alt2=1;
			}
			switch (alt2) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:33:80: VOID
					{
					VOID5=(Token)match(input,VOID,FOLLOW_VOID_in_shader88); 
					VOID5_tree = (CommonTree)adaptor.create(VOID5);
					adaptor.addChild(root_0, VOID5_tree);

					}
					break;

			}

			}

			RPAREN6=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_shader93); 
			LCURLY7=(Token)match(input,LCURLY,FOLLOW_LCURLY_in_shader96); 

			    retval.result = result;
			  
			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "shader"


	public static class parameter_return extends ParserRuleReturnScope {
		public ShaderArg result;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parameter"
	// core/src/main/resources/shader/antlr/GlslArg.g:39:1: parameter returns [ShaderArg result] : (pqual= qualifier )? (pinout= inout )? ptype= type pname= identifier ( LBRACKET RBRACKET )? SEMI !;
	public final GlslArgParser.parameter_return parameter() throws RecognitionException {
		GlslArgParser.parameter_return retval = new GlslArgParser.parameter_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LBRACKET8=null;
		Token RBRACKET9=null;
		Token SEMI10=null;
		ParserRuleReturnScope pqual =null;
		ParserRuleReturnScope pinout =null;
		ParserRuleReturnScope ptype =null;
		ParserRuleReturnScope pname =null;

		CommonTree LBRACKET8_tree=null;
		CommonTree RBRACKET9_tree=null;
		CommonTree SEMI10_tree=null;


		  String name;
		  ShaderArgType type;
		  ShaderArgQualifier qual;
		  ShaderArgInOut inout;

		try {
			// core/src/main/resources/shader/antlr/GlslArg.g:47:3: ( (pqual= qualifier )? (pinout= inout )? ptype= type pname= identifier ( LBRACKET RBRACKET )? SEMI !)
			// core/src/main/resources/shader/antlr/GlslArg.g:47:5: (pqual= qualifier )? (pinout= inout )? ptype= type pname= identifier ( LBRACKET RBRACKET )? SEMI !
			{
			root_0 = (CommonTree)adaptor.nil();


			// core/src/main/resources/shader/antlr/GlslArg.g:47:10: (pqual= qualifier )?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==ATTRIBUTE||LA3_0==CONST||LA3_0==INVARIANT||LA3_0==UNIFORM||LA3_0==VARYING) ) {
				alt3=1;
			}
			switch (alt3) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:47:10: pqual= qualifier
					{
					pushFollow(FOLLOW_qualifier_in_parameter125);
					pqual=qualifier();
					state._fsp--;

					adaptor.addChild(root_0, pqual.getTree());

					}
					break;

			}

			 qual = (pqual!=null?((GlslArgParser.qualifier_return)pqual).result:null); 
			// core/src/main/resources/shader/antlr/GlslArg.g:48:11: (pinout= inout )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( ((LA4_0 >= IN && LA4_0 <= INOUT)||LA4_0==OUT) ) {
				alt4=1;
			}
			switch (alt4) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:48:11: pinout= inout
					{
					pushFollow(FOLLOW_inout_in_parameter136);
					pinout=inout();
					state._fsp--;

					adaptor.addChild(root_0, pinout.getTree());

					}
					break;

			}

			 inout = (pinout!=null?((GlslArgParser.inout_return)pinout).result:null); 
			pushFollow(FOLLOW_type_in_parameter147);
			ptype=type();
			state._fsp--;

			adaptor.addChild(root_0, ptype.getTree());

			 type = (ptype!=null?((GlslArgParser.type_return)ptype).result:null); 
			pushFollow(FOLLOW_identifier_in_parameter157);
			pname=identifier();
			state._fsp--;

			adaptor.addChild(root_0, pname.getTree());

			 name = (pname!=null?input.toString(pname.start,pname.stop):null); 
			// core/src/main/resources/shader/antlr/GlslArg.g:51:5: ( LBRACKET RBRACKET )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==LBRACKET) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:51:6: LBRACKET RBRACKET
					{
					LBRACKET8=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_parameter166); 
					LBRACKET8_tree = (CommonTree)adaptor.create(LBRACKET8);
					adaptor.addChild(root_0, LBRACKET8_tree);

					RBRACKET9=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_parameter168); 
					RBRACKET9_tree = (CommonTree)adaptor.create(RBRACKET9);
					adaptor.addChild(root_0, RBRACKET9_tree);

					}
					break;

			}

			SEMI10=(Token)match(input,SEMI,FOLLOW_SEMI_in_parameter176); 

			    retval.result = new ShaderArg( name, type, qual, inout );
			  
			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "parameter"


	public static class type_return extends ParserRuleReturnScope {
		public ShaderArgType result;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "type"
	// core/src/main/resources/shader/antlr/GlslArg.g:58:1: type returns [ShaderArgType result] : ( VOID | FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | SAMPLER1D | ISAMPLER1D | USAMPLER1D | SAMPLER2D | ISAMPLER2D | USAMPLER2D | SAMPLERCUBE | SAMPLER1DARRAY | SAMPLER2DARRAY );
	public final GlslArgParser.type_return type() throws RecognitionException {
		GlslArgParser.type_return retval = new GlslArgParser.type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token VOID11=null;
		Token FLOAT12=null;
		Token INT13=null;
		Token BOOL14=null;
		Token VEC215=null;
		Token VEC316=null;
		Token VEC417=null;
		Token BVEC218=null;
		Token BVEC319=null;
		Token BVEC420=null;
		Token IVEC221=null;
		Token IVEC322=null;
		Token IVEC423=null;
		Token MAT224=null;
		Token MAT325=null;
		Token MAT426=null;
		Token SAMPLER1D27=null;
		Token ISAMPLER1D28=null;
		Token USAMPLER1D29=null;
		Token SAMPLER2D30=null;
		Token ISAMPLER2D31=null;
		Token USAMPLER2D32=null;
		Token SAMPLERCUBE33=null;
		Token SAMPLER1DARRAY34=null;
		Token SAMPLER2DARRAY35=null;

		CommonTree VOID11_tree=null;
		CommonTree FLOAT12_tree=null;
		CommonTree INT13_tree=null;
		CommonTree BOOL14_tree=null;
		CommonTree VEC215_tree=null;
		CommonTree VEC316_tree=null;
		CommonTree VEC417_tree=null;
		CommonTree BVEC218_tree=null;
		CommonTree BVEC319_tree=null;
		CommonTree BVEC420_tree=null;
		CommonTree IVEC221_tree=null;
		CommonTree IVEC322_tree=null;
		CommonTree IVEC423_tree=null;
		CommonTree MAT224_tree=null;
		CommonTree MAT325_tree=null;
		CommonTree MAT426_tree=null;
		CommonTree SAMPLER1D27_tree=null;
		CommonTree ISAMPLER1D28_tree=null;
		CommonTree USAMPLER1D29_tree=null;
		CommonTree SAMPLER2D30_tree=null;
		CommonTree ISAMPLER2D31_tree=null;
		CommonTree USAMPLER2D32_tree=null;
		CommonTree SAMPLERCUBE33_tree=null;
		CommonTree SAMPLER1DARRAY34_tree=null;
		CommonTree SAMPLER2DARRAY35_tree=null;

		try {
			// core/src/main/resources/shader/antlr/GlslArg.g:59:3: ( VOID | FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | SAMPLER1D | ISAMPLER1D | USAMPLER1D | SAMPLER2D | ISAMPLER2D | USAMPLER2D | SAMPLERCUBE | SAMPLER1DARRAY | SAMPLER2DARRAY )
			int alt6=25;
			switch ( input.LA(1) ) {
			case VOID:
				{
				alt6=1;
				}
				break;
			case FLOAT:
				{
				alt6=2;
				}
				break;
			case INT:
				{
				alt6=3;
				}
				break;
			case BOOL:
				{
				alt6=4;
				}
				break;
			case VEC2:
				{
				alt6=5;
				}
				break;
			case VEC3:
				{
				alt6=6;
				}
				break;
			case VEC4:
				{
				alt6=7;
				}
				break;
			case BVEC2:
				{
				alt6=8;
				}
				break;
			case BVEC3:
				{
				alt6=9;
				}
				break;
			case BVEC4:
				{
				alt6=10;
				}
				break;
			case IVEC2:
				{
				alt6=11;
				}
				break;
			case IVEC3:
				{
				alt6=12;
				}
				break;
			case IVEC4:
				{
				alt6=13;
				}
				break;
			case MAT2:
				{
				alt6=14;
				}
				break;
			case MAT3:
				{
				alt6=15;
				}
				break;
			case MAT4:
				{
				alt6=16;
				}
				break;
			case SAMPLER1D:
				{
				alt6=17;
				}
				break;
			case ISAMPLER1D:
				{
				alt6=18;
				}
				break;
			case USAMPLER1D:
				{
				alt6=19;
				}
				break;
			case SAMPLER2D:
				{
				alt6=20;
				}
				break;
			case ISAMPLER2D:
				{
				alt6=21;
				}
				break;
			case USAMPLER2D:
				{
				alt6=22;
				}
				break;
			case SAMPLERCUBE:
				{
				alt6=23;
				}
				break;
			case SAMPLER1DARRAY:
				{
				alt6=24;
				}
				break;
			case SAMPLER2DARRAY:
				{
				alt6=25;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}
			switch (alt6) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:59:5: VOID
					{
					root_0 = (CommonTree)adaptor.nil();


					VOID11=(Token)match(input,VOID,FOLLOW_VOID_in_type198); 
					VOID11_tree = (CommonTree)adaptor.create(VOID11);
					adaptor.addChild(root_0, VOID11_tree);

					}
					break;
				case 2 :
					// core/src/main/resources/shader/antlr/GlslArg.g:60:5: FLOAT
					{
					root_0 = (CommonTree)adaptor.nil();


					FLOAT12=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_type204); 
					FLOAT12_tree = (CommonTree)adaptor.create(FLOAT12);
					adaptor.addChild(root_0, FLOAT12_tree);

					 retval.result = ShaderArgType.FLOAT; 
					}
					break;
				case 3 :
					// core/src/main/resources/shader/antlr/GlslArg.g:61:5: INT
					{
					root_0 = (CommonTree)adaptor.nil();


					INT13=(Token)match(input,INT,FOLLOW_INT_in_type219); 
					INT13_tree = (CommonTree)adaptor.create(INT13);
					adaptor.addChild(root_0, INT13_tree);

					 retval.result = ShaderArgType.INT; 
					}
					break;
				case 4 :
					// core/src/main/resources/shader/antlr/GlslArg.g:62:5: BOOL
					{
					root_0 = (CommonTree)adaptor.nil();


					BOOL14=(Token)match(input,BOOL,FOLLOW_BOOL_in_type236); 
					BOOL14_tree = (CommonTree)adaptor.create(BOOL14);
					adaptor.addChild(root_0, BOOL14_tree);

					 retval.result = ShaderArgType.BOOLEAN; 
					}
					break;
				case 5 :
					// core/src/main/resources/shader/antlr/GlslArg.g:63:5: VEC2
					{
					root_0 = (CommonTree)adaptor.nil();


					VEC215=(Token)match(input,VEC2,FOLLOW_VEC2_in_type252); 
					VEC215_tree = (CommonTree)adaptor.create(VEC215);
					adaptor.addChild(root_0, VEC215_tree);

					 retval.result = ShaderArgType.VEC2; 
					}
					break;
				case 6 :
					// core/src/main/resources/shader/antlr/GlslArg.g:64:5: VEC3
					{
					root_0 = (CommonTree)adaptor.nil();


					VEC316=(Token)match(input,VEC3,FOLLOW_VEC3_in_type268); 
					VEC316_tree = (CommonTree)adaptor.create(VEC316);
					adaptor.addChild(root_0, VEC316_tree);

					 retval.result = ShaderArgType.VEC3; 
					}
					break;
				case 7 :
					// core/src/main/resources/shader/antlr/GlslArg.g:65:5: VEC4
					{
					root_0 = (CommonTree)adaptor.nil();


					VEC417=(Token)match(input,VEC4,FOLLOW_VEC4_in_type284); 
					VEC417_tree = (CommonTree)adaptor.create(VEC417);
					adaptor.addChild(root_0, VEC417_tree);

					 retval.result = ShaderArgType.VEC4; 
					}
					break;
				case 8 :
					// core/src/main/resources/shader/antlr/GlslArg.g:66:5: BVEC2
					{
					root_0 = (CommonTree)adaptor.nil();


					BVEC218=(Token)match(input,BVEC2,FOLLOW_BVEC2_in_type300); 
					BVEC218_tree = (CommonTree)adaptor.create(BVEC218);
					adaptor.addChild(root_0, BVEC218_tree);

					 retval.result = ShaderArgType.BVEC2; 
					}
					break;
				case 9 :
					// core/src/main/resources/shader/antlr/GlslArg.g:67:5: BVEC3
					{
					root_0 = (CommonTree)adaptor.nil();


					BVEC319=(Token)match(input,BVEC3,FOLLOW_BVEC3_in_type315); 
					BVEC319_tree = (CommonTree)adaptor.create(BVEC319);
					adaptor.addChild(root_0, BVEC319_tree);

					 retval.result = ShaderArgType.BVEC3; 
					}
					break;
				case 10 :
					// core/src/main/resources/shader/antlr/GlslArg.g:68:5: BVEC4
					{
					root_0 = (CommonTree)adaptor.nil();


					BVEC420=(Token)match(input,BVEC4,FOLLOW_BVEC4_in_type330); 
					BVEC420_tree = (CommonTree)adaptor.create(BVEC420);
					adaptor.addChild(root_0, BVEC420_tree);

					 retval.result = ShaderArgType.BVEC4; 
					}
					break;
				case 11 :
					// core/src/main/resources/shader/antlr/GlslArg.g:69:5: IVEC2
					{
					root_0 = (CommonTree)adaptor.nil();


					IVEC221=(Token)match(input,IVEC2,FOLLOW_IVEC2_in_type345); 
					IVEC221_tree = (CommonTree)adaptor.create(IVEC221);
					adaptor.addChild(root_0, IVEC221_tree);

					 retval.result = ShaderArgType.IVEC2; 
					}
					break;
				case 12 :
					// core/src/main/resources/shader/antlr/GlslArg.g:70:5: IVEC3
					{
					root_0 = (CommonTree)adaptor.nil();


					IVEC322=(Token)match(input,IVEC3,FOLLOW_IVEC3_in_type360); 
					IVEC322_tree = (CommonTree)adaptor.create(IVEC322);
					adaptor.addChild(root_0, IVEC322_tree);

					 retval.result = ShaderArgType.IVEC3; 
					}
					break;
				case 13 :
					// core/src/main/resources/shader/antlr/GlslArg.g:71:5: IVEC4
					{
					root_0 = (CommonTree)adaptor.nil();


					IVEC423=(Token)match(input,IVEC4,FOLLOW_IVEC4_in_type375); 
					IVEC423_tree = (CommonTree)adaptor.create(IVEC423);
					adaptor.addChild(root_0, IVEC423_tree);

					 retval.result = ShaderArgType.IVEC4; 
					}
					break;
				case 14 :
					// core/src/main/resources/shader/antlr/GlslArg.g:72:5: MAT2
					{
					root_0 = (CommonTree)adaptor.nil();


					MAT224=(Token)match(input,MAT2,FOLLOW_MAT2_in_type390); 
					MAT224_tree = (CommonTree)adaptor.create(MAT224);
					adaptor.addChild(root_0, MAT224_tree);

					 retval.result = ShaderArgType.MAT2; 
					}
					break;
				case 15 :
					// core/src/main/resources/shader/antlr/GlslArg.g:73:5: MAT3
					{
					root_0 = (CommonTree)adaptor.nil();


					MAT325=(Token)match(input,MAT3,FOLLOW_MAT3_in_type406); 
					MAT325_tree = (CommonTree)adaptor.create(MAT325);
					adaptor.addChild(root_0, MAT325_tree);

					 retval.result = ShaderArgType.MAT3; 
					}
					break;
				case 16 :
					// core/src/main/resources/shader/antlr/GlslArg.g:74:5: MAT4
					{
					root_0 = (CommonTree)adaptor.nil();


					MAT426=(Token)match(input,MAT4,FOLLOW_MAT4_in_type422); 
					MAT426_tree = (CommonTree)adaptor.create(MAT426);
					adaptor.addChild(root_0, MAT426_tree);

					 retval.result = ShaderArgType.MAT4; 
					}
					break;
				case 17 :
					// core/src/main/resources/shader/antlr/GlslArg.g:75:5: SAMPLER1D
					{
					root_0 = (CommonTree)adaptor.nil();


					SAMPLER1D27=(Token)match(input,SAMPLER1D,FOLLOW_SAMPLER1D_in_type438); 
					SAMPLER1D27_tree = (CommonTree)adaptor.create(SAMPLER1D27);
					adaptor.addChild(root_0, SAMPLER1D27_tree);

					 retval.result = ShaderArgType.SAMPLER_1D; 
					}
					break;
				case 18 :
					// core/src/main/resources/shader/antlr/GlslArg.g:76:5: ISAMPLER1D
					{
					root_0 = (CommonTree)adaptor.nil();


					ISAMPLER1D28=(Token)match(input,ISAMPLER1D,FOLLOW_ISAMPLER1D_in_type449); 
					ISAMPLER1D28_tree = (CommonTree)adaptor.create(ISAMPLER1D28);
					adaptor.addChild(root_0, ISAMPLER1D28_tree);

					 retval.result = ShaderArgType.ISAMPLER_1D; 
					}
					break;
				case 19 :
					// core/src/main/resources/shader/antlr/GlslArg.g:77:5: USAMPLER1D
					{
					root_0 = (CommonTree)adaptor.nil();


					USAMPLER1D29=(Token)match(input,USAMPLER1D,FOLLOW_USAMPLER1D_in_type459); 
					USAMPLER1D29_tree = (CommonTree)adaptor.create(USAMPLER1D29);
					adaptor.addChild(root_0, USAMPLER1D29_tree);

					 retval.result = ShaderArgType.USAMPLER_1D; 
					}
					break;
				case 20 :
					// core/src/main/resources/shader/antlr/GlslArg.g:78:5: SAMPLER2D
					{
					root_0 = (CommonTree)adaptor.nil();


					SAMPLER2D30=(Token)match(input,SAMPLER2D,FOLLOW_SAMPLER2D_in_type469); 
					SAMPLER2D30_tree = (CommonTree)adaptor.create(SAMPLER2D30);
					adaptor.addChild(root_0, SAMPLER2D30_tree);

					 retval.result = ShaderArgType.SAMPLER_2D; 
					}
					break;
				case 21 :
					// core/src/main/resources/shader/antlr/GlslArg.g:79:5: ISAMPLER2D
					{
					root_0 = (CommonTree)adaptor.nil();


					ISAMPLER2D31=(Token)match(input,ISAMPLER2D,FOLLOW_ISAMPLER2D_in_type480); 
					ISAMPLER2D31_tree = (CommonTree)adaptor.create(ISAMPLER2D31);
					adaptor.addChild(root_0, ISAMPLER2D31_tree);

					 retval.result = ShaderArgType.ISAMPLER_2D; 
					}
					break;
				case 22 :
					// core/src/main/resources/shader/antlr/GlslArg.g:80:5: USAMPLER2D
					{
					root_0 = (CommonTree)adaptor.nil();


					USAMPLER2D32=(Token)match(input,USAMPLER2D,FOLLOW_USAMPLER2D_in_type490); 
					USAMPLER2D32_tree = (CommonTree)adaptor.create(USAMPLER2D32);
					adaptor.addChild(root_0, USAMPLER2D32_tree);

					 retval.result = ShaderArgType.USAMPLER_2D; 
					}
					break;
				case 23 :
					// core/src/main/resources/shader/antlr/GlslArg.g:81:5: SAMPLERCUBE
					{
					root_0 = (CommonTree)adaptor.nil();


					SAMPLERCUBE33=(Token)match(input,SAMPLERCUBE,FOLLOW_SAMPLERCUBE_in_type500); 
					SAMPLERCUBE33_tree = (CommonTree)adaptor.create(SAMPLERCUBE33);
					adaptor.addChild(root_0, SAMPLERCUBE33_tree);

					 retval.result = ShaderArgType.SAMPLER_CUBE; 
					}
					break;
				case 24 :
					// core/src/main/resources/shader/antlr/GlslArg.g:82:5: SAMPLER1DARRAY
					{
					root_0 = (CommonTree)adaptor.nil();


					SAMPLER1DARRAY34=(Token)match(input,SAMPLER1DARRAY,FOLLOW_SAMPLER1DARRAY_in_type509); 
					SAMPLER1DARRAY34_tree = (CommonTree)adaptor.create(SAMPLER1DARRAY34);
					adaptor.addChild(root_0, SAMPLER1DARRAY34_tree);

					 retval.result = ShaderArgType.SAMPLER_1D_ARRAY; 
					}
					break;
				case 25 :
					// core/src/main/resources/shader/antlr/GlslArg.g:83:5: SAMPLER2DARRAY
					{
					root_0 = (CommonTree)adaptor.nil();


					SAMPLER2DARRAY35=(Token)match(input,SAMPLER2DARRAY,FOLLOW_SAMPLER2DARRAY_in_type520); 
					SAMPLER2DARRAY35_tree = (CommonTree)adaptor.create(SAMPLER2DARRAY35);
					adaptor.addChild(root_0, SAMPLER2DARRAY35_tree);

					 retval.result = ShaderArgType.SAMPLER_2D_ARRAY; 
					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "type"


	public static class qualifier_return extends ParserRuleReturnScope {
		public ShaderArgQualifier result;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "qualifier"
	// core/src/main/resources/shader/antlr/GlslArg.g:86:1: qualifier returns [ShaderArgQualifier result] : ( CONST | ATTRIBUTE | VARYING | INVARIANT VARYING | UNIFORM );
	public final GlslArgParser.qualifier_return qualifier() throws RecognitionException {
		GlslArgParser.qualifier_return retval = new GlslArgParser.qualifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CONST36=null;
		Token ATTRIBUTE37=null;
		Token VARYING38=null;
		Token INVARIANT39=null;
		Token VARYING40=null;
		Token UNIFORM41=null;

		CommonTree CONST36_tree=null;
		CommonTree ATTRIBUTE37_tree=null;
		CommonTree VARYING38_tree=null;
		CommonTree INVARIANT39_tree=null;
		CommonTree VARYING40_tree=null;
		CommonTree UNIFORM41_tree=null;

		try {
			// core/src/main/resources/shader/antlr/GlslArg.g:87:3: ( CONST | ATTRIBUTE | VARYING | INVARIANT VARYING | UNIFORM )
			int alt7=5;
			switch ( input.LA(1) ) {
			case CONST:
				{
				alt7=1;
				}
				break;
			case ATTRIBUTE:
				{
				alt7=2;
				}
				break;
			case VARYING:
				{
				alt7=3;
				}
				break;
			case INVARIANT:
				{
				alt7=4;
				}
				break;
			case UNIFORM:
				{
				alt7=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}
			switch (alt7) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:87:5: CONST
					{
					root_0 = (CommonTree)adaptor.nil();


					CONST36=(Token)match(input,CONST,FOLLOW_CONST_in_qualifier542); 
					CONST36_tree = (CommonTree)adaptor.create(CONST36);
					adaptor.addChild(root_0, CONST36_tree);

					 retval.result = ShaderArgQualifier.CONST; 
					}
					break;
				case 2 :
					// core/src/main/resources/shader/antlr/GlslArg.g:88:5: ATTRIBUTE
					{
					root_0 = (CommonTree)adaptor.nil();


					ATTRIBUTE37=(Token)match(input,ATTRIBUTE,FOLLOW_ATTRIBUTE_in_qualifier565); 
					ATTRIBUTE37_tree = (CommonTree)adaptor.create(ATTRIBUTE37);
					adaptor.addChild(root_0, ATTRIBUTE37_tree);

					 retval.result = ShaderArgQualifier.ATTRIBUTE; 
					}
					break;
				case 3 :
					// core/src/main/resources/shader/antlr/GlslArg.g:89:5: VARYING
					{
					root_0 = (CommonTree)adaptor.nil();


					VARYING38=(Token)match(input,VARYING,FOLLOW_VARYING_in_qualifier584); 
					VARYING38_tree = (CommonTree)adaptor.create(VARYING38);
					adaptor.addChild(root_0, VARYING38_tree);

					 retval.result = ShaderArgQualifier.VARYING; 
					}
					break;
				case 4 :
					// core/src/main/resources/shader/antlr/GlslArg.g:90:5: INVARIANT VARYING
					{
					root_0 = (CommonTree)adaptor.nil();


					INVARIANT39=(Token)match(input,INVARIANT,FOLLOW_INVARIANT_in_qualifier605); 
					INVARIANT39_tree = (CommonTree)adaptor.create(INVARIANT39);
					adaptor.addChild(root_0, INVARIANT39_tree);

					VARYING40=(Token)match(input,VARYING,FOLLOW_VARYING_in_qualifier607); 
					VARYING40_tree = (CommonTree)adaptor.create(VARYING40);
					adaptor.addChild(root_0, VARYING40_tree);

					 retval.result = ShaderArgQualifier.INVARIANT_VARYING; 
					}
					break;
				case 5 :
					// core/src/main/resources/shader/antlr/GlslArg.g:91:5: UNIFORM
					{
					root_0 = (CommonTree)adaptor.nil();


					UNIFORM41=(Token)match(input,UNIFORM,FOLLOW_UNIFORM_in_qualifier618); 
					UNIFORM41_tree = (CommonTree)adaptor.create(UNIFORM41);
					adaptor.addChild(root_0, UNIFORM41_tree);

					 retval.result = ShaderArgQualifier.UNIFORM; 
					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "qualifier"


	public static class inout_return extends ParserRuleReturnScope {
		public ShaderArgInOut result;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "inout"
	// core/src/main/resources/shader/antlr/GlslArg.g:94:1: inout returns [ShaderArgInOut result] : ( IN | OUT | INOUT );
	public final GlslArgParser.inout_return inout() throws RecognitionException {
		GlslArgParser.inout_return retval = new GlslArgParser.inout_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IN42=null;
		Token OUT43=null;
		Token INOUT44=null;

		CommonTree IN42_tree=null;
		CommonTree OUT43_tree=null;
		CommonTree INOUT44_tree=null;

		try {
			// core/src/main/resources/shader/antlr/GlslArg.g:95:3: ( IN | OUT | INOUT )
			int alt8=3;
			switch ( input.LA(1) ) {
			case IN:
				{
				alt8=1;
				}
				break;
			case OUT:
				{
				alt8=2;
				}
				break;
			case INOUT:
				{
				alt8=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}
			switch (alt8) {
				case 1 :
					// core/src/main/resources/shader/antlr/GlslArg.g:95:5: IN
					{
					root_0 = (CommonTree)adaptor.nil();


					IN42=(Token)match(input,IN,FOLLOW_IN_in_inout650); 
					IN42_tree = (CommonTree)adaptor.create(IN42);
					adaptor.addChild(root_0, IN42_tree);

					 retval.result = ShaderArgInOut.IN; 
					}
					break;
				case 2 :
					// core/src/main/resources/shader/antlr/GlslArg.g:96:5: OUT
					{
					root_0 = (CommonTree)adaptor.nil();


					OUT43=(Token)match(input,OUT,FOLLOW_OUT_in_inout661); 
					OUT43_tree = (CommonTree)adaptor.create(OUT43);
					adaptor.addChild(root_0, OUT43_tree);

					 retval.result = ShaderArgInOut.OUT; 
					}
					break;
				case 3 :
					// core/src/main/resources/shader/antlr/GlslArg.g:97:5: INOUT
					{
					root_0 = (CommonTree)adaptor.nil();


					INOUT44=(Token)match(input,INOUT,FOLLOW_INOUT_in_inout671); 
					INOUT44_tree = (CommonTree)adaptor.create(INOUT44);
					adaptor.addChild(root_0, INOUT44_tree);

					 retval.result = ShaderArgInOut.INOUT; 
					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "inout"


	public static class identifier_return extends ParserRuleReturnScope {
		public String result;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "identifier"
	// core/src/main/resources/shader/antlr/GlslArg.g:100:1: identifier returns [String result] : IDENTIFIER ;
	public final GlslArgParser.identifier_return identifier() throws RecognitionException {
		GlslArgParser.identifier_return retval = new GlslArgParser.identifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER45=null;

		CommonTree IDENTIFIER45_tree=null;

		try {
			// core/src/main/resources/shader/antlr/GlslArg.g:101:3: ( IDENTIFIER )
			// core/src/main/resources/shader/antlr/GlslArg.g:101:5: IDENTIFIER
			{
			root_0 = (CommonTree)adaptor.nil();


			IDENTIFIER45=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_identifier690); 
			IDENTIFIER45_tree = (CommonTree)adaptor.create(IDENTIFIER45);
			adaptor.addChild(root_0, IDENTIFIER45_tree);

			 retval.result = (IDENTIFIER45!=null?IDENTIFIER45.getText():null); 
			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "identifier"

	// Delegated rules



	public static final BitSet FOLLOW_parameter_in_shader71 = new BitSet(new long[]{0x7F8F8270FF840BB0L});
	public static final BitSet FOLLOW_VOID_in_shader78 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_65_in_shader81 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_LPAREN_in_shader84 = new BitSet(new long[]{0x4000400000000000L});
	public static final BitSet FOLLOW_VOID_in_shader88 = new BitSet(new long[]{0x0000400000000000L});
	public static final BitSet FOLLOW_RPAREN_in_shader93 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_LCURLY_in_shader96 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifier_in_parameter125 = new BitSet(new long[]{0x7B0F8270FB8403A0L});
	public static final BitSet FOLLOW_inout_in_parameter136 = new BitSet(new long[]{0x7B0F8070FA0403A0L});
	public static final BitSet FOLLOW_type_in_parameter147 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_identifier_in_parameter157 = new BitSet(new long[]{0x0010000100000000L});
	public static final BitSet FOLLOW_LBRACKET_in_parameter166 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_RBRACKET_in_parameter168 = new BitSet(new long[]{0x0010000000000000L});
	public static final BitSet FOLLOW_SEMI_in_parameter176 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_in_type198 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_in_type204 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INT_in_type219 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOL_in_type236 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VEC2_in_type252 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VEC3_in_type268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VEC4_in_type284 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BVEC2_in_type300 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BVEC3_in_type315 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BVEC4_in_type330 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IVEC2_in_type345 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IVEC3_in_type360 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IVEC4_in_type375 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MAT2_in_type390 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MAT3_in_type406 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MAT4_in_type422 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SAMPLER1D_in_type438 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ISAMPLER1D_in_type449 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_USAMPLER1D_in_type459 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SAMPLER2D_in_type469 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ISAMPLER2D_in_type480 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_USAMPLER2D_in_type490 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SAMPLERCUBE_in_type500 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SAMPLER1DARRAY_in_type509 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SAMPLER2DARRAY_in_type520 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONST_in_qualifier542 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ATTRIBUTE_in_qualifier565 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARYING_in_qualifier584 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INVARIANT_in_qualifier605 = new BitSet(new long[]{0x0400000000000000L});
	public static final BitSet FOLLOW_VARYING_in_qualifier607 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_UNIFORM_in_qualifier618 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IN_in_inout650 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OUT_in_inout661 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INOUT_in_inout671 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_identifier690 = new BitSet(new long[]{0x0000000000000002L});
}
