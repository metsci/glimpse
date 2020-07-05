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
package com.metsci.glimpse.painter.texture;

import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;
import static com.metsci.glimpse.support.DisposableUtils.addAxisListener1D;
import static com.metsci.glimpse.util.GeneralUtils.floatsEqual;

import java.nio.FloatBuffer;
import java.util.List;

import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.FloatTexture1D;
import com.metsci.glimpse.gl.texture.FloatTexture1D.MutatorFloat1D;
import com.metsci.glimpse.util.primitives.Floats;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.var.DisposableGroup;

/**
 * A HeatMapPainter whose coloring is controlled via a
 * {@link com.metsci.glimpse.axis.tagged.TaggedAxis1D}.
 *
 * @author ulman
 */
public class TaggedHeatMapPainter extends HeatMapPainter
{
    protected static final int tagFractionsTexUnit = colormapTexUnit + 1;
    protected static final int tagValuesTexUnit = tagFractionsTexUnit + 1;


    protected final DisposableGroup disposables;

    protected Floats tagStates;
    protected FloatTexture1D tagFractionsTable;
    protected FloatTexture1D tagValuesTable;


    public TaggedHeatMapPainter( TaggedAxis1D colorAxis )
    {
        super( new MultiTagHeatMapProgram( valuesTexUnit, colormapTexUnit, tagFractionsTexUnit, tagValuesTexUnit ) );

        this.disposables = new DisposableGroup( );

        this.tagStates = new FloatsArray( );
        this.tagFractionsTable = new FloatTexture1D( 0 );
        this.tagValuesTable = new FloatTexture1D( 0 );

        this.updateTagTables( colorAxis );
        this.disposables.add( addAxisListener1D( colorAxis, ( ) ->
        {
            this.updateTagTables( colorAxis );
        } ) );
    }

    protected void updateTagTables( TaggedAxis1D colorAxis )
    {
        Floats newTagStates = getTagStates( colorAxis );
        if ( !allEqual( newTagStates, this.tagStates ) )
        {
            this.tagStates = newTagStates;

            int tableSize = this.tagStates.n( ) / 2;
            int tableCapacity = this.tagFractionsTable.getDimensionSize( 0 );
            if ( tableSize != tableCapacity )
            {
                this.removeNonDrawableTexture( this.tagFractionsTable );
                this.tagFractionsTable = new FloatTexture1D( tableSize );
                this.addNonDrawableTexture( this.tagFractionsTable, tagFractionsTexUnit );

                this.removeNonDrawableTexture( this.tagValuesTable );
                this.tagValuesTable = new FloatTexture1D( tableSize );
                this.addNonDrawableTexture( this.tagValuesTable, tagValuesTexUnit );
            }

            this.tagFractionsTable.mutate( new MutatorFloat1D( )
            {
                @Override
                public void mutate( FloatBuffer buffer, int n0 )
                {
                    for ( int i = tableSize - 1; i >= 0; i-- )
                    {
                        float tagFraction = tagStates.v( 2*i + 0 );
                        buffer.put( tagFraction );
                    }
                }
            } );

            this.tagValuesTable.mutate( new MutatorFloat1D( )
            {
                @Override
                public void mutate( FloatBuffer buffer, int n0 )
                {
                    for ( int i = tableSize - 1; i >= 0; i-- )
                    {
                        float tagValue = tagStates.v( 2*i + 1 );
                        buffer.put( tagValue );
                    }
                }
            } );
        }
    }

    protected static boolean allEqual( Floats a, Floats b )
    {
        if ( a.n( ) != b.n( ) )
        {
            return false;
        }

        int n = a.n( );
        for ( int i = 0; i < n; i++ )
        {
            if ( !floatsEqual( a.v( i ), b.v( i ) ) )
            {
                return false;
            }
        }

        return true;
    }

    protected static Floats getTagStates( TaggedAxis1D axis )
    {
        List<Tag> tags = axis.getSortedTags( );
        FloatsArray states = new FloatsArray( 2 * tags.size( ) );
        for ( Tag tag : tags )
        {
            Object attr = tag.getAttribute( TEX_COORD_ATTR );
            if ( attr instanceof Number )
            {
                float fraction = ( ( Number ) attr ).floatValue( );
                float value = ( float ) tag.getValue( );
                states.append( fraction );
                states.append( value );
            }
        }
        return states;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        super.doDispose( context );

        this.disposables.dispose( );

        this.removeAllDrawableTextures( );
        this.removeAllNonDrawableTextures( );

        this.tagFractionsTable.dispose( context.getGLContext( ) );
        this.tagValuesTable.dispose( context.getGLContext( ) );
    }

}
