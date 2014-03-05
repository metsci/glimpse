package com.metsci.glimpse.canvas;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.gl.GLRunnable;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;

public abstract class AbstractGlimpseCanvas implements GlimpseCanvas
{
    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;

    protected LayoutManager layoutManager;

    protected List<GLRunnable> disposeListeners;

    public AbstractGlimpseCanvas( )
    {
        this.layoutManager = new LayoutManager( );
        this.disposeListeners = new CopyOnWriteArrayList<GLRunnable>( );
    }

    @Override
    public boolean isEventConsumer( )
    {
        return isEventConsumer;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        this.isEventConsumer = consume;
    }

    @Override
    public boolean isEventGenerator( )
    {
        return isEventGenerator;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        this.isEventGenerator = generate;
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        layoutManager.addLayout( layout );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        layoutManager.addLayout( layout, zOrder );
    }

    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        layoutManager.setZOrder( layout, zOrder );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        layoutManager.removeLayout( layout );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        // layoutManager returns an unmodifiable list, thus this cast is typesafe
        // (there is no way for the recipient of the List<GlimpseTarget> view to
        // add GlimpseTargets which are not GlimpseLayouts to the list)
        return ( List ) this.layoutManager.getLayoutList( );
    }

    @Override
    public void removeAllLayouts( )
    {
        layoutManager.removeAllLayouts( );
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        this.disposeListeners.add( runnable );
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseTarget target : this.layoutManager.getLayoutList( ) )
        {
            target.setLookAndFeel( laf );
        }
    }
}