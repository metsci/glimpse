/*
 * Copyright (c) 2020, Metron, Inc.
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

import static com.metsci.glimpse.util.var2.VarUtils.doAddActivityListener;
import static com.metsci.glimpse.util.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.util.var2.VarUtils.listenable;

import java.util.Set;

import com.metsci.glimpse.util.var.Disposable;

public class ActivityListenableBasic implements ActivityListenable
{

    protected final ListenableBasic ongoing;
    protected final ListenableBasic completed;
    protected final Listenable all;


    public ActivityListenableBasic( )
    {
        this.ongoing = new ListenableBasic( );
        this.completed = new ListenableBasic( );
        this.all = listenable( this.ongoing, this.completed );
    }

    public void fire( boolean ongoing )
    {
        ( ongoing ? this.ongoing : this.completed ).fire( );
    }

    @Override
    public Listenable completed( )
    {
        return this.completed;
    }

    @Override
    public Listenable all( )
    {
        return this.all;
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, ActivityListener listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            return doAddActivityListener( this.ongoing, this.completed, flags2, listener );
        } );
    }

}
