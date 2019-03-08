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
package com.metsci.glimpse.util.var2;

import static com.metsci.glimpse.util.var2.ListenerFlag.EMPTY_FLAGS;
import static com.metsci.glimpse.util.var2.ListenerFlag.flags;

import java.util.Set;
import java.util.function.Function;

import com.metsci.glimpse.util.var.Disposable;

public interface ListenablePair
{

    /**
     * Statically importable alias for {@link #completed()}.
     */
    static final Function<ListenablePair,Listenable> COMPLETED = ListenablePair::completed;

    /**
     * Statically importable alias for {@link #all()}.
     */
    static final Function<ListenablePair,Listenable> ALL = ListenablePair::all;

    /**
     * Includes notifications for completed events only.
     */
    Listenable completed( );

    /**
     * Includes notifications for both ongoing and completed events.
     */
    Listenable all( );

    /**
     * Includes notifications for ongoing events only.
     * <p>
     * <strong>WARNING:</strong> Don't use this method unless you know what you're
     * doing. It is very hard to use without introducing subtle bugs.
     * <p>
     * Conceptually, ongoing events don't form a complete sequence by themselves.
     * This breaks assumptions behind most usage of this class. For example, consider
     * the following sequence of value updates:
     * <ol>
     * <li>ongoing A
     * <li>completed B
     * <li>ongoing A
     * </ol>
     * If you listen to ongoing events only, you see:
     * <ol>
     * <li>ongoing A
     * <li value="3">ongoing A
     * </ol>
     * This makes it look like event #3 is redundant, and can be ignored. But
     * considering the original sequence, ignoring event #3 is probably a bug.
     * Even worse, the bug is subtle and infrequent (only happens when value #3
     * is identical to value #1). In general, you should avoid this method, and
     * listen to {@link #all()} instead.
     * <p>
     * However, this method is necessary for supporting {@link ListenablePairListener}s.
     */
    @Deprecated
    Listenable ongoing( );

    default Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
    {
        return this.all( ).addListener( flags, listener );
    }

    default Disposable addListener( ListenerFlag flag, Runnable listener )
    {
        return this.addListener( flags( flag ), listener );
    }

    default Disposable addListener( Runnable listener )
    {
        return this.addListener( EMPTY_FLAGS, listener );
    }

    Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener );

    default Disposable addListener( ListenerFlag flag, ListenablePairListener listener )
    {
        return this.addListener( flags( flag ), listener );
    }

    default Disposable addListener( ListenablePairListener listener )
    {
        return this.addListener( EMPTY_FLAGS, listener );
    }

}