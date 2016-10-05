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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SplitPane extends JPanel
{

    public static interface SplitPaneListener
    {
        void movedDivider( );
    }

    protected static final String CHILD_A = "A";
    protected static final String CHILD_B = "B";

    public final boolean arrangeVertically;
    public final int gapSize;
    public final int minChildSize = 1;

    protected Component childA;
    protected Component childB;
    protected double splitFrac;

    protected JPanel divider;

    protected final Set<SplitPaneListener> listeners;

    public SplitPane( boolean arrangeVertically, double splitFrac, int gapSize )
    {
        super( null );

        this.arrangeVertically = arrangeVertically;
        this.gapSize = gapSize;

        this.childA = null;
        this.childB = null;
        this.splitFrac = splitFrac;

        this.divider = new JPanel( );
        divider.setOpaque( false );
        divider.setCursor( getPredefinedCursor( arrangeVertically ? S_RESIZE_CURSOR : E_RESIZE_CURSOR ) );
        add( divider );

        SplitLayout layout = new SplitLayout( );
        setLayout( layout );

        MouseAdapter mouseAdapter = createMouseAdapter( layout );
        divider.addMouseListener( mouseAdapter );
        divider.addMouseMotionListener( mouseAdapter );

        this.listeners = new LinkedHashSet<>( );
    }

    public void addListener( SplitPaneListener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( SplitPaneListener listener )
    {
        listeners.remove( listener );
    }

    protected MouseAdapter createMouseAdapter( final SplitLayout layout )
    {
        return new MouseAdapter( )
        {
            Integer grab = null;

            @Override
            public void mousePressed( MouseEvent ev )
            {
                if ( ev.getButton( ) == BUTTON1 && isVisible( childA ) && isVisible( childB ) )
                {
                    grab = ( arrangeVertically ? ev.getY( ) : ev.getX( ) );
                }
            }

            @Override
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

            @Override
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
            return CHILD_A;
        }
        else if ( child == childB )
        {
            return CHILD_B;
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
        int thisSize = ( arrangeVertically ? getHeight( ) : getWidth( ) );

        // Prep to keep dividers fixed for childA and its descendants
        //
        List<SplitPane> splitsToTweakA = new ArrayList<>( );
        List<Integer> fixedSizesA = new ArrayList<>( );
        if ( isVisible( childA ) && isVisible( childB ) )
        {
            Component c = childA;
            while ( true )
            {
                if ( ! ( c instanceof SplitPane ) ) break;
                SplitPane split = ( SplitPane ) c;

                if ( split.arrangeVertically != this.arrangeVertically ) break;

                Component descendantA = split.getChildA( );
                Component descendantB = split.getChildB( );
                boolean hasA = isVisible( descendantA );
                boolean hasB = isVisible( descendantB );
                if ( hasA && hasB )
                {
                    splitsToTweakA.add( split );
                    fixedSizesA.add( arrangeVertically ? descendantA.getHeight( ) : descendantA.getWidth( ) );
                }

                c = ( hasB ? descendantB : ( hasA ? descendantA : null ) );
            }
        }

        // Prep to keep dividers fixed for childB and its descendants
        //
        List<SplitPane> splitsToTweakB = new ArrayList<>( );
        List<Integer> fixedSizesB = new ArrayList<>( );
        if ( isVisible( childA ) && isVisible( childB ) )
        {
            Component c = childB;
            while ( true )
            {
                if ( ! ( c instanceof SplitPane ) ) break;
                SplitPane split = ( SplitPane ) c;

                if ( split.arrangeVertically != this.arrangeVertically ) break;

                Component descendantA = split.getChildA( );
                Component descendantB = split.getChildB( );
                boolean hasA = isVisible( descendantA );
                boolean hasB = isVisible( descendantB );
                if ( hasA && hasB )
                {
                    splitsToTweakB.add( split );
                    fixedSizesB.add( arrangeVertically ? descendantB.getHeight( ) : descendantB.getWidth( ) );
                }

                c = ( hasA ? descendantA : ( hasB ? descendantB : null ) );
            }
        }

        // Find minimum split-fraction
        //
        double minFrac = 0;
        if ( !splitsToTweakA.isEmpty( ) )
        {
            int sizeA = ( arrangeVertically ? childA.getHeight( ) : childA.getWidth( ) );

            SplitPane adjacentSplit = splitsToTweakA.get( splitsToTweakA.size( ) - 1 );
            Component victim = adjacentSplit.getChildB( );
            int available = ( arrangeVertically ? victim.getHeight( ) : victim.getWidth( ) ) - adjacentSplit.minChildSize;
            int minSizeA = sizeA - available;

            minFrac = ( ( double ) minSizeA ) / ( ( double ) ( thisSize - gapSize ) );
        }

        // Find maximum split-fraction
        //
        double maxFrac = 1;
        if ( !splitsToTweakB.isEmpty( ) )
        {
            int sizeA = ( arrangeVertically ? childA.getHeight( ) : childA.getWidth( ) );

            SplitPane adjacentSplit = splitsToTweakB.get( splitsToTweakB.size( ) - 1 );
            Component victim = adjacentSplit.getChildA( );
            int available = ( arrangeVertically ? victim.getHeight( ) : victim.getWidth( ) ) - adjacentSplit.minChildSize;
            int maxSizeA = sizeA + available;

            maxFrac = ( ( double ) maxSizeA ) / ( ( double ) ( thisSize - gapSize ) );
        }

        // Set split-fraction
        //
        splitFrac = max( minFrac, min( maxFrac, splitFrac ) );
        if ( splitFrac == this.splitFrac ) return;
        _setSplitFrac( splitFrac );
        int[] childSizes = computeChildSizes( thisSize, gapSize, minChildSize, splitFrac );

        // Keep dividers fixed for childA and its descendants
        //
        int splitSizeA = childSizes[0];
        for ( int i = 0; i < fixedSizesA.size( ); i++ )
        {
            SplitPane split = splitsToTweakA.get( i );
            int sizeA = fixedSizesA.get( i );
            int sizeB = splitSizeA - split.gapSize - sizeA;

            double tweakedFrac = ( ( double ) sizeA ) / ( ( double ) ( sizeA + sizeB ) );
            split._setSplitFrac( tweakedFrac );
            splitSizeA = computeChildSizes( splitSizeA, split.gapSize, split.minChildSize, split.getSplitFrac( ) )[1];
        }

        // Keep dividers fixed for childB and its descendants
        //
        int splitSizeB = childSizes[1];
        for ( int i = 0; i < fixedSizesB.size( ); i++ )
        {
            SplitPane split = splitsToTweakB.get( i );
            int sizeB = fixedSizesB.get( i );
            int sizeA = splitSizeB - split.gapSize - sizeB;

            double tweakedFrac = ( ( double ) sizeA ) / ( ( double ) ( sizeA + sizeB ) );
            split._setSplitFrac( tweakedFrac );
            splitSizeB = computeChildSizes( splitSizeB, split.gapSize, split.minChildSize, split.getSplitFrac( ) )[0];
        }

        // Notify listeners
        //
        for ( SplitPaneListener listener : listeners )
        {
            listener.movedDivider( );
        }
    }

    protected void _setSplitFrac( double splitFrac )
    {
        this.splitFrac = splitFrac;
        invalidate( );
    }

    protected static int[] computeChildSizes( int containerSize, int gapSize, int minChildSize, double splitFrac )
    {
        int maxSizeA = containerSize - gapSize - minChildSize;

        int prelimSizeA = iround( splitFrac * ( containerSize - gapSize ) );
        int sizeA = max( minChildSize, min( maxSizeA, prelimSizeA ) );
        int sizeB = max( minChildSize, containerSize - sizeA - gapSize );

        return new int[] { sizeA, sizeB };
    }

    // Layout Manager
    //

    public class SplitLayout implements LayoutManager
    {
        @Override
        public void addLayoutComponent( String name, Component c )
        {
            if ( CHILD_A.equalsIgnoreCase( name ) )
            {
                if ( childA != null ) throw new RuntimeException( "Existing 'A' child must be removed before adding a new one" );
                childA = c;
            }
            else if ( CHILD_B.equalsIgnoreCase( name ) )
            {
                if ( childB != null ) throw new RuntimeException( "Existing 'B' child must be removed before adding a new one" );
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
                    int[] heights = computeChildSizes( heightContainer, gapSize, minChildSize, splitFrac );
                    int heightA = heights[0];
                    int heightB = heights[1];

                    childA.setBounds( 0, 0, widthContainer, heightA );
                    childB.setBounds( 0, heightA + gapSize, widthContainer, heightB );
                    divider.setBounds( 0, heightA, widthContainer, gapSize );
                }
                else
                {
                    int[] widths = computeChildSizes( widthContainer, gapSize, minChildSize, splitFrac );
                    int widthA = widths[0];
                    int widthB = widths[1];

                    childA.setBounds( 0, 0, widthA, heightContainer );
                    childB.setBounds( widthA + gapSize, 0, widthB, heightContainer );
                    divider.setBounds( widthA, 0, gapSize, heightContainer );
                }
                divider.setVisible( true );
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
                divider.setVisible( false );
            }
        }
    }

}
