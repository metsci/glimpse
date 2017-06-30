package com.metsci.glimpse.util.var;

import static java.util.Arrays.*;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ListenableGroup<T> implements Listenable<T>
{

    protected final Collection<? extends Listenable<T>> members;


    @SafeVarargs
    public ListenableGroup( Listenable<T>... members )
    {
        this( asList( members ) );
    }

    public ListenableGroup( Collection<? extends Listenable<T>> members )
    {
        this.members = new CopyOnWriteArrayList<>( members );
    }

    @Override
    public Disposable addListener( boolean runImmediately, Runnable runnable )
    {
        return this.addListener( runImmediately, ( ev ) -> runnable.run( ) );
    }

    @Override
    public Disposable addListener( boolean runImmediately, Consumer<T> listener )
    {
        DisposableGroup bindings = new DisposableGroup( );
        for ( Listenable<T> member : this.members )
        {
            bindings.add( member.addListener( runImmediately, listener ) );
        }
        return bindings;
    }

}
