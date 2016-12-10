package com.metsci.glimpse.util.var;

import static com.google.common.collect.Sets.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VarUtils
{

    public static <T> Runnable addElementAddedListener( ReadableVar<? extends Collection<T>> var, boolean runImmediately, Consumer<? super T> listener )
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

    public static <T> Runnable addElementRemovedListener( ReadableVar<? extends Collection<T>> var, boolean runImmediately, Consumer<? super T> listener )
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

    public static <K,V> Runnable addEntryAddedListener( ReadableVar<? extends Map<K,V>> var, boolean runImmediately, BiConsumer<? super K,? super V> listener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private Set<Entry<K,V>> entriesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<Entry<K,V>> entriesNew = new HashSet<>( var.v( ).entrySet( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<Entry<K,V>> entriesAdded = difference( entriesNew, entriesOld );
                for ( Entry<K,V> entry : entriesAdded )
                {
                    listener.accept( entry.getKey( ), entry.getValue( ) );
                }

                this.entriesOld = entriesNew;
            }
        } );
    }

    public static <K,V> Runnable addEntryRemovedListener( ReadableVar<? extends Map<K,V>> var, boolean runImmediately, BiConsumer<? super K,? super V> listener )
    {
        return var.addListener( runImmediately, new Runnable( )
        {
            private Set<Entry<K,V>> entriesOld = emptySet( );

            @Override
            public void run( )
            {
                Set<Entry<K,V>> entriesNew = new HashSet<>( var.v( ).entrySet( ) );

                // difference() returns an unmodifiable view, which is what we want
                Set<Entry<K,V>> entriesRemoved = difference( entriesOld, entriesNew );
                for ( Entry<K,V> entry : entriesRemoved )
                {
                    listener.accept( entry.getKey( ), entry.getValue( ) );
                }

                this.entriesOld = entriesNew;
            }
        } );
    }

}
