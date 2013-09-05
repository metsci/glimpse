package com.metsci.glimpse.canvas;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;

public class CanvasUtil
{
    public static void addDisposeListener( final JFrame frame, final NewtGlimpseCanvas canvas, final GLAutoDrawable sharedContextSource )
    {
        // Removing the canvas from the frame may prevent X11 errors (see http://tinyurl.com/m4rnuvf)
        // This listener must be added before adding the SwingGlimpseCanvas to the frame because
        // NEWTGLCanvas adds its own WindowListener and this WindowListener must receive the WindowEvent first.
        frame.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                // dispose of resources associated with the canvas
                canvas.dispose( );
                
                // destroy the source of the shared glContext
                sharedContextSource.destroy( );
                
                // remove the canvas from the frame
                frame.remove( canvas );
            }
        } );
    }
}
