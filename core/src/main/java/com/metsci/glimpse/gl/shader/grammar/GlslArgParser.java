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
// $ANTLR 3.5 ../core/src/main/resources/shader/antlr/GlslArg.g 2013-05-29 11:26:33

package com.metsci.glimpse.gl.shader.grammar;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.BitSet;
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

import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderArgInOut;
import com.metsci.glimpse.gl.shader.ShaderArgQualifier;
import com.metsci.glimpse.gl.shader.ShaderArgType;

@SuppressWarnings( "all" )
public class GlslArgParser extends Parser
{
    public static final String[] tokenNames = new String[] { "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ATTRIBUTE", "BOOL", "BREAK", "BVEC2", "BVEC3", "BVEC4", "COMMENT", "CONST", "CONTINUE", "DIRECTIVE", "DISCARD", "DO", "ELSE", "FALSE", "FLOAT", "FOR", "HIGH_PRECISION", "IDENTIFIER", "IF", "IN", "INOUT", "INT", "INVARIANT", "ISAMPLER1D", "ISAMPLER2D", "IVEC2", "IVEC3", "IVEC4", "LCURLY", "LOW_PRECISION", "LPAREN", "MAT2", "MAT3", "MAT4", "MEDIUM_PRECISION", "MULTILINE_COMMENT", "OUT", "PRECISION", "RCURLY", "RETURN", "RPAREN", "SAMPLER1D", "SAMPLER1DARRAY", "SAMPLER2D", "SAMPLER2DARRAY", "SAMPLERCUBE", "SEMI", "STRUCT", "TRUE", "UNIFORM", "USAMPLER1D", "USAMPLER2D", "VARYING", "VEC2", "VEC3", "VEC4", "VOID", "WHILE", "WHITESPACE", "'main'" };
    public static final int EOF = -1;
    public static final int T__63 = 63;
    public static final int ATTRIBUTE = 4;
    public static final int BOOL = 5;
    public static final int BREAK = 6;
    public static final int BVEC2 = 7;
    public static final int BVEC3 = 8;
    public static final int BVEC4 = 9;
    public static final int COMMENT = 10;
    public static final int CONST = 11;
    public static final int CONTINUE = 12;
    public static final int DIRECTIVE = 13;
    public static final int DISCARD = 14;
    public static final int DO = 15;
    public static final int ELSE = 16;
    public static final int FALSE = 17;
    public static final int FLOAT = 18;
    public static final int FOR = 19;
    public static final int HIGH_PRECISION = 20;
    public static final int IDENTIFIER = 21;
    public static final int IF = 22;
    public static final int IN = 23;
    public static final int INOUT = 24;
    public static final int INT = 25;
    public static final int INVARIANT = 26;
    public static final int ISAMPLER1D = 27;
    public static final int ISAMPLER2D = 28;
    public static final int IVEC2 = 29;
    public static final int IVEC3 = 30;
    public static final int IVEC4 = 31;
    public static final int LCURLY = 32;
    public static final int LOW_PRECISION = 33;
    public static final int LPAREN = 34;
    public static final int MAT2 = 35;
    public static final int MAT3 = 36;
    public static final int MAT4 = 37;
    public static final int MEDIUM_PRECISION = 38;
    public static final int MULTILINE_COMMENT = 39;
    public static final int OUT = 40;
    public static final int PRECISION = 41;
    public static final int RCURLY = 42;
    public static final int RETURN = 43;
    public static final int RPAREN = 44;
    public static final int SAMPLER1D = 45;
    public static final int SAMPLER1DARRAY = 46;
    public static final int SAMPLER2D = 47;
    public static final int SAMPLER2DARRAY = 48;
    public static final int SAMPLERCUBE = 49;
    public static final int SEMI = 50;
    public static final int STRUCT = 51;
    public static final int TRUE = 52;
    public static final int UNIFORM = 53;
    public static final int USAMPLER1D = 54;
    public static final int USAMPLER2D = 55;
    public static final int VARYING = 56;
    public static final int VEC2 = 57;
    public static final int VEC3 = 58;
    public static final int VEC4 = 59;
    public static final int VOID = 60;
    public static final int WHILE = 61;
    public static final int WHITESPACE = 62;

    // delegates
    public Parser[] getDelegates( )
    {
        return new Parser[] {};
    }

    // delegators

    public GlslArgParser( TokenStream input )
    {
        this( input, new RecognizerSharedState( ) );
    }

    public GlslArgParser( TokenStream input, RecognizerSharedState state )
    {
        super( input, state );
    }

    protected TreeAdaptor adaptor = new CommonTreeAdaptor( );

    public void setTreeAdaptor( TreeAdaptor adaptor )
    {
        this.adaptor = adaptor;
    }

    public TreeAdaptor getTreeAdaptor( )
    {
        return adaptor;
    }

    @Override
    public String[] getTokenNames( )
    {
        return GlslArgParser.tokenNames;
    }

    @Override
    public String getGrammarFileName( )
    {
        return "../core/src/main/resources/shader/antlr/GlslArg.g";
    }

    public static class shader_return extends ParserRuleReturnScope
    {
        public List<ShaderArg> result;
        CommonTree tree;

        @Override
        public CommonTree getTree( )
        {
            return tree;
        }
    };

