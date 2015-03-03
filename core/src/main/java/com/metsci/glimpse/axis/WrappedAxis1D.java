package com.metsci.glimpse.axis;

/**
 * <p>An Axis1D which is intended to be interpreted as wrapping for values outside of its minWrapVal and maxWrapVal.
 *    minWrapVal is considered an inclusive bound, maxWrapVal an exclusive bound.</p>
 * 
 * <p>For example: minWrapVal=-10, maxWrapVal=10, minVal=20, and maxVal=21. The axis should paint data from 0 to 1 
 *    (imagine shifting the minVal/maxVal down by maxWrapVal-minWrapVal until it falls in the correct range).</p>
 * 
 * <p>Another example: minWrapVal=-10, maxWrapVal=10, minVal=-10, maxVal=20. The axis should paint the data twice, from
 *    -10 to 10 then wrapping and painting data from -10 to 10 again.</p>
 * 
 * @author ulman
 */
public class WrappedAxis1D extends Axis1D
{
    private double minWrapVal;
    private double maxWrapVal;
    
    public WrappedAxis1D( double minWrapVal, double maxWrapVal ) {
        this( null, minWrapVal, maxWrapVal );
    }
    
    public WrappedAxis1D( Axis1D parent, double minWrapVal, double maxWrapVal ) {
        super( parent );
        this.minWrapVal = minWrapVal;
        this.maxWrapVal = maxWrapVal;
    }

    public double getWrapMin( )
    {
        return minWrapVal;
    }

    public double getWrapMax( )
    {
        return maxWrapVal;
    }
    
    public void setWrapMin( double min )
    {
        this.minWrapVal = min;
    }
    
    public void setWrapMax( double max )
    {
        this.maxWrapVal = max;
    }
    
    public double getWrapSpan( )
    {
        return getWrapMax( ) - getWrapMin( );
    }
    
    public double getWrappedValue( double value )
    {
        return getWrappedValue( value, false );
    }

    /**
     * @param value the linear axis value to convert
     * @param roundUp if true, values on the seam will return getWrapMax( ) instead of getWrapMin( )
     * @return the value converted to wrapped coordinates
     */
    public double getWrappedValue( double value, boolean roundUp )
    {
        return minWrapVal + getWrappedMod( value, roundUp );
    }
    
    public double getWrappedMod( double value )
    {
        return getWrappedMod( value, false );
    }
    
    /**
     * @param value the linear axis value to convert
     * @param roundUp if true, values on the seam will return getWrapMax( ) instead of getWrapMin( )
     * @return the distance between this value and the left seam
     */
    public double getWrappedMod( double value, boolean roundUp )
    {
        double span = getWrapSpan( );
        double v = value - minWrapVal; // shift minWrapVal to 0
        double mod = v % span;
        return mod + ( ( mod == 0 && roundUp ) || mod < 0 ? span : 0 );
    }
    
    public static void main( String[] args )
    {
        WrappedAxis1D axis = new WrappedAxis1D( -10, 10 );
        
        for ( int i = -11 ; i < 0 ; i++ )
        {
            System.out.println( i + " " + axis.getWrappedValue( i ) );
        }
    }
}
