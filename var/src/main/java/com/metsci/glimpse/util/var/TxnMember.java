package com.metsci.glimpse.util.var;

public interface TxnMember
{

    /**
     * Implementations of this method must always succeed, and must never
     * throw exceptions.
     */
    default void rollback( )
    {
        // By default, do nothing
    }

    /**
     * Implementations of this method must always succeed, and must never
     * throw exceptions.
     */
    default void commit( )
    {
        // By default, do nothing
    }

    /**
     * Implementations of this method may fail, and may throw exceptions.
     * <p>
     * If an impl throws an exception, the {@link Txn}'s post-commit sequence
     * terminates immediately, without performing any post-commit operations
     * for subsequent members.
     */
    default void postCommit( )
    {
        // By default, do nothing
    }

}
