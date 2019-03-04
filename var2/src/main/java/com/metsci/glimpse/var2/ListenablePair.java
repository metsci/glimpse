package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.ListenerFlag.EMPTY_FLAGS;
import static com.metsci.glimpse.var2.ListenerFlag.IMMEDIATE;
import static com.metsci.glimpse.var2.ListenerFlag.ONCE;
import static com.metsci.glimpse.var2.ListenerFlag.flags;
import static com.metsci.glimpse.var2.VarUtils.setMinus;

import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;

public interface ListenablePair
{

    /**
     * Statically importable alias for {@link #completed()}.
     */
    static final Function<ListenablePair,Listenable> COMPLETED = ListenablePair::completed;

    /**
     * Statically importable alias for {@link #all()}.
     */
    static final Function<ListenablePair,Listenable> ALL = ListenablePair::all;

    /**
     * Includes notifications for completed events only.
     */
    Listenable completed( );

    /**
     * Includes notifications for both ongoing and completed events.
     */
    Listenable all( );

    /**
     * Includes notifications for ongoing events only.
     * <p>
     * <strong>WARNING:</strong> Don't use this method unless you know what you're
     * doing. It is very hard to use without introducing subtle bugs.
     * <p>
     * Conceptually, ongoing events don't form a complete sequence by themselves.
     * This breaks assumptions behind most usage of this class. For example, consider
     * the following sequence of value updates:
     * <ol>
     * <li>ongoing A
     * <li>completed B
     * <li>ongoing A
     * </ol>
     * If you listen to ongoing events only, you see:
     * <ol>
     * <li>ongoing A
     * <li value="3">ongoing A
     * </ol>
     * This makes it look like event #3 is redundant, and can be ignored. But
     * considering the original sequence, ignoring event #3 is probably a bug.
     * Even worse, the bug is subtle and infrequent (only happens when value #3
     * is identical to value #1). In general, you should avoid this method, and
     * listen to {@link #all()} instead.
     * <p>
     * However, this method is necessary for supporting {@link ListenablePairListener}s.
     */
    @Deprecated
    Listenable ongoing( );

    default Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
    {
        return this.all( ).addListener( flags, listener );
    }

    default Disposable addListener( ListenerFlag flag, Runnable listener )
    {
        return this.addListener( flags( flag ), listener );
    }

    default Disposable addListener( Runnable listener )
    {
        return this.addListener( EMPTY_FLAGS, listener );
    }

    default Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener )
    {
        if ( flags.contains( IMMEDIATE ) )
        {
            listener.run( false );
            if ( flags.contains( ONCE ) )
            {
                return ( ) -> { };
            }
        }

        DisposableGroup disposables = new DisposableGroup( );

        Set<ListenerFlag> flags2 = setMinus( ImmutableSet.copyOf( flags ), IMMEDIATE, ONCE );

        if ( flags.contains( ONCE ) )
        {
            disposables.add( this.ongoing( ).addListener( flags2, ( ) ->
            {
                listener.run( true );
                disposables.dispose( );
                disposables.clear( );
            } ) );

            disposables.add( this.completed( ).addListener( flags2, ( ) ->
            {
                listener.run( false );
                disposables.dispose( );
                disposables.clear( );
            } ) );
        }
        else
        {
            disposables.add( this.ongoing( ).addListener( flags2, ( ) ->
            {
                listener.run( true );
            } ) );

            disposables.add( this.completed( ).addListener( flags2, ( ) ->
            {
                listener.run( false );
            } ) );
        }

        return disposables;
    }

    default Disposable addListener( ListenerFlag flag, ListenablePairListener listener )
    {
        return this.addListener( flags( flag ), listener );
    }

    default Disposable addListener( ListenablePairListener listener )
    {
        return this.addListener( EMPTY_FLAGS, listener );
    }

}
