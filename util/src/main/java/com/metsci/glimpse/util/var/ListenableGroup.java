package com.metsci.glimpse.util.var;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Collection;
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
        this.members = new ArrayList<>( members );
    }

    @Override
    public Runnable addListener( boolean runImmediately, Runnable runnable )
    {
        for ( Listenable<T> member : this.members )
        {
            member.addListener( runImmediately, runnable );
        }
        return runnable;
    }

    @Override
    public void removeListener( Runnable runnable )
    {
        for ( Listenable<T> member : this.members )
        {
            member.removeListener( runnable );
        }
    }

    @Override
    public Consumer<T> addListener( boolean runImmediately, Consumer<T> consumer )
    {
        for ( Listenable<T> member : this.members )
        {
            member.addListener( runImmediately, consumer );
        }
        return consumer;
    }

    @Override
    public void removeListener( Consumer<T> consumer )
    {
        for ( Listenable<T> member : this.members )
        {
            member.removeListener( consumer );
        }
    }

}
