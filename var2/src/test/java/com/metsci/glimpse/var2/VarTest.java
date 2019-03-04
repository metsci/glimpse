package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarTestUtils.f;
import static com.metsci.glimpse.var2.VarUtils.addOldNewListener;
import static com.metsci.glimpse.var2.VarUtils.listenablePair;
import static com.metsci.glimpse.var2.VarUtils.mapValueVar;
import static com.metsci.glimpse.var2.VarUtils.propertyVar;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

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
    void listenableSetShouldDedupeDeps( )
    {
        Var<ImmutableMap<String,String>> map1 = new VarBasic<>( ImmutableMap.of( "A", "m1vA", "B", "m1vB" ) );
        Var<ImmutableMap<String,String>> map2 = new VarBasic<>( ImmutableMap.of( "A", "m2vA", "B", "m2vB" ) );
        Var<String> key = new VarBasic<>( "x" );

        Var<String> value1 = mapValueVar( map1, key );
        Var<String> value2 = mapValueVar( map2, key );

        ListenablePair eitherValue = listenablePair( value1, value2 );

        List<Object> fs = new ArrayList<>( );
        eitherValue.addListener( ( ) ->
        {
            fs.add( new Object( ) );
        } );

        // When we set the key, the listener should only run once,
        // even though both value1 and value2 depend on key
        key.set( "A" );

        assertEquals( 1, fs.size( ) );
    }

    @Test
    void derivedVarShouldFireIffDerivedValueChanges( )
    {
        Var<String> a = new VarBasic<>( "AAAA" );
        ReadableVar<Integer> b = propertyVar( a, s -> s.length( ) );

        List<Object> fs = new ArrayList<>( );
        b.addListener( ( ) ->
        {
            fs.add( new Object( ) );
        } );

        // String length stays the same, so listener shouldn't run
        a.set( "BBBB" );
        assertEquals( 0, fs.size( ) );

        // String length changes, so listener should run
        a.set( "B" );
        assertEquals( 1, fs.size( ) );
    }

}
