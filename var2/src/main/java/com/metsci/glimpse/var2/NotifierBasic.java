package com.metsci.glimpse.var2;

import static com.metsci.glimpse.util.var.Txn.addToActiveTxn;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.TxnMember;

public class NotifierBasic<T> implements Notifier<T>
{

    protected static class ListenerEntry<T>
    {
        public final ListenerFlagSet flags;
        public final Consumer<? super T> listener;

        public ListenerEntry( ListenerFlagSet flags, Consumer<? super T> listener )
        {
            this.flags = flags;
            this.listener = listener;
        }
    }


    protected final T immediateArg;
    protected final CopyOnWriteArrayList<ListenerEntry<T>> entries;


    public NotifierBasic( T immediateArg )
    {
        this.immediateArg = immediateArg;
        this.entries = new CopyOnWriteArrayList<>( );
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, Consumer<? super T> listener )
    {
        ListenerEntry<T> entry = new ListenerEntry<>( new ListenerFlagSet( flags ), listener );

        if ( entry.flags.immediate )
        {
            entry.listener.accept( this.immediateArg );
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

    public void fire( T t )
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
                for ( ListenerEntry<T> entry : NotifierBasic.this.entries )
                {
                    entry.listener.accept( t );
                    if ( entry.flags.once )
                    {
                        // COW list makes this safe while iterating
                        NotifierBasic.this.entries.remove( entry );
                    }
                }
            }
        } );
    }

}
