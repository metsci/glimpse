package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarTestUtils.f;
import static com.metsci.glimpse.var2.VarUtils.addOldNewListener;
import static com.metsci.glimpse.var2.VarUtils.propertyVar;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

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
