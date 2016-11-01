/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.axis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.listener.AxisListener1D;

/**
 * A one dimensional numeric axis. Has minimum and maximum bounds which
 * represent its current extent. Constraints can also be set on the axis which
 * constrain what values the minimum and maximum bounds can take on.</p>
 *
 * Axis1D can be part of a hierarchy of axes which all mirror changes in each
 * other, making it easy to set up a series of linked plots. Axis1D also supports
 * locking its aspect ratio with respect to another (usually orthogonal) axis
 * in order to ensure that the ratio of the scales of the two axes remains consistent.</p>
 *
 * Axis1D are associated with one of two {@link com.metsci.glimpse.layout.GlimpseLayout}
 * implementations: {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D} and
 * {@link com.metsci.glimpse.layout.GlimpseAxisLayout2D}. When a
 * {@link com.metsci.glimpse.painter.base.GlimpsePainter} is added to a
 * {@link com.metsci.glimpse.layout.GlimpseLayout}, it paints based on the
 * bounds provided by the Axis1D associated with the
 * {@link com.metsci.glimpse.layout.GlimpseLayout}.
 *
 * @author ulman
 *
 */
public class Axis1D
{
    public static Logger logger = Logger.getLogger( Axis1D.class.getName( ) );

    // fields are postfixed with either 'value' or 'pixel'
    // referring to whether they hold a value in screen pixels
    // or axis data units

    //// axis linkable state fields ////
    // these values are shared between linked axis

    protected double selectionCenterValue;
    protected double selectionSizeValue;
    protected boolean selectionLocked;

    protected boolean lockMin;
    protected double lockMinValue;
    protected boolean lockMax;
    protected double lockMaxValue;

    protected boolean constrainMinDiff;
    protected double minDiff;
    protected boolean constrainMaxDiff;
    protected double maxDiff;

    protected double absoluteMin;
    protected double absoluteMax;

    protected double minValue;
    protected double maxValue;
    protected double pixelsPerValue;

    protected double mousePosValue;

    protected UpdateMode updateMode;

    //// axis internal state fields ////

    // these values store information about the
    // configuration or state of the axis

    protected double minValue_lastValid;
    protected double maxValue_lastValid;

    protected int axisSizePixels;
    protected boolean initialized;

    protected Set<AxisListener1D> listeners;
    protected Set<Axis1D> children;
    protected Axis1D parentAxis;

    protected Axis1D orthogonalAxis;
    protected double orthogonalAspectRatio;

    protected boolean linkChildren;

    public Axis1D( Axis1D parent )
    {
        this.initialize( parent );
    }

    public Axis1D( )
    {
        this( null );
    }

    protected void initialize( Axis1D parent )
    {
        this.children = new CopyOnWriteArraySet<Axis1D>( );
        this.listeners = new CopyOnWriteArraySet<AxisListener1D>( );

        this.setDefaults( );
        this.setParent( parent );
    }

    @Override
    public Axis1D clone( )
    {
        return new Axis1D( this );
    }

    ////////////////////////////////////////////////////////////
    // Setter Methods                                         //
    ////////////////////////////////////////////////////////////

    /**
     * Ensures that the axis min and max values do not violate any
     * constraints and publishes this axis' values to all linked axes.</p>
     *
     * Should be called after any Axis1D setter methods are called.
     */
    public void validate( )
    {
        applyConstraints( );
        updateLinkedAxes( );
    }

    /**
     * Allows a group of axis which should mirror each other to be defined
     * by placing them in a tree-hierarchy.</p>
     *
     * Whenever this Axis1D or any axis linked to it through its parents or children
     * is modified (and validate() is called to indicate it has been modified),
     * all linked axis are update.</p>
     *
     * @param newParent The new parent for this axis.
     */
    public void setParent( Axis1D newParent )
    {
        setParent( newParent, false );
    }

    public void setParent( Axis1D newParent, boolean duplicateChild )
    {
        // unlink from our current parent
        if ( this.parentAxis != null )
        {
            this.parentAxis.removeChildAxis( this );
        }

        // save a reference to the parent to we can let it know when we change
        this.parentAxis = newParent;

        if ( this.parentAxis != null )
        {
            // register with the parent so that it can let us know when its other children change
            this.parentAxis.addChildAxis( this );

            if ( !duplicateChild )
            {
                // have the parent broadcast its settings
                this.parentAxis.updateLinkedAxes( );
            }
        }

        if ( duplicateChild )
        {
            updateLinkedAxes( );
        }
    }

