/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.MiscUtils.iround;
import static java.awt.Cursor.E_RESIZE_CURSOR;
import static java.awt.Cursor.S_RESIZE_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

public class SplitPane extends JPanel
{

    public final boolean arrangeVertically;
    public final int gapSize;

    protected Component childA;
    protected Component childB;
    protected double splitFrac;

    protected JPanel separator;


    public SplitPane( boolean arrangeVertically )
    {
        this( arrangeVertically, 0.5 );
    }

    public SplitPane( boolean arrangeVertically, double splitFrac )
    {
        this( arrangeVertically, splitFrac, 7 );
    }

    public SplitPane( boolean arrangeVertically, double splitFrac, int gapSize )
    {
        super( null );

        this.arrangeVertically = arrangeVertically;
        this.gapSize = gapSize;

        this.childA = null;
        this.childB = null;
        this.splitFrac = splitFrac;

        this.separator = new JPanel( );
        separator.setOpaque( false );
        separator.setCursor( getPredefinedCursor( arrangeVertically ? S_RESIZE_CURSOR : E_RESIZE_CURSOR ) );
        add( separator );

        SplitLayout layout = new SplitLayout( );
        setLayout( layout );

        MouseAdapter mouseAdapter = createMouseAdapter( layout );
        separator.addMouseListener( mouseAdapter );
        separator.addMouseMotionListener( mouseAdapter );
    }

    protected MouseAdapter createMouseAdapter( final SplitLayout layout )
    {
        return new MouseAdapter( )
        {
            Integer grab = null;

            public void mousePressed( MouseEvent ev )
            {
                if ( ev.getButton( ) == BUTTON1 && isVisible( childA ) && isVisible( childB ) )
                {
                    grab = ( arrangeVertically ? ev.getY( ) : ev.getX( ) );
                }
            }

            public void mouseDragged( MouseEvent ev )
            {
                if ( grab != null )
                {
                    int newSizeA = ( arrangeVertically ? childA.getHeight( ) + ev.getY( ) : childA.getWidth( ) + ev.getX( ) ) - grab;
                    int contentSize = ( arrangeVertically ? getHeight( ) : getWidth( ) ) - gapSize;
                    setSplitFrac( ( ( double ) newSizeA ) / ( ( double ) contentSize ) );
                    validate( );
                }
            }

            public void mouseReleased( MouseEvent ev )
            {
                if ( ev.getButton( ) == BUTTON1 && grab != null )
                {
                    int newSizeA = ( arrangeVertically ? childA.getHeight( ) + ev.getY( ) : childA.getWidth( ) + ev.getX( ) ) - grab;
                    int contentSize = ( arrangeVertically ? getHeight( ) : getWidth( ) ) - gapSize;
                    setSplitFrac( ( ( double ) newSizeA ) / ( ( double ) contentSize ) );
                    validate( );

                    grab = null;
                }
            }
        };
    }

    protected static boolean isVisible( Component c )
    {
        return ( c != null && c.isVisible( ) );
    }

    public Component getChildA( )
    {
        return childA;
    }

    public Component getChildB( )
    {
        return childB;
    }

    public Component getSibling( Component child )
    {
        if ( child == childA )
        {
            return childB;
        }
        else if ( child == childB )
        {
            return childA;
        }
        else
        {
            throw new RuntimeException( "Component is not a child" );
        }
    }

    public Object getConstraints( Component child )
    {
        if ( child == childA )
        {
            return "A";
        }
        else if ( child == childB )
        {
            return "B";
        }
        else
        {
            throw new RuntimeException( "Component is not a child" );
        }
    }

    public double getSplitFrac( )
    {
        return splitFrac;
    }

    public void setSplitFrac( double splitFrac )
    {
        splitFrac = max( 0, min( 1, splitFrac ) );
        if ( splitFrac != this.splitFrac )
        {
            // Prep to keep childA's divider fixed
            //
            int fixedSizeGrandchildA = -1;
            if ( childA instanceof SplitPane )
            {
                SplitPane child = ( SplitPane ) childA;
                Component grandchildA = child.getChildA( );
                Component grandchildB = child.getChildB( );
                if ( child.arrangeVertically == this.arrangeVertically && isVisible( grandchildA ) && isVisible( grandchildB ) )
                {
                    fixedSizeGrandchildA = ( arrangeVertically ? grandchildA.getHeight( ) : grandchildA.getWidth( ) );
                }
            }

            // Prep to keep childB's divider fixed
            //
            int fixedSizeGrandchildB = -1;
            if ( childB instanceof SplitPane )
            {
                SplitPane child = ( SplitPane ) childB;
                Component grandchildA = child.getChildA( );
                Component grandchildB = child.getChildB( );
                if ( child.arrangeVertically == this.arrangeVertically && isVisible( grandchildA ) && isVisible( grandchildB ) )
                {
                    fixedSizeGrandchildB = ( arrangeVertically ? grandchildB.getHeight( ) : grandchildB.getWidth( ) );
                }
            }

            // Set this split-pane's split-fraction
            //
            this.splitFrac = splitFrac;
            invalidate( );

            // Duplicate some layout logic here, to figure out what the child
            // sizes will be -- so that we can keep child dividers fixed without
            // getting recursive
            //
            int childSizeA;
            int childSizeB;
            if ( arrangeVertically )
            {
                int heightContainer = getHeight( );

                int minHeightA = 1;
                int minHeightB = 1;
                int maxHeightA = heightContainer - gapSize - minHeightB;

                int prelimHeightA = iround( splitFrac * ( heightContainer - gapSize ) );
                int heightA = max( minHeightA, min( maxHeightA, prelimHeightA ) );
                int yB = heightA + gapSize;
                int heightB = max( minHeightB, heightContainer - yB );

                childSizeA = heightA;
                childSizeB = heightB;
            }
            else
            {
                int widthContainer = getWidth( );

                int minWidthA = 1;
                int minWidthB = 1;
                int maxWidthA = widthContainer - gapSize - minWidthB;

                int prelimWidthA = iround( splitFrac * ( widthContainer - gapSize ) );
                int widthA = max( minWidthA, min( maxWidthA, prelimWidthA ) );
                int xB = widthA + gapSize;
                int widthB = max( minWidthB, widthContainer - xB );

                childSizeA = widthA;
                childSizeB = widthB;
            }

            // Keep childA's divider fixed
            //
            if ( fixedSizeGrandchildA >= 0 )
            {
                SplitPane child = ( SplitPane ) childA;
                int childSize = childSizeA;
                int grandchildSizeA = fixedSizeGrandchildA;
                int grandchildSizeB = childSize - child.gapSize - grandchildSizeA;
                double childFrac = ( ( double ) grandchildSizeA ) / ( ( double ) ( grandchildSizeA + grandchildSizeB ) );
                child.setSplitFrac( childFrac );
            }

            // Keep childB's divider fixed
            //
            if ( fixedSizeGrandchildB >= 0 )
            {
                SplitPane child = ( SplitPane ) childB;
                int childSize = childSizeB;
                int grandchildSizeB = fixedSizeGrandchildB;
                int grandchildSizeA = childSize - child.gapSize - grandchildSizeB;
                double childFrac = ( ( double ) grandchildSizeA ) / ( ( double ) ( grandchildSizeA + grandchildSizeB ) );
                child.setSplitFrac( childFrac );
            }
        }
    }


