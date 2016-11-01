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
package com.metsci.glimpse.painter.treemap;

import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static java.lang.Math.ceil;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;

/**
 * Draws a <a href="http://en.wikipedia.org/wiki/Treemapping">TreeMap</a>,
 * useful for hierarchical data. There are 4 dimensions of information to a
 * treemap: the level in the hierarchy, the size, the color and title/text. The
 * structure, size and text is contained in the {@link NestedTreeMap}
 * datastructure. The colors and layout are determined by this painter and
 * implemented in a way that allows switching on information within a TreeMap
 * node (see
 * {@link com.metsci.glimpse.painter.treemap.SimpleTreeMapPainter#getLeafColor(int, boolean)}).
 *
 * @author borkholder
 */
public abstract class AbstractTreeMapPainter extends GlimpsePainterBase
{
    protected NestedTreeMap tree;

    protected TreeMapLayout treeLayout = new SquarifiedLayout( );
    protected LayoutCache layoutCache;

    protected NestedTreeMap newTree;
    protected TreeMapLayout newTreeLayout;

    public void setLayout( TreeMapLayout layout )
    {
        newTreeLayout = layout;
    }

    public TreeMapLayout getTreeMapLayout( )
    {
        return treeLayout;
    }

    public void setTreeMapData( NestedTreeMap tree )
    {
        newTree = tree;
    }

    public NestedTreeMap getTreeMapData( )
    {
        return tree;
    }

    public Integer getLeafAt( Axis2D axis, double x, double y )
    {
        if ( tree == null || tree.isEmpty( ) || treeLayout == null )
        {
            return null;
        }

        updateLayoutCache( axis );
        return getLeafAtHelper( new Point2D.Double( x, y ), tree.getRoot( ) );
    }

    protected Integer getLeafAtHelper( Point2D point, int node )
    {
        Rectangle2D[] rects = layoutCache.get( node );
        int[] children = tree.getChildren( node );
        for ( int i = 0; i < children.length; i++ )
        {
            if ( rects[i].contains( point ) )
            {
                return getLeafAtHelper( point, children[i] );
            }
        }

        return node;
    }

    protected void flushChanges( )
    {
        if ( newTree != null )
        {
            tree = newTree;
            newTree = null;
            flushLayoutCache( );
        }

        if ( newTreeLayout != null )
        {
            treeLayout = newTreeLayout;
            newTreeLayout = null;
            flushLayoutCache( );
        }
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        flushChanges( );

        if ( tree == null || tree.isEmpty( ) || treeLayout == null )
        {
            return;
        }

        GL3 gl = getGL3( context );
        Axis2D axis = requireAxis2D( context );

        gl.glEnable( GL3.GL_SCISSOR_TEST );
        enableStandardBlending( gl );

        updateLayoutCache( axis );

        double width = axis.getAxisX( ).getAbsoluteMax( ) - axis.getAxisY( ).getAbsoluteMin( );
        double height = axis.getAxisY( ).getAbsoluteMax( ) - axis.getAxisY( ).getAbsoluteMin( );
        Rectangle2D nodeBounds = new Rectangle2D.Double( 0, 0, width, height );
        displayNode( gl, axis, context.getTargetStack( ).getBounds( ), nodeBounds, tree.getRoot( ) );
    }

    protected void flushLayoutCache( )
    {
        if ( layoutCache != null )
        {
            layoutCache.setInvalid( );
        }
    }

    protected void updateLayoutCache( Axis2D axis )
    {
        if ( layoutCache == null || !layoutCache.isValid( axis ) )
        {
            double width = axis.getAxisX( ).getAbsoluteMax( ) - axis.getAxisY( ).getAbsoluteMin( );
            double height = axis.getAxisY( ).getAbsoluteMax( ) - axis.getAxisY( ).getAbsoluteMin( );
            Rectangle2D boundary = new Rectangle2D.Double( 0, 0, width, height );

            layoutCache = new LayoutCache( axis );
            populateLayout( tree.getRoot( ), boundary );
        }
    }

