package com.metsci.glimpse.var2;

import static com.metsci.glimpse.util.var.Txn.addToActiveTxn;

import java.util.Objects;
import java.util.function.Predicate;

import com.metsci.glimpse.util.var.InvalidValueException;
import com.metsci.glimpse.util.var.TxnMember;

public class VarBasic<V> extends ListenablePairBasic implements Var<V>
{

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
        if ( ( !ongoing && this.hasOngoingChanges ) || !Objects.equals( value, this.value ) )
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
            this.fire( ongoing );

            return true;
        }
        else
        {
            return false;
        }
    }

}
