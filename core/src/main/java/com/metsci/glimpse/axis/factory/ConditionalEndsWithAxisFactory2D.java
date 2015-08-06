package com.metsci.glimpse.axis.factory;

import static com.metsci.glimpse.context.TargetStackUtil.*;

import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * A {@link ConditionalAxisFactory2D} which chooses the AxisFactory2D to use based on
 * whether the query GlimpseTargetStack ends with the associated GlimpseTargetStack.
 * 
 * @author ulman
 */
public class ConditionalEndsWithAxisFactory2D extends ConditionalAxisFactory2D
{
    public ConditionalEndsWithAxisFactory2D( )
    {
        super( );
    }

    public ConditionalEndsWithAxisFactory2D( GlimpseTargetStack stack, AxisFactory2D factory )
    {
        super( stack, factory );
    }
    
    @Override
    protected boolean isConditionMet( GlimpseTargetStack stack, GlimpseTargetStack candidate )
    {
        return endsWith( stack, candidate );
    }
}