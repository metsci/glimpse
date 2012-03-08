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
package com.metsci.glimpse.context;

import java.util.List;

/**
 * Utility method for manipulating {@link GlimpseTargetStack} instances.
 *
 * @author ulman
 */
public class TargetStackUtil
{
    /**
     * Creates a new GlimpseTargetStack which is an exact copy of the given stack.
     *
     * @param stack the stack to copy
     * @return a deep copy of the provided stack
     */
    public static GlimpseTargetStack newTargetStack( GlimpseTargetStack stack )
    {
        return newTargetStack( ).push( stack );
    }


    /**
     * Creates a new target stack which is the concatenation of the provided GlimpseTargets.
     * The first provided GlimpseTarget is placed at the bottom of the new GlimpseTargetStack.
     *
     * @param targets the GlimpseTargets to concatenate
     * @return the newly constructed GlimpseTargetStack
     */
    public static GlimpseTargetStack newTargetStack( GlimpseTarget... targets )
    {
        return new GlimpseTargetStackImpl( targets );
    }

    /**
     * Returns true if the query stack ends with the sequence of GlimpseTargets defined by the prefix stack.
     * Ignores the GlimpseBounds.
     *
     * @param query the GlimpseTargetStack to investigate
     * @param suffix a GlimpseTargetStack to search for at the end of the query stack
     * @return whether the query stack ends with the GlimpseTargets in the prefix stack
     */
    public static boolean endsWith( GlimpseTargetStack query, GlimpseTargetStack suffix )
    {
        int suffixSize = suffix.getSize( );
        int querySize = query.getSize( );

        List<GlimpseTarget> suffixList = suffix.getTargetList( );
        List<GlimpseTarget> queryList = query.getTargetList( );

        if ( suffixSize > querySize )
            return false;

        for ( int i = 0 ; i < suffixSize ; i++ )
        {
            GlimpseTarget prefixTarget = suffixList.get( suffixSize - i - 1 );
            GlimpseTarget queryTarget = queryList.get( querySize - i - 1 );

            if ( !queryTarget.equals( prefixTarget ) )
                return false;
        }

        return true;
    }

    /**
     * Returns true if the query stack starts with the sequence of GlimpseTargets defined by the prefix stack.
     * Ignores the GlimpseBounds.
     *
     * @param query the GlimpseTargetStack to investigate
     * @param prefix a GlimpseTargetStack to search for at the start of the query stack
     * @return whether the query stack starts with the GlimpseTargets in the prefix stack
     */
    public static boolean startsWith( GlimpseTargetStack query, GlimpseTargetStack prefix )
    {
        int prefixSize = prefix.getSize( );
        int querySize = query.getSize( );

        List<GlimpseTarget> prefixList = prefix.getTargetList( );
        List<GlimpseTarget> queryList = query.getTargetList( );

        if ( prefixSize > querySize )
            return false;

        for ( int i = 0 ; i < prefixSize ; i++ )
        {
            GlimpseTarget prefixTarget = prefixList.get( i );
            GlimpseTarget queryTarget = queryList.get( i );

            if ( !queryTarget.equals( prefixTarget ) )
                return false;
        }

        return true;
    }
}
