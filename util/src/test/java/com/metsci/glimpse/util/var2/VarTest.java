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
package com.metsci.glimpse.util.var2;

import static com.metsci.glimpse.util.var2.VarTestUtils.*;
import static com.metsci.glimpse.util.var2.VarUtils.addOldNewListener;
import static com.metsci.glimpse.util.var2.VarUtils.propertyVar;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.util.var2.ReadableVar;
import com.metsci.glimpse.util.var2.Var;
import com.metsci.glimpse.util.var2.VarBasic;

class VarTest
{

    @Test
    void oldNewShouldFireCompleted( )
    {
        Var<String> a = new VarBasic<>( "x" );

        List<OldNewPairFiring<String>> fs = new ArrayList<>( );
        addOldNewListener( a, ( ongoing, vOld, vNew ) ->
        {
            fs.add( f( ongoing, vOld, vNew ) );
        } );

        // When we make a non-ongoing change, the listener should
        // run, even though the value hasn't changed
        a.set(  true, "A" );
        a.set( false, "A" );

        assertEquals( asList( f(  true, "x", "A" ),
                              f( false, "A", "A" ) ),
                      fs );
    }

    @Test
    void varShouldFireOnEvenIfOngoingHasntChanged( )
    {
        Var<String> a = new VarBasic<>( "x" );

        List<VarFiring<String>> fs = new ArrayList<>( );
        a.addListener( ongoing ->
        {
            fs.add( f( ongoing, a.v( ) ) );
        } );

        // The last change in this sequence should cause the listener
        // to run, even though it sets the same value as the previous
        // ongoing change
        a.set(  true, "A" );
        a.set( false, "B" );
        a.set(  true, "A" );

        assertEquals( asList( f(  true, "A" ),
                              f( false, "B" ),
                              f(  true, "A" ) ),
                      fs );
    }

    @Test
    void derivedVarShouldFireIffDerivedValueChanges( )
    {
        Var<String> a = new VarBasic<>( "AAAA" );
        ReadableVar<Integer> b = propertyVar( a, s -> s.length( ) );

        List<String> vs = new ArrayList<>( );
        b.addListener( ( ) ->
        {
            vs.add( a.v( ) );
        } );

        // String length stays the same, so listener shouldn't run
        a.set( "BBBB" );
        assertEquals( 0, vs.size( ) );

        // String length changes, so listener should run
        a.set( "B" );
        assertEquals( 1, vs.size( ) );
    }

    @Test
    void basicVarShouldFireIffValueChanges( )
    {
        Var<String> a = new VarBasic<>( "x" );

        a.addListener( ( ) ->
        {
            a.set( "B" );
        } );

        List<String> vs = new ArrayList<>( );
        a.addListener( ( ) ->
        {
            vs.add( a.v( ) );
        } );

        // This is a little tricky:
        //
        //   set( A )
        //   ├ listener1.run( )
        //   │ └ set( B )
        //   │   ├ listener1.run( )
        //   │   └ listener2.run( )
        //   └ listener2.run( )
        //
        // Note that listener2 runs twice -- but the value is B
        // both times, so the second run should get filtered
        a.set( "A" );

        assertEquals( ImmutableList.of( "B" ), vs );
    }

}