    public void setLinkChildren( boolean link )
    {
        this.linkChildren = link;
    }

    protected void removeChildAxis( Axis1D child )
    {
        this.children.remove( child );
    }

    protected void addChildAxis( Axis1D child )
    {
        this.children.add( child );
    }

    public Axis1D getParent( )
    {
        return this.parentAxis;
    }

    public Set<Axis1D> getChildren( )
    {
        return Collections.unmodifiableSet( this.children );
    }

    /**
     * Adds a listener which will be notified whenever this axis changes.
     *
     * @param listener
     */
    public void addAxisListener( AxisListener1D listener )
    {
        this.listeners.add( listener );
    }

    /**
     * Removes the provided listener from the list of listeners which are
     * notified when this axis changes.
     *
     * @param listener
     */
    public void removeAxisListener( AxisListener1D listener )
    {
        this.listeners.remove( listener );
    }

    /**
     * Locks the aspect ratio between this axis and a provided orthogonal
     * aspect to a fixed ratio. Whenever either axis updates the other will
     * adjust to maintain the desired aspect.
     *
     * @param orthogonalAxis The axis to remain at a fixed aspect ratio with.
     * @param aspectRatio The aspect ratio to maintain.
     */
    public void lockAspectRatio( Axis1D orthogonalAxis, double aspectRatio )
    {
        // aspect ratio linking will not work correctly in MinMax mode
        // because keeping min and max constant requires adjusting pixelsPerValue
        // which may break locked aspect ratio
        if ( !this.updateMode.isScalePreserving( ) ) this.setUpdateMode( UpdateMode.CenterScale );

        this.orthogonalAxis = orthogonalAxis;
        this.orthogonalAspectRatio = aspectRatio;
    }

    /**
     * Constrains the minimum difference between the axis max and min values.
     * @param diff
     */
    public void setMinSpan( double diff )
    {
        this.minDiff = diff;
        this.constrainMinDiff = true;
    }

    /**
     * Constrains the maximum difference between the axis max and min values.
     * @param diff
     */
    public void setMaxSpan( double diff )
    {
        this.maxDiff = diff;
        this.constrainMaxDiff = true;
    }

    /**
     * If true, the minimum difference set by setMinSpan() is enforced.
     * @param constrain
     */
    public void setConstrainMinSpan( boolean constrain )
    {
        this.constrainMinDiff = constrain;
    }

    /**
     * If true, the maximum difference set by setMaxSpan() is enforced.
     * @param constrain
     */
    public void setConstrainMaxSpan( boolean constrain )
    {
        this.constrainMaxDiff = constrain;
    }

    public boolean isMinSpanConstrained( )
    {
        return this.constrainMinDiff;
    }

    public boolean isMaxSpanConstrained( )
    {
        return this.constrainMaxDiff;
    }

    /**
     * Removes the aspect ratio locking which was established through a call
     * to lockAspectRatio( ).
     */
    public void unlockAspectRatio( )
    {
        this.orthogonalAxis = null;
    }

    /**
     * Locks the minimum value of this axis to a fixed value.
     *
     * @param value
     */
    public void lockMin( double value )
    {
        this.lockMin = true;
        this.lockMinValue = value;
    }

    /**
     * Locks the maximum value of this axis to a fixed value.
     *
     * @param value
     */
    public void lockMax( double value )
    {
        this.lockMax = true;
        this.lockMaxValue = value;
    }

    /**
     * Locks both the minimum and maximum value of this axis to their current values.
     */
    public void lock( )
    {
        this.lockMin( );
        this.lockMax( );
    }

    /**
     * Locks the minimum value of this axis to its current min value.
     */
    public void lockMin( )
    {
        this.lockMin = true;
        this.lockMinValue = this.minValue;
    }

    /**
     * Locks the maximum value of this axis to its current max value.
     */
    public void lockMax( )
    {
        this.lockMax = true;
        this.lockMaxValue = this.maxValue;
    }

    /**
     * Unlocks the minimum an maximum values of this axis.
     */
    public void unlock( )
    {
        this.unlockMin( );
        this.unlockMax( );
    }

    /**
     * Unlocks the minimum value of this axis.
     */
    public void unlockMin( )
    {
        this.lockMin = false;
    }

