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
// $ANTLR 3.3 Nov 30, 2010 12:50:56 src/main/resources/shader/antlr/GlslEs.g 2011-03-10 22:59:41

package com.metsci.glimpse.gl.shader.grammar;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class GlslEsLexer extends Lexer
{
    public static final int EOF = -1;
    public static final int IDENTIFIER = 4;
    public static final int INTCONSTANT = 5;
    public static final int FLOATCONSTANT = 6;
    public static final int BOOLCONSTANT = 7;
    public static final int LEFT_PAREN = 8;
    public static final int RIGHT_PAREN = 9;
    public static final int LEFT_BRACKET = 10;
    public static final int RIGHT_BRACKET = 11;
    public static final int DOT = 12;
    public static final int INC_OP = 13;
    public static final int DEC_OP = 14;
    public static final int VOID = 15;
    public static final int COMMA = 16;
    public static final int FLOAT = 17;
    public static final int INT = 18;
    public static final int BOOL = 19;
    public static final int VEC2 = 20;
    public static final int VEC3 = 21;
    public static final int VEC4 = 22;
    public static final int BVEC2 = 23;
    public static final int BVEC3 = 24;
    public static final int BVEC4 = 25;
    public static final int IVEC2 = 26;
    public static final int IVEC3 = 27;
    public static final int IVEC4 = 28;
    public static final int MAT2 = 29;
    public static final int MAT3 = 30;
    public static final int MAT4 = 31;
    public static final int PLUS = 32;
    public static final int DASH = 33;
    public static final int BANG = 34;
    public static final int STAR = 35;
    public static final int SLASH = 36;
    public static final int LEFT_ANGLE = 37;
    public static final int RIGHT_ANGLE = 38;
    public static final int LE_OP = 39;
    public static final int GE_OP = 40;
    public static final int EQ_OP = 41;
    public static final int NE_OP = 42;
    public static final int AND_OP = 43;
    public static final int XOR_OP = 44;
    public static final int OR_OP = 45;
    public static final int QUESTION = 46;
    public static final int COLON = 47;
    public static final int EQUAL = 48;
    public static final int MUL_ASSIGN = 49;
    public static final int DIV_ASSIGN = 50;
    public static final int ADD_ASSIGN = 51;
    public static final int SUB_ASSIGN = 52;
    public static final int SEMICOLON = 53;
    public static final int PRECISION = 54;
    public static final int IN = 55;
    public static final int OUT = 56;
    public static final int INOUT = 57;
    public static final int INVARIANT = 58;
    public static final int CONST = 59;
    public static final int ATTRIBUTE = 60;
    public static final int VARYING = 61;
    public static final int UNIFORM = 62;
    public static final int SAMPLER2D = 63;
    public static final int SAMPLERCUBE = 64;
    public static final int HIGH_PRECISION = 65;
    public static final int MEDIUM_PRECISION = 66;
    public static final int LOW_PRECISION = 67;
    public static final int STRUCT = 68;
    public static final int LEFT_BRACE = 69;
    public static final int RIGHT_BRACE = 70;
    public static final int IF = 71;
    public static final int ELSE = 72;
    public static final int WHILE = 73;
    public static final int DO = 74;
    public static final int FOR = 75;
    public static final int CONTINUE = 76;
    public static final int BREAK = 77;
    public static final int RETURN = 78;
    public static final int DISCARD = 79;
    public static final int FALSE = 80;
    public static final int TRUE = 81;
    public static final int EXPONENT_PART = 82;
    public static final int DECIMAL_CONSTANT = 83;
    public static final int OCTAL_CONSTANT = 84;
    public static final int HEXDIGIT = 85;
    public static final int HEXADECIMAL_CONSTANT = 86;
    public static final int MOD_ASSIGN = 87;
    public static final int TILDE = 88;
    public static final int PERCENT = 89;
    public static final int VERTICAL_BAR = 90;
    public static final int CARET = 91;
    public static final int AMPERSAND = 92;
    public static final int WHITESPACE = 93;
    public static final int COMMENT = 94;
    public static final int MULTILINE_COMMENT = 95;

    // delegates
    // delegators

    public GlslEsLexer( )
    {
        ;
    }

    public GlslEsLexer( CharStream input )
    {
        this( input, new RecognizerSharedState( ) );
    }

    public GlslEsLexer( CharStream input, RecognizerSharedState state )
    {
        super( input, state );

    }

    public String getGrammarFileName( )
    {
        return "src/main/resources/shader/antlr/GlslEs.g";
    }

    // $ANTLR start "ATTRIBUTE"
    public final void mATTRIBUTE( ) throws RecognitionException
    {
        try
        {
            int _type = ATTRIBUTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:463:18: ( 'attribute' )
            // src/main/resources/shader/antlr/GlslEs.g:463:20: 'attribute'
            {
                match( "attribute" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "ATTRIBUTE"

    // $ANTLR start "BOOL"
    public final void mBOOL( ) throws RecognitionException
    {
        try
        {
            int _type = BOOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:464:18: ( 'bool' )
            // src/main/resources/shader/antlr/GlslEs.g:464:20: 'bool'
            {
                match( "bool" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "BOOL"

    // $ANTLR start "BREAK"
    public final void mBREAK( ) throws RecognitionException
    {
        try
        {
            int _type = BREAK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:465:18: ( 'break' )
            // src/main/resources/shader/antlr/GlslEs.g:465:20: 'break'
            {
                match( "break" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "BREAK"

    // $ANTLR start "BVEC2"
    public final void mBVEC2( ) throws RecognitionException
    {
        try
        {
            int _type = BVEC2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:466:18: ( 'bvec2' )
            // src/main/resources/shader/antlr/GlslEs.g:466:20: 'bvec2'
            {
                match( "bvec2" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "BVEC2"

    // $ANTLR start "BVEC3"
    public final void mBVEC3( ) throws RecognitionException
    {
        try
        {
            int _type = BVEC3;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:467:18: ( 'bvec3' )
            // src/main/resources/shader/antlr/GlslEs.g:467:20: 'bvec3'
            {
                match( "bvec3" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "BVEC3"

    // $ANTLR start "BVEC4"
    public final void mBVEC4( ) throws RecognitionException
    {
        try
        {
            int _type = BVEC4;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:468:18: ( 'bvec4' )
            // src/main/resources/shader/antlr/GlslEs.g:468:20: 'bvec4'
            {
                match( "bvec4" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "BVEC4"

    // $ANTLR start "CONST"
    public final void mCONST( ) throws RecognitionException
    {
        try
        {
            int _type = CONST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:469:18: ( 'const' )
            // src/main/resources/shader/antlr/GlslEs.g:469:20: 'const'
            {
                match( "const" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "CONST"

    // $ANTLR start "CONTINUE"
    public final void mCONTINUE( ) throws RecognitionException
    {
        try
        {
            int _type = CONTINUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:470:18: ( 'continue' )
            // src/main/resources/shader/antlr/GlslEs.g:470:20: 'continue'
            {
                match( "continue" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "CONTINUE"

    // $ANTLR start "DISCARD"
    public final void mDISCARD( ) throws RecognitionException
    {
        try
        {
            int _type = DISCARD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:471:18: ( 'discard' )
            // src/main/resources/shader/antlr/GlslEs.g:471:20: 'discard'
            {
                match( "discard" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "DISCARD"

    // $ANTLR start "DO"
    public final void mDO( ) throws RecognitionException
    {
        try
        {
            int _type = DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:472:18: ( 'do' )
            // src/main/resources/shader/antlr/GlslEs.g:472:20: 'do'
            {
                match( "do" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "DO"

    // $ANTLR start "ELSE"
    public final void mELSE( ) throws RecognitionException
    {
        try
        {
            int _type = ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:473:18: ( 'else' )
            // src/main/resources/shader/antlr/GlslEs.g:473:20: 'else'
            {
                match( "else" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "ELSE"

    // $ANTLR start "FALSE"
    public final void mFALSE( ) throws RecognitionException
    {
        try
        {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:474:18: ( 'false' )
            // src/main/resources/shader/antlr/GlslEs.g:474:20: 'false'
            {
                match( "false" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "FLOAT"
    public final void mFLOAT( ) throws RecognitionException
    {
        try
        {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:475:18: ( 'float' )
            // src/main/resources/shader/antlr/GlslEs.g:475:20: 'float'
            {
                match( "float" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "FOR"
    public final void mFOR( ) throws RecognitionException
    {
        try
        {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:476:18: ( 'for' )
            // src/main/resources/shader/antlr/GlslEs.g:476:20: 'for'
            {
                match( "for" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "HIGH_PRECISION"
    public final void mHIGH_PRECISION( ) throws RecognitionException
    {
        try
        {
            int _type = HIGH_PRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:477:18: ( 'highp' )
            // src/main/resources/shader/antlr/GlslEs.g:477:20: 'highp'
            {
                match( "highp" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "HIGH_PRECISION"

    // $ANTLR start "IF"
    public final void mIF( ) throws RecognitionException
    {
        try
        {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:478:18: ( 'if' )
            // src/main/resources/shader/antlr/GlslEs.g:478:20: 'if'
            {
                match( "if" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "IF"

    // $ANTLR start "IN"
    public final void mIN( ) throws RecognitionException
    {
        try
        {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:479:18: ( 'in' )
            // src/main/resources/shader/antlr/GlslEs.g:479:20: 'in'
            {
                match( "in" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "INOUT"
    public final void mINOUT( ) throws RecognitionException
    {
        try
        {
            int _type = INOUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:480:18: ( 'inout' )
            // src/main/resources/shader/antlr/GlslEs.g:480:20: 'inout'
            {
                match( "inout" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "INOUT"

    // $ANTLR start "INT"
    public final void mINT( ) throws RecognitionException
    {
        try
        {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:481:18: ( 'int' )
            // src/main/resources/shader/antlr/GlslEs.g:481:20: 'int'
            {
                match( "int" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "INVARIANT"
    public final void mINVARIANT( ) throws RecognitionException
    {
        try
        {
            int _type = INVARIANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:482:18: ( 'invariant' )
            // src/main/resources/shader/antlr/GlslEs.g:482:20: 'invariant'
            {
                match( "invariant" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "INVARIANT"

    // $ANTLR start "IVEC2"
    public final void mIVEC2( ) throws RecognitionException
    {
        try
        {
            int _type = IVEC2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:483:18: ( 'ivec2' )
            // src/main/resources/shader/antlr/GlslEs.g:483:20: 'ivec2'
            {
                match( "ivec2" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "IVEC2"

    // $ANTLR start "IVEC3"
    public final void mIVEC3( ) throws RecognitionException
    {
        try
        {
            int _type = IVEC3;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:484:18: ( 'ivec3' )
            // src/main/resources/shader/antlr/GlslEs.g:484:20: 'ivec3'
            {
                match( "ivec3" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "IVEC3"

    // $ANTLR start "IVEC4"
    public final void mIVEC4( ) throws RecognitionException
    {
        try
        {
            int _type = IVEC4;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:485:18: ( 'ivec4' )
            // src/main/resources/shader/antlr/GlslEs.g:485:20: 'ivec4'
            {
                match( "ivec4" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "IVEC4"

    // $ANTLR start "LOW_PRECISION"
    public final void mLOW_PRECISION( ) throws RecognitionException
    {
        try
        {
            int _type = LOW_PRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:486:18: ( 'lowp' )
            // src/main/resources/shader/antlr/GlslEs.g:486:20: 'lowp'
            {
                match( "lowp" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "LOW_PRECISION"

    // $ANTLR start "MAT2"
    public final void mMAT2( ) throws RecognitionException
    {
        try
        {
            int _type = MAT2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:487:18: ( 'mat2' )
            // src/main/resources/shader/antlr/GlslEs.g:487:20: 'mat2'
            {
                match( "mat2" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MAT2"

    // $ANTLR start "MAT3"
    public final void mMAT3( ) throws RecognitionException
    {
        try
        {
            int _type = MAT3;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:488:18: ( 'mat3' )
            // src/main/resources/shader/antlr/GlslEs.g:488:20: 'mat3'
            {
                match( "mat3" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MAT3"

    // $ANTLR start "MAT4"
    public final void mMAT4( ) throws RecognitionException
    {
        try
        {
            int _type = MAT4;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:489:18: ( 'mat4' )
            // src/main/resources/shader/antlr/GlslEs.g:489:20: 'mat4'
            {
                match( "mat4" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MAT4"

    // $ANTLR start "MEDIUM_PRECISION"
    public final void mMEDIUM_PRECISION( ) throws RecognitionException
    {
        try
        {
            int _type = MEDIUM_PRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:490:18: ( 'mediump' )
            // src/main/resources/shader/antlr/GlslEs.g:490:20: 'mediump'
            {
                match( "mediump" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MEDIUM_PRECISION"

    // $ANTLR start "OUT"
    public final void mOUT( ) throws RecognitionException
    {
        try
        {
            int _type = OUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:491:18: ( 'out' )
            // src/main/resources/shader/antlr/GlslEs.g:491:20: 'out'
            {
                match( "out" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "OUT"

    // $ANTLR start "PRECISION"
    public final void mPRECISION( ) throws RecognitionException
    {
        try
        {
            int _type = PRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:492:18: ( 'precision' )
            // src/main/resources/shader/antlr/GlslEs.g:492:20: 'precision'
            {
                match( "precision" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "PRECISION"

    // $ANTLR start "RETURN"
    public final void mRETURN( ) throws RecognitionException
    {
        try
        {
            int _type = RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:493:18: ( 'return' )
            // src/main/resources/shader/antlr/GlslEs.g:493:20: 'return'
            {
                match( "return" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "RETURN"

    // $ANTLR start "SAMPLER2D"
    public final void mSAMPLER2D( ) throws RecognitionException
    {
        try
        {
            int _type = SAMPLER2D;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:494:18: ( 'sampler2D' )
            // src/main/resources/shader/antlr/GlslEs.g:494:20: 'sampler2D'
            {
                match( "sampler2D" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "SAMPLER2D"

    // $ANTLR start "SAMPLERCUBE"
    public final void mSAMPLERCUBE( ) throws RecognitionException
    {
        try
        {
            int _type = SAMPLERCUBE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:495:18: ( 'samplerCube' )
            // src/main/resources/shader/antlr/GlslEs.g:495:20: 'samplerCube'
            {
                match( "samplerCube" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "SAMPLERCUBE"

    // $ANTLR start "STRUCT"
    public final void mSTRUCT( ) throws RecognitionException
    {
        try
        {
            int _type = STRUCT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:496:18: ( 'struct' )
            // src/main/resources/shader/antlr/GlslEs.g:496:20: 'struct'
            {
                match( "struct" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "STRUCT"

    // $ANTLR start "TRUE"
    public final void mTRUE( ) throws RecognitionException
    {
        try
        {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:497:18: ( 'true' )
            // src/main/resources/shader/antlr/GlslEs.g:497:20: 'true'
            {
                match( "true" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "UNIFORM"
    public final void mUNIFORM( ) throws RecognitionException
    {
        try
        {
            int _type = UNIFORM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:498:18: ( 'uniform' )
            // src/main/resources/shader/antlr/GlslEs.g:498:20: 'uniform'
            {
                match( "uniform" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "UNIFORM"

    // $ANTLR start "VARYING"
    public final void mVARYING( ) throws RecognitionException
    {
        try
        {
            int _type = VARYING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:499:18: ( 'varying' )
            // src/main/resources/shader/antlr/GlslEs.g:499:20: 'varying'
            {
                match( "varying" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "VARYING"

    // $ANTLR start "VEC2"
    public final void mVEC2( ) throws RecognitionException
    {
        try
        {
            int _type = VEC2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:500:18: ( 'vec2' )
            // src/main/resources/shader/antlr/GlslEs.g:500:20: 'vec2'
            {
                match( "vec2" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "VEC2"

    // $ANTLR start "VEC3"
    public final void mVEC3( ) throws RecognitionException
    {
        try
        {
            int _type = VEC3;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:501:18: ( 'vec3' )
            // src/main/resources/shader/antlr/GlslEs.g:501:20: 'vec3'
            {
                match( "vec3" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "VEC3"

    // $ANTLR start "VEC4"
    public final void mVEC4( ) throws RecognitionException
    {
        try
        {
            int _type = VEC4;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:502:18: ( 'vec4' )
            // src/main/resources/shader/antlr/GlslEs.g:502:20: 'vec4'
            {
                match( "vec4" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "VEC4"

    // $ANTLR start "VOID"
    public final void mVOID( ) throws RecognitionException
    {
        try
        {
            int _type = VOID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:503:18: ( 'void' )
            // src/main/resources/shader/antlr/GlslEs.g:503:20: 'void'
            {
                match( "void" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "VOID"

    // $ANTLR start "WHILE"
    public final void mWHILE( ) throws RecognitionException
    {
        try
        {
            int _type = WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:504:18: ( 'while' )
            // src/main/resources/shader/antlr/GlslEs.g:504:20: 'while'
            {
                match( "while" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "WHILE"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER( ) throws RecognitionException
    {
        try
        {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:507:3: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // src/main/resources/shader/antlr/GlslEs.g:507:5: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
                if ( ( input.LA( 1 ) >= 'A' && input.LA( 1 ) <= 'Z' ) || input.LA( 1 ) == '_' || ( input.LA( 1 ) >= 'a' && input.LA( 1 ) <= 'z' ) )
                {
                    input.consume( );

                }
                else
                {
                    MismatchedSetException mse = new MismatchedSetException( null, input );
                    recover( mse );
                    throw mse;
                }

                // src/main/resources/shader/antlr/GlslEs.g:507:28: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
                loop1: do
                {
                    int alt1 = 2;
                    int LA1_0 = input.LA( 1 );

                    if ( ( ( LA1_0 >= '0' && LA1_0 <= '9' ) || ( LA1_0 >= 'A' && LA1_0 <= 'Z' ) || LA1_0 == '_' || ( LA1_0 >= 'a' && LA1_0 <= 'z' ) ) )
                    {
                        alt1 = 1;
                    }

                    switch ( alt1 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:
                        {
                            if ( ( input.LA( 1 ) >= '0' && input.LA( 1 ) <= '9' ) || ( input.LA( 1 ) >= 'A' && input.LA( 1 ) <= 'Z' ) || input.LA( 1 ) == '_' || ( input.LA( 1 ) >= 'a' && input.LA( 1 ) <= 'z' ) )
                            {
                                input.consume( );

                            }
                            else
                            {
                                MismatchedSetException mse = new MismatchedSetException( null, input );
                                recover( mse );
                                throw mse;
                            }

                        }
                            break;

                        default:
                            break loop1;
                    }
                }
                while ( true );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "EXPONENT_PART"
    public final void mEXPONENT_PART( ) throws RecognitionException
    {
        try
        {
            // src/main/resources/shader/antlr/GlslEs.g:521:24: ( ( 'e' | 'E' ) ( PLUS | DASH )? ( '0' .. '9' )+ )
            // src/main/resources/shader/antlr/GlslEs.g:521:26: ( 'e' | 'E' ) ( PLUS | DASH )? ( '0' .. '9' )+
            {
                if ( input.LA( 1 ) == 'E' || input.LA( 1 ) == 'e' )
                {
                    input.consume( );

                }
                else
                {
                    MismatchedSetException mse = new MismatchedSetException( null, input );
                    recover( mse );
                    throw mse;
                }

                // src/main/resources/shader/antlr/GlslEs.g:521:36: ( PLUS | DASH )?
                int alt2 = 2;
                int LA2_0 = input.LA( 1 );

                if ( ( LA2_0 == '+' || LA2_0 == '-' ) )
                {
                    alt2 = 1;
                }
                switch ( alt2 )
                {
                    case 1:
                    // src/main/resources/shader/antlr/GlslEs.g:
                    {
                        if ( input.LA( 1 ) == '+' || input.LA( 1 ) == '-' )
                        {
                            input.consume( );

                        }
                        else
                        {
                            MismatchedSetException mse = new MismatchedSetException( null, input );
                            recover( mse );
                            throw mse;
                        }

                    }
                        break;

                }

                // src/main/resources/shader/antlr/GlslEs.g:521:51: ( '0' .. '9' )+
                int cnt3 = 0;
                loop3: do
                {
                    int alt3 = 2;
                    int LA3_0 = input.LA( 1 );

                    if ( ( ( LA3_0 >= '0' && LA3_0 <= '9' ) ) )
                    {
                        alt3 = 1;
                    }

                    switch ( alt3 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:521:52: '0' .. '9'
                        {
                            matchRange( '0', '9' );

                        }
                            break;

                        default:
                            if ( cnt3 >= 1 ) break loop3;
                            EarlyExitException eee = new EarlyExitException( 3, input );
                            throw eee;
                    }
                    cnt3++;
                }
                while ( true );

            }

        }
        finally
        {
        }
    }
    // $ANTLR end "EXPONENT_PART"

    // $ANTLR start "FLOATCONSTANT"
    public final void mFLOATCONSTANT( ) throws RecognitionException
    {
        try
        {
            int _type = FLOATCONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:524:3: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT_PART )? | '.' ( '0' .. '9' )+ ( EXPONENT_PART )? )
            int alt9 = 2;
            int LA9_0 = input.LA( 1 );

            if ( ( ( LA9_0 >= '0' && LA9_0 <= '9' ) ) )
            {
                alt9 = 1;
            }
            else if ( ( LA9_0 == '.' ) )
            {
                alt9 = 2;
            }
            else
            {
                NoViableAltException nvae = new NoViableAltException( "", 9, 0, input );

                throw nvae;
            }
            switch ( alt9 )
            {
                case 1:
                // src/main/resources/shader/antlr/GlslEs.g:524:5: ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT_PART )?
                {
                    // src/main/resources/shader/antlr/GlslEs.g:524:5: ( '0' .. '9' )+
                    int cnt4 = 0;
                    loop4: do
                    {
                        int alt4 = 2;
                        int LA4_0 = input.LA( 1 );

                        if ( ( ( LA4_0 >= '0' && LA4_0 <= '9' ) ) )
                        {
                            alt4 = 1;
                        }

                        switch ( alt4 )
                        {
                            case 1:
                            // src/main/resources/shader/antlr/GlslEs.g:524:6: '0' .. '9'
                            {
                                matchRange( '0', '9' );

                            }
                                break;

                            default:
                                if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee = new EarlyExitException( 4, input );
                                throw eee;
                        }
                        cnt4++;
                    }
                    while ( true );

                    match( '.' );
                    // src/main/resources/shader/antlr/GlslEs.g:524:21: ( '0' .. '9' )*
                    loop5: do
                    {
                        int alt5 = 2;
                        int LA5_0 = input.LA( 1 );

                        if ( ( ( LA5_0 >= '0' && LA5_0 <= '9' ) ) )
                        {
                            alt5 = 1;
                        }

                        switch ( alt5 )
                        {
                            case 1:
                            // src/main/resources/shader/antlr/GlslEs.g:524:22: '0' .. '9'
                            {
                                matchRange( '0', '9' );

                            }
                                break;

                            default:
                                break loop5;
                        }
                    }
                    while ( true );

                    // src/main/resources/shader/antlr/GlslEs.g:524:33: ( EXPONENT_PART )?
                    int alt6 = 2;
                    int LA6_0 = input.LA( 1 );

                    if ( ( LA6_0 == 'E' || LA6_0 == 'e' ) )
                    {
                        alt6 = 1;
                    }
                    switch ( alt6 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:524:34: EXPONENT_PART
                        {
                            mEXPONENT_PART( );

                        }
                            break;

                    }

                }
                    break;
                case 2:
                // src/main/resources/shader/antlr/GlslEs.g:525:5: '.' ( '0' .. '9' )+ ( EXPONENT_PART )?
                {
                    match( '.' );
                    // src/main/resources/shader/antlr/GlslEs.g:525:9: ( '0' .. '9' )+
                    int cnt7 = 0;
                    loop7: do
                    {
                        int alt7 = 2;
                        int LA7_0 = input.LA( 1 );

                        if ( ( ( LA7_0 >= '0' && LA7_0 <= '9' ) ) )
                        {
                            alt7 = 1;
                        }

                        switch ( alt7 )
                        {
                            case 1:
                            // src/main/resources/shader/antlr/GlslEs.g:525:10: '0' .. '9'
                            {
                                matchRange( '0', '9' );

                            }
                                break;

                            default:
                                if ( cnt7 >= 1 ) break loop7;
                                EarlyExitException eee = new EarlyExitException( 7, input );
                                throw eee;
                        }
                        cnt7++;
                    }
                    while ( true );

                    // src/main/resources/shader/antlr/GlslEs.g:525:21: ( EXPONENT_PART )?
                    int alt8 = 2;
                    int LA8_0 = input.LA( 1 );

                    if ( ( LA8_0 == 'E' || LA8_0 == 'e' ) )
                    {
                        alt8 = 1;
                    }
                    switch ( alt8 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:525:22: EXPONENT_PART
                        {
                            mEXPONENT_PART( );

                        }
                            break;

                    }

                }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "FLOATCONSTANT"

    // $ANTLR start "DECIMAL_CONSTANT"
    public final void mDECIMAL_CONSTANT( ) throws RecognitionException
    {
        try
        {
            // src/main/resources/shader/antlr/GlslEs.g:529:3: ( ( '1' .. '9' ) ( '0' .. '9' )* )
            // src/main/resources/shader/antlr/GlslEs.g:529:5: ( '1' .. '9' ) ( '0' .. '9' )*
            {
                // src/main/resources/shader/antlr/GlslEs.g:529:5: ( '1' .. '9' )
                // src/main/resources/shader/antlr/GlslEs.g:529:6: '1' .. '9'
                {
                    matchRange( '1', '9' );

                }

                // src/main/resources/shader/antlr/GlslEs.g:529:15: ( '0' .. '9' )*
                loop10: do
                {
                    int alt10 = 2;
                    int LA10_0 = input.LA( 1 );

                    if ( ( ( LA10_0 >= '0' && LA10_0 <= '9' ) ) )
                    {
                        alt10 = 1;
                    }

                    switch ( alt10 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:529:16: '0' .. '9'
                        {
                            matchRange( '0', '9' );

                        }
                            break;

                        default:
                            break loop10;
                    }
                }
                while ( true );

            }

        }
        finally
        {
        }
    }
    // $ANTLR end "DECIMAL_CONSTANT"

    // $ANTLR start "OCTAL_CONSTANT"
    public final void mOCTAL_CONSTANT( ) throws RecognitionException
    {
        try
        {
            // src/main/resources/shader/antlr/GlslEs.g:533:3: ( '0' ( '0' .. '7' )* )
            // src/main/resources/shader/antlr/GlslEs.g:533:5: '0' ( '0' .. '7' )*
            {
                match( '0' );
                // src/main/resources/shader/antlr/GlslEs.g:533:9: ( '0' .. '7' )*
                loop11: do
                {
                    int alt11 = 2;
                    int LA11_0 = input.LA( 1 );

                    if ( ( ( LA11_0 >= '0' && LA11_0 <= '7' ) ) )
                    {
                        alt11 = 1;
                    }

                    switch ( alt11 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:533:10: '0' .. '7'
                        {
                            matchRange( '0', '7' );

                        }
                            break;

                        default:
                            break loop11;
                    }
                }
                while ( true );

            }

        }
        finally
        {
        }
    }
    // $ANTLR end "OCTAL_CONSTANT"

    // $ANTLR start "HEXADECIMAL_CONSTANT"
    public final void mHEXADECIMAL_CONSTANT( ) throws RecognitionException
    {
        try
        {
            // src/main/resources/shader/antlr/GlslEs.g:537:3: ( '0' ( 'x' | 'X' ) ( HEXDIGIT )+ )
            // src/main/resources/shader/antlr/GlslEs.g:537:5: '0' ( 'x' | 'X' ) ( HEXDIGIT )+
            {
                match( '0' );
                if ( input.LA( 1 ) == 'X' || input.LA( 1 ) == 'x' )
                {
                    input.consume( );

                }
                else
                {
                    MismatchedSetException mse = new MismatchedSetException( null, input );
                    recover( mse );
                    throw mse;
                }

                // src/main/resources/shader/antlr/GlslEs.g:537:19: ( HEXDIGIT )+
                int cnt12 = 0;
                loop12: do
                {
                    int alt12 = 2;
                    int LA12_0 = input.LA( 1 );

                    if ( ( ( LA12_0 >= '0' && LA12_0 <= '9' ) || ( LA12_0 >= 'A' && LA12_0 <= 'F' ) || ( LA12_0 >= 'a' && LA12_0 <= 'f' ) ) )
                    {
                        alt12 = 1;
                    }

                    switch ( alt12 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:537:19: HEXDIGIT
                        {
                            mHEXDIGIT( );

                        }
                            break;

                        default:
                            if ( cnt12 >= 1 ) break loop12;
                            EarlyExitException eee = new EarlyExitException( 12, input );
                            throw eee;
                    }
                    cnt12++;
                }
                while ( true );

            }

        }
        finally
        {
        }
    }
    // $ANTLR end "HEXADECIMAL_CONSTANT"

    // $ANTLR start "HEXDIGIT"
    public final void mHEXDIGIT( ) throws RecognitionException
    {
        try
        {
            // src/main/resources/shader/antlr/GlslEs.g:541:3: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // src/main/resources/shader/antlr/GlslEs.g:541:5: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            {
                if ( ( input.LA( 1 ) >= '0' && input.LA( 1 ) <= '9' ) || ( input.LA( 1 ) >= 'A' && input.LA( 1 ) <= 'F' ) || ( input.LA( 1 ) >= 'a' && input.LA( 1 ) <= 'f' ) )
                {
                    input.consume( );

                }
                else
                {
                    MismatchedSetException mse = new MismatchedSetException( null, input );
                    recover( mse );
                    throw mse;
                }

            }

        }
        finally
        {
        }
    }
    // $ANTLR end "HEXDIGIT"

    // $ANTLR start "INTCONSTANT"
    public final void mINTCONSTANT( ) throws RecognitionException
    {
        try
        {
            int _type = INTCONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:545:3: ( DECIMAL_CONSTANT | OCTAL_CONSTANT | HEXADECIMAL_CONSTANT )
            int alt13 = 3;
            int LA13_0 = input.LA( 1 );

            if ( ( ( LA13_0 >= '1' && LA13_0 <= '9' ) ) )
            {
                alt13 = 1;
            }
            else if ( ( LA13_0 == '0' ) )
            {
                int LA13_2 = input.LA( 2 );

                if ( ( LA13_2 == 'X' || LA13_2 == 'x' ) )
                {
                    alt13 = 3;
                }
                else
                {
                    alt13 = 2;
                }
            }
            else
            {
                NoViableAltException nvae = new NoViableAltException( "", 13, 0, input );

                throw nvae;
            }
            switch ( alt13 )
            {
                case 1:
                // src/main/resources/shader/antlr/GlslEs.g:545:5: DECIMAL_CONSTANT
                {
                    mDECIMAL_CONSTANT( );

                }
                    break;
                case 2:
                // src/main/resources/shader/antlr/GlslEs.g:546:5: OCTAL_CONSTANT
                {
                    mOCTAL_CONSTANT( );

                }
                    break;
                case 3:
                // src/main/resources/shader/antlr/GlslEs.g:547:5: HEXADECIMAL_CONSTANT
                {
                    mHEXADECIMAL_CONSTANT( );

                }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "INTCONSTANT"

    // $ANTLR start "BOOLCONSTANT"
    public final void mBOOLCONSTANT( ) throws RecognitionException
    {
        try
        {
            // src/main/resources/shader/antlr/GlslEs.g:551:3: ( TRUE | FALSE )
            int alt14 = 2;
            int LA14_0 = input.LA( 1 );

            if ( ( LA14_0 == 't' ) )
            {
                alt14 = 1;
            }
            else if ( ( LA14_0 == 'f' ) )
            {
                alt14 = 2;
            }
            else
            {
                NoViableAltException nvae = new NoViableAltException( "", 14, 0, input );

                throw nvae;
            }
            switch ( alt14 )
            {
                case 1:
                // src/main/resources/shader/antlr/GlslEs.g:551:5: TRUE
                {
                    mTRUE( );

                }
                    break;
                case 2:
                // src/main/resources/shader/antlr/GlslEs.g:552:5: FALSE
                {
                    mFALSE( );

                }
                    break;

            }
        }
        finally
        {
        }
    }
    // $ANTLR end "BOOLCONSTANT"

    // $ANTLR start "INC_OP"
    public final void mINC_OP( ) throws RecognitionException
    {
        try
        {
            int _type = INC_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:563:18: ( '++' )
            // src/main/resources/shader/antlr/GlslEs.g:563:20: '++'
            {
                match( "++" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "INC_OP"

    // $ANTLR start "DEC_OP"
    public final void mDEC_OP( ) throws RecognitionException
    {
        try
        {
            int _type = DEC_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:564:18: ( '--' )
            // src/main/resources/shader/antlr/GlslEs.g:564:20: '--'
            {
                match( "--" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "DEC_OP"

    // $ANTLR start "LE_OP"
    public final void mLE_OP( ) throws RecognitionException
    {
        try
        {
            int _type = LE_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:565:18: ( '<=' )
            // src/main/resources/shader/antlr/GlslEs.g:565:20: '<='
            {
                match( "<=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "LE_OP"

    // $ANTLR start "GE_OP"
    public final void mGE_OP( ) throws RecognitionException
    {
        try
        {
            int _type = GE_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:566:18: ( '>=' )
            // src/main/resources/shader/antlr/GlslEs.g:566:20: '>='
            {
                match( ">=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "GE_OP"

    // $ANTLR start "EQ_OP"
    public final void mEQ_OP( ) throws RecognitionException
    {
        try
        {
            int _type = EQ_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:567:18: ( '==' )
            // src/main/resources/shader/antlr/GlslEs.g:567:20: '=='
            {
                match( "==" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "EQ_OP"

    // $ANTLR start "NE_OP"
    public final void mNE_OP( ) throws RecognitionException
    {
        try
        {
            int _type = NE_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:568:18: ( '!=' )
            // src/main/resources/shader/antlr/GlslEs.g:568:20: '!='
            {
                match( "!=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "NE_OP"

    // $ANTLR start "AND_OP"
    public final void mAND_OP( ) throws RecognitionException
    {
        try
        {
            int _type = AND_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:570:18: ( '&&' )
            // src/main/resources/shader/antlr/GlslEs.g:570:20: '&&'
            {
                match( "&&" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "AND_OP"

    // $ANTLR start "OR_OP"
    public final void mOR_OP( ) throws RecognitionException
    {
        try
        {
            int _type = OR_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:571:18: ( '||' )
            // src/main/resources/shader/antlr/GlslEs.g:571:20: '||'
            {
                match( "||" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "OR_OP"

    // $ANTLR start "XOR_OP"
    public final void mXOR_OP( ) throws RecognitionException
    {
        try
        {
            int _type = XOR_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:572:18: ( '^^' )
            // src/main/resources/shader/antlr/GlslEs.g:572:20: '^^'
            {
                match( "^^" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "XOR_OP"

    // $ANTLR start "MUL_ASSIGN"
    public final void mMUL_ASSIGN( ) throws RecognitionException
    {
        try
        {
            int _type = MUL_ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:573:18: ( '*=' )
            // src/main/resources/shader/antlr/GlslEs.g:573:20: '*='
            {
                match( "*=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MUL_ASSIGN"

    // $ANTLR start "DIV_ASSIGN"
    public final void mDIV_ASSIGN( ) throws RecognitionException
    {
        try
        {
            int _type = DIV_ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:574:18: ( '/=' )
            // src/main/resources/shader/antlr/GlslEs.g:574:20: '/='
            {
                match( "/=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "DIV_ASSIGN"

    // $ANTLR start "ADD_ASSIGN"
    public final void mADD_ASSIGN( ) throws RecognitionException
    {
        try
        {
            int _type = ADD_ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:575:18: ( '+=' )
            // src/main/resources/shader/antlr/GlslEs.g:575:20: '+='
            {
                match( "+=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "ADD_ASSIGN"

    // $ANTLR start "MOD_ASSIGN"
    public final void mMOD_ASSIGN( ) throws RecognitionException
    {
        try
        {
            int _type = MOD_ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:576:18: ( '%=' )
            // src/main/resources/shader/antlr/GlslEs.g:576:20: '%='
            {
                match( "%=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MOD_ASSIGN"

    // $ANTLR start "SUB_ASSIGN"
    public final void mSUB_ASSIGN( ) throws RecognitionException
    {
        try
        {
            int _type = SUB_ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:582:18: ( '-=' )
            // src/main/resources/shader/antlr/GlslEs.g:582:20: '-='
            {
                match( "-=" );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "SUB_ASSIGN"

    // $ANTLR start "LEFT_PAREN"
    public final void mLEFT_PAREN( ) throws RecognitionException
    {
        try
        {
            int _type = LEFT_PAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:584:18: ( '(' )
            // src/main/resources/shader/antlr/GlslEs.g:584:20: '('
            {
                match( '(' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "LEFT_PAREN"

    // $ANTLR start "RIGHT_PAREN"
    public final void mRIGHT_PAREN( ) throws RecognitionException
    {
        try
        {
            int _type = RIGHT_PAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:585:18: ( ')' )
            // src/main/resources/shader/antlr/GlslEs.g:585:20: ')'
            {
                match( ')' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "RIGHT_PAREN"

    // $ANTLR start "LEFT_BRACKET"
    public final void mLEFT_BRACKET( ) throws RecognitionException
    {
        try
        {
            int _type = LEFT_BRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:586:18: ( '[' )
            // src/main/resources/shader/antlr/GlslEs.g:586:20: '['
            {
                match( '[' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "LEFT_BRACKET"

    // $ANTLR start "RIGHT_BRACKET"
    public final void mRIGHT_BRACKET( ) throws RecognitionException
    {
        try
        {
            int _type = RIGHT_BRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:587:18: ( ']' )
            // src/main/resources/shader/antlr/GlslEs.g:587:20: ']'
            {
                match( ']' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "RIGHT_BRACKET"

    // $ANTLR start "LEFT_BRACE"
    public final void mLEFT_BRACE( ) throws RecognitionException
    {
        try
        {
            int _type = LEFT_BRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:588:18: ( '{' )
            // src/main/resources/shader/antlr/GlslEs.g:588:20: '{'
            {
                match( '{' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "LEFT_BRACE"

    // $ANTLR start "RIGHT_BRACE"
    public final void mRIGHT_BRACE( ) throws RecognitionException
    {
        try
        {
            int _type = RIGHT_BRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:589:18: ( '}' )
            // src/main/resources/shader/antlr/GlslEs.g:589:20: '}'
            {
                match( '}' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "RIGHT_BRACE"

    // $ANTLR start "DOT"
    public final void mDOT( ) throws RecognitionException
    {
        try
        {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:590:18: ( '.' )
            // src/main/resources/shader/antlr/GlslEs.g:590:20: '.'
            {
                match( '.' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "COMMA"
    public final void mCOMMA( ) throws RecognitionException
    {
        try
        {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:592:18: ( ',' )
            // src/main/resources/shader/antlr/GlslEs.g:592:20: ','
            {
                match( ',' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "COLON"
    public final void mCOLON( ) throws RecognitionException
    {
        try
        {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:593:18: ( ':' )
            // src/main/resources/shader/antlr/GlslEs.g:593:20: ':'
            {
                match( ':' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "EQUAL"
    public final void mEQUAL( ) throws RecognitionException
    {
        try
        {
            int _type = EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:594:18: ( '=' )
            // src/main/resources/shader/antlr/GlslEs.g:594:20: '='
            {
                match( '=' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "EQUAL"

    // $ANTLR start "SEMICOLON"
    public final void mSEMICOLON( ) throws RecognitionException
    {
        try
        {
            int _type = SEMICOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:595:18: ( ';' )
            // src/main/resources/shader/antlr/GlslEs.g:595:20: ';'
            {
                match( ';' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "SEMICOLON"

    // $ANTLR start "BANG"
    public final void mBANG( ) throws RecognitionException
    {
        try
        {
            int _type = BANG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:596:18: ( '!' )
            // src/main/resources/shader/antlr/GlslEs.g:596:20: '!'
            {
                match( '!' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "BANG"

    // $ANTLR start "DASH"
    public final void mDASH( ) throws RecognitionException
    {
        try
        {
            int _type = DASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:597:18: ( '-' )
            // src/main/resources/shader/antlr/GlslEs.g:597:20: '-'
            {
                match( '-' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "DASH"

    // $ANTLR start "TILDE"
    public final void mTILDE( ) throws RecognitionException
    {
        try
        {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:598:18: ( '~' )
            // src/main/resources/shader/antlr/GlslEs.g:598:20: '~'
            {
                match( '~' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "PLUS"
    public final void mPLUS( ) throws RecognitionException
    {
        try
        {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:599:18: ( '+' )
            // src/main/resources/shader/antlr/GlslEs.g:599:20: '+'
            {
                match( '+' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "STAR"
    public final void mSTAR( ) throws RecognitionException
    {
        try
        {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:600:18: ( '*' )
            // src/main/resources/shader/antlr/GlslEs.g:600:20: '*'
            {
                match( '*' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "SLASH"
    public final void mSLASH( ) throws RecognitionException
    {
        try
        {
            int _type = SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:601:18: ( '/' )
            // src/main/resources/shader/antlr/GlslEs.g:601:20: '/'
            {
                match( '/' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "SLASH"

    // $ANTLR start "PERCENT"
    public final void mPERCENT( ) throws RecognitionException
    {
        try
        {
            int _type = PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:602:18: ( '%' )
            // src/main/resources/shader/antlr/GlslEs.g:602:20: '%'
            {
                match( '%' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "PERCENT"

    // $ANTLR start "LEFT_ANGLE"
    public final void mLEFT_ANGLE( ) throws RecognitionException
    {
        try
        {
            int _type = LEFT_ANGLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:604:18: ( '<' )
            // src/main/resources/shader/antlr/GlslEs.g:604:20: '<'
            {
                match( '<' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "LEFT_ANGLE"

    // $ANTLR start "RIGHT_ANGLE"
    public final void mRIGHT_ANGLE( ) throws RecognitionException
    {
        try
        {
            int _type = RIGHT_ANGLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:605:18: ( '>' )
            // src/main/resources/shader/antlr/GlslEs.g:605:20: '>'
            {
                match( '>' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "RIGHT_ANGLE"

    // $ANTLR start "VERTICAL_BAR"
    public final void mVERTICAL_BAR( ) throws RecognitionException
    {
        try
        {
            int _type = VERTICAL_BAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:606:18: ( '|' )
            // src/main/resources/shader/antlr/GlslEs.g:606:20: '|'
            {
                match( '|' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "VERTICAL_BAR"

    // $ANTLR start "CARET"
    public final void mCARET( ) throws RecognitionException
    {
        try
        {
            int _type = CARET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:607:18: ( '^' )
            // src/main/resources/shader/antlr/GlslEs.g:607:20: '^'
            {
                match( '^' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "CARET"

    // $ANTLR start "AMPERSAND"
    public final void mAMPERSAND( ) throws RecognitionException
    {
        try
        {
            int _type = AMPERSAND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:608:18: ( '&' )
            // src/main/resources/shader/antlr/GlslEs.g:608:20: '&'
            {
                match( '&' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "AMPERSAND"

    // $ANTLR start "QUESTION"
    public final void mQUESTION( ) throws RecognitionException
    {
        try
        {
            int _type = QUESTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:609:18: ( '?' )
            // src/main/resources/shader/antlr/GlslEs.g:609:20: '?'
            {
                match( '?' );

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "QUESTION"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE( ) throws RecognitionException
    {
        try
        {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:615:3: ( ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' ) )
            // src/main/resources/shader/antlr/GlslEs.g:615:5: ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' )
            {
                if ( ( input.LA( 1 ) >= '\t' && input.LA( 1 ) <= '\n' ) || ( input.LA( 1 ) >= '\f' && input.LA( 1 ) <= '\r' ) || input.LA( 1 ) == ' ' )
                {
                    input.consume( );

                }
                else
                {
                    MismatchedSetException mse = new MismatchedSetException( null, input );
                    recover( mse );
                    throw mse;
                }

                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "WHITESPACE"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT( ) throws RecognitionException
    {
        try
        {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:620:3: ( '//' (~ ( '\\n' | '\\r' ) )* )
            // src/main/resources/shader/antlr/GlslEs.g:620:5: '//' (~ ( '\\n' | '\\r' ) )*
            {
                match( "//" );

                // src/main/resources/shader/antlr/GlslEs.g:620:10: (~ ( '\\n' | '\\r' ) )*
                loop15: do
                {
                    int alt15 = 2;
                    int LA15_0 = input.LA( 1 );

                    if ( ( ( LA15_0 >= '\u0000' && LA15_0 <= '\t' ) || ( LA15_0 >= '\u000B' && LA15_0 <= '\f' ) || ( LA15_0 >= '\u000E' && LA15_0 <= '\uFFFF' ) ) )
                    {
                        alt15 = 1;
                    }

                    switch ( alt15 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:620:11: ~ ( '\\n' | '\\r' )
                        {
                            if ( ( input.LA( 1 ) >= '\u0000' && input.LA( 1 ) <= '\t' ) || ( input.LA( 1 ) >= '\u000B' && input.LA( 1 ) <= '\f' ) || ( input.LA( 1 ) >= '\u000E' && input.LA( 1 ) <= '\uFFFF' ) )
                            {
                                input.consume( );

                            }
                            else
                            {
                                MismatchedSetException mse = new MismatchedSetException( null, input );
                                recover( mse );
                                throw mse;
                            }

                        }
                            break;

                        default:
                            break loop15;
                    }
                }
                while ( true );

                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "MULTILINE_COMMENT"
    public final void mMULTILINE_COMMENT( ) throws RecognitionException
    {
        try
        {
            int _type = MULTILINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/resources/shader/antlr/GlslEs.g:625:3: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // src/main/resources/shader/antlr/GlslEs.g:625:5: '/*' ( options {greedy=false; } : . )* '*/'
            {
                match( "/*" );

                // src/main/resources/shader/antlr/GlslEs.g:625:10: ( options {greedy=false; } : . )*
                loop16: do
                {
                    int alt16 = 2;
                    int LA16_0 = input.LA( 1 );

                    if ( ( LA16_0 == '*' ) )
                    {
                        int LA16_1 = input.LA( 2 );

                        if ( ( LA16_1 == '/' ) )
                        {
                            alt16 = 2;
                        }
                        else if ( ( ( LA16_1 >= '\u0000' && LA16_1 <= '.' ) || ( LA16_1 >= '0' && LA16_1 <= '\uFFFF' ) ) )
                        {
                            alt16 = 1;
                        }

                    }
                    else if ( ( ( LA16_0 >= '\u0000' && LA16_0 <= ')' ) || ( LA16_0 >= '+' && LA16_0 <= '\uFFFF' ) ) )
                    {
                        alt16 = 1;
                    }

                    switch ( alt16 )
                    {
                        case 1:
                        // src/main/resources/shader/antlr/GlslEs.g:625:38: .
                        {
                            matchAny( );

                        }
                            break;

                        default:
                            break loop16;
                    }
                }
                while ( true );

                match( "*/" );

                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally
        {
        }
    }
    // $ANTLR end "MULTILINE_COMMENT"

    public void mTokens( ) throws RecognitionException
    {
        // src/main/resources/shader/antlr/GlslEs.g:1:8: ( ATTRIBUTE | BOOL | BREAK | BVEC2 | BVEC3 | BVEC4 | CONST | CONTINUE | DISCARD | DO | ELSE | FALSE | FLOAT | FOR | HIGH_PRECISION | IF | IN | INOUT | INT | INVARIANT | IVEC2 | IVEC3 | IVEC4 | LOW_PRECISION | MAT2 | MAT3 | MAT4 | MEDIUM_PRECISION | OUT | PRECISION | RETURN | SAMPLER2D | SAMPLERCUBE | STRUCT | TRUE | UNIFORM | VARYING | VEC2 | VEC3 | VEC4 | VOID | WHILE | IDENTIFIER | FLOATCONSTANT | INTCONSTANT | INC_OP | DEC_OP | LE_OP | GE_OP | EQ_OP | NE_OP | AND_OP | OR_OP | XOR_OP | MUL_ASSIGN | DIV_ASSIGN | ADD_ASSIGN | MOD_ASSIGN | SUB_ASSIGN | LEFT_PAREN | RIGHT_PAREN | LEFT_BRACKET | RIGHT_BRACKET | LEFT_BRACE | RIGHT_BRACE | DOT | COMMA | COLON | EQUAL | SEMICOLON | BANG | DASH | TILDE | PLUS | STAR | SLASH | PERCENT | LEFT_ANGLE | RIGHT_ANGLE | VERTICAL_BAR | CARET | AMPERSAND | QUESTION | WHITESPACE | COMMENT | MULTILINE_COMMENT )
        int alt17 = 86;
        alt17 = dfa17.predict( input );
        switch ( alt17 )
        {
            case 1:
            // src/main/resources/shader/antlr/GlslEs.g:1:10: ATTRIBUTE
            {
                mATTRIBUTE( );

            }
                break;
            case 2:
            // src/main/resources/shader/antlr/GlslEs.g:1:20: BOOL
            {
                mBOOL( );

            }
                break;
            case 3:
            // src/main/resources/shader/antlr/GlslEs.g:1:25: BREAK
            {
                mBREAK( );

            }
                break;
            case 4:
            // src/main/resources/shader/antlr/GlslEs.g:1:31: BVEC2
            {
                mBVEC2( );

            }
                break;
            case 5:
            // src/main/resources/shader/antlr/GlslEs.g:1:37: BVEC3
            {
                mBVEC3( );

            }
                break;
            case 6:
            // src/main/resources/shader/antlr/GlslEs.g:1:43: BVEC4
            {
                mBVEC4( );

            }
                break;
            case 7:
            // src/main/resources/shader/antlr/GlslEs.g:1:49: CONST
            {
                mCONST( );

            }
                break;
            case 8:
            // src/main/resources/shader/antlr/GlslEs.g:1:55: CONTINUE
            {
                mCONTINUE( );

            }
                break;
            case 9:
            // src/main/resources/shader/antlr/GlslEs.g:1:64: DISCARD
            {
                mDISCARD( );

            }
                break;
            case 10:
            // src/main/resources/shader/antlr/GlslEs.g:1:72: DO
            {
                mDO( );

            }
                break;
            case 11:
            // src/main/resources/shader/antlr/GlslEs.g:1:75: ELSE
            {
                mELSE( );

            }
                break;
            case 12:
            // src/main/resources/shader/antlr/GlslEs.g:1:80: FALSE
            {
                mFALSE( );

            }
                break;
            case 13:
            // src/main/resources/shader/antlr/GlslEs.g:1:86: FLOAT
            {
                mFLOAT( );

            }
                break;
            case 14:
            // src/main/resources/shader/antlr/GlslEs.g:1:92: FOR
            {
                mFOR( );

            }
                break;
            case 15:
            // src/main/resources/shader/antlr/GlslEs.g:1:96: HIGH_PRECISION
            {
                mHIGH_PRECISION( );

            }
                break;
            case 16:
            // src/main/resources/shader/antlr/GlslEs.g:1:111: IF
            {
                mIF( );

            }
                break;
            case 17:
            // src/main/resources/shader/antlr/GlslEs.g:1:114: IN
            {
                mIN( );

            }
                break;
            case 18:
            // src/main/resources/shader/antlr/GlslEs.g:1:117: INOUT
            {
                mINOUT( );

            }
                break;
            case 19:
            // src/main/resources/shader/antlr/GlslEs.g:1:123: INT
            {
                mINT( );

            }
                break;
            case 20:
            // src/main/resources/shader/antlr/GlslEs.g:1:127: INVARIANT
            {
                mINVARIANT( );

            }
                break;
            case 21:
            // src/main/resources/shader/antlr/GlslEs.g:1:137: IVEC2
            {
                mIVEC2( );

            }
                break;
            case 22:
            // src/main/resources/shader/antlr/GlslEs.g:1:143: IVEC3
            {
                mIVEC3( );

            }
                break;
            case 23:
            // src/main/resources/shader/antlr/GlslEs.g:1:149: IVEC4
            {
                mIVEC4( );

            }
                break;
            case 24:
            // src/main/resources/shader/antlr/GlslEs.g:1:155: LOW_PRECISION
            {
                mLOW_PRECISION( );

            }
                break;
            case 25:
            // src/main/resources/shader/antlr/GlslEs.g:1:169: MAT2
            {
                mMAT2( );

            }
                break;
            case 26:
            // src/main/resources/shader/antlr/GlslEs.g:1:174: MAT3
            {
                mMAT3( );

            }
                break;
            case 27:
            // src/main/resources/shader/antlr/GlslEs.g:1:179: MAT4
            {
                mMAT4( );

            }
                break;
            case 28:
            // src/main/resources/shader/antlr/GlslEs.g:1:184: MEDIUM_PRECISION
            {
                mMEDIUM_PRECISION( );

            }
                break;
            case 29:
            // src/main/resources/shader/antlr/GlslEs.g:1:201: OUT
            {
                mOUT( );

            }
                break;
            case 30:
            // src/main/resources/shader/antlr/GlslEs.g:1:205: PRECISION
            {
                mPRECISION( );

            }
                break;
            case 31:
            // src/main/resources/shader/antlr/GlslEs.g:1:215: RETURN
            {
                mRETURN( );

            }
                break;
            case 32:
            // src/main/resources/shader/antlr/GlslEs.g:1:222: SAMPLER2D
            {
                mSAMPLER2D( );

            }
                break;
            case 33:
            // src/main/resources/shader/antlr/GlslEs.g:1:232: SAMPLERCUBE
            {
                mSAMPLERCUBE( );

            }
                break;
            case 34:
            // src/main/resources/shader/antlr/GlslEs.g:1:244: STRUCT
            {
                mSTRUCT( );

            }
                break;
            case 35:
            // src/main/resources/shader/antlr/GlslEs.g:1:251: TRUE
            {
                mTRUE( );

            }
                break;
            case 36:
            // src/main/resources/shader/antlr/GlslEs.g:1:256: UNIFORM
            {
                mUNIFORM( );

            }
                break;
            case 37:
            // src/main/resources/shader/antlr/GlslEs.g:1:264: VARYING
            {
                mVARYING( );

            }
                break;
            case 38:
            // src/main/resources/shader/antlr/GlslEs.g:1:272: VEC2
            {
                mVEC2( );

            }
                break;
            case 39:
            // src/main/resources/shader/antlr/GlslEs.g:1:277: VEC3
            {
                mVEC3( );

            }
                break;
            case 40:
            // src/main/resources/shader/antlr/GlslEs.g:1:282: VEC4
            {
                mVEC4( );

            }
                break;
            case 41:
            // src/main/resources/shader/antlr/GlslEs.g:1:287: VOID
            {
                mVOID( );

            }
                break;
            case 42:
            // src/main/resources/shader/antlr/GlslEs.g:1:292: WHILE
            {
                mWHILE( );

            }
                break;
            case 43:
            // src/main/resources/shader/antlr/GlslEs.g:1:298: IDENTIFIER
            {
                mIDENTIFIER( );

            }
                break;
            case 44:
            // src/main/resources/shader/antlr/GlslEs.g:1:309: FLOATCONSTANT
            {
                mFLOATCONSTANT( );

            }
                break;
            case 45:
            // src/main/resources/shader/antlr/GlslEs.g:1:323: INTCONSTANT
            {
                mINTCONSTANT( );

            }
                break;
            case 46:
            // src/main/resources/shader/antlr/GlslEs.g:1:335: INC_OP
            {
                mINC_OP( );

            }
                break;
            case 47:
            // src/main/resources/shader/antlr/GlslEs.g:1:342: DEC_OP
            {
                mDEC_OP( );

            }
                break;
            case 48:
            // src/main/resources/shader/antlr/GlslEs.g:1:349: LE_OP
            {
                mLE_OP( );

            }
                break;
            case 49:
            // src/main/resources/shader/antlr/GlslEs.g:1:355: GE_OP
            {
                mGE_OP( );

            }
                break;
            case 50:
            // src/main/resources/shader/antlr/GlslEs.g:1:361: EQ_OP
            {
                mEQ_OP( );

            }
                break;
            case 51:
            // src/main/resources/shader/antlr/GlslEs.g:1:367: NE_OP
            {
                mNE_OP( );

            }
                break;
            case 52:
            // src/main/resources/shader/antlr/GlslEs.g:1:373: AND_OP
            {
                mAND_OP( );

            }
                break;
            case 53:
            // src/main/resources/shader/antlr/GlslEs.g:1:380: OR_OP
            {
                mOR_OP( );

            }
                break;
            case 54:
            // src/main/resources/shader/antlr/GlslEs.g:1:386: XOR_OP
            {
                mXOR_OP( );

            }
                break;
            case 55:
            // src/main/resources/shader/antlr/GlslEs.g:1:393: MUL_ASSIGN
            {
                mMUL_ASSIGN( );

            }
                break;
            case 56:
            // src/main/resources/shader/antlr/GlslEs.g:1:404: DIV_ASSIGN
            {
                mDIV_ASSIGN( );

            }
                break;
            case 57:
            // src/main/resources/shader/antlr/GlslEs.g:1:415: ADD_ASSIGN
            {
                mADD_ASSIGN( );

            }
                break;
            case 58:
            // src/main/resources/shader/antlr/GlslEs.g:1:426: MOD_ASSIGN
            {
                mMOD_ASSIGN( );

            }
                break;
            case 59:
            // src/main/resources/shader/antlr/GlslEs.g:1:437: SUB_ASSIGN
            {
                mSUB_ASSIGN( );

            }
                break;
            case 60:
            // src/main/resources/shader/antlr/GlslEs.g:1:448: LEFT_PAREN
            {
                mLEFT_PAREN( );

            }
                break;
            case 61:
            // src/main/resources/shader/antlr/GlslEs.g:1:459: RIGHT_PAREN
            {
                mRIGHT_PAREN( );

            }
                break;
            case 62:
            // src/main/resources/shader/antlr/GlslEs.g:1:471: LEFT_BRACKET
            {
                mLEFT_BRACKET( );

            }
                break;
            case 63:
            // src/main/resources/shader/antlr/GlslEs.g:1:484: RIGHT_BRACKET
            {
                mRIGHT_BRACKET( );

            }
                break;
            case 64:
            // src/main/resources/shader/antlr/GlslEs.g:1:498: LEFT_BRACE
            {
                mLEFT_BRACE( );

            }
                break;
            case 65:
            // src/main/resources/shader/antlr/GlslEs.g:1:509: RIGHT_BRACE
            {
                mRIGHT_BRACE( );

            }
                break;
            case 66:
            // src/main/resources/shader/antlr/GlslEs.g:1:521: DOT
            {
                mDOT( );

            }
                break;
            case 67:
            // src/main/resources/shader/antlr/GlslEs.g:1:525: COMMA
            {
                mCOMMA( );

            }
                break;
            case 68:
            // src/main/resources/shader/antlr/GlslEs.g:1:531: COLON
            {
                mCOLON( );

            }
                break;
            case 69:
            // src/main/resources/shader/antlr/GlslEs.g:1:537: EQUAL
            {
                mEQUAL( );

            }
                break;
            case 70:
            // src/main/resources/shader/antlr/GlslEs.g:1:543: SEMICOLON
            {
                mSEMICOLON( );

            }
                break;
            case 71:
            // src/main/resources/shader/antlr/GlslEs.g:1:553: BANG
            {
                mBANG( );

            }
                break;
            case 72:
            // src/main/resources/shader/antlr/GlslEs.g:1:558: DASH
            {
                mDASH( );

            }
                break;
            case 73:
            // src/main/resources/shader/antlr/GlslEs.g:1:563: TILDE
            {
                mTILDE( );

            }
                break;
            case 74:
            // src/main/resources/shader/antlr/GlslEs.g:1:569: PLUS
            {
                mPLUS( );

            }
                break;
            case 75:
            // src/main/resources/shader/antlr/GlslEs.g:1:574: STAR
            {
                mSTAR( );

            }
                break;
            case 76:
            // src/main/resources/shader/antlr/GlslEs.g:1:579: SLASH
            {
                mSLASH( );

            }
                break;
            case 77:
            // src/main/resources/shader/antlr/GlslEs.g:1:585: PERCENT
            {
                mPERCENT( );

            }
                break;
            case 78:
            // src/main/resources/shader/antlr/GlslEs.g:1:593: LEFT_ANGLE
            {
                mLEFT_ANGLE( );

            }
                break;
            case 79:
            // src/main/resources/shader/antlr/GlslEs.g:1:604: RIGHT_ANGLE
            {
                mRIGHT_ANGLE( );

            }
                break;
            case 80:
            // src/main/resources/shader/antlr/GlslEs.g:1:616: VERTICAL_BAR
            {
                mVERTICAL_BAR( );

            }
                break;
            case 81:
            // src/main/resources/shader/antlr/GlslEs.g:1:629: CARET
            {
                mCARET( );

            }
                break;
            case 82:
            // src/main/resources/shader/antlr/GlslEs.g:1:635: AMPERSAND
            {
                mAMPERSAND( );

            }
                break;
            case 83:
            // src/main/resources/shader/antlr/GlslEs.g:1:645: QUESTION
            {
                mQUESTION( );

            }
                break;
            case 84:
            // src/main/resources/shader/antlr/GlslEs.g:1:654: WHITESPACE
            {
                mWHITESPACE( );

            }
                break;
            case 85:
            // src/main/resources/shader/antlr/GlslEs.g:1:665: COMMENT
            {
                mCOMMENT( );

            }
                break;
            case 86:
            // src/main/resources/shader/antlr/GlslEs.g:1:673: MULTILINE_COMMENT
            {
                mMULTILINE_COMMENT( );

            }
                break;

        }

    }

    protected DFA17 dfa17 = new DFA17( this );
    static final String DFA17_eotS = "\1\uffff\22\23\1\uffff\1\116\1\117\1\116\1\123\1\126\1\130\1\132" + "\1\134\1\136\1\140\1\142\1\144\1\146\1\152\1\154\14\uffff\6\23\1" + "\163\5\23\1\171\1\175\17\23\1\uffff\1\116\2\uffff\1\116\34\uffff" + "\6\23\1\uffff\3\23\1\u0097\1\23\1\uffff\1\23\1\u009a\1\23\1\uffff" + "\4\23\1\u00a2\13\23\1\u00b0\5\23\1\u00b8\2\23\1\uffff\2\23\1\uffff" + "\2\23\1\u00c1\1\u00c2\1\u00c3\1\u00c4\1\23\1\uffff\4\23\1\u00ca" + "\2\23\1\u00cd\1\u00ce\1\u00cf\1\u00d0\2\23\1\uffff\1\u00d3\1\u00d4" + "\1\u00d5\1\u00d6\1\u00d7\2\23\1\uffff\1\u00da\1\u00db\1\u00dc\1" + "\u00dd\1\23\1\u00df\1\u00e0\1\u00e1\4\uffff\5\23\1\uffff\2\23\4" + "\uffff\1\u00e9\1\23\5\uffff\2\23\4\uffff\1\23\3\uffff\2\23\1\u00f0" + "\1\23\1\u00f2\2\23\1\uffff\2\23\1\u00f7\1\23\1\u00f9\1\23\1\uffff" + "\1\23\1\uffff\1\u00fd\1\u00fe\1\23\1\u0100\1\uffff\1\23\1\uffff" + "\3\23\2\uffff\1\u0105\1\uffff\1\u0106\1\u0107\1\u0108\1\23\4\uffff" + "\1\23\1\u010b\1\uffff";
    static final String DFA17_eofS = "\u010c\uffff";
    static final String DFA17_minS = "\1\11\1\164\2\157\1\151\1\154\1\141\1\151\1\146\1\157\1\141\1\165" + "\1\162\1\145\1\141\1\162\1\156\1\141\1\150\1\uffff\1\56\1\60\1\56" + "\1\53\1\55\4\75\1\46\1\174\1\136\1\75\1\52\1\75\14\uffff\1\164\1" + "\157\2\145\1\156\1\163\1\60\1\163\1\154\1\157\1\162\1\147\2\60\1" + "\145\1\167\1\164\1\144\1\164\1\145\1\164\1\155\1\162\1\165\1\151" + "\1\162\1\143\2\151\1\uffff\1\56\2\uffff\1\56\34\uffff\1\162\1\154" + "\1\141\1\143\1\163\1\143\1\uffff\1\145\1\163\1\141\1\60\1\150\1" + "\uffff\1\165\1\60\1\141\1\uffff\1\143\1\160\1\62\1\151\1\60\1\143" + "\1\165\1\160\1\165\1\145\1\146\1\171\1\62\1\144\1\154\1\151\1\60" + "\1\153\1\62\1\164\1\151\1\141\1\60\1\145\1\164\1\uffff\1\160\1\164" + "\1\uffff\1\162\1\62\4\60\1\165\1\uffff\1\151\1\162\1\154\1\143\1" + "\60\1\157\1\151\4\60\1\145\1\142\1\uffff\5\60\1\156\1\162\1\uffff" + "\4\60\1\151\3\60\4\uffff\1\155\1\163\1\156\1\145\1\164\1\uffff\1" + "\162\1\156\4\uffff\1\60\1\165\5\uffff\1\165\1\144\4\uffff\1\141" + "\3\uffff\1\160\1\151\1\60\1\162\1\60\1\155\1\147\1\uffff\1\164\1" + "\145\1\60\1\156\1\60\1\157\1\uffff\1\62\1\uffff\2\60\1\145\1\60" + "\1\uffff\1\164\1\uffff\1\156\1\104\1\165\2\uffff\1\60\1\uffff\3" + "\60\1\142\4\uffff\1\145\1\60\1\uffff";
    static final String DFA17_maxS = "\1\176\1\164\1\166\2\157\1\154\1\157\1\151\1\166\1\157\1\145\1\165" + "\1\162\1\145\1\164\1\162\1\156\1\157\1\150\1\uffff\3\71\6\75\1\46" + "\1\174\1\136\3\75\14\uffff\1\164\1\157\2\145\1\156\1\163\1\172\1" + "\163\1\154\1\157\1\162\1\147\2\172\1\145\1\167\1\164\1\144\1\164" + "\1\145\1\164\1\155\1\162\1\165\1\151\1\162\1\143\2\151\1\uffff\1" + "\71\2\uffff\1\71\34\uffff\1\162\1\154\1\141\1\143\1\164\1\143\1" + "\uffff\1\145\1\163\1\141\1\172\1\150\1\uffff\1\165\1\172\1\141\1" + "\uffff\1\143\1\160\1\64\1\151\1\172\1\143\1\165\1\160\1\165\1\145" + "\1\146\1\171\1\64\1\144\1\154\1\151\1\172\1\153\1\64\1\164\1\151" + "\1\141\1\172\1\145\1\164\1\uffff\1\160\1\164\1\uffff\1\162\1\64" + "\4\172\1\165\1\uffff\1\151\1\162\1\154\1\143\1\172\1\157\1\151\4" + "\172\1\145\1\142\1\uffff\5\172\1\156\1\162\1\uffff\4\172\1\151\3" + "\172\4\uffff\1\155\1\163\1\156\1\145\1\164\1\uffff\1\162\1\156\4" + "\uffff\1\172\1\165\5\uffff\1\165\1\144\4\uffff\1\141\3\uffff\1\160" + "\1\151\1\172\1\162\1\172\1\155\1\147\1\uffff\1\164\1\145\1\172\1" + "\156\1\172\1\157\1\uffff\1\103\1\uffff\2\172\1\145\1\172\1\uffff" + "\1\164\1\uffff\1\156\1\104\1\165\2\uffff\1\172\1\uffff\3\172\1\142" + "\4\uffff\1\145\1\172\1\uffff";
    static final String DFA17_acceptS = "\23\uffff\1\53\17\uffff\1\74\1\75\1\76\1\77\1\100\1\101\1\103\1" + "\104\1\106\1\111\1\123\1\124\35\uffff\1\54\1\uffff\1\55\1\102\1" + "\uffff\1\56\1\71\1\112\1\57\1\73\1\110\1\60\1\116\1\61\1\117\1\62" + "\1\105\1\63\1\107\1\64\1\122\1\65\1\120\1\66\1\121\1\67\1\113\1" + "\70\1\125\1\126\1\114\1\72\1\115\6\uffff\1\12\5\uffff\1\20\3\uffff" + "\1\21\31\uffff\1\16\2\uffff\1\23\7\uffff\1\35\15\uffff\1\2\7\uffff" + "\1\13\10\uffff\1\30\1\31\1\32\1\33\5\uffff\1\43\2\uffff\1\46\1\47" + "\1\50\1\51\2\uffff\1\3\1\4\1\5\1\6\1\7\2\uffff\1\14\1\15\1\17\1" + "\22\1\uffff\1\25\1\26\1\27\7\uffff\1\52\6\uffff\1\37\1\uffff\1\42" + "\4\uffff\1\11\1\uffff\1\34\3\uffff\1\44\1\45\1\uffff\1\10\4\uffff" + "\1\1\1\24\1\36\1\40\2\uffff\1\41";
    static final String DFA17_specialS = "\u010c\uffff}>";
    static final String[] DFA17_transitionS = { "\2\56\1\uffff\2\56\22\uffff\1\56\1\34\3\uffff\1\42\1\35\1\uffff" + "\1\43\1\44\1\40\1\27\1\51\1\30\1\25\1\41\1\26\11\24\1\52\1\53" + "\1\31\1\33\1\32\1\55\1\uffff\32\23\1\45\1\uffff\1\46\1\37\1" + "\23\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\23\1\7\1\10\2\23\1\11" + "\1\12\1\23\1\13\1\14\1\23\1\15\1\16\1\17\1\20\1\21\1\22\3\23" + "\1\47\1\36\1\50\1\54", "\1\57", "\1\60\2\uffff\1\61\3\uffff\1\62", "\1\63", "\1\64\5\uffff\1\65", "\1\66", "\1\67\12\uffff\1\70\2\uffff\1\71", "\1\72", "\1\73\7\uffff\1\74\7\uffff\1\75", "\1\76", "\1\77\3\uffff\1\100", "\1\101", "\1\102", "\1\103", "\1\104\22\uffff\1\105", "\1\106", "\1\107", "\1\110\3\uffff\1\111\11\uffff\1\112", "\1\113", "", "\1\114\1\uffff\12\115", "\12\114", "\1\114\1\uffff\10\120\2\114", "\1\121\21\uffff\1\122", "\1\124\17\uffff\1\125", "\1\127", "\1\131", "\1\133", "\1\135", "\1\137", "\1\141", "\1\143", "\1\145", "\1\151\4\uffff\1\150\15\uffff\1\147", "\1\153", "", "", "", "", "", "", "", "", "", "", "", "", "\1\155", "\1\156", "\1\157", "\1\160", "\1\161", "\1\162", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\164", "\1\165", "\1\166", "\1\167", "\1\170", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\16\23\1\172\4\23" + "\1\173\1\23\1\174\4\23", "\1\176", "\1\177", "\1\u0080", "\1\u0081", "\1\u0082", "\1\u0083", "\1\u0084", "\1\u0085", "\1\u0086", "\1\u0087", "\1\u0088", "\1\u0089", "\1\u008a", "\1\u008b", "\1\u008c", "", "\1\114\1\uffff\12\115", "", "", "\1\114\1\uffff\10\120\2\114", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "\1\u008d", "\1\u008e", "\1\u008f", "\1\u0090", "\1\u0091\1\u0092", "\1\u0093", "", "\1\u0094", "\1\u0095", "\1\u0096", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u0098", "", "\1\u0099", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u009b", "", "\1\u009c", "\1\u009d", "\1\u009e\1\u009f\1\u00a0", "\1\u00a1", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00a3", "\1\u00a4", "\1\u00a5", "\1\u00a6", "\1\u00a7", "\1\u00a8", "\1\u00a9", "\1\u00aa\1\u00ab\1\u00ac", "\1\u00ad", "\1\u00ae", "\1\u00af", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00b1", "\1\u00b2\1\u00b3\1\u00b4", "\1\u00b5", "\1\u00b6", "\1\u00b7", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00b9", "\1\u00ba", "", "\1\u00bb", "\1\u00bc", "", "\1\u00bd", "\1\u00be\1\u00bf\1\u00c0", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00c5", "", "\1\u00c6", "\1\u00c7", "\1\u00c8", "\1\u00c9", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00cb", "\1\u00cc", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00d1", "\1\u00d2", "", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00d8", "\1\u00d9", "", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00de", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "", "", "", "", "\1\u00e2", "\1\u00e3", "\1\u00e4", "\1\u00e5", "\1\u00e6", "", "\1\u00e7", "\1\u00e8", "", "", "", "", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00ea", "", "", "", "", "", "\1\u00eb", "\1\u00ec", "", "", "", "", "\1\u00ed", "", "", "", "\1\u00ee", "\1\u00ef", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00f1", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00f3", "\1\u00f4", "", "\1\u00f5", "\1\u00f6", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00f8", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00fa", "", "\1\u00fb\20\uffff\1\u00fc", "", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u00ff", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "", "\1\u0101", "", "\1\u0102", "\1\u0103", "\1\u0104", "", "", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "\1\u0109", "", "", "", "", "\1\u010a", "\12\23\7\uffff\32\23\4\uffff\1\23\1\uffff\32\23", "" };

    static final short[] DFA17_eot = DFA.unpackEncodedString( DFA17_eotS );
    static final short[] DFA17_eof = DFA.unpackEncodedString( DFA17_eofS );
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars( DFA17_minS );
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars( DFA17_maxS );
    static final short[] DFA17_accept = DFA.unpackEncodedString( DFA17_acceptS );
    static final short[] DFA17_special = DFA.unpackEncodedString( DFA17_specialS );
    static final short[][] DFA17_transition;

    static
    {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for ( int i = 0; i < numStates; i++ )
        {
            DFA17_transition[i] = DFA.unpackEncodedString( DFA17_transitionS[i] );
        }
    }

    class DFA17 extends DFA
    {

        public DFA17( BaseRecognizer recognizer )
        {
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

        public String getDescription( )
        {
            return "1:1: Tokens : ( ATTRIBUTE | BOOL | BREAK | BVEC2 | BVEC3 | BVEC4 | CONST | CONTINUE | DISCARD | DO | ELSE | FALSE | FLOAT | FOR | HIGH_PRECISION | IF | IN | INOUT | INT | INVARIANT | IVEC2 | IVEC3 | IVEC4 | LOW_PRECISION | MAT2 | MAT3 | MAT4 | MEDIUM_PRECISION | OUT | PRECISION | RETURN | SAMPLER2D | SAMPLERCUBE | STRUCT | TRUE | UNIFORM | VARYING | VEC2 | VEC3 | VEC4 | VOID | WHILE | IDENTIFIER | FLOATCONSTANT | INTCONSTANT | INC_OP | DEC_OP | LE_OP | GE_OP | EQ_OP | NE_OP | AND_OP | OR_OP | XOR_OP | MUL_ASSIGN | DIV_ASSIGN | ADD_ASSIGN | MOD_ASSIGN | SUB_ASSIGN | LEFT_PAREN | RIGHT_PAREN | LEFT_BRACKET | RIGHT_BRACKET | LEFT_BRACE | RIGHT_BRACE | DOT | COMMA | COLON | EQUAL | SEMICOLON | BANG | DASH | TILDE | PLUS | STAR | SLASH | PERCENT | LEFT_ANGLE | RIGHT_ANGLE | VERTICAL_BAR | CARET | AMPERSAND | QUESTION | WHITESPACE | COMMENT | MULTILINE_COMMENT );";
        }
    }

}
