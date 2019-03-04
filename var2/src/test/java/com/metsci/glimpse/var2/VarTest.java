package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarTestUtils.f;
import static com.metsci.glimpse.var2.VarUtils.addOldNewListener;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

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

        a.set(  true, "A" );
        a.set( false, "B" );
        a.set(  true, "A" );

        assertEquals( asList( f(  true, "A" ),
                              f( false, "B" ),
                              f(  true, "A" ) ),
                      fs );
    }

}