    /**
     * Computes the layout of the entire treemap.
     * {@code #getLayout(Rectangle2D, int)} will then adjust each individual
     * rectangle based on how much is drawn and visible on the screen. By
     * computing the layout a-priori, the placement of any box never changes, but
     * portions of it may not be drawn because the area is too small.
     * <p>
     * Additionally, what makes this most complex is that the title is always
     * drawn a fixed number of pixels high. By zooming in and out, the proportion
     * of the rectangle changes as the title gets smaller relative to the rest of
     * the rectangle. This causes the layout to change if it were to be computed
     * on-demand.
     * </p>
     */
    protected void populateLayout( int nodeId, Rectangle2D boundary )
    {
        double[] sizes = tree.getSizesOfChildren( nodeId );
        if ( sizes.length > 0 )
        {
            Rectangle2D[] rects = getTreeMapLayout( ).layout( boundary, sizes, tree.getLevel( nodeId ) );
            rects = Arrays.copyOf( rects, rects.length + 1 );
            rects[rects.length - 1] = boundary;
            layoutCache.put( nodeId, rects );

            int[] children = tree.getChildren( nodeId );
            for ( int i = 0; i < children.length; i++ )
            {
                populateLayout( children[i], rects[i] );
            }
        }
    }

    /**
     * Recompute the child rectangles by scaling the old boundary to the new
     * boundary and then proportionally scaling the children. We cache the new
     * scaled values in the cache each time.
     * <p>
     * This particularly helps when calling {@link #getLeafAt(double, double)}
     * because if we're hovering over the title of a parent node, we don't want to
     * return the children. Also helps slightly with speed at a possible loss of
     * precision. If we keep rescaling from the previous rectangle without
     * starting over from a fresh boundary calculation, it might eventually get
     * off.
     * </p>
     */
    protected Rectangle2D[] getLayout( Rectangle2D boundary, int nodeId )
    {
        Rectangle2D[] rects = layoutCache.get( nodeId );
        Rectangle2D origBound = rects[rects.length - 1];

        boolean isOutdated = Math.abs( boundary.getHeight( ) - origBound.getHeight( ) ) > 1e-6;
        isOutdated |= Math.abs( boundary.getWidth( ) - origBound.getWidth( ) ) > 1e-6;

        if ( isOutdated )
        {
            double scaleX = boundary.getWidth( ) / origBound.getWidth( );
            double scaleY = boundary.getHeight( ) / origBound.getHeight( );

            // scale the old bounding rectangle and sub-rectangles to fit the new area
            Rectangle2D[] newRects = new Rectangle2D[rects.length];
            for ( int i = 0; i < rects.length - 1; i++ )
            {
                Rectangle2D oldRect = rects[i];
                Rectangle2D newRect = new Rectangle2D.Double( ( oldRect.getMinX( ) - origBound.getMinX( ) ) * scaleX + boundary.getMinX( ), ( oldRect.getMinY( ) - origBound.getMinY( ) ) * scaleY + boundary.getMinY( ), oldRect.getWidth( ) * scaleX, oldRect.getHeight( ) * scaleY );
                newRects[i] = newRect;
            }

            rects = newRects;
            rects[rects.length - 1] = boundary;
            layoutCache.put( nodeId, rects );
        }

        return rects;
    }

    /**
     * Recursively draws nodes. This should determine visibility, adjust the clip
     * and then delegate to draw the node/leaf itself.
     */
    protected void displayNode( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId )
    {
        // are the node bounds outside the viewport
        if ( axis.getMinX( ) >= nodeBounds.getMaxX( ) || axis.getMinY( ) >= nodeBounds.getMaxY( ) || nodeBounds.getMinX( ) >= axis.getMaxX( ) || nodeBounds.getMinY( ) >= axis.getMaxY( ) )
        {
            return;
        }

        if ( nodeBounds.getWidth( ) <= 0 || nodeBounds.getHeight( ) <= 0 )
        {
            return;
        }

        // clip so we don't draw text outside the boundary
        int pxX = axis.getAxisX( ).valueToScreenPixel( nodeBounds.getMinX( ) );
        int pxY = axis.getAxisY( ).valueToScreenPixel( nodeBounds.getMinY( ) );
        int width = ( int ) ceil( nodeBounds.getWidth( ) * axis.getAxisX( ).getPixelsPerValue( ) );
        int height = ( int ) ceil( nodeBounds.getHeight( ) * axis.getAxisY( ).getPixelsPerValue( ) );

        gl.glScissor( pxX + layoutBounds.getX( ), pxY + layoutBounds.getY( ), width, height );

        if ( tree.isLeaf( nodeId ) )
        {
            drawLeaf( gl, axis, layoutBounds, nodeBounds, nodeId );
        }
        else
        {
            drawParent( gl, axis, layoutBounds, nodeBounds, nodeId );
        }
    }

