/*
 * Copyright (c) 2020, Metron, Inc.
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

import static com.metsci.glimpse.util.var2.ListenerFlag.IMMEDIATE;
import static com.metsci.glimpse.util.var2.VarTestUtils.f;
import static com.metsci.glimpse.util.var2.VarUtils.addOldNewListener;
import static com.metsci.glimpse.util.var2.VarUtils.mapValueVar;
import static com.metsci.glimpse.util.var2.VarUtils.propertyVar;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

class VarTest
{

    @Test
    void mapValueVarShouldHonorImmediateFlag( )
    {
        Var<ImmutableMap<String,String>> mapVar = new VarBasic<>( ImmutableMap.of( "theKey", "theValue" ) );
        Var<String> valueVar = mapValueVar( mapVar, "theKey" );

        List<VarFiring<String>> fs = new ArrayList<>( );
        valueVar.addListener( IMMEDIATE, ongoing ->
        {
            fs.add( f( ongoing, valueVar.v( ) ) );
        } );

        assertEquals( asList( f( false, "theValue" ) ),
                      fs );
    }

    @Test
    void listenerTensesShouldNotAffectFiringOrder( )
    {
        Var<String> a = new VarBasic<>( "x" );

        List<String> expected = new ArrayList<>( );
        List<String> actual = new ArrayList<>( );

        Random random = new Random( 0 );
        for ( int i = 0; i < 100; i++ )
        {
            String s = "Listener " + i;

            expected.add( s );

            int listenerTense = ( random.nextInt( ) & Integer.MAX_VALUE ) % 3;
            switch ( listenerTense )
            {
                case 0: a.addListener( ongoing -> actual.add( s ) ); break;
                case 1: a.all( ).addListener( ( ) -> actual.add( s ) ); break;
                case 2: a.completed( ).addListener( ( ) -> actual.add( s ) ); break;
                default: throw new RuntimeException( "Illegal listener tense: " + listenerTense );
            }
        }

        // Listener firing order should be determined entirely by the sequence
        // of addListener() calls -- regardless of whether they were added via
        // via completed().addListener(), all().addListener(), etc.
        a.set( false, "w" );

        assertEquals( expected, actual );
    }

    @Test
    void derivedVarShouldFireEvenIfMembersDontChange( )
    {
        Var<String> a = new VarBasic<>( "x" );

        ReadableVar<String> b = new ReadableVarDerived<String>( a )
        {
            @Override
            public String v( )
            {
                return a.v( );
            }
        };

        List<VarFiring<String>> fs = new ArrayList<>( );
        b.addListener( ongoing ->
        {
            fs.add( f( ongoing, b.v( ) ) );
        } );

        // The listener should fire 4 times
        a.set( false, "A" );
        a.set(  true, "B" );
        a.set( false, "A" );
        a.set(  true, "B" );

        assertEquals( asList( f( false, "A" ),
                              f(  true, "B" ),
                              f( false, "A" ),
                              f(  true, "B" ) ),
                      fs );
    }

    /**
     * Same as {@link #derivedVarShouldFireEvenIfMembersDontChange()}, but
     * adding a {@link Runnable} instead of a {@link ActivityListener}.
     */
    @Test
    void derivedVarShouldFireEvenIfMembersDontChange2( )
    {
        Var<String> a = new VarBasic<>( "x" );

        ReadableVar<String> b = new ReadableVarDerived<String>( a )
        {
            @Override
            public String v( )
            {
                return a.v( );
            }
        };

        List<String> fs = new ArrayList<>( );
        b.addListener( ( ) ->
        {
            fs.add( b.v( ) );
        } );

        // The listener should fire 4 times
        a.set( false, "A" );
        a.set(  true, "B" );
        a.set( false, "A" );
        a.set(  true, "B" );

        assertEquals( asList( "A",
                              "B",
                              "A",
                              "B" ),
                      fs );
    }

    @Test
    void oldNewShouldFireCompleted( )
    {
        Var<String> a = new VarBasic<>( "x" );

        List<OldNewFiring<String>> fs = new ArrayList<>( );
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
    void varShouldFireEvenIfOngoingHasntChanged( )
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
