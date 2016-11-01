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
package com.metsci.glimpse.context;

import java.util.List;

import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * <p>GlimpseTarget represents a location to which GlimpsePainters may be drawn. GlimpseTargets may either
 * be heavyweight instances of GlimpseCanvas or lightweight instances of GlimpseLayout representing
 * a subsection of a parent GlimpseLayout or GlimpseCanvas.</p>
 *
 * <p>GlimpseTargets may be nested (child GlimpseTargets are retrieved via getTargetChildren()). A single GlimpseTarget
 * may have multiple parents, thus no getParent() method is provided. Because of this, a GlimpseTarget
 * does not have a single size. Instead, it maintains a size for every parent hierarchy which it is
 * part of. Therefore, in order to get the GlimpseBounds of a GlimpseTarget, a GlimpseLayoutStack must
 * be provided. The GlimpseLayoutStack specifies the parent hierarchy to provide a size for.</p>
 *
 * @author ulman
 */
public interface GlimpseTarget
{
    /**
     * If true, the GlimpseTarget should be drawn on the screen and mouse events
     * should be dispatched for it. Otherwise it is invisible and mouse events should not
     * fire for the GlimpseTarget or its children.
     */
    public boolean isVisible( );

    /**
     * Sets whether this GlimpseTarget hides events from GlimpseTargets under it. This
     * value does not determine whether or not the GlimpseTarget will generate
     * GlimpseMouseEvents (see {@link #isEventGenerator()).
     *
     * @return whether this target hides events from targets under it
     */
    public boolean isEventConsumer( );

    /**
     * Set whether or not this GlimpseTarget will consume or pass through mouse events.
     *
     * @param consume
     */
    public void setEventConsumer( boolean consume );

    /**
     * Sets whether this GlimpseTarget generates GlimpseMouseEvents. This value does
     * not determine whether or not GlimpseTargets underneath this GlimpseTarget will
     * also generate GlimpseMouseEvents (see {@link #isEventConsumer()}).
     *
     * @return whether this target will generate GlimpseMouseEvents
     */
    public boolean isEventGenerator( );

    /**
     * Set whether or not this GlimpseTarget will generate GlimpseMouseEvents.
     *
     * @param generate
     */
    public void setEventGenerator( boolean generate );

    /**
     * Adds a sub-layout to this GlimpseTarget which will only paint in a region of this GlimpseTarget
     * based on its layout constraints. This same GlimpseLayout may be a child of any number of
     * different GlimpseTargets.
     *
     * @param layout
     */
    public void addLayout( GlimpseLayout layout );

    public void addLayout( GlimpseLayout layout, int zOrder );

    public void setZOrder( GlimpseLayout layout, int zOrder );

    /**
     * Removes a previously added layout from this GlimpseTarget.
     * @param layout
     */
    public void removeLayout( GlimpseLayout layout );

    /**
     * Clears the canvas, removing all attached GlimpseLayouts.
     */
    public void removeAllLayouts( );

    /**
     * @return the list of children added through addLayout( GlimpseLayout ).
     */
    public List<GlimpseTarget> getTargetChildren( );

    /**
     * Returns the cached or calculated bounds of this GlimpseTarget for a particular context.
     *
     * @param stack
     * @return the bounds for the given stack
     */
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack );

    /**
     * Sets the LookAndFeel for this GlimpseTarget and all child GlimpseTargets.
     */
    public void setLookAndFeel( LookAndFeel laf );
}