    /**
     * Draw a node that is a parent of other nodes. Typically this will draw a
     * title and then just delegate to drawing the children.
     */
    protected void drawParent( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId )
    {
        Rectangle2D newBoundary = drawTitle( gl, axis, layoutBounds, nodeBounds, nodeId );
        if ( newBoundary.getWidth( ) <= 0 || newBoundary.getHeight( ) <= 0 )
        {
            return;
        }

        int[] children = tree.getChildren( nodeId );
        Rectangle2D[] childRects = getLayout( newBoundary, nodeId );

        for ( int i = 0; i < children.length; i++ )
        {
            Rectangle2D childRect = childRects[i];
            int childId = children[i];
            displayNode( gl, axis, layoutBounds, childRect, childId );
        }
    }

    /**
     * Draws a leaf.
     */
    protected void drawLeaf( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId )
    {
        drawLeafBackground( gl, axis, layoutBounds, nodeBounds, leafId );

        Rectangle2D newBoundary = drawTitle( gl, axis, layoutBounds, nodeBounds, leafId );
        if ( newBoundary.getWidth( ) <= 0 || newBoundary.getHeight( ) <= 0 )
        {
            return;
        }

        drawBorder( gl, axis, layoutBounds, nodeBounds, leafId );
        drawLeafInterior( gl, axis, layoutBounds, newBoundary, leafId );
    }

    /**
     * Returns true of the selection is contained within the given rectangle.
     */
    protected boolean isSelected( Axis2D axis, Rectangle2D boundary )
    {
        return boundary.contains( axis.getAxisX( ).getSelectionCenter( ), axis.getAxisY( ).getSelectionCenter( ) );
    }

    /**
     * Draws the title. The given boundary determines the bounds of the
     * parent/leaf area. The returned rectangle should give the new bounds for the
     * rest of the drawing - the {@code boundary} minus the title bounds. For a
     * parent, the new rectangle determines the bounds for the children. For a
     * leaf, the new rectangle determines the bounds for any other drawing inside
     * the leaf.
     */
    protected abstract Rectangle2D drawTitle( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId );

    /**
     * Draws the interior of a leaf. Can be an icon, text or anything.
     */
    protected abstract void drawLeafInterior( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId );

    /**
     * Draws the background of a leaf. Parent nodes don't normally have
     * backgrounds, because their children cover them.
     */
    protected abstract void drawLeafBackground( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int leafId );

    /**
     * Draws the border around a node, either a parent or leaf.
     */
    protected abstract void drawBorder( GL3 gl, Axis2D axis, GlimpseBounds layoutBounds, Rectangle2D nodeBounds, int nodeId );

    @SuppressWarnings( "serial" )
    protected class LayoutCache extends Int2ObjectAVLTreeMap<Rectangle2D[]>
    {
        private double absoluteMinX;
        private double absoluteMaxX;
        private double absoluteMinY;
        private double absoluteMaxY;

        public LayoutCache( Axis2D axis )
        {
            absoluteMinX = axis.getAxisX( ).getAbsoluteMin( );
            absoluteMaxX = axis.getAxisX( ).getAbsoluteMax( );
            absoluteMinY = axis.getAxisY( ).getAbsoluteMin( );
            absoluteMaxY = axis.getAxisY( ).getAbsoluteMax( );
        }

        public void setInvalid( )
        {
            absoluteMinX = Double.NaN;
            absoluteMaxX = Double.NaN;
            absoluteMinY = Double.NaN;
            absoluteMaxY = Double.NaN;
        }

        public boolean isValid( Axis2D axis )
        {
            return absoluteMinX == axis.getAxisX( ).getAbsoluteMin( ) && absoluteMaxX == axis.getAxisX( ).getAbsoluteMax( ) && absoluteMinY == axis.getAxisY( ).getAbsoluteMin( ) && absoluteMaxY == axis.getAxisY( ).getAbsoluteMax( );
        }
    }
}
