package com.metsci.glimpse.var2;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class Txn
{

    public static interface TxnMember
    {
        /**
         * Implementations of this method must always succeed, and must never
         * throw exceptions.
         */
        void rollback( );

        /**
         * Implementations of this method must always succeed, and must never
         * throw exceptions.
         */
        void commit( );

        /**
         * Implementations of this method may fail, and may throw exceptions.
         * <p>
         * If an impl throws an exception, the {@link Txn}'s post-commit sequence
         * terminates immediately, without performing any post-commit operations
         * for subsequent members.
         */
        void postCommit( );
    }


    protected static final ThreadLocal<LinkedHashSet<TxnMember>> activeTxns = new ThreadLocal<>( );


    protected static void addToActiveTxn( TxnMember member )
    {
        Set<TxnMember> txn = activeTxns.get( );
        if ( txn == null )
        {
            // No active txn, so commit immediately
            member.commit( );
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
