/*
 * Copyright (c) 2019, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.util.var2;

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