    /**
     * Unlocks the maximum value of this axis.
     */
    public void unlockMax( )
    {
        this.lockMax = false;
    }

    /**
     * Sets the UpdateMode of this axis. This determines how the axis
     * reacts when it is resized.
     *
     * @param mode
     * @see com.metsci.glimpse.axis.UpdateMode
     */
    public void setUpdateMode( UpdateMode mode )
    {
        this.updateMode = mode;
    }

    /**
     * Sets the minimum value of this axis.
     * @param value
     */
    public void setMin( double value )
    {
        this.minValue = value;
    }

    /**
     * Sets the maximum value of this axis.
     * @param value
     */
    public void setMax( double value )
    {
        this.maxValue = value;
    }

    /**
     * Sets the absolute minimum value of this axis. The minimum value of the
     * axis is allowed to take on any values as long as it does not fall below
     * the absolute minimum.
     * @param min
     */
    public void setAbsoluteMin( double min )
    {
        this.absoluteMin = min;
    }

    /**
     * Sets the absolute maximum value of this axis. The maximum value of the
     * axis is allowed to take on any values as long as it does not rise above
     * the absolute maximum.
     * @param max
     */
    public void setAbsoluteMax( double max )
    {
        this.absoluteMax = max;
    }

    /**
     * Sets the size of the selected range of this axis.
     * @param value
     */
    public void setSelectionSize( double value )
    {
        this.selectionSizeValue = value;
    }

    /**
     * Sets the center of the selected range of this axis.
     * @param value
     */
    public void setSelectionCenter( double value )
    {
        this.selectionCenterValue = value;
    }

    /**
     * Locks the selected region of this axis so that it does not adjust.
     * @param lock
     */
    public void setSelectionLock( boolean lock )
    {
        this.selectionLocked = lock;
    }

    /**
     * Sets the position of the mouse in axis value space.
     * @param value
     */
    public void setMouseValue( double value )
    {
        this.mousePosValue = value;
    }

    /**
     * Sets the pixel size of this axis.
     * @param newSize
     */
    public void setSizePixels( int newSize )
    {
        this.setSizePixels( newSize, true );
    }

    /**
     * Sets the pixel size of this axis. Provides an option to not
     * mark the axis as initialized. This is useful for orthogonal
     * linked axes which must both be initialized before applying
     * constraints.
     *
     * @param newSize
     */
    public void setSizePixels( int newSize, boolean initialize )
    {
        // an axis with pixel size 0 is invalid, ignore the reshape
        if ( newSize == 0 ) return;

        this.axisSizePixels = newSize;
        this.applyUpdateMode( );
        this.recalculatePixelsPerValue0( );
        this.applyConstraints( );

        if ( initialize )
        {
            this.setInitialized( );
        }
    }

    /**
     * Should be called once axisSizePixels has been properly set
     * (usually once the underlying canvas associated with this
     * Axis1D has been drawn).
     */
    public void setInitialized( )
    {
        if ( !this.initialized && this.axisSizePixels > 0 )
        {
            this.initialized = true;
            this.broadcastAxisUpdateUp( );
        }
    }

    ////////////////////////////////////////////////////////////
    // Getter Methods                                         //
    ////////////////////////////////////////////////////////////

    public double getMinSpan( )
    {
        return this.minDiff;
    }

    public double getMaxSpan( )
    {
        return this.maxDiff;
    }

    public Axis1D getLockedAspectAxis( )
    {
        return this.orthogonalAxis;
    }

    public double getLockedAspectRatio( )
    {
        return this.orthogonalAspectRatio;
    }

    public double getLockMin( )
    {
        return lockMinValue;
    }

    public double getLockMax( )
    {
        return lockMaxValue;
    }

    /**
     * @return true if the minimum value of the axis is locked.
     */
    public boolean isLockMin( )
    {
        return lockMin;
    }

    /**
     * @return true if the maximum value of the axis is locked.
     */
    public boolean isLockMax( )
    {
        return lockMax;
    }

    /**
     * @return true if the axis is initialized (and should have a valid
     *         value for its pixel size.
     */
    public boolean isInitialized( )
    {
        return initialized;
    }

    /**
     * @return the axis' UpdateMode
     * @see com.metsci.glimpse.axis.UpdateMode
     */
    public UpdateMode getUpdateMode( )
    {
        return updateMode;
    }

