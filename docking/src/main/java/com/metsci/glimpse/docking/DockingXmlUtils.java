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
