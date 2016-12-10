package com.metsci.glimpse.util.var;

import static com.google.common.collect.Sets.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class VarUtils
{

    public static <T> Runnable addElementAddedListener( Var<? extends Collection<T>> var, boolean runImmediately, Consumer<? super T> listener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private Set<T> valuesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<T> valuesNew = new HashSet<>( var.v( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<T> valuesAdded = difference( valuesNew, valuesOld );
                for ( T value : valuesAdded )
                {
                    listener.accept( value );
                }

                this.valuesOld = valuesNew;
            }
        } );
    }

    public static <T> Runnable addElementRemovedListener( Var<? extends Collection<T>> var, boolean runImmediately, Consumer<? super T> listener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private Set<T> valuesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<T> valuesNew = new HashSet<>( var.v( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<T> valuesRemoved = difference( valuesOld, valuesNew );
                for ( T value : valuesRemoved )
                {
                    listener.accept( value );
                }

                this.valuesOld = valuesNew;
            }
        } );
    }

}