    /**
    * @return the minimum numeric value displayed by the axis.
    */
    public double getMin( )
    {
        return minValue;
    }

    /**
    * @return the maximum numeric value displayed by the axis.
    */
    public double getMax( )
    {
        return maxValue;
    }

    /**
     * @return the minimum value that getMin() can return.
     */
    public double getAbsoluteMin( )
    {
        return absoluteMin;
    }

    /**
     * @return the maximum value that getMax() can return.
     */
    public double getAbsoluteMax( )
    {
        return absoluteMax;
    }

    /**
     * @return the size of the selected area on the axis in axis units.
     */
    public double getSelectionSize( )
    {
        return selectionSizeValue;
    }

    /**
     * @return the center of the selected area on the axis in axis units.
     */
    public double getSelectionCenter( )
    {
        return selectionCenterValue;
    }

    /**
    * @return true if the selected region is locked and not updating as the mouse moves.
    */
    public boolean isSelectionLocked( )
    {
        return selectionLocked;
    }

    /**
    * @return the size of the axis in screen pixels.
    */
    public int getSizePixels( )
    {
        return axisSizePixels;
    }

    /**
     * @return the position of the mouse cursor in axis units.
     */
    public double getMouseValue( )
    {
        return mousePosValue;
    }

    /**
    * @return the number of screen pixels per numeric value.
    */
    public double getPixelsPerValue( )
    {
        return pixelsPerValue;
    }

    /**
    * @return the numeric value corresponding to the given screen pixel.
    */
    public double screenPixelToValue( double pixel )
    {
        return screenPixelToValue0( pixel, pixelsPerValue );
    }

    /**
    * @return the screen pixel corresponding to the given numeric value.
    */
    public int valueToScreenPixel( double value )
    {
        return valueToScreenPixel0( value, pixelsPerValue );
    }

    /**
    * @return the screen pixel corresponding to the given numeric value (w/o truncation).
    */
    public double valueToScreenPixelUnits( double value )
    {
        return valueToScreenPixelUnits0( value, pixelsPerValue );
    }

    ////////////////////////////////////////////////////////////
    // Maintain Constraint Invariants                         //
    ////////////////////////////////////////////////////////////

    /**
     * Adjusts the min and max values of the axis to conform to the
     * various axis constraints like absolute bounds and locked min/max.
     */
    public void applyConstraints( )
    {
        if ( !isInitialized( ) ) return;

        recalculatePixelsPerValue0( );
        applyBoundConstraints( );
        applyDiffConstraints( );
        applyLockConstraints( );

        minValue_lastValid = minValue;
        maxValue_lastValid = maxValue;
    }

    /*
     * Ensures that minValue and maxValue never fall outside of their absolute bounds.
     */
    protected void applyBoundConstraints( )
    {
        if ( minValue < absoluteMin )
        {
            minValue = absoluteMin;
            recalculateMaxValue0( );
        }

        if ( maxValue > absoluteMax )
        {
            maxValue = absoluteMax;
            recalculateMinValue0( );
        }

        if ( minValue < absoluteMin || maxValue > absoluteMax )
        {
            minValue = absoluteMin;
            maxValue = absoluteMax;
            recalculatePixelsPerValue0( );
        }
    }

    /*
     * Ensure that minValue and maxValue never change if they are locked to a particular value.
     */
    protected void applyLockConstraints( )
    {
        double diff = maxValue - minValue;

        if ( lockMin && lockMax )
        {
            minValue = lockMinValue;
            maxValue = lockMaxValue;
        }
        else if ( lockMin )
        {
            minValue = lockMinValue;
            maxValue = minValue + diff;
        }
        else if ( lockMax )
        {
            maxValue = lockMaxValue;
            minValue = maxValue - diff;
        }

        recalculatePixelsPerValue0( );
    }

