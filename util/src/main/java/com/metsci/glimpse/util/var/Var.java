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

import static com.google.common.base.Objects.*;
import static com.metsci.glimpse.util.PredicateUtils.*;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Var<V> extends Notifier<VarEvent> implements ReadableVar<V>
{

    protected static final VarEvent syntheticEvent = new VarEvent( false );


    protected static final ThreadLocal<Txn> activeTxn = new ThreadLocal<>( );

    public static void doTxn( Runnable task )
    {
        doTxn( ( ) ->
        {
            task.run( );
            return null;
        } );
    }

    public static <T> T doTxn( Supplier<T> task )
    {
        if ( activeTxn.get( ) == null )
        {
            T result;
            Txn txn = new Txn( );
            activeTxn.set( txn );
            try
            {
                result = task.get( );
            }
            catch ( Exception e )
            {
                // Re-throwing the exception will pop us out of the method,
                // so exactly one of rollback() or commit() will be called
                txn.rollback( );
                throw e;
            }
            finally
            {
                activeTxn.set( null );
            }

            // Guaranteed to commit the new values successfully; may then
            // throw an exception while firing listeners, which is okay
            txn.commit( );
            return result;
        }
        else
        {
            // Already inside a txn
            return task.get( );
        }
    }


    protected final Predicate<? super V> validateFn;

    protected Var<V> parent;
    protected final Set<Var<V>> children;

    protected V rollbackValue;
    protected boolean rollbackOngoing;

    protected V value;
    protected boolean ongoing;


    public Var( V value )
    {
        this( value, alwaysTrue );
    }

    public Var( V value, Predicate<? super V> validateFn )
    {
        this.validateFn = validateFn;

        this.parent = null;
        this.children = new CopyOnWriteArraySet<>( );

        this.rollbackValue = null;
        this.rollbackOngoing = false;

        this.value = this.requireValid( value );
        this.ongoing = false;
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
        if ( this.parent != null )
        {
            this.parent.children.remove( this );
            this.parent = null;
        }

        if ( parent != null )
        {
            // Make sure child state exactly matches parent state, to keep the tree consistent
            if ( this.ongoing != parent.ongoing || !equal( this.value, parent.value ) )
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
        if ( ( !ongoing && this.ongoing ) || !equal( value, this.value ) )
        {
            Var<V> root = this.root( );
            root.requireValidForSubtree( value );
            root.setForSubtree( ongoing, value );

            Txn txn = activeTxn.get( );
            if ( txn == null )
            {
                root.commitForSubtree( );
                root.fireForSubtree( new VarEvent( ongoing ) );
            }
            else
            {
                txn.recordSubtreeMod( root, new VarEvent( ongoing ) );
            }
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
            throw new InvalidValueException( this, value );
        }

        for ( Var<V> child : this.children )
        {
            child.requireValidForSubtree( value );
        }

        return value;
    }

    protected void setForSubtree( boolean ongoing, V value )
    {
        this.rollbackValue = this.value;
        this.rollbackOngoing = this.ongoing;

        this.value = value;
        this.ongoing = ongoing;

        for ( Var<V> child : this.children )
        {
            child.setForSubtree( ongoing, value );
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link Txn}.
     */
    protected void commitForSubtree( )
    {
        this.rollbackValue = null;
        this.rollbackOngoing = false;

        for ( Var<V> child : this.children )
        {
            child.commitForSubtree( );
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link Txn}.
     */
    protected void rollbackForSubtree( )
    {
        this.value = this.rollbackValue;
        this.ongoing = this.rollbackOngoing;

        this.rollbackValue = null;
        this.rollbackOngoing = false;

        for ( Var<V> child : this.children )
        {
            child.rollbackForSubtree( );
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link Txn}.
     */
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
