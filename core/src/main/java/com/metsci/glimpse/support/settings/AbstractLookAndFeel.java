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
package com.metsci.glimpse.support.settings;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractLookAndFeel implements LookAndFeel
{
    public static String TOOLTIP_BACKGROUND_COLOR = "tooltip.background.color";
    public static String TOOLTIP_TEXT_COLOR = "tooltip.text.color";

    public static String AXIS_TEXT_COLOR = "axis.text.color";
    public static String AXIS_TICK_COLOR = "axis.tick.color";
    public static String AXIS_TAG_COLOR = "axis.tag.color";

    public static String AXIS_FONT = "axis.font";
    public static String TITLE_FONT = "title.font";

    public static String CROSSHAIR_COLOR = "crosshair.color";
    public static String BORDER_COLOR = "border.color";

    public static String FRAME_BACKGROUND_COLOR = "frame.background.color";
    public static String PLOT_BACKGROUND_COLOR = "plot.background.color";

    public Map<String, Object> map;

    public AbstractLookAndFeel( )
    {
        map = new HashMap<String, Object>( );
    }

    @Override
    public Object getValue( String key )
    {
        return map.get( key );
    }

    @Override
    public float[] getColor( String key )
    {
        return ( float[] ) map.get( key );
    }

    @Override
    public float getFloat( String key )
    {
        return ( Float ) map.get( key );
    }

    @Override
    public Font getFont( String key )
    {
        return ( Font ) map.get( key );
    }
}
