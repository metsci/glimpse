package com.metsci.glimpse.var2;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.metsci.glimpse.var2.ListenerFlag.IMMEDIATE;
import static com.metsci.glimpse.var2.ListenerFlag.ONCE;
import static com.metsci.glimpse.var2.VarUtils.setMinus;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;

public class ListenableSet implements Listenable
{

    protected final ImmutableSet<Listenable> members;


    public ListenableSet( Listenable... members )
    {
        this( asList( members ) );
    }

    public ListenableSet( Collection<? extends Listenable> members )
    {
        this.members = ImmutableSet.copyOf( findLeafListenables( members ) );
    }

    protected static Set<Listenable> findLeafListenables( Collection<? extends Listenable> listenables )
    {
        Set<Listenable> results = new LinkedHashSet<>( );
        addLeafListenables( listenables, results );
        return results;
    }

    protected static void addLeafListenables( Collection<? extends Listenable> listenables, Set<Listenable> results )
    {
        for ( Listenable listenable : listenables )
        {
            if ( listenable instanceof ListenableSet )
            {
                addLeafListenables( ( ( ListenableSet ) listenable ).members, results );
            }
            else
            {
                results.add( listenable );
            }
        }
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
    {
        if ( flags.contains( IMMEDIATE ) )
        {
            listener.run( );
            if ( flags.contains( ONCE ) )
            {
                return ( ) -> { };
            }
        }

        DisposableGroup disposables = new DisposableGroup( );

        Set<ListenerFlag> flags2 = setMinus( copyOf( flags ), IMMEDIATE, ONCE );

        Runnable listener2 = ( !flags.contains( ONCE ) ? listener : ( ) ->
        {
            listener.run( );
            disposables.dispose( );
            disposables.clear( );
        } );

        for ( Listenable member : this.members )
        {
            disposables.add( member.addListener( flags2, listener2 ) );
        }

        return disposables;
    }

}