    protected void applyDiffConstraints( )
    {
        double diff = maxValue - minValue;
        double center = ( maxValue_lastValid - minValue_lastValid ) / 2 + minValue_lastValid;

        if ( constrainMinDiff && diff < minDiff )
        {
            minValue = center - minDiff / 2;
            maxValue = center + minDiff / 2;

            // rounding error can cause ( diff < minDiff ) to still be true, in which case the axis becomes un-pannable
            double newDiff = maxValue - minValue;
            if ( newDiff < minDiff )
            {
                maxValue += Math.max( Math.ulp( maxValue ), minDiff - newDiff );
            }
        }
        else if ( constrainMaxDiff && diff > maxDiff )
        {
            minValue = center - maxDiff / 2;
            maxValue = center + maxDiff / 2;

            // rounding error can cause ( diff > maxDiff ) to still be true, in which case the axis becomes un-pannable
            double newDiff = maxValue - minValue;
            if ( newDiff > maxDiff )
            {
                minValue += Math.max( Math.ulp( minValue ), newDiff - maxDiff );
            }
        }

        // if we have an orthogonal (aspect ratio locked) axis then its diff constraints must apply to us as well
        if ( this.orthogonalAxis != null )
        {
            double orthoginalMinDiff = this.orthogonalAxis.minDiff * this.orthogonalAspectRatio;
            double orthoginalMaxDiff = this.orthogonalAxis.maxDiff * this.orthogonalAspectRatio;

            if ( this.orthogonalAxis.constrainMinDiff && diff < orthoginalMinDiff )
            {
                minValue = center - orthoginalMinDiff / 2;
                maxValue = center + orthoginalMinDiff / 2;
            }
            else if ( this.orthogonalAxis.constrainMaxDiff && diff > orthoginalMaxDiff )
            {
                minValue = center - orthoginalMaxDiff / 2;
                maxValue = center + orthoginalMaxDiff / 2;
            }
        }
    }

    /*
     * Called when the axisSizePixels changes in order to re-adjust either
     * minValue, maxValue, or pixelsPerValue so that they remain consistent
     * with the new axisSizePixels.
     *
     * How this is done is determined by the UpdateMode.
     */
    protected void applyUpdateMode( )
    {
        // a change in the axis size may cause a change in min, max
        // or pixelsPerValue depending on the current update mode
        if ( isInitialized( ) && ! ( lockMax && lockMin ) )
        {
            if ( updateMode == UpdateMode.MinScale )
            {
                if ( lockMax )
                {
                    recalculateMinValue0( );
                }
                else
                {
                    recalculateMaxValue0( );
                }
            }
            else if ( updateMode == UpdateMode.CenterScale )
            {
                if ( lockMax )
                {
                    recalculateMinValue0( );
                }
                else if ( lockMin )
                {
                    recalculateMaxValue0( );
                }
                else
                {
                    recalculateMinMaxValue0( );
                }
            }
            else if ( updateMode == UpdateMode.MinMax )
            {
                recalculatePixelsPerValue0( );
            }
            else if ( updateMode == UpdateMode.FixedPixel )
            {
                double maxPercent = ( maxValue - absoluteMin ) / ( absoluteMax - absoluteMin );
                double minPercent = ( minValue - absoluteMin ) / ( absoluteMax - absoluteMin );
                absoluteMax = absoluteMin + axisSizePixels;

                if ( lockMax )
                {
                    minValue = minPercent * axisSizePixels + absoluteMin;
                }
                else if ( lockMin )
                {
                    maxValue = maxPercent * axisSizePixels + absoluteMin;
                }
                else
                {
                    minValue = minPercent * axisSizePixels + absoluteMin;
                    maxValue = maxPercent * axisSizePixels + absoluteMin;
                }

                recalculatePixelsPerValue0( );
            }
        }
    }

    ////////////////////////////////////////////////////////////
    // Update Linked and Orthogonal Axis                      //
    ////////////////////////////////////////////////////////////

    public void updateLinkedAxes( )
    {
        Set<Axis1D> visited = new HashSet<Axis1D>( );

        broadcastAxisUpdateUp( this, visited );
    }

    public void updateLinkedAxes( Axis1D... ignore )
    {
        Set<Axis1D> visited = new HashSet<Axis1D>( );
        for ( Axis1D axis : ignore )
            visited.add( axis );

        broadcastAxisUpdateUp( this, visited );
    }

    /*
     * Adjusts the min, max, and pixelsPerValue of this axis in order to maintain
     * the provided aspect ratio against the provided axis.
     */
    protected void updateAspectRatio( Axis1D updated, double aspectRatio, Set<Axis1D> visited )
    {
        if ( !isInitialized( ) ) return;

        this.updateAspectRatio( updated, aspectRatio );
        this.broadcastAxisUpdateUp( this, visited );
    }

