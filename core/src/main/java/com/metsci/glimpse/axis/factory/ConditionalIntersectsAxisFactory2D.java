package com.metsci.glimpse.axis.factory;

import static com.metsci.glimpse.context.TargetStackUtil.*;

import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * A {@link ConditionalAxisFactory2D} which chooses the AxisFactory2D to use based on
 * whether the query GlimpseTargetStack has any match with the associated GlimpseTargetStack.
 * 
 * @author ulman
 */
public class ConditionalIntersectsAxisFactory2D extends ConditionalAxisFactory2D
{
    @Override
    protected boolean isConditionMet( GlimpseTargetStack stack, GlimpseTargetStack candidate )
    {
        return intersects( stack, candidate );
    }
}