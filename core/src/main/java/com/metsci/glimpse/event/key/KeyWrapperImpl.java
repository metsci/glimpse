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
package com.metsci.glimpse.event.key;

import java.util.Collection;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.MouseWrapper;

public abstract class KeyWrapperImpl<E> extends KeyWrapper<E>
{

    public KeyWrapperImpl( GlimpseCanvas canvas, MouseWrapper<?> canvasMouseHelper )
    {
        super( canvas, canvasMouseHelper );
    }

    public boolean doKeyPressed( E ev )
    {
        if ( ev == null ) return false;

        Collection<GlimpseTargetStack> stacks = this.canvasMouseHelper.getFocused( );
        for ( GlimpseTargetStack stack : stacks )
        {
            Keyable keyTarget = getKeyTarget( stack );
            if ( keyTarget == null )
            {
                return false;
            }

            GlimpseKeyEvent ev2 = this.toGlimpseEvent( ev, stack );
            keyTarget.keyPressed( ev2 );

            if ( ev2.isHandled( ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean doKeyReleased( E ev )
    {
        if ( ev == null ) return false;

        Collection<GlimpseTargetStack> stacks = this.canvasMouseHelper.getFocused( );
        for ( GlimpseTargetStack stack : stacks )
        {
            Keyable keyTarget = getKeyTarget( stack );
            if ( keyTarget == null )
            {
                return false;
            }

            GlimpseKeyEvent ev2 = this.toGlimpseEvent( ev, stack );
            keyTarget.keyReleased( ev2 );

            if ( ev2.isHandled( ) )
            {
                return true;
            }
        }

        return false;
    }

}
