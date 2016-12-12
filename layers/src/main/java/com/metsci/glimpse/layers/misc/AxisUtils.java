package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.axis.tagged.TaggedAxisListener1D.newTaggedAxisListener1D;

import java.util.function.Consumer;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;

public class AxisUtils
{

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and store a reference to the
     * lambda (so that the listener can be removed later).
     */
    public static AxisListener1D addAxisListener1D( Axis1D axis, boolean runImmediately, AxisListener1D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return listener;
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and store a reference to the
     * lambda (so that the listener can be removed later).
     */
    public static TaggedAxisListener1D addTaggedAxisListener1D( TaggedAxis1D axis, boolean runImmediately, Consumer<TaggedAxis1D> tagsUpdatedFn )
    {
        return addTaggedAxisListener1D( axis, runImmediately, newTaggedAxisListener1D( tagsUpdatedFn ) );
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and store a reference to the
     * lambda (so that the listener can be removed later).
     */
    public static TaggedAxisListener1D addTaggedAxisListener1D( TaggedAxis1D axis, boolean runImmediately, TaggedAxisListener1D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return listener;
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and store a reference to the
     * lambda (so that the listener can be removed later).
     */
    public static AxisListener2D addAxisListener2D( Axis2D axis, boolean runImmediately, AxisListener2D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return listener;
    }

}
