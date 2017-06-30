package com.metsci.glimpse.util.var;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Notifier<T> implements Listenable<T>
{

    protected final CopyOnWriteArrayList<Consumer<T>> listeners;


    public Notifier( )
    {
        this.listeners = new CopyOnWriteArrayList<>( );
    }

    @Override
    public Disposable addListener( boolean runImmediately, Runnable runnable )
    {
        return this.addListener( runImmediately, ( ev ) -> runnable.run( ) );
    }

    @Override
    public Disposable addListener( boolean runImmediately, Consumer<T> listener )
    {
        if ( runImmediately )
        {
            listener.accept( null );
        }

        this.listeners.add( listener );

        return ( ) ->
        {
            this.listeners.remove( listener );
        };
    }

    public void fire( T ev )
    {
        for ( Consumer<T> listener : this.listeners )
        {
            listener.accept( ev );
        }
    }

}
