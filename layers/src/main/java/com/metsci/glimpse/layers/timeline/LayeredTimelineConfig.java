package com.metsci.glimpse.layers.timeline;

import static com.metsci.glimpse.plot.timeline.StackedTimePlot2D.CURRENT_TIME;
import static com.metsci.glimpse.plot.timeline.StackedTimePlot2D.MAX_TIME;
import static com.metsci.glimpse.plot.timeline.StackedTimePlot2D.MIN_TIME;
import static com.metsci.glimpse.util.PredicateUtils.require;
import static java.util.Arrays.asList;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import com.google.common.base.Objects;
import com.metsci.glimpse.axis.tagged.OrderedConstraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.layers.LayeredViewConfig;
import com.metsci.glimpse.plot.timeline.data.Epoch;

public class LayeredTimelineConfig implements LayeredViewConfig
{

    public static final String timelineConfigKey = LayeredTimelineConfig.class.getName( );

    public static void setDefaultTimelineConfigurator( LayeredGui gui, Supplier<? extends LayeredTimelineConfig> timelineConfigurator )
    {
        gui.setDefaultViewConfigurator( timelineConfigKey, LayeredTimelineConfig.class, timelineConfigurator );
    }

    public static void setTimelineConfig( LayeredView view, LayeredTimelineConfig timelineConfig )
    {
        view.setConfig( timelineConfigKey, timelineConfig );
    }

    public static LayeredTimelineConfig requireTimelineConfig( LayeredView view )
    {
        return view.requireConfig( timelineConfigKey, LayeredTimelineConfig.class );
    }


    public final Epoch epoch;

    public final TaggedAxis1D axis;
    protected final Tag selectionMinTag;
    protected final Tag selectionMaxTag;
    protected final Tag selectionCursorTag;

    protected LayeredTimelineConfig parent;


    public LayeredTimelineConfig( Epoch epoch )
    {
        this.epoch = epoch;

        // XXX: Duplicated from StackedTimePlot2D.addTimeTags()
        this.axis = new TaggedAxis1D( );
        this.selectionMinTag = this.axis.addTag( MIN_TIME, 0 );
        this.selectionMaxTag = this.axis.addTag( MAX_TIME, 10 );
        this.selectionCursorTag = this.axis.addTag( CURRENT_TIME, 10 );
        this.axis.addConstraint( new OrderedConstraint( "order", asList( MIN_TIME, CURRENT_TIME, MAX_TIME ) ) );

        this.parent = null;
    }

    public void setRelativeBounds( DoubleUnaryOperator unitsToSeconds, double min_UNITS_SINCE_EPOCH, double max_UNITS_SINCE_EPOCH )
    {
        this.axis.setMin( unitsToSeconds.applyAsDouble( min_UNITS_SINCE_EPOCH ) );
        this.axis.setMax( unitsToSeconds.applyAsDouble( max_UNITS_SINCE_EPOCH ) );
        this.axis.validate( );
    }

    public void setRelativeSelection( DoubleUnaryOperator unitsToSeconds, double min_UNITS_SINCE_EPOCH, double max_UNITS_SINCE_EPOCH )
    {
        double min_SEC_SINCE_EPOCH = unitsToSeconds.applyAsDouble( min_UNITS_SINCE_EPOCH );
        double max_SEC_SINCE_EPOCH = unitsToSeconds.applyAsDouble( max_UNITS_SINCE_EPOCH );
        double cursor_SEC_SINCE_EPOCH = max_SEC_SINCE_EPOCH;

        this.selectionMinTag.setValue( min_SEC_SINCE_EPOCH );
        this.selectionMaxTag.setValue( max_SEC_SINCE_EPOCH );
        this.selectionCursorTag.setValue( cursor_SEC_SINCE_EPOCH );
        this.axis.validateTags( );
    }

    public long selectionMin_PMILLIS( )
    {
        return this.epoch.toPosixMillis( this.selectionMinTag.getValue( ) );
    }

    public long selectionMax_PMILLIS( )
    {
        return this.epoch.toPosixMillis( this.selectionMaxTag.getValue( ) );
    }

    public long selectionCursor_PMILLIS( )
    {
        return this.epoch.toPosixMillis( this.selectionCursorTag.getValue( ) );
    }

    @Override
    public boolean allowsParent( LayeredViewConfig newParent )
    {
        if ( newParent == null )
        {
            return true;
        }
        else if ( newParent instanceof LayeredTimelineConfig )
        {
            LayeredTimelineConfig parent = ( LayeredTimelineConfig ) newParent;
            return Objects.equal( parent.epoch, this.epoch );
        }
        else
        {
            return false;
        }
    }

    @Override
    public void setParent( LayeredViewConfig newParent )
    {
        require( newParent, this::allowsParent );

        this.parent = ( LayeredTimelineConfig ) newParent;
        this.axis.setParent( this.parent == null ? null : this.parent.axis );
    }

    @Override
    public LayeredTimelineConfig getParent( )
    {
        return this.parent;
    }

}
