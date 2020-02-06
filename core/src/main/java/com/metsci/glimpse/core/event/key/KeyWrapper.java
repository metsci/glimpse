/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.event.key;

import com.metsci.glimpse.core.canvas.GlimpseCanvas;
import com.metsci.glimpse.core.context.GlimpseTarget;
import com.metsci.glimpse.core.context.GlimpseTargetStack;
import com.metsci.glimpse.core.event.mouse.MouseWrapper;

/**
 * Type parameter {@code E} is the type of event received from the windowing toolkit (e.g. NEWT).
 */
public abstract class KeyWrapper<E>
{

    protected GlimpseCanvas canvas;
    protected MouseWrapper<?> canvasMouseHelper;

    public KeyWrapper( GlimpseCanvas canvas, MouseWrapper<?> canvasMouseHelper )
    {
        this.canvas = canvas;
        this.canvasMouseHelper = canvasMouseHelper;
    }

    public void dispose( )
    {
        this.canvas = null;
        this.canvasMouseHelper = null;
    }

    protected abstract GlimpseKeyEvent toGlimpseEvent( E ev, GlimpseTargetStack stack );

    public static Keyable getKeyTarget( GlimpseTargetStack stack )
    {
        GlimpseTarget target = ( stack == null ? null : stack.getTarget( ) );
        return ( target instanceof Keyable ? ( Keyable ) target : null );
    }

}
