package com.metsci.glimpse.support.wrapped;

import static com.metsci.glimpse.support.wrapped.NoopWrapper1D.NOOP_WRAPPER_1D;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.WrappedAxis1D;

public interface Wrapper1D
{

    /**
     * If {@code axis} is a {@link WrappedAxis1D}, returns a {@link Wrapper1D}
     * representing the wrapping of the axis.
     * <p>
     * If {@code axis} is not a {@link WrappedAxis1D}, returns a {@link Wrapper1D}
     * that will leave input values and delta unchanged.
     */
    public static Wrapper1D getWrapper1D( Axis1D axis )
    {
        if ( axis instanceof WrappedAxis1D )
        {
            WrappedAxis1D wrappedAxis = ( WrappedAxis1D ) axis;
            return new StandardWrapper1D( wrappedAxis.getWrapMin( ), wrappedAxis.getWrapMax( ) );
        }
        else
        {
            return NOOP_WRAPPER_1D;
        }
    }

    /**
     * The mininum edge of this range. For horizontal wrapping, this is the
     * left edge; for vertical it is the bottom.
     * <p>
     * May return an infinite value.
     */
    double wrapMin( );

    /**
     * The maxinum edge of this range. For horizontal wrapping, this is the
     * right edge; for vertical it is the top.
     * <p>
     * May return an infinite value.
     */
    double wrapMax( );

    /**
     * Wraps {@code value} into the range specified by this {@link Wrap}.
     */
    double wrapValue( double value );

    /**
     * Wraps {@code value} so that it is as near as possible to {@code ref}.
     * This may or may not be inside the range specified by this {@link Wrap}.
     * For example, if {@code ref} is near the right edge of the range, then
     * the returned value may be beyond the right edge.
     * <p>
     * This is useful when drawing an item in a wrapped context without getting
     * ugly retrace artifacts if the item crosses the edge of the wrap range.
     * (The typical approach is to draw the item twice: once straddling the max
     * edge, and once straddling the min edge.)
     */
    double wrapNear( double ref, double value );

    /**
     * Returns the apparent distance between two values in a wrapped context.
     * For example, a {@code delta} slightly less than the wrap-span will give
     * a wrapped delta slightly less than zero.
     */
    double wrapDelta( double delta );

    /**
     * For an item occupying coordinates [minValue,maxValue], return a set of
     * coordinate shifts at which the item should be rendered.
     * <p>
     * Common cases:
     * <ol>
     * <li>For an item that does not cross either wrapMin or wrapMax, returns { 0.0 }
     * <li>For an item that crosses wrapMin, returns { 0.0, +wrapSpan }
     * <li>For an item that crosses wrapMax, returns { 0.0, -wrapSpan }
     * </ol>
     * <p>
     * An item may cross both wrapMin and wrapMax. It may also cross the wrap
     * range more than once.
     */
    double[] getRenderShifts( double minValue, double maxValue );

}
