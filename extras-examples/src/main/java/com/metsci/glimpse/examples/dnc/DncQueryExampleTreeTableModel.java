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
package com.metsci.glimpse.examples.dnc;

import static com.metsci.glimpse.dnc.util.DncMiscUtils.newThreadFactory;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metsci.glimpse.dnc.DncFeature;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.facc.FaccAttr;
import com.metsci.glimpse.dnc.facc.FaccFeature;

public class DncQueryExampleTreeTableModel extends AbstractTreeTableModel
{

    protected static class Feature
    {
        public final String fcode;
        public final String faccName;
        public final List<Attr> attrs;

        public Feature( String fcode, String faccName, Collection<Attr> attrs )
        {
            this.fcode = fcode;
            this.faccName = faccName;
            this.attrs = ImmutableList.copyOf( attrs );
        }
    }

    protected static class Attr
    {
        public final String code;
        public final String faccName;
        public final Object value;

        public Attr( String code, String faccName, Object value )
        {
            this.code = code;
            this.faccName = faccName;
            this.value = value;
        }
    }


    protected final Map<String,FaccFeature> faccFeatures;
    protected final Map<String,FaccAttr> faccAttrs;

    // Accessed while synchronized
    protected final Object workingMutex;
    protected final Map<DncChunkKey,List<Feature>> workingMap;

    // Written while synchronized, but read with volatile read
    protected volatile List<Feature> workingList;

    // Accessed only on the Swing thread
    protected List<Feature> featuresList;

    protected final Runnable stopCommitTimer;


    protected DncQueryExampleTreeTableModel( Map<String,FaccFeature> faccFeatures, Map<String,FaccAttr> faccAttrs )
    {
        super( emptyList( ) );

        this.faccFeatures = ImmutableMap.copyOf( faccFeatures );
        this.faccAttrs = ImmutableMap.copyOf( faccAttrs );

        this.workingMutex = new Object( );
        this.workingMap = new HashMap<>( );

        this.workingList = emptyList( );

        this.featuresList = workingList;

        Runnable commitWorkingList = ( ) ->
        {
            SwingUtilities.invokeLater( ( ) ->
            {
                // Volatile read
                List<Feature> newFeaturesList = workingList;
                if ( newFeaturesList != featuresList )
                {
                    featuresList = newFeaturesList;
                    root = featuresList;
                    modelSupport.fireNewRoot( );
                }
            } );
        };
        ScheduledExecutorService commitTimer = newSingleThreadScheduledExecutor( newThreadFactory( "DncQueryExampleTreeTableModel", true ) );
        ScheduledFuture<?> commitFuture = commitTimer.scheduleAtFixedRate( commitWorkingList, 0, 30, MILLISECONDS );

        this.stopCommitTimer = ( ) ->
        {
            commitFuture.cancel( false );
            commitTimer.shutdown( );
        };
    }

    protected void updateFeatures( Consumer<Map<DncChunkKey,List<Feature>>> updateFn )
    {
        synchronized ( workingMutex )
        {
            updateFn.accept( workingMap );

            List<Feature> newWorkingList = new ArrayList<>( );
            workingMap.values( ).forEach( newWorkingList::addAll );
            newWorkingList.sort( ( a, b ) ->
            {
                return ( a.faccName ).compareTo( b.faccName );
            } );

            // Volatile write -- will get picked up later by the commit timer
            workingList = unmodifiableList( newWorkingList );
        }
    }

    public void dispose( )
    {
        stopCommitTimer.run( );
    }

    public void retainChunks( Set<DncChunkKey> chunkKeys )
    {
        updateFeatures( ( map ) ->
        {
            map.keySet( ).retainAll( chunkKeys );
        } );
    }

    public void setChunkFeatures( DncChunkKey chunkKey, Collection<DncFeature> features )
    {
        List<Feature> chunkFeatures = toTableFeatures( features, faccFeatures, faccAttrs );
        updateFeatures( ( map ) ->
        {
            map.put( chunkKey, chunkFeatures );
        } );
    }

    protected static List<Feature> toTableFeatures( Collection<DncFeature> queryFeatures, Map<String,FaccFeature> faccFeatures, Map<String,FaccAttr> faccAttrs )
    {
        List<Feature> features = new ArrayList<>( );
        queryFeatures.forEach( ( queryFeature ) ->
        {
            String fcode = queryFeature.fcode;

            Collection<Attr> attrs = new ArrayList<>( );
            queryFeature.getAttrs( ).forEach( ( attrCode, attrValue ) ->
            {
                FaccAttr faccAttr = faccAttrs.get( attrCode );
                if ( faccAttr == null )
                {
                    attrs.add( new Attr( attrCode, null, attrValue ) );
                }
                else
                {
                    attrs.add( new Attr( attrCode, faccAttr.name, faccAttr.translateValue( attrValue ) ) );
                }
            } );

            FaccFeature faccFeature = faccFeatures.get( fcode );
            if ( faccFeature == null )
            {
                features.add( new Feature( fcode, null, attrs ) );
            }
            else
            {
                features.add( new Feature( fcode, faccFeature.name, attrs ) );
            }
        } );
        return features;
    }

    @Override
    public int getChildCount( Object parent )
    {
        if ( parent == featuresList )
        {
            return featuresList.size( );
        }
        else if ( parent instanceof Feature )
        {
            Feature feature = ( Feature ) parent;
            return feature.attrs.size( );
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getChild( Object parent, int index )
    {
        if ( parent == featuresList )
        {
            return featuresList.get( index );
        }
        else if ( parent instanceof Feature )
        {
            Feature feature = ( Feature ) parent;
            return feature.attrs.get( index );
        }
        else
        {
            return null;
        }
    }

    @Override
    public int getIndexOfChild( Object parent, Object child )
    {
        if ( parent == featuresList )
        {
            return featuresList.indexOf( child );
        }
        else if ( parent instanceof Feature )
        {
            Feature feature = ( Feature ) parent;
            return feature.attrs.indexOf( child );
        }
        else
        {
            return -1;
        }
    }

    @Override
    public int getColumnCount( )
    {
        return 2;
    }

    @Override
    public String getColumnName( int column )
    {
        switch ( column )
        {
            case 0: return "Name";
            case 1: return "Value";
            default: return null;
        }
    }

    @Override
    public Object getValueAt( Object node, int column )
    {
        if ( node instanceof Feature )
        {
            Feature feature = ( Feature ) node;
            switch ( column )
            {
                case 0: return feature.faccName;
                default: return null;
            }
        }
        else if ( node instanceof Attr )
        {
            Attr attr = ( Attr ) node;
            switch ( column )
            {
                case 0: return attr.faccName;
                case 1: return attr.value;
                default: return null;
            }
        }
        else
        {
            return null;
        }
    }

}
