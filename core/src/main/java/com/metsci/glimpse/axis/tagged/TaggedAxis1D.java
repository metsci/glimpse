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
package com.metsci.glimpse.axis.tagged;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.axis.Axis1D;

/**
 * An Axis1D which keeps track of a set of labeled locations along the axis.
 *
 * Tags may be manipulated programmatically or via mouse interaction.
 * To enable mouse interaction, a {@link TaggedAxisMouseListener1D} must
 * be added to the {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D}
 * for the {@link TaggedAxis1D}.
 *
 * Tags may be visualized by drawing the TaggedAxis1D using a painter from the
 * {@link com.metsci.glimpse.axis.tagged.painter} package.
 *
 * @author ulman
 */
public class TaggedAxis1D extends Axis1D
{
    protected Map<String, Tag> tagMap;
    protected Map<String, Constraint> constraintMap;

    // a view of the Tags in tagMap to avoid constantly calling
    // Collections.unmodifiableCollection( this.tagMap.values( ) )
    protected List<Tag> tags;
    protected Collection<Constraint> constraints;

    protected boolean linkTags = true;

    protected Comparator<Tag> valueComparator = new Comparator<Tag>( )
    {
        @Override
        public int compare( Tag t1, Tag t2 )
        {
            return Double.compare( t1.getValue( ), t2.getValue( ) );
        }
    };

    public TaggedAxis1D( Axis1D parent )
    {
        super( parent );
        super.initialize( parent );
    }

    public TaggedAxis1D( )
    {
        this( null );
    }

    @Override
    protected void initialize( Axis1D parent )
    {
        this.constraintMap = new HashMap<String, Constraint>( );
        this.tagMap = new HashMap<String, Tag>( );
        this.updateTagArray( );
        this.updateConstraintArray( );

        super.initialize( parent );
    }

    @Override
    public TaggedAxis1D clone( )
    {
        TaggedAxis1D axis = new TaggedAxis1D( this );

        // copy tags
        for ( Tag tag : this.getSortedTags( ) )
        {
            axis.addTag( new Tag( tag ) );
        }

        // copy constraints
        for ( Constraint constraint : this.getAllConstraints( ) )
        {
            axis.addConstraint( constraint );
        }

        return axis;
    }

    public boolean isLinkTags( )
    {
        return linkTags;
    }

    public void setLinkTags( boolean link )
    {
        this.linkTags = link;
    }

    public void validateTags( )
    {
        applyTagConstraints( );
        updateTagArray( );
        updateLinkedAxes( );
    }

    public Tag addTag( Tag tag )
    {
        this.tagMap.put( tag.getName( ), tag );
        this.validateTags( );
        return tag;
    }

    public Tag addTag( String name, double value )
    {
        return addTag( new Tag( name, value ) );
    }

    public void removeTag( String id )
    {
        this.tagMap.remove( id );
        this.validateTags( );
    }

    public void removeAllTags( )
    {
        this.tagMap.clear( );
        this.validateTags( );
    }

    public void addConstraint( Constraint constraint )
    {
        this.constraintMap.put( constraint.getName( ), constraint );
        this.updateConstraintArray( );
        this.validateTags( );
    }

    public void removeConstraint( String name )
    {
        this.constraintMap.remove( name );
        this.updateConstraintArray( );
        this.validateTags( );
    }

    public Collection<Constraint> getAllConstraints( )
    {
        return constraints;
    }

    public void removeAllConstraints( )
    {
        this.constraintMap.clear( );
        this.updateConstraintArray( );
        this.validateTags( );
    }

    public Tag getTag( String id )
    {
        return id == null ? null : this.tagMap.get( id );
    }

    public List<Tag> getSortedTags( )
    {
        return tags;
    }

    public void applyTagConstraints( )
    {
        for ( Constraint constraint : constraints )
        {
            constraint.applyConstraint( this );
        }
    }

    protected void updateTagArray( )
    {
        List<Tag> sortedTags = new ArrayList<Tag>( );
        sortedTags.addAll( tagMap.values( ) );
        Collections.sort( sortedTags, valueComparator );
        tags = Collections.unmodifiableList( sortedTags );
    }

    protected void updateConstraintArray( )
    {
        constraints = Collections.unmodifiableCollection( this.constraintMap.values( ) );
    }

    @Override
    protected void broadcastAxisUpdateUp( Axis1D source, Set<Axis1D> visited )
    {
        HashSet<Axis1D> visitedCopy = new HashSet<>( visited );

        super.broadcastAxisUpdateUp( source, visited );

        if ( source instanceof TaggedAxis1D )
        {
            broadcastTaggedAxisUpdateUp( ( TaggedAxis1D ) source, visitedCopy );
        }

    }

    protected void broadcastTaggedAxisUpdateUp( TaggedAxis1D source, Set<Axis1D> visited )
    {
        broadcastTaggedAxisUpdateUp0( source, visited );
    }

    protected void broadcastTaggedAxisUpdateUp0( TaggedAxis1D source, Set<Axis1D> visited )
    {
        TaggedAxis1D parentTaggedAxis = getLinkedParentAxis( );

        if ( parentTaggedAxis != null )
        {
            parentTaggedAxis.broadcastTaggedAxisUpdateUp0( source, visited );
        }
        else
        {
            taggedAxisUpdated( source, visited );
        }
    }

    protected TaggedAxis1D getLinkedParentAxis( )
    {
        if ( this.parentAxis != null && this.parentAxis instanceof TaggedAxis1D )
        {
            TaggedAxis1D parentTaggedAxis = ( TaggedAxis1D ) this.parentAxis;
            if ( parentTaggedAxis.isLinkTags( ) )
            {
                return parentTaggedAxis;
            }
        }

        return null;
    }

    // recursively apply the update to all linked children
    protected void taggedAxisUpdated( TaggedAxis1D source, Set<Axis1D> visited )
    {
        // if we've already been visited, don't revisit and cause a loop
        if ( !visited.add( this ) )
        {
            return;
        }

        // update ourself
        this.taggedAxisUpdated0( source );

        // updated our children
        if ( this.linkChildren )
        {
            for ( Axis1D axis : this.children )
            {
                if ( axis instanceof TaggedAxis1D )
                {
                    ( ( TaggedAxis1D ) axis ).taggedAxisUpdated( source, visited );
                }
            }
        }

        //XXX should axis listeners be fired here like axisUpdated( )?
    }

    protected void taggedAxisUpdated0( TaggedAxis1D axis )
    {
        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        if ( taggedAxis.isLinkTags( ) )
        {
            for ( Tag tag : taggedAxis.getSortedTags( ) )
            {
                Tag myTag = this.getTag( tag.getName( ) );

                if ( myTag != null )
                {
                    myTag.setValue( tag.getValue( ) );
                }
            }

            this.applyTagConstraints( );
        }
    }

    @Override
    public String toString( )
    {
        return String.format( "[%.3f %.3f %d %s]", minValue, maxValue, axisSizePixels, getSortedTags( ) );
    }
}
