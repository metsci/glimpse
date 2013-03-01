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
    
    public void setVisible( boolean visible );
    
    public boolean isVisible( );
    
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
     * Returns the {@code GlimpseLayout} for this plot. This can be used
     * to add subplots of painters to the plotting area.
     */
    public GlimpseAxisLayout2D getLayout( );

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
     * Removes this plot from its StackedPlot2D. This has the same effect
     * as calling StackedPlot2D.deletePlot( this.getId( ) )
     */
    public void deletePlot( );
    
    /**
     * Sets the indentation level of this plot. This can be
     * @param level
     */
    public void setIndentLevel( int level );
    
    public int getIndentLevel( );

    public void setLookAndFeel( LookAndFeel laf );
    
    public void updateLayout( int index );
}