/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// $ANTLR 3.3 Nov 30, 2010 12:50:56 src/main/resources/shader/antlr/GlslEs.g 2011-03-10 22:59:40

    package com.metsci.glimpse.gl.shader.grammar;


import java.util.HashMap;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

@SuppressWarnings({"unused","rawtypes"})
public class GlslEsParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "IDENTIFIER", "INTCONSTANT", "FLOATCONSTANT", "BOOLCONSTANT", "LEFT_PAREN", "RIGHT_PAREN", "LEFT_BRACKET", "RIGHT_BRACKET", "DOT", "INC_OP", "DEC_OP", "VOID", "COMMA", "FLOAT", "INT", "BOOL", "VEC2", "VEC3", "VEC4", "BVEC2", "BVEC3", "BVEC4", "IVEC2", "IVEC3", "IVEC4", "MAT2", "MAT3", "MAT4", "PLUS", "DASH", "BANG", "STAR", "SLASH", "LEFT_ANGLE", "RIGHT_ANGLE", "LE_OP", "GE_OP", "EQ_OP", "NE_OP", "AND_OP", "XOR_OP", "OR_OP", "QUESTION", "COLON", "EQUAL", "MUL_ASSIGN", "DIV_ASSIGN", "ADD_ASSIGN", "SUB_ASSIGN", "SEMICOLON", "PRECISION", "IN", "OUT", "INOUT", "INVARIANT", "CONST", "ATTRIBUTE", "VARYING", "UNIFORM", "SAMPLER2D", "SAMPLERCUBE", "HIGH_PRECISION", "MEDIUM_PRECISION", "LOW_PRECISION", "STRUCT", "LEFT_BRACE", "RIGHT_BRACE", "IF", "ELSE", "WHILE", "DO", "FOR", "CONTINUE", "BREAK", "RETURN", "DISCARD", "FALSE", "TRUE", "EXPONENT_PART", "DECIMAL_CONSTANT", "OCTAL_CONSTANT", "HEXDIGIT", "HEXADECIMAL_CONSTANT", "MOD_ASSIGN", "TILDE", "PERCENT", "VERTICAL_BAR", "CARET", "AMPERSAND", "WHITESPACE", "COMMENT", "MULTILINE_COMMENT"
    };
    public static final int EOF=-1;
    public static final int IDENTIFIER=4;
    public static final int INTCONSTANT=5;
    public static final int FLOATCONSTANT=6;
    public static final int BOOLCONSTANT=7;
    public static final int LEFT_PAREN=8;
    public static final int RIGHT_PAREN=9;
    public static final int LEFT_BRACKET=10;
    public static final int RIGHT_BRACKET=11;
    public static final int DOT=12;
    public static final int INC_OP=13;
    public static final int DEC_OP=14;
    public static final int VOID=15;
    public static final int COMMA=16;
    public static final int FLOAT=17;
    public static final int INT=18;
    public static final int BOOL=19;
    public static final int VEC2=20;
    public static final int VEC3=21;
    public static final int VEC4=22;
    public static final int BVEC2=23;
    public static final int BVEC3=24;
    public static final int BVEC4=25;
    public static final int IVEC2=26;
    public static final int IVEC3=27;
    public static final int IVEC4=28;
    public static final int MAT2=29;
    public static final int MAT3=30;
    public static final int MAT4=31;
    public static final int PLUS=32;
    public static final int DASH=33;
    public static final int BANG=34;
    public static final int STAR=35;
    public static final int SLASH=36;
    public static final int LEFT_ANGLE=37;
    public static final int RIGHT_ANGLE=38;
    public static final int LE_OP=39;
    public static final int GE_OP=40;
    public static final int EQ_OP=41;
    public static final int NE_OP=42;
    public static final int AND_OP=43;
    public static final int XOR_OP=44;
    public static final int OR_OP=45;
    public static final int QUESTION=46;
    public static final int COLON=47;
    public static final int EQUAL=48;
    public static final int MUL_ASSIGN=49;
    public static final int DIV_ASSIGN=50;
    public static final int ADD_ASSIGN=51;
    public static final int SUB_ASSIGN=52;
    public static final int SEMICOLON=53;
    public static final int PRECISION=54;
    public static final int IN=55;
    public static final int OUT=56;
    public static final int INOUT=57;
    public static final int INVARIANT=58;
    public static final int CONST=59;
    public static final int ATTRIBUTE=60;
    public static final int VARYING=61;
    public static final int UNIFORM=62;
    public static final int SAMPLER2D=63;
    public static final int SAMPLERCUBE=64;
    public static final int HIGH_PRECISION=65;
    public static final int MEDIUM_PRECISION=66;
    public static final int LOW_PRECISION=67;
    public static final int STRUCT=68;
    public static final int LEFT_BRACE=69;
    public static final int RIGHT_BRACE=70;
    public static final int IF=71;
    public static final int ELSE=72;
    public static final int WHILE=73;
    public static final int DO=74;
    public static final int FOR=75;
    public static final int CONTINUE=76;
    public static final int BREAK=77;
    public static final int RETURN=78;
    public static final int DISCARD=79;
    public static final int FALSE=80;
    public static final int TRUE=81;
    public static final int EXPONENT_PART=82;
    public static final int DECIMAL_CONSTANT=83;
    public static final int OCTAL_CONSTANT=84;
    public static final int HEXDIGIT=85;
    public static final int HEXADECIMAL_CONSTANT=86;
    public static final int MOD_ASSIGN=87;
    public static final int TILDE=88;
    public static final int PERCENT=89;
    public static final int VERTICAL_BAR=90;
    public static final int CARET=91;
    public static final int AMPERSAND=92;
    public static final int WHITESPACE=93;
    public static final int COMMENT=94;
    public static final int MULTILINE_COMMENT=95;

    // delegates
    // delegators


        public GlslEsParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public GlslEsParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);

        }

    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return GlslEsParser.tokenNames; }
    public String getGrammarFileName() { return "src/main/resources/shader/antlr/GlslEs.g"; }


        private HashMap memory = new HashMap();


    public static class translation_unit_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "translation_unit"
    // src/main/resources/shader/antlr/GlslEs.g:60:1: translation_unit : ( external_declaration )* EOF ;
    public final GlslEsParser.translation_unit_return translation_unit() throws RecognitionException {
        GlslEsParser.translation_unit_return retval = new GlslEsParser.translation_unit_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        GlslEsParser.external_declaration_return external_declaration1 = null;


        CommonTree EOF2_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:61:3: ( ( external_declaration )* EOF )
            // src/main/resources/shader/antlr/GlslEs.g:61:5: ( external_declaration )* EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:61:5: ( external_declaration )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==IDENTIFIER||LA1_0==VOID||(LA1_0>=FLOAT && LA1_0<=MAT4)||LA1_0==PRECISION||(LA1_0>=INVARIANT && LA1_0<=STRUCT)) ) {
                    alt1=1;
                }


                switch (alt1) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:61:7: external_declaration
                    {
                    pushFollow(FOLLOW_external_declaration_in_translation_unit83);
                    external_declaration1=external_declaration();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, external_declaration1.getTree());

                    }
                    break;

                default :
                    break loop1;
                }
            } while (true);

            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_translation_unit88); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF2_tree = (CommonTree)adaptor.create(EOF2);
            adaptor.addChild(root_0, EOF2_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "translation_unit"

    public static class variable_identifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable_identifier"
    // src/main/resources/shader/antlr/GlslEs.g:64:1: variable_identifier : IDENTIFIER ;
    public final GlslEsParser.variable_identifier_return variable_identifier() throws RecognitionException {
        GlslEsParser.variable_identifier_return retval = new GlslEsParser.variable_identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER3=null;

        CommonTree IDENTIFIER3_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:65:3: ( IDENTIFIER )
            // src/main/resources/shader/antlr/GlslEs.g:65:5: IDENTIFIER
            {
            root_0 = (CommonTree)adaptor.nil();

            IDENTIFIER3=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variable_identifier101); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER3_tree = (CommonTree)adaptor.create(IDENTIFIER3);
            adaptor.addChild(root_0, IDENTIFIER3_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable_identifier"

    public static class primary_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primary_expression"
    // src/main/resources/shader/antlr/GlslEs.g:68:1: primary_expression : ( INTCONSTANT | FLOATCONSTANT | BOOLCONSTANT | variable_identifier | LEFT_PAREN expression RIGHT_PAREN );
    public final GlslEsParser.primary_expression_return primary_expression() throws RecognitionException {
        GlslEsParser.primary_expression_return retval = new GlslEsParser.primary_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token INTCONSTANT4=null;
        Token FLOATCONSTANT5=null;
        Token BOOLCONSTANT6=null;
        Token LEFT_PAREN8=null;
        Token RIGHT_PAREN10=null;
        GlslEsParser.variable_identifier_return variable_identifier7 = null;

        GlslEsParser.expression_return expression9 = null;


        CommonTree INTCONSTANT4_tree=null;
        CommonTree FLOATCONSTANT5_tree=null;
        CommonTree BOOLCONSTANT6_tree=null;
        CommonTree LEFT_PAREN8_tree=null;
        CommonTree RIGHT_PAREN10_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:69:3: ( INTCONSTANT | FLOATCONSTANT | BOOLCONSTANT | variable_identifier | LEFT_PAREN expression RIGHT_PAREN )
            int alt2=5;
            switch ( input.LA(1) ) {
            case INTCONSTANT:
                {
                alt2=1;
                }
                break;
            case FLOATCONSTANT:
                {
                alt2=2;
                }
                break;
            case BOOLCONSTANT:
                {
                alt2=3;
                }
                break;
            case IDENTIFIER:
                {
                alt2=4;
                }
                break;
            case LEFT_PAREN:
                {
                alt2=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:69:5: INTCONSTANT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    INTCONSTANT4=(Token)match(input,INTCONSTANT,FOLLOW_INTCONSTANT_in_primary_expression114); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTCONSTANT4_tree = (CommonTree)adaptor.create(INTCONSTANT4);
                    adaptor.addChild(root_0, INTCONSTANT4_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:70:5: FLOATCONSTANT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    FLOATCONSTANT5=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_primary_expression120); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOATCONSTANT5_tree = (CommonTree)adaptor.create(FLOATCONSTANT5);
                    adaptor.addChild(root_0, FLOATCONSTANT5_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:71:5: BOOLCONSTANT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    BOOLCONSTANT6=(Token)match(input,BOOLCONSTANT,FOLLOW_BOOLCONSTANT_in_primary_expression126); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOLCONSTANT6_tree = (CommonTree)adaptor.create(BOOLCONSTANT6);
                    adaptor.addChild(root_0, BOOLCONSTANT6_tree);
                    }

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:72:5: variable_identifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_variable_identifier_in_primary_expression132);
                    variable_identifier7=variable_identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, variable_identifier7.getTree());

                    }
                    break;
                case 5 :
                    // src/main/resources/shader/antlr/GlslEs.g:73:5: LEFT_PAREN expression RIGHT_PAREN
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    LEFT_PAREN8=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_primary_expression138); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN8_tree = (CommonTree)adaptor.create(LEFT_PAREN8);
                    adaptor.addChild(root_0, LEFT_PAREN8_tree);
                    }
                    pushFollow(FOLLOW_expression_in_primary_expression140);
                    expression9=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression9.getTree());
                    RIGHT_PAREN10=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_primary_expression142); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN10_tree = (CommonTree)adaptor.create(RIGHT_PAREN10);
                    adaptor.addChild(root_0, RIGHT_PAREN10_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primary_expression"

    public static class postfix_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "postfix_expression"
    // src/main/resources/shader/antlr/GlslEs.g:76:1: postfix_expression : primary_expression_or_function_call ( LEFT_BRACKET integer_expression RIGHT_BRACKET | DOT field_selection | INC_OP | DEC_OP )* ;
    public final GlslEsParser.postfix_expression_return postfix_expression() throws RecognitionException {
        GlslEsParser.postfix_expression_return retval = new GlslEsParser.postfix_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LEFT_BRACKET12=null;
        Token RIGHT_BRACKET14=null;
        Token DOT15=null;
        Token INC_OP17=null;
        Token DEC_OP18=null;
        GlslEsParser.primary_expression_or_function_call_return primary_expression_or_function_call11 = null;

        GlslEsParser.integer_expression_return integer_expression13 = null;

        GlslEsParser.field_selection_return field_selection16 = null;


        CommonTree LEFT_BRACKET12_tree=null;
        CommonTree RIGHT_BRACKET14_tree=null;
        CommonTree DOT15_tree=null;
        CommonTree INC_OP17_tree=null;
        CommonTree DEC_OP18_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:77:3: ( primary_expression_or_function_call ( LEFT_BRACKET integer_expression RIGHT_BRACKET | DOT field_selection | INC_OP | DEC_OP )* )
            // src/main/resources/shader/antlr/GlslEs.g:77:5: primary_expression_or_function_call ( LEFT_BRACKET integer_expression RIGHT_BRACKET | DOT field_selection | INC_OP | DEC_OP )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_primary_expression_or_function_call_in_postfix_expression155);
            primary_expression_or_function_call11=primary_expression_or_function_call();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, primary_expression_or_function_call11.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:78:5: ( LEFT_BRACKET integer_expression RIGHT_BRACKET | DOT field_selection | INC_OP | DEC_OP )*
            loop3:
            do {
                int alt3=5;
                switch ( input.LA(1) ) {
                case LEFT_BRACKET:
                    {
                    alt3=1;
                    }
                    break;
                case DOT:
                    {
                    alt3=2;
                    }
                    break;
                case INC_OP:
                    {
                    alt3=3;
                    }
                    break;
                case DEC_OP:
                    {
                    alt3=4;
                    }
                    break;

                }

                switch (alt3) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:78:7: LEFT_BRACKET integer_expression RIGHT_BRACKET
                    {
                    LEFT_BRACKET12=(Token)match(input,LEFT_BRACKET,FOLLOW_LEFT_BRACKET_in_postfix_expression163); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_BRACKET12_tree = (CommonTree)adaptor.create(LEFT_BRACKET12);
                    adaptor.addChild(root_0, LEFT_BRACKET12_tree);
                    }
                    pushFollow(FOLLOW_integer_expression_in_postfix_expression165);
                    integer_expression13=integer_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, integer_expression13.getTree());
                    RIGHT_BRACKET14=(Token)match(input,RIGHT_BRACKET,FOLLOW_RIGHT_BRACKET_in_postfix_expression167); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_BRACKET14_tree = (CommonTree)adaptor.create(RIGHT_BRACKET14);
                    adaptor.addChild(root_0, RIGHT_BRACKET14_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:79:9: DOT field_selection
                    {
                    DOT15=(Token)match(input,DOT,FOLLOW_DOT_in_postfix_expression177); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOT15_tree = (CommonTree)adaptor.create(DOT15);
                    adaptor.addChild(root_0, DOT15_tree);
                    }
                    pushFollow(FOLLOW_field_selection_in_postfix_expression179);
                    field_selection16=field_selection();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, field_selection16.getTree());

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:80:9: INC_OP
                    {
                    INC_OP17=(Token)match(input,INC_OP,FOLLOW_INC_OP_in_postfix_expression189); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INC_OP17_tree = (CommonTree)adaptor.create(INC_OP17);
                    adaptor.addChild(root_0, INC_OP17_tree);
                    }

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:81:9: DEC_OP
                    {
                    DEC_OP18=(Token)match(input,DEC_OP,FOLLOW_DEC_OP_in_postfix_expression199); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DEC_OP18_tree = (CommonTree)adaptor.create(DEC_OP18);
                    adaptor.addChild(root_0, DEC_OP18_tree);
                    }

                    }
                    break;

                default :
                    break loop3;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "postfix_expression"

    public static class primary_expression_or_function_call_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primary_expression_or_function_call"
    // src/main/resources/shader/antlr/GlslEs.g:85:1: primary_expression_or_function_call : ( ( INTCONSTANT )=> primary_expression | ( FLOATCONSTANT )=> primary_expression | ( BOOLCONSTANT )=> primary_expression | ( LEFT_PAREN )=> primary_expression | ( function_call_header )=> function_call | primary_expression );
    public final GlslEsParser.primary_expression_or_function_call_return primary_expression_or_function_call() throws RecognitionException {
        GlslEsParser.primary_expression_or_function_call_return retval = new GlslEsParser.primary_expression_or_function_call_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.primary_expression_return primary_expression19 = null;

        GlslEsParser.primary_expression_return primary_expression20 = null;

        GlslEsParser.primary_expression_return primary_expression21 = null;

        GlslEsParser.primary_expression_return primary_expression22 = null;

        GlslEsParser.function_call_return function_call23 = null;

        GlslEsParser.primary_expression_return primary_expression24 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:86:3: ( ( INTCONSTANT )=> primary_expression | ( FLOATCONSTANT )=> primary_expression | ( BOOLCONSTANT )=> primary_expression | ( LEFT_PAREN )=> primary_expression | ( function_call_header )=> function_call | primary_expression )
            int alt4=6;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:86:5: ( INTCONSTANT )=> primary_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_primary_expression_in_primary_expression_or_function_call227);
                    primary_expression19=primary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, primary_expression19.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:87:5: ( FLOATCONSTANT )=> primary_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_primary_expression_in_primary_expression_or_function_call241);
                    primary_expression20=primary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, primary_expression20.getTree());

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:88:5: ( BOOLCONSTANT )=> primary_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_primary_expression_in_primary_expression_or_function_call255);
                    primary_expression21=primary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, primary_expression21.getTree());

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:89:5: ( LEFT_PAREN )=> primary_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_primary_expression_in_primary_expression_or_function_call269);
                    primary_expression22=primary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, primary_expression22.getTree());

                    }
                    break;
                case 5 :
                    // src/main/resources/shader/antlr/GlslEs.g:90:5: ( function_call_header )=> function_call
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_function_call_in_primary_expression_or_function_call283);
                    function_call23=function_call();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function_call23.getTree());

                    }
                    break;
                case 6 :
                    // src/main/resources/shader/antlr/GlslEs.g:91:5: primary_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_primary_expression_in_primary_expression_or_function_call289);
                    primary_expression24=primary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, primary_expression24.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primary_expression_or_function_call"

    public static class integer_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "integer_expression"
    // src/main/resources/shader/antlr/GlslEs.g:94:1: integer_expression : expression ;
    public final GlslEsParser.integer_expression_return integer_expression() throws RecognitionException {
        GlslEsParser.integer_expression_return retval = new GlslEsParser.integer_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.expression_return expression25 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:95:3: ( expression )
            // src/main/resources/shader/antlr/GlslEs.g:95:5: expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_expression_in_integer_expression302);
            expression25=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, expression25.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "integer_expression"

    public static class function_call_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_call"
    // src/main/resources/shader/antlr/GlslEs.g:98:1: function_call : function_call_generic ;
    public final GlslEsParser.function_call_return function_call() throws RecognitionException {
        GlslEsParser.function_call_return retval = new GlslEsParser.function_call_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.function_call_generic_return function_call_generic26 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:99:3: ( function_call_generic )
            // src/main/resources/shader/antlr/GlslEs.g:99:5: function_call_generic
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_function_call_generic_in_function_call315);
            function_call_generic26=function_call_generic();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, function_call_generic26.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_call"

    public static class function_call_generic_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_call_generic"
    // src/main/resources/shader/antlr/GlslEs.g:102:1: function_call_generic : function_call_header ( ( VOID )? | assignment_expression ( COMMA assignment_expression )* ) RIGHT_PAREN ;
    public final GlslEsParser.function_call_generic_return function_call_generic() throws RecognitionException {
        GlslEsParser.function_call_generic_return retval = new GlslEsParser.function_call_generic_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token VOID28=null;
        Token COMMA30=null;
        Token RIGHT_PAREN32=null;
        GlslEsParser.function_call_header_return function_call_header27 = null;

        GlslEsParser.assignment_expression_return assignment_expression29 = null;

        GlslEsParser.assignment_expression_return assignment_expression31 = null;


        CommonTree VOID28_tree=null;
        CommonTree COMMA30_tree=null;
        CommonTree RIGHT_PAREN32_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:103:3: ( function_call_header ( ( VOID )? | assignment_expression ( COMMA assignment_expression )* ) RIGHT_PAREN )
            // src/main/resources/shader/antlr/GlslEs.g:103:5: function_call_header ( ( VOID )? | assignment_expression ( COMMA assignment_expression )* ) RIGHT_PAREN
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_function_call_header_in_function_call_generic328);
            function_call_header27=function_call_header();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, function_call_header27.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:104:5: ( ( VOID )? | assignment_expression ( COMMA assignment_expression )* )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==RIGHT_PAREN||LA7_0==VOID) ) {
                alt7=1;
            }
            else if ( ((LA7_0>=IDENTIFIER && LA7_0<=LEFT_PAREN)||(LA7_0>=INC_OP && LA7_0<=DEC_OP)||(LA7_0>=FLOAT && LA7_0<=BANG)) ) {
                alt7=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:105:9: ( VOID )?
                    {
                    // src/main/resources/shader/antlr/GlslEs.g:105:9: ( VOID )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==VOID) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // src/main/resources/shader/antlr/GlslEs.g:105:10: VOID
                            {
                            VOID28=(Token)match(input,VOID,FOLLOW_VOID_in_function_call_generic345); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            VOID28_tree = (CommonTree)adaptor.create(VOID28);
                            adaptor.addChild(root_0, VOID28_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:106:9: assignment_expression ( COMMA assignment_expression )*
                    {
                    pushFollow(FOLLOW_assignment_expression_in_function_call_generic357);
                    assignment_expression29=assignment_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression29.getTree());
                    // src/main/resources/shader/antlr/GlslEs.g:106:31: ( COMMA assignment_expression )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==COMMA) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                        case 1 :
                            // src/main/resources/shader/antlr/GlslEs.g:106:32: COMMA assignment_expression
                            {
                            COMMA30=(Token)match(input,COMMA,FOLLOW_COMMA_in_function_call_generic360); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA30_tree = (CommonTree)adaptor.create(COMMA30);
                            adaptor.addChild(root_0, COMMA30_tree);
                            }
                            pushFollow(FOLLOW_assignment_expression_in_function_call_generic362);
                            assignment_expression31=assignment_expression();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression31.getTree());

                            }
                            break;

                        default :
                            break loop6;
                        }
                    } while (true);


                    }
                    break;

            }

            RIGHT_PAREN32=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function_call_generic376); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RIGHT_PAREN32_tree = (CommonTree)adaptor.create(RIGHT_PAREN32);
            adaptor.addChild(root_0, RIGHT_PAREN32_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_call_generic"

    public static class function_call_header_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_call_header"
    // src/main/resources/shader/antlr/GlslEs.g:111:1: function_call_header : function_identifier LEFT_PAREN ;
    public final GlslEsParser.function_call_header_return function_call_header() throws RecognitionException {
        GlslEsParser.function_call_header_return retval = new GlslEsParser.function_call_header_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LEFT_PAREN34=null;
        GlslEsParser.function_identifier_return function_identifier33 = null;


        CommonTree LEFT_PAREN34_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:112:3: ( function_identifier LEFT_PAREN )
            // src/main/resources/shader/antlr/GlslEs.g:112:5: function_identifier LEFT_PAREN
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_function_identifier_in_function_call_header389);
            function_identifier33=function_identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, function_identifier33.getTree());
            LEFT_PAREN34=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function_call_header391); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LEFT_PAREN34_tree = (CommonTree)adaptor.create(LEFT_PAREN34);
            adaptor.addChild(root_0, LEFT_PAREN34_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_call_header"

    public static class function_identifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_identifier"
    // src/main/resources/shader/antlr/GlslEs.g:117:1: function_identifier : constructor_identifier ;
    public final GlslEsParser.function_identifier_return function_identifier() throws RecognitionException {
        GlslEsParser.function_identifier_return retval = new GlslEsParser.function_identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.constructor_identifier_return constructor_identifier35 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:118:3: ( constructor_identifier )
            // src/main/resources/shader/antlr/GlslEs.g:118:5: constructor_identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_constructor_identifier_in_function_identifier406);
            constructor_identifier35=constructor_identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, constructor_identifier35.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_identifier"

    public static class constructor_identifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constructor_identifier"
    // src/main/resources/shader/antlr/GlslEs.g:129:1: constructor_identifier : ( FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | IDENTIFIER );
    public final GlslEsParser.constructor_identifier_return constructor_identifier() throws RecognitionException {
        GlslEsParser.constructor_identifier_return retval = new GlslEsParser.constructor_identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set36=null;

        CommonTree set36_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:130:3: ( FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | IDENTIFIER )
            // src/main/resources/shader/antlr/GlslEs.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set36=(Token)input.LT(1);
            if ( input.LA(1)==IDENTIFIER||(input.LA(1)>=FLOAT && input.LA(1)<=MAT4) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set36));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constructor_identifier"

    public static class unary_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unary_expression"
    // src/main/resources/shader/antlr/GlslEs.g:149:1: unary_expression : ( INC_OP | DEC_OP | unary_operator )* postfix_expression ;
    public final GlslEsParser.unary_expression_return unary_expression() throws RecognitionException {
        GlslEsParser.unary_expression_return retval = new GlslEsParser.unary_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token INC_OP37=null;
        Token DEC_OP38=null;
        GlslEsParser.unary_operator_return unary_operator39 = null;

        GlslEsParser.postfix_expression_return postfix_expression40 = null;


        CommonTree INC_OP37_tree=null;
        CommonTree DEC_OP38_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:150:3: ( ( INC_OP | DEC_OP | unary_operator )* postfix_expression )
            // src/main/resources/shader/antlr/GlslEs.g:150:5: ( INC_OP | DEC_OP | unary_operator )* postfix_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:150:5: ( INC_OP | DEC_OP | unary_operator )*
            loop8:
            do {
                int alt8=4;
                switch ( input.LA(1) ) {
                case INC_OP:
                    {
                    alt8=1;
                    }
                    break;
                case DEC_OP:
                    {
                    alt8=2;
                    }
                    break;
                case PLUS:
                case DASH:
                case BANG:
                    {
                    alt8=3;
                    }
                    break;

                }

                switch (alt8) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:150:6: INC_OP
                    {
                    INC_OP37=(Token)match(input,INC_OP,FOLLOW_INC_OP_in_unary_expression532); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INC_OP37_tree = (CommonTree)adaptor.create(INC_OP37);
                    adaptor.addChild(root_0, INC_OP37_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:150:15: DEC_OP
                    {
                    DEC_OP38=(Token)match(input,DEC_OP,FOLLOW_DEC_OP_in_unary_expression536); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DEC_OP38_tree = (CommonTree)adaptor.create(DEC_OP38);
                    adaptor.addChild(root_0, DEC_OP38_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:150:24: unary_operator
                    {
                    pushFollow(FOLLOW_unary_operator_in_unary_expression540);
                    unary_operator39=unary_operator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unary_operator39.getTree());

                    }
                    break;

                default :
                    break loop8;
                }
            } while (true);

            pushFollow(FOLLOW_postfix_expression_in_unary_expression544);
            postfix_expression40=postfix_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, postfix_expression40.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unary_expression"

    public static class unary_operator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unary_operator"
    // src/main/resources/shader/antlr/GlslEs.g:155:1: unary_operator : ( PLUS | DASH | BANG );
    public final GlslEsParser.unary_operator_return unary_operator() throws RecognitionException {
        GlslEsParser.unary_operator_return retval = new GlslEsParser.unary_operator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set41=null;

        CommonTree set41_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:156:3: ( PLUS | DASH | BANG )
            // src/main/resources/shader/antlr/GlslEs.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set41=(Token)input.LT(1);
            if ( (input.LA(1)>=PLUS && input.LA(1)<=BANG) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set41));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unary_operator"

    public static class multiplicative_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multiplicative_expression"
    // src/main/resources/shader/antlr/GlslEs.g:164:1: multiplicative_expression : unary_expression ( ( STAR | SLASH ) unary_expression )* ;
    public final GlslEsParser.multiplicative_expression_return multiplicative_expression() throws RecognitionException {
        GlslEsParser.multiplicative_expression_return retval = new GlslEsParser.multiplicative_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set43=null;
        GlslEsParser.unary_expression_return unary_expression42 = null;

        GlslEsParser.unary_expression_return unary_expression44 = null;


        CommonTree set43_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:165:3: ( unary_expression ( ( STAR | SLASH ) unary_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:165:5: unary_expression ( ( STAR | SLASH ) unary_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_unary_expression_in_multiplicative_expression587);
            unary_expression42=unary_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, unary_expression42.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:165:22: ( ( STAR | SLASH ) unary_expression )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>=STAR && LA9_0<=SLASH)) ) {
                    alt9=1;
                }


                switch (alt9) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:165:23: ( STAR | SLASH ) unary_expression
                    {
                    set43=(Token)input.LT(1);
                    if ( (input.LA(1)>=STAR && input.LA(1)<=SLASH) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set43));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_unary_expression_in_multiplicative_expression598);
                    unary_expression44=unary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unary_expression44.getTree());

                    }
                    break;

                default :
                    break loop9;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "multiplicative_expression"

    public static class additive_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "additive_expression"
    // src/main/resources/shader/antlr/GlslEs.g:169:1: additive_expression : multiplicative_expression ( ( PLUS | DASH ) multiplicative_expression )* ;
    public final GlslEsParser.additive_expression_return additive_expression() throws RecognitionException {
        GlslEsParser.additive_expression_return retval = new GlslEsParser.additive_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set46=null;
        GlslEsParser.multiplicative_expression_return multiplicative_expression45 = null;

        GlslEsParser.multiplicative_expression_return multiplicative_expression47 = null;


        CommonTree set46_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:170:3: ( multiplicative_expression ( ( PLUS | DASH ) multiplicative_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:170:5: multiplicative_expression ( ( PLUS | DASH ) multiplicative_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_multiplicative_expression_in_additive_expression614);
            multiplicative_expression45=multiplicative_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, multiplicative_expression45.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:170:31: ( ( PLUS | DASH ) multiplicative_expression )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>=PLUS && LA10_0<=DASH)) ) {
                    alt10=1;
                }


                switch (alt10) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:170:32: ( PLUS | DASH ) multiplicative_expression
                    {
                    set46=(Token)input.LT(1);
                    if ( (input.LA(1)>=PLUS && input.LA(1)<=DASH) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set46));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_multiplicative_expression_in_additive_expression625);
                    multiplicative_expression47=multiplicative_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, multiplicative_expression47.getTree());

                    }
                    break;

                default :
                    break loop10;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "additive_expression"

    public static class shift_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift_expression"
    // src/main/resources/shader/antlr/GlslEs.g:173:1: shift_expression : additive_expression ;
    public final GlslEsParser.shift_expression_return shift_expression() throws RecognitionException {
        GlslEsParser.shift_expression_return retval = new GlslEsParser.shift_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.additive_expression_return additive_expression48 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:174:3: ( additive_expression )
            // src/main/resources/shader/antlr/GlslEs.g:174:5: additive_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_additive_expression_in_shift_expression640);
            additive_expression48=additive_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, additive_expression48.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "shift_expression"

    public static class relational_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "relational_expression"
    // src/main/resources/shader/antlr/GlslEs.g:179:1: relational_expression : shift_expression ( ( LEFT_ANGLE | RIGHT_ANGLE | LE_OP | GE_OP ) shift_expression )* ;
    public final GlslEsParser.relational_expression_return relational_expression() throws RecognitionException {
        GlslEsParser.relational_expression_return retval = new GlslEsParser.relational_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set50=null;
        GlslEsParser.shift_expression_return shift_expression49 = null;

        GlslEsParser.shift_expression_return shift_expression51 = null;


        CommonTree set50_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:180:3: ( shift_expression ( ( LEFT_ANGLE | RIGHT_ANGLE | LE_OP | GE_OP ) shift_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:180:5: shift_expression ( ( LEFT_ANGLE | RIGHT_ANGLE | LE_OP | GE_OP ) shift_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_shift_expression_in_relational_expression655);
            shift_expression49=shift_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, shift_expression49.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:180:22: ( ( LEFT_ANGLE | RIGHT_ANGLE | LE_OP | GE_OP ) shift_expression )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>=LEFT_ANGLE && LA11_0<=GE_OP)) ) {
                    alt11=1;
                }


                switch (alt11) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:180:23: ( LEFT_ANGLE | RIGHT_ANGLE | LE_OP | GE_OP ) shift_expression
                    {
                    set50=(Token)input.LT(1);
                    if ( (input.LA(1)>=LEFT_ANGLE && input.LA(1)<=GE_OP) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set50));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_shift_expression_in_relational_expression674);
                    shift_expression51=shift_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, shift_expression51.getTree());

                    }
                    break;

                default :
                    break loop11;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relational_expression"

    public static class equality_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "equality_expression"
    // src/main/resources/shader/antlr/GlslEs.g:183:1: equality_expression : relational_expression ( ( EQ_OP | NE_OP ) relational_expression )* ;
    public final GlslEsParser.equality_expression_return equality_expression() throws RecognitionException {
        GlslEsParser.equality_expression_return retval = new GlslEsParser.equality_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set53=null;
        GlslEsParser.relational_expression_return relational_expression52 = null;

        GlslEsParser.relational_expression_return relational_expression54 = null;


        CommonTree set53_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:184:3: ( relational_expression ( ( EQ_OP | NE_OP ) relational_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:184:5: relational_expression ( ( EQ_OP | NE_OP ) relational_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_relational_expression_in_equality_expression689);
            relational_expression52=relational_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, relational_expression52.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:184:27: ( ( EQ_OP | NE_OP ) relational_expression )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( ((LA12_0>=EQ_OP && LA12_0<=NE_OP)) ) {
                    alt12=1;
                }


                switch (alt12) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:184:28: ( EQ_OP | NE_OP ) relational_expression
                    {
                    set53=(Token)input.LT(1);
                    if ( (input.LA(1)>=EQ_OP && input.LA(1)<=NE_OP) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set53));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_relational_expression_in_equality_expression700);
                    relational_expression54=relational_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, relational_expression54.getTree());

                    }
                    break;

                default :
                    break loop12;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "equality_expression"

    public static class and_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and_expression"
    // src/main/resources/shader/antlr/GlslEs.g:187:1: and_expression : equality_expression ;
    public final GlslEsParser.and_expression_return and_expression() throws RecognitionException {
        GlslEsParser.and_expression_return retval = new GlslEsParser.and_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.equality_expression_return equality_expression55 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:188:3: ( equality_expression )
            // src/main/resources/shader/antlr/GlslEs.g:188:5: equality_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_equality_expression_in_and_expression715);
            equality_expression55=equality_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, equality_expression55.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and_expression"

    public static class exclusive_or_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exclusive_or_expression"
    // src/main/resources/shader/antlr/GlslEs.g:192:1: exclusive_or_expression : and_expression ;
    public final GlslEsParser.exclusive_or_expression_return exclusive_or_expression() throws RecognitionException {
        GlslEsParser.exclusive_or_expression_return retval = new GlslEsParser.exclusive_or_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.and_expression_return and_expression56 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:193:3: ( and_expression )
            // src/main/resources/shader/antlr/GlslEs.g:193:5: and_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_and_expression_in_exclusive_or_expression729);
            and_expression56=and_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, and_expression56.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exclusive_or_expression"

    public static class inclusive_or_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inclusive_or_expression"
    // src/main/resources/shader/antlr/GlslEs.g:197:1: inclusive_or_expression : exclusive_or_expression ;
    public final GlslEsParser.inclusive_or_expression_return inclusive_or_expression() throws RecognitionException {
        GlslEsParser.inclusive_or_expression_return retval = new GlslEsParser.inclusive_or_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.exclusive_or_expression_return exclusive_or_expression57 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:198:3: ( exclusive_or_expression )
            // src/main/resources/shader/antlr/GlslEs.g:198:5: exclusive_or_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression743);
            exclusive_or_expression57=exclusive_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, exclusive_or_expression57.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "inclusive_or_expression"

    public static class logical_and_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logical_and_expression"
    // src/main/resources/shader/antlr/GlslEs.g:202:1: logical_and_expression : inclusive_or_expression ( AND_OP inclusive_or_expression )* ;
    public final GlslEsParser.logical_and_expression_return logical_and_expression() throws RecognitionException {
        GlslEsParser.logical_and_expression_return retval = new GlslEsParser.logical_and_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND_OP59=null;
        GlslEsParser.inclusive_or_expression_return inclusive_or_expression58 = null;

        GlslEsParser.inclusive_or_expression_return inclusive_or_expression60 = null;


        CommonTree AND_OP59_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:203:3: ( inclusive_or_expression ( AND_OP inclusive_or_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:203:5: inclusive_or_expression ( AND_OP inclusive_or_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression757);
            inclusive_or_expression58=inclusive_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, inclusive_or_expression58.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:203:29: ( AND_OP inclusive_or_expression )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==AND_OP) ) {
                    alt13=1;
                }


                switch (alt13) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:203:30: AND_OP inclusive_or_expression
                    {
                    AND_OP59=(Token)match(input,AND_OP,FOLLOW_AND_OP_in_logical_and_expression760); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AND_OP59_tree = (CommonTree)adaptor.create(AND_OP59);
                    adaptor.addChild(root_0, AND_OP59_tree);
                    }
                    pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression762);
                    inclusive_or_expression60=inclusive_or_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, inclusive_or_expression60.getTree());

                    }
                    break;

                default :
                    break loop13;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logical_and_expression"

    public static class logical_xor_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logical_xor_expression"
    // src/main/resources/shader/antlr/GlslEs.g:206:1: logical_xor_expression : logical_and_expression ( XOR_OP logical_and_expression )* ;
    public final GlslEsParser.logical_xor_expression_return logical_xor_expression() throws RecognitionException {
        GlslEsParser.logical_xor_expression_return retval = new GlslEsParser.logical_xor_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token XOR_OP62=null;
        GlslEsParser.logical_and_expression_return logical_and_expression61 = null;

        GlslEsParser.logical_and_expression_return logical_and_expression63 = null;


        CommonTree XOR_OP62_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:207:3: ( logical_and_expression ( XOR_OP logical_and_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:207:5: logical_and_expression ( XOR_OP logical_and_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_logical_and_expression_in_logical_xor_expression777);
            logical_and_expression61=logical_and_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_and_expression61.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:207:28: ( XOR_OP logical_and_expression )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==XOR_OP) ) {
                    alt14=1;
                }


                switch (alt14) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:207:29: XOR_OP logical_and_expression
                    {
                    XOR_OP62=(Token)match(input,XOR_OP,FOLLOW_XOR_OP_in_logical_xor_expression780); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    XOR_OP62_tree = (CommonTree)adaptor.create(XOR_OP62);
                    adaptor.addChild(root_0, XOR_OP62_tree);
                    }
                    pushFollow(FOLLOW_logical_and_expression_in_logical_xor_expression782);
                    logical_and_expression63=logical_and_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_and_expression63.getTree());

                    }
                    break;

                default :
                    break loop14;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logical_xor_expression"

    public static class logical_or_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logical_or_expression"
    // src/main/resources/shader/antlr/GlslEs.g:210:1: logical_or_expression : logical_xor_expression ( OR_OP logical_xor_expression )* ;
    public final GlslEsParser.logical_or_expression_return logical_or_expression() throws RecognitionException {
        GlslEsParser.logical_or_expression_return retval = new GlslEsParser.logical_or_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR_OP65=null;
        GlslEsParser.logical_xor_expression_return logical_xor_expression64 = null;

        GlslEsParser.logical_xor_expression_return logical_xor_expression66 = null;


        CommonTree OR_OP65_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:211:3: ( logical_xor_expression ( OR_OP logical_xor_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:211:5: logical_xor_expression ( OR_OP logical_xor_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_logical_xor_expression_in_logical_or_expression797);
            logical_xor_expression64=logical_xor_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_xor_expression64.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:211:28: ( OR_OP logical_xor_expression )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==OR_OP) ) {
                    alt15=1;
                }


                switch (alt15) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:211:29: OR_OP logical_xor_expression
                    {
                    OR_OP65=(Token)match(input,OR_OP,FOLLOW_OR_OP_in_logical_or_expression800); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OR_OP65_tree = (CommonTree)adaptor.create(OR_OP65);
                    adaptor.addChild(root_0, OR_OP65_tree);
                    }
                    pushFollow(FOLLOW_logical_xor_expression_in_logical_or_expression802);
                    logical_xor_expression66=logical_xor_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_xor_expression66.getTree());

                    }
                    break;

                default :
                    break loop15;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logical_or_expression"

    public static class conditional_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "conditional_expression"
    // src/main/resources/shader/antlr/GlslEs.g:214:1: conditional_expression : logical_or_expression ( QUESTION expression COLON assignment_expression )? ;
    public final GlslEsParser.conditional_expression_return conditional_expression() throws RecognitionException {
        GlslEsParser.conditional_expression_return retval = new GlslEsParser.conditional_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION68=null;
        Token COLON70=null;
        GlslEsParser.logical_or_expression_return logical_or_expression67 = null;

        GlslEsParser.expression_return expression69 = null;

        GlslEsParser.assignment_expression_return assignment_expression71 = null;


        CommonTree QUESTION68_tree=null;
        CommonTree COLON70_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:215:3: ( logical_or_expression ( QUESTION expression COLON assignment_expression )? )
            // src/main/resources/shader/antlr/GlslEs.g:215:5: logical_or_expression ( QUESTION expression COLON assignment_expression )?
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_logical_or_expression_in_conditional_expression817);
            logical_or_expression67=logical_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_or_expression67.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:215:27: ( QUESTION expression COLON assignment_expression )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==QUESTION) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:215:28: QUESTION expression COLON assignment_expression
                    {
                    QUESTION68=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_conditional_expression820); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    QUESTION68_tree = (CommonTree)adaptor.create(QUESTION68);
                    adaptor.addChild(root_0, QUESTION68_tree);
                    }
                    pushFollow(FOLLOW_expression_in_conditional_expression822);
                    expression69=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression69.getTree());
                    COLON70=(Token)match(input,COLON,FOLLOW_COLON_in_conditional_expression824); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON70_tree = (CommonTree)adaptor.create(COLON70);
                    adaptor.addChild(root_0, COLON70_tree);
                    }
                    pushFollow(FOLLOW_assignment_expression_in_conditional_expression826);
                    assignment_expression71=assignment_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression71.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "conditional_expression"

    public static class assignment_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignment_expression"
    // src/main/resources/shader/antlr/GlslEs.g:218:1: assignment_expression : ( ( unary_expression assignment_operator )=> unary_expression assignment_operator assignment_expression | conditional_expression );
    public final GlslEsParser.assignment_expression_return assignment_expression() throws RecognitionException {
        GlslEsParser.assignment_expression_return retval = new GlslEsParser.assignment_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.unary_expression_return unary_expression72 = null;

        GlslEsParser.assignment_operator_return assignment_operator73 = null;

        GlslEsParser.assignment_expression_return assignment_expression74 = null;

        GlslEsParser.conditional_expression_return conditional_expression75 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:219:3: ( ( unary_expression assignment_operator )=> unary_expression assignment_operator assignment_expression | conditional_expression )
            int alt17=2;
            alt17 = dfa17.predict(input);
            switch (alt17) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:219:5: ( unary_expression assignment_operator )=> unary_expression assignment_operator assignment_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_unary_expression_in_assignment_expression849);
                    unary_expression72=unary_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unary_expression72.getTree());
                    pushFollow(FOLLOW_assignment_operator_in_assignment_expression851);
                    assignment_operator73=assignment_operator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_operator73.getTree());
                    pushFollow(FOLLOW_assignment_expression_in_assignment_expression853);
                    assignment_expression74=assignment_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression74.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:220:5: conditional_expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_conditional_expression_in_assignment_expression859);
                    conditional_expression75=conditional_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_expression75.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignment_expression"

    public static class assignment_operator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignment_operator"
    // src/main/resources/shader/antlr/GlslEs.g:223:1: assignment_operator : ( EQUAL | MUL_ASSIGN | DIV_ASSIGN | ADD_ASSIGN | SUB_ASSIGN );
    public final GlslEsParser.assignment_operator_return assignment_operator() throws RecognitionException {
        GlslEsParser.assignment_operator_return retval = new GlslEsParser.assignment_operator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set76=null;

        CommonTree set76_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:224:3: ( EQUAL | MUL_ASSIGN | DIV_ASSIGN | ADD_ASSIGN | SUB_ASSIGN )
            // src/main/resources/shader/antlr/GlslEs.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set76=(Token)input.LT(1);
            if ( (input.LA(1)>=EQUAL && input.LA(1)<=SUB_ASSIGN) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set76));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignment_operator"

    public static class expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression"
    // src/main/resources/shader/antlr/GlslEs.g:237:1: expression : assignment_expression ( COMMA assignment_expression )* ;
    public final GlslEsParser.expression_return expression() throws RecognitionException {
        GlslEsParser.expression_return retval = new GlslEsParser.expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA78=null;
        GlslEsParser.assignment_expression_return assignment_expression77 = null;

        GlslEsParser.assignment_expression_return assignment_expression79 = null;


        CommonTree COMMA78_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:238:3: ( assignment_expression ( COMMA assignment_expression )* )
            // src/main/resources/shader/antlr/GlslEs.g:238:5: assignment_expression ( COMMA assignment_expression )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_assignment_expression_in_expression915);
            assignment_expression77=assignment_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression77.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:238:27: ( COMMA assignment_expression )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==COMMA) ) {
                    alt18=1;
                }


                switch (alt18) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:238:28: COMMA assignment_expression
                    {
                    COMMA78=(Token)match(input,COMMA,FOLLOW_COMMA_in_expression918); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA78_tree = (CommonTree)adaptor.create(COMMA78);
                    adaptor.addChild(root_0, COMMA78_tree);
                    }
                    pushFollow(FOLLOW_assignment_expression_in_expression920);
                    assignment_expression79=assignment_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression79.getTree());

                    }
                    break;

                default :
                    break loop18;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression"

    public static class constant_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constant_expression"
    // src/main/resources/shader/antlr/GlslEs.g:241:1: constant_expression : conditional_expression ;
    public final GlslEsParser.constant_expression_return constant_expression() throws RecognitionException {
        GlslEsParser.constant_expression_return retval = new GlslEsParser.constant_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.conditional_expression_return conditional_expression80 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:242:3: ( conditional_expression )
            // src/main/resources/shader/antlr/GlslEs.g:242:5: conditional_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_conditional_expression_in_constant_expression935);
            conditional_expression80=conditional_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_expression80.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constant_expression"

    public static class declaration_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "declaration"
    // src/main/resources/shader/antlr/GlslEs.g:245:1: declaration : ( ( function_header )=> function_prototype SEMICOLON | init_declarator_list SEMICOLON | PRECISION precision_qualifier type_specifier_no_prec SEMICOLON );
    public final GlslEsParser.declaration_return declaration() throws RecognitionException {
        GlslEsParser.declaration_return retval = new GlslEsParser.declaration_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMICOLON82=null;
        Token SEMICOLON84=null;
        Token PRECISION85=null;
        Token SEMICOLON88=null;
        GlslEsParser.function_prototype_return function_prototype81 = null;

        GlslEsParser.init_declarator_list_return init_declarator_list83 = null;

        GlslEsParser.precision_qualifier_return precision_qualifier86 = null;

        GlslEsParser.type_specifier_no_prec_return type_specifier_no_prec87 = null;


        CommonTree SEMICOLON82_tree=null;
        CommonTree SEMICOLON84_tree=null;
        CommonTree PRECISION85_tree=null;
        CommonTree SEMICOLON88_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:246:3: ( ( function_header )=> function_prototype SEMICOLON | init_declarator_list SEMICOLON | PRECISION precision_qualifier type_specifier_no_prec SEMICOLON )
            int alt19=3;
            alt19 = dfa19.predict(input);
            switch (alt19) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:246:5: ( function_header )=> function_prototype SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_function_prototype_in_declaration954);
                    function_prototype81=function_prototype();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function_prototype81.getTree());
                    SEMICOLON82=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_declaration956); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON82_tree = (CommonTree)adaptor.create(SEMICOLON82);
                    adaptor.addChild(root_0, SEMICOLON82_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:247:5: init_declarator_list SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_init_declarator_list_in_declaration962);
                    init_declarator_list83=init_declarator_list();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, init_declarator_list83.getTree());
                    SEMICOLON84=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_declaration964); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON84_tree = (CommonTree)adaptor.create(SEMICOLON84);
                    adaptor.addChild(root_0, SEMICOLON84_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:248:5: PRECISION precision_qualifier type_specifier_no_prec SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    PRECISION85=(Token)match(input,PRECISION,FOLLOW_PRECISION_in_declaration970); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PRECISION85_tree = (CommonTree)adaptor.create(PRECISION85);
                    adaptor.addChild(root_0, PRECISION85_tree);
                    }
                    pushFollow(FOLLOW_precision_qualifier_in_declaration972);
                    precision_qualifier86=precision_qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, precision_qualifier86.getTree());
                    pushFollow(FOLLOW_type_specifier_no_prec_in_declaration974);
                    type_specifier_no_prec87=type_specifier_no_prec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier_no_prec87.getTree());
                    SEMICOLON88=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_declaration976); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON88_tree = (CommonTree)adaptor.create(SEMICOLON88);
                    adaptor.addChild(root_0, SEMICOLON88_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "declaration"

    public static class function_prototype_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_prototype"
    // src/main/resources/shader/antlr/GlslEs.g:251:1: function_prototype : function_declarator RIGHT_PAREN ;
    public final GlslEsParser.function_prototype_return function_prototype() throws RecognitionException {
        GlslEsParser.function_prototype_return retval = new GlslEsParser.function_prototype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RIGHT_PAREN90=null;
        GlslEsParser.function_declarator_return function_declarator89 = null;


        CommonTree RIGHT_PAREN90_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:252:3: ( function_declarator RIGHT_PAREN )
            // src/main/resources/shader/antlr/GlslEs.g:252:5: function_declarator RIGHT_PAREN
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_function_declarator_in_function_prototype989);
            function_declarator89=function_declarator();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, function_declarator89.getTree());
            RIGHT_PAREN90=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function_prototype991); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RIGHT_PAREN90_tree = (CommonTree)adaptor.create(RIGHT_PAREN90);
            adaptor.addChild(root_0, RIGHT_PAREN90_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_prototype"

    public static class function_declarator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_declarator"
    // src/main/resources/shader/antlr/GlslEs.g:255:1: function_declarator : function_header ( parameter_declaration ( COMMA parameter_declaration )* )? ;
    public final GlslEsParser.function_declarator_return function_declarator() throws RecognitionException {
        GlslEsParser.function_declarator_return retval = new GlslEsParser.function_declarator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA93=null;
        GlslEsParser.function_header_return function_header91 = null;

        GlslEsParser.parameter_declaration_return parameter_declaration92 = null;

        GlslEsParser.parameter_declaration_return parameter_declaration94 = null;


        CommonTree COMMA93_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:256:3: ( function_header ( parameter_declaration ( COMMA parameter_declaration )* )? )
            // src/main/resources/shader/antlr/GlslEs.g:256:5: function_header ( parameter_declaration ( COMMA parameter_declaration )* )?
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_function_header_in_function_declarator1004);
            function_header91=function_header();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, function_header91.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:256:21: ( parameter_declaration ( COMMA parameter_declaration )* )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==IDENTIFIER||LA21_0==VOID||(LA21_0>=FLOAT && LA21_0<=MAT4)||(LA21_0>=IN && LA21_0<=STRUCT)) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:256:22: parameter_declaration ( COMMA parameter_declaration )*
                    {
                    pushFollow(FOLLOW_parameter_declaration_in_function_declarator1007);
                    parameter_declaration92=parameter_declaration();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter_declaration92.getTree());
                    // src/main/resources/shader/antlr/GlslEs.g:256:44: ( COMMA parameter_declaration )*
                    loop20:
                    do {
                        int alt20=2;
                        int LA20_0 = input.LA(1);

                        if ( (LA20_0==COMMA) ) {
                            alt20=1;
                        }


                        switch (alt20) {
                        case 1 :
                            // src/main/resources/shader/antlr/GlslEs.g:256:45: COMMA parameter_declaration
                            {
                            COMMA93=(Token)match(input,COMMA,FOLLOW_COMMA_in_function_declarator1010); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA93_tree = (CommonTree)adaptor.create(COMMA93);
                            adaptor.addChild(root_0, COMMA93_tree);
                            }
                            pushFollow(FOLLOW_parameter_declaration_in_function_declarator1012);
                            parameter_declaration94=parameter_declaration();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter_declaration94.getTree());

                            }
                            break;

                        default :
                            break loop20;
                        }
                    } while (true);


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_declarator"

    public static class function_header_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_header"
    // src/main/resources/shader/antlr/GlslEs.g:259:1: function_header : fully_specified_type IDENTIFIER LEFT_PAREN ;
    public final GlslEsParser.function_header_return function_header() throws RecognitionException {
        GlslEsParser.function_header_return retval = new GlslEsParser.function_header_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER96=null;
        Token LEFT_PAREN97=null;
        GlslEsParser.fully_specified_type_return fully_specified_type95 = null;


        CommonTree IDENTIFIER96_tree=null;
        CommonTree LEFT_PAREN97_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:260:3: ( fully_specified_type IDENTIFIER LEFT_PAREN )
            // src/main/resources/shader/antlr/GlslEs.g:260:5: fully_specified_type IDENTIFIER LEFT_PAREN
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_fully_specified_type_in_function_header1030);
            fully_specified_type95=fully_specified_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fully_specified_type95.getTree());
            IDENTIFIER96=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_function_header1032); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER96_tree = (CommonTree)adaptor.create(IDENTIFIER96);
            adaptor.addChild(root_0, IDENTIFIER96_tree);
            }
            LEFT_PAREN97=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function_header1034); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LEFT_PAREN97_tree = (CommonTree)adaptor.create(LEFT_PAREN97);
            adaptor.addChild(root_0, LEFT_PAREN97_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_header"

    public static class parameter_declaration_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameter_declaration"
    // src/main/resources/shader/antlr/GlslEs.g:263:1: parameter_declaration : ( type_qualifier )? ( parameter_qualifier )? ( type_specifier ( IDENTIFIER )? ( LEFT_BRACKET constant_expression RIGHT_BRACKET )? ) ;
    public final GlslEsParser.parameter_declaration_return parameter_declaration() throws RecognitionException {
        GlslEsParser.parameter_declaration_return retval = new GlslEsParser.parameter_declaration_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER101=null;
        Token LEFT_BRACKET102=null;
        Token RIGHT_BRACKET104=null;
        GlslEsParser.type_qualifier_return type_qualifier98 = null;

        GlslEsParser.parameter_qualifier_return parameter_qualifier99 = null;

        GlslEsParser.type_specifier_return type_specifier100 = null;

        GlslEsParser.constant_expression_return constant_expression103 = null;


        CommonTree IDENTIFIER101_tree=null;
        CommonTree LEFT_BRACKET102_tree=null;
        CommonTree RIGHT_BRACKET104_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:264:3: ( ( type_qualifier )? ( parameter_qualifier )? ( type_specifier ( IDENTIFIER )? ( LEFT_BRACKET constant_expression RIGHT_BRACKET )? ) )
            // src/main/resources/shader/antlr/GlslEs.g:264:5: ( type_qualifier )? ( parameter_qualifier )? ( type_specifier ( IDENTIFIER )? ( LEFT_BRACKET constant_expression RIGHT_BRACKET )? )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:264:5: ( type_qualifier )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0>=INVARIANT && LA22_0<=UNIFORM)) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:264:6: type_qualifier
                    {
                    pushFollow(FOLLOW_type_qualifier_in_parameter_declaration1048);
                    type_qualifier98=type_qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_qualifier98.getTree());

                    }
                    break;

            }

            // src/main/resources/shader/antlr/GlslEs.g:264:23: ( parameter_qualifier )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( ((LA23_0>=IN && LA23_0<=INOUT)) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:264:24: parameter_qualifier
                    {
                    pushFollow(FOLLOW_parameter_qualifier_in_parameter_declaration1053);
                    parameter_qualifier99=parameter_qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter_qualifier99.getTree());

                    }
                    break;

            }

            // src/main/resources/shader/antlr/GlslEs.g:265:5: ( type_specifier ( IDENTIFIER )? ( LEFT_BRACKET constant_expression RIGHT_BRACKET )? )
            // src/main/resources/shader/antlr/GlslEs.g:265:7: type_specifier ( IDENTIFIER )? ( LEFT_BRACKET constant_expression RIGHT_BRACKET )?
            {
            pushFollow(FOLLOW_type_specifier_in_parameter_declaration1063);
            type_specifier100=type_specifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier100.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:267:7: ( IDENTIFIER )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==IDENTIFIER) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:267:8: IDENTIFIER
                    {
                    IDENTIFIER101=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_parameter_declaration1079); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER101_tree = (CommonTree)adaptor.create(IDENTIFIER101);
                    adaptor.addChild(root_0, IDENTIFIER101_tree);
                    }

                    }
                    break;

            }

            // src/main/resources/shader/antlr/GlslEs.g:269:7: ( LEFT_BRACKET constant_expression RIGHT_BRACKET )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==LEFT_BRACKET) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:269:8: LEFT_BRACKET constant_expression RIGHT_BRACKET
                    {
                    LEFT_BRACKET102=(Token)match(input,LEFT_BRACKET,FOLLOW_LEFT_BRACKET_in_parameter_declaration1097); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_BRACKET102_tree = (CommonTree)adaptor.create(LEFT_BRACKET102);
                    adaptor.addChild(root_0, LEFT_BRACKET102_tree);
                    }
                    pushFollow(FOLLOW_constant_expression_in_parameter_declaration1099);
                    constant_expression103=constant_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant_expression103.getTree());
                    RIGHT_BRACKET104=(Token)match(input,RIGHT_BRACKET,FOLLOW_RIGHT_BRACKET_in_parameter_declaration1101); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_BRACKET104_tree = (CommonTree)adaptor.create(RIGHT_BRACKET104);
                    adaptor.addChild(root_0, RIGHT_BRACKET104_tree);
                    }

                    }
                    break;

            }


            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameter_declaration"

    public static class parameter_qualifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameter_qualifier"
    // src/main/resources/shader/antlr/GlslEs.g:275:1: parameter_qualifier : ( IN | OUT | INOUT );
    public final GlslEsParser.parameter_qualifier_return parameter_qualifier() throws RecognitionException {
        GlslEsParser.parameter_qualifier_return retval = new GlslEsParser.parameter_qualifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set105=null;

        CommonTree set105_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:276:3: ( IN | OUT | INOUT )
            // src/main/resources/shader/antlr/GlslEs.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set105=(Token)input.LT(1);
            if ( (input.LA(1)>=IN && input.LA(1)<=INOUT) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set105));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameter_qualifier"

    public static class init_declarator_list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "init_declarator_list"
    // src/main/resources/shader/antlr/GlslEs.g:281:1: init_declarator_list : single_declaration ( init_declarator_list_1 )* ;
    public final GlslEsParser.init_declarator_list_return init_declarator_list() throws RecognitionException {
        GlslEsParser.init_declarator_list_return retval = new GlslEsParser.init_declarator_list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.single_declaration_return single_declaration106 = null;

        GlslEsParser.init_declarator_list_1_return init_declarator_list_1107 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:282:3: ( single_declaration ( init_declarator_list_1 )* )
            // src/main/resources/shader/antlr/GlslEs.g:282:5: single_declaration ( init_declarator_list_1 )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_single_declaration_in_init_declarator_list1149);
            single_declaration106=single_declaration();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, single_declaration106.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:282:24: ( init_declarator_list_1 )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==COMMA) ) {
                    alt26=1;
                }


                switch (alt26) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:282:25: init_declarator_list_1
                    {
                    pushFollow(FOLLOW_init_declarator_list_1_in_init_declarator_list1152);
                    init_declarator_list_1107=init_declarator_list_1();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, init_declarator_list_1107.getTree());

                    }
                    break;

                default :
                    break loop26;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "init_declarator_list"

    public static class init_declarator_list_1_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "init_declarator_list_1"
    // src/main/resources/shader/antlr/GlslEs.g:285:1: init_declarator_list_1 : COMMA IDENTIFIER ( init_declarator_list_2 )? ;
    public final GlslEsParser.init_declarator_list_1_return init_declarator_list_1() throws RecognitionException {
        GlslEsParser.init_declarator_list_1_return retval = new GlslEsParser.init_declarator_list_1_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA108=null;
        Token IDENTIFIER109=null;
        GlslEsParser.init_declarator_list_2_return init_declarator_list_2110 = null;


        CommonTree COMMA108_tree=null;
        CommonTree IDENTIFIER109_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:286:3: ( COMMA IDENTIFIER ( init_declarator_list_2 )? )
            // src/main/resources/shader/antlr/GlslEs.g:286:5: COMMA IDENTIFIER ( init_declarator_list_2 )?
            {
            root_0 = (CommonTree)adaptor.nil();

            COMMA108=(Token)match(input,COMMA,FOLLOW_COMMA_in_init_declarator_list_11167); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA108_tree = (CommonTree)adaptor.create(COMMA108);
            adaptor.addChild(root_0, COMMA108_tree);
            }
            IDENTIFIER109=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_init_declarator_list_11169); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER109_tree = (CommonTree)adaptor.create(IDENTIFIER109);
            adaptor.addChild(root_0, IDENTIFIER109_tree);
            }
            // src/main/resources/shader/antlr/GlslEs.g:286:22: ( init_declarator_list_2 )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==LEFT_BRACKET||LA27_0==EQUAL) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:286:23: init_declarator_list_2
                    {
                    pushFollow(FOLLOW_init_declarator_list_2_in_init_declarator_list_11172);
                    init_declarator_list_2110=init_declarator_list_2();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, init_declarator_list_2110.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "init_declarator_list_1"

    public static class init_declarator_list_2_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "init_declarator_list_2"
    // src/main/resources/shader/antlr/GlslEs.g:289:1: init_declarator_list_2 : ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer );
    public final GlslEsParser.init_declarator_list_2_return init_declarator_list_2() throws RecognitionException {
        GlslEsParser.init_declarator_list_2_return retval = new GlslEsParser.init_declarator_list_2_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LEFT_BRACKET111=null;
        Token RIGHT_BRACKET113=null;
        Token EQUAL114=null;
        GlslEsParser.constant_expression_return constant_expression112 = null;

        GlslEsParser.initializer_return initializer115 = null;


        CommonTree LEFT_BRACKET111_tree=null;
        CommonTree RIGHT_BRACKET113_tree=null;
        CommonTree EQUAL114_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:290:3: ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==LEFT_BRACKET) ) {
                alt28=1;
            }
            else if ( (LA28_0==EQUAL) ) {
                alt28=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:290:5: LEFT_BRACKET constant_expression RIGHT_BRACKET
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    LEFT_BRACKET111=(Token)match(input,LEFT_BRACKET,FOLLOW_LEFT_BRACKET_in_init_declarator_list_21187); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_BRACKET111_tree = (CommonTree)adaptor.create(LEFT_BRACKET111);
                    adaptor.addChild(root_0, LEFT_BRACKET111_tree);
                    }
                    pushFollow(FOLLOW_constant_expression_in_init_declarator_list_21189);
                    constant_expression112=constant_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant_expression112.getTree());
                    RIGHT_BRACKET113=(Token)match(input,RIGHT_BRACKET,FOLLOW_RIGHT_BRACKET_in_init_declarator_list_21191); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_BRACKET113_tree = (CommonTree)adaptor.create(RIGHT_BRACKET113);
                    adaptor.addChild(root_0, RIGHT_BRACKET113_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:291:5: EQUAL initializer
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    EQUAL114=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_init_declarator_list_21197); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUAL114_tree = (CommonTree)adaptor.create(EQUAL114);
                    adaptor.addChild(root_0, EQUAL114_tree);
                    }
                    pushFollow(FOLLOW_initializer_in_init_declarator_list_21199);
                    initializer115=initializer();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initializer115.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "init_declarator_list_2"

    public static class single_declaration_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "single_declaration"
    // src/main/resources/shader/antlr/GlslEs.g:294:1: single_declaration : ( fully_specified_type ( IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )? )? | INVARIANT IDENTIFIER );
    public final GlslEsParser.single_declaration_return single_declaration() throws RecognitionException {
        GlslEsParser.single_declaration_return retval = new GlslEsParser.single_declaration_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER117=null;
        Token LEFT_BRACKET118=null;
        Token RIGHT_BRACKET120=null;
        Token EQUAL121=null;
        Token INVARIANT123=null;
        Token IDENTIFIER124=null;
        GlslEsParser.fully_specified_type_return fully_specified_type116 = null;

        GlslEsParser.constant_expression_return constant_expression119 = null;

        GlslEsParser.initializer_return initializer122 = null;


        CommonTree IDENTIFIER117_tree=null;
        CommonTree LEFT_BRACKET118_tree=null;
        CommonTree RIGHT_BRACKET120_tree=null;
        CommonTree EQUAL121_tree=null;
        CommonTree INVARIANT123_tree=null;
        CommonTree IDENTIFIER124_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:295:3: ( fully_specified_type ( IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )? )? | INVARIANT IDENTIFIER )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==IDENTIFIER||LA31_0==VOID||(LA31_0>=FLOAT && LA31_0<=MAT4)||(LA31_0>=CONST && LA31_0<=STRUCT)) ) {
                alt31=1;
            }
            else if ( (LA31_0==INVARIANT) ) {
                int LA31_2 = input.LA(2);

                if ( (LA31_2==VARYING) ) {
                    alt31=1;
                }
                else if ( (LA31_2==IDENTIFIER) ) {
                    alt31=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 31, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:295:5: fully_specified_type ( IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )? )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_fully_specified_type_in_single_declaration1212);
                    fully_specified_type116=fully_specified_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fully_specified_type116.getTree());
                    // src/main/resources/shader/antlr/GlslEs.g:296:5: ( IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )? )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==IDENTIFIER) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // src/main/resources/shader/antlr/GlslEs.g:296:7: IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )?
                            {
                            IDENTIFIER117=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_single_declaration1220); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            IDENTIFIER117_tree = (CommonTree)adaptor.create(IDENTIFIER117);
                            adaptor.addChild(root_0, IDENTIFIER117_tree);
                            }
                            // src/main/resources/shader/antlr/GlslEs.g:297:7: ( LEFT_BRACKET constant_expression RIGHT_BRACKET | EQUAL initializer )?
                            int alt29=3;
                            int LA29_0 = input.LA(1);

                            if ( (LA29_0==LEFT_BRACKET) ) {
                                alt29=1;
                            }
                            else if ( (LA29_0==EQUAL) ) {
                                alt29=2;
                            }
                            switch (alt29) {
                                case 1 :
                                    // src/main/resources/shader/antlr/GlslEs.g:297:11: LEFT_BRACKET constant_expression RIGHT_BRACKET
                                    {
                                    LEFT_BRACKET118=(Token)match(input,LEFT_BRACKET,FOLLOW_LEFT_BRACKET_in_single_declaration1232); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    LEFT_BRACKET118_tree = (CommonTree)adaptor.create(LEFT_BRACKET118);
                                    adaptor.addChild(root_0, LEFT_BRACKET118_tree);
                                    }
                                    pushFollow(FOLLOW_constant_expression_in_single_declaration1234);
                                    constant_expression119=constant_expression();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant_expression119.getTree());
                                    RIGHT_BRACKET120=(Token)match(input,RIGHT_BRACKET,FOLLOW_RIGHT_BRACKET_in_single_declaration1236); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    RIGHT_BRACKET120_tree = (CommonTree)adaptor.create(RIGHT_BRACKET120);
                                    adaptor.addChild(root_0, RIGHT_BRACKET120_tree);
                                    }

                                    }
                                    break;
                                case 2 :
                                    // src/main/resources/shader/antlr/GlslEs.g:298:11: EQUAL initializer
                                    {
                                    EQUAL121=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_single_declaration1248); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    EQUAL121_tree = (CommonTree)adaptor.create(EQUAL121);
                                    adaptor.addChild(root_0, EQUAL121_tree);
                                    }
                                    pushFollow(FOLLOW_initializer_in_single_declaration1250);
                                    initializer122=initializer();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initializer122.getTree());

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:301:5: INVARIANT IDENTIFIER
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    INVARIANT123=(Token)match(input,INVARIANT,FOLLOW_INVARIANT_in_single_declaration1274); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INVARIANT123_tree = (CommonTree)adaptor.create(INVARIANT123);
                    adaptor.addChild(root_0, INVARIANT123_tree);
                    }
                    IDENTIFIER124=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_single_declaration1276); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER124_tree = (CommonTree)adaptor.create(IDENTIFIER124);
                    adaptor.addChild(root_0, IDENTIFIER124_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "single_declaration"

    public static class fully_specified_type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fully_specified_type"
    // src/main/resources/shader/antlr/GlslEs.g:306:1: fully_specified_type : ( type_specifier | type_qualifier type_specifier );
    public final GlslEsParser.fully_specified_type_return fully_specified_type() throws RecognitionException {
        GlslEsParser.fully_specified_type_return retval = new GlslEsParser.fully_specified_type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.type_specifier_return type_specifier125 = null;

        GlslEsParser.type_qualifier_return type_qualifier126 = null;

        GlslEsParser.type_specifier_return type_specifier127 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:307:3: ( type_specifier | type_qualifier type_specifier )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==IDENTIFIER||LA32_0==VOID||(LA32_0>=FLOAT && LA32_0<=MAT4)||(LA32_0>=SAMPLER2D && LA32_0<=STRUCT)) ) {
                alt32=1;
            }
            else if ( ((LA32_0>=INVARIANT && LA32_0<=UNIFORM)) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:307:5: type_specifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_type_specifier_in_fully_specified_type1294);
                    type_specifier125=type_specifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier125.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:308:5: type_qualifier type_specifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_type_qualifier_in_fully_specified_type1300);
                    type_qualifier126=type_qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_qualifier126.getTree());
                    pushFollow(FOLLOW_type_specifier_in_fully_specified_type1302);
                    type_specifier127=type_specifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier127.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fully_specified_type"

    public static class type_qualifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type_qualifier"
    // src/main/resources/shader/antlr/GlslEs.g:311:1: type_qualifier : ( CONST | ATTRIBUTE | VARYING | INVARIANT VARYING | UNIFORM );
    public final GlslEsParser.type_qualifier_return type_qualifier() throws RecognitionException {
        GlslEsParser.type_qualifier_return retval = new GlslEsParser.type_qualifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CONST128=null;
        Token ATTRIBUTE129=null;
        Token VARYING130=null;
        Token INVARIANT131=null;
        Token VARYING132=null;
        Token UNIFORM133=null;

        CommonTree CONST128_tree=null;
        CommonTree ATTRIBUTE129_tree=null;
        CommonTree VARYING130_tree=null;
        CommonTree INVARIANT131_tree=null;
        CommonTree VARYING132_tree=null;
        CommonTree UNIFORM133_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:312:3: ( CONST | ATTRIBUTE | VARYING | INVARIANT VARYING | UNIFORM )
            int alt33=5;
            switch ( input.LA(1) ) {
            case CONST:
                {
                alt33=1;
                }
                break;
            case ATTRIBUTE:
                {
                alt33=2;
                }
                break;
            case VARYING:
                {
                alt33=3;
                }
                break;
            case INVARIANT:
                {
                alt33=4;
                }
                break;
            case UNIFORM:
                {
                alt33=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }

            switch (alt33) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:312:5: CONST
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CONST128=(Token)match(input,CONST,FOLLOW_CONST_in_type_qualifier1315); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CONST128_tree = (CommonTree)adaptor.create(CONST128);
                    adaptor.addChild(root_0, CONST128_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:313:5: ATTRIBUTE
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    ATTRIBUTE129=(Token)match(input,ATTRIBUTE,FOLLOW_ATTRIBUTE_in_type_qualifier1321); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ATTRIBUTE129_tree = (CommonTree)adaptor.create(ATTRIBUTE129);
                    adaptor.addChild(root_0, ATTRIBUTE129_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:314:5: VARYING
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    VARYING130=(Token)match(input,VARYING,FOLLOW_VARYING_in_type_qualifier1330); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VARYING130_tree = (CommonTree)adaptor.create(VARYING130);
                    adaptor.addChild(root_0, VARYING130_tree);
                    }

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:315:5: INVARIANT VARYING
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    INVARIANT131=(Token)match(input,INVARIANT,FOLLOW_INVARIANT_in_type_qualifier1336); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INVARIANT131_tree = (CommonTree)adaptor.create(INVARIANT131);
                    adaptor.addChild(root_0, INVARIANT131_tree);
                    }
                    VARYING132=(Token)match(input,VARYING,FOLLOW_VARYING_in_type_qualifier1338); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VARYING132_tree = (CommonTree)adaptor.create(VARYING132);
                    adaptor.addChild(root_0, VARYING132_tree);
                    }

                    }
                    break;
                case 5 :
                    // src/main/resources/shader/antlr/GlslEs.g:316:5: UNIFORM
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    UNIFORM133=(Token)match(input,UNIFORM,FOLLOW_UNIFORM_in_type_qualifier1344); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    UNIFORM133_tree = (CommonTree)adaptor.create(UNIFORM133);
                    adaptor.addChild(root_0, UNIFORM133_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type_qualifier"

    public static class type_specifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type_specifier"
    // src/main/resources/shader/antlr/GlslEs.g:319:1: type_specifier : ( type_specifier_no_prec | precision_qualifier type_specifier_no_prec );
    public final GlslEsParser.type_specifier_return type_specifier() throws RecognitionException {
        GlslEsParser.type_specifier_return retval = new GlslEsParser.type_specifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.type_specifier_no_prec_return type_specifier_no_prec134 = null;

        GlslEsParser.precision_qualifier_return precision_qualifier135 = null;

        GlslEsParser.type_specifier_no_prec_return type_specifier_no_prec136 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:320:3: ( type_specifier_no_prec | precision_qualifier type_specifier_no_prec )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==IDENTIFIER||LA34_0==VOID||(LA34_0>=FLOAT && LA34_0<=MAT4)||(LA34_0>=SAMPLER2D && LA34_0<=SAMPLERCUBE)||LA34_0==STRUCT) ) {
                alt34=1;
            }
            else if ( ((LA34_0>=HIGH_PRECISION && LA34_0<=LOW_PRECISION)) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:320:5: type_specifier_no_prec
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_type_specifier_no_prec_in_type_specifier1357);
                    type_specifier_no_prec134=type_specifier_no_prec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier_no_prec134.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:321:5: precision_qualifier type_specifier_no_prec
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_precision_qualifier_in_type_specifier1363);
                    precision_qualifier135=precision_qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, precision_qualifier135.getTree());
                    pushFollow(FOLLOW_type_specifier_no_prec_in_type_specifier1365);
                    type_specifier_no_prec136=type_specifier_no_prec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier_no_prec136.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type_specifier"

    public static class type_specifier_no_prec_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type_specifier_no_prec"
    // src/main/resources/shader/antlr/GlslEs.g:324:1: type_specifier_no_prec : ( VOID | FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | SAMPLER2D | SAMPLERCUBE | struct_specifier | IDENTIFIER );
    public final GlslEsParser.type_specifier_no_prec_return type_specifier_no_prec() throws RecognitionException {
        GlslEsParser.type_specifier_no_prec_return retval = new GlslEsParser.type_specifier_no_prec_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token VOID137=null;
        Token FLOAT138=null;
        Token INT139=null;
        Token BOOL140=null;
        Token VEC2141=null;
        Token VEC3142=null;
        Token VEC4143=null;
        Token BVEC2144=null;
        Token BVEC3145=null;
        Token BVEC4146=null;
        Token IVEC2147=null;
        Token IVEC3148=null;
        Token IVEC4149=null;
        Token MAT2150=null;
        Token MAT3151=null;
        Token MAT4152=null;
        Token SAMPLER2D153=null;
        Token SAMPLERCUBE154=null;
        Token IDENTIFIER156=null;
        GlslEsParser.struct_specifier_return struct_specifier155 = null;


        CommonTree VOID137_tree=null;
        CommonTree FLOAT138_tree=null;
        CommonTree INT139_tree=null;
        CommonTree BOOL140_tree=null;
        CommonTree VEC2141_tree=null;
        CommonTree VEC3142_tree=null;
        CommonTree VEC4143_tree=null;
        CommonTree BVEC2144_tree=null;
        CommonTree BVEC3145_tree=null;
        CommonTree BVEC4146_tree=null;
        CommonTree IVEC2147_tree=null;
        CommonTree IVEC3148_tree=null;
        CommonTree IVEC4149_tree=null;
        CommonTree MAT2150_tree=null;
        CommonTree MAT3151_tree=null;
        CommonTree MAT4152_tree=null;
        CommonTree SAMPLER2D153_tree=null;
        CommonTree SAMPLERCUBE154_tree=null;
        CommonTree IDENTIFIER156_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:325:3: ( VOID | FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | SAMPLER2D | SAMPLERCUBE | struct_specifier | IDENTIFIER )
            int alt35=20;
            switch ( input.LA(1) ) {
            case VOID:
                {
                alt35=1;
                }
                break;
            case FLOAT:
                {
                alt35=2;
                }
                break;
            case INT:
                {
                alt35=3;
                }
                break;
            case BOOL:
                {
                alt35=4;
                }
                break;
            case VEC2:
                {
                alt35=5;
                }
                break;
            case VEC3:
                {
                alt35=6;
                }
                break;
            case VEC4:
                {
                alt35=7;
                }
                break;
            case BVEC2:
                {
                alt35=8;
                }
                break;
            case BVEC3:
                {
                alt35=9;
                }
                break;
            case BVEC4:
                {
                alt35=10;
                }
                break;
            case IVEC2:
                {
                alt35=11;
                }
                break;
            case IVEC3:
                {
                alt35=12;
                }
                break;
            case IVEC4:
                {
                alt35=13;
                }
                break;
            case MAT2:
                {
                alt35=14;
                }
                break;
            case MAT3:
                {
                alt35=15;
                }
                break;
            case MAT4:
                {
                alt35=16;
                }
                break;
            case SAMPLER2D:
                {
                alt35=17;
                }
                break;
            case SAMPLERCUBE:
                {
                alt35=18;
                }
                break;
            case STRUCT:
                {
                alt35=19;
                }
                break;
            case IDENTIFIER:
                {
                alt35=20;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }

            switch (alt35) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:325:5: VOID
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    VOID137=(Token)match(input,VOID,FOLLOW_VOID_in_type_specifier_no_prec1378); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VOID137_tree = (CommonTree)adaptor.create(VOID137);
                    adaptor.addChild(root_0, VOID137_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:326:5: FLOAT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    FLOAT138=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_type_specifier_no_prec1384); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT138_tree = (CommonTree)adaptor.create(FLOAT138);
                    adaptor.addChild(root_0, FLOAT138_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:327:5: INT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    INT139=(Token)match(input,INT,FOLLOW_INT_in_type_specifier_no_prec1390); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT139_tree = (CommonTree)adaptor.create(INT139);
                    adaptor.addChild(root_0, INT139_tree);
                    }

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:328:5: BOOL
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    BOOL140=(Token)match(input,BOOL,FOLLOW_BOOL_in_type_specifier_no_prec1396); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL140_tree = (CommonTree)adaptor.create(BOOL140);
                    adaptor.addChild(root_0, BOOL140_tree);
                    }

                    }
                    break;
                case 5 :
                    // src/main/resources/shader/antlr/GlslEs.g:329:5: VEC2
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    VEC2141=(Token)match(input,VEC2,FOLLOW_VEC2_in_type_specifier_no_prec1402); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VEC2141_tree = (CommonTree)adaptor.create(VEC2141);
                    adaptor.addChild(root_0, VEC2141_tree);
                    }

                    }
                    break;
                case 6 :
                    // src/main/resources/shader/antlr/GlslEs.g:330:5: VEC3
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    VEC3142=(Token)match(input,VEC3,FOLLOW_VEC3_in_type_specifier_no_prec1408); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VEC3142_tree = (CommonTree)adaptor.create(VEC3142);
                    adaptor.addChild(root_0, VEC3142_tree);
                    }

                    }
                    break;
                case 7 :
                    // src/main/resources/shader/antlr/GlslEs.g:331:5: VEC4
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    VEC4143=(Token)match(input,VEC4,FOLLOW_VEC4_in_type_specifier_no_prec1414); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VEC4143_tree = (CommonTree)adaptor.create(VEC4143);
                    adaptor.addChild(root_0, VEC4143_tree);
                    }

                    }
                    break;
                case 8 :
                    // src/main/resources/shader/antlr/GlslEs.g:332:5: BVEC2
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    BVEC2144=(Token)match(input,BVEC2,FOLLOW_BVEC2_in_type_specifier_no_prec1420); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BVEC2144_tree = (CommonTree)adaptor.create(BVEC2144);
                    adaptor.addChild(root_0, BVEC2144_tree);
                    }

                    }
                    break;
                case 9 :
                    // src/main/resources/shader/antlr/GlslEs.g:333:5: BVEC3
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    BVEC3145=(Token)match(input,BVEC3,FOLLOW_BVEC3_in_type_specifier_no_prec1426); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BVEC3145_tree = (CommonTree)adaptor.create(BVEC3145);
                    adaptor.addChild(root_0, BVEC3145_tree);
                    }

                    }
                    break;
                case 10 :
                    // src/main/resources/shader/antlr/GlslEs.g:334:5: BVEC4
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    BVEC4146=(Token)match(input,BVEC4,FOLLOW_BVEC4_in_type_specifier_no_prec1432); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BVEC4146_tree = (CommonTree)adaptor.create(BVEC4146);
                    adaptor.addChild(root_0, BVEC4146_tree);
                    }

                    }
                    break;
                case 11 :
                    // src/main/resources/shader/antlr/GlslEs.g:335:5: IVEC2
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    IVEC2147=(Token)match(input,IVEC2,FOLLOW_IVEC2_in_type_specifier_no_prec1438); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IVEC2147_tree = (CommonTree)adaptor.create(IVEC2147);
                    adaptor.addChild(root_0, IVEC2147_tree);
                    }

                    }
                    break;
                case 12 :
                    // src/main/resources/shader/antlr/GlslEs.g:336:5: IVEC3
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    IVEC3148=(Token)match(input,IVEC3,FOLLOW_IVEC3_in_type_specifier_no_prec1444); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IVEC3148_tree = (CommonTree)adaptor.create(IVEC3148);
                    adaptor.addChild(root_0, IVEC3148_tree);
                    }

                    }
                    break;
                case 13 :
                    // src/main/resources/shader/antlr/GlslEs.g:337:5: IVEC4
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    IVEC4149=(Token)match(input,IVEC4,FOLLOW_IVEC4_in_type_specifier_no_prec1450); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IVEC4149_tree = (CommonTree)adaptor.create(IVEC4149);
                    adaptor.addChild(root_0, IVEC4149_tree);
                    }

                    }
                    break;
                case 14 :
                    // src/main/resources/shader/antlr/GlslEs.g:338:5: MAT2
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    MAT2150=(Token)match(input,MAT2,FOLLOW_MAT2_in_type_specifier_no_prec1456); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAT2150_tree = (CommonTree)adaptor.create(MAT2150);
                    adaptor.addChild(root_0, MAT2150_tree);
                    }

                    }
                    break;
                case 15 :
                    // src/main/resources/shader/antlr/GlslEs.g:339:5: MAT3
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    MAT3151=(Token)match(input,MAT3,FOLLOW_MAT3_in_type_specifier_no_prec1462); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAT3151_tree = (CommonTree)adaptor.create(MAT3151);
                    adaptor.addChild(root_0, MAT3151_tree);
                    }

                    }
                    break;
                case 16 :
                    // src/main/resources/shader/antlr/GlslEs.g:340:5: MAT4
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    MAT4152=(Token)match(input,MAT4,FOLLOW_MAT4_in_type_specifier_no_prec1468); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAT4152_tree = (CommonTree)adaptor.create(MAT4152);
                    adaptor.addChild(root_0, MAT4152_tree);
                    }

                    }
                    break;
                case 17 :
                    // src/main/resources/shader/antlr/GlslEs.g:341:5: SAMPLER2D
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    SAMPLER2D153=(Token)match(input,SAMPLER2D,FOLLOW_SAMPLER2D_in_type_specifier_no_prec1474); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SAMPLER2D153_tree = (CommonTree)adaptor.create(SAMPLER2D153);
                    adaptor.addChild(root_0, SAMPLER2D153_tree);
                    }

                    }
                    break;
                case 18 :
                    // src/main/resources/shader/antlr/GlslEs.g:342:5: SAMPLERCUBE
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    SAMPLERCUBE154=(Token)match(input,SAMPLERCUBE,FOLLOW_SAMPLERCUBE_in_type_specifier_no_prec1480); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SAMPLERCUBE154_tree = (CommonTree)adaptor.create(SAMPLERCUBE154);
                    adaptor.addChild(root_0, SAMPLERCUBE154_tree);
                    }

                    }
                    break;
                case 19 :
                    // src/main/resources/shader/antlr/GlslEs.g:343:5: struct_specifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_struct_specifier_in_type_specifier_no_prec1486);
                    struct_specifier155=struct_specifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_specifier155.getTree());

                    }
                    break;
                case 20 :
                    // src/main/resources/shader/antlr/GlslEs.g:345:5: IDENTIFIER
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    IDENTIFIER156=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_type_specifier_no_prec1493); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER156_tree = (CommonTree)adaptor.create(IDENTIFIER156);
                    adaptor.addChild(root_0, IDENTIFIER156_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type_specifier_no_prec"

    public static class precision_qualifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "precision_qualifier"
    // src/main/resources/shader/antlr/GlslEs.g:348:1: precision_qualifier : ( HIGH_PRECISION | MEDIUM_PRECISION | LOW_PRECISION );
    public final GlslEsParser.precision_qualifier_return precision_qualifier() throws RecognitionException {
        GlslEsParser.precision_qualifier_return retval = new GlslEsParser.precision_qualifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set157=null;

        CommonTree set157_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:349:3: ( HIGH_PRECISION | MEDIUM_PRECISION | LOW_PRECISION )
            // src/main/resources/shader/antlr/GlslEs.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set157=(Token)input.LT(1);
            if ( (input.LA(1)>=HIGH_PRECISION && input.LA(1)<=LOW_PRECISION) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set157));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "precision_qualifier"

    public static class struct_specifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "struct_specifier"
    // src/main/resources/shader/antlr/GlslEs.g:354:1: struct_specifier : STRUCT ( IDENTIFIER )? LEFT_BRACE struct_declaration_list RIGHT_BRACE ;
    public final GlslEsParser.struct_specifier_return struct_specifier() throws RecognitionException {
        GlslEsParser.struct_specifier_return retval = new GlslEsParser.struct_specifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STRUCT158=null;
        Token IDENTIFIER159=null;
        Token LEFT_BRACE160=null;
        Token RIGHT_BRACE162=null;
        GlslEsParser.struct_declaration_list_return struct_declaration_list161 = null;


        CommonTree STRUCT158_tree=null;
        CommonTree IDENTIFIER159_tree=null;
        CommonTree LEFT_BRACE160_tree=null;
        CommonTree RIGHT_BRACE162_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:355:3: ( STRUCT ( IDENTIFIER )? LEFT_BRACE struct_declaration_list RIGHT_BRACE )
            // src/main/resources/shader/antlr/GlslEs.g:355:5: STRUCT ( IDENTIFIER )? LEFT_BRACE struct_declaration_list RIGHT_BRACE
            {
            root_0 = (CommonTree)adaptor.nil();

            STRUCT158=(Token)match(input,STRUCT,FOLLOW_STRUCT_in_struct_specifier1531); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STRUCT158_tree = (CommonTree)adaptor.create(STRUCT158);
            adaptor.addChild(root_0, STRUCT158_tree);
            }
            // src/main/resources/shader/antlr/GlslEs.g:355:12: ( IDENTIFIER )?
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==IDENTIFIER) ) {
                alt36=1;
            }
            switch (alt36) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:355:13: IDENTIFIER
                    {
                    IDENTIFIER159=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_specifier1534); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER159_tree = (CommonTree)adaptor.create(IDENTIFIER159);
                    adaptor.addChild(root_0, IDENTIFIER159_tree);
                    }

                    }
                    break;

            }

            LEFT_BRACE160=(Token)match(input,LEFT_BRACE,FOLLOW_LEFT_BRACE_in_struct_specifier1538); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LEFT_BRACE160_tree = (CommonTree)adaptor.create(LEFT_BRACE160);
            adaptor.addChild(root_0, LEFT_BRACE160_tree);
            }
            pushFollow(FOLLOW_struct_declaration_list_in_struct_specifier1540);
            struct_declaration_list161=struct_declaration_list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_declaration_list161.getTree());
            RIGHT_BRACE162=(Token)match(input,RIGHT_BRACE,FOLLOW_RIGHT_BRACE_in_struct_specifier1542); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RIGHT_BRACE162_tree = (CommonTree)adaptor.create(RIGHT_BRACE162);
            adaptor.addChild(root_0, RIGHT_BRACE162_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "struct_specifier"

    public static class struct_declaration_list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "struct_declaration_list"
    // src/main/resources/shader/antlr/GlslEs.g:358:1: struct_declaration_list : ( struct_declaration )+ ;
    public final GlslEsParser.struct_declaration_list_return struct_declaration_list() throws RecognitionException {
        GlslEsParser.struct_declaration_list_return retval = new GlslEsParser.struct_declaration_list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.struct_declaration_return struct_declaration163 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:359:3: ( ( struct_declaration )+ )
            // src/main/resources/shader/antlr/GlslEs.g:359:5: ( struct_declaration )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:359:5: ( struct_declaration )+
            int cnt37=0;
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==IDENTIFIER||LA37_0==VOID||(LA37_0>=FLOAT && LA37_0<=MAT4)||(LA37_0>=SAMPLER2D && LA37_0<=STRUCT)) ) {
                    alt37=1;
                }


                switch (alt37) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:359:6: struct_declaration
                    {
                    pushFollow(FOLLOW_struct_declaration_in_struct_declaration_list1556);
                    struct_declaration163=struct_declaration();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_declaration163.getTree());

                    }
                    break;

                default :
                    if ( cnt37 >= 1 ) break loop37;
                    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(37, input);
                        throw eee;
                }
                cnt37++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "struct_declaration_list"

    public static class struct_declaration_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "struct_declaration"
    // src/main/resources/shader/antlr/GlslEs.g:362:1: struct_declaration : type_specifier struct_declarator_list SEMICOLON ;
    public final GlslEsParser.struct_declaration_return struct_declaration() throws RecognitionException {
        GlslEsParser.struct_declaration_return retval = new GlslEsParser.struct_declaration_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMICOLON166=null;
        GlslEsParser.type_specifier_return type_specifier164 = null;

        GlslEsParser.struct_declarator_list_return struct_declarator_list165 = null;


        CommonTree SEMICOLON166_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:363:3: ( type_specifier struct_declarator_list SEMICOLON )
            // src/main/resources/shader/antlr/GlslEs.g:363:5: type_specifier struct_declarator_list SEMICOLON
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_type_specifier_in_struct_declaration1571);
            type_specifier164=type_specifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, type_specifier164.getTree());
            pushFollow(FOLLOW_struct_declarator_list_in_struct_declaration1573);
            struct_declarator_list165=struct_declarator_list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_declarator_list165.getTree());
            SEMICOLON166=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_struct_declaration1575); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SEMICOLON166_tree = (CommonTree)adaptor.create(SEMICOLON166);
            adaptor.addChild(root_0, SEMICOLON166_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "struct_declaration"

    public static class struct_declarator_list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "struct_declarator_list"
    // src/main/resources/shader/antlr/GlslEs.g:366:1: struct_declarator_list : struct_declarator ( COMMA struct_declarator )* ;
    public final GlslEsParser.struct_declarator_list_return struct_declarator_list() throws RecognitionException {
        GlslEsParser.struct_declarator_list_return retval = new GlslEsParser.struct_declarator_list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA168=null;
        GlslEsParser.struct_declarator_return struct_declarator167 = null;

        GlslEsParser.struct_declarator_return struct_declarator169 = null;


        CommonTree COMMA168_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:367:3: ( struct_declarator ( COMMA struct_declarator )* )
            // src/main/resources/shader/antlr/GlslEs.g:367:5: struct_declarator ( COMMA struct_declarator )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_struct_declarator_in_struct_declarator_list1588);
            struct_declarator167=struct_declarator();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_declarator167.getTree());
            // src/main/resources/shader/antlr/GlslEs.g:367:23: ( COMMA struct_declarator )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==COMMA) ) {
                    alt38=1;
                }


                switch (alt38) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:367:24: COMMA struct_declarator
                    {
                    COMMA168=(Token)match(input,COMMA,FOLLOW_COMMA_in_struct_declarator_list1591); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA168_tree = (CommonTree)adaptor.create(COMMA168);
                    adaptor.addChild(root_0, COMMA168_tree);
                    }
                    pushFollow(FOLLOW_struct_declarator_in_struct_declarator_list1593);
                    struct_declarator169=struct_declarator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_declarator169.getTree());

                    }
                    break;

                default :
                    break loop38;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "struct_declarator_list"

    public static class struct_declarator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "struct_declarator"
    // src/main/resources/shader/antlr/GlslEs.g:370:1: struct_declarator : IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET )? ;
    public final GlslEsParser.struct_declarator_return struct_declarator() throws RecognitionException {
        GlslEsParser.struct_declarator_return retval = new GlslEsParser.struct_declarator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER170=null;
        Token LEFT_BRACKET171=null;
        Token RIGHT_BRACKET173=null;
        GlslEsParser.constant_expression_return constant_expression172 = null;


        CommonTree IDENTIFIER170_tree=null;
        CommonTree LEFT_BRACKET171_tree=null;
        CommonTree RIGHT_BRACKET173_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:371:3: ( IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET )? )
            // src/main/resources/shader/antlr/GlslEs.g:371:5: IDENTIFIER ( LEFT_BRACKET constant_expression RIGHT_BRACKET )?
            {
            root_0 = (CommonTree)adaptor.nil();

            IDENTIFIER170=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_declarator1608); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER170_tree = (CommonTree)adaptor.create(IDENTIFIER170);
            adaptor.addChild(root_0, IDENTIFIER170_tree);
            }
            // src/main/resources/shader/antlr/GlslEs.g:371:16: ( LEFT_BRACKET constant_expression RIGHT_BRACKET )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==LEFT_BRACKET) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:371:17: LEFT_BRACKET constant_expression RIGHT_BRACKET
                    {
                    LEFT_BRACKET171=(Token)match(input,LEFT_BRACKET,FOLLOW_LEFT_BRACKET_in_struct_declarator1611); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_BRACKET171_tree = (CommonTree)adaptor.create(LEFT_BRACKET171);
                    adaptor.addChild(root_0, LEFT_BRACKET171_tree);
                    }
                    pushFollow(FOLLOW_constant_expression_in_struct_declarator1613);
                    constant_expression172=constant_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant_expression172.getTree());
                    RIGHT_BRACKET173=(Token)match(input,RIGHT_BRACKET,FOLLOW_RIGHT_BRACKET_in_struct_declarator1615); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_BRACKET173_tree = (CommonTree)adaptor.create(RIGHT_BRACKET173);
                    adaptor.addChild(root_0, RIGHT_BRACKET173_tree);
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "struct_declarator"

    public static class initializer_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initializer"
    // src/main/resources/shader/antlr/GlslEs.g:374:1: initializer : assignment_expression ;
    public final GlslEsParser.initializer_return initializer() throws RecognitionException {
        GlslEsParser.initializer_return retval = new GlslEsParser.initializer_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.assignment_expression_return assignment_expression174 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:375:3: ( assignment_expression )
            // src/main/resources/shader/antlr/GlslEs.g:375:5: assignment_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_assignment_expression_in_initializer1630);
            assignment_expression174=assignment_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment_expression174.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "initializer"

    public static class declaration_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "declaration_statement"
    // src/main/resources/shader/antlr/GlslEs.g:378:1: declaration_statement : declaration ;
    public final GlslEsParser.declaration_statement_return declaration_statement() throws RecognitionException {
        GlslEsParser.declaration_statement_return retval = new GlslEsParser.declaration_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.declaration_return declaration175 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:379:3: ( declaration )
            // src/main/resources/shader/antlr/GlslEs.g:379:5: declaration
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_declaration_in_declaration_statement1643);
            declaration175=declaration();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, declaration175.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "declaration_statement"

    public static class statement_no_new_scope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement_no_new_scope"
    // src/main/resources/shader/antlr/GlslEs.g:382:1: statement_no_new_scope : ( compound_statement_with_scope | simple_statement );
    public final GlslEsParser.statement_no_new_scope_return statement_no_new_scope() throws RecognitionException {
        GlslEsParser.statement_no_new_scope_return retval = new GlslEsParser.statement_no_new_scope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.compound_statement_with_scope_return compound_statement_with_scope176 = null;

        GlslEsParser.simple_statement_return simple_statement177 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:383:3: ( compound_statement_with_scope | simple_statement )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==LEFT_BRACE) ) {
                alt40=1;
            }
            else if ( ((LA40_0>=IDENTIFIER && LA40_0<=LEFT_PAREN)||(LA40_0>=INC_OP && LA40_0<=VOID)||(LA40_0>=FLOAT && LA40_0<=BANG)||(LA40_0>=SEMICOLON && LA40_0<=PRECISION)||(LA40_0>=INVARIANT && LA40_0<=STRUCT)||LA40_0==IF||(LA40_0>=WHILE && LA40_0<=DISCARD)) ) {
                alt40=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:383:5: compound_statement_with_scope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_compound_statement_with_scope_in_statement_no_new_scope1656);
                    compound_statement_with_scope176=compound_statement_with_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, compound_statement_with_scope176.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:384:5: simple_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simple_statement_in_statement_no_new_scope1662);
                    simple_statement177=simple_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_statement177.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement_no_new_scope"

    public static class simple_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "simple_statement"
    // src/main/resources/shader/antlr/GlslEs.g:387:1: simple_statement options {backtrack=true; } : ( declaration_statement | expression_statement | selection_statement | iteration_statement | jump_statement );
    public final GlslEsParser.simple_statement_return simple_statement() throws RecognitionException {
        GlslEsParser.simple_statement_return retval = new GlslEsParser.simple_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.declaration_statement_return declaration_statement178 = null;

        GlslEsParser.expression_statement_return expression_statement179 = null;

        GlslEsParser.selection_statement_return selection_statement180 = null;

        GlslEsParser.iteration_statement_return iteration_statement181 = null;

        GlslEsParser.jump_statement_return jump_statement182 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:389:3: ( declaration_statement | expression_statement | selection_statement | iteration_statement | jump_statement )
            int alt41=5;
            alt41 = dfa41.predict(input);
            switch (alt41) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:389:5: declaration_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_declaration_statement_in_simple_statement1684);
                    declaration_statement178=declaration_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, declaration_statement178.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:390:5: expression_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_expression_statement_in_simple_statement1690);
                    expression_statement179=expression_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression_statement179.getTree());

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:391:5: selection_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selection_statement_in_simple_statement1696);
                    selection_statement180=selection_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selection_statement180.getTree());

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:392:5: iteration_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_iteration_statement_in_simple_statement1702);
                    iteration_statement181=iteration_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, iteration_statement181.getTree());

                    }
                    break;
                case 5 :
                    // src/main/resources/shader/antlr/GlslEs.g:393:5: jump_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_jump_statement_in_simple_statement1708);
                    jump_statement182=jump_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, jump_statement182.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "simple_statement"

    public static class compound_statement_with_scope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compound_statement_with_scope"
    // src/main/resources/shader/antlr/GlslEs.g:396:1: compound_statement_with_scope : LEFT_BRACE ( statement_list )? RIGHT_BRACE ;
    public final GlslEsParser.compound_statement_with_scope_return compound_statement_with_scope() throws RecognitionException {
        GlslEsParser.compound_statement_with_scope_return retval = new GlslEsParser.compound_statement_with_scope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LEFT_BRACE183=null;
        Token RIGHT_BRACE185=null;
        GlslEsParser.statement_list_return statement_list184 = null;


        CommonTree LEFT_BRACE183_tree=null;
        CommonTree RIGHT_BRACE185_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:397:3: ( LEFT_BRACE ( statement_list )? RIGHT_BRACE )
            // src/main/resources/shader/antlr/GlslEs.g:397:5: LEFT_BRACE ( statement_list )? RIGHT_BRACE
            {
            root_0 = (CommonTree)adaptor.nil();

            LEFT_BRACE183=(Token)match(input,LEFT_BRACE,FOLLOW_LEFT_BRACE_in_compound_statement_with_scope1721); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LEFT_BRACE183_tree = (CommonTree)adaptor.create(LEFT_BRACE183);
            adaptor.addChild(root_0, LEFT_BRACE183_tree);
            }
            // src/main/resources/shader/antlr/GlslEs.g:397:16: ( statement_list )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( ((LA42_0>=IDENTIFIER && LA42_0<=LEFT_PAREN)||(LA42_0>=INC_OP && LA42_0<=VOID)||(LA42_0>=FLOAT && LA42_0<=BANG)||(LA42_0>=SEMICOLON && LA42_0<=PRECISION)||(LA42_0>=INVARIANT && LA42_0<=LEFT_BRACE)||LA42_0==IF||(LA42_0>=WHILE && LA42_0<=DISCARD)) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:397:17: statement_list
                    {
                    pushFollow(FOLLOW_statement_list_in_compound_statement_with_scope1724);
                    statement_list184=statement_list();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_list184.getTree());

                    }
                    break;

            }

            RIGHT_BRACE185=(Token)match(input,RIGHT_BRACE,FOLLOW_RIGHT_BRACE_in_compound_statement_with_scope1728); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RIGHT_BRACE185_tree = (CommonTree)adaptor.create(RIGHT_BRACE185);
            adaptor.addChild(root_0, RIGHT_BRACE185_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "compound_statement_with_scope"

    public static class statement_with_scope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement_with_scope"
    // src/main/resources/shader/antlr/GlslEs.g:400:1: statement_with_scope : ( compound_statement_no_new_scope | simple_statement );
    public final GlslEsParser.statement_with_scope_return statement_with_scope() throws RecognitionException {
        GlslEsParser.statement_with_scope_return retval = new GlslEsParser.statement_with_scope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.compound_statement_no_new_scope_return compound_statement_no_new_scope186 = null;

        GlslEsParser.simple_statement_return simple_statement187 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:401:3: ( compound_statement_no_new_scope | simple_statement )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==LEFT_BRACE) ) {
                alt43=1;
            }
            else if ( ((LA43_0>=IDENTIFIER && LA43_0<=LEFT_PAREN)||(LA43_0>=INC_OP && LA43_0<=VOID)||(LA43_0>=FLOAT && LA43_0<=BANG)||(LA43_0>=SEMICOLON && LA43_0<=PRECISION)||(LA43_0>=INVARIANT && LA43_0<=STRUCT)||LA43_0==IF||(LA43_0>=WHILE && LA43_0<=DISCARD)) ) {
                alt43=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:401:5: compound_statement_no_new_scope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_compound_statement_no_new_scope_in_statement_with_scope1741);
                    compound_statement_no_new_scope186=compound_statement_no_new_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, compound_statement_no_new_scope186.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:402:5: simple_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simple_statement_in_statement_with_scope1747);
                    simple_statement187=simple_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_statement187.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement_with_scope"

    public static class compound_statement_no_new_scope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compound_statement_no_new_scope"
    // src/main/resources/shader/antlr/GlslEs.g:405:1: compound_statement_no_new_scope : LEFT_BRACE ( statement_list )? RIGHT_BRACE ;
    public final GlslEsParser.compound_statement_no_new_scope_return compound_statement_no_new_scope() throws RecognitionException {
        GlslEsParser.compound_statement_no_new_scope_return retval = new GlslEsParser.compound_statement_no_new_scope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LEFT_BRACE188=null;
        Token RIGHT_BRACE190=null;
        GlslEsParser.statement_list_return statement_list189 = null;


        CommonTree LEFT_BRACE188_tree=null;
        CommonTree RIGHT_BRACE190_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:406:3: ( LEFT_BRACE ( statement_list )? RIGHT_BRACE )
            // src/main/resources/shader/antlr/GlslEs.g:406:5: LEFT_BRACE ( statement_list )? RIGHT_BRACE
            {
            root_0 = (CommonTree)adaptor.nil();

            LEFT_BRACE188=(Token)match(input,LEFT_BRACE,FOLLOW_LEFT_BRACE_in_compound_statement_no_new_scope1760); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LEFT_BRACE188_tree = (CommonTree)adaptor.create(LEFT_BRACE188);
            adaptor.addChild(root_0, LEFT_BRACE188_tree);
            }
            // src/main/resources/shader/antlr/GlslEs.g:406:16: ( statement_list )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( ((LA44_0>=IDENTIFIER && LA44_0<=LEFT_PAREN)||(LA44_0>=INC_OP && LA44_0<=VOID)||(LA44_0>=FLOAT && LA44_0<=BANG)||(LA44_0>=SEMICOLON && LA44_0<=PRECISION)||(LA44_0>=INVARIANT && LA44_0<=LEFT_BRACE)||LA44_0==IF||(LA44_0>=WHILE && LA44_0<=DISCARD)) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:406:17: statement_list
                    {
                    pushFollow(FOLLOW_statement_list_in_compound_statement_no_new_scope1763);
                    statement_list189=statement_list();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_list189.getTree());

                    }
                    break;

            }

            RIGHT_BRACE190=(Token)match(input,RIGHT_BRACE,FOLLOW_RIGHT_BRACE_in_compound_statement_no_new_scope1767); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RIGHT_BRACE190_tree = (CommonTree)adaptor.create(RIGHT_BRACE190);
            adaptor.addChild(root_0, RIGHT_BRACE190_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "compound_statement_no_new_scope"

    public static class statement_list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement_list"
    // src/main/resources/shader/antlr/GlslEs.g:409:1: statement_list : ( statement_no_new_scope )+ ;
    public final GlslEsParser.statement_list_return statement_list() throws RecognitionException {
        GlslEsParser.statement_list_return retval = new GlslEsParser.statement_list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.statement_no_new_scope_return statement_no_new_scope191 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:410:3: ( ( statement_no_new_scope )+ )
            // src/main/resources/shader/antlr/GlslEs.g:410:5: ( statement_no_new_scope )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:410:5: ( statement_no_new_scope )+
            int cnt45=0;
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( ((LA45_0>=IDENTIFIER && LA45_0<=LEFT_PAREN)||(LA45_0>=INC_OP && LA45_0<=VOID)||(LA45_0>=FLOAT && LA45_0<=BANG)||(LA45_0>=SEMICOLON && LA45_0<=PRECISION)||(LA45_0>=INVARIANT && LA45_0<=LEFT_BRACE)||LA45_0==IF||(LA45_0>=WHILE && LA45_0<=DISCARD)) ) {
                    alt45=1;
                }


                switch (alt45) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:410:6: statement_no_new_scope
                    {
                    pushFollow(FOLLOW_statement_no_new_scope_in_statement_list1781);
                    statement_no_new_scope191=statement_no_new_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_no_new_scope191.getTree());

                    }
                    break;

                default :
                    if ( cnt45 >= 1 ) break loop45;
                    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(45, input);
                        throw eee;
                }
                cnt45++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement_list"

    public static class expression_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression_statement"
    // src/main/resources/shader/antlr/GlslEs.g:413:1: expression_statement : ( expression )? SEMICOLON ;
    public final GlslEsParser.expression_statement_return expression_statement() throws RecognitionException {
        GlslEsParser.expression_statement_return retval = new GlslEsParser.expression_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMICOLON193=null;
        GlslEsParser.expression_return expression192 = null;


        CommonTree SEMICOLON193_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:414:3: ( ( expression )? SEMICOLON )
            // src/main/resources/shader/antlr/GlslEs.g:414:5: ( expression )? SEMICOLON
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:414:5: ( expression )?
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( ((LA46_0>=IDENTIFIER && LA46_0<=LEFT_PAREN)||(LA46_0>=INC_OP && LA46_0<=DEC_OP)||(LA46_0>=FLOAT && LA46_0<=BANG)) ) {
                alt46=1;
            }
            switch (alt46) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:414:6: expression
                    {
                    pushFollow(FOLLOW_expression_in_expression_statement1797);
                    expression192=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression192.getTree());

                    }
                    break;

            }

            SEMICOLON193=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_expression_statement1801); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SEMICOLON193_tree = (CommonTree)adaptor.create(SEMICOLON193);
            adaptor.addChild(root_0, SEMICOLON193_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression_statement"

    public static class selection_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selection_statement"
    // src/main/resources/shader/antlr/GlslEs.g:417:1: selection_statement options {backtrack=true; } : ( IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope ELSE statement_with_scope | IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope );
    public final GlslEsParser.selection_statement_return selection_statement() throws RecognitionException {
        GlslEsParser.selection_statement_return retval = new GlslEsParser.selection_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IF194=null;
        Token LEFT_PAREN195=null;
        Token RIGHT_PAREN197=null;
        Token ELSE199=null;
        Token IF201=null;
        Token LEFT_PAREN202=null;
        Token RIGHT_PAREN204=null;
        GlslEsParser.expression_return expression196 = null;

        GlslEsParser.statement_with_scope_return statement_with_scope198 = null;

        GlslEsParser.statement_with_scope_return statement_with_scope200 = null;

        GlslEsParser.expression_return expression203 = null;

        GlslEsParser.statement_with_scope_return statement_with_scope205 = null;


        CommonTree IF194_tree=null;
        CommonTree LEFT_PAREN195_tree=null;
        CommonTree RIGHT_PAREN197_tree=null;
        CommonTree ELSE199_tree=null;
        CommonTree IF201_tree=null;
        CommonTree LEFT_PAREN202_tree=null;
        CommonTree RIGHT_PAREN204_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:419:3: ( IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope ELSE statement_with_scope | IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==IF) ) {
                int LA47_1 = input.LA(2);

                if ( (synpred12_GlslEs()) ) {
                    alt47=1;
                }
                else if ( (true) ) {
                    alt47=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 47, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:419:5: IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope ELSE statement_with_scope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    IF194=(Token)match(input,IF,FOLLOW_IF_in_selection_statement1823); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IF194_tree = (CommonTree)adaptor.create(IF194);
                    adaptor.addChild(root_0, IF194_tree);
                    }
                    LEFT_PAREN195=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_selection_statement1825); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN195_tree = (CommonTree)adaptor.create(LEFT_PAREN195);
                    adaptor.addChild(root_0, LEFT_PAREN195_tree);
                    }
                    pushFollow(FOLLOW_expression_in_selection_statement1827);
                    expression196=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression196.getTree());
                    RIGHT_PAREN197=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_selection_statement1829); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN197_tree = (CommonTree)adaptor.create(RIGHT_PAREN197);
                    adaptor.addChild(root_0, RIGHT_PAREN197_tree);
                    }
                    pushFollow(FOLLOW_statement_with_scope_in_selection_statement1831);
                    statement_with_scope198=statement_with_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_with_scope198.getTree());
                    ELSE199=(Token)match(input,ELSE,FOLLOW_ELSE_in_selection_statement1833); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ELSE199_tree = (CommonTree)adaptor.create(ELSE199);
                    adaptor.addChild(root_0, ELSE199_tree);
                    }
                    pushFollow(FOLLOW_statement_with_scope_in_selection_statement1835);
                    statement_with_scope200=statement_with_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_with_scope200.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:420:5: IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    IF201=(Token)match(input,IF,FOLLOW_IF_in_selection_statement1841); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IF201_tree = (CommonTree)adaptor.create(IF201);
                    adaptor.addChild(root_0, IF201_tree);
                    }
                    LEFT_PAREN202=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_selection_statement1843); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN202_tree = (CommonTree)adaptor.create(LEFT_PAREN202);
                    adaptor.addChild(root_0, LEFT_PAREN202_tree);
                    }
                    pushFollow(FOLLOW_expression_in_selection_statement1845);
                    expression203=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression203.getTree());
                    RIGHT_PAREN204=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_selection_statement1847); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN204_tree = (CommonTree)adaptor.create(RIGHT_PAREN204);
                    adaptor.addChild(root_0, RIGHT_PAREN204_tree);
                    }
                    pushFollow(FOLLOW_statement_with_scope_in_selection_statement1849);
                    statement_with_scope205=statement_with_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_with_scope205.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selection_statement"

    public static class condition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condition"
    // src/main/resources/shader/antlr/GlslEs.g:423:1: condition : ( expression | fully_specified_type IDENTIFIER EQUAL initializer );
    public final GlslEsParser.condition_return condition() throws RecognitionException {
        GlslEsParser.condition_return retval = new GlslEsParser.condition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER208=null;
        Token EQUAL209=null;
        GlslEsParser.expression_return expression206 = null;

        GlslEsParser.fully_specified_type_return fully_specified_type207 = null;

        GlslEsParser.initializer_return initializer210 = null;


        CommonTree IDENTIFIER208_tree=null;
        CommonTree EQUAL209_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:424:3: ( expression | fully_specified_type IDENTIFIER EQUAL initializer )
            int alt48=2;
            alt48 = dfa48.predict(input);
            switch (alt48) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:424:5: expression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_expression_in_condition1862);
                    expression206=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression206.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:425:5: fully_specified_type IDENTIFIER EQUAL initializer
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_fully_specified_type_in_condition1868);
                    fully_specified_type207=fully_specified_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fully_specified_type207.getTree());
                    IDENTIFIER208=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_condition1870); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER208_tree = (CommonTree)adaptor.create(IDENTIFIER208);
                    adaptor.addChild(root_0, IDENTIFIER208_tree);
                    }
                    EQUAL209=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_condition1872); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUAL209_tree = (CommonTree)adaptor.create(EQUAL209);
                    adaptor.addChild(root_0, EQUAL209_tree);
                    }
                    pushFollow(FOLLOW_initializer_in_condition1874);
                    initializer210=initializer();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initializer210.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "condition"

    public static class iteration_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "iteration_statement"
    // src/main/resources/shader/antlr/GlslEs.g:428:1: iteration_statement : ( WHILE LEFT_PAREN condition RIGHT_PAREN statement_no_new_scope | DO statement_with_scope WHILE LEFT_PAREN expression RIGHT_PAREN SEMICOLON | FOR LEFT_PAREN for_init_statement for_rest_statement RIGHT_PAREN statement_no_new_scope );
    public final GlslEsParser.iteration_statement_return iteration_statement() throws RecognitionException {
        GlslEsParser.iteration_statement_return retval = new GlslEsParser.iteration_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHILE211=null;
        Token LEFT_PAREN212=null;
        Token RIGHT_PAREN214=null;
        Token DO216=null;
        Token WHILE218=null;
        Token LEFT_PAREN219=null;
        Token RIGHT_PAREN221=null;
        Token SEMICOLON222=null;
        Token FOR223=null;
        Token LEFT_PAREN224=null;
        Token RIGHT_PAREN227=null;
        GlslEsParser.condition_return condition213 = null;

        GlslEsParser.statement_no_new_scope_return statement_no_new_scope215 = null;

        GlslEsParser.statement_with_scope_return statement_with_scope217 = null;

        GlslEsParser.expression_return expression220 = null;

        GlslEsParser.for_init_statement_return for_init_statement225 = null;

        GlslEsParser.for_rest_statement_return for_rest_statement226 = null;

        GlslEsParser.statement_no_new_scope_return statement_no_new_scope228 = null;


        CommonTree WHILE211_tree=null;
        CommonTree LEFT_PAREN212_tree=null;
        CommonTree RIGHT_PAREN214_tree=null;
        CommonTree DO216_tree=null;
        CommonTree WHILE218_tree=null;
        CommonTree LEFT_PAREN219_tree=null;
        CommonTree RIGHT_PAREN221_tree=null;
        CommonTree SEMICOLON222_tree=null;
        CommonTree FOR223_tree=null;
        CommonTree LEFT_PAREN224_tree=null;
        CommonTree RIGHT_PAREN227_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:429:3: ( WHILE LEFT_PAREN condition RIGHT_PAREN statement_no_new_scope | DO statement_with_scope WHILE LEFT_PAREN expression RIGHT_PAREN SEMICOLON | FOR LEFT_PAREN for_init_statement for_rest_statement RIGHT_PAREN statement_no_new_scope )
            int alt49=3;
            switch ( input.LA(1) ) {
            case WHILE:
                {
                alt49=1;
                }
                break;
            case DO:
                {
                alt49=2;
                }
                break;
            case FOR:
                {
                alt49=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;
            }

            switch (alt49) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:429:5: WHILE LEFT_PAREN condition RIGHT_PAREN statement_no_new_scope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    WHILE211=(Token)match(input,WHILE,FOLLOW_WHILE_in_iteration_statement1887); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WHILE211_tree = (CommonTree)adaptor.create(WHILE211);
                    adaptor.addChild(root_0, WHILE211_tree);
                    }
                    LEFT_PAREN212=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_iteration_statement1889); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN212_tree = (CommonTree)adaptor.create(LEFT_PAREN212);
                    adaptor.addChild(root_0, LEFT_PAREN212_tree);
                    }
                    pushFollow(FOLLOW_condition_in_iteration_statement1891);
                    condition213=condition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, condition213.getTree());
                    RIGHT_PAREN214=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_iteration_statement1893); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN214_tree = (CommonTree)adaptor.create(RIGHT_PAREN214);
                    adaptor.addChild(root_0, RIGHT_PAREN214_tree);
                    }
                    pushFollow(FOLLOW_statement_no_new_scope_in_iteration_statement1895);
                    statement_no_new_scope215=statement_no_new_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_no_new_scope215.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:430:5: DO statement_with_scope WHILE LEFT_PAREN expression RIGHT_PAREN SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    DO216=(Token)match(input,DO,FOLLOW_DO_in_iteration_statement1901); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DO216_tree = (CommonTree)adaptor.create(DO216);
                    adaptor.addChild(root_0, DO216_tree);
                    }
                    pushFollow(FOLLOW_statement_with_scope_in_iteration_statement1903);
                    statement_with_scope217=statement_with_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_with_scope217.getTree());
                    WHILE218=(Token)match(input,WHILE,FOLLOW_WHILE_in_iteration_statement1905); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WHILE218_tree = (CommonTree)adaptor.create(WHILE218);
                    adaptor.addChild(root_0, WHILE218_tree);
                    }
                    LEFT_PAREN219=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_iteration_statement1907); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN219_tree = (CommonTree)adaptor.create(LEFT_PAREN219);
                    adaptor.addChild(root_0, LEFT_PAREN219_tree);
                    }
                    pushFollow(FOLLOW_expression_in_iteration_statement1909);
                    expression220=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression220.getTree());
                    RIGHT_PAREN221=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_iteration_statement1911); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN221_tree = (CommonTree)adaptor.create(RIGHT_PAREN221);
                    adaptor.addChild(root_0, RIGHT_PAREN221_tree);
                    }
                    SEMICOLON222=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_iteration_statement1913); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON222_tree = (CommonTree)adaptor.create(SEMICOLON222);
                    adaptor.addChild(root_0, SEMICOLON222_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:431:5: FOR LEFT_PAREN for_init_statement for_rest_statement RIGHT_PAREN statement_no_new_scope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    FOR223=(Token)match(input,FOR,FOLLOW_FOR_in_iteration_statement1919); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR223_tree = (CommonTree)adaptor.create(FOR223);
                    adaptor.addChild(root_0, FOR223_tree);
                    }
                    LEFT_PAREN224=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_iteration_statement1921); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN224_tree = (CommonTree)adaptor.create(LEFT_PAREN224);
                    adaptor.addChild(root_0, LEFT_PAREN224_tree);
                    }
                    pushFollow(FOLLOW_for_init_statement_in_iteration_statement1923);
                    for_init_statement225=for_init_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, for_init_statement225.getTree());
                    pushFollow(FOLLOW_for_rest_statement_in_iteration_statement1925);
                    for_rest_statement226=for_rest_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, for_rest_statement226.getTree());
                    RIGHT_PAREN227=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_iteration_statement1927); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN227_tree = (CommonTree)adaptor.create(RIGHT_PAREN227);
                    adaptor.addChild(root_0, RIGHT_PAREN227_tree);
                    }
                    pushFollow(FOLLOW_statement_no_new_scope_in_iteration_statement1929);
                    statement_no_new_scope228=statement_no_new_scope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement_no_new_scope228.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "iteration_statement"

    public static class for_init_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "for_init_statement"
    // src/main/resources/shader/antlr/GlslEs.g:434:1: for_init_statement options {backtrack=true; } : ( expression_statement | declaration_statement );
    public final GlslEsParser.for_init_statement_return for_init_statement() throws RecognitionException {
        GlslEsParser.for_init_statement_return retval = new GlslEsParser.for_init_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.expression_statement_return expression_statement229 = null;

        GlslEsParser.declaration_statement_return declaration_statement230 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:436:3: ( expression_statement | declaration_statement )
            int alt50=2;
            alt50 = dfa50.predict(input);
            switch (alt50) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:436:5: expression_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_expression_statement_in_for_init_statement1951);
                    expression_statement229=expression_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression_statement229.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:437:5: declaration_statement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_declaration_statement_in_for_init_statement1957);
                    declaration_statement230=declaration_statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, declaration_statement230.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "for_init_statement"

    public static class for_rest_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "for_rest_statement"
    // src/main/resources/shader/antlr/GlslEs.g:440:1: for_rest_statement : ( condition )? SEMICOLON ( expression )? ;
    public final GlslEsParser.for_rest_statement_return for_rest_statement() throws RecognitionException {
        GlslEsParser.for_rest_statement_return retval = new GlslEsParser.for_rest_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMICOLON232=null;
        GlslEsParser.condition_return condition231 = null;

        GlslEsParser.expression_return expression233 = null;


        CommonTree SEMICOLON232_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:441:3: ( ( condition )? SEMICOLON ( expression )? )
            // src/main/resources/shader/antlr/GlslEs.g:441:5: ( condition )? SEMICOLON ( expression )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/main/resources/shader/antlr/GlslEs.g:441:5: ( condition )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( ((LA51_0>=IDENTIFIER && LA51_0<=LEFT_PAREN)||(LA51_0>=INC_OP && LA51_0<=VOID)||(LA51_0>=FLOAT && LA51_0<=BANG)||(LA51_0>=INVARIANT && LA51_0<=STRUCT)) ) {
                alt51=1;
            }
            switch (alt51) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:441:6: condition
                    {
                    pushFollow(FOLLOW_condition_in_for_rest_statement1971);
                    condition231=condition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, condition231.getTree());

                    }
                    break;

            }

            SEMICOLON232=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_for_rest_statement1975); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SEMICOLON232_tree = (CommonTree)adaptor.create(SEMICOLON232);
            adaptor.addChild(root_0, SEMICOLON232_tree);
            }
            // src/main/resources/shader/antlr/GlslEs.g:441:28: ( expression )?
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( ((LA52_0>=IDENTIFIER && LA52_0<=LEFT_PAREN)||(LA52_0>=INC_OP && LA52_0<=DEC_OP)||(LA52_0>=FLOAT && LA52_0<=BANG)) ) {
                alt52=1;
            }
            switch (alt52) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:441:29: expression
                    {
                    pushFollow(FOLLOW_expression_in_for_rest_statement1978);
                    expression233=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression233.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "for_rest_statement"

    public static class jump_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "jump_statement"
    // src/main/resources/shader/antlr/GlslEs.g:444:1: jump_statement : ( CONTINUE SEMICOLON | BREAK SEMICOLON | RETURN ( expression )? SEMICOLON | DISCARD SEMICOLON );
    public final GlslEsParser.jump_statement_return jump_statement() throws RecognitionException {
        GlslEsParser.jump_statement_return retval = new GlslEsParser.jump_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CONTINUE234=null;
        Token SEMICOLON235=null;
        Token BREAK236=null;
        Token SEMICOLON237=null;
        Token RETURN238=null;
        Token SEMICOLON240=null;
        Token DISCARD241=null;
        Token SEMICOLON242=null;
        GlslEsParser.expression_return expression239 = null;


        CommonTree CONTINUE234_tree=null;
        CommonTree SEMICOLON235_tree=null;
        CommonTree BREAK236_tree=null;
        CommonTree SEMICOLON237_tree=null;
        CommonTree RETURN238_tree=null;
        CommonTree SEMICOLON240_tree=null;
        CommonTree DISCARD241_tree=null;
        CommonTree SEMICOLON242_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:445:3: ( CONTINUE SEMICOLON | BREAK SEMICOLON | RETURN ( expression )? SEMICOLON | DISCARD SEMICOLON )
            int alt54=4;
            switch ( input.LA(1) ) {
            case CONTINUE:
                {
                alt54=1;
                }
                break;
            case BREAK:
                {
                alt54=2;
                }
                break;
            case RETURN:
                {
                alt54=3;
                }
                break;
            case DISCARD:
                {
                alt54=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }

            switch (alt54) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:445:5: CONTINUE SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CONTINUE234=(Token)match(input,CONTINUE,FOLLOW_CONTINUE_in_jump_statement1993); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CONTINUE234_tree = (CommonTree)adaptor.create(CONTINUE234);
                    adaptor.addChild(root_0, CONTINUE234_tree);
                    }
                    SEMICOLON235=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_jump_statement1995); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON235_tree = (CommonTree)adaptor.create(SEMICOLON235);
                    adaptor.addChild(root_0, SEMICOLON235_tree);
                    }

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:446:5: BREAK SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    BREAK236=(Token)match(input,BREAK,FOLLOW_BREAK_in_jump_statement2001); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BREAK236_tree = (CommonTree)adaptor.create(BREAK236);
                    adaptor.addChild(root_0, BREAK236_tree);
                    }
                    SEMICOLON237=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_jump_statement2003); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON237_tree = (CommonTree)adaptor.create(SEMICOLON237);
                    adaptor.addChild(root_0, SEMICOLON237_tree);
                    }

                    }
                    break;
                case 3 :
                    // src/main/resources/shader/antlr/GlslEs.g:447:5: RETURN ( expression )? SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    RETURN238=(Token)match(input,RETURN,FOLLOW_RETURN_in_jump_statement2009); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RETURN238_tree = (CommonTree)adaptor.create(RETURN238);
                    adaptor.addChild(root_0, RETURN238_tree);
                    }
                    // src/main/resources/shader/antlr/GlslEs.g:447:12: ( expression )?
                    int alt53=2;
                    int LA53_0 = input.LA(1);

                    if ( ((LA53_0>=IDENTIFIER && LA53_0<=LEFT_PAREN)||(LA53_0>=INC_OP && LA53_0<=DEC_OP)||(LA53_0>=FLOAT && LA53_0<=BANG)) ) {
                        alt53=1;
                    }
                    switch (alt53) {
                        case 1 :
                            // src/main/resources/shader/antlr/GlslEs.g:447:13: expression
                            {
                            pushFollow(FOLLOW_expression_in_jump_statement2012);
                            expression239=expression();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, expression239.getTree());

                            }
                            break;

                    }

                    SEMICOLON240=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_jump_statement2016); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON240_tree = (CommonTree)adaptor.create(SEMICOLON240);
                    adaptor.addChild(root_0, SEMICOLON240_tree);
                    }

                    }
                    break;
                case 4 :
                    // src/main/resources/shader/antlr/GlslEs.g:448:5: DISCARD SEMICOLON
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    DISCARD241=(Token)match(input,DISCARD,FOLLOW_DISCARD_in_jump_statement2022); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DISCARD241_tree = (CommonTree)adaptor.create(DISCARD241);
                    adaptor.addChild(root_0, DISCARD241_tree);
                    }
                    SEMICOLON242=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_jump_statement2024); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMICOLON242_tree = (CommonTree)adaptor.create(SEMICOLON242);
                    adaptor.addChild(root_0, SEMICOLON242_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "jump_statement"

    public static class external_declaration_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "external_declaration"
    // src/main/resources/shader/antlr/GlslEs.g:451:1: external_declaration : ( ( function_header )=> function_definition | declaration );
    public final GlslEsParser.external_declaration_return external_declaration() throws RecognitionException {
        GlslEsParser.external_declaration_return retval = new GlslEsParser.external_declaration_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.function_definition_return function_definition243 = null;

        GlslEsParser.declaration_return declaration244 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:452:3: ( ( function_header )=> function_definition | declaration )
            int alt55=2;
            alt55 = dfa55.predict(input);
            switch (alt55) {
                case 1 :
                    // src/main/resources/shader/antlr/GlslEs.g:452:5: ( function_header )=> function_definition
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_function_definition_in_external_declaration2046);
                    function_definition243=function_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function_definition243.getTree());

                    }
                    break;
                case 2 :
                    // src/main/resources/shader/antlr/GlslEs.g:453:5: declaration
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_declaration_in_external_declaration2052);
                    declaration244=declaration();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, declaration244.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "external_declaration"

    public static class function_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_definition"
    // src/main/resources/shader/antlr/GlslEs.g:456:1: function_definition : function_prototype compound_statement_no_new_scope ;
    public final GlslEsParser.function_definition_return function_definition() throws RecognitionException {
        GlslEsParser.function_definition_return retval = new GlslEsParser.function_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        GlslEsParser.function_prototype_return function_prototype245 = null;

        GlslEsParser.compound_statement_no_new_scope_return compound_statement_no_new_scope246 = null;



        try {
            // src/main/resources/shader/antlr/GlslEs.g:457:3: ( function_prototype compound_statement_no_new_scope )
            // src/main/resources/shader/antlr/GlslEs.g:457:5: function_prototype compound_statement_no_new_scope
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_function_prototype_in_function_definition2065);
            function_prototype245=function_prototype();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, function_prototype245.getTree());
            pushFollow(FOLLOW_compound_statement_no_new_scope_in_function_definition2067);
            compound_statement_no_new_scope246=compound_statement_no_new_scope();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, compound_statement_no_new_scope246.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_definition"

    public static class field_selection_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "field_selection"
    // src/main/resources/shader/antlr/GlslEs.g:556:1: field_selection : IDENTIFIER ;
    public final GlslEsParser.field_selection_return field_selection() throws RecognitionException {
        GlslEsParser.field_selection_return retval = new GlslEsParser.field_selection_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IDENTIFIER247=null;

        CommonTree IDENTIFIER247_tree=null;

        try {
            // src/main/resources/shader/antlr/GlslEs.g:557:3: ( IDENTIFIER )
            // src/main/resources/shader/antlr/GlslEs.g:557:5: IDENTIFIER
            {
            root_0 = (CommonTree)adaptor.nil();

            IDENTIFIER247=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_field_selection3075); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER247_tree = (CommonTree)adaptor.create(IDENTIFIER247);
            adaptor.addChild(root_0, IDENTIFIER247_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "field_selection"

    // $ANTLR start synpred1_GlslEs
    public final void synpred1_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:86:5: ( INTCONSTANT )
        // src/main/resources/shader/antlr/GlslEs.g:86:7: INTCONSTANT
        {
        match(input,INTCONSTANT,FOLLOW_INTCONSTANT_in_synpred1_GlslEs221); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_GlslEs

    // $ANTLR start synpred2_GlslEs
    public final void synpred2_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:87:5: ( FLOATCONSTANT )
        // src/main/resources/shader/antlr/GlslEs.g:87:7: FLOATCONSTANT
        {
        match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_synpred2_GlslEs235); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_GlslEs

    // $ANTLR start synpred3_GlslEs
    public final void synpred3_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:88:5: ( BOOLCONSTANT )
        // src/main/resources/shader/antlr/GlslEs.g:88:7: BOOLCONSTANT
        {
        match(input,BOOLCONSTANT,FOLLOW_BOOLCONSTANT_in_synpred3_GlslEs249); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_GlslEs

    // $ANTLR start synpred4_GlslEs
    public final void synpred4_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:89:5: ( LEFT_PAREN )
        // src/main/resources/shader/antlr/GlslEs.g:89:7: LEFT_PAREN
        {
        match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_synpred4_GlslEs263); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_GlslEs

    // $ANTLR start synpred5_GlslEs
    public final void synpred5_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:90:5: ( function_call_header )
        // src/main/resources/shader/antlr/GlslEs.g:90:7: function_call_header
        {
        pushFollow(FOLLOW_function_call_header_in_synpred5_GlslEs277);
        function_call_header();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_GlslEs

    // $ANTLR start synpred6_GlslEs
    public final void synpred6_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:219:5: ( unary_expression assignment_operator )
        // src/main/resources/shader/antlr/GlslEs.g:219:6: unary_expression assignment_operator
        {
        pushFollow(FOLLOW_unary_expression_in_synpred6_GlslEs842);
        unary_expression();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignment_operator_in_synpred6_GlslEs844);
        assignment_operator();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_GlslEs

    // $ANTLR start synpred7_GlslEs
    public final void synpred7_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:246:5: ( function_header )
        // src/main/resources/shader/antlr/GlslEs.g:246:6: function_header
        {
        pushFollow(FOLLOW_function_header_in_synpred7_GlslEs949);
        function_header();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_GlslEs

    // $ANTLR start synpred8_GlslEs
    public final void synpred8_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:389:5: ( declaration_statement )
        // src/main/resources/shader/antlr/GlslEs.g:389:5: declaration_statement
        {
        pushFollow(FOLLOW_declaration_statement_in_synpred8_GlslEs1684);
        declaration_statement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_GlslEs

    // $ANTLR start synpred9_GlslEs
    public final void synpred9_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:390:5: ( expression_statement )
        // src/main/resources/shader/antlr/GlslEs.g:390:5: expression_statement
        {
        pushFollow(FOLLOW_expression_statement_in_synpred9_GlslEs1690);
        expression_statement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_GlslEs

    // $ANTLR start synpred12_GlslEs
    public final void synpred12_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:419:5: ( IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope ELSE statement_with_scope )
        // src/main/resources/shader/antlr/GlslEs.g:419:5: IF LEFT_PAREN expression RIGHT_PAREN statement_with_scope ELSE statement_with_scope
        {
        match(input,IF,FOLLOW_IF_in_synpred12_GlslEs1823); if (state.failed) return ;
        match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_synpred12_GlslEs1825); if (state.failed) return ;
        pushFollow(FOLLOW_expression_in_synpred12_GlslEs1827);
        expression();

        state._fsp--;
        if (state.failed) return ;
        match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_synpred12_GlslEs1829); if (state.failed) return ;
        pushFollow(FOLLOW_statement_with_scope_in_synpred12_GlslEs1831);
        statement_with_scope();

        state._fsp--;
        if (state.failed) return ;
        match(input,ELSE,FOLLOW_ELSE_in_synpred12_GlslEs1833); if (state.failed) return ;
        pushFollow(FOLLOW_statement_with_scope_in_synpred12_GlslEs1835);
        statement_with_scope();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_GlslEs

    // $ANTLR start synpred13_GlslEs
    public final void synpred13_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:436:5: ( expression_statement )
        // src/main/resources/shader/antlr/GlslEs.g:436:5: expression_statement
        {
        pushFollow(FOLLOW_expression_statement_in_synpred13_GlslEs1951);
        expression_statement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_GlslEs

    // $ANTLR start synpred14_GlslEs
    public final void synpred14_GlslEs_fragment() throws RecognitionException {
        // src/main/resources/shader/antlr/GlslEs.g:452:5: ( function_header )
        // src/main/resources/shader/antlr/GlslEs.g:452:6: function_header
        {
        pushFollow(FOLLOW_function_header_in_synpred14_GlslEs2041);
        function_header();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_GlslEs

    // Delegated rules

    public final boolean synpred6_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred14_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_GlslEs() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_GlslEs_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA17 dfa17 = new DFA17(this);
    protected DFA19 dfa19 = new DFA19(this);
    protected DFA41 dfa41 = new DFA41(this);
    protected DFA48 dfa48 = new DFA48(this);
    protected DFA50 dfa50 = new DFA50(this);
    protected DFA55 dfa55 = new DFA55(this);
    static final String DFA4_eotS =
        "\14\uffff";
    static final String DFA4_eofS =
        "\14\uffff";
    static final String DFA4_minS =
        "\1\4\5\0\6\uffff";
    static final String DFA4_maxS =
        "\1\37\5\0\6\uffff";
    static final String DFA4_acceptS =
        "\6\uffff\1\5\1\1\1\2\1\3\1\4\1\6";
    static final String DFA4_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\6\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\4\1\1\1\2\1\3\1\5\10\uffff\17\6",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "85:1: primary_expression_or_function_call : ( ( INTCONSTANT )=> primary_expression | ( FLOATCONSTANT )=> primary_expression | ( BOOLCONSTANT )=> primary_expression | ( LEFT_PAREN )=> primary_expression | ( function_call_header )=> function_call | primary_expression );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
            int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA4_0 = input.LA(1);


                        int index4_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_0==INTCONSTANT) ) {s = 1;}

                        else if ( (LA4_0==FLOATCONSTANT) ) {s = 2;}

                        else if ( (LA4_0==BOOLCONSTANT) ) {s = 3;}

                        else if ( (LA4_0==IDENTIFIER) ) {s = 4;}

                        else if ( (LA4_0==LEFT_PAREN) ) {s = 5;}

                        else if ( ((LA4_0>=FLOAT && LA4_0<=MAT4)) && (synpred5_GlslEs())) {s = 6;}


                        input.seek(index4_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA4_1 = input.LA(1);


                        int index4_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_GlslEs()) ) {s = 7;}

                        else if ( (synpred2_GlslEs()) ) {s = 8;}

                        else if ( (synpred3_GlslEs()) ) {s = 9;}

                        else if ( (synpred4_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index4_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA4_2 = input.LA(1);


                        int index4_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_GlslEs()) ) {s = 7;}

                        else if ( (synpred2_GlslEs()) ) {s = 8;}

                        else if ( (synpred3_GlslEs()) ) {s = 9;}

                        else if ( (synpred4_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index4_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA4_3 = input.LA(1);


                        int index4_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_GlslEs()) ) {s = 7;}

                        else if ( (synpred2_GlslEs()) ) {s = 8;}

                        else if ( (synpred3_GlslEs()) ) {s = 9;}

                        else if ( (synpred4_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index4_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA4_4 = input.LA(1);


                        int index4_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_GlslEs()) ) {s = 7;}

                        else if ( (synpred2_GlslEs()) ) {s = 8;}

                        else if ( (synpred3_GlslEs()) ) {s = 9;}

                        else if ( (synpred4_GlslEs()) ) {s = 10;}

                        else if ( (synpred5_GlslEs()) ) {s = 6;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index4_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA4_5 = input.LA(1);


                        int index4_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_GlslEs()) ) {s = 7;}

                        else if ( (synpred2_GlslEs()) ) {s = 8;}

                        else if ( (synpred3_GlslEs()) ) {s = 9;}

                        else if ( (synpred4_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index4_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA17_eotS =
        "\14\uffff";
    static final String DFA17_eofS =
        "\14\uffff";
    static final String DFA17_minS =
        "\1\4\11\0\2\uffff";
    static final String DFA17_maxS =
        "\1\42\11\0\2\uffff";
    static final String DFA17_acceptS =
        "\12\uffff\1\1\1\2";
    static final String DFA17_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\2\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\7\1\4\1\5\1\6\1\10\4\uffff\1\1\1\2\2\uffff\17\11\3\3",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
    static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
    static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
    static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
    static final short[][] DFA17_transition;

    static {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
        }
    }

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = DFA17_eot;
            this.eof = DFA17_eof;
            this.min = DFA17_min;
            this.max = DFA17_max;
            this.accept = DFA17_accept;
            this.special = DFA17_special;
            this.transition = DFA17_transition;
        }
        public String getDescription() {
            return "218:1: assignment_expression : ( ( unary_expression assignment_operator )=> unary_expression assignment_operator assignment_expression | conditional_expression );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
            int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA17_1 = input.LA(1);


                        int index17_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA17_2 = input.LA(1);


                        int index17_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA17_3 = input.LA(1);


                        int index17_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA17_4 = input.LA(1);


                        int index17_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA17_5 = input.LA(1);


                        int index17_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA17_6 = input.LA(1);


                        int index17_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA17_7 = input.LA(1);


                        int index17_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA17_8 = input.LA(1);


                        int index17_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA17_9 = input.LA(1);


                        int index17_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_GlslEs()) ) {s = 10;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index17_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 17, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA19_eotS =
        "\36\uffff";
    static final String DFA19_eofS =
        "\36\uffff";
    static final String DFA19_minS =
        "\1\4\32\0\3\uffff";
    static final String DFA19_maxS =
        "\1\104\32\0\3\uffff";
    static final String DFA19_acceptS =
        "\33\uffff\1\3\1\1\1\2";
    static final String DFA19_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\3\uffff}>";
    static final String[] DFA19_transitionS = {
            "\1\24\12\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11"+
            "\1\12\1\13\1\14\1\15\1\16\1\17\1\20\26\uffff\1\33\3\uffff\1"+
            "\31\1\26\1\27\1\30\1\32\1\21\1\22\3\25\1\23",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA19_eot = DFA.unpackEncodedString(DFA19_eotS);
    static final short[] DFA19_eof = DFA.unpackEncodedString(DFA19_eofS);
    static final char[] DFA19_min = DFA.unpackEncodedStringToUnsignedChars(DFA19_minS);
    static final char[] DFA19_max = DFA.unpackEncodedStringToUnsignedChars(DFA19_maxS);
    static final short[] DFA19_accept = DFA.unpackEncodedString(DFA19_acceptS);
    static final short[] DFA19_special = DFA.unpackEncodedString(DFA19_specialS);
    static final short[][] DFA19_transition;

    static {
        int numStates = DFA19_transitionS.length;
        DFA19_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA19_transition[i] = DFA.unpackEncodedString(DFA19_transitionS[i]);
        }
    }

    class DFA19 extends DFA {

        public DFA19(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 19;
            this.eot = DFA19_eot;
            this.eof = DFA19_eof;
            this.min = DFA19_min;
            this.max = DFA19_max;
            this.accept = DFA19_accept;
            this.special = DFA19_special;
            this.transition = DFA19_transition;
        }
        public String getDescription() {
            return "245:1: declaration : ( ( function_header )=> function_prototype SEMICOLON | init_declarator_list SEMICOLON | PRECISION precision_qualifier type_specifier_no_prec SEMICOLON );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
            int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA19_1 = input.LA(1);


                        int index19_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA19_2 = input.LA(1);


                        int index19_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA19_3 = input.LA(1);


                        int index19_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA19_4 = input.LA(1);


                        int index19_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA19_5 = input.LA(1);


                        int index19_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA19_6 = input.LA(1);


                        int index19_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA19_7 = input.LA(1);


                        int index19_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA19_8 = input.LA(1);


                        int index19_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA19_9 = input.LA(1);


                        int index19_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA19_10 = input.LA(1);


                        int index19_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA19_11 = input.LA(1);


                        int index19_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA19_12 = input.LA(1);


                        int index19_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA19_13 = input.LA(1);


                        int index19_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA19_14 = input.LA(1);


                        int index19_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA19_15 = input.LA(1);


                        int index19_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA19_16 = input.LA(1);


                        int index19_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA19_17 = input.LA(1);


                        int index19_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA19_18 = input.LA(1);


                        int index19_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA19_19 = input.LA(1);


                        int index19_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA19_20 = input.LA(1);


                        int index19_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA19_21 = input.LA(1);


                        int index19_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA19_22 = input.LA(1);


                        int index19_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA19_23 = input.LA(1);


                        int index19_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA19_24 = input.LA(1);


                        int index19_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA19_25 = input.LA(1);


                        int index19_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 :
                        int LA19_26 = input.LA(1);


                        int index19_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}


                        input.seek(index19_26);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 19, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA41_eotS =
        "\54\uffff";
    static final String DFA41_eofS =
        "\54\uffff";
    static final String DFA41_minS =
        "\1\4\1\uffff\17\0\3\uffff\1\0\27\uffff";
    static final String DFA41_maxS =
        "\1\117\1\uffff\17\0\3\uffff\1\0\27\uffff";
    static final String DFA41_acceptS =
        "\1\uffff\1\1\32\uffff\1\2\7\uffff\1\3\1\4\2\uffff\1\5\3\uffff";
    static final String DFA41_specialS =
        "\2\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\3\uffff\1\17\27\uffff}>";
    static final String[] DFA41_transitionS = {
            "\1\24\4\34\4\uffff\2\34\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\7"+
            "\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\3\34\22\uffff"+
            "\1\34\1\1\3\uffff\13\1\2\uffff\1\44\1\uffff\3\45\4\50",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
    static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
    static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
    static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
    static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
    static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
    static final short[][] DFA41_transition;

    static {
        int numStates = DFA41_transitionS.length;
        DFA41_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
        }
    }

    class DFA41 extends DFA {

        public DFA41(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 41;
            this.eot = DFA41_eot;
            this.eof = DFA41_eof;
            this.min = DFA41_min;
            this.max = DFA41_max;
            this.accept = DFA41_accept;
            this.special = DFA41_special;
            this.transition = DFA41_transition;
        }
        public String getDescription() {
            return "387:1: simple_statement options {backtrack=true; } : ( declaration_statement | expression_statement | selection_statement | iteration_statement | jump_statement );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
            int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA41_2 = input.LA(1);


                        int index41_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA41_3 = input.LA(1);


                        int index41_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA41_4 = input.LA(1);


                        int index41_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_4);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA41_5 = input.LA(1);


                        int index41_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA41_6 = input.LA(1);


                        int index41_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA41_7 = input.LA(1);


                        int index41_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA41_8 = input.LA(1);


                        int index41_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA41_9 = input.LA(1);


                        int index41_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA41_10 = input.LA(1);


                        int index41_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA41_11 = input.LA(1);


                        int index41_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA41_12 = input.LA(1);


                        int index41_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_12);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA41_13 = input.LA(1);


                        int index41_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_13);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA41_14 = input.LA(1);


                        int index41_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA41_15 = input.LA(1);


                        int index41_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_15);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA41_16 = input.LA(1);


                        int index41_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_16);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA41_20 = input.LA(1);


                        int index41_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_GlslEs()) ) {s = 1;}

                        else if ( (synpred9_GlslEs()) ) {s = 28;}


                        input.seek(index41_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 41, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA48_eotS =
        "\23\uffff";
    static final String DFA48_eofS =
        "\23\uffff";
    static final String DFA48_minS =
        "\1\4\1\uffff\2\4\1\uffff\16\4";
    static final String DFA48_maxS =
        "\1\104\1\uffff\1\65\1\10\1\uffff\16\10";
    static final String DFA48_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\16\uffff";
    static final String DFA48_specialS =
        "\23\uffff}>";
    static final String[] DFA48_transitionS = {
            "\1\2\4\1\4\uffff\2\1\1\4\1\uffff\1\3\1\5\1\6\1\7\1\10\1\11\1"+
            "\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\3\1\27\uffff\13"+
            "\4",
            "",
            "\1\4\3\uffff\3\1\1\uffff\3\1\1\uffff\1\1\17\uffff\2\1\1\uffff"+
            "\14\1\1\uffff\6\1",
            "\1\4\3\uffff\1\1",
            "",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1",
            "\1\4\3\uffff\1\1"
    };

    static final short[] DFA48_eot = DFA.unpackEncodedString(DFA48_eotS);
    static final short[] DFA48_eof = DFA.unpackEncodedString(DFA48_eofS);
    static final char[] DFA48_min = DFA.unpackEncodedStringToUnsignedChars(DFA48_minS);
    static final char[] DFA48_max = DFA.unpackEncodedStringToUnsignedChars(DFA48_maxS);
    static final short[] DFA48_accept = DFA.unpackEncodedString(DFA48_acceptS);
    static final short[] DFA48_special = DFA.unpackEncodedString(DFA48_specialS);
    static final short[][] DFA48_transition;

    static {
        int numStates = DFA48_transitionS.length;
        DFA48_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA48_transition[i] = DFA.unpackEncodedString(DFA48_transitionS[i]);
        }
    }

    class DFA48 extends DFA {

        public DFA48(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 48;
            this.eot = DFA48_eot;
            this.eof = DFA48_eof;
            this.min = DFA48_min;
            this.max = DFA48_max;
            this.accept = DFA48_accept;
            this.special = DFA48_special;
            this.transition = DFA48_transition;
        }
        public String getDescription() {
            return "423:1: condition : ( expression | fully_specified_type IDENTIFIER EQUAL initializer );";
        }
    }
    static final String DFA50_eotS =
        "\44\uffff";
    static final String DFA50_eofS =
        "\44\uffff";
    static final String DFA50_minS =
        "\1\4\6\uffff\1\0\1\uffff\1\0\2\uffff\16\0\12\uffff";
    static final String DFA50_maxS =
        "\1\104\6\uffff\1\0\1\uffff\1\0\2\uffff\16\0\12\uffff";
    static final String DFA50_acceptS =
        "\1\uffff\1\1\11\uffff\1\2\30\uffff";
    static final String DFA50_specialS =
        "\7\uffff\1\0\1\uffff\1\1\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1"+
        "\11\1\12\1\13\1\14\1\15\1\16\1\17\12\uffff}>";
    static final String[] DFA50_transitionS = {
            "\1\7\4\1\4\uffff\2\1\1\13\1\uffff\1\11\1\14\1\15\1\16\1\17\1"+
            "\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\3\1\22\uffff"+
            "\1\1\1\13\3\uffff\13\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA50_eot = DFA.unpackEncodedString(DFA50_eotS);
    static final short[] DFA50_eof = DFA.unpackEncodedString(DFA50_eofS);
    static final char[] DFA50_min = DFA.unpackEncodedStringToUnsignedChars(DFA50_minS);
    static final char[] DFA50_max = DFA.unpackEncodedStringToUnsignedChars(DFA50_maxS);
    static final short[] DFA50_accept = DFA.unpackEncodedString(DFA50_acceptS);
    static final short[] DFA50_special = DFA.unpackEncodedString(DFA50_specialS);
    static final short[][] DFA50_transition;

    static {
        int numStates = DFA50_transitionS.length;
        DFA50_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA50_transition[i] = DFA.unpackEncodedString(DFA50_transitionS[i]);
        }
    }

    class DFA50 extends DFA {

        public DFA50(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 50;
            this.eot = DFA50_eot;
            this.eof = DFA50_eof;
            this.min = DFA50_min;
            this.max = DFA50_max;
            this.accept = DFA50_accept;
            this.special = DFA50_special;
            this.transition = DFA50_transition;
        }
        public String getDescription() {
            return "434:1: for_init_statement options {backtrack=true; } : ( expression_statement | declaration_statement );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
            int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA50_7 = input.LA(1);


                        int index50_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA50_9 = input.LA(1);


                        int index50_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA50_12 = input.LA(1);


                        int index50_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_12);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA50_13 = input.LA(1);


                        int index50_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_13);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA50_14 = input.LA(1);


                        int index50_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_14);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA50_15 = input.LA(1);


                        int index50_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_15);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA50_16 = input.LA(1);


                        int index50_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_16);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA50_17 = input.LA(1);


                        int index50_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_17);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA50_18 = input.LA(1);


                        int index50_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_18);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA50_19 = input.LA(1);


                        int index50_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_19);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA50_20 = input.LA(1);


                        int index50_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_20);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA50_21 = input.LA(1);


                        int index50_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_21);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA50_22 = input.LA(1);


                        int index50_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_22);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA50_23 = input.LA(1);


                        int index50_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_23);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA50_24 = input.LA(1);


                        int index50_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_24);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA50_25 = input.LA(1);


                        int index50_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_GlslEs()) ) {s = 1;}

                        else if ( (true) ) {s = 11;}


                        input.seek(index50_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 50, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA55_eotS =
        "\35\uffff";
    static final String DFA55_eofS =
        "\35\uffff";
    static final String DFA55_minS =
        "\1\4\32\0\2\uffff";
    static final String DFA55_maxS =
        "\1\104\32\0\2\uffff";
    static final String DFA55_acceptS =
        "\33\uffff\1\2\1\1";
    static final String DFA55_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\2\uffff}>";
    static final String[] DFA55_transitionS = {
            "\1\24\12\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11"+
            "\1\12\1\13\1\14\1\15\1\16\1\17\1\20\26\uffff\1\33\3\uffff\1"+
            "\31\1\26\1\27\1\30\1\32\1\21\1\22\3\25\1\23",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA55_eot = DFA.unpackEncodedString(DFA55_eotS);
    static final short[] DFA55_eof = DFA.unpackEncodedString(DFA55_eofS);
    static final char[] DFA55_min = DFA.unpackEncodedStringToUnsignedChars(DFA55_minS);
    static final char[] DFA55_max = DFA.unpackEncodedStringToUnsignedChars(DFA55_maxS);
    static final short[] DFA55_accept = DFA.unpackEncodedString(DFA55_acceptS);
    static final short[] DFA55_special = DFA.unpackEncodedString(DFA55_specialS);
    static final short[][] DFA55_transition;

    static {
        int numStates = DFA55_transitionS.length;
        DFA55_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA55_transition[i] = DFA.unpackEncodedString(DFA55_transitionS[i]);
        }
    }

    class DFA55 extends DFA {

        public DFA55(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 55;
            this.eot = DFA55_eot;
            this.eof = DFA55_eof;
            this.min = DFA55_min;
            this.max = DFA55_max;
            this.accept = DFA55_accept;
            this.special = DFA55_special;
            this.transition = DFA55_transition;
        }
        public String getDescription() {
            return "451:1: external_declaration : ( ( function_header )=> function_definition | declaration );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
            int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA55_1 = input.LA(1);


                        int index55_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA55_2 = input.LA(1);


                        int index55_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA55_3 = input.LA(1);


                        int index55_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA55_4 = input.LA(1);


                        int index55_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA55_5 = input.LA(1);


                        int index55_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA55_6 = input.LA(1);


                        int index55_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA55_7 = input.LA(1);


                        int index55_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA55_8 = input.LA(1);


                        int index55_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA55_9 = input.LA(1);


                        int index55_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA55_10 = input.LA(1);


                        int index55_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA55_11 = input.LA(1);


                        int index55_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA55_12 = input.LA(1);


                        int index55_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA55_13 = input.LA(1);


                        int index55_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA55_14 = input.LA(1);


                        int index55_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA55_15 = input.LA(1);


                        int index55_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA55_16 = input.LA(1);


                        int index55_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA55_17 = input.LA(1);


                        int index55_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA55_18 = input.LA(1);


                        int index55_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA55_19 = input.LA(1);


                        int index55_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA55_20 = input.LA(1);


                        int index55_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA55_21 = input.LA(1);


                        int index55_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA55_22 = input.LA(1);


                        int index55_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA55_23 = input.LA(1);


                        int index55_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA55_24 = input.LA(1);


                        int index55_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA55_25 = input.LA(1);


                        int index55_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 :
                        int LA55_26 = input.LA(1);


                        int index55_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_GlslEs()) ) {s = 28;}

                        else if ( (true) ) {s = 27;}


                        input.seek(index55_26);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 55, _s, input);
            error(nvae);
            throw nvae;
        }
    }


    public static final BitSet FOLLOW_external_declaration_in_translation_unit83 = new BitSet(new long[]{0xFC400000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_EOF_in_translation_unit88 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_variable_identifier101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTCONSTANT_in_primary_expression114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_primary_expression120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLCONSTANT_in_primary_expression126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_identifier_in_primary_expression132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_primary_expression138 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_primary_expression140 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_primary_expression142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_expression_or_function_call_in_postfix_expression155 = new BitSet(new long[]{0x0000000000007402L});
    public static final BitSet FOLLOW_LEFT_BRACKET_in_postfix_expression163 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_integer_expression_in_postfix_expression165 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RIGHT_BRACKET_in_postfix_expression167 = new BitSet(new long[]{0x0000000000007402L});
    public static final BitSet FOLLOW_DOT_in_postfix_expression177 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_field_selection_in_postfix_expression179 = new BitSet(new long[]{0x0000000000007402L});
    public static final BitSet FOLLOW_INC_OP_in_postfix_expression189 = new BitSet(new long[]{0x0000000000007402L});
    public static final BitSet FOLLOW_DEC_OP_in_postfix_expression199 = new BitSet(new long[]{0x0000000000007402L});
    public static final BitSet FOLLOW_primary_expression_in_primary_expression_or_function_call227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_expression_in_primary_expression_or_function_call241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_expression_in_primary_expression_or_function_call255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_expression_in_primary_expression_or_function_call269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_call_in_primary_expression_or_function_call283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_expression_in_primary_expression_or_function_call289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_integer_expression302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_call_generic_in_function_call315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_call_header_in_function_call_generic328 = new BitSet(new long[]{0x00000007FFFEE3F0L});
    public static final BitSet FOLLOW_VOID_in_function_call_generic345 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_assignment_expression_in_function_call_generic357 = new BitSet(new long[]{0x0000000000010200L});
    public static final BitSet FOLLOW_COMMA_in_function_call_generic360 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_assignment_expression_in_function_call_generic362 = new BitSet(new long[]{0x0000000000010200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function_call_generic376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_identifier_in_function_call_header389 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function_call_header391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constructor_identifier_in_function_identifier406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_constructor_identifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INC_OP_in_unary_expression532 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_DEC_OP_in_unary_expression536 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_unary_operator_in_unary_expression540 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_postfix_expression_in_unary_expression544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unary_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unary_expression_in_multiplicative_expression587 = new BitSet(new long[]{0x0000001800000002L});
    public static final BitSet FOLLOW_set_in_multiplicative_expression590 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_unary_expression_in_multiplicative_expression598 = new BitSet(new long[]{0x0000001800000002L});
    public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression614 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_set_in_additive_expression617 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression625 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_additive_expression_in_shift_expression640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_shift_expression_in_relational_expression655 = new BitSet(new long[]{0x000001E000000002L});
    public static final BitSet FOLLOW_set_in_relational_expression658 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_shift_expression_in_relational_expression674 = new BitSet(new long[]{0x000001E000000002L});
    public static final BitSet FOLLOW_relational_expression_in_equality_expression689 = new BitSet(new long[]{0x0000060000000002L});
    public static final BitSet FOLLOW_set_in_equality_expression692 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_relational_expression_in_equality_expression700 = new BitSet(new long[]{0x0000060000000002L});
    public static final BitSet FOLLOW_equality_expression_in_and_expression715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression757 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_AND_OP_in_logical_and_expression760 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression762 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_logical_and_expression_in_logical_xor_expression777 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_XOR_OP_in_logical_xor_expression780 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_logical_and_expression_in_logical_xor_expression782 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_logical_xor_expression_in_logical_or_expression797 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_OR_OP_in_logical_or_expression800 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_logical_xor_expression_in_logical_or_expression802 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_logical_or_expression_in_conditional_expression817 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_QUESTION_in_conditional_expression820 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_conditional_expression822 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COLON_in_conditional_expression824 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_assignment_expression_in_conditional_expression826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unary_expression_in_assignment_expression849 = new BitSet(new long[]{0x001F000000000000L});
    public static final BitSet FOLLOW_assignment_operator_in_assignment_expression851 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_assignment_expression_in_assignment_expression853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditional_expression_in_assignment_expression859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_assignment_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_expression_in_expression915 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_COMMA_in_expression918 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_assignment_expression_in_expression920 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_conditional_expression_in_constant_expression935 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_prototype_in_declaration954 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_declaration956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_init_declarator_list_in_declaration962 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_declaration964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRECISION_in_declaration970 = new BitSet(new long[]{0x80000000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_precision_qualifier_in_declaration972 = new BitSet(new long[]{0x80000000FFFE8010L,0x0000000000000011L});
    public static final BitSet FOLLOW_type_specifier_no_prec_in_declaration974 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_declaration976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_declarator_in_function_prototype989 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function_prototype991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_header_in_function_declarator1004 = new BitSet(new long[]{0xFF800000FFFE8012L,0x000000000000001FL});
    public static final BitSet FOLLOW_parameter_declaration_in_function_declarator1007 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_COMMA_in_function_declarator1010 = new BitSet(new long[]{0xFF800000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_parameter_declaration_in_function_declarator1012 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_fully_specified_type_in_function_header1030 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_IDENTIFIER_in_function_header1032 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function_header1034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_qualifier_in_parameter_declaration1048 = new BitSet(new long[]{0x83800000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_parameter_qualifier_in_parameter_declaration1053 = new BitSet(new long[]{0x80000000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_type_specifier_in_parameter_declaration1063 = new BitSet(new long[]{0x0000000000000412L});
    public static final BitSet FOLLOW_IDENTIFIER_in_parameter_declaration1079 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_LEFT_BRACKET_in_parameter_declaration1097 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_constant_expression_in_parameter_declaration1099 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RIGHT_BRACKET_in_parameter_declaration1101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_parameter_qualifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_single_declaration_in_init_declarator_list1149 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_init_declarator_list_1_in_init_declarator_list1152 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_COMMA_in_init_declarator_list_11167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_IDENTIFIER_in_init_declarator_list_11169 = new BitSet(new long[]{0x0001000000000402L});
    public static final BitSet FOLLOW_init_declarator_list_2_in_init_declarator_list_11172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_BRACKET_in_init_declarator_list_21187 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_constant_expression_in_init_declarator_list_21189 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RIGHT_BRACKET_in_init_declarator_list_21191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUAL_in_init_declarator_list_21197 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_initializer_in_init_declarator_list_21199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fully_specified_type_in_single_declaration1212 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENTIFIER_in_single_declaration1220 = new BitSet(new long[]{0x0001000000000402L});
    public static final BitSet FOLLOW_LEFT_BRACKET_in_single_declaration1232 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_constant_expression_in_single_declaration1234 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RIGHT_BRACKET_in_single_declaration1236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUAL_in_single_declaration1248 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_initializer_in_single_declaration1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INVARIANT_in_single_declaration1274 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_IDENTIFIER_in_single_declaration1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_specifier_in_fully_specified_type1294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_qualifier_in_fully_specified_type1300 = new BitSet(new long[]{0x80000000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_type_specifier_in_fully_specified_type1302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONST_in_type_qualifier1315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATTRIBUTE_in_type_qualifier1321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARYING_in_type_qualifier1330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INVARIANT_in_type_qualifier1336 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_VARYING_in_type_qualifier1338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNIFORM_in_type_qualifier1344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_specifier_no_prec_in_type_specifier1357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_precision_qualifier_in_type_specifier1363 = new BitSet(new long[]{0x80000000FFFE8010L,0x0000000000000011L});
    public static final BitSet FOLLOW_type_specifier_no_prec_in_type_specifier1365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VOID_in_type_specifier_no_prec1378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_type_specifier_no_prec1384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_type_specifier_no_prec1390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_type_specifier_no_prec1396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VEC2_in_type_specifier_no_prec1402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VEC3_in_type_specifier_no_prec1408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VEC4_in_type_specifier_no_prec1414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BVEC2_in_type_specifier_no_prec1420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BVEC3_in_type_specifier_no_prec1426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BVEC4_in_type_specifier_no_prec1432 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IVEC2_in_type_specifier_no_prec1438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IVEC3_in_type_specifier_no_prec1444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IVEC4_in_type_specifier_no_prec1450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAT2_in_type_specifier_no_prec1456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAT3_in_type_specifier_no_prec1462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAT4_in_type_specifier_no_prec1468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SAMPLER2D_in_type_specifier_no_prec1474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SAMPLERCUBE_in_type_specifier_no_prec1480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_specifier_in_type_specifier_no_prec1486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_type_specifier_no_prec1493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_precision_qualifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUCT_in_struct_specifier1531 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000020L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_specifier1534 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_LEFT_BRACE_in_struct_specifier1538 = new BitSet(new long[]{0x80000000FFFE8010L,0x000000000000001FL});
    public static final BitSet FOLLOW_struct_declaration_list_in_struct_specifier1540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RIGHT_BRACE_in_struct_specifier1542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_declaration_in_struct_declaration_list1556 = new BitSet(new long[]{0x80000000FFFE8012L,0x000000000000001FL});
    public static final BitSet FOLLOW_type_specifier_in_struct_declaration1571 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_struct_declarator_list_in_struct_declaration1573 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_struct_declaration1575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_declarator_in_struct_declarator_list1588 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_COMMA_in_struct_declarator_list1591 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_struct_declarator_in_struct_declarator_list1593 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_declarator1608 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_LEFT_BRACKET_in_struct_declarator1611 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_constant_expression_in_struct_declarator1613 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RIGHT_BRACKET_in_struct_declarator1615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_expression_in_initializer1630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declaration_statement1643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_statement_with_scope_in_statement_no_new_scope1656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_statement_in_statement_no_new_scope1662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_statement_in_simple_statement1684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_statement_in_simple_statement1690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selection_statement_in_simple_statement1696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iteration_statement_in_simple_statement1702 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_jump_statement_in_simple_statement1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_BRACE_in_compound_statement_with_scope1721 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEFFL});
    public static final BitSet FOLLOW_statement_list_in_compound_statement_with_scope1724 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RIGHT_BRACE_in_compound_statement_with_scope1728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_statement_no_new_scope_in_statement_with_scope1741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_statement_in_statement_with_scope1747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_BRACE_in_compound_statement_no_new_scope1760 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEFFL});
    public static final BitSet FOLLOW_statement_list_in_compound_statement_no_new_scope1763 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RIGHT_BRACE_in_compound_statement_no_new_scope1767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_no_new_scope_in_statement_list1781 = new BitSet(new long[]{0xFC600007FFFEE1F2L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_expression_in_expression_statement1797 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_expression_statement1801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_selection_statement1823 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_selection_statement1825 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_selection_statement1827 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_selection_statement1829 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_with_scope_in_selection_statement1831 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_ELSE_in_selection_statement1833 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_with_scope_in_selection_statement1835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_selection_statement1841 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_selection_statement1843 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_selection_statement1845 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_selection_statement1847 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_with_scope_in_selection_statement1849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_condition1862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fully_specified_type_in_condition1868 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_IDENTIFIER_in_condition1870 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_EQUAL_in_condition1872 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_initializer_in_condition1874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_iteration_statement1887 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_iteration_statement1889 = new BitSet(new long[]{0xFC000007FFFEE1F0L,0x000000000000001FL});
    public static final BitSet FOLLOW_condition_in_iteration_statement1891 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_iteration_statement1893 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_no_new_scope_in_iteration_statement1895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_iteration_statement1901 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_with_scope_in_iteration_statement1903 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_WHILE_in_iteration_statement1905 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_iteration_statement1907 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_iteration_statement1909 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_iteration_statement1911 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_iteration_statement1913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_iteration_statement1919 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_iteration_statement1921 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000001FL});
    public static final BitSet FOLLOW_for_init_statement_in_iteration_statement1923 = new BitSet(new long[]{0xFC200007FFFEE1F0L,0x000000000000001FL});
    public static final BitSet FOLLOW_for_rest_statement_in_iteration_statement1925 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_iteration_statement1927 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_no_new_scope_in_iteration_statement1929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_statement_in_for_init_statement1951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_statement_in_for_init_statement1957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condition_in_for_rest_statement1971 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_for_rest_statement1975 = new BitSet(new long[]{0x00000007FFFE61F2L});
    public static final BitSet FOLLOW_expression_in_for_rest_statement1978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTINUE_in_jump_statement1993 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_jump_statement1995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_jump_statement2001 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_jump_statement2003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_jump_statement2009 = new BitSet(new long[]{0x00200007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_jump_statement2012 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_jump_statement2016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISCARD_in_jump_statement2022 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_jump_statement2024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_definition_in_external_declaration2046 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_external_declaration2052 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_prototype_in_function_definition2065 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_compound_statement_no_new_scope_in_function_definition2067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_field_selection3075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTCONSTANT_in_synpred1_GlslEs221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_synpred2_GlslEs235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLCONSTANT_in_synpred3_GlslEs249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_synpred4_GlslEs263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_call_header_in_synpred5_GlslEs277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unary_expression_in_synpred6_GlslEs842 = new BitSet(new long[]{0x001F000000000000L});
    public static final BitSet FOLLOW_assignment_operator_in_synpred6_GlslEs844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_header_in_synpred7_GlslEs949 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_statement_in_synpred8_GlslEs1684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_statement_in_synpred9_GlslEs1690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_synpred12_GlslEs1823 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_synpred12_GlslEs1825 = new BitSet(new long[]{0x00000007FFFE61F0L});
    public static final BitSet FOLLOW_expression_in_synpred12_GlslEs1827 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_synpred12_GlslEs1829 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_with_scope_in_synpred12_GlslEs1831 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_ELSE_in_synpred12_GlslEs1833 = new BitSet(new long[]{0xFC600007FFFEE1F0L,0x000000000000FEBFL});
    public static final BitSet FOLLOW_statement_with_scope_in_synpred12_GlslEs1835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_statement_in_synpred13_GlslEs1951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_header_in_synpred14_GlslEs2041 = new BitSet(new long[]{0x0000000000000002L});

}
