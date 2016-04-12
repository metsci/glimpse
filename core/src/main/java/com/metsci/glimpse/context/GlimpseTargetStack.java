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

import com.metsci.glimpse.layout.GlimpseLayoutCache;

/**
 * A class representing a nested sequence of GlimpseTarget containers and
 * their bounds. The current TargetStack is provided to GlimpsePainters
 * via the current GlimpseContext. The current TargetStack is used by
 * LayoutCache as a key to store the GlimpseBounds for a given GlimpseTarget
 * for each unique TargetStack which it has been rendered to.
 *
 * @see GlimpseLayoutCache
 * @see GlimpseContext
 * @author ulman
 */
public interface GlimpseTargetStack
{
    /**
     * Adds a new GlimpseTarget and its corresponding GlimpseBounds to the TargetStack.
     *
     * @param target
     * @param bounds
     * @return this GlimpseTargetStack (to enable chaining of push calls)
     */
    public GlimpseTargetStack push( GlimpseTarget target, GlimpseBounds bounds );

    /**
     * Pushes a GlimpseTarget onto this TargetStack (with dummy GlimpseBounds).
     *
     * @param target
     * @return this GlimpseTargetStack (to enable chaining of push calls)
     */
    public GlimpseTargetStack push( GlimpseTarget target );

    /**
     * Pushes an existing stack onto the top of this stack, merging the two.
     *
     * @param stack
     * @return this GlimpseTargetStack (to enable chaining of push calls)
     */
    public GlimpseTargetStack push( GlimpseTargetStack stack );

    /**
     * Removes the top GlimpseTarget / GlimpseBounds pair from the target stack.
     */
    public GlimpseTargetStack pop( );

    /**
     * @return The GlimpseTarget at the top of the target stack (the highest index)
     */
    public GlimpseTarget getTarget( );

    /**
     * @return The GlimpsetBounds associated with the GlimpseTarget at the top of the target stack
     */
    public GlimpseBounds getBounds( );

    public List<GlimpseTarget> getTargetList( );

    public List<GlimpseBounds> getBoundsList( );

    public int getSize( );
}
