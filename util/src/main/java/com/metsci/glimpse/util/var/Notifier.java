package com.metsci.glimpse.util.var;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Notifier<T> implements Listenable<T>
{

    protected class RunnableWrapper
    {
        public final Runnable runnable;
        public final Consumer<T> consumer;
        public int count;

        public RunnableWrapper( Runnable runnable )
        {
            this.runnable = runnable;
            this.consumer = ( ev ) -> runnable.run( );
            this.count = 0;
        }
    }


    protected final Map<Runnable,RunnableWrapper> runnableWrappers;
    protected final CopyOnWriteArrayList<Consumer<T>> consumers;


    public Notifier( )
    {
        this.runnableWrappers = new HashMap<>( );
        this.consumers = new CopyOnWriteArrayList<>( );
    }

    @Override
    public Runnable addListener( boolean runImmediately, Runnable runnable )
    {
        RunnableWrapper wrapper = this.runnableWrappers.computeIfAbsent( runnable, RunnableWrapper::new );
        wrapper.count += 1;

        this.addListener( runImmediately, wrapper.consumer );

        return runnable;
    }

    @Override
    public void removeListener( Runnable runnable )
    {
        RunnableWrapper wrapper = this.runnableWrappers.get( runnable );
        wrapper.count -= 1;
        if ( wrapper.count <= 0 )
        {
            this.runnableWrappers.remove( runnable );
        }

        this.removeListener( wrapper.consumer );
    }

    @Override
    public Consumer<T> addListener( boolean runImmediately, Consumer<T> consumer )
    {
        if ( runImmediately )
        {
            consumer.accept( null );
        }

        this.consumers.add( consumer );
        return consumer;
    }

    @Override
    public void removeListener( Consumer<T> consumer )
    {
        this.consumers.remove( consumer );
    }

    public void fire( T ev )
    {
        for ( Consumer<T> consumer : this.consumers )
        {
            consumer.accept( ev );
        }
    }

}
