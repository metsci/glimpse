
uniform sampler2D tex;
uniform sampler1D colortex_high;
uniform sampler1D colortex_low;

uniform float dataMin;
uniform float dataZero;
uniform float dataMax;

uniform float alpha;

void main()
{
    float dataVal = texture2D( tex, gl_TexCoord[0].st ).r;
    
    if ( dataVal > dataZero )
    {
        float normalizedVal = ( dataVal - dataZero ) / ( dataMax - dataZero );
    	clamp( normalizedVal, 0.0, 1.0 );
    	
    	vec4 color = texture1D( colortex_high, normalizedVal );
        gl_FragColor = color;
        gl_FragColor.a = alpha;
    }
    else
    {
        float normalizedVal = ( dataZero - dataVal ) / ( dataZero - dataMin );
    	clamp( normalizedVal, 0.0, 1.0 );
    	
    	vec4 color = texture1D( colortex_low, normalizedVal );
        gl_FragColor = color;
        gl_FragColor.a = alpha;
    }
}