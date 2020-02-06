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
package com.metsci.glimpse.core.support.swing;

import static com.jogamp.newt.event.MouseEvent.BUTTON3;
import static com.metsci.glimpse.core.event.mouse.FocusBehavior.CLICK_FOCUS;
import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.getModalBlockedStatus;
import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.DEFINITELY_BLOCKED;

import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;

import com.jogamp.newt.event.MouseEvent;
import com.metsci.glimpse.core.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.core.context.GlimpseTarget;
import com.metsci.glimpse.core.context.GlimpseTargetStack;
import com.metsci.glimpse.core.event.mouse.FocusBehavior;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.core.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.core.support.popup.GlimpsePopupMenuTarget;

/**
 * A version of MouseWrapperNewt which fires Glimpse events on the Swing EDT
 * @author ulman
 */
public class MouseWrapperNewtSwingEDT extends MouseWrapperNewt
{

    protected NewtSwingGlimpseCanvas swingCanvas;

    public MouseWrapperNewtSwingEDT( NewtSwingGlimpseCanvas canvas )
    {
        this( canvas, CLICK_FOCUS );
    }

    public MouseWrapperNewtSwingEDT( NewtSwingGlimpseCanvas canvas, FocusBehavior focusBehavior )
    {
        super( canvas, focusBehavior );
        this.swingCanvas = canvas;
    }

    @Override
    public void mouseClicked( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseClicked0( ev );
        }
    }

    @Override
    public void mousePressed( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            MenuSelectionManager menuMan = MenuSelectionManager.defaultManager( );
            boolean isMenuVisible = ( menuMan.getSelectedPath( ) != null && menuMan.getSelectedPath( ).length > 0 );
            if ( isMenuVisible )
            {
                menuMan.clearSelectedPath( );
                ev.setConsumed( true );
            }
            else if ( ev.getButton( ) == BUTTON3 )
            {
                GlimpseTargetStack stack = null;
                GlimpsePopupMenuTarget target = null;
                for ( GlimpseTargetStack stack0 : this.getContainingTargets( ev ) )
                {
                    GlimpseTarget target0 = stack0.getTarget( );
                    if ( target0 instanceof GlimpsePopupMenuTarget )
                    {
                        stack = stack0;
                        target = ( ( GlimpsePopupMenuTarget ) target0 );
                        break;
                    }
                }

                if ( target != null )
                {
                    JPopupMenu menu = target.getPopupMenu( );
                    if ( menu != null )
                    {
                        GlimpseMouseEvent ev2 = this.toGlimpseEvent( ev, stack );
                        target.getPopupMenuNotifier( ).fire( ev2 );
                        menu.show( this.swingCanvas, ev.getX( ), ev.getY( ) );
                    }
                    ev.setConsumed( true );
                }
                else
                {
                    this.mousePressed0( ev );
                }
            }
            else
            {
                this.mousePressed0( ev );
            }
        }
    }

    @Override
    public void mouseReleased( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseReleased0( ev );
        }
    }

    @Override
    public void mouseEntered( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseEntered0( ev );
        }
    }

    @Override
    public void mouseExited( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseExited0( ev );
        }
    }

    @Override
    public void mouseDragged( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseDragged0( ev );
        }
    }

    @Override
    public void mouseMoved( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseMoved0( ev );
        }
    }

    @Override
    public void mouseWheelMoved( MouseEvent ev )
    {
        if ( getModalBlockedStatus( ev ) != DEFINITELY_BLOCKED )
        {
            this.mouseWheelMoved0( ev );
        }
    }
}
