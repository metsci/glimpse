package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.Txn.addToActiveTxn;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.var2.Txn.TxnMember;

public class ListenableBasic implements Listenable
{

    protected static class ListenerEntry
    {
        public final ListenerFlagSet flags;
        public final Runnable listener;

        public ListenerEntry( ListenerFlagSet flags, Runnable listener )
        {
            this.flags = flags;
            this.listener = listener;
        }
    }


    protected final CopyOnWriteArrayList<ListenerEntry> entries;


    public ListenableBasic( )
    {
        this.entries = new CopyOnWriteArrayList<>( );
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
    {
        ListenerEntry entry = new ListenerEntry( new ListenerFlagSet( flags ), listener );

        if ( entry.flags.immediate )
        {
            entry.listener.run( );
            if ( entry.flags.once )
            {
                return ( ) -> { };
            }
        }

        this.entries.add( entry );
        this.entries.sort( ( a, b ) ->
        {
            return ( a.flags.order - b.flags.order );
        } );

        return ( ) ->
        {
            this.entries.remove( entry );
        };
    }

    public void fire( )
    {
        addToActiveTxn( new TxnMember( )
        {
            @Override
            public void rollback( )
            {
                // Do nothing
            }

            @Override
            public void commit( )
            {
                // Do nothing
            }

            @Override
            public void postCommit( )
            {
                for ( ListenerEntry entry : ListenableBasic.this.entries )
                {
                    entry.listener.run( );
                    if ( entry.flags.once )
                    {
                        // COW list makes this safe while iterating
                        ListenableBasic.this.entries.remove( entry );
                    }
                }
            }
        } );
    }

}
