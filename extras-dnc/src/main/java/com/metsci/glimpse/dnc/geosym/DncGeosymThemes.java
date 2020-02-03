/*
 * Copyright (c) 2019, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.dnc.geosym;

public class DncGeosymThemes
{

    public static final DncGeosymTheme DNC_THEME_STANDARD = new DncGeosymTheme( DncGeosymThemes.class.getResource( "themes/standard/color.txt" ),
                                                                                DncGeosymThemes.class.getResource( "themes/standard/line-area-styles.csv" ),
                                                                                symbolId -> DncGeosymThemes.class.getResource( "themes/standard/cgm/" + symbolId + ".cgm" ),
                                                                                symbolId -> DncGeosymThemes.class.getResource( "themes/standard/svg/" + symbolId + ".svg" ) );

    public static final DncGeosymTheme DNC_THEME_NIGHT = new DncGeosymTheme( DncGeosymThemes.class.getResource( "themes/night/color.txt" ),
                                                                             DncGeosymThemes.class.getResource( "themes/night/line-area-styles.csv" ),
                                                                             symbolId -> DncGeosymThemes.class.getResource( "themes/standard/cgm/" + symbolId + ".cgm" ),
                                                                             symbolId -> DncGeosymThemes.class.getResource( "themes/night/svg/" + symbolId + ".svg" ) );

}
