package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.ListenerFlag.ONCE;
import static com.metsci.glimpse.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.var2.VarUtils.setMinus;
import static java.util.Arrays.asList;

import java.util.Collection;
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
        this.members = ImmutableSet.copyOf( members );
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            DisposableGroup disposables = new DisposableGroup( );
            if ( flags.contains( ONCE ) )
            {
                Set<ListenerFlag> flags3 = setMinus( ImmutableSet.copyOf( flags ), ONCE );
                Runnable listener2 = ( ) ->
                {
                    listener.run( );
                    disposables.dispose( );
                    disposables.clear( );
                };
                for ( Listenable member : this.members )
                {
                    disposables.add( member.addListener( flags3, listener2 ) );
                }
            }
            else
            {
                for ( Listenable member : this.members )
                {
                    disposables.add( member.addListener( flags2, listener ) );
                }
            }
            return disposables;
        } );
    }

}
