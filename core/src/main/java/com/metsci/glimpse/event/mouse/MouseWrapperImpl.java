package com.metsci.glimpse.event.mouse;

import java.util.List;
import java.util.Set;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;

public abstract class MouseWrapperImpl<E> extends MouseWrapper<E>
{

    public MouseWrapperImpl( GlimpseCanvas canvas )
    {
        super( canvas );
    }
    
    public void mouseClicked( E event )
    {
        // not handled by GlimpseMouseListener, use mousePressed/mouseReleased
    }

    public void mouseEntered( E event )
    {
        // save the old hovered stacks
        Set<GlimpseTargetStack> oldHovered = clearHovered( );
        
        // update hovered stacks
        getContainingTargets( event );
        
        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
    }

    public void mouseExited( E event )
    {
        // save the old hovered stacks
        Set<GlimpseTargetStack> oldHovered = clearHovered( );
        
        // update hovered stacks
        getContainingTargets( event );
        
        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
    }

    public void mousePressed( E event )
    {
        List<GlimpseTargetStack> list = getContainingTargets( event );

        setAllHovered( list );

        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : list )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return;

            GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack );

            mouseTarget.mousePressed( glimpseEvent );
        
            if ( glimpseEvent.isHandled( ) ) break;
        }
    }

    public void mouseReleased( E event )
    {
        // always always deliver the mouseUp event regardless of which
        // component the mouse event occurred inside
        if ( isDragHovered( ) )
        {
            Set<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, hoveredStack );

                if ( mouseTarget != null ) mouseTarget.mouseReleased( glimpseEvent );
                
                if ( glimpseEvent.isHandled( ) ) break;
            }
        }

        // call getContainingTarget to setHovered correctly
        // call after event is sent because we want to send the mouseReleased event
        // to the previously hovered component then setHovered to the GlimpseTarget
        // currently under the mouse
        getContainingTargets( event );
    }

    public void mouseDragged( E event )
    {
        // save the old hovered stacks
        Set<GlimpseTargetStack> oldHovered = clearHovered( );
        
        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
        
        if ( isDragHovered( ) )
        {
            Set<GlimpseTargetStack> hoveredList = getDragHovered( );
            for ( GlimpseTargetStack hoveredStack : hoveredList )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toLocalGlimpseEvent( event, hoveredStack );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );
                
                if ( glimpseHoveredEvent.isHandled( ) ) break;
            }
        }
    }

    public void mouseMoved( E event )
    {
        // if the mouse is hovering, recalculate hovered components every event
        // isButtonDown check isn't necessary like it is for MouseWrapperSWT.mouseMove(),
        // since this event would be a mouseDragged if it was
        Set<GlimpseTargetStack> oldHovered = clearAllHovered( );

        // call getContainingTarget to setHovered correctly
        getContainingTargets( event );

        // get the new hovered stacks
        Set<GlimpseTargetStack> newHovered = getHovered( );
        
        // send mouseExited and mouseEntered events based on the old/new hovered stacks
        notifyMouseEnteredExited( event, oldHovered, newHovered );
        
        // if we have something hovered, send mouseMoved events
        if ( isDragHovered( ) )
        {
            for ( GlimpseTargetStack hoveredStack : newHovered )
            {
                Mouseable mouseHoveredTarget = getMouseTarget( hoveredStack );
                GlimpseMouseEvent glimpseHoveredEvent = toLocalGlimpseEvent( event, hoveredStack );

                if ( mouseHoveredTarget != null ) mouseHoveredTarget.mouseMoved( glimpseHoveredEvent );
                
                if ( glimpseHoveredEvent.isHandled( ) ) break;
            }
        }
    }
    
    public void mouseWheelMoved( E event )
    {
        // stacks with low indices are on top in the layout, and
        // have their mouse events generated first
        for ( GlimpseTargetStack stack : getContainingTargets( event ) )
        {
            Mouseable mouseTarget = getMouseTarget( stack );
            if ( mouseTarget == null ) return;

            GlimpseMouseEvent glimpseEvent = toLocalGlimpseEvent( event, stack );
            mouseTarget.mouseWheelMoved( glimpseEvent );
            
            if ( glimpseEvent.isHandled( ) ) break;
        }
    }

}
