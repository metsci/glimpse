package com.metsci.glimpse.layers.time;

import static com.metsci.glimpse.plot.timeline.StackedTimePlot2D.CURRENT_TIME;
import static com.metsci.glimpse.plot.timeline.StackedTimePlot2D.MAX_TIME;
import static com.metsci.glimpse.plot.timeline.StackedTimePlot2D.MIN_TIME;
import static java.util.Arrays.asList;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import com.google.common.base.Objects;
import com.metsci.glimpse.axis.tagged.OrderedConstraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.layers.Trait;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.var.Var;

public class TimeTrait implements Trait
{

    public static final String timeTraitKey = TimeTrait.class.getName( );

    public static void setDefaultTimeExtender( LayeredGui gui, Supplier<? extends TimeTrait> timeExtender )
    {
        gui.setDefaultExtender( timeTraitKey, TimeTrait.class, timeExtender );
    }

    public static void setTimeTrait( LayeredView view, TimeTrait timeTrait )
    {
        view.setTrait( timeTraitKey, timeTrait );
    }

    public static TimeTrait requireTimeTrait( LayeredView view )
    {
        return view.requireTrait( timeTraitKey, TimeTrait.class );
    }


    public final Epoch epoch;

    public final TaggedAxis1D axis;
    protected final Tag selectionMinTag;
    protected final Tag selectionMaxTag;
    protected final Tag selectionCursorTag;

    protected final Var<Trait> parent;


    public TimeTrait( Epoch epoch )
    {
        this.epoch = epoch;

        // XXX: Duplicated from StackedTimePlot2D.addTimeTags()
        this.axis = new TaggedAxis1D( );
        this.selectionMinTag = this.axis.addTag( MIN_TIME, 0 );
        this.selectionMaxTag = this.axis.addTag( MAX_TIME, 10 );
        this.selectionCursorTag = this.axis.addTag( CURRENT_TIME, 10 );
        this.axis.addConstraint( new OrderedConstraint( "order", asList( MIN_TIME, CURRENT_TIME, MAX_TIME ) ) );

        this.parent = new Var<>( null, ( candidate ) ->
        {
            if ( candidate == null )
            {
                return true;
            }
            else if ( candidate instanceof TimeTrait )
            {
                TimeTrait parent = ( TimeTrait ) candidate;
                return Objects.equal( parent.epoch, this.epoch );
            }
            else
            {
                return false;
            }
        } );

        this.parent.addListener( true, ( ) ->
        {
            TimeTrait newParent = ( TimeTrait ) this.parent.v( );
            this.axis.setParent( newParent == null ? null : newParent.axis );
        } );
    }

    @Override
    public Var<Trait> parent( )
    {
        return this.parent;
    }

    @Override
    public TimeTrait createClone( )
    {
        return new TimeTrait( this.epoch );
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

}
