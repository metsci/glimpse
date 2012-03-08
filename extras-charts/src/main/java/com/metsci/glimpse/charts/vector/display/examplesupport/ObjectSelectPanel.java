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
package com.metsci.glimpse.charts.vector.display.examplesupport;

import com.metsci.glimpse.charts.vector.iteration.GeoRecordList;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Displays a list box listing all the geo objects. Publishes selections on the 
 * list box to publisher passed in at the constructor.
 * 
 * @author Cunningham 
 */
@SuppressWarnings("serial")
public class ObjectSelectPanel<V extends GeoObject> extends JPanel
{
    private static Logger logger = Logger.getLogger( ObjectSelectPanel.class.toString( ) );

    private JList list;
    private AListModel listModel;
    private GeoRecordList<V> recordList;

    private boolean dontpublish = false;
    private boolean iAmPublishing = false;

    public ObjectSelectPanel( final UpdatePublisher<SelectedShapeChange<V>> publisher )
    {
        setLayout( new BorderLayout( ) );

        listModel = new AListModel( );
        list = new JList( listModel );
        list.addListSelectionListener( new ListSelectionListener( )
        {
            public void valueChanged( ListSelectionEvent e )
            {
                if ( e.getValueIsAdjusting( ) ) return;
                if ( dontpublish ) return;
                try
                {
                    iAmPublishing = true;
                    int selectedIndex = list.getSelectedIndex( );
                    publisher.notifyUpdateOccurred( new SelectedShapeChange<V>( selectedIndex, recordList.get( selectedIndex ) ) );
                    iAmPublishing = false;
                }
                catch ( IOException ex )
                {
                    logger.log( Level.WARNING, null, ex );
                }
            }
        } );
        JScrollPane scroll = new JScrollPane( list );
        add( scroll, BorderLayout.CENTER );
    }

    public void setList( GeoRecordList<V> recordList ) throws IOException
    {
        this.recordList = recordList;
        String[] textDescription = new String[recordList.size( )];
        int size = recordList.size( );
        for ( int i = 0; i < size; i++ )
        {
            V geoObject = recordList.get( i );
            StringBuilder sb = new StringBuilder( );
            sb.append( i );
            sb.append( ": " );
            sb.append( geoObject.getGeoFeatureType( ).name( ) );
            sb.append( ": " );
            for ( GeoShape shape : geoObject.getGeoShapes( ) )
            {
                sb.append( ' ' );
                sb.append( shape.getShapeType( ).name( ) );
            }
            textDescription[i] = sb.toString( );
        }
        listModel.setTextDescription( textDescription );
    }

    private void selectObject( int indexInFile, V encObject )
    {
        dontpublish = true;
        list.setSelectedIndex( indexInFile );
        list.ensureIndexIsVisible( indexInFile );
        dontpublish = false;
    }

    private static class AListModel extends AbstractListModel
    {
        private String[] textDescription;

        public AListModel( )
        {
            this( new String[0] );
        }

        public AListModel( String[] textDescription )
        {
            setTextDescription( textDescription );
        }

        public int getSize( )
        {
            return textDescription.length;
        }

        public Object getElementAt( int index )
        {
            return textDescription[index];
        }

        public void setTextDescription( String[] textDescription )
        {
            this.textDescription = textDescription;
            this.fireContentsChanged( this, 0, textDescription.length - 1 );
        }

    }

    public UpdateListener<SelectedShapeChange<V>> getSelectedShapeListener( )
    {
        return new UpdateListener<SelectedShapeChange<V>>( )
        {
            public void updateOccurred( SelectedShapeChange<V> ssc )
            {
                if ( !iAmPublishing )
                {
                    ObjectSelectPanel.this.selectObject( ssc.getIndexInFile( ), ssc.getGeoObject( ) );
                }
            }
        };
    }
}
