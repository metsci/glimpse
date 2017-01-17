package com.metsci.glimpse.util.var;

import static com.google.common.base.Objects.*;
import static com.metsci.glimpse.util.PredicateUtils.*;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Predicate;

public class Var<V> extends Notifier<VarEvent> implements ReadableVar<V>
{

    protected final Predicate<? super V> validateFn;

    protected Var<V> parent;
    protected final Set<Var<V>> children;

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
            root.fireForSubtree( new VarEvent( ongoing ) );
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

}