    protected void updateAspectRatio( Axis1D updated, double aspectRatio )
    {
        double oldPixelsPerValue = getPixelsPerValue( );
        double newPixelsPerValue = updated.getPixelsPerValue( ) * aspectRatio;

        this.minValue = recenterMinValue0( getSizePixels( ) / 2d, oldPixelsPerValue, newPixelsPerValue );
        this.pixelsPerValue = newPixelsPerValue;
        this.recalculateMaxValue0( );

        this.applyConstraints( );
    }

    protected void updateOrthogonalAspectRatio( Set<Axis1D> visited )
    {
        if ( this.orthogonalAxis != null )
        {
            this.orthogonalAxis.updateAspectRatio( this, this.orthogonalAspectRatio, visited );
        }
    }

    /*
     * A utility method which returns the new min value this axis would have to take on in order to stay
     * centered on proposedCenterPixels in pixel space while adjusting the pixelsPerValue from
     * oldPixelsPerValue to newPixelsPerValue.
     */
    protected double recenterMinValue0( double proposedCenterPixels, double oldPixelsPerValue, double newPixelsPerValue )
    {
        double zoomCenterValue;
        double zoomCenterPixels;

        // if the min or max value is locked, zoom centered on that value
        if ( lockMax )
        {
            zoomCenterValue = maxValue;
            zoomCenterPixels = axisSizePixels;
        }
        else if ( lockMin )
        {
            zoomCenterValue = minValue;
            zoomCenterPixels = 0;
        }
        else
        {
            // the position of the zoom center in pixels
            zoomCenterPixels = proposedCenterPixels;
            // the position of the zoom center in value space
            zoomCenterValue = screenPixelToValue0( zoomCenterPixels, oldPixelsPerValue );
        }

        return zoomCenterValue - zoomCenterPixels / newPixelsPerValue;
    }

    /*
     * Update this axis to match the provided axis. If the axis are different
     * sizes, the characteristics of the axis which are preserved (some
     * combination of min, max, and pixelsPerValue) is determined by the current
     * update mode of the axis.
     */
    protected void axisUpdated0( Axis1D axis )
    {
        this.updateMode = axis.getUpdateMode( );

        this.mousePosValue = axis.getMouseValue( );

        this.minDiff = axis.getMinSpan( );
        this.maxDiff = axis.getMaxSpan( );
        this.constrainMinDiff = axis.isMinSpanConstrained( );
        this.constrainMaxDiff = axis.isMaxSpanConstrained( );

        this.lockMin = axis.isLockMin( );
        this.lockMax = axis.isLockMax( );
        this.lockMinValue = axis.getLockMin( );
        this.lockMaxValue = axis.getLockMax( );

        this.absoluteMin = axis.getAbsoluteMin( );
        this.absoluteMax = axis.getAbsoluteMax( );

        this.selectionCenterValue = axis.getSelectionCenter( );
        this.selectionSizeValue = axis.getSelectionSize( );
        this.selectionLocked = axis.isSelectionLocked( );

        // if the axis is not yet initialized, just copy the
        // min, max, and pixelsPerValue and make no attempt
        // to make them self consistent with the axisSizePixels
        // because it hasn't been set yet
        if ( !this.isInitialized( ) )
        {
            this.minValue = axis.getMin( );
            this.maxValue = axis.getMax( );
            this.pixelsPerValue = axis.getPixelsPerValue( );
        }
        // copy the min value and pixelsPerValue and adjust
        // our max value to remain consistent with our axisSizePixels
        else if ( this.updateMode == UpdateMode.MinScale )
        {
            this.minValue = axis.getMin( );
            this.pixelsPerValue = axis.getPixelsPerValue( );
            this.recalculateMaxValue0( );
            this.applyConstraints( );
        }
        // copy the axis center value and pixelsPerValue and adjust
        // our min and max values to remain consistent with our axisSizePixels
        else if ( this.updateMode == UpdateMode.CenterScale )
        {
            this.pixelsPerValue = axis.getPixelsPerValue( );
            double centerValue = ( axis.getMax( ) - axis.getMin( ) ) / 2.0 + axis.getMin( );
            this.recalculateMinMaxValue0( centerValue );
            this.applyConstraints( );
        }
        // copy the min value and max value and adjust our pixelsPerValue
        // to remain consistent with our axisSizePixels
        else if ( this.updateMode == UpdateMode.MinMax )
        {
            this.minValue = axis.getMin( );
            this.maxValue = axis.getMax( );
            this.recalculatePixelsPerValue0( );
            this.applyConstraints( );
        }
        else if ( this.updateMode == UpdateMode.FixedPixel )
        {
            // do nothing
        }
    }

