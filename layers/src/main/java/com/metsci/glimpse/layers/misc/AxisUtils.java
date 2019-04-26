/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.axis.tagged.TaggedAxisListener1D.newTaggedAxisListener1D;

import java.util.function.Consumer;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.util.var.Disposable;

public class AxisUtils
{

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addAxisListener1D( Axis1D axis, boolean runImmediately, AxisListener1D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addTaggedAxisListener1D( TaggedAxis1D axis, boolean runImmediately, Consumer<TaggedAxis1D> tagsUpdatedFn )
    {
        return addTaggedAxisListener1D( axis, runImmediately, newTaggedAxisListener1D( tagsUpdatedFn ) );
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addTaggedAxisListener1D( TaggedAxis1D axis, boolean runImmediately, TaggedAxisListener1D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

    /**
     * Adds the listener to the axis, and also:
     * <ul>
     * <li>Invokes the listener immediately, if {@code runImmediately} is true
     * <li>Returns a {@link Disposable} for removing the listener
     * </ul>
     * This can improve conciseness in controller code, where we often want to: define
     * a lambda, add it as a listener, run it immediately, and remove the listener later.
     */
    public static Disposable addAxisListener2D( Axis2D axis, boolean runImmediately, AxisListener2D listener )
    {
        if ( runImmediately )
        {
            listener.axisUpdated( axis );
        }

        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

}