    // $ANTLR start "shader"
    // ../core/src/main/resources/shader/antlr/GlslArg.g:28:1: shader returns [List<ShaderArg> result] : ( parameter )* VOID ! 'main' ! LPAREN ! ( ( VOID )? ) ! RPAREN ! LCURLY !;
    public final GlslArgParser.shader_return shader( ) throws RecognitionException
    {
        GlslArgParser.shader_return retval = new GlslArgParser.shader_return( );
        retval.start = input.LT( 1 );

        CommonTree root_0 = null;

        Token VOID2 = null;
        Token string_literal3 = null;
        Token LPAREN4 = null;
        Token VOID5 = null;
        Token RPAREN6 = null;
        Token LCURLY7 = null;
        ParserRuleReturnScope parameter1 = null;

        CommonTree VOID2_tree = null;
        CommonTree string_literal3_tree = null;
        CommonTree LPAREN4_tree = null;
        CommonTree VOID5_tree = null;
        CommonTree RPAREN6_tree = null;
        CommonTree LCURLY7_tree = null;

        List<ShaderArg> result = new ArrayList<ShaderArg>( );

        try
        {
            // ../core/src/main/resources/shader/antlr/GlslArg.g:33:3: ( ( parameter )* VOID ! 'main' ! LPAREN ! ( ( VOID )? ) ! RPAREN ! LCURLY !)
            // ../core/src/main/resources/shader/antlr/GlslArg.g:33:5: ( parameter )* VOID ! 'main' ! LPAREN ! ( ( VOID )? ) ! RPAREN ! LCURLY !
            {
                root_0 = ( CommonTree ) adaptor.nil( );

                // ../core/src/main/resources/shader/antlr/GlslArg.g:33:5: ( parameter )*
                loop1: while ( true )
                {
                    int alt1 = 2;
                    int LA1_0 = input.LA( 1 );
                    if ( ( LA1_0 == VOID ) )
                    {
                        int LA1_1 = input.LA( 2 );
                        if ( ( LA1_1 == IDENTIFIER ) )
                        {
                            alt1 = 1;
                        }

                    }
                    else if ( ( ( LA1_0 >= ATTRIBUTE && LA1_0 <= BOOL ) || ( LA1_0 >= BVEC2 && LA1_0 <= BVEC4 ) || LA1_0 == CONST || LA1_0 == FLOAT || ( LA1_0 >= IN && LA1_0 <= IVEC4 ) || ( LA1_0 >= MAT2 && LA1_0 <= MAT4 ) || LA1_0 == OUT || ( LA1_0 >= SAMPLER1D && LA1_0 <= SAMPLERCUBE ) || ( LA1_0 >= UNIFORM && LA1_0 <= VEC4 ) ) )
                    {
                        alt1 = 1;
                    }

                    switch ( alt1 )
                    {
                        case 1:
                        // ../core/src/main/resources/shader/antlr/GlslArg.g:33:7: parameter
                        {
                            pushFollow( FOLLOW_parameter_in_shader71 );
                            parameter1 = parameter( );
                            state._fsp--;

                            adaptor.addChild( root_0, parameter1.getTree( ) );

                            result.add( ( parameter1 != null ? ( ( GlslArgParser.parameter_return ) parameter1 ).result : null ) );
                        }
                            break;

                        default:
                            break loop1;
                    }
                }

                VOID2 = ( Token ) match( input, VOID, FOLLOW_VOID_in_shader78 );
                string_literal3 = ( Token ) match( input, 63, FOLLOW_63_in_shader81 );
                LPAREN4 = ( Token ) match( input, LPAREN, FOLLOW_LPAREN_in_shader84 );
                // ../core/src/main/resources/shader/antlr/GlslArg.g:33:79: ( ( VOID )? )
                // ../core/src/main/resources/shader/antlr/GlslArg.g:33:80: ( VOID )?
                {
                    // ../core/src/main/resources/shader/antlr/GlslArg.g:33:80: ( VOID )?
                    int alt2 = 2;
                    int LA2_0 = input.LA( 1 );
                    if ( ( LA2_0 == VOID ) )
                    {
                        alt2 = 1;
                    }
                    switch ( alt2 )
                    {
                        case 1:
                        // ../core/src/main/resources/shader/antlr/GlslArg.g:33:80: VOID
                        {
                            VOID5 = ( Token ) match( input, VOID, FOLLOW_VOID_in_shader88 );
                            VOID5_tree = ( CommonTree ) adaptor.create( VOID5 );
                            adaptor.addChild( root_0, VOID5_tree );

                        }
                            break;

                    }

                }

                RPAREN6 = ( Token ) match( input, RPAREN, FOLLOW_RPAREN_in_shader93 );
                LCURLY7 = ( Token ) match( input, LCURLY, FOLLOW_LCURLY_in_shader96 );

                retval.result = result;

            }

            retval.stop = input.LT( -1 );

            retval.tree = ( CommonTree ) adaptor.rulePostProcessing( root_0 );
            adaptor.setTokenBoundaries( retval.tree, retval.start, retval.stop );

        }
        catch ( RecognitionException re )
        {
            reportError( re );
            recover( input, re );
            retval.tree = ( CommonTree ) adaptor.errorNode( input, retval.start, input.LT( -1 ), re );
        }
        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "shader"

    public static class parameter_return extends ParserRuleReturnScope
    {
        public ShaderArg result;
        CommonTree tree;

        @Override
        public CommonTree getTree( )
        {
            return tree;
        }
    };

    // $ANTLR start "parameter"
    // ../core/src/main/resources/shader/antlr/GlslArg.g:39:1: parameter returns [ShaderArg result] : (pqual= qualifier )? (pinout= inout )? ptype= type pname= identifier SEMI !;
    public final GlslArgParser.parameter_return parameter( ) throws RecognitionException
    {
        GlslArgParser.parameter_return retval = new GlslArgParser.parameter_return( );
        retval.start = input.LT( 1 );

        CommonTree root_0 = null;

        Token SEMI8 = null;
        ParserRuleReturnScope pqual = null;
        ParserRuleReturnScope pinout = null;
        ParserRuleReturnScope ptype = null;
        ParserRuleReturnScope pname = null;

        CommonTree SEMI8_tree = null;

        String name;
        ShaderArgType type;
        ShaderArgQualifier qual;
        ShaderArgInOut inout;

        try
        {
            // ../core/src/main/resources/shader/antlr/GlslArg.g:47:3: ( (pqual= qualifier )? (pinout= inout )? ptype= type pname= identifier SEMI !)
            // ../core/src/main/resources/shader/antlr/GlslArg.g:47:5: (pqual= qualifier )? (pinout= inout )? ptype= type pname= identifier SEMI !
            {
                root_0 = ( CommonTree ) adaptor.nil( );

                // ../core/src/main/resources/shader/antlr/GlslArg.g:47:10: (pqual= qualifier )?
                int alt3 = 2;
                int LA3_0 = input.LA( 1 );
                if ( ( LA3_0 == ATTRIBUTE || LA3_0 == CONST || LA3_0 == INVARIANT || LA3_0 == UNIFORM || LA3_0 == VARYING ) )
                {
                    alt3 = 1;
                }
                switch ( alt3 )
                {
                    case 1:
                    // ../core/src/main/resources/shader/antlr/GlslArg.g:47:10: pqual= qualifier
                    {
                        pushFollow( FOLLOW_qualifier_in_parameter125 );
                        pqual = qualifier( );
                        state._fsp--;

                        adaptor.addChild( root_0, pqual.getTree( ) );

                    }
                        break;

                }

                qual = ( pqual != null ? ( ( GlslArgParser.qualifier_return ) pqual ).result : null );
                // ../core/src/main/resources/shader/antlr/GlslArg.g:48:11: (pinout= inout )?
                int alt4 = 2;
                int LA4_0 = input.LA( 1 );
                if ( ( ( LA4_0 >= IN && LA4_0 <= INOUT ) || LA4_0 == OUT ) )
                {
                    alt4 = 1;
                }
                switch ( alt4 )
                {
                    case 1:
                    // ../core/src/main/resources/shader/antlr/GlslArg.g:48:11: pinout= inout
                    {
                        pushFollow( FOLLOW_inout_in_parameter136 );
                        pinout = inout( );
                        state._fsp--;

                        adaptor.addChild( root_0, pinout.getTree( ) );

                    }
                        break;

                }

                inout = ( pinout != null ? ( ( GlslArgParser.inout_return ) pinout ).result : null );
                pushFollow( FOLLOW_type_in_parameter147 );
                ptype = type( );
                state._fsp--;

                adaptor.addChild( root_0, ptype.getTree( ) );

                type = ( ptype != null ? ( ( GlslArgParser.type_return ) ptype ).result : null );
                pushFollow( FOLLOW_identifier_in_parameter157 );
                pname = identifier( );
                state._fsp--;

                adaptor.addChild( root_0, pname.getTree( ) );

                name = ( pname != null ? input.toString( pname.start, pname.stop ) : null );
                SEMI8 = ( Token ) match( input, SEMI, FOLLOW_SEMI_in_parameter165 );

                retval.result = new ShaderArg( name, type, qual, inout );

            }

            retval.stop = input.LT( -1 );

            retval.tree = ( CommonTree ) adaptor.rulePostProcessing( root_0 );
            adaptor.setTokenBoundaries( retval.tree, retval.start, retval.stop );

        }
        catch ( RecognitionException re )
        {
            reportError( re );
            recover( input, re );
            retval.tree = ( CommonTree ) adaptor.errorNode( input, retval.start, input.LT( -1 ), re );
        }
        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "parameter"

    public static class type_return extends ParserRuleReturnScope
    {
        public ShaderArgType result;
        CommonTree tree;

        @Override
        public CommonTree getTree( )
        {
            return tree;
        }
    };

    // $ANTLR start "type"
    // ../core/src/main/resources/shader/antlr/GlslArg.g:57:1: type returns [ShaderArgType result] : ( VOID | FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | SAMPLER1D | ISAMPLER1D | USAMPLER1D | SAMPLER2D | ISAMPLER2D | USAMPLER2D | SAMPLERCUBE | SAMPLER1DARRAY | SAMPLER2DARRAY );
    public final GlslArgParser.type_return type( ) throws RecognitionException
    {
        GlslArgParser.type_return retval = new GlslArgParser.type_return( );
        retval.start = input.LT( 1 );

        CommonTree root_0 = null;

        Token VOID9 = null;
        Token FLOAT10 = null;
        Token INT11 = null;
        Token BOOL12 = null;
        Token VEC213 = null;
        Token VEC314 = null;
        Token VEC415 = null;
        Token BVEC216 = null;
        Token BVEC317 = null;
        Token BVEC418 = null;
        Token IVEC219 = null;
        Token IVEC320 = null;
        Token IVEC421 = null;
        Token MAT222 = null;
        Token MAT323 = null;
        Token MAT424 = null;
        Token SAMPLER1D25 = null;
        Token ISAMPLER1D26 = null;
        Token USAMPLER1D27 = null;
        Token SAMPLER2D28 = null;
        Token ISAMPLER2D29 = null;
        Token USAMPLER2D30 = null;
        Token SAMPLERCUBE31 = null;
        Token SAMPLER1DARRAY32 = null;
        Token SAMPLER2DARRAY33 = null;

        CommonTree VOID9_tree = null;
        CommonTree FLOAT10_tree = null;
        CommonTree INT11_tree = null;
        CommonTree BOOL12_tree = null;
        CommonTree VEC213_tree = null;
        CommonTree VEC314_tree = null;
        CommonTree VEC415_tree = null;
        CommonTree BVEC216_tree = null;
        CommonTree BVEC317_tree = null;
        CommonTree BVEC418_tree = null;
        CommonTree IVEC219_tree = null;
        CommonTree IVEC320_tree = null;
        CommonTree IVEC421_tree = null;
        CommonTree MAT222_tree = null;
        CommonTree MAT323_tree = null;
        CommonTree MAT424_tree = null;
        CommonTree SAMPLER1D25_tree = null;
        CommonTree ISAMPLER1D26_tree = null;
        CommonTree USAMPLER1D27_tree = null;
        CommonTree SAMPLER2D28_tree = null;
        CommonTree ISAMPLER2D29_tree = null;
        CommonTree USAMPLER2D30_tree = null;
        CommonTree SAMPLERCUBE31_tree = null;
        CommonTree SAMPLER1DARRAY32_tree = null;
        CommonTree SAMPLER2DARRAY33_tree = null;

        try
        {
            // ../core/src/main/resources/shader/antlr/GlslArg.g:58:3: ( VOID | FLOAT | INT | BOOL | VEC2 | VEC3 | VEC4 | BVEC2 | BVEC3 | BVEC4 | IVEC2 | IVEC3 | IVEC4 | MAT2 | MAT3 | MAT4 | SAMPLER1D | ISAMPLER1D | USAMPLER1D | SAMPLER2D | ISAMPLER2D | USAMPLER2D | SAMPLERCUBE | SAMPLER1DARRAY | SAMPLER2DARRAY )
            int alt5 = 25;
            switch ( input.LA( 1 ) )
            {
                case VOID:
                {
                    alt5 = 1;
                }
                    break;
                case FLOAT:
                {
                    alt5 = 2;
                }
                    break;
                case INT:
                {
                    alt5 = 3;
                }
                    break;
                case BOOL:
                {
                    alt5 = 4;
                }
                    break;
                case VEC2:
                {
                    alt5 = 5;
                }
                    break;
                case VEC3:
                {
                    alt5 = 6;
                }
                    break;
                case VEC4:
                {
                    alt5 = 7;
                }
                    break;
                case BVEC2:
                {
                    alt5 = 8;
                }
                    break;
                case BVEC3:
                {
                    alt5 = 9;
                }
                    break;
                case BVEC4:
                {
                    alt5 = 10;
                }
                    break;
                case IVEC2:
                {
                    alt5 = 11;
                }
                    break;
                case IVEC3:
                {
                    alt5 = 12;
                }
                    break;
                case IVEC4:
                {
                    alt5 = 13;
                }
                    break;
                case MAT2:
                {
                    alt5 = 14;
                }
                    break;
                case MAT3:
                {
                    alt5 = 15;
                }
                    break;
                case MAT4:
                {
                    alt5 = 16;
                }
                    break;
                case SAMPLER1D:
                {
                    alt5 = 17;
                }
                    break;
                case ISAMPLER1D:
                {
                    alt5 = 18;
                }
                    break;
                case USAMPLER1D:
                {
                    alt5 = 19;
                }
                    break;
                case SAMPLER2D:
                {
                    alt5 = 20;
                }
                    break;
                case ISAMPLER2D:
                {
                    alt5 = 21;
                }
                    break;
                case USAMPLER2D:
                {
                    alt5 = 22;
                }
                    break;
                case SAMPLERCUBE:
                {
                    alt5 = 23;
                }
                    break;
                case SAMPLER1DARRAY:
                {
                    alt5 = 24;
                }
                    break;
                case SAMPLER2DARRAY:
                {
                    alt5 = 25;
                }
                    break;
                default:
                    NoViableAltException nvae = new NoViableAltException( "", 5, 0, input );
                    throw nvae;
            }
            switch ( alt5 )
            {
                case 1:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:58:5: VOID
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    VOID9 = ( Token ) match( input, VOID, FOLLOW_VOID_in_type187 );
                    VOID9_tree = ( CommonTree ) adaptor.create( VOID9 );
                    adaptor.addChild( root_0, VOID9_tree );

                }
                    break;
                case 2:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:59:5: FLOAT
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    FLOAT10 = ( Token ) match( input, FLOAT, FOLLOW_FLOAT_in_type193 );
                    FLOAT10_tree = ( CommonTree ) adaptor.create( FLOAT10 );
                    adaptor.addChild( root_0, FLOAT10_tree );

                    retval.result = ShaderArgType.FLOAT;
                }
                    break;
                case 3:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:60:5: INT
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    INT11 = ( Token ) match( input, INT, FOLLOW_INT_in_type208 );
                    INT11_tree = ( CommonTree ) adaptor.create( INT11 );
                    adaptor.addChild( root_0, INT11_tree );

                    retval.result = ShaderArgType.INT;
                }
                    break;
                case 4:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:61:5: BOOL
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    BOOL12 = ( Token ) match( input, BOOL, FOLLOW_BOOL_in_type225 );
                    BOOL12_tree = ( CommonTree ) adaptor.create( BOOL12 );
                    adaptor.addChild( root_0, BOOL12_tree );

                    retval.result = ShaderArgType.BOOLEAN;
                }
                    break;
                case 5:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:62:5: VEC2
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    VEC213 = ( Token ) match( input, VEC2, FOLLOW_VEC2_in_type241 );
                    VEC213_tree = ( CommonTree ) adaptor.create( VEC213 );
                    adaptor.addChild( root_0, VEC213_tree );

                    retval.result = ShaderArgType.VEC2;
                }
                    break;
                case 6:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:63:5: VEC3
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    VEC314 = ( Token ) match( input, VEC3, FOLLOW_VEC3_in_type257 );
                    VEC314_tree = ( CommonTree ) adaptor.create( VEC314 );
                    adaptor.addChild( root_0, VEC314_tree );

