/*
 * Adapted from: http://www.rgagnon.com/javadetails/java-0416.html
 *
 * According to the site FAQ (http://www.rgagnon.com/varia/faq-e.htm#license)
 * while the site is licensed under CC (http://creativecommons.org/licenses/by-nc-sa/2.0/)
 * the individual how-to articles have no restriction on use.
 */
package com.metsci.glimpse.dspl.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum
{
    public static byte[] createChecksum( InputStream in ) throws NoSuchAlgorithmException, IOException
    {
        BufferedInputStream bin = new BufferedInputStream( in );

        byte[] buffer = new byte[1024 * 100];
        MessageDigest complete = MessageDigest.getInstance( "MD5" );
        int numRead;
        do
        {
            numRead = bin.read( buffer );
            if ( numRead > 0 )
            {
                complete.update( buffer, 0, numRead );
            }
        }
        while ( numRead != -1 );
        bin.close( );
        return complete.digest( );
    }

    public static byte[] createChecksum( String filename ) throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        return createChecksum( new FileInputStream( filename ) );
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum( String filename ) throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        return getMD5Checksum( createChecksum( filename ) );
    }

    public static String getMD5Checksum( InputStream in ) throws NoSuchAlgorithmException, IOException
    {
        return getMD5Checksum( createChecksum( in ) );
    }

    public static String getMD5Checksum( byte[] b )
    {
        String result = "";
        for ( int i = 0; i < b.length; i++ )
        {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16 ).substring( 1 );
        }
        return result;
    }
}
