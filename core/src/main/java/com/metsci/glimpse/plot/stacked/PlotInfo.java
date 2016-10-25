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
package com.metsci.glimpse.plot.stacked;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.support.settings.LookAndFeel;

public interface PlotInfo
{
    /**
     * Gets a reference to the parent StackedPlot2D which this PlotInfo
     * belongs to.
     *
     * @return the parent StackedPlot2D
     */
    public StackedPlot2D getStackedPlot( );

    /**
     * Gets the unique identifier assigned to this plot. This identifier can
     * be used to retrieve this plot handle from the StackedPlot2D.
     *
     * @return the plot unique identifier
     */
    public Object getId( );

    /**
     * @return the ordering value for this plot
     */
    public int getOrder( );

    /**
     * @return the pixel size of this plot
     */
    public int getSize( );

    /**
     * Sets the ordering of this plot relative to the other plots in the
     * StackedPlot2D. The particular value does not matter, only the
     * values relative to other plots. All plots start with order 0.
     * Plots with the same order value are arranged in the order they
     * were added to the StackedPlot2D.
     *
     * @param order the ordering value for this plot
     */
    public void setOrder( int order );

    /**
     * <p>Sets the size in pixels for this plot. If {@code size < 0}, then
     * the plot will attempt to fill all available space, sharing space
     * evenly with other plots with negative size.</p>
     *
     * <p>For a VERTICAL oriented plot, {@code setSize( )} adjusts the plot
     * height, for a HORIZONTAL oriented plot, the width is adjusted.</p>
     *
     * @param size the size of the plot in pixels.
     */
    public void setSize( int size );

    /**
     * <p>Sets the plot to fill all available space. First, fixed size plots
     * are given their space (set by {@link #setSize(int)}) and then
     * all plots set to grow fill the remaining space.</p>
     *
     * <p>Setting the plot size to a negative value is the same as setting
     * grow to true (but this behavior is deprecated and {@link #setGrow(boolean)}
     * should be used). Setting size to a positive value will set grow to
     * false.</p>
     * @param grow
     */
    public void setGrow( boolean grow );

    /**
     * @see #setGrow(boolean)
     */
    public boolean isGrow( );

    /**
     * If true, the PlotInfo will be drawn as a row on the timeline, if false the
     * PlotInfo will be hidden.
     */
    public void setVisible( boolean visible );

    /**
     * @see #setVisible(boolean)
     */
    public boolean isVisible( );

    public boolean isExpanded( );

    public void setParent( PlotInfo parent );

    public PlotInfo getParent( );

    /**
     * Sets the spacing between this plot and those above and below it. This value
     * overrides the default set via {@link StackedTimePlot2D#setPlotSpacing(int)}.
     * This controls the space between this plot and the one above or to the right
     * of it. The spacing between this plot and the one below or to the left is
     * controlled by the other plots spacing.
     */
    public void setPlotSpacing( int spacing );

    /**
     * @see #setPlotSpacing(int)
     */
    public int getPlotSpacing( );

    /**
     * Returns the {@code GlimpseAxisLayout2D} for this plot. This can be used
     * to add subplots of painters to the plotting area.
     */
    public GlimpseAxisLayout2D getLayout( );

    /**
     * Returns the base {@code GlimpseLayout}. If this plot is made of a single GlimpseLayout,
     * {@code #getLayout()} and {@code #getBaseLayout()} will return the same GlimpseLayout.
     * Otherwise, the result of {@code #getLayout()} will be a child of {@code #getBaseLayout()}.
     * Further, {@code #getBaseLayout()} will be a direct child of the {@link StackedPlot2D}
     * that this {@code PlotInfo} is part of.
     */
    public GlimpseAxisLayout2D getBaseLayout( );

    /**
     * Returns the common axis associated with the given GlimpseTargetStack.
     * Users generally should simply call {@link #getCommonAxis()}.
     */
    public Axis1D getCommonAxis( GlimpseTargetStack stack );

    /**
     * Returns the data axis associated with the given GlimpseTargetStack.
     * Users generally should simply call {@link #getOrthogonalAxis()}.
     */
    public Axis1D getOrthogonalAxis( GlimpseTargetStack stack );

    /**
     * Returns the common axis shared by all the plots in a StackedPlot2D.
     *
     * @return the shared axis for this plot
     */
    public Axis1D getCommonAxis( );

    /**
     * Returns the data axis associated with this plot. The data axes for
     * each GlimpseLayout in a StackedPlot2D are unlinked by default, but
     * they can be linked if desired using {@link com.metsci.glimpse.axis.Axis1D#setParent( Axis1D )}.
     *
     * @return the data axis for this plot
     */
    public Axis1D getOrthogonalAxis( );

    /**
     * Adds the childLayout to the part of the StackedPlot2D represented
     * by this LayoutInfo. Also links the common axis of the child to the
     * common axis of the parent layout.
     *
     * @param childLayout
     */
    public void addLayout( GlimpseAxisLayout2D childLayout );

    /**
     * @deprecated {@link #removePlot()}
     */
    public void deletePlot( );

    /**
     * Removes this plot from its StackedPlot2D. This has the same effect
     * as calling StackedPlot2D.removePlot( this.getId( ) )
     */
    public void removePlot( );

    /**
     * Sets the indentation level of this plot. This can be
     * @param level
     */
    public void setIndentLevel( int level );

    /**
     *
     * @see #setIndentLevel(int)
     */
    public int getIndentLevel( );

    /**
     * <p>Sets the MIG Layout constraints which position this PlotInfo within the
     * StackedPlot2D. Normally, calling this method is not necessary because
     * the StackedPlot2D automatically positions its PlotInfo. However, this method
     * can be used to override this default position.</p>
     *
     * <p>Calling {@link #setLayoutData(String)} with {@code null} as the argument
     * will cause the StackedPlot2D to resume automatically positioning this PlotInfo.
     *
     * @param layoutData
     */
    public void setLayoutData( String layoutData );

    /**
     *
     * @see #setLayoutData(String)
     */
    public String getLayoutData( );

    public void setLookAndFeel( LookAndFeel laf );

    public void updateLayout( int index );
}