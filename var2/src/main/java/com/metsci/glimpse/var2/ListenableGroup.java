package com.metsci.glimpse.var2;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.metsci.glimpse.var2.ListenerFlag.IMMEDIATE;
import static com.metsci.glimpse.var2.ListenerFlag.ONCE;
import static com.metsci.glimpse.var2.VarUtils.setMinus;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;

public class ListenableGroup implements Listenable
{

    protected final CopyOnWriteArrayList<Listenable> members;


    public ListenableGroup( Listenable... members )
    {
        this( asList( members ) );
    }

    public ListenableGroup( Collection<? extends Listenable> members )
    {
        this.members = new CopyOnWriteArrayList<>( members );
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