                    retval.result = ShaderArgType.VEC3;
                }
                    break;
                case 7:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:64:5: VEC4
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    VEC415 = ( Token ) match( input, VEC4, FOLLOW_VEC4_in_type273 );
                    VEC415_tree = ( CommonTree ) adaptor.create( VEC415 );
                    adaptor.addChild( root_0, VEC415_tree );

                    retval.result = ShaderArgType.VEC4;
                }
                    break;
                case 8:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:65:5: BVEC2
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    BVEC216 = ( Token ) match( input, BVEC2, FOLLOW_BVEC2_in_type289 );
                    BVEC216_tree = ( CommonTree ) adaptor.create( BVEC216 );
                    adaptor.addChild( root_0, BVEC216_tree );

                    retval.result = ShaderArgType.BVEC2;
                }
                    break;
                case 9:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:66:5: BVEC3
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    BVEC317 = ( Token ) match( input, BVEC3, FOLLOW_BVEC3_in_type304 );
                    BVEC317_tree = ( CommonTree ) adaptor.create( BVEC317 );
                    adaptor.addChild( root_0, BVEC317_tree );

                    retval.result = ShaderArgType.BVEC3;
                }
                    break;
                case 10:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:67:5: BVEC4
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    BVEC418 = ( Token ) match( input, BVEC4, FOLLOW_BVEC4_in_type319 );
                    BVEC418_tree = ( CommonTree ) adaptor.create( BVEC418 );
                    adaptor.addChild( root_0, BVEC418_tree );

                    retval.result = ShaderArgType.BVEC4;
                }
                    break;
                case 11:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:68:5: IVEC2
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    IVEC219 = ( Token ) match( input, IVEC2, FOLLOW_IVEC2_in_type334 );
                    IVEC219_tree = ( CommonTree ) adaptor.create( IVEC219 );
                    adaptor.addChild( root_0, IVEC219_tree );

                    retval.result = ShaderArgType.IVEC2;
                }
                    break;
                case 12:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:69:5: IVEC3
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    IVEC320 = ( Token ) match( input, IVEC3, FOLLOW_IVEC3_in_type349 );
                    IVEC320_tree = ( CommonTree ) adaptor.create( IVEC320 );
                    adaptor.addChild( root_0, IVEC320_tree );

                    retval.result = ShaderArgType.IVEC3;
                }
                    break;
                case 13:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:70:5: IVEC4
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    IVEC421 = ( Token ) match( input, IVEC4, FOLLOW_IVEC4_in_type364 );
                    IVEC421_tree = ( CommonTree ) adaptor.create( IVEC421 );
                    adaptor.addChild( root_0, IVEC421_tree );

                    retval.result = ShaderArgType.IVEC4;
                }
                    break;
                case 14:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:71:5: MAT2
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    MAT222 = ( Token ) match( input, MAT2, FOLLOW_MAT2_in_type379 );
                    MAT222_tree = ( CommonTree ) adaptor.create( MAT222 );
                    adaptor.addChild( root_0, MAT222_tree );

                    retval.result = ShaderArgType.MAT2;
                }
                    break;
                case 15:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:72:5: MAT3
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    MAT323 = ( Token ) match( input, MAT3, FOLLOW_MAT3_in_type395 );
                    MAT323_tree = ( CommonTree ) adaptor.create( MAT323 );
                    adaptor.addChild( root_0, MAT323_tree );

                    retval.result = ShaderArgType.MAT3;
                }
                    break;
                case 16:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:73:5: MAT4
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    MAT424 = ( Token ) match( input, MAT4, FOLLOW_MAT4_in_type411 );
                    MAT424_tree = ( CommonTree ) adaptor.create( MAT424 );
                    adaptor.addChild( root_0, MAT424_tree );

                    retval.result = ShaderArgType.MAT4;
                }
                    break;
                case 17:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:74:5: SAMPLER1D
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    SAMPLER1D25 = ( Token ) match( input, SAMPLER1D, FOLLOW_SAMPLER1D_in_type427 );
                    SAMPLER1D25_tree = ( CommonTree ) adaptor.create( SAMPLER1D25 );
                    adaptor.addChild( root_0, SAMPLER1D25_tree );

                    retval.result = ShaderArgType.SAMPLER_1D;
                }
                    break;
                case 18:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:75:5: ISAMPLER1D
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    ISAMPLER1D26 = ( Token ) match( input, ISAMPLER1D, FOLLOW_ISAMPLER1D_in_type438 );
                    ISAMPLER1D26_tree = ( CommonTree ) adaptor.create( ISAMPLER1D26 );
                    adaptor.addChild( root_0, ISAMPLER1D26_tree );

                    retval.result = ShaderArgType.ISAMPLER_1D;
                }
                    break;
                case 19:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:76:5: USAMPLER1D
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    USAMPLER1D27 = ( Token ) match( input, USAMPLER1D, FOLLOW_USAMPLER1D_in_type448 );
                    USAMPLER1D27_tree = ( CommonTree ) adaptor.create( USAMPLER1D27 );
                    adaptor.addChild( root_0, USAMPLER1D27_tree );

                    retval.result = ShaderArgType.USAMPLER_1D;
                }
                    break;
                case 20:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:77:5: SAMPLER2D
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    SAMPLER2D28 = ( Token ) match( input, SAMPLER2D, FOLLOW_SAMPLER2D_in_type458 );
                    SAMPLER2D28_tree = ( CommonTree ) adaptor.create( SAMPLER2D28 );
                    adaptor.addChild( root_0, SAMPLER2D28_tree );

                    retval.result = ShaderArgType.SAMPLER_2D;
                }
                    break;
                case 21:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:78:5: ISAMPLER2D
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    ISAMPLER2D29 = ( Token ) match( input, ISAMPLER2D, FOLLOW_ISAMPLER2D_in_type469 );
                    ISAMPLER2D29_tree = ( CommonTree ) adaptor.create( ISAMPLER2D29 );
                    adaptor.addChild( root_0, ISAMPLER2D29_tree );

                    retval.result = ShaderArgType.ISAMPLER_2D;
                }
                    break;
                case 22:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:79:5: USAMPLER2D
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    USAMPLER2D30 = ( Token ) match( input, USAMPLER2D, FOLLOW_USAMPLER2D_in_type479 );
                    USAMPLER2D30_tree = ( CommonTree ) adaptor.create( USAMPLER2D30 );
                    adaptor.addChild( root_0, USAMPLER2D30_tree );

                    retval.result = ShaderArgType.USAMPLER_2D;
                }
                    break;
                case 23:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:80:5: SAMPLERCUBE
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    SAMPLERCUBE31 = ( Token ) match( input, SAMPLERCUBE, FOLLOW_SAMPLERCUBE_in_type489 );
                    SAMPLERCUBE31_tree = ( CommonTree ) adaptor.create( SAMPLERCUBE31 );
                    adaptor.addChild( root_0, SAMPLERCUBE31_tree );

                    retval.result = ShaderArgType.SAMPLER_CUBE;
                }
                    break;
                case 24:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:81:5: SAMPLER1DARRAY
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    SAMPLER1DARRAY32 = ( Token ) match( input, SAMPLER1DARRAY, FOLLOW_SAMPLER1DARRAY_in_type498 );
                    SAMPLER1DARRAY32_tree = ( CommonTree ) adaptor.create( SAMPLER1DARRAY32 );
                    adaptor.addChild( root_0, SAMPLER1DARRAY32_tree );

                    retval.result = ShaderArgType.SAMPLER_1D_ARRAY;
                }
                    break;
                case 25:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:82:5: SAMPLER2DARRAY
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    SAMPLER2DARRAY33 = ( Token ) match( input, SAMPLER2DARRAY, FOLLOW_SAMPLER2DARRAY_in_type509 );
                    SAMPLER2DARRAY33_tree = ( CommonTree ) adaptor.create( SAMPLER2DARRAY33 );
                    adaptor.addChild( root_0, SAMPLER2DARRAY33_tree );

                    retval.result = ShaderArgType.SAMPLER_2D_ARRAY;
                }
                    break;

            }
            retval.stop = input.LT( -1 );

            retval.tree = ( CommonTree ) adaptor.rulePostProcessing( root_0 );
            adaptor.setTokenBoundaries( retval.tree, retval.start, retval.stop );

        }
        catch ( RecognitionException re )
        {
            reportError( re );
            recover( input, re );
            retval.tree = ( CommonTree ) adaptor.errorNode( input, retval.start, input.LT( -1 ), re );
        }
        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "type"

    public static class qualifier_return extends ParserRuleReturnScope
    {
        public ShaderArgQualifier result;
        CommonTree tree;

        @Override
        public CommonTree getTree( )
        {
            return tree;
        }
    };

    // $ANTLR start "qualifier"
    // ../core/src/main/resources/shader/antlr/GlslArg.g:85:1: qualifier returns [ShaderArgQualifier result] : ( CONST | ATTRIBUTE | VARYING | INVARIANT VARYING | UNIFORM );
    public final GlslArgParser.qualifier_return qualifier( ) throws RecognitionException
    {
        GlslArgParser.qualifier_return retval = new GlslArgParser.qualifier_return( );
        retval.start = input.LT( 1 );

        CommonTree root_0 = null;

        Token CONST34 = null;
        Token ATTRIBUTE35 = null;
        Token VARYING36 = null;
        Token INVARIANT37 = null;
        Token VARYING38 = null;
        Token UNIFORM39 = null;

        CommonTree CONST34_tree = null;
        CommonTree ATTRIBUTE35_tree = null;
        CommonTree VARYING36_tree = null;
        CommonTree INVARIANT37_tree = null;
        CommonTree VARYING38_tree = null;
        CommonTree UNIFORM39_tree = null;

        try
        {
            // ../core/src/main/resources/shader/antlr/GlslArg.g:86:3: ( CONST | ATTRIBUTE | VARYING | INVARIANT VARYING | UNIFORM )
            int alt6 = 5;
            switch ( input.LA( 1 ) )
            {
                case CONST:
                {
                    alt6 = 1;
                }
                    break;
                case ATTRIBUTE:
                {
                    alt6 = 2;
                }
                    break;
                case VARYING:
                {
                    alt6 = 3;
                }
                    break;
                case INVARIANT:
                {
                    alt6 = 4;
                }
                    break;
                case UNIFORM:
                {
                    alt6 = 5;
                }
                    break;
                default:
                    NoViableAltException nvae = new NoViableAltException( "", 6, 0, input );
                    throw nvae;
            }
            switch ( alt6 )
            {
                case 1:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:86:5: CONST
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    CONST34 = ( Token ) match( input, CONST, FOLLOW_CONST_in_qualifier531 );
                    CONST34_tree = ( CommonTree ) adaptor.create( CONST34 );
                    adaptor.addChild( root_0, CONST34_tree );

                    retval.result = ShaderArgQualifier.CONST;
                }
                    break;
                case 2:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:87:5: ATTRIBUTE
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    ATTRIBUTE35 = ( Token ) match( input, ATTRIBUTE, FOLLOW_ATTRIBUTE_in_qualifier554 );
                    ATTRIBUTE35_tree = ( CommonTree ) adaptor.create( ATTRIBUTE35 );
                    adaptor.addChild( root_0, ATTRIBUTE35_tree );

                    retval.result = ShaderArgQualifier.ATTRIBUTE;
                }
                    break;
                case 3:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:88:5: VARYING
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    VARYING36 = ( Token ) match( input, VARYING, FOLLOW_VARYING_in_qualifier573 );
                    VARYING36_tree = ( CommonTree ) adaptor.create( VARYING36 );
                    adaptor.addChild( root_0, VARYING36_tree );

                    retval.result = ShaderArgQualifier.VARYING;
                }
                    break;
                case 4:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:89:5: INVARIANT VARYING
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    INVARIANT37 = ( Token ) match( input, INVARIANT, FOLLOW_INVARIANT_in_qualifier594 );
                    INVARIANT37_tree = ( CommonTree ) adaptor.create( INVARIANT37 );
                    adaptor.addChild( root_0, INVARIANT37_tree );

                    VARYING38 = ( Token ) match( input, VARYING, FOLLOW_VARYING_in_qualifier596 );
                    VARYING38_tree = ( CommonTree ) adaptor.create( VARYING38 );
                    adaptor.addChild( root_0, VARYING38_tree );

                    retval.result = ShaderArgQualifier.INVARIANT_VARYING;
                }
                    break;
                case 5:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:90:5: UNIFORM
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    UNIFORM39 = ( Token ) match( input, UNIFORM, FOLLOW_UNIFORM_in_qualifier607 );
                    UNIFORM39_tree = ( CommonTree ) adaptor.create( UNIFORM39 );
                    adaptor.addChild( root_0, UNIFORM39_tree );

                    retval.result = ShaderArgQualifier.UNIFORM;
                }
                    break;

            }
            retval.stop = input.LT( -1 );

            retval.tree = ( CommonTree ) adaptor.rulePostProcessing( root_0 );
            adaptor.setTokenBoundaries( retval.tree, retval.start, retval.stop );

        }
        catch ( RecognitionException re )
        {
            reportError( re );
            recover( input, re );
            retval.tree = ( CommonTree ) adaptor.errorNode( input, retval.start, input.LT( -1 ), re );
        }
        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "qualifier"

    public static class inout_return extends ParserRuleReturnScope
    {
        public ShaderArgInOut result;
        CommonTree tree;

        @Override
        public CommonTree getTree( )
        {
            return tree;
        }
    };

    // $ANTLR start "inout"
    // ../core/src/main/resources/shader/antlr/GlslArg.g:93:1: inout returns [ShaderArgInOut result] : ( IN | OUT | INOUT );
    public final GlslArgParser.inout_return inout( ) throws RecognitionException
    {
        GlslArgParser.inout_return retval = new GlslArgParser.inout_return( );
        retval.start = input.LT( 1 );

        CommonTree root_0 = null;

        Token IN40 = null;
        Token OUT41 = null;
        Token INOUT42 = null;

        CommonTree IN40_tree = null;
        CommonTree OUT41_tree = null;
        CommonTree INOUT42_tree = null;

        try
        {
            // ../core/src/main/resources/shader/antlr/GlslArg.g:94:3: ( IN | OUT | INOUT )
            int alt7 = 3;
            switch ( input.LA( 1 ) )
            {
                case IN:
                {
                    alt7 = 1;
                }
                    break;
                case OUT:
                {
                    alt7 = 2;
                }
                    break;
                case INOUT:
                {
                    alt7 = 3;
                }
                    break;
                default:
                    NoViableAltException nvae = new NoViableAltException( "", 7, 0, input );
                    throw nvae;
            }
            switch ( alt7 )
            {
                case 1:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:94:5: IN
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    IN40 = ( Token ) match( input, IN, FOLLOW_IN_in_inout639 );
                    IN40_tree = ( CommonTree ) adaptor.create( IN40 );
                    adaptor.addChild( root_0, IN40_tree );

                    retval.result = ShaderArgInOut.IN;
                }
                    break;
                case 2:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:95:5: OUT
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    OUT41 = ( Token ) match( input, OUT, FOLLOW_OUT_in_inout650 );
                    OUT41_tree = ( CommonTree ) adaptor.create( OUT41 );
                    adaptor.addChild( root_0, OUT41_tree );

                    retval.result = ShaderArgInOut.OUT;
                }
                    break;
                case 3:
                // ../core/src/main/resources/shader/antlr/GlslArg.g:96:5: INOUT
                {
                    root_0 = ( CommonTree ) adaptor.nil( );

                    INOUT42 = ( Token ) match( input, INOUT, FOLLOW_INOUT_in_inout660 );
                    INOUT42_tree = ( CommonTree ) adaptor.create( INOUT42 );
                    adaptor.addChild( root_0, INOUT42_tree );

                    retval.result = ShaderArgInOut.INOUT;
                }
                    break;

            }
            retval.stop = input.LT( -1 );

            retval.tree = ( CommonTree ) adaptor.rulePostProcessing( root_0 );
            adaptor.setTokenBoundaries( retval.tree, retval.start, retval.stop );

        }
        catch ( RecognitionException re )
        {
            reportError( re );
            recover( input, re );
            retval.tree = ( CommonTree ) adaptor.errorNode( input, retval.start, input.LT( -1 ), re );
        }
        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "inout"

    public static class identifier_return extends ParserRuleReturnScope
    {
        public String result;
        CommonTree tree;

        @Override
        public CommonTree getTree( )
        {
            return tree;
        }
    };

    // $ANTLR start "identifier"
    // ../core/src/main/resources/shader/antlr/GlslArg.g:99:1: identifier returns [String result] : IDENTIFIER ;
    public final GlslArgParser.identifier_return identifier( ) throws RecognitionException
    {
        GlslArgParser.identifier_return retval = new GlslArgParser.identifier_return( );
        retval.start = input.LT( 1 );

        CommonTree root_0 = null;

        Token IDENTIFIER43 = null;

        CommonTree IDENTIFIER43_tree = null;

        try
        {
            // ../core/src/main/resources/shader/antlr/GlslArg.g:100:3: ( IDENTIFIER )
            // ../core/src/main/resources/shader/antlr/GlslArg.g:100:5: IDENTIFIER
            {
                root_0 = ( CommonTree ) adaptor.nil( );

                IDENTIFIER43 = ( Token ) match( input, IDENTIFIER, FOLLOW_IDENTIFIER_in_identifier679 );
                IDENTIFIER43_tree = ( CommonTree ) adaptor.create( IDENTIFIER43 );
                adaptor.addChild( root_0, IDENTIFIER43_tree );

                retval.result = ( IDENTIFIER43 != null ? IDENTIFIER43.getText( ) : null );
            }

            retval.stop = input.LT( -1 );

            retval.tree = ( CommonTree ) adaptor.rulePostProcessing( root_0 );
            adaptor.setTokenBoundaries( retval.tree, retval.start, retval.stop );

        }
        catch ( RecognitionException re )
        {
            reportError( re );
            recover( input, re );
            retval.tree = ( CommonTree ) adaptor.errorNode( input, retval.start, input.LT( -1 ), re );
        }
        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "identifier"

    // Delegated rules

    public static final BitSet FOLLOW_parameter_in_shader71 = new BitSet( new long[] { 0x1FE3E138FF840BB0L } );
    public static final BitSet FOLLOW_VOID_in_shader78 = new BitSet( new long[] { 0x8000000000000000L } );
    public static final BitSet FOLLOW_63_in_shader81 = new BitSet( new long[] { 0x0000000400000000L } );
    public static final BitSet FOLLOW_LPAREN_in_shader84 = new BitSet( new long[] { 0x1000100000000000L } );
    public static final BitSet FOLLOW_VOID_in_shader88 = new BitSet( new long[] { 0x0000100000000000L } );
    public static final BitSet FOLLOW_RPAREN_in_shader93 = new BitSet( new long[] { 0x0000000100000000L } );
    public static final BitSet FOLLOW_LCURLY_in_shader96 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_qualifier_in_parameter125 = new BitSet( new long[] { 0x1EC3E138FB8403A0L } );
    public static final BitSet FOLLOW_inout_in_parameter136 = new BitSet( new long[] { 0x1EC3E038FA0403A0L } );
    public static final BitSet FOLLOW_type_in_parameter147 = new BitSet( new long[] { 0x0000000000200000L } );
    public static final BitSet FOLLOW_identifier_in_parameter157 = new BitSet( new long[] { 0x0004000000000000L } );
    public static final BitSet FOLLOW_SEMI_in_parameter165 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_VOID_in_type187 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_FLOAT_in_type193 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_INT_in_type208 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_BOOL_in_type225 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_VEC2_in_type241 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_VEC3_in_type257 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_VEC4_in_type273 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_BVEC2_in_type289 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_BVEC3_in_type304 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_BVEC4_in_type319 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_IVEC2_in_type334 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_IVEC3_in_type349 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_IVEC4_in_type364 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_MAT2_in_type379 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_MAT3_in_type395 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_MAT4_in_type411 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_SAMPLER1D_in_type427 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_ISAMPLER1D_in_type438 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_USAMPLER1D_in_type448 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_SAMPLER2D_in_type458 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_ISAMPLER2D_in_type469 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_USAMPLER2D_in_type479 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_SAMPLERCUBE_in_type489 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_SAMPLER1DARRAY_in_type498 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_SAMPLER2DARRAY_in_type509 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_CONST_in_qualifier531 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_ATTRIBUTE_in_qualifier554 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_VARYING_in_qualifier573 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_INVARIANT_in_qualifier594 = new BitSet( new long[] { 0x0100000000000000L } );
    public static final BitSet FOLLOW_VARYING_in_qualifier596 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_UNIFORM_in_qualifier607 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_IN_in_inout639 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_OUT_in_inout650 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_INOUT_in_inout660 = new BitSet( new long[] { 0x0000000000000002L } );
    public static final BitSet FOLLOW_IDENTIFIER_in_identifier679 = new BitSet( new long[] { 0x0000000000000002L } );
}
