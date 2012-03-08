/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.gl.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import com.metsci.glimpse.gl.shader.grammar.GlslArgLexer;
import com.metsci.glimpse.gl.shader.grammar.GlslArgParser;
import com.metsci.glimpse.util.io.StreamOpener;


public class ShaderSource
{
    private static final Logger logger = Logger.getLogger( ShaderSource.class.getName() );

    private final String[] lines;

    public ShaderSource( String location, StreamOpener opener ) throws IOException
    {
        lines = readSource( opener.openForRead( location ) );
    }

    public String[] getSourceLines( )
    {
        if( lines == null )
            return null;

        return Arrays.copyOf( lines, lines.length );
    }

    public String getSource( )
    {
        StringBuffer r = new StringBuffer();
        for( String line: lines )
            r.append( line ).append( "\n" );

        return r.toString();
    }

    /**
     * Reads an ASCII file into an array of strings, one for each line.
     *
     * @param in input stream to load source from
     * @return an array of strings, one for each line in the file
     */
    public static String[] readSource( InputStream in ) throws IOException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        StringBuilder b = new StringBuilder();

        String line = null;
        String sep = System.getProperty( "line.separator" );
        while( ( line = reader.readLine() ) != null )
        {
            b.append( line.trim() + sep );
        }

        return new String[] { b.toString() };
    }

    public boolean containsMain( )
    {
        // TODO: Do this in the lexer/parser. This is going to annoy someone at some point.
        return getSource().contains( " main(" );
    }

    public List<ShaderArg> extractArgs( )
    {
        if( !containsMain() )
            return null;

        try
        {
            // parse out arguments
            CharStream stream = new ANTLRStringStream( getSource() );
            GlslArgLexer l = new GlslArgLexer( stream );
            GlslArgParser p = new GlslArgParser( new CommonTokenStream( l ) );
            List<ShaderArg> shaderArgs = p.shader().result;

            logger.log( Level.FINER, "Found " + shaderArgs.size() + " args." );
            if( shaderArgs.size() > 0 )
            {
                for( ShaderArg arg: shaderArgs )
                {
                    logger.log( Level.FINEST, "Shader arg: " + arg.toString() );
                }
            }

            return shaderArgs;
        }
        catch( RecognitionException e )
        {
            logger.severe( "Unable to parse shader file." );
            return null;
        }
    }
}
