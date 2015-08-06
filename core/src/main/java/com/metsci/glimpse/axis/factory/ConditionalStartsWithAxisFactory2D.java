package com.metsci.glimpse.axis.factory;

import static com.metsci.glimpse.context.TargetStackUtil.*;

import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * A {@link ConditionalAxisFactory2D} which chooses the AxisFactory2D to use based on
 * whether the query GlimpseTargetStack starts with the associated GlimpseTargetStack.
 * 
 * @author ulman
 */
public class ConditionalStartsWithAxisFactory2D extends ConditionalAxisFactory2D
{
    public ConditionalStartsWithAxisFactory2D( )
    {
        super( );
    }

    public ConditionalStartsWithAxisFactory2D( GlimpseTargetStack stack, AxisFactory2D factory )
    {
        super( stack, factory );
    }
    
    @Override
    protected boolean isConditionMet( GlimpseTargetStack stack, GlimpseTargetStack candidate )
    {
        return startsWith( stack, candidate );
    }
}