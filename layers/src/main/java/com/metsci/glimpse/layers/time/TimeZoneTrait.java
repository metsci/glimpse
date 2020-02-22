/*
 * Copyright (c) 2020, Metron, Inc.
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
