package com.metsci.glimpse.layers.time;

import static com.metsci.glimpse.util.PredicateUtils.notNull;

import java.time.ZoneId;

import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.Trait;
import com.metsci.glimpse.layers.View;
import com.metsci.glimpse.util.var.Var;

public class TimeZoneTrait extends Trait
{

    public static final String timeZoneTraitKey = TimeZoneTrait.class.getName( );

    public static void addTimeZoneLinkage( LayeredGui gui, String name, TimeZoneTrait master )
    {
        gui.addLinkage( timeZoneTraitKey, name, master );
    }

    public static void setTimeZoneTrait( View view, TimeZoneTrait timeZoneTrait )
    {
        view.setTrait( timeZoneTraitKey, timeZoneTrait );
    }

    public static TimeZoneTrait requireTimeZoneTrait( View view )
    {
        return view.requireTrait( timeZoneTraitKey, TimeZoneTrait.class );
    }


    public final Var<ZoneId> timeZone;


    public TimeZoneTrait( boolean isLinkage, String zoneId )
    {
        this( isLinkage, ZoneId.of( zoneId ) );
    }

    public TimeZoneTrait( boolean isLinkage, ZoneId zoneId )
    {
        this( isLinkage );
        this.timeZone.set( zoneId );
    }

    public TimeZoneTrait( boolean isLinkage )
    {
        super( isLinkage );

        this.timeZone = new Var<>( ZoneId.of( "UTC" ), notNull );

        this.parent.addListener( true, ( ) ->
        {
            TimeZoneTrait newParent = ( TimeZoneTrait ) this.parent.v( );
            this.timeZone.setParent( newParent == null ? null : newParent.timeZone );
        } );
    }

    @Override
    protected boolean isValidParent( Trait linkage )
    {
        return ( linkage instanceof TimeZoneTrait );
    }

    @Override
    public TimeZoneTrait copy( boolean isLinkage )
    {
        TimeZoneTrait copy = new TimeZoneTrait( isLinkage );

        copy.timeZone.set( this.timeZone.v( ) );

        return copy;
    }

}
