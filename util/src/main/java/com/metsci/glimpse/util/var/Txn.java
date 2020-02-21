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