    // Layout Manager
    //

    public class SplitLayout implements LayoutManager
    {
        @Override
        public void addLayoutComponent( String name, Component c )
        {
            if ( "A".equalsIgnoreCase( name ) )
            {
                // XXX: Require childA != childB
                childA = c;
            }
            else if ( "B".equalsIgnoreCase( name ) )
            {
                // XXX: Require childA != childB
                childB = c;
            }
            else
            {
                throw new RuntimeException( "Unrecognized layout-component name: " + name );
            }
        }

        @Override
        public void removeLayoutComponent( Component c )
        {
            if ( c == childA )
            {
                childA = null;
            }
            else if ( c == childB )
            {
                childB = null;
            }
        }

        @Override
        public Dimension preferredLayoutSize( Container container )
        {
            Dimension sizeA = ( isVisible( childA ) ? childA.getPreferredSize( ) : null );
            Dimension sizeB = ( isVisible( childB ) ? childB.getPreferredSize( ) : null );
            return getTotalSize( sizeA, sizeB );
        }

        @Override
        public Dimension minimumLayoutSize( Container container )
        {
            Dimension sizeA = ( isVisible( childA ) ? childA.getMinimumSize( ) : null );
            Dimension sizeB = ( isVisible( childB ) ? childB.getMinimumSize( ) : null );
            return getTotalSize( sizeA, sizeB );
        }

        protected Dimension getTotalSize( Dimension sizeA, Dimension sizeB )
        {
            int widthA = ( sizeA == null ? 0 : sizeA.width );
            int heightA = ( sizeA == null ? 0 : sizeA.height );

            int widthB = ( sizeB == null ? 0 : sizeB.width );
            int heightB = ( sizeB == null ? 0 : sizeB.height );

            int gap = ( childA == null || childB == null ? 0 : gapSize );

            if ( arrangeVertically )
            {
                int width = max( widthA, widthB );
                int height = heightA + gap + heightB;
                return new Dimension( width, height );
            }
            else
            {
                int width = widthA + gap + widthB;
                int height = max( heightA, heightB );
                return new Dimension( width, height );
            }
        }

        @Override
        public void layoutContainer( Container container )
        {
            int widthContainer = container.getWidth( );
            int heightContainer = container.getHeight( );

            boolean hasA = isVisible( childA );
            boolean hasB = isVisible( childB );
            if ( hasA && hasB )
            {
                if ( arrangeVertically )
                {
                    int minHeightA = 1;
                    int minHeightB = 1;
                    int maxHeightA = heightContainer - gapSize - minHeightB;

                    int prelimHeightA = iround( splitFrac * ( heightContainer - gapSize ) );
                    int heightA = max( minHeightA, min( maxHeightA, prelimHeightA ) );
                    int yB = heightA + gapSize;
                    int heightB = max( minHeightB, heightContainer - yB );

                    childA.setBounds( 0, 0, widthContainer, heightA );
                    childB.setBounds( 0, yB, widthContainer, heightB );
                    separator.setBounds( 0, heightA, widthContainer, gapSize );
                }
                else
                {
                    int minWidthA = 1;
                    int minWidthB = 1;
                    int maxWidthA = widthContainer - gapSize - minWidthB;

                    int prelimWidthA = iround( splitFrac * ( widthContainer - gapSize ) );
                    int widthA = max( minWidthA, min( maxWidthA, prelimWidthA ) );
                    int xB = widthA + gapSize;
                    int widthB = max( minWidthB, widthContainer - xB );

                    childA.setBounds( 0, 0, widthA, heightContainer );
                    childB.setBounds( xB, 0, widthB, heightContainer );
                    separator.setBounds( widthA, 0, gapSize, heightContainer );
                }
                separator.setVisible( true );
            }
            else
            {
                if ( hasA )
                {
                    childA.setBounds( 0, 0, widthContainer, heightContainer );
                }
                else if ( hasB )
                {
                    childB.setBounds( 0, 0, widthContainer, heightContainer );
                }
                separator.setVisible( false );
            }
        }
    }

}
