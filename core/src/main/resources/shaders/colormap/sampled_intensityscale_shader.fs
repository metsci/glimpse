
uniform sampler2D datatex;
uniform sampler2D huetex;

uniform float dataMin;
uniform float dataMax;

uniform float alpha;

void main()
{
    float hueVal = texture2D( huetex, gl_TexCoord[0].st ).r;
    float dataVal = texture2D( datatex, gl_TexCoord[0].st ).r;
    float normalizedVal = ( dataVal - dataMin ) / ( dataMax - dataMin );
    clamp( normalizedVal, 0.0, 1.0 );

    vec3 hsv = vec3(hueVal, 1.0, normalizedVal);
    
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(hsv.xxx + K.xyz) * 6.0 - K.www);
    vec3 color = hsv.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), hsv.y);

    gl_FragColor.rgb = color;
    gl_FragColor.a = alpha;
}