package com.metsci.glimpse.context;

import java.util.Collections;
import java.util.List;

public class UnmodifiableTargetStack implements GlimpseTargetStack
{
    private GlimpseTargetStack delegate;

    public UnmodifiableTargetStack( GlimpseTargetStack stack )
    {
        this.delegate = stack;
    }
    
    @Override
    public GlimpseTargetStack push( GlimpseTarget target, GlimpseBounds bounds )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public GlimpseTargetStack push( GlimpseTarget target )
    {
        throw new UnsupportedOperationException( );

    }

    @Override
    public GlimpseTargetStack push( GlimpseTargetStack stack )
    {
        throw new UnsupportedOperationException( );

    }

    @Override
    public GlimpseTargetStack pop( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public GlimpseTarget getTarget( )
    {
        return delegate.getTarget( );
    }

    @Override
    public GlimpseBounds getBounds( )
    {
        return delegate.getBounds( );
    }

    @Override
    public List<GlimpseTarget> getTargetList( )
    {
        return Collections.unmodifiableList( delegate.getTargetList( ) );
    }

    @Override
    public List<GlimpseBounds> getBoundsList( )
    {
        return Collections.unmodifiableList( delegate.getBoundsList( ) );

    }

    @Override
    public int getSize( )
    {
        return delegate.getSize( );
    }
}