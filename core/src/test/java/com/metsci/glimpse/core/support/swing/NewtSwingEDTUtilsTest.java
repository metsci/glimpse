package com.metsci.glimpse.core.support.swing;

import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.modalBlockedStatusFn;
import static com.metsci.glimpse.core.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.DEFINITELY_NOT_BLOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Window;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

public class NewtSwingEDTUtilsTest
{

    @Test
    void modalBlockedStatusFnShouldBasicallyWork( )
    {
        Window w = new JFrame( );
        try
        {
            assertEquals( DEFINITELY_NOT_BLOCKED, modalBlockedStatusFn.apply( w ) );
        }
        finally
        {
            w.dispose( );
        }
    }

}

