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

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.util.var.Txn.addToActiveTxn;
import static com.metsci.glimpse.util.var2.VarUtils.doAddPairListener;
import static com.metsci.glimpse.util.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.util.var2.VarUtils.filterListenable;
import static com.metsci.glimpse.util.var2.VarUtils.filterListener;
import static com.metsci.glimpse.util.var2.VarUtils.listenable;

import java.util.Set;
import java.util.function.Predicate;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.InvalidValueException;
import com.metsci.glimpse.util.var.TxnMember;

public class VarBasic<V> implements Var<V>
{

    protected final ListenableBasic ongoingRaw;
    protected final ListenableBasic completedRaw;

    protected final Listenable ongoingFiltered;
    protected final Listenable completedFiltered;
    protected final Listenable allFiltered;

    protected final Predicate<? super V> validateFn;
    protected V value;
    protected boolean hasOngoingChanges;
    protected boolean hasTxnMember;


    public VarBasic( V value )
    {
        this( value, v -> true );
    }

    public VarBasic( V value, Predicate<? super V> validateFn )
    {
        this.ongoingRaw = new ListenableBasic( );
        this.completedRaw = new ListenableBasic( );
        Listenable allRaw = listenable( this.ongoingRaw, this.completedRaw );

        this.ongoingFiltered = filterListenable( this.ongoingRaw, this::v );
        this.completedFiltered = filterListenable( this.completedRaw, this::v );
        this.allFiltered = filterListenable( allRaw, this::v );

        this.validateFn = validateFn;
        this.value = this.requireValid( value );
        this.hasOngoingChanges = false;
        this.hasTxnMember = false;
    }

    public boolean isValid( V value )
    {
        return this.validateFn.test( value );
    }

    public V requireValid( V value )
    {
        if ( this.isValid( value ) )
        {
            return value;
        }
        else
        {
            throw new InvalidValueException( "Value was rejected by this Var's validate function: var = " + this + ", value = " + value );
        }
    }

    @Override
    public V v( )
    {
        return this.value;
    }

    @Override
    public boolean set( boolean ongoing, V value )
    {
        if ( ( !ongoing && this.hasOngoingChanges ) || !equal( value, this.value ) )
        {
            this.requireValid( value );

            // If this will be the first change since the start of the current
            // txn, register a TxnMember to handle the possibility of rollback
            if ( !this.hasTxnMember )
            {
                this.hasTxnMember = true;
                V rollbackValue = this.value;
                addToActiveTxn( new TxnMember( )
                {
                    @Override
                    public void rollback( )
                    {
                        VarBasic.this.value = rollbackValue;
                        VarBasic.this.hasTxnMember = false;
                    }

                    @Override
                    public void commit( )
                    {
                        VarBasic.this.hasTxnMember = false;
                    }
                } );
            }

            // Update current value
            this.value = value;

            // Keep track of whether we've seen any ongoing changes since
            // the last completed change -- the current change is either
            // ongoing (set the flag), or completed (clear the flag)
            this.hasOngoingChanges = ongoing;

            // Fire listeners on txn commit
            ( ongoing ? this.ongoingRaw : this.completedRaw ).fire( );

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public Listenable completed( )
    {
        return this.completedFiltered;
    }

    @Override
    public Listenable all( )
    {
        return this.allFiltered;
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            ListenablePairListener listener2 = filterListener( listener, this::v );
            return doAddPairListener( this.ongoingRaw, this.completedRaw, flags2, listener2 );
        } );
    }

}
