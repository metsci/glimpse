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
package com.metsci.glimpse.util.var;

import static com.metsci.glimpse.util.var.Txn.addToActiveTxn;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Var<V> extends Notifier<VarEvent> implements ReadableVar<V>
{

    protected static final VarEvent syntheticEvent = new VarEvent( false );


    public static void doTxn( Runnable task )
    {
        Txn.doTxn( task );
    }

    public static <T> T doTxn( Supplier<T> task )
    {
        return Txn.doTxn( task );
    }


    protected final Predicate<? super V> validateFn;

    protected Var<V> parent;
    protected final Set<Var<V>> children;

    protected V value;
    protected boolean ongoing;
    protected boolean hasTxnMember;


    public Var( V value )
    {
        this( value, v -> true );
    }

    public Var( V value, Predicate<? super V> validateFn )
    {
        this.validateFn = validateFn;

        this.parent = null;
        this.children = new CopyOnWriteArraySet<>( );

        this.value = this.requireValid( value );
        this.ongoing = false;
        this.hasTxnMember = false;
    }

    public boolean isValid( V value )
    {
        return this.root( ).isValidForSubtree( value );
    }

    public V requireValid( V value )
    {
        return this.root( ).requireValidForSubtree( value );
    }

    public void setParent( Var<V> parent )
    {
        if ( this.hasTxnMember )
        {
            throw new RuntimeException( "Var's parent cannot be changed while a txn is pending" );
        }

        if ( this.parent != null )
        {
            this.parent.children.remove( this );
            this.parent = null;
        }

        if ( parent != null )
        {
            // Make sure child state exactly matches parent state, to keep the tree consistent
            if ( this.ongoing != parent.ongoing || !Objects.equals( this.value, parent.value ) )
            {
                this.requireValidForSubtree( parent.value );
                this.setForSubtree( parent.ongoing, parent.value );
                this.fireForSubtree( new VarEvent( parent.ongoing ) );
            }

            this.parent = parent;
            this.parent.children.add( this );
        }
    }

    @Override
    public V v( )
    {
        return this.value;
    }

    public V update( Function<? super V,? extends V> updateFn )
    {
        return this.update( false, updateFn );
    }

    public V update( boolean ongoing, Function<? super V,? extends V> updateFn )
    {
        return this.set( ongoing, updateFn.apply( this.value ) );
    }

    public V set( V value )
    {
        return this.set( false, value );
    }

    public V set( boolean ongoing, V value )
    {
        // Update if value has changed, or if changes were previously ongoing but no longer are
        if ( ( !ongoing && this.ongoing ) || !Objects.equals( value, this.value ) )
        {
            Var<V> root = this.root( );
            root.requireValidForSubtree( value );

            if ( !root.hasTxnMember )
            {
                root.hasTxnMember = true;

                V rollbackValue = root.value;
                boolean rollbackOngoing = root.ongoing;

                addToActiveTxn( new TxnMember( )
                {
                    @Override
                    public void rollback( )
                    {
                        root.setForSubtree( rollbackOngoing, rollbackValue );
                        root.hasTxnMember = false;
                    }

                    @Override
                    public void commit( )
                    {
                        root.hasTxnMember = false;
                    }
                } );
            }

            root.setForSubtree( ongoing, value );

            addToActiveTxn( new TxnMember( )
            {
                @Override
                public void postCommit( )
                {
                    root.fireForSubtree( new VarEvent( ongoing ) );
                }
            } );
        }
        return this.value;
    }

    protected Var<V> root( )
    {
        return ( this.parent == null ? this : this.parent.root( ) );
    }

    protected boolean isValidForSubtree( V value )
    {
        if ( !this.validateFn.test( value ) )
        {
            return false;
        }

        for ( Var<V> child : this.children )
        {
            if ( !child.isValidForSubtree( value ) )
            {
                return false;
            }
        }

        return true;
    }

    protected V requireValidForSubtree( V value )
    {
        if ( !this.validateFn.test( value ) )
        {
            throw new InvalidValueException( "Value was rejected by this Var's validate function: var = " + this + ", value = " + value );
        }

        for ( Var<V> child : this.children )
        {
            child.requireValidForSubtree( value );
        }

        return value;
    }

    protected void setForSubtree( boolean ongoing, V value )
    {
        this.value = value;
        this.ongoing = ongoing;

        for ( Var<V> child : this.children )
        {
            child.setForSubtree( ongoing, value );
        }
    }

    protected void fireForSubtree( VarEvent ev )
    {
        this.fire( ev );

        for ( Var<V> child : this.children )
        {
            child.fireForSubtree( ev );
        }
    }

    /**
     * Overridden so that listeners never receive a null event.
     */
    @Override
    protected VarEvent getSyntheticEvent( )
    {
        return syntheticEvent;
    }

}
