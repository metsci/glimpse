/*
 * Copyright (c) 2016, Metron, Inc.
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
            listener.accept( this.getSyntheticEvent( ) );
        }

        this.listeners.add( listener );

        return ( ) ->
        {
            this.listeners.remove( listener );
        };
    }

    /**
     * Returns an object that should be passed to listeners when listeners need
     * to be fired synthetically -- for example, when calling {@link #addListener(boolean, Consumer)}
     * with {@code runImmediately = true}.
     * <p>
     * Defaults to null.
     */
    protected T getSyntheticEvent( )
    {
        return null;
    }

    public void fire( T ev )
    {
        for ( Consumer<T> listener : this.listeners )
        {
            listener.accept( ev );
        }
    }

}
