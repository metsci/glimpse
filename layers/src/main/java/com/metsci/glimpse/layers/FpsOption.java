package com.metsci.glimpse.layers;

import java.util.Collection;

public class FpsOption implements GuiOption
{

    public static FpsOption FPS( double fps )
    {
        return new FpsOption( fps );
    }

    public static double findFps( Collection<? extends GuiOption> guiOptions, double fpsDefault )
    {
        for ( GuiOption option : guiOptions )
        {
            if ( option instanceof FpsOption )
            {
                return ( ( FpsOption ) option ).fps;
            }
        }
        return fpsDefault;
    }


    public final double fps;


    public FpsOption( double fps )
    {
        this.fps = fps;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 15527;
        int result = 1;
        result = prime * result + Double.hashCode( this.fps );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        FpsOption other = ( FpsOption ) o;
        return ( Double.compare( other.fps, this.fps ) == 0 );
    }

}
