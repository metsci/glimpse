package com.metsci.glimpse.util.var;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class Txn
{

    protected static final ThreadLocal<LinkedHashSet<TxnMember>> activeTxns = new ThreadLocal<>( );

    /**
     * Generally, this method shouldn't be called directly from application
     * code. It is public to allow new utilities to be implemented in ways
     * that use the {@link Txn} mechanism.
     */
    public static void addToActiveTxn( TxnMember member )
    {
        Set<TxnMember> txn = activeTxns.get( );
        if ( txn == null )
        {
            // No active txn, so commit immediately
            member.commit( );
            member.postCommit( );
        }
        else
        {
            // Defer until commit or rollback of active txn
            txn.add( member );
        }
    }

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
        if ( activeTxns.get( ) != null )
        {
            // Already inside a txn
            return task.get( );
        }
        else
        {
            LinkedHashSet<TxnMember> txn = new LinkedHashSet<>( );
            T result;

            activeTxns.set( txn );
            try
            {
                result = task.get( );

                // By general contract, commit() must always succeed
                for ( TxnMember member : txn )
                {
                    member.commit( );
                }
            }
            catch ( Exception e )
            {
                // By general contract, rollback() must always succeed
                for ( TxnMember member : txn )
                {
                    member.rollback( );
                }

                // Re-throwing the exception here pops us out of the method
                throw e;
            }
            finally
            {
                activeTxns.set( null );
            }

            // An exception from postCommit() will cause this method to terminate
            // immediately, without calling postCommit() for subsequent members
            for ( TxnMember member : txn )
            {
                member.postCommit( );
            }

            return result;
        }
    }

}
