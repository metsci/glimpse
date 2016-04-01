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
package com.metsci.glimpse.event.touch;

import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * Gesture implementations vary, but this event implementation should
 * be flexible enough for most platforms. Just like GlimpseMouseEvent,
 * this event class does not update a previous event. Gestures events
 * can be thought of as updates to a single gesture.  But we will
 * treat each event as self-contained (e.g. a lon pan is comprised of
 * multiple small pan events with small dx/dy).
 *
 * <p>
 * The source string can contain generic information about the source
 * of the touch.
 * </p>
 *
 * <p>
 * There is a reference point x,y for each event. For tap/long press
 * this is where the finger pressed the screen (in pixels).
 * For pan/swipe, this is where the finger was at the start of this
 * event. For pinch, this is the center X coordinate.
 * </p>
 */
public class GlimpseGestureEvent
{
    protected String source;
    protected GlimpseTargetStack stack;

    protected int x;
    protected int y;

    protected boolean handled;

    public GlimpseGestureEvent( String source, GlimpseTargetStack stack, int x, int y )
    {
        this.source = source;
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    /**
     * Can be some information about where the gesture/touch event came
     * from.
     */
    public String getSource( )
    {
        return source;
    }

    public GlimpseTargetStack getTargetStack( )
    {
        return stack;
    }

    /**
     * A reference point X for the touch event. See the class comments.
     */
    public int getX( )
    {
        return x;
    }

    /**
     * A reference point Y for the touch event. See the class comments.
     */
    public int getY( )
    {
        return y;
    }

    public void setHandled( boolean handled )
    {
        this.handled = handled;
    }

    public boolean isHandled( )
    {
        return handled;
    }

    public GlimpseGestureEvent withNewTarget( GlimpseTargetStack targetStack, int x, int y )
    {
        return new GlimpseGestureEvent( source, targetStack, x, y );
    }
}
