package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.axis.tagged.TaggedAxisListener1D.newTaggedAxisListener1D;

import java.util.function.Consumer;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.util.var.Disposable;

public class AxisUtils
{

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addAxisListener1D( Axis1D axis, boolean runImmediately, AxisListener1D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addTaggedAxisListener1D( TaggedAxis1D axis, boolean runImmediately, Consumer<TaggedAxis1D> tagsUpdatedFn )
    {
        return addTaggedAxisListener1D( axis, runImmediately, newTaggedAxisListener1D( tagsUpdatedFn ) );
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addTaggedAxisListener1D( TaggedAxis1D axis, boolean runImmediately, TaggedAxisListener1D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addAxisListener2D( Axis2D axis, boolean runImmediately, AxisListener2D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

}
