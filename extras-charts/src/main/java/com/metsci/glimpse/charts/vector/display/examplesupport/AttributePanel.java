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

import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;

import java.awt.BorderLayout;
import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Displays the attributes and vertices of the selected GeoObject.
 * 
 * @author Cunningham
 *
 * @param <V>
 */
@SuppressWarnings( "serial" )
public class AttributePanel<V extends GeoObject> extends JPanel implements UpdateListener<SelectedShapeChange<V>>
{

    private JTextArea textArea;

    public AttributePanel( )
    {
        setLayout( new BorderLayout( ) );

        textArea = new JTextArea( );
        JScrollPane scroll = new JScrollPane( textArea );
        add( scroll, BorderLayout.CENTER );
    }

    public UpdateListener<SelectedShapeChange<V>> getSelectedShapeListener( )
    {
        return this;
    }

    public void updateOccurred( SelectedShapeChange<V> ssc )
    {
        newShape( ssc.getIndexInFile( ), ssc.getGeoObject( ) );
    }

    private void newShape( int indexInFile, V geoObject )
    {
        StringBuilder sb = new StringBuilder( );
        sb.append( geoObject.toString( ) );

        Collection<? extends GeoShape> shapeList = geoObject.getGeoShapes( );
        for ( GeoShape shape : shapeList )
        {
            int pointSize = shape.getPointSize( );
            sb.append( "\nShape: " ).append( shape.getShapeType( ) ).append( "; pt size: " ).append( pointSize ).append( "; " ).append( shape.getNumCoordinates( ) ).append( " points" ).append( "\n" );
            int coordCount = shape.getNumCoordinates( );
            for ( int i = 0; i < coordCount; i++ )
            {
                sb.append( "\tlat,lon: " );
                sb.append( shape.getVertex( 1, i ) );
                sb.append( ", " );
                sb.append( shape.getVertex( 0, i ) );
                for ( int d = 2; d < pointSize; d++ )
                {
                    sb.append( ", " );
                    sb.append( shape.getVertex( d, i ) );
                }
                sb.append( "\n" );
            }
        }
        textArea.setText( sb.toString( ) );
        textArea.setCaretPosition( 0 );
    }
}
