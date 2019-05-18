package com.metsci.glimpse;

import java.io.IOException;

public class Test
{
    public static void main( String[] args ) throws IOException
    {
        System.out.println( Test.class.getClassLoader( ).getResourceAsStream( "/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getClassLoader( ).getResourceAsStream( "fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getClassLoader( ).getResource( "/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getClassLoader( ).getResource( "fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( ClassLoader.getSystemResources( "/fonts/bitstream/VeraBd.ttf" ).hasMoreElements( ) );
        System.out.println( ClassLoader.getSystemResources( "fonts/bitstream/VeraBd.ttf" ).hasMoreElements( ) );
        System.out.println( ModuleLayer.boot( ).findModule( "com.metsci.glimpse.core" ).get( ).getResourceAsStream( "/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( ModuleLayer.boot( ).findModule( "com.metsci.glimpse.core" ).get( ).getResourceAsStream( "fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getModule( ).getResourceAsStream( "/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getModule( ).getResourceAsStream( "fonts/bitstream/VeraBd.ttf" ) );

        System.out.println( Test.class.getClassLoader( ).getResourceAsStream( "/com/metsci/glimpse/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getClassLoader( ).getResourceAsStream( "com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getClassLoader( ).getResource( "/com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getClassLoader( ).getResource( "com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( ClassLoader.getSystemResources( "/com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ).hasMoreElements( ) );
        System.out.println( ClassLoader.getSystemResources( "com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ).hasMoreElements( ) );
        System.out.println( ModuleLayer.boot( ).findModule( "com.metsci.glimpse.core" ).get( ).getResourceAsStream( "/com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( ModuleLayer.boot( ).findModule( "com.metsci.glimpse.core" ).get( ).getResourceAsStream( "com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getModule( ).getResourceAsStream( "/com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
        System.out.println( Test.class.getModule( ).getResourceAsStream( "com/metsci/glimpse/fonts/fonts/bitstream/VeraBd.ttf" ) );
    }
}
