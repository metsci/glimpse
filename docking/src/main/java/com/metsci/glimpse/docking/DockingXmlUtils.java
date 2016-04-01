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

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingXmlUtils
{

    // Read
    //

    public static GroupArrangement readArrangementXml( URL url ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( url ) );
    }

    public static GroupArrangement readArrangementXml( File file ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( file ) );
    }

    public static GroupArrangement readArrangementXml( Reader reader ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( reader ) );
    }

    public static GroupArrangement readArrangementXml( InputStream stream ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( stream ) );
    }

    public static Unmarshaller newJaxbUnmarshaller( ) throws JAXBException, IOException
    {
        Unmarshaller unmarshaller = JAXBContext.newInstance( GroupArrangement.class, FrameArrangement.class, DockerArrangementNode.class, DockerArrangementSplit.class, DockerArrangementTile.class ).createUnmarshaller( );
        return unmarshaller;
    }

    protected static GroupArrangement castToArrangement( Object object )
    {
        if ( object instanceof GroupArrangement )
        {
            return ( GroupArrangement ) object;
        }
        else if ( object instanceof JAXBElement )
        {
            return castToArrangement( ( ( JAXBElement<?> ) object ).getValue( ) );
        }
        else
        {
            throw new ClassCastException( "Object is neither a " + GroupArrangement.class.getName( ) + " nor a " + JAXBElement.class.getName( ) + ": classname = " + object.getClass( ).getName( ) );
        }
    }


    // Write
    //

    public static void writeArrangementXml( GroupArrangement groupArr, File file ) throws JAXBException, IOException
    {
        newJaxbMarshaller( ).marshal( groupArr, file );
    }

    public static void writeArrangementXml( GroupArrangement groupArr, Writer writer ) throws JAXBException, IOException
    {
        newJaxbMarshaller( ).marshal( groupArr, writer );
    }

    public static void writeArrangementXml( GroupArrangement groupArr, OutputStream stream ) throws JAXBException, IOException
    {
        newJaxbMarshaller( ).marshal( groupArr, stream );
    }

    public static Marshaller newJaxbMarshaller( ) throws IOException, JAXBException
    {
        Marshaller marshaller = JAXBContext.newInstance( GroupArrangement.class, FrameArrangement.class, DockerArrangementNode.class, DockerArrangementSplit.class, DockerArrangementTile.class ).createMarshaller( );
        marshaller.setProperty( JAXB_FORMATTED_OUTPUT, true );
        return marshaller;
    }

}
