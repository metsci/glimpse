package com.metsci.glimpse.timing;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;

public class FpsPrinter extends GlimpsePainterBase
{

    protected long startTime_PMILLIS;
    protected long frameCount;


    public FpsPrinter( )
    {
        this.startTime_PMILLIS = -1;
        this.frameCount = 0;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        long frameTime_PMILLIS = System.currentTimeMillis( );

        this.frameCount++;

        if ( this.startTime_PMILLIS < 0 )
        {
            this.startTime_PMILLIS = frameTime_PMILLIS;
            this.frameCount = 0;
        }
        else if ( this.frameCount >= 50 || frameTime_PMILLIS >= this.startTime_PMILLIS + 5000 )
        {
            double avgFrameDuration_MILLIS = ( frameTime_PMILLIS - this.startTime_PMILLIS ) / ( ( double ) this.frameCount );
            double avgFps = 1000.0 / avgFrameDuration_MILLIS;
            System.err.format( "%9.1f ms/frame  (%.1f fps)\n", avgFrameDuration_MILLIS, avgFps );

            this.startTime_PMILLIS = frameTime_PMILLIS;
            this.frameCount = 0;
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    { }

}