    protected double screenPixelToValue0( double pixel, double pixelsPerValue )
    {
        return pixel / pixelsPerValue + minValue;
    }

    protected int valueToScreenPixel0( double value, double pixelsPerValue )
    {
        return ( int ) Math.rint( tweakUp( valueToScreenPixelUnits0( value, pixelsPerValue ) ) );
    }

    protected double valueToScreenPixelUnits0( double value, double pixelsPerValue )
    {
        return ( value - minValue ) * pixelsPerValue;
    }

    protected void recalculateMinValue0( )
    {
        minValue = maxValue - axisSizePixels / pixelsPerValue;
    }

    protected void recalculateMaxValue0( )
    {
        maxValue = minValue + axisSizePixels / pixelsPerValue;
    }

    protected void recalculatePixelsPerValue0( )
    {
        pixelsPerValue = axisSizePixels / ( maxValue - minValue );
    }

    protected void recalculateMinMaxValue0( double centerValue )
    {
        // ensure that the axis remains centered around its
        // original min and max values even if considerations
        // like aspect ratio locking force the min and max
        // values to adjust
        double axisSizeValue = axisSizePixels / pixelsPerValue;
        minValue = centerValue - axisSizeValue / 2.0;
        maxValue = centerValue + axisSizeValue / 2.0;
    }

    protected void recalculateMinMaxValue0( )
    {
        recalculateMinMaxValue0( getCenterValue( ) );
    }

    protected double getCenterValue( )
    {
        return ( maxValue - minValue ) / 2.0 + minValue;
    }

    protected void broadcastAxisUpdateUp( )
    {
        broadcastAxisUpdateUp( this, new HashSet<Axis1D>( ) );
    }

    protected void broadcastAxisUpdateUp( Axis1D source, Set<Axis1D> visited )
    {
        broadcastAxisUpdateUp0( source, visited );
    }

    // walk up the chain of parents until we reach the top level
    protected void broadcastAxisUpdateUp0( Axis1D source, Set<Axis1D> visited )
    {
        if ( this.parentAxis != null && this.parentAxis.linkChildren )
        {
            parentAxis.broadcastAxisUpdateUp0( source, visited );
        }
        else
        {
            axisUpdated( source, visited );
        }
    }

    // recursively apply the update to all linked children
    protected void axisUpdated( Axis1D source, Set<Axis1D> visited )
    {
        // if we've already been visited, don't revisit and cause a loop
        if ( !visited.add( this ) )
        {
            return;
        }

        // update ourself
        this.axisUpdated0( source );

        // update orthogonal axis
        this.updateOrthogonalAspectRatio( visited );

        // updated our children
        if ( this.linkChildren )
        {
            for ( Axis1D axis : this.children )
            {
                axis.axisUpdated( source, visited );
            }
        }

        // update our listeners
        for ( AxisListener1D child : this.listeners )
        {
            child.axisUpdated( source );
        }
    }

    protected void setDefaults( )
    {
        this.selectionCenterValue = 5.0;
        this.selectionSizeValue = 1.0;
        this.selectionLocked = false;

        this.mousePosValue = 0.0;

        this.minValue = 0.0;
        this.maxValue = 10.0;
        this.pixelsPerValue = 1.0;

        this.minValue_lastValid = 0.0;
        this.maxValue_lastValid = 10.0;

        this.updateMode = UpdateMode.MinMax;

        this.lockMin = false;
        this.lockMax = false;

        this.linkChildren = true;

        this.axisSizePixels = 0;
        this.initialized = false;
        this.absoluteMin = -Double.MAX_VALUE;
        this.absoluteMax = Double.MAX_VALUE;
    }

    // a numerical fix to ensure that tick marks for the start and end point
    // of axes are visible when they should be
    protected static double tweakUp( double val )
    {
        return val + Math.ulp( val );
    }

    @Override
    public String toString( )
    {
        return String.format( "[%.3f %.3f %d]", minValue, maxValue, axisSizePixels );
    }
}
