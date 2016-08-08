/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.support.color;

import static com.metsci.glimpse.support.color.RGBA.fromIntRGB;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * <span style="background:#f0f8ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AliceBlue <br>
 * <span style="background:#faebd7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite <br>
 * <span style="background:#ffefdb">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite1 <br>
 * <span style="background:#eedfcc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite2 <br>
 * <span style="background:#cdc0b0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite3 <br>
 * <span style="background:#8b8378">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite4 <br>
 * <span style="background:#7fffd4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Aquamarine <br>
 * <span style="background:#76eec6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Aquamarine2 <br>
 * <span style="background:#458b74">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Aquamarine4 <br>
 * <span style="background:#f0ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure <br>
 * <span style="background:#e0eeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure2 <br>
 * <span style="background:#c1cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure3 <br>
 * <span style="background:#838b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure4 <br>
 * <span style="background:#f5f5dc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Beige <br>
 * <span style="background:#ffe4c4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque <br>
 * <span style="background:#eed5b7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque2 <br>
 * <span style="background:#cdb79e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque3 <br>
 * <span style="background:#8b7d6b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque4 <br>
 * <span style="background:#000000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Black <br>
 * <span style="background:#ffebcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> BlanchedAlmond <br>
 * <span style="background:#0000ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Blue <br>
 * <span style="background:#0000ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Blue2 <br>
 * <span style="background:#8a2be2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> BlueViolet <br>
 * <span style="background:#a52a2a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown <br>
 * <span style="background:#ff4040">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown1 <br>
 * <span style="background:#ee3b3b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown2 <br>
 * <span style="background:#cd3333">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown3 <br>
 * <span style="background:#8b2323">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown4 <br>
 * <span style="background:#deb887">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood <br>
 * <span style="background:#ffd39b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood1 <br>
 * <span style="background:#eec591">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood2 <br>
 * <span style="background:#cdaa7d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood3 <br>
 * <span style="background:#8b7355">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood4 <br>
 * <span style="background:#5f9ea0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue <br>
 * <span style="background:#98f5ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue1 <br>
 * <span style="background:#8ee5ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue2 <br>
 * <span style="background:#7ac5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue3 <br>
 * <span style="background:#53868b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue4 <br>
 * <span style="background:#7fff00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse <br>
 * <span style="background:#76ee00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse2 <br>
 * <span style="background:#66cd00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse3 <br>
 * <span style="background:#458b00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse4 <br>
 * <span style="background:#d2691e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate <br>
 * <span style="background:#ff7f24">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate1 <br>
 * <span style="background:#ee7621">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate2 <br>
 * <span style="background:#cd661d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate3 <br>
 * <span style="background:#ff7f50">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral <br>
 * <span style="background:#ff7256">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral1 <br>
 * <span style="background:#ee6a50">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral2 <br>
 * <span style="background:#cd5b45">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral3 <br>
 * <span style="background:#8b3e2f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral4 <br>
 * <span style="background:#6495ed">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CornflowerBlue <br>
 * <span style="background:#fff8dc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk <br>
 * <span style="background:#eee8cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk2 <br>
 * <span style="background:#cdc8b1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk3 <br>
 * <span style="background:#8b8878">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk4 <br>
 * <span style="background:#00ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cyan <br>
 * <span style="background:#00eeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cyan2 <br>
 * <span style="background:#00cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cyan3 <br>
 * <span style="background:#00008b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkBlue <br>
 * <span style="background:#008b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkCyan <br>
 * <span style="background:#b8860b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod <br>
 * <span style="background:#ffb90f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod1 <br>
 * <span style="background:#eead0e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod2 <br>
 * <span style="background:#cd950c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod3 <br>
 * <span style="background:#8b6508">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod4 <br>
 * <span style="background:#a9a9a9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGray <br>
 * <span style="background:#006400">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGreen <br>
 * <span style="background:#bdb76b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkKhaki <br>
 * <span style="background:#8b008b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkMagenta <br>
 * <span style="background:#556b2f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen <br>
 * <span style="background:#caff70">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen1 <br>
 * <span style="background:#bcee68">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen2 <br>
 * <span style="background:#a2cd5a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen3 <br>
 * <span style="background:#6e8b3d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen4 <br>
 * <span style="background:#ff8c00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange <br>
 * <span style="background:#ff7f00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange1 <br>
 * <span style="background:#ee7600">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange2 <br>
 * <span style="background:#cd6600">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange3 <br>
 * <span style="background:#8b4500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange4 <br>
 * <span style="background:#9932cc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid <br>
 * <span style="background:#bf3eff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid1 <br>
 * <span style="background:#b23aee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid2 <br>
 * <span style="background:#9a32cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid3 <br>
 * <span style="background:#68228b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid4 <br>
 * <span style="background:#8b0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkRed <br>
 * <span style="background:#e9967a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSalmon <br>
 * <span style="background:#8fbc8f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen <br>
 * <span style="background:#c1ffc1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen1 <br>
 * <span style="background:#b4eeb4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen2 <br>
 * <span style="background:#9bcd9b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen3 <br>
 * <span style="background:#698b69">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen4 <br>
 * <span style="background:#483d8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateBlue <br>
 * <span style="background:#2f4f4f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray <br>
 * <span style="background:#97ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray1 <br>
 * <span style="background:#8deeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray2 <br>
 * <span style="background:#79cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray3 <br>
 * <span style="background:#528b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray4 <br>
 * <span style="background:#00ced1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkTurquoise <br>
 * <span style="background:#9400d3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkViolet <br>
 * <span style="background:#ff1493">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink <br>
 * <span style="background:#ee1289">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink2 <br>
 * <span style="background:#cd1076">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink3 <br>
 * <span style="background:#8b0a50">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink4 <br>
 * <span style="background:#00bfff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue <br>
 * <span style="background:#00b2ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue2 <br>
 * <span style="background:#009acd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue3 <br>
 * <span style="background:#00688b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue4 <br>
 * <span style="background:#696969">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DimGray <br>
 * <span style="background:#1e90ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue <br>
 * <span style="background:#1c86ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue2 <br>
 * <span style="background:#1874cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue3 <br>
 * <span style="background:#104e8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue4 <br>
 * <span style="background:#b22222">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick <br>
 * <span style="background:#ff3030">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick1 <br>
 * <span style="background:#ee2c2c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick2 <br>
 * <span style="background:#cd2626">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick3 <br>
 * <span style="background:#8b1a1a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick4 <br>
 * <span style="background:#fffaf0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> FloralWhite <br>
 * <span style="background:#228b22">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> ForestGreen <br>
 * <span style="background:#dcdcdc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gainsboro <br>
 * <span style="background:#f8f8ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> GhostWhite <br>
 * <span style="background:#ffd700">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold <br>
 * <span style="background:#eec900">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold2 <br>
 * <span style="background:#cdad00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold3 <br>
 * <span style="background:#8b7500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold4 <br>
 * <span style="background:#daa520">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod <br>
 * <span style="background:#ffc125">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod1 <br>
 * <span style="background:#eeb422">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod2 <br>
 * <span style="background:#cd9b1d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod3 <br>
 * <span style="background:#8b6914">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod4 <br>
 * <span style="background:#bebebe">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gray <br>
 * <span style="background:#00ff00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green <br>
 * <span style="background:#00ee00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green2 <br>
 * <span style="background:#00cd00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green3 <br>
 * <span style="background:#008b00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green4 <br>
 * <span style="background:#adff2f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> GreenYellow <br>
 * <span style="background:#f0fff0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew <br>
 * <span style="background:#e0eee0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew2 <br>
 * <span style="background:#c1cdc1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew3 <br>
 * <span style="background:#838b83">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew4 <br>
 * <span style="background:#ff69b4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink <br>
 * <span style="background:#ff6eb4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink1 <br>
 * <span style="background:#ee6aa7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink2 <br>
 * <span style="background:#cd6090">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink3 <br>
 * <span style="background:#8b3a62">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink4 <br>
 * <span style="background:#cd5c5c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed <br>
 * <span style="background:#ff6a6a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed1 <br>
 * <span style="background:#ee6363">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed2 <br>
 * <span style="background:#cd5555">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed3 <br>
 * <span style="background:#8b3a3a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed4 <br>
 * <span style="background:#fffff0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory <br>
 * <span style="background:#eeeee0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory2 <br>
 * <span style="background:#cdcdc1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory3 <br>
 * <span style="background:#8b8b83">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory4 <br>
 * <span style="background:#f0e68c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki <br>
 * <span style="background:#fff68f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki1 <br>
 * <span style="background:#eee685">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki2 <br>
 * <span style="background:#cdc673">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki3 <br>
 * <span style="background:#8b864e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki4 <br>
 * <span style="background:#e6e6fa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Lavender <br>
 * <span style="background:#fff0f5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush <br>
 * <span style="background:#eee0e5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush2 <br>
 * <span style="background:#cdc1c5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush3 <br>
 * <span style="background:#8b8386">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush4 <br>
 * <span style="background:#7cfc00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LawnGreen <br>
 * <span style="background:#fffacd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon <br>
 * <span style="background:#eee9bf">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon2 <br>
 * <span style="background:#cdc9a5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon3 <br>
 * <span style="background:#8b8970">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon4 <br>
 * <span style="background:#add8e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue <br>
 * <span style="background:#bfefff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue1 <br>
 * <span style="background:#b2dfee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue2 <br>
 * <span style="background:#9ac0cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue3 <br>
 * <span style="background:#68838b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue4 <br>
 * <span style="background:#f08080">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCoral <br>
 * <span style="background:#e0ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan <br>
 * <span style="background:#d1eeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan2 <br>
 * <span style="background:#b4cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan3 <br>
 * <span style="background:#7a8b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan4 <br>
 * <span style="background:#eedd82">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod <br>
 * <span style="background:#ffec8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod1 <br>
 * <span style="background:#eedc82">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod2 <br>
 * <span style="background:#cdbe70">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod3 <br>
 * <span style="background:#8b814c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod4 <br>
 * <span style="background:#fafad2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrodYellow <br>
 * <span style="background:#90ee90">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGreen <br>
 * <span style="background:#d3d3d3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGrey <br>
 * <span style="background:#ffb6c1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink <br>
 * <span style="background:#ffaeb9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink1 <br>
 * <span style="background:#eea2ad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink2 <br>
 * <span style="background:#cd8c95">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink3 <br>
 * <span style="background:#8b5f65">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink4 <br>
 * <span style="background:#ffa07a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon <br>
 * <span style="background:#ee9572">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon2 <br>
 * <span style="background:#cd8162">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon3 <br>
 * <span style="background:#8b5742">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon4 <br>
 * <span style="background:#20b2aa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSeaGreen <br>
 * <span style="background:#87cefa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue <br>
 * <span style="background:#b0e2ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue1 <br>
 * <span style="background:#a4d3ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue2 <br>
 * <span style="background:#8db6cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue3 <br>
 * <span style="background:#607b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue4 <br>
 * <span style="background:#8470ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSlateBlue <br>
 * <span style="background:#778899">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSlateGray <br>
 * <span style="background:#b0c4de">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue <br>
 * <span style="background:#cae1ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue1 <br>
 * <span style="background:#bcd2ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue2 <br>
 * <span style="background:#a2b5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue3 <br>
 * <span style="background:#6e7b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue4 <br>
 * <span style="background:#ffffe0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow <br>
 * <span style="background:#eeeed1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow2 <br>
 * <span style="background:#cdcdb4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow3 <br>
 * <span style="background:#8b8b7a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow4 <br>
 * <span style="background:#32cd32">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LimeGreen <br>
 * <span style="background:#faf0e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Linen <br>
 * <span style="background:#ff00ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Magenta <br>
 * <span style="background:#ee00ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Magenta2 <br>
 * <span style="background:#cd00cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Magenta3 <br>
 * <span style="background:#b03060">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon <br>
 * <span style="background:#ff34b3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon1 <br>
 * <span style="background:#ee30a7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon2 <br>
 * <span style="background:#cd2990">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon3 <br>
 * <span style="background:#8b1c62">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon4 <br>
 * <span style="background:#66cdaa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumAquamarine <br>
 * <span style="background:#0000cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumBlue <br>
 * <span style="background:#ba55d3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid <br>
 * <span style="background:#e066ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid1 <br>
 * <span style="background:#d15fee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid2 <br>
 * <span style="background:#b452cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid3 <br>
 * <span style="background:#7a378b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid4 <br>
 * <span style="background:#9370db">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple <br>
 * <span style="background:#ab82ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple1 <br>
 * <span style="background:#9f79ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple2 <br>
 * <span style="background:#8968cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple3 <br>
 * <span style="background:#5d478b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple4 <br>
 * <span style="background:#3cb371">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumSeaGreen <br>
 * <span style="background:#7b68ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumSlateBlue <br>
 * <span style="background:#00fa9a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumSpring <br>
 * <span style="background:#48d1cc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumTurquoise <br>
 * <span style="background:#c71585">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumVioletRed <br>
 * <span style="background:#191970">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MidnightBlue <br>
 * <span style="background:#f5fffa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MintCream <br>
 * <span style="background:#ffe4e1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose <br>
 * <span style="background:#eed5d2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose2 <br>
 * <span style="background:#cdb7b5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose3 <br>
 * <span style="background:#8b7d7b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose4 <br>
 * <span style="background:#ffe4b5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Moccasin <br>
 * <span style="background:#ffdead">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite <br>
 * <span style="background:#eecfa1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite2 <br>
 * <span style="background:#cdb38b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite3 <br>
 * <span style="background:#8b795e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite4 <br>
 * <span style="background:#000080">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Navy <br>
 * <span style="background:#fdf5e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OldLace <br>
 * <span style="background:#6b8e23">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab <br>
 * <span style="background:#c0ff3e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab1 <br>
 * <span style="background:#b3ee3a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab2 <br>
 * <span style="background:#698b22">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab4 <br>
 * <span style="background:#ffa500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange <br>
 * <span style="background:#ee9a00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange2 <br>
 * <span style="background:#cd8500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange3 <br>
 * <span style="background:#8b5a00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange4 <br>
 * <span style="background:#ff4500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed <br>
 * <span style="background:#ee4000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed2 <br>
 * <span style="background:#cd3700">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed3 <br>
 * <span style="background:#8b2500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed4 <br>
 * <span style="background:#da70d6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid <br>
 * <span style="background:#ff83fa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid1 <br>
 * <span style="background:#ee7ae9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid2 <br>
 * <span style="background:#cd69c9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid3 <br>
 * <span style="background:#8b4789">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid4 <br>
 * <span style="background:#eee8aa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGoldenrod <br>
 * <span style="background:#98fb98">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen <br>
 * <span style="background:#9aff9a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen1 <br>
 * <span style="background:#7ccd7c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen3 <br>
 * <span style="background:#548b54">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen4 <br>
 * <span style="background:#afeeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise <br>
 * <span style="background:#bbffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise1 <br>
 * <span style="background:#aeeeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise2 <br>
 * <span style="background:#96cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise3 <br>
 * <span style="background:#668b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise4 <br>
 * <span style="background:#db7093">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed <br>
 * <span style="background:#ff82ab">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed1 <br>
 * <span style="background:#ee799f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed2 <br>
 * <span style="background:#cd6889">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed3 <br>
 * <span style="background:#8b475d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed4 <br>
 * <span style="background:#ffefd5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PapayaWhip <br>
 * <span style="background:#ffdab9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff <br>
 * <span style="background:#eecbad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff2 <br>
 * <span style="background:#cdaf95">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff3 <br>
 * <span style="background:#8b7765">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff4 <br>
 * <span style="background:#cd853f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Peru <br>
 * <span style="background:#ffc0cb">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink <br>
 * <span style="background:#ffb5c5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink1 <br>
 * <span style="background:#eea9b8">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink2 <br>
 * <span style="background:#cd919e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink3 <br>
 * <span style="background:#8b636c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink4 <br>
 * <span style="background:#dda0dd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum <br>
 * <span style="background:#ffbbff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum1 <br>
 * <span style="background:#eeaeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum2 <br>
 * <span style="background:#cd96cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum3 <br>
 * <span style="background:#8b668b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum4 <br>
 * <span style="background:#b0e0e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PowderBlue <br>
 * <span style="background:#a020f0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple <br>
 * <span style="background:#9b30ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple1 <br>
 * <span style="background:#912cee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple2 <br>
 * <span style="background:#7d26cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple3 <br>
 * <span style="background:#551a8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple4 <br>
 * <span style="background:#ff0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Red <br>
 * <span style="background:#ee0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Red2 <br>
 * <span style="background:#cd0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Red3 <br>
 * <span style="background:#bc8f8f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown <br>
 * <span style="background:#ffc1c1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown1 <br>
 * <span style="background:#eeb4b4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown2 <br>
 * <span style="background:#cd9b9b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown3 <br>
 * <span style="background:#8b6969">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown4 <br>
 * <span style="background:#4169e1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue <br>
 * <span style="background:#4876ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue1 <br>
 * <span style="background:#436eee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue2 <br>
 * <span style="background:#3a5fcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue3 <br>
 * <span style="background:#27408b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue4 <br>
 * <span style="background:#8b4513">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SaddleBrown <br>
 * <span style="background:#fa8072">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon <br>
 * <span style="background:#ff8c69">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon1 <br>
 * <span style="background:#ee8262">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon2 <br>
 * <span style="background:#cd7054">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon3 <br>
 * <span style="background:#8b4c39">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon4 <br>
 * <span style="background:#f4a460">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SandyBrown <br>
 * <span style="background:#2e8b57">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen <br>
 * <span style="background:#54ff9f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen1 <br>
 * <span style="background:#4eee94">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen2 <br>
 * <span style="background:#43cd80">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen3 <br>
 * <span style="background:#fff5ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell <br>
 * <span style="background:#eee5de">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell2 <br>
 * <span style="background:#cdc5bf">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell3 <br>
 * <span style="background:#8b8682">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell4 <br>
 * <span style="background:#a0522d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna <br>
 * <span style="background:#ff8247">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna1 <br>
 * <span style="background:#ee7942">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna2 <br>
 * <span style="background:#cd6839">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna3 <br>
 * <span style="background:#8b4726">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna4 <br>
 * <span style="background:#87ceeb">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue <br>
 * <span style="background:#87ceff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue1 <br>
 * <span style="background:#7ec0ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue2 <br>
 * <span style="background:#6ca6cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue3 <br>
 * <span style="background:#4a708b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue4 <br>
 * <span style="background:#6a5acd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue <br>
 * <span style="background:#836fff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue1 <br>
 * <span style="background:#7a67ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue2 <br>
 * <span style="background:#6959cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue3 <br>
 * <span style="background:#473c8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue4 <br>
 * <span style="background:#708090">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray <br>
 * <span style="background:#c6e2ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray1 <br>
 * <span style="background:#b9d3ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray2 <br>
 * <span style="background:#9fb6cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray3 <br>
 * <span style="background:#6c7b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray4 <br>
 * <span style="background:#fffafa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow <br>
 * <span style="background:#eee9e9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow2 <br>
 * <span style="background:#cdc9c9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow3 <br>
 * <span style="background:#8b8989">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow4 <br>
 * <span style="background:#00ff7f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen <br>
 * <span style="background:#00ee76">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen2 <br>
 * <span style="background:#00cd66">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen3 <br>
 * <span style="background:#008b45">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen4 <br>
 * <span style="background:#4682b4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue <br>
 * <span style="background:#63b8ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue1 <br>
 * <span style="background:#5cacee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue2 <br>
 * <span style="background:#4f94cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue3 <br>
 * <span style="background:#36648b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue4 <br>
 * <span style="background:#d2b48c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan <br>
 * <span style="background:#ffa54f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan1 <br>
 * <span style="background:#ee9a49">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan2 <br>
 * <span style="background:#8b5a2b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan4 <br>
 * <span style="background:#d8bfd8">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle <br>
 * <span style="background:#ffe1ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle1 <br>
 * <span style="background:#eed2ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle2 <br>
 * <span style="background:#cdb5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle3 <br>
 * <span style="background:#8b7b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle4 <br>
 * <span style="background:#ff6347">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato <br>
 * <span style="background:#ee5c42">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato2 <br>
 * <span style="background:#cd4f39">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato3 <br>
 * <span style="background:#8b3626">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato4 <br>
 * <span style="background:#40e0d0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise <br>
 * <span style="background:#00f5ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise1 <br>
 * <span style="background:#00e5ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise2 <br>
 * <span style="background:#00c5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise3 <br>
 * <span style="background:#00868b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise4 <br>
 * <span style="background:#ee82ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Violet <br>
 * <span style="background:#d02090">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed <br>
 * <span style="background:#ff3e96">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed1 <br>
 * <span style="background:#ee3a8c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed2 <br>
 * <span style="background:#cd3278">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed3 <br>
 * <span style="background:#8b2252">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed4 <br>
 * <span style="background:#f5deb3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat <br>
 * <span style="background:#ffe7ba">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat1 <br>
 * <span style="background:#eed8ae">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat2 <br>
 * <span style="background:#cdba96">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat3 <br>
 * <span style="background:#8b7e66">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat4 <br>
 * <span style="background:#ffffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> White <br>
 * <span style="background:#f5f5f5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> WhiteSmoke <br>
 * <span style="background:#ffff00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow <br>
 * <span style="background:#eeee00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow2 <br>
 * <span style="background:#cdcd00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow3 <br>
 * <span style="background:#8b8b00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow4 <br>
 * <span style="background:#9acd32">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> YellowGreen <br>
 */

public class WebColors
{
    private WebColors( )
    {
    }

    public static RGBA get( String name )
    {
        return getColors( ).get( name );
    }

    private static Map<String, RGBA> getColors( )
    {
        return MapInstance.colors;
    }

    private static class MapInstance
    {
        final static Map<String, RGBA> colors = buildColors( );

        private static Map<String, RGBA> buildColors( )
        {
            Map<String, RGBA> colors = new TreeMap<String, RGBA>( );

            colors.put( "Snow", fromIntRGB( 255, 250, 250 ) );
            colors.put( "GhostWhite", fromIntRGB( 248, 248, 255 ) );
            colors.put( "WhiteSmoke", fromIntRGB( 245, 245, 245 ) );
            colors.put( "Gainsboro", fromIntRGB( 220, 220, 220 ) );
            colors.put( "FloralWhite", fromIntRGB( 255, 250, 240 ) );
            colors.put( "OldLace", fromIntRGB( 253, 245, 230 ) );
            colors.put( "Linen", fromIntRGB( 250, 240, 230 ) );
            colors.put( "AntiqueWhite", fromIntRGB( 250, 235, 215 ) );
            colors.put( "PapayaWhip", fromIntRGB( 255, 239, 213 ) );
            colors.put( "BlanchedAlmond", fromIntRGB( 255, 235, 205 ) );
            colors.put( "Bisque", fromIntRGB( 255, 228, 196 ) );
            colors.put( "PeachPuff", fromIntRGB( 255, 218, 185 ) );
            colors.put( "NavajoWhite", fromIntRGB( 255, 222, 173 ) );
            colors.put( "Moccasin", fromIntRGB( 255, 228, 181 ) );
            colors.put( "Cornsilk", fromIntRGB( 255, 248, 220 ) );
            colors.put( "Ivory", fromIntRGB( 255, 255, 240 ) );
            colors.put( "LemonChiffon", fromIntRGB( 255, 250, 205 ) );
            colors.put( "Seashell", fromIntRGB( 255, 245, 238 ) );
            colors.put( "Honeydew", fromIntRGB( 240, 255, 240 ) );
            colors.put( "MintCream", fromIntRGB( 245, 255, 250 ) );
            colors.put( "Azure", fromIntRGB( 240, 255, 255 ) );
            colors.put( "AliceBlue", fromIntRGB( 240, 248, 255 ) );
            colors.put( "Lavender", fromIntRGB( 230, 230, 250 ) );
            colors.put( "LavenderBlush", fromIntRGB( 255, 240, 245 ) );
            colors.put( "MistyRose", fromIntRGB( 255, 228, 225 ) );
            colors.put( "White", fromIntRGB( 255, 255, 255 ) );
            colors.put( "Black", fromIntRGB( 0, 0, 0 ) );
            colors.put( "DarkSlateGray", fromIntRGB( 47, 79, 79 ) );
            colors.put( "DimGray", fromIntRGB( 105, 105, 105 ) );
            colors.put( "SlateGray", fromIntRGB( 112, 128, 144 ) );
            colors.put( "LightSlateGray", fromIntRGB( 119, 136, 153 ) );
            colors.put( "DarkGray", fromIntRGB( 169, 169, 169 ) );
            colors.put( "Gray", fromIntRGB( 190, 190, 190 ) );
            colors.put( "LightGrey", fromIntRGB( 211, 211, 211 ) );
            colors.put( "MidnightBlue", fromIntRGB( 25, 25, 112 ) );
            colors.put( "Navy", fromIntRGB( 0, 0, 128 ) );
            colors.put( "CornflowerBlue", fromIntRGB( 100, 149, 237 ) );
            colors.put( "DarkSlateBlue", fromIntRGB( 72, 61, 139 ) );
            colors.put( "SlateBlue", fromIntRGB( 106, 90, 205 ) );
            colors.put( "MediumSlateBlue", fromIntRGB( 123, 104, 238 ) );
            colors.put( "LightSlateBlue", fromIntRGB( 132, 112, 255 ) );
            colors.put( "MediumBlue", fromIntRGB( 0, 0, 205 ) );
            colors.put( "RoyalBlue", fromIntRGB( 65, 105, 225 ) );
            colors.put( "Blue", fromIntRGB( 0, 0, 255 ) );
            colors.put( "DarkBlue", fromIntRGB( 0, 0, 139 ) );
            colors.put( "DodgerBlue", fromIntRGB( 30, 144, 255 ) );
            colors.put( "DeepSkyBlue", fromIntRGB( 0, 191, 255 ) );
            colors.put( "SkyBlue", fromIntRGB( 135, 206, 235 ) );
            colors.put( "LightSkyBlue", fromIntRGB( 135, 206, 250 ) );
            colors.put( "SteelBlue", fromIntRGB( 70, 130, 180 ) );
            colors.put( "LightSteelBlue", fromIntRGB( 176, 196, 222 ) );
            colors.put( "LightBlue", fromIntRGB( 173, 216, 230 ) );
            colors.put( "PowderBlue", fromIntRGB( 176, 224, 230 ) );
            colors.put( "PaleTurquoise", fromIntRGB( 175, 238, 238 ) );
            colors.put( "DarkTurquoise", fromIntRGB( 0, 206, 209 ) );
            colors.put( "MediumTurquoise", fromIntRGB( 72, 209, 204 ) );
            colors.put( "Turquoise", fromIntRGB( 64, 224, 208 ) );
            colors.put( "Cyan", fromIntRGB( 0, 255, 255 ) );
            colors.put( "DarkCyan", fromIntRGB( 0, 139, 139 ) );
            colors.put( "LightCyan", fromIntRGB( 224, 255, 255 ) );
            colors.put( "CadetBlue", fromIntRGB( 95, 158, 160 ) );
            colors.put( "MediumAquamarine", fromIntRGB( 102, 205, 170 ) );
            colors.put( "Aquamarine", fromIntRGB( 127, 255, 212 ) );
            colors.put( "DarkGreen", fromIntRGB( 0, 100, 0 ) );
            colors.put( "LightGreen", fromIntRGB( 144, 238, 144 ) );
            colors.put( "DarkOliveGreen", fromIntRGB( 85, 107, 47 ) );
            colors.put( "DarkSeaGreen", fromIntRGB( 143, 188, 143 ) );
            colors.put( "SeaGreen", fromIntRGB( 46, 139, 87 ) );
            colors.put( "MediumSeaGreen", fromIntRGB( 60, 179, 113 ) );
            colors.put( "LightSeaGreen", fromIntRGB( 32, 178, 170 ) );
            colors.put( "PaleGreen", fromIntRGB( 152, 251, 152 ) );
            colors.put( "SpringGreen", fromIntRGB( 0, 255, 127 ) );
            colors.put( "LawnGreen", fromIntRGB( 124, 252, 0 ) );
            colors.put( "Green", fromIntRGB( 0, 255, 0 ) );
            colors.put( "Chartreuse", fromIntRGB( 127, 255, 0 ) );
            colors.put( "MediumSpring", fromIntRGB( 0, 250, 154 ) );
            colors.put( "GreenYellow", fromIntRGB( 173, 255, 47 ) );
            colors.put( "LimeGreen", fromIntRGB( 50, 205, 50 ) );
            colors.put( "YellowGreen", fromIntRGB( 154, 205, 50 ) );
            colors.put( "ForestGreen", fromIntRGB( 34, 139, 34 ) );
            colors.put( "OliveDrab", fromIntRGB( 107, 142, 35 ) );
            colors.put( "DarkKhaki", fromIntRGB( 189, 183, 107 ) );
            colors.put( "Khaki", fromIntRGB( 240, 230, 140 ) );
            colors.put( "PaleGoldenrod", fromIntRGB( 238, 232, 170 ) );
            colors.put( "LightGoldenrodYellow", fromIntRGB( 250, 250, 210 ) );
            colors.put( "LightYellow", fromIntRGB( 255, 255, 224 ) );
            colors.put( "Yellow", fromIntRGB( 255, 255, 0 ) );
            colors.put( "Gold", fromIntRGB( 255, 215, 0 ) );
            colors.put( "LightGoldenrod", fromIntRGB( 238, 221, 130 ) );
            colors.put( "Goldenrod", fromIntRGB( 218, 165, 32 ) );
            colors.put( "DarkGoldenrod", fromIntRGB( 184, 134, 11 ) );
            colors.put( "RosyBrown", fromIntRGB( 188, 143, 143 ) );
            colors.put( "IndianRed", fromIntRGB( 205, 92, 92 ) );
            colors.put( "SaddleBrown", fromIntRGB( 139, 69, 19 ) );
            colors.put( "Sienna", fromIntRGB( 160, 82, 45 ) );
            colors.put( "Peru", fromIntRGB( 205, 133, 63 ) );
            colors.put( "Burlywood", fromIntRGB( 222, 184, 135 ) );
            colors.put( "Beige", fromIntRGB( 245, 245, 220 ) );
            colors.put( "Wheat", fromIntRGB( 245, 222, 179 ) );
            colors.put( "SandyBrown", fromIntRGB( 244, 164, 96 ) );
            colors.put( "Tan", fromIntRGB( 210, 180, 140 ) );
            colors.put( "Chocolate", fromIntRGB( 210, 105, 30 ) );
            colors.put( "Firebrick", fromIntRGB( 178, 34, 34 ) );
            colors.put( "Brown", fromIntRGB( 165, 42, 42 ) );
            colors.put( "DarkSalmon", fromIntRGB( 233, 150, 122 ) );
            colors.put( "Salmon", fromIntRGB( 250, 128, 114 ) );
            colors.put( "LightSalmon", fromIntRGB( 255, 160, 122 ) );
            colors.put( "Orange", fromIntRGB( 255, 165, 0 ) );
            colors.put( "DarkOrange", fromIntRGB( 255, 140, 0 ) );
            colors.put( "Coral", fromIntRGB( 255, 127, 80 ) );
            colors.put( "LightCoral", fromIntRGB( 240, 128, 128 ) );
            colors.put( "Tomato", fromIntRGB( 255, 99, 71 ) );
            colors.put( "OrangeRed", fromIntRGB( 255, 69, 0 ) );
            colors.put( "Red", fromIntRGB( 255, 0, 0 ) );
            colors.put( "DarkRed", fromIntRGB( 139, 0, 0 ) );
            colors.put( "HotPink", fromIntRGB( 255, 105, 180 ) );
            colors.put( "DeepPink", fromIntRGB( 255, 20, 147 ) );
            colors.put( "Pink", fromIntRGB( 255, 192, 203 ) );
            colors.put( "LightPink", fromIntRGB( 255, 182, 193 ) );
            colors.put( "PaleVioletRed", fromIntRGB( 219, 112, 147 ) );
            colors.put( "Maroon", fromIntRGB( 176, 48, 96 ) );
            colors.put( "MediumVioletRed", fromIntRGB( 199, 21, 133 ) );
            colors.put( "VioletRed", fromIntRGB( 208, 32, 144 ) );
            colors.put( "Magenta", fromIntRGB( 255, 0, 255 ) );
            colors.put( "DarkMagenta", fromIntRGB( 139, 0, 139 ) );
            colors.put( "Violet", fromIntRGB( 238, 130, 238 ) );
            colors.put( "Plum", fromIntRGB( 221, 160, 221 ) );
            colors.put( "Orchid", fromIntRGB( 218, 112, 214 ) );
            colors.put( "MediumOrchid", fromIntRGB( 186, 85, 211 ) );
            colors.put( "DarkOrchid", fromIntRGB( 153, 50, 204 ) );
            colors.put( "DarkViolet", fromIntRGB( 148, 0, 211 ) );
            colors.put( "BlueViolet", fromIntRGB( 138, 43, 226 ) );
            colors.put( "Purple", fromIntRGB( 160, 32, 240 ) );
            colors.put( "MediumPurple", fromIntRGB( 147, 112, 219 ) );
            colors.put( "Thistle", fromIntRGB( 216, 191, 216 ) );
            colors.put( "Snow2", fromIntRGB( 238, 233, 233 ) );
            colors.put( "Snow3", fromIntRGB( 205, 201, 201 ) );
            colors.put( "Snow4", fromIntRGB( 139, 137, 137 ) );
            colors.put( "Seashell2", fromIntRGB( 238, 229, 222 ) );
            colors.put( "Seashell3", fromIntRGB( 205, 197, 191 ) );
            colors.put( "Seashell4", fromIntRGB( 139, 134, 130 ) );
            colors.put( "AntiqueWhite1", fromIntRGB( 255, 239, 219 ) );
            colors.put( "AntiqueWhite2", fromIntRGB( 238, 223, 204 ) );
            colors.put( "AntiqueWhite3", fromIntRGB( 205, 192, 176 ) );
            colors.put( "AntiqueWhite4", fromIntRGB( 139, 131, 120 ) );
            colors.put( "Bisque2", fromIntRGB( 238, 213, 183 ) );
            colors.put( "Bisque3", fromIntRGB( 205, 183, 158 ) );
            colors.put( "Bisque4", fromIntRGB( 139, 125, 107 ) );
            colors.put( "PeachPuff2", fromIntRGB( 238, 203, 173 ) );
            colors.put( "PeachPuff3", fromIntRGB( 205, 175, 149 ) );
            colors.put( "PeachPuff4", fromIntRGB( 139, 119, 101 ) );
            colors.put( "NavajoWhite2", fromIntRGB( 238, 207, 161 ) );
            colors.put( "NavajoWhite3", fromIntRGB( 205, 179, 139 ) );
            colors.put( "NavajoWhite4", fromIntRGB( 139, 121, 94 ) );
            colors.put( "LemonChiffon2", fromIntRGB( 238, 233, 191 ) );
            colors.put( "LemonChiffon3", fromIntRGB( 205, 201, 165 ) );
            colors.put( "LemonChiffon4", fromIntRGB( 139, 137, 112 ) );
            colors.put( "Cornsilk2", fromIntRGB( 238, 232, 205 ) );
            colors.put( "Cornsilk3", fromIntRGB( 205, 200, 177 ) );
            colors.put( "Cornsilk4", fromIntRGB( 139, 136, 120 ) );
            colors.put( "Ivory2", fromIntRGB( 238, 238, 224 ) );
            colors.put( "Ivory3", fromIntRGB( 205, 205, 193 ) );
            colors.put( "Ivory4", fromIntRGB( 139, 139, 131 ) );
            colors.put( "Honeydew2", fromIntRGB( 224, 238, 224 ) );
            colors.put( "Honeydew3", fromIntRGB( 193, 205, 193 ) );
            colors.put( "Honeydew4", fromIntRGB( 131, 139, 131 ) );
            colors.put( "LavenderBlush2", fromIntRGB( 238, 224, 229 ) );
            colors.put( "LavenderBlush3", fromIntRGB( 205, 193, 197 ) );
            colors.put( "LavenderBlush4", fromIntRGB( 139, 131, 134 ) );
            colors.put( "MistyRose2", fromIntRGB( 238, 213, 210 ) );
            colors.put( "MistyRose3", fromIntRGB( 205, 183, 181 ) );
            colors.put( "MistyRose4", fromIntRGB( 139, 125, 123 ) );
            colors.put( "Azure2", fromIntRGB( 224, 238, 238 ) );
            colors.put( "Azure3", fromIntRGB( 193, 205, 205 ) );
            colors.put( "Azure4", fromIntRGB( 131, 139, 139 ) );
            colors.put( "SlateBlue1", fromIntRGB( 131, 111, 255 ) );
            colors.put( "SlateBlue2", fromIntRGB( 122, 103, 238 ) );
            colors.put( "SlateBlue3", fromIntRGB( 105, 89, 205 ) );
            colors.put( "SlateBlue4", fromIntRGB( 71, 60, 139 ) );
            colors.put( "RoyalBlue1", fromIntRGB( 72, 118, 255 ) );
            colors.put( "RoyalBlue2", fromIntRGB( 67, 110, 238 ) );
            colors.put( "RoyalBlue3", fromIntRGB( 58, 95, 205 ) );
            colors.put( "RoyalBlue4", fromIntRGB( 39, 64, 139 ) );
            colors.put( "Blue2", fromIntRGB( 0, 0, 238 ) );
            colors.put( "DodgerBlue2", fromIntRGB( 28, 134, 238 ) );
            colors.put( "DodgerBlue3", fromIntRGB( 24, 116, 205 ) );
            colors.put( "DodgerBlue4", fromIntRGB( 16, 78, 139 ) );
            colors.put( "SteelBlue1", fromIntRGB( 99, 184, 255 ) );
            colors.put( "SteelBlue2", fromIntRGB( 92, 172, 238 ) );
            colors.put( "SteelBlue3", fromIntRGB( 79, 148, 205 ) );
            colors.put( "SteelBlue4", fromIntRGB( 54, 100, 139 ) );
            colors.put( "DeepSkyBlue2", fromIntRGB( 0, 178, 238 ) );
            colors.put( "DeepSkyBlue3", fromIntRGB( 0, 154, 205 ) );
            colors.put( "DeepSkyBlue4", fromIntRGB( 0, 104, 139 ) );
            colors.put( "SkyBlue1", fromIntRGB( 135, 206, 255 ) );
            colors.put( "SkyBlue2", fromIntRGB( 126, 192, 238 ) );
            colors.put( "SkyBlue3", fromIntRGB( 108, 166, 205 ) );
            colors.put( "SkyBlue4", fromIntRGB( 74, 112, 139 ) );
            colors.put( "LightSkyBlue1", fromIntRGB( 176, 226, 255 ) );
            colors.put( "LightSkyBlue2", fromIntRGB( 164, 211, 238 ) );
            colors.put( "LightSkyBlue3", fromIntRGB( 141, 182, 205 ) );
            colors.put( "LightSkyBlue4", fromIntRGB( 96, 123, 139 ) );
            colors.put( "SlateGray1", fromIntRGB( 198, 226, 255 ) );
            colors.put( "SlateGray2", fromIntRGB( 185, 211, 238 ) );
            colors.put( "SlateGray3", fromIntRGB( 159, 182, 205 ) );
            colors.put( "SlateGray4", fromIntRGB( 108, 123, 139 ) );
            colors.put( "LightSteelBlue1", fromIntRGB( 202, 225, 255 ) );
            colors.put( "LightSteelBlue2", fromIntRGB( 188, 210, 238 ) );
            colors.put( "LightSteelBlue3", fromIntRGB( 162, 181, 205 ) );
            colors.put( "LightSteelBlue4", fromIntRGB( 110, 123, 139 ) );
            colors.put( "LightBlue1", fromIntRGB( 191, 239, 255 ) );
            colors.put( "LightBlue2", fromIntRGB( 178, 223, 238 ) );
            colors.put( "LightBlue3", fromIntRGB( 154, 192, 205 ) );
            colors.put( "LightBlue4", fromIntRGB( 104, 131, 139 ) );
            colors.put( "LightCyan2", fromIntRGB( 209, 238, 238 ) );
            colors.put( "LightCyan3", fromIntRGB( 180, 205, 205 ) );
            colors.put( "LightCyan4", fromIntRGB( 122, 139, 139 ) );
            colors.put( "PaleTurquoise1", fromIntRGB( 187, 255, 255 ) );
            colors.put( "PaleTurquoise2", fromIntRGB( 174, 238, 238 ) );
            colors.put( "PaleTurquoise3", fromIntRGB( 150, 205, 205 ) );
            colors.put( "PaleTurquoise4", fromIntRGB( 102, 139, 139 ) );
            colors.put( "CadetBlue1", fromIntRGB( 152, 245, 255 ) );
            colors.put( "CadetBlue2", fromIntRGB( 142, 229, 238 ) );
            colors.put( "CadetBlue3", fromIntRGB( 122, 197, 205 ) );
            colors.put( "CadetBlue4", fromIntRGB( 83, 134, 139 ) );
            colors.put( "Turquoise1", fromIntRGB( 0, 245, 255 ) );
            colors.put( "Turquoise2", fromIntRGB( 0, 229, 238 ) );
            colors.put( "Turquoise3", fromIntRGB( 0, 197, 205 ) );
            colors.put( "Turquoise4", fromIntRGB( 0, 134, 139 ) );
            colors.put( "Cyan2", fromIntRGB( 0, 238, 238 ) );
            colors.put( "Cyan3", fromIntRGB( 0, 205, 205 ) );
            colors.put( "DarkSlateGray1", fromIntRGB( 151, 255, 255 ) );
            colors.put( "DarkSlateGray2", fromIntRGB( 141, 238, 238 ) );
            colors.put( "DarkSlateGray3", fromIntRGB( 121, 205, 205 ) );
            colors.put( "DarkSlateGray4", fromIntRGB( 82, 139, 139 ) );
            colors.put( "Aquamarine2", fromIntRGB( 118, 238, 198 ) );
            colors.put( "Aquamarine4", fromIntRGB( 69, 139, 116 ) );
            colors.put( "DarkSeaGreen1", fromIntRGB( 193, 255, 193 ) );
            colors.put( "DarkSeaGreen2", fromIntRGB( 180, 238, 180 ) );
            colors.put( "DarkSeaGreen3", fromIntRGB( 155, 205, 155 ) );
            colors.put( "DarkSeaGreen4", fromIntRGB( 105, 139, 105 ) );
            colors.put( "SeaGreen1", fromIntRGB( 84, 255, 159 ) );
            colors.put( "SeaGreen2", fromIntRGB( 78, 238, 148 ) );
            colors.put( "SeaGreen3", fromIntRGB( 67, 205, 128 ) );
            colors.put( "PaleGreen1", fromIntRGB( 154, 255, 154 ) );
            colors.put( "PaleGreen3", fromIntRGB( 124, 205, 124 ) );
            colors.put( "PaleGreen4", fromIntRGB( 84, 139, 84 ) );
            colors.put( "SpringGreen2", fromIntRGB( 0, 238, 118 ) );
            colors.put( "SpringGreen3", fromIntRGB( 0, 205, 102 ) );
            colors.put( "SpringGreen4", fromIntRGB( 0, 139, 69 ) );
            colors.put( "Green2", fromIntRGB( 0, 238, 0 ) );
            colors.put( "Green3", fromIntRGB( 0, 205, 0 ) );
            colors.put( "Green4", fromIntRGB( 0, 139, 0 ) );
            colors.put( "Chartreuse2", fromIntRGB( 118, 238, 0 ) );
            colors.put( "Chartreuse3", fromIntRGB( 102, 205, 0 ) );
            colors.put( "Chartreuse4", fromIntRGB( 69, 139, 0 ) );
            colors.put( "OliveDrab1", fromIntRGB( 192, 255, 62 ) );
            colors.put( "OliveDrab2", fromIntRGB( 179, 238, 58 ) );
            colors.put( "OliveDrab4", fromIntRGB( 105, 139, 34 ) );
            colors.put( "DarkOliveGreen1", fromIntRGB( 202, 255, 112 ) );
            colors.put( "DarkOliveGreen2", fromIntRGB( 188, 238, 104 ) );
            colors.put( "DarkOliveGreen3", fromIntRGB( 162, 205, 90 ) );
            colors.put( "DarkOliveGreen4", fromIntRGB( 110, 139, 61 ) );
            colors.put( "Khaki1", fromIntRGB( 255, 246, 143 ) );
            colors.put( "Khaki2", fromIntRGB( 238, 230, 133 ) );
            colors.put( "Khaki3", fromIntRGB( 205, 198, 115 ) );
            colors.put( "Khaki4", fromIntRGB( 139, 134, 78 ) );
            colors.put( "LightGoldenrod1", fromIntRGB( 255, 236, 139 ) );
            colors.put( "LightGoldenrod2", fromIntRGB( 238, 220, 130 ) );
            colors.put( "LightGoldenrod3", fromIntRGB( 205, 190, 112 ) );
            colors.put( "LightGoldenrod4", fromIntRGB( 139, 129, 76 ) );
            colors.put( "LightYellow2", fromIntRGB( 238, 238, 209 ) );
            colors.put( "LightYellow3", fromIntRGB( 205, 205, 180 ) );
            colors.put( "LightYellow4", fromIntRGB( 139, 139, 122 ) );
            colors.put( "Yellow2", fromIntRGB( 238, 238, 0 ) );
            colors.put( "Yellow3", fromIntRGB( 205, 205, 0 ) );
            colors.put( "Yellow4", fromIntRGB( 139, 139, 0 ) );
            colors.put( "Gold2", fromIntRGB( 238, 201, 0 ) );
            colors.put( "Gold3", fromIntRGB( 205, 173, 0 ) );
            colors.put( "Gold4", fromIntRGB( 139, 117, 0 ) );
            colors.put( "Goldenrod1", fromIntRGB( 255, 193, 37 ) );
            colors.put( "Goldenrod2", fromIntRGB( 238, 180, 34 ) );
            colors.put( "Goldenrod3", fromIntRGB( 205, 155, 29 ) );
            colors.put( "Goldenrod4", fromIntRGB( 139, 105, 20 ) );
            colors.put( "DarkGoldenrod1", fromIntRGB( 255, 185, 15 ) );
            colors.put( "DarkGoldenrod2", fromIntRGB( 238, 173, 14 ) );
            colors.put( "DarkGoldenrod3", fromIntRGB( 205, 149, 12 ) );
            colors.put( "DarkGoldenrod4", fromIntRGB( 139, 101, 8 ) );
            colors.put( "RosyBrown1", fromIntRGB( 255, 193, 193 ) );
            colors.put( "RosyBrown2", fromIntRGB( 238, 180, 180 ) );
            colors.put( "RosyBrown3", fromIntRGB( 205, 155, 155 ) );
            colors.put( "RosyBrown4", fromIntRGB( 139, 105, 105 ) );
            colors.put( "IndianRed1", fromIntRGB( 255, 106, 106 ) );
            colors.put( "IndianRed2", fromIntRGB( 238, 99, 99 ) );
            colors.put( "IndianRed3", fromIntRGB( 205, 85, 85 ) );
            colors.put( "IndianRed4", fromIntRGB( 139, 58, 58 ) );
            colors.put( "Sienna1", fromIntRGB( 255, 130, 71 ) );
            colors.put( "Sienna2", fromIntRGB( 238, 121, 66 ) );
            colors.put( "Sienna3", fromIntRGB( 205, 104, 57 ) );
            colors.put( "Sienna4", fromIntRGB( 139, 71, 38 ) );
            colors.put( "Burlywood1", fromIntRGB( 255, 211, 155 ) );
            colors.put( "Burlywood2", fromIntRGB( 238, 197, 145 ) );
            colors.put( "Burlywood3", fromIntRGB( 205, 170, 125 ) );
            colors.put( "Burlywood4", fromIntRGB( 139, 115, 85 ) );
            colors.put( "Wheat1", fromIntRGB( 255, 231, 186 ) );
            colors.put( "Wheat2", fromIntRGB( 238, 216, 174 ) );
            colors.put( "Wheat3", fromIntRGB( 205, 186, 150 ) );
            colors.put( "Wheat4", fromIntRGB( 139, 126, 102 ) );
            colors.put( "Tan1", fromIntRGB( 255, 165, 79 ) );
            colors.put( "Tan2", fromIntRGB( 238, 154, 73 ) );
            colors.put( "Tan4", fromIntRGB( 139, 90, 43 ) );
            colors.put( "Chocolate1", fromIntRGB( 255, 127, 36 ) );
            colors.put( "Chocolate2", fromIntRGB( 238, 118, 33 ) );
            colors.put( "Chocolate3", fromIntRGB( 205, 102, 29 ) );
            colors.put( "Firebrick1", fromIntRGB( 255, 48, 48 ) );
            colors.put( "Firebrick2", fromIntRGB( 238, 44, 44 ) );
            colors.put( "Firebrick3", fromIntRGB( 205, 38, 38 ) );
            colors.put( "Firebrick4", fromIntRGB( 139, 26, 26 ) );
            colors.put( "Brown1", fromIntRGB( 255, 64, 64 ) );
            colors.put( "Brown2", fromIntRGB( 238, 59, 59 ) );
            colors.put( "Brown3", fromIntRGB( 205, 51, 51 ) );
            colors.put( "Brown4", fromIntRGB( 139, 35, 35 ) );
            colors.put( "Salmon1", fromIntRGB( 255, 140, 105 ) );
            colors.put( "Salmon2", fromIntRGB( 238, 130, 98 ) );
            colors.put( "Salmon3", fromIntRGB( 205, 112, 84 ) );
            colors.put( "Salmon4", fromIntRGB( 139, 76, 57 ) );
            colors.put( "LightSalmon2", fromIntRGB( 238, 149, 114 ) );
            colors.put( "LightSalmon3", fromIntRGB( 205, 129, 98 ) );
            colors.put( "LightSalmon4", fromIntRGB( 139, 87, 66 ) );
            colors.put( "Orange2", fromIntRGB( 238, 154, 0 ) );
            colors.put( "Orange3", fromIntRGB( 205, 133, 0 ) );
            colors.put( "Orange4", fromIntRGB( 139, 90, 0 ) );
            colors.put( "DarkOrange1", fromIntRGB( 255, 127, 0 ) );
            colors.put( "DarkOrange2", fromIntRGB( 238, 118, 0 ) );
            colors.put( "DarkOrange3", fromIntRGB( 205, 102, 0 ) );
            colors.put( "DarkOrange4", fromIntRGB( 139, 69, 0 ) );
            colors.put( "Coral1", fromIntRGB( 255, 114, 86 ) );
            colors.put( "Coral2", fromIntRGB( 238, 106, 80 ) );
            colors.put( "Coral3", fromIntRGB( 205, 91, 69 ) );
            colors.put( "Coral4", fromIntRGB( 139, 62, 47 ) );
            colors.put( "Tomato2", fromIntRGB( 238, 92, 66 ) );
            colors.put( "Tomato3", fromIntRGB( 205, 79, 57 ) );
            colors.put( "Tomato4", fromIntRGB( 139, 54, 38 ) );
            colors.put( "OrangeRed2", fromIntRGB( 238, 64, 0 ) );
            colors.put( "OrangeRed3", fromIntRGB( 205, 55, 0 ) );
            colors.put( "OrangeRed4", fromIntRGB( 139, 37, 0 ) );
            colors.put( "Red2", fromIntRGB( 238, 0, 0 ) );
            colors.put( "Red3", fromIntRGB( 205, 0, 0 ) );
            colors.put( "DeepPink2", fromIntRGB( 238, 18, 137 ) );
            colors.put( "DeepPink3", fromIntRGB( 205, 16, 118 ) );
            colors.put( "DeepPink4", fromIntRGB( 139, 10, 80 ) );
            colors.put( "HotPink1", fromIntRGB( 255, 110, 180 ) );
            colors.put( "HotPink2", fromIntRGB( 238, 106, 167 ) );
            colors.put( "HotPink3", fromIntRGB( 205, 96, 144 ) );
            colors.put( "HotPink4", fromIntRGB( 139, 58, 98 ) );
            colors.put( "Pink1", fromIntRGB( 255, 181, 197 ) );
            colors.put( "Pink2", fromIntRGB( 238, 169, 184 ) );
            colors.put( "Pink3", fromIntRGB( 205, 145, 158 ) );
            colors.put( "Pink4", fromIntRGB( 139, 99, 108 ) );
            colors.put( "LightPink1", fromIntRGB( 255, 174, 185 ) );
            colors.put( "LightPink2", fromIntRGB( 238, 162, 173 ) );
            colors.put( "LightPink3", fromIntRGB( 205, 140, 149 ) );
            colors.put( "LightPink4", fromIntRGB( 139, 95, 101 ) );
            colors.put( "PaleVioletRed1", fromIntRGB( 255, 130, 171 ) );
            colors.put( "PaleVioletRed2", fromIntRGB( 238, 121, 159 ) );
            colors.put( "PaleVioletRed3", fromIntRGB( 205, 104, 137 ) );
            colors.put( "PaleVioletRed4", fromIntRGB( 139, 71, 93 ) );
            colors.put( "Maroon1", fromIntRGB( 255, 52, 179 ) );
            colors.put( "Maroon2", fromIntRGB( 238, 48, 167 ) );
            colors.put( "Maroon3", fromIntRGB( 205, 41, 144 ) );
            colors.put( "Maroon4", fromIntRGB( 139, 28, 98 ) );
            colors.put( "VioletRed1", fromIntRGB( 255, 62, 150 ) );
            colors.put( "VioletRed2", fromIntRGB( 238, 58, 140 ) );
            colors.put( "VioletRed3", fromIntRGB( 205, 50, 120 ) );
            colors.put( "VioletRed4", fromIntRGB( 139, 34, 82 ) );
            colors.put( "Magenta2", fromIntRGB( 238, 0, 238 ) );
            colors.put( "Magenta3", fromIntRGB( 205, 0, 205 ) );
            colors.put( "Orchid1", fromIntRGB( 255, 131, 250 ) );
            colors.put( "Orchid2", fromIntRGB( 238, 122, 233 ) );
            colors.put( "Orchid3", fromIntRGB( 205, 105, 201 ) );
            colors.put( "Orchid4", fromIntRGB( 139, 71, 137 ) );
            colors.put( "Plum1", fromIntRGB( 255, 187, 255 ) );
            colors.put( "Plum2", fromIntRGB( 238, 174, 238 ) );
            colors.put( "Plum3", fromIntRGB( 205, 150, 205 ) );
            colors.put( "Plum4", fromIntRGB( 139, 102, 139 ) );
            colors.put( "MediumOrchid1", fromIntRGB( 224, 102, 255 ) );
            colors.put( "MediumOrchid2", fromIntRGB( 209, 95, 238 ) );
            colors.put( "MediumOrchid3", fromIntRGB( 180, 82, 205 ) );
            colors.put( "MediumOrchid4", fromIntRGB( 122, 55, 139 ) );
            colors.put( "DarkOrchid1", fromIntRGB( 191, 62, 255 ) );
            colors.put( "DarkOrchid2", fromIntRGB( 178, 58, 238 ) );
            colors.put( "DarkOrchid3", fromIntRGB( 154, 50, 205 ) );
            colors.put( "DarkOrchid4", fromIntRGB( 104, 34, 139 ) );
            colors.put( "Purple1", fromIntRGB( 155, 48, 255 ) );
            colors.put( "Purple2", fromIntRGB( 145, 44, 238 ) );
            colors.put( "Purple3", fromIntRGB( 125, 38, 205 ) );
            colors.put( "Purple4", fromIntRGB( 85, 26, 139 ) );
            colors.put( "MediumPurple1", fromIntRGB( 171, 130, 255 ) );
            colors.put( "MediumPurple2", fromIntRGB( 159, 121, 238 ) );
            colors.put( "MediumPurple3", fromIntRGB( 137, 104, 205 ) );
            colors.put( "MediumPurple4", fromIntRGB( 93, 71, 139 ) );
            colors.put( "Thistle1", fromIntRGB( 255, 225, 255 ) );
            colors.put( "Thistle2", fromIntRGB( 238, 210, 238 ) );
            colors.put( "Thistle3", fromIntRGB( 205, 181, 205 ) );
            colors.put( "Thistle4", fromIntRGB( 139, 123, 139 ) );

            return Collections.unmodifiableMap( colors );
        }
    }

    /**
     * <span style="background:#f0f8ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AliceBlue <br>
     */
    public static final RGBA AliceBlue = getColors( ).get( "AliceBlue" );

    /**
     * <span style="background:#faebd7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite <br>
     */
    public static final RGBA AntiqueWhite = getColors( ).get( "AntiqueWhite" );

    /**
     * <span style="background:#ffefdb">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite1 <br>
     */
    public static final RGBA AntiqueWhite1 = getColors( ).get( "AntiqueWhite1" );

    /**
     * <span style="background:#eedfcc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite2 <br>
     */
    public static final RGBA AntiqueWhite2 = getColors( ).get( "AntiqueWhite2" );

    /**
     * <span style="background:#cdc0b0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite3 <br>
     */
    public static final RGBA AntiqueWhite3 = getColors( ).get( "AntiqueWhite3" );

    /**
     * <span style="background:#8b8378">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> AntiqueWhite4 <br>
     */
    public static final RGBA AntiqueWhite4 = getColors( ).get( "AntiqueWhite4" );

    /**
     * <span style="background:#7fffd4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Aquamarine <br>
     */
    public static final RGBA Aquamarine = getColors( ).get( "Aquamarine" );

    /**
     * <span style="background:#76eec6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Aquamarine2 <br>
     */
    public static final RGBA Aquamarine2 = getColors( ).get( "Aquamarine2" );

    /**
     * <span style="background:#458b74">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Aquamarine4 <br>
     */
    public static final RGBA Aquamarine4 = getColors( ).get( "Aquamarine4" );

    /**
     * <span style="background:#f0ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure <br>
     */
    public static final RGBA Azure = getColors( ).get( "Azure" );

    /**
     * <span style="background:#e0eeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure2 <br>
     */
    public static final RGBA Azure2 = getColors( ).get( "Azure2" );

    /**
     * <span style="background:#c1cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure3 <br>
     */
    public static final RGBA Azure3 = getColors( ).get( "Azure3" );

    /**
     * <span style="background:#838b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Azure4 <br>
     */
    public static final RGBA Azure4 = getColors( ).get( "Azure4" );

    /**
     * <span style="background:#f5f5dc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Beige <br>
     */
    public static final RGBA Beige = getColors( ).get( "Beige" );

    /**
     * <span style="background:#ffe4c4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque <br>
     */
    public static final RGBA Bisque = getColors( ).get( "Bisque" );

    /**
     * <span style="background:#eed5b7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque2 <br>
     */
    public static final RGBA Bisque2 = getColors( ).get( "Bisque2" );

    /**
     * <span style="background:#cdb79e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque3 <br>
     */
    public static final RGBA Bisque3 = getColors( ).get( "Bisque3" );

    /**
     * <span style="background:#8b7d6b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Bisque4 <br>
     */
    public static final RGBA Bisque4 = getColors( ).get( "Bisque4" );

    /**
     * <span style="background:#000000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Black <br>
     */
    public static final RGBA Black = getColors( ).get( "Black" );

    /**
     * <span style="background:#ffebcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> BlanchedAlmond <br>
     */
    public static final RGBA BlanchedAlmond = getColors( ).get( "BlanchedAlmond" );

    /**
     * <span style="background:#0000ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Blue <br>
     */
    public static final RGBA Blue = getColors( ).get( "Blue" );

    /**
     * <span style="background:#0000ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Blue2 <br>
     */
    public static final RGBA Blue2 = getColors( ).get( "Blue2" );

    /**
     * <span style="background:#8a2be2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> BlueViolet <br>
     */
    public static final RGBA BlueViolet = getColors( ).get( "BlueViolet" );

    /**
     * <span style="background:#a52a2a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown <br>
     */
    public static final RGBA Brown = getColors( ).get( "Brown" );

    /**
     * <span style="background:#ff4040">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown1 <br>
     */
    public static final RGBA Brown1 = getColors( ).get( "Brown1" );

    /**
     * <span style="background:#ee3b3b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown2 <br>
     */
    public static final RGBA Brown2 = getColors( ).get( "Brown2" );

    /**
     * <span style="background:#cd3333">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown3 <br>
     */
    public static final RGBA Brown3 = getColors( ).get( "Brown3" );

    /**
     * <span style="background:#8b2323">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Brown4 <br>
     */
    public static final RGBA Brown4 = getColors( ).get( "Brown4" );

    /**
     * <span style="background:#deb887">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood <br>
     */
    public static final RGBA Burlywood = getColors( ).get( "Burlywood" );

    /**
     * <span style="background:#ffd39b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood1 <br>
     */
    public static final RGBA Burlywood1 = getColors( ).get( "Burlywood1" );

    /**
     * <span style="background:#eec591">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood2 <br>
     */
    public static final RGBA Burlywood2 = getColors( ).get( "Burlywood2" );

    /**
     * <span style="background:#cdaa7d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood3 <br>
     */
    public static final RGBA Burlywood3 = getColors( ).get( "Burlywood3" );

    /**
     * <span style="background:#8b7355">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Burlywood4 <br>
     */
    public static final RGBA Burlywood4 = getColors( ).get( "Burlywood4" );

    /**
     * <span style="background:#5f9ea0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue <br>
     */
    public static final RGBA CadetBlue = getColors( ).get( "CadetBlue" );

    /**
     * <span style="background:#98f5ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue1 <br>
     */
    public static final RGBA CadetBlue1 = getColors( ).get( "CadetBlue1" );

    /**
     * <span style="background:#8ee5ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue2 <br>
     */
    public static final RGBA CadetBlue2 = getColors( ).get( "CadetBlue2" );

    /**
     * <span style="background:#7ac5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue3 <br>
     */
    public static final RGBA CadetBlue3 = getColors( ).get( "CadetBlue3" );

    /**
     * <span style="background:#53868b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CadetBlue4 <br>
     */
    public static final RGBA CadetBlue4 = getColors( ).get( "CadetBlue4" );

    /**
     * <span style="background:#7fff00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse <br>
     */
    public static final RGBA Chartreuse = getColors( ).get( "Chartreuse" );

    /**
     * <span style="background:#76ee00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse2 <br>
     */
    public static final RGBA Chartreuse2 = getColors( ).get( "Chartreuse2" );

    /**
     * <span style="background:#66cd00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse3 <br>
     */
    public static final RGBA Chartreuse3 = getColors( ).get( "Chartreuse3" );

    /**
     * <span style="background:#458b00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chartreuse4 <br>
     */
    public static final RGBA Chartreuse4 = getColors( ).get( "Chartreuse4" );

    /**
     * <span style="background:#d2691e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate <br>
     */
    public static final RGBA Chocolate = getColors( ).get( "Chocolate" );

    /**
     * <span style="background:#ff7f24">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate1 <br>
     */
    public static final RGBA Chocolate1 = getColors( ).get( "Chocolate1" );

    /**
     * <span style="background:#ee7621">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate2 <br>
     */
    public static final RGBA Chocolate2 = getColors( ).get( "Chocolate2" );

    /**
     * <span style="background:#cd661d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Chocolate3 <br>
     */
    public static final RGBA Chocolate3 = getColors( ).get( "Chocolate3" );

    /**
     * <span style="background:#ff7f50">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral <br>
     */
    public static final RGBA Coral = getColors( ).get( "Coral" );

    /**
     * <span style="background:#ff7256">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral1 <br>
     */
    public static final RGBA Coral1 = getColors( ).get( "Coral1" );

    /**
     * <span style="background:#ee6a50">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral2 <br>
     */
    public static final RGBA Coral2 = getColors( ).get( "Coral2" );

    /**
     * <span style="background:#cd5b45">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral3 <br>
     */
    public static final RGBA Coral3 = getColors( ).get( "Coral3" );

    /**
     * <span style="background:#8b3e2f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Coral4 <br>
     */
    public static final RGBA Coral4 = getColors( ).get( "Coral4" );

    /**
     * <span style="background:#6495ed">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> CornflowerBlue <br>
     */
    public static final RGBA CornflowerBlue = getColors( ).get( "CornflowerBlue" );

    /**
     * <span style="background:#fff8dc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk <br>
     */
    public static final RGBA Cornsilk = getColors( ).get( "Cornsilk" );

    /**
     * <span style="background:#eee8cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk2 <br>
     */
    public static final RGBA Cornsilk2 = getColors( ).get( "Cornsilk2" );

    /**
     * <span style="background:#cdc8b1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk3 <br>
     */
    public static final RGBA Cornsilk3 = getColors( ).get( "Cornsilk3" );

    /**
     * <span style="background:#8b8878">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cornsilk4 <br>
     */
    public static final RGBA Cornsilk4 = getColors( ).get( "Cornsilk4" );

    /**
     * <span style="background:#00ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cyan <br>
     */
    public static final RGBA Cyan = getColors( ).get( "Cyan" );

    /**
     * <span style="background:#00eeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cyan2 <br>
     */
    public static final RGBA Cyan2 = getColors( ).get( "Cyan2" );

    /**
     * <span style="background:#00cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Cyan3 <br>
     */
    public static final RGBA Cyan3 = getColors( ).get( "Cyan3" );

    /**
     * <span style="background:#00008b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkBlue <br>
     */
    public static final RGBA DarkBlue = getColors( ).get( "DarkBlue" );

    /**
     * <span style="background:#008b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkCyan <br>
     */
    public static final RGBA DarkCyan = getColors( ).get( "DarkCyan" );

    /**
     * <span style="background:#b8860b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod <br>
     */
    public static final RGBA DarkGoldenrod = getColors( ).get( "DarkGoldenrod" );

    /**
     * <span style="background:#ffb90f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod1 <br>
     */
    public static final RGBA DarkGoldenrod1 = getColors( ).get( "DarkGoldenrod1" );

    /**
     * <span style="background:#eead0e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod2 <br>
     */
    public static final RGBA DarkGoldenrod2 = getColors( ).get( "DarkGoldenrod2" );

    /**
     * <span style="background:#cd950c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod3 <br>
     */
    public static final RGBA DarkGoldenrod3 = getColors( ).get( "DarkGoldenrod3" );

    /**
     * <span style="background:#8b6508">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGoldenrod4 <br>
     */
    public static final RGBA DarkGoldenrod4 = getColors( ).get( "DarkGoldenrod4" );

    /**
     * <span style="background:#a9a9a9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGray <br>
     */
    public static final RGBA DarkGray = getColors( ).get( "DarkGray" );

    /**
     * <span style="background:#006400">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkGreen <br>
     */
    public static final RGBA DarkGreen = getColors( ).get( "DarkGreen" );

    /**
     * <span style="background:#bdb76b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkKhaki <br>
     */
    public static final RGBA DarkKhaki = getColors( ).get( "DarkKhaki" );

    /**
     * <span style="background:#8b008b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkMagenta <br>
     */
    public static final RGBA DarkMagenta = getColors( ).get( "DarkMagenta" );

    /**
     * <span style="background:#556b2f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen <br>
     */
    public static final RGBA DarkOliveGreen = getColors( ).get( "DarkOliveGreen" );

    /**
     * <span style="background:#caff70">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen1 <br>
     */
    public static final RGBA DarkOliveGreen1 = getColors( ).get( "DarkOliveGreen1" );

    /**
     * <span style="background:#bcee68">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen2 <br>
     */
    public static final RGBA DarkOliveGreen2 = getColors( ).get( "DarkOliveGreen2" );

    /**
     * <span style="background:#a2cd5a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen3 <br>
     */
    public static final RGBA DarkOliveGreen3 = getColors( ).get( "DarkOliveGreen3" );

    /**
     * <span style="background:#6e8b3d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOliveGreen4 <br>
     */
    public static final RGBA DarkOliveGreen4 = getColors( ).get( "DarkOliveGreen4" );

    /**
     * <span style="background:#ff8c00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange <br>
     */
    public static final RGBA DarkOrange = getColors( ).get( "DarkOrange" );

    /**
     * <span style="background:#ff7f00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange1 <br>
     */
    public static final RGBA DarkOrange1 = getColors( ).get( "DarkOrange1" );

    /**
     * <span style="background:#ee7600">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange2 <br>
     */
    public static final RGBA DarkOrange2 = getColors( ).get( "DarkOrange2" );

    /**
     * <span style="background:#cd6600">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange3 <br>
     */
    public static final RGBA DarkOrange3 = getColors( ).get( "DarkOrange3" );

    /**
     * <span style="background:#8b4500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrange4 <br>
     */
    public static final RGBA DarkOrange4 = getColors( ).get( "DarkOrange4" );

    /**
     * <span style="background:#9932cc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid <br>
     */
    public static final RGBA DarkOrchid = getColors( ).get( "DarkOrchid" );

    /**
     * <span style="background:#bf3eff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid1 <br>
     */
    public static final RGBA DarkOrchid1 = getColors( ).get( "DarkOrchid1" );

    /**
     * <span style="background:#b23aee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid2 <br>
     */
    public static final RGBA DarkOrchid2 = getColors( ).get( "DarkOrchid2" );

    /**
     * <span style="background:#9a32cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid3 <br>
     */
    public static final RGBA DarkOrchid3 = getColors( ).get( "DarkOrchid3" );

    /**
     * <span style="background:#68228b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkOrchid4 <br>
     */
    public static final RGBA DarkOrchid4 = getColors( ).get( "DarkOrchid4" );

    /**
     * <span style="background:#8b0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkRed <br>
     */
    public static final RGBA DarkRed = getColors( ).get( "DarkRed" );

    /**
     * <span style="background:#e9967a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSalmon <br>
     */
    public static final RGBA DarkSalmon = getColors( ).get( "DarkSalmon" );

    /**
     * <span style="background:#8fbc8f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen <br>
     */
    public static final RGBA DarkSeaGreen = getColors( ).get( "DarkSeaGreen" );

    /**
     * <span style="background:#c1ffc1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen1 <br>
     */
    public static final RGBA DarkSeaGreen1 = getColors( ).get( "DarkSeaGreen1" );

    /**
     * <span style="background:#b4eeb4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen2 <br>
     */
    public static final RGBA DarkSeaGreen2 = getColors( ).get( "DarkSeaGreen2" );

    /**
     * <span style="background:#9bcd9b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen3 <br>
     */
    public static final RGBA DarkSeaGreen3 = getColors( ).get( "DarkSeaGreen3" );

    /**
     * <span style="background:#698b69">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSeaGreen4 <br>
     */
    public static final RGBA DarkSeaGreen4 = getColors( ).get( "DarkSeaGreen4" );

    /**
     * <span style="background:#483d8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateBlue <br>
     */
    public static final RGBA DarkSlateBlue = getColors( ).get( "DarkSlateBlue" );

    /**
     * <span style="background:#2f4f4f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray <br>
     */
    public static final RGBA DarkSlateGray = getColors( ).get( "DarkSlateGray" );

    /**
     * <span style="background:#97ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray1 <br>
     */
    public static final RGBA DarkSlateGray1 = getColors( ).get( "DarkSlateGray1" );

    /**
     * <span style="background:#8deeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray2 <br>
     */
    public static final RGBA DarkSlateGray2 = getColors( ).get( "DarkSlateGray2" );

    /**
     * <span style="background:#79cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray3 <br>
     */
    public static final RGBA DarkSlateGray3 = getColors( ).get( "DarkSlateGray3" );

    /**
     * <span style="background:#528b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkSlateGray4 <br>
     */
    public static final RGBA DarkSlateGray4 = getColors( ).get( "DarkSlateGray4" );

    /**
     * <span style="background:#00ced1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkTurquoise <br>
     */
    public static final RGBA DarkTurquoise = getColors( ).get( "DarkTurquoise" );

    /**
     * <span style="background:#9400d3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DarkViolet <br>
     */
    public static final RGBA DarkViolet = getColors( ).get( "DarkViolet" );

    /**
     * <span style="background:#ff1493">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink <br>
     */
    public static final RGBA DeepPink = getColors( ).get( "DeepPink" );

    /**
     * <span style="background:#ee1289">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink2 <br>
     */
    public static final RGBA DeepPink2 = getColors( ).get( "DeepPink2" );

    /**
     * <span style="background:#cd1076">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink3 <br>
     */
    public static final RGBA DeepPink3 = getColors( ).get( "DeepPink3" );

    /**
     * <span style="background:#8b0a50">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepPink4 <br>
     */
    public static final RGBA DeepPink4 = getColors( ).get( "DeepPink4" );

    /**
     * <span style="background:#00bfff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue <br>
     */
    public static final RGBA DeepSkyBlue = getColors( ).get( "DeepSkyBlue" );

    /**
     * <span style="background:#00b2ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue2 <br>
     */
    public static final RGBA DeepSkyBlue2 = getColors( ).get( "DeepSkyBlue2" );

    /**
     * <span style="background:#009acd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue3 <br>
     */
    public static final RGBA DeepSkyBlue3 = getColors( ).get( "DeepSkyBlue3" );

    /**
     * <span style="background:#00688b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DeepSkyBlue4 <br>
     */
    public static final RGBA DeepSkyBlue4 = getColors( ).get( "DeepSkyBlue4" );

    /**
     * <span style="background:#696969">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DimGray <br>
     */
    public static final RGBA DimGray = getColors( ).get( "DimGray" );

    /**
     * <span style="background:#1e90ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue <br>
     */
    public static final RGBA DodgerBlue = getColors( ).get( "DodgerBlue" );

    /**
     * <span style="background:#1c86ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue2 <br>
     */
    public static final RGBA DodgerBlue2 = getColors( ).get( "DodgerBlue2" );

    /**
     * <span style="background:#1874cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue3 <br>
     */
    public static final RGBA DodgerBlue3 = getColors( ).get( "DodgerBlue3" );

    /**
     * <span style="background:#104e8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> DodgerBlue4 <br>
     */
    public static final RGBA DodgerBlue4 = getColors( ).get( "DodgerBlue4" );

    /**
     * <span style="background:#b22222">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick <br>
     */
    public static final RGBA Firebrick = getColors( ).get( "Firebrick" );

    /**
     * <span style="background:#ff3030">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick1 <br>
     */
    public static final RGBA Firebrick1 = getColors( ).get( "Firebrick1" );

    /**
     * <span style="background:#ee2c2c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick2 <br>
     */
    public static final RGBA Firebrick2 = getColors( ).get( "Firebrick2" );

    /**
     * <span style="background:#cd2626">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick3 <br>
     */
    public static final RGBA Firebrick3 = getColors( ).get( "Firebrick3" );

    /**
     * <span style="background:#8b1a1a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Firebrick4 <br>
     */
    public static final RGBA Firebrick4 = getColors( ).get( "Firebrick4" );

    /**
     * <span style="background:#fffaf0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> FloralWhite <br>
     */
    public static final RGBA FloralWhite = getColors( ).get( "FloralWhite" );

    /**
     * <span style="background:#228b22">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> ForestGreen <br>
     */
    public static final RGBA ForestGreen = getColors( ).get( "ForestGreen" );

    /**
     * <span style="background:#dcdcdc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gainsboro <br>
     */
    public static final RGBA Gainsboro = getColors( ).get( "Gainsboro" );

    /**
     * <span style="background:#f8f8ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> GhostWhite <br>
     */
    public static final RGBA GhostWhite = getColors( ).get( "GhostWhite" );

    /**
     * <span style="background:#ffd700">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold <br>
     */
    public static final RGBA Gold = getColors( ).get( "Gold" );

    /**
     * <span style="background:#eec900">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold2 <br>
     */
    public static final RGBA Gold2 = getColors( ).get( "Gold2" );

    /**
     * <span style="background:#cdad00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold3 <br>
     */
    public static final RGBA Gold3 = getColors( ).get( "Gold3" );

    /**
     * <span style="background:#8b7500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gold4 <br>
     */
    public static final RGBA Gold4 = getColors( ).get( "Gold4" );

    /**
     * <span style="background:#daa520">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod <br>
     */
    public static final RGBA Goldenrod = getColors( ).get( "Goldenrod" );

    /**
     * <span style="background:#ffc125">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod1 <br>
     */
    public static final RGBA Goldenrod1 = getColors( ).get( "Goldenrod1" );

    /**
     * <span style="background:#eeb422">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod2 <br>
     */
    public static final RGBA Goldenrod2 = getColors( ).get( "Goldenrod2" );

    /**
     * <span style="background:#cd9b1d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod3 <br>
     */
    public static final RGBA Goldenrod3 = getColors( ).get( "Goldenrod3" );

    /**
     * <span style="background:#8b6914">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Goldenrod4 <br>
     */
    public static final RGBA Goldenrod4 = getColors( ).get( "Goldenrod4" );

    /**
     * <span style="background:#bebebe">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Gray <br>
     */
    public static final RGBA Gray = getColors( ).get( "Gray" );

    /**
     * <span style="background:#00ff00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green <br>
     */
    public static final RGBA Green = getColors( ).get( "Green" );

    /**
     * <span style="background:#00ee00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green2 <br>
     */
    public static final RGBA Green2 = getColors( ).get( "Green2" );

    /**
     * <span style="background:#00cd00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green3 <br>
     */
    public static final RGBA Green3 = getColors( ).get( "Green3" );

    /**
     * <span style="background:#008b00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Green4 <br>
     */
    public static final RGBA Green4 = getColors( ).get( "Green4" );

    /**
     * <span style="background:#adff2f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> GreenYellow <br>
     */
    public static final RGBA GreenYellow = getColors( ).get( "GreenYellow" );

    /**
     * <span style="background:#f0fff0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew <br>
     */
    public static final RGBA Honeydew = getColors( ).get( "Honeydew" );

    /**
     * <span style="background:#e0eee0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew2 <br>
     */
    public static final RGBA Honeydew2 = getColors( ).get( "Honeydew2" );

    /**
     * <span style="background:#c1cdc1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew3 <br>
     */
    public static final RGBA Honeydew3 = getColors( ).get( "Honeydew3" );

    /**
     * <span style="background:#838b83">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Honeydew4 <br>
     */
    public static final RGBA Honeydew4 = getColors( ).get( "Honeydew4" );

    /**
     * <span style="background:#ff69b4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink <br>
     */
    public static final RGBA HotPink = getColors( ).get( "HotPink" );

    /**
     * <span style="background:#ff6eb4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink1 <br>
     */
    public static final RGBA HotPink1 = getColors( ).get( "HotPink1" );

    /**
     * <span style="background:#ee6aa7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink2 <br>
     */
    public static final RGBA HotPink2 = getColors( ).get( "HotPink2" );

    /**
     * <span style="background:#cd6090">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink3 <br>
     */
    public static final RGBA HotPink3 = getColors( ).get( "HotPink3" );

    /**
     * <span style="background:#8b3a62">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> HotPink4 <br>
     */
    public static final RGBA HotPink4 = getColors( ).get( "HotPink4" );

    /**
     * <span style="background:#cd5c5c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed <br>
     */
    public static final RGBA IndianRed = getColors( ).get( "IndianRed" );

    /**
     * <span style="background:#ff6a6a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed1 <br>
     */
    public static final RGBA IndianRed1 = getColors( ).get( "IndianRed1" );

    /**
     * <span style="background:#ee6363">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed2 <br>
     */
    public static final RGBA IndianRed2 = getColors( ).get( "IndianRed2" );

    /**
     * <span style="background:#cd5555">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed3 <br>
     */
    public static final RGBA IndianRed3 = getColors( ).get( "IndianRed3" );

    /**
     * <span style="background:#8b3a3a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> IndianRed4 <br>
     */
    public static final RGBA IndianRed4 = getColors( ).get( "IndianRed4" );

    /**
     * <span style="background:#fffff0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory <br>
     */
    public static final RGBA Ivory = getColors( ).get( "Ivory" );

    /**
     * <span style="background:#eeeee0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory2 <br>
     */
    public static final RGBA Ivory2 = getColors( ).get( "Ivory2" );

    /**
     * <span style="background:#cdcdc1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory3 <br>
     */
    public static final RGBA Ivory3 = getColors( ).get( "Ivory3" );

    /**
     * <span style="background:#8b8b83">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Ivory4 <br>
     */
    public static final RGBA Ivory4 = getColors( ).get( "Ivory4" );

    /**
     * <span style="background:#f0e68c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki <br>
     */
    public static final RGBA Khaki = getColors( ).get( "Khaki" );

    /**
     * <span style="background:#fff68f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki1 <br>
     */
    public static final RGBA Khaki1 = getColors( ).get( "Khaki1" );

    /**
     * <span style="background:#eee685">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki2 <br>
     */
    public static final RGBA Khaki2 = getColors( ).get( "Khaki2" );

    /**
     * <span style="background:#cdc673">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki3 <br>
     */
    public static final RGBA Khaki3 = getColors( ).get( "Khaki3" );

    /**
     * <span style="background:#8b864e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Khaki4 <br>
     */
    public static final RGBA Khaki4 = getColors( ).get( "Khaki4" );

    /**
     * <span style="background:#e6e6fa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Lavender <br>
     */
    public static final RGBA Lavender = getColors( ).get( "Lavender" );

    /**
     * <span style="background:#fff0f5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush <br>
     */
    public static final RGBA LavenderBlush = getColors( ).get( "LavenderBlush" );

    /**
     * <span style="background:#eee0e5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush2 <br>
     */
    public static final RGBA LavenderBlush2 = getColors( ).get( "LavenderBlush2" );

    /**
     * <span style="background:#cdc1c5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush3 <br>
     */
    public static final RGBA LavenderBlush3 = getColors( ).get( "LavenderBlush3" );

    /**
     * <span style="background:#8b8386">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LavenderBlush4 <br>
     */
    public static final RGBA LavenderBlush4 = getColors( ).get( "LavenderBlush4" );

    /**
     * <span style="background:#7cfc00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LawnGreen <br>
     */
    public static final RGBA LawnGreen = getColors( ).get( "LawnGreen" );

    /**
     * <span style="background:#fffacd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon <br>
     */
    public static final RGBA LemonChiffon = getColors( ).get( "LemonChiffon" );

    /**
     * <span style="background:#eee9bf">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon2 <br>
     */
    public static final RGBA LemonChiffon2 = getColors( ).get( "LemonChiffon2" );

    /**
     * <span style="background:#cdc9a5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon3 <br>
     */
    public static final RGBA LemonChiffon3 = getColors( ).get( "LemonChiffon3" );

    /**
     * <span style="background:#8b8970">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LemonChiffon4 <br>
     */
    public static final RGBA LemonChiffon4 = getColors( ).get( "LemonChiffon4" );

    /**
     * <span style="background:#add8e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue <br>
     */
    public static final RGBA LightBlue = getColors( ).get( "LightBlue" );

    /**
     * <span style="background:#bfefff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue1 <br>
     */
    public static final RGBA LightBlue1 = getColors( ).get( "LightBlue1" );

    /**
     * <span style="background:#b2dfee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue2 <br>
     */
    public static final RGBA LightBlue2 = getColors( ).get( "LightBlue2" );

    /**
     * <span style="background:#9ac0cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue3 <br>
     */
    public static final RGBA LightBlue3 = getColors( ).get( "LightBlue3" );

    /**
     * <span style="background:#68838b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightBlue4 <br>
     */
    public static final RGBA LightBlue4 = getColors( ).get( "LightBlue4" );

    /**
     * <span style="background:#f08080">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCoral <br>
     */
    public static final RGBA LightCoral = getColors( ).get( "LightCoral" );

    /**
     * <span style="background:#e0ffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan <br>
     */
    public static final RGBA LightCyan = getColors( ).get( "LightCyan" );

    /**
     * <span style="background:#d1eeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan2 <br>
     */
    public static final RGBA LightCyan2 = getColors( ).get( "LightCyan2" );

    /**
     * <span style="background:#b4cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan3 <br>
     */
    public static final RGBA LightCyan3 = getColors( ).get( "LightCyan3" );

    /**
     * <span style="background:#7a8b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightCyan4 <br>
     */
    public static final RGBA LightCyan4 = getColors( ).get( "LightCyan4" );

    /**
     * <span style="background:#eedd82">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod <br>
     */
    public static final RGBA LightGoldenrod = getColors( ).get( "LightGoldenrod" );

    /**
     * <span style="background:#ffec8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod1 <br>
     */
    public static final RGBA LightGoldenrod1 = getColors( ).get( "LightGoldenrod1" );

    /**
     * <span style="background:#eedc82">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod2 <br>
     */
    public static final RGBA LightGoldenrod2 = getColors( ).get( "LightGoldenrod2" );

    /**
     * <span style="background:#cdbe70">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod3 <br>
     */
    public static final RGBA LightGoldenrod3 = getColors( ).get( "LightGoldenrod3" );

    /**
     * <span style="background:#8b814c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrod4 <br>
     */
    public static final RGBA LightGoldenrod4 = getColors( ).get( "LightGoldenrod4" );

    /**
     * <span style="background:#fafad2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGoldenrodYellow <br>
     */
    public static final RGBA LightGoldenrodYellow = getColors( ).get( "LightGoldenrodYellow" );

    /**
     * <span style="background:#90ee90">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGreen <br>
     */
    public static final RGBA LightGreen = getColors( ).get( "LightGreen" );

    /**
     * <span style="background:#d3d3d3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightGrey <br>
     */
    public static final RGBA LightGrey = getColors( ).get( "LightGrey" );

    /**
     * <span style="background:#ffb6c1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink <br>
     */
    public static final RGBA LightPink = getColors( ).get( "LightPink" );

    /**
     * <span style="background:#ffaeb9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink1 <br>
     */
    public static final RGBA LightPink1 = getColors( ).get( "LightPink1" );

    /**
     * <span style="background:#eea2ad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink2 <br>
     */
    public static final RGBA LightPink2 = getColors( ).get( "LightPink2" );

    /**
     * <span style="background:#cd8c95">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink3 <br>
     */
    public static final RGBA LightPink3 = getColors( ).get( "LightPink3" );

    /**
     * <span style="background:#8b5f65">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightPink4 <br>
     */
    public static final RGBA LightPink4 = getColors( ).get( "LightPink4" );

    /**
     * <span style="background:#ffa07a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon <br>
     */
    public static final RGBA LightSalmon = getColors( ).get( "LightSalmon" );

    /**
     * <span style="background:#ee9572">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon2 <br>
     */
    public static final RGBA LightSalmon2 = getColors( ).get( "LightSalmon2" );

    /**
     * <span style="background:#cd8162">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon3 <br>
     */
    public static final RGBA LightSalmon3 = getColors( ).get( "LightSalmon3" );

    /**
     * <span style="background:#8b5742">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSalmon4 <br>
     */
    public static final RGBA LightSalmon4 = getColors( ).get( "LightSalmon4" );

    /**
     * <span style="background:#20b2aa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSeaGreen <br>
     */
    public static final RGBA LightSeaGreen = getColors( ).get( "LightSeaGreen" );

    /**
     * <span style="background:#87cefa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue <br>
     */
    public static final RGBA LightSkyBlue = getColors( ).get( "LightSkyBlue" );

    /**
     * <span style="background:#b0e2ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue1 <br>
     */
    public static final RGBA LightSkyBlue1 = getColors( ).get( "LightSkyBlue1" );

    /**
     * <span style="background:#a4d3ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue2 <br>
     */
    public static final RGBA LightSkyBlue2 = getColors( ).get( "LightSkyBlue2" );

    /**
     * <span style="background:#8db6cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue3 <br>
     */
    public static final RGBA LightSkyBlue3 = getColors( ).get( "LightSkyBlue3" );

    /**
     * <span style="background:#607b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSkyBlue4 <br>
     */
    public static final RGBA LightSkyBlue4 = getColors( ).get( "LightSkyBlue4" );

    /**
     * <span style="background:#8470ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSlateBlue <br>
     */
    public static final RGBA LightSlateBlue = getColors( ).get( "LightSlateBlue" );

    /**
     * <span style="background:#778899">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSlateGray <br>
     */
    public static final RGBA LightSlateGray = getColors( ).get( "LightSlateGray" );

    /**
     * <span style="background:#b0c4de">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue <br>
     */
    public static final RGBA LightSteelBlue = getColors( ).get( "LightSteelBlue" );

    /**
     * <span style="background:#cae1ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue1 <br>
     */
    public static final RGBA LightSteelBlue1 = getColors( ).get( "LightSteelBlue1" );

    /**
     * <span style="background:#bcd2ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue2 <br>
     */
    public static final RGBA LightSteelBlue2 = getColors( ).get( "LightSteelBlue2" );

    /**
     * <span style="background:#a2b5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue3 <br>
     */
    public static final RGBA LightSteelBlue3 = getColors( ).get( "LightSteelBlue3" );

    /**
     * <span style="background:#6e7b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightSteelBlue4 <br>
     */
    public static final RGBA LightSteelBlue4 = getColors( ).get( "LightSteelBlue4" );

    /**
     * <span style="background:#ffffe0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow <br>
     */
    public static final RGBA LightYellow = getColors( ).get( "LightYellow" );

    /**
     * <span style="background:#eeeed1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow2 <br>
     */
    public static final RGBA LightYellow2 = getColors( ).get( "LightYellow2" );

    /**
     * <span style="background:#cdcdb4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow3 <br>
     */
    public static final RGBA LightYellow3 = getColors( ).get( "LightYellow3" );

    /**
     * <span style="background:#8b8b7a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LightYellow4 <br>
     */
    public static final RGBA LightYellow4 = getColors( ).get( "LightYellow4" );

    /**
     * <span style="background:#32cd32">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> LimeGreen <br>
     */
    public static final RGBA LimeGreen = getColors( ).get( "LimeGreen" );

    /**
     * <span style="background:#faf0e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Linen <br>
     */
    public static final RGBA Linen = getColors( ).get( "Linen" );

    /**
     * <span style="background:#ff00ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Magenta <br>
     */
    public static final RGBA Magenta = getColors( ).get( "Magenta" );

    /**
     * <span style="background:#ee00ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Magenta2 <br>
     */
    public static final RGBA Magenta2 = getColors( ).get( "Magenta2" );

    /**
     * <span style="background:#cd00cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Magenta3 <br>
     */
    public static final RGBA Magenta3 = getColors( ).get( "Magenta3" );

    /**
     * <span style="background:#b03060">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon <br>
     */
    public static final RGBA Maroon = getColors( ).get( "Maroon" );

    /**
     * <span style="background:#ff34b3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon1 <br>
     */
    public static final RGBA Maroon1 = getColors( ).get( "Maroon1" );

    /**
     * <span style="background:#ee30a7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon2 <br>
     */
    public static final RGBA Maroon2 = getColors( ).get( "Maroon2" );

    /**
     * <span style="background:#cd2990">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon3 <br>
     */
    public static final RGBA Maroon3 = getColors( ).get( "Maroon3" );

    /**
     * <span style="background:#8b1c62">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Maroon4 <br>
     */
    public static final RGBA Maroon4 = getColors( ).get( "Maroon4" );

    /**
     * <span style="background:#66cdaa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumAquamarine <br>
     */
    public static final RGBA MediumAquamarine = getColors( ).get( "MediumAquamarine" );

    /**
     * <span style="background:#0000cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumBlue <br>
     */
    public static final RGBA MediumBlue = getColors( ).get( "MediumBlue" );

    /**
     * <span style="background:#ba55d3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid <br>
     */
    public static final RGBA MediumOrchid = getColors( ).get( "MediumOrchid" );

    /**
     * <span style="background:#e066ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid1 <br>
     */
    public static final RGBA MediumOrchid1 = getColors( ).get( "MediumOrchid1" );

    /**
     * <span style="background:#d15fee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid2 <br>
     */
    public static final RGBA MediumOrchid2 = getColors( ).get( "MediumOrchid2" );

    /**
     * <span style="background:#b452cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid3 <br>
     */
    public static final RGBA MediumOrchid3 = getColors( ).get( "MediumOrchid3" );

    /**
     * <span style="background:#7a378b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumOrchid4 <br>
     */
    public static final RGBA MediumOrchid4 = getColors( ).get( "MediumOrchid4" );

    /**
     * <span style="background:#9370db">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple <br>
     */
    public static final RGBA MediumPurple = getColors( ).get( "MediumPurple" );

    /**
     * <span style="background:#ab82ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple1 <br>
     */
    public static final RGBA MediumPurple1 = getColors( ).get( "MediumPurple1" );

    /**
     * <span style="background:#9f79ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple2 <br>
     */
    public static final RGBA MediumPurple2 = getColors( ).get( "MediumPurple2" );

    /**
     * <span style="background:#8968cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple3 <br>
     */
    public static final RGBA MediumPurple3 = getColors( ).get( "MediumPurple3" );

    /**
     * <span style="background:#5d478b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumPurple4 <br>
     */
    public static final RGBA MediumPurple4 = getColors( ).get( "MediumPurple4" );

    /**
     * <span style="background:#3cb371">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumSeaGreen <br>
     */
    public static final RGBA MediumSeaGreen = getColors( ).get( "MediumSeaGreen" );

    /**
     * <span style="background:#7b68ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumSlateBlue <br>
     */
    public static final RGBA MediumSlateBlue = getColors( ).get( "MediumSlateBlue" );

    /**
     * <span style="background:#00fa9a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumSpring <br>
     */
    public static final RGBA MediumSpring = getColors( ).get( "MediumSpring" );

    /**
     * <span style="background:#48d1cc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumTurquoise <br>
     */
    public static final RGBA MediumTurquoise = getColors( ).get( "MediumTurquoise" );

    /**
     * <span style="background:#c71585">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MediumVioletRed <br>
     */
    public static final RGBA MediumVioletRed = getColors( ).get( "MediumVioletRed" );

    /**
     * <span style="background:#191970">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MidnightBlue <br>
     */
    public static final RGBA MidnightBlue = getColors( ).get( "MidnightBlue" );

    /**
     * <span style="background:#f5fffa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MintCream <br>
     */
    public static final RGBA MintCream = getColors( ).get( "MintCream" );

    /**
     * <span style="background:#ffe4e1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose <br>
     */
    public static final RGBA MistyRose = getColors( ).get( "MistyRose" );

    /**
     * <span style="background:#eed5d2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose2 <br>
     */
    public static final RGBA MistyRose2 = getColors( ).get( "MistyRose2" );

    /**
     * <span style="background:#cdb7b5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose3 <br>
     */
    public static final RGBA MistyRose3 = getColors( ).get( "MistyRose3" );

    /**
     * <span style="background:#8b7d7b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> MistyRose4 <br>
     */
    public static final RGBA MistyRose4 = getColors( ).get( "MistyRose4" );

    /**
     * <span style="background:#ffe4b5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Moccasin <br>
     */
    public static final RGBA Moccasin = getColors( ).get( "Moccasin" );

    /**
     * <span style="background:#ffdead">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite <br>
     */
    public static final RGBA NavajoWhite = getColors( ).get( "NavajoWhite" );

    /**
     * <span style="background:#eecfa1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite2 <br>
     */
    public static final RGBA NavajoWhite2 = getColors( ).get( "NavajoWhite2" );

    /**
     * <span style="background:#cdb38b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite3 <br>
     */
    public static final RGBA NavajoWhite3 = getColors( ).get( "NavajoWhite3" );

    /**
     * <span style="background:#8b795e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> NavajoWhite4 <br>
     */
    public static final RGBA NavajoWhite4 = getColors( ).get( "NavajoWhite4" );

    /**
     * <span style="background:#000080">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Navy <br>
     */
    public static final RGBA Navy = getColors( ).get( "Navy" );

    /**
     * <span style="background:#fdf5e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OldLace <br>
     */
    public static final RGBA OldLace = getColors( ).get( "OldLace" );

    /**
     * <span style="background:#6b8e23">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab <br>
     */
    public static final RGBA OliveDrab = getColors( ).get( "OliveDrab" );

    /**
     * <span style="background:#c0ff3e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab1 <br>
     */
    public static final RGBA OliveDrab1 = getColors( ).get( "OliveDrab1" );

    /**
     * <span style="background:#b3ee3a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab2 <br>
     */
    public static final RGBA OliveDrab2 = getColors( ).get( "OliveDrab2" );

    /**
     * <span style="background:#698b22">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OliveDrab4 <br>
     */
    public static final RGBA OliveDrab4 = getColors( ).get( "OliveDrab4" );

    /**
     * <span style="background:#ffa500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange <br>
     */
    public static final RGBA Orange = getColors( ).get( "Orange" );

    /**
     * <span style="background:#ee9a00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange2 <br>
     */
    public static final RGBA Orange2 = getColors( ).get( "Orange2" );

    /**
     * <span style="background:#cd8500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange3 <br>
     */
    public static final RGBA Orange3 = getColors( ).get( "Orange3" );

    /**
     * <span style="background:#8b5a00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orange4 <br>
     */
    public static final RGBA Orange4 = getColors( ).get( "Orange4" );

    /**
     * <span style="background:#ff4500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed <br>
     */
    public static final RGBA OrangeRed = getColors( ).get( "OrangeRed" );

    /**
     * <span style="background:#ee4000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed2 <br>
     */
    public static final RGBA OrangeRed2 = getColors( ).get( "OrangeRed2" );

    /**
     * <span style="background:#cd3700">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed3 <br>
     */
    public static final RGBA OrangeRed3 = getColors( ).get( "OrangeRed3" );

    /**
     * <span style="background:#8b2500">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> OrangeRed4 <br>
     */
    public static final RGBA OrangeRed4 = getColors( ).get( "OrangeRed4" );

    /**
     * <span style="background:#da70d6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid <br>
     */
    public static final RGBA Orchid = getColors( ).get( "Orchid" );

    /**
     * <span style="background:#ff83fa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid1 <br>
     */
    public static final RGBA Orchid1 = getColors( ).get( "Orchid1" );

    /**
     * <span style="background:#ee7ae9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid2 <br>
     */
    public static final RGBA Orchid2 = getColors( ).get( "Orchid2" );

    /**
     * <span style="background:#cd69c9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid3 <br>
     */
    public static final RGBA Orchid3 = getColors( ).get( "Orchid3" );

    /**
     * <span style="background:#8b4789">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Orchid4 <br>
     */
    public static final RGBA Orchid4 = getColors( ).get( "Orchid4" );

    /**
     * <span style="background:#eee8aa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGoldenrod <br>
     */
    public static final RGBA PaleGoldenrod = getColors( ).get( "PaleGoldenrod" );

    /**
     * <span style="background:#98fb98">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen <br>
     */
    public static final RGBA PaleGreen = getColors( ).get( "PaleGreen" );

    /**
     * <span style="background:#9aff9a">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen1 <br>
     */
    public static final RGBA PaleGreen1 = getColors( ).get( "PaleGreen1" );

    /**
     * <span style="background:#7ccd7c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen3 <br>
     */
    public static final RGBA PaleGreen3 = getColors( ).get( "PaleGreen3" );

    /**
     * <span style="background:#548b54">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleGreen4 <br>
     */
    public static final RGBA PaleGreen4 = getColors( ).get( "PaleGreen4" );

    /**
     * <span style="background:#afeeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise <br>
     */
    public static final RGBA PaleTurquoise = getColors( ).get( "PaleTurquoise" );

    /**
     * <span style="background:#bbffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise1 <br>
     */
    public static final RGBA PaleTurquoise1 = getColors( ).get( "PaleTurquoise1" );

    /**
     * <span style="background:#aeeeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise2 <br>
     */
    public static final RGBA PaleTurquoise2 = getColors( ).get( "PaleTurquoise2" );

    /**
     * <span style="background:#96cdcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise3 <br>
     */
    public static final RGBA PaleTurquoise3 = getColors( ).get( "PaleTurquoise3" );

    /**
     * <span style="background:#668b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleTurquoise4 <br>
     */
    public static final RGBA PaleTurquoise4 = getColors( ).get( "PaleTurquoise4" );

    /**
     * <span style="background:#db7093">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed <br>
     */
    public static final RGBA PaleVioletRed = getColors( ).get( "PaleVioletRed" );

    /**
     * <span style="background:#ff82ab">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed1 <br>
     */
    public static final RGBA PaleVioletRed1 = getColors( ).get( "PaleVioletRed1" );

    /**
     * <span style="background:#ee799f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed2 <br>
     */
    public static final RGBA PaleVioletRed2 = getColors( ).get( "PaleVioletRed2" );

    /**
     * <span style="background:#cd6889">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed3 <br>
     */
    public static final RGBA PaleVioletRed3 = getColors( ).get( "PaleVioletRed3" );

    /**
     * <span style="background:#8b475d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PaleVioletRed4 <br>
     */
    public static final RGBA PaleVioletRed4 = getColors( ).get( "PaleVioletRed4" );

    /**
     * <span style="background:#ffefd5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PapayaWhip <br>
     */
    public static final RGBA PapayaWhip = getColors( ).get( "PapayaWhip" );

    /**
     * <span style="background:#ffdab9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff <br>
     */
    public static final RGBA PeachPuff = getColors( ).get( "PeachPuff" );

    /**
     * <span style="background:#eecbad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff2 <br>
     */
    public static final RGBA PeachPuff2 = getColors( ).get( "PeachPuff2" );

    /**
     * <span style="background:#cdaf95">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff3 <br>
     */
    public static final RGBA PeachPuff3 = getColors( ).get( "PeachPuff3" );

    /**
     * <span style="background:#8b7765">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PeachPuff4 <br>
     */
    public static final RGBA PeachPuff4 = getColors( ).get( "PeachPuff4" );

    /**
     * <span style="background:#cd853f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Peru <br>
     */
    public static final RGBA Peru = getColors( ).get( "Peru" );

    /**
     * <span style="background:#ffc0cb">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink <br>
     */
    public static final RGBA Pink = getColors( ).get( "Pink" );

    /**
     * <span style="background:#ffb5c5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink1 <br>
     */
    public static final RGBA Pink1 = getColors( ).get( "Pink1" );

    /**
     * <span style="background:#eea9b8">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink2 <br>
     */
    public static final RGBA Pink2 = getColors( ).get( "Pink2" );

    /**
     * <span style="background:#cd919e">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink3 <br>
     */
    public static final RGBA Pink3 = getColors( ).get( "Pink3" );

    /**
     * <span style="background:#8b636c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Pink4 <br>
     */
    public static final RGBA Pink4 = getColors( ).get( "Pink4" );

    /**
     * <span style="background:#dda0dd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum <br>
     */
    public static final RGBA Plum = getColors( ).get( "Plum" );

    /**
     * <span style="background:#ffbbff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum1 <br>
     */
    public static final RGBA Plum1 = getColors( ).get( "Plum1" );

    /**
     * <span style="background:#eeaeee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum2 <br>
     */
    public static final RGBA Plum2 = getColors( ).get( "Plum2" );

    /**
     * <span style="background:#cd96cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum3 <br>
     */
    public static final RGBA Plum3 = getColors( ).get( "Plum3" );

    /**
     * <span style="background:#8b668b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Plum4 <br>
     */
    public static final RGBA Plum4 = getColors( ).get( "Plum4" );

    /**
     * <span style="background:#b0e0e6">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> PowderBlue <br>
     */
    public static final RGBA PowderBlue = getColors( ).get( "PowderBlue" );

    /**
     * <span style="background:#a020f0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple <br>
     */
    public static final RGBA Purple = getColors( ).get( "Purple" );

    /**
     * <span style="background:#9b30ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple1 <br>
     */
    public static final RGBA Purple1 = getColors( ).get( "Purple1" );

    /**
     * <span style="background:#912cee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple2 <br>
     */
    public static final RGBA Purple2 = getColors( ).get( "Purple2" );

    /**
     * <span style="background:#7d26cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple3 <br>
     */
    public static final RGBA Purple3 = getColors( ).get( "Purple3" );

    /**
     * <span style="background:#551a8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Purple4 <br>
     */
    public static final RGBA Purple4 = getColors( ).get( "Purple4" );

    /**
     * <span style="background:#ff0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Red <br>
     */
    public static final RGBA Red = getColors( ).get( "Red" );

    /**
     * <span style="background:#ee0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Red2 <br>
     */
    public static final RGBA Red2 = getColors( ).get( "Red2" );

    /**
     * <span style="background:#cd0000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Red3 <br>
     */
    public static final RGBA Red3 = getColors( ).get( "Red3" );

    /**
     * <span style="background:#bc8f8f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown <br>
     */
    public static final RGBA RosyBrown = getColors( ).get( "RosyBrown" );

    /**
     * <span style="background:#ffc1c1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown1 <br>
     */
    public static final RGBA RosyBrown1 = getColors( ).get( "RosyBrown1" );

    /**
     * <span style="background:#eeb4b4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown2 <br>
     */
    public static final RGBA RosyBrown2 = getColors( ).get( "RosyBrown2" );

    /**
     * <span style="background:#cd9b9b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown3 <br>
     */
    public static final RGBA RosyBrown3 = getColors( ).get( "RosyBrown3" );

    /**
     * <span style="background:#8b6969">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RosyBrown4 <br>
     */
    public static final RGBA RosyBrown4 = getColors( ).get( "RosyBrown4" );

    /**
     * <span style="background:#4169e1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue <br>
     */
    public static final RGBA RoyalBlue = getColors( ).get( "RoyalBlue" );

    /**
     * <span style="background:#4876ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue1 <br>
     */
    public static final RGBA RoyalBlue1 = getColors( ).get( "RoyalBlue1" );

    /**
     * <span style="background:#436eee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue2 <br>
     */
    public static final RGBA RoyalBlue2 = getColors( ).get( "RoyalBlue2" );

    /**
     * <span style="background:#3a5fcd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue3 <br>
     */
    public static final RGBA RoyalBlue3 = getColors( ).get( "RoyalBlue3" );

    /**
     * <span style="background:#27408b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> RoyalBlue4 <br>
     */
    public static final RGBA RoyalBlue4 = getColors( ).get( "RoyalBlue4" );

    /**
     * <span style="background:#8b4513">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SaddleBrown <br>
     */
    public static final RGBA SaddleBrown = getColors( ).get( "SaddleBrown" );

    /**
     * <span style="background:#fa8072">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon <br>
     */
    public static final RGBA Salmon = getColors( ).get( "Salmon" );

    /**
     * <span style="background:#ff8c69">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon1 <br>
     */
    public static final RGBA Salmon1 = getColors( ).get( "Salmon1" );

    /**
     * <span style="background:#ee8262">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon2 <br>
     */
    public static final RGBA Salmon2 = getColors( ).get( "Salmon2" );

    /**
     * <span style="background:#cd7054">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon3 <br>
     */
    public static final RGBA Salmon3 = getColors( ).get( "Salmon3" );

    /**
     * <span style="background:#8b4c39">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Salmon4 <br>
     */
    public static final RGBA Salmon4 = getColors( ).get( "Salmon4" );

    /**
     * <span style="background:#f4a460">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SandyBrown <br>
     */
    public static final RGBA SandyBrown = getColors( ).get( "SandyBrown" );

    /**
     * <span style="background:#2e8b57">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen <br>
     */
    public static final RGBA SeaGreen = getColors( ).get( "SeaGreen" );

    /**
     * <span style="background:#54ff9f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen1 <br>
     */
    public static final RGBA SeaGreen1 = getColors( ).get( "SeaGreen1" );

    /**
     * <span style="background:#4eee94">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen2 <br>
     */
    public static final RGBA SeaGreen2 = getColors( ).get( "SeaGreen2" );

    /**
     * <span style="background:#43cd80">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SeaGreen3 <br>
     */
    public static final RGBA SeaGreen3 = getColors( ).get( "SeaGreen3" );

    /**
     * <span style="background:#fff5ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell <br>
     */
    public static final RGBA Seashell = getColors( ).get( "Seashell" );

    /**
     * <span style="background:#eee5de">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell2 <br>
     */
    public static final RGBA Seashell2 = getColors( ).get( "Seashell2" );

    /**
     * <span style="background:#cdc5bf">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell3 <br>
     */
    public static final RGBA Seashell3 = getColors( ).get( "Seashell3" );

    /**
     * <span style="background:#8b8682">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Seashell4 <br>
     */
    public static final RGBA Seashell4 = getColors( ).get( "Seashell4" );

    /**
     * <span style="background:#a0522d">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna <br>
     */
    public static final RGBA Sienna = getColors( ).get( "Sienna" );

    /**
     * <span style="background:#ff8247">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna1 <br>
     */
    public static final RGBA Sienna1 = getColors( ).get( "Sienna1" );

    /**
     * <span style="background:#ee7942">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna2 <br>
     */
    public static final RGBA Sienna2 = getColors( ).get( "Sienna2" );

    /**
     * <span style="background:#cd6839">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna3 <br>
     */
    public static final RGBA Sienna3 = getColors( ).get( "Sienna3" );

    /**
     * <span style="background:#8b4726">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Sienna4 <br>
     */
    public static final RGBA Sienna4 = getColors( ).get( "Sienna4" );

    /**
     * <span style="background:#87ceeb">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue <br>
     */
    public static final RGBA SkyBlue = getColors( ).get( "SkyBlue" );

    /**
     * <span style="background:#87ceff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue1 <br>
     */
    public static final RGBA SkyBlue1 = getColors( ).get( "SkyBlue1" );

    /**
     * <span style="background:#7ec0ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue2 <br>
     */
    public static final RGBA SkyBlue2 = getColors( ).get( "SkyBlue2" );

    /**
     * <span style="background:#6ca6cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue3 <br>
     */
    public static final RGBA SkyBlue3 = getColors( ).get( "SkyBlue3" );

    /**
     * <span style="background:#4a708b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SkyBlue4 <br>
     */
    public static final RGBA SkyBlue4 = getColors( ).get( "SkyBlue4" );

    /**
     * <span style="background:#6a5acd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue <br>
     */
    public static final RGBA SlateBlue = getColors( ).get( "SlateBlue" );

    /**
     * <span style="background:#836fff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue1 <br>
     */
    public static final RGBA SlateBlue1 = getColors( ).get( "SlateBlue1" );

    /**
     * <span style="background:#7a67ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue2 <br>
     */
    public static final RGBA SlateBlue2 = getColors( ).get( "SlateBlue2" );

    /**
     * <span style="background:#6959cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue3 <br>
     */
    public static final RGBA SlateBlue3 = getColors( ).get( "SlateBlue3" );

    /**
     * <span style="background:#473c8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateBlue4 <br>
     */
    public static final RGBA SlateBlue4 = getColors( ).get( "SlateBlue4" );

    /**
     * <span style="background:#708090">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray <br>
     */
    public static final RGBA SlateGray = getColors( ).get( "SlateGray" );

    /**
     * <span style="background:#c6e2ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray1 <br>
     */
    public static final RGBA SlateGray1 = getColors( ).get( "SlateGray1" );

    /**
     * <span style="background:#b9d3ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray2 <br>
     */
    public static final RGBA SlateGray2 = getColors( ).get( "SlateGray2" );

    /**
     * <span style="background:#9fb6cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray3 <br>
     */
    public static final RGBA SlateGray3 = getColors( ).get( "SlateGray3" );

    /**
     * <span style="background:#6c7b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SlateGray4 <br>
     */
    public static final RGBA SlateGray4 = getColors( ).get( "SlateGray4" );

    /**
     * <span style="background:#fffafa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow <br>
     */
    public static final RGBA Snow = getColors( ).get( "Snow" );

    /**
     * <span style="background:#eee9e9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow2 <br>
     */
    public static final RGBA Snow2 = getColors( ).get( "Snow2" );

    /**
     * <span style="background:#cdc9c9">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow3 <br>
     */
    public static final RGBA Snow3 = getColors( ).get( "Snow3" );

    /**
     * <span style="background:#8b8989">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Snow4 <br>
     */
    public static final RGBA Snow4 = getColors( ).get( "Snow4" );

    /**
     * <span style="background:#00ff7f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen <br>
     */
    public static final RGBA SpringGreen = getColors( ).get( "SpringGreen" );

    /**
     * <span style="background:#00ee76">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen2 <br>
     */
    public static final RGBA SpringGreen2 = getColors( ).get( "SpringGreen2" );

    /**
     * <span style="background:#00cd66">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen3 <br>
     */
    public static final RGBA SpringGreen3 = getColors( ).get( "SpringGreen3" );

    /**
     * <span style="background:#008b45">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SpringGreen4 <br>
     */
    public static final RGBA SpringGreen4 = getColors( ).get( "SpringGreen4" );

    /**
     * <span style="background:#4682b4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue <br>
     */
    public static final RGBA SteelBlue = getColors( ).get( "SteelBlue" );

    /**
     * <span style="background:#63b8ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue1 <br>
     */
    public static final RGBA SteelBlue1 = getColors( ).get( "SteelBlue1" );

    /**
     * <span style="background:#5cacee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue2 <br>
     */
    public static final RGBA SteelBlue2 = getColors( ).get( "SteelBlue2" );

    /**
     * <span style="background:#4f94cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue3 <br>
     */
    public static final RGBA SteelBlue3 = getColors( ).get( "SteelBlue3" );

    /**
     * <span style="background:#36648b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> SteelBlue4 <br>
     */
    public static final RGBA SteelBlue4 = getColors( ).get( "SteelBlue4" );

    /**
     * <span style="background:#d2b48c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan <br>
     */
    public static final RGBA Tan = getColors( ).get( "Tan" );

    /**
     * <span style="background:#ffa54f">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan1 <br>
     */
    public static final RGBA Tan1 = getColors( ).get( "Tan1" );

    /**
     * <span style="background:#ee9a49">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan2 <br>
     */
    public static final RGBA Tan2 = getColors( ).get( "Tan2" );

    /**
     * <span style="background:#8b5a2b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tan4 <br>
     */
    public static final RGBA Tan4 = getColors( ).get( "Tan4" );

    /**
     * <span style="background:#d8bfd8">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle <br>
     */
    public static final RGBA Thistle = getColors( ).get( "Thistle" );

    /**
     * <span style="background:#ffe1ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle1 <br>
     */
    public static final RGBA Thistle1 = getColors( ).get( "Thistle1" );

    /**
     * <span style="background:#eed2ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle2 <br>
     */
    public static final RGBA Thistle2 = getColors( ).get( "Thistle2" );

    /**
     * <span style="background:#cdb5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle3 <br>
     */
    public static final RGBA Thistle3 = getColors( ).get( "Thistle3" );

    /**
     * <span style="background:#8b7b8b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Thistle4 <br>
     */
    public static final RGBA Thistle4 = getColors( ).get( "Thistle4" );

    /**
     * <span style="background:#ff6347">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato <br>
     */
    public static final RGBA Tomato = getColors( ).get( "Tomato" );

    /**
     * <span style="background:#ee5c42">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato2 <br>
     */
    public static final RGBA Tomato2 = getColors( ).get( "Tomato2" );

    /**
     * <span style="background:#cd4f39">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato3 <br>
     */
    public static final RGBA Tomato3 = getColors( ).get( "Tomato3" );

    /**
     * <span style="background:#8b3626">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Tomato4 <br>
     */
    public static final RGBA Tomato4 = getColors( ).get( "Tomato4" );

    /**
     * <span style="background:#40e0d0">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise <br>
     */
    public static final RGBA Turquoise = getColors( ).get( "Turquoise" );

    /**
     * <span style="background:#00f5ff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise1 <br>
     */
    public static final RGBA Turquoise1 = getColors( ).get( "Turquoise1" );

    /**
     * <span style="background:#00e5ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise2 <br>
     */
    public static final RGBA Turquoise2 = getColors( ).get( "Turquoise2" );

    /**
     * <span style="background:#00c5cd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise3 <br>
     */
    public static final RGBA Turquoise3 = getColors( ).get( "Turquoise3" );

    /**
     * <span style="background:#00868b">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Turquoise4 <br>
     */
    public static final RGBA Turquoise4 = getColors( ).get( "Turquoise4" );

    /**
     * <span style="background:#ee82ee">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Violet <br>
     */
    public static final RGBA Violet = getColors( ).get( "Violet" );

    /**
     * <span style="background:#d02090">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed <br>
     */
    public static final RGBA VioletRed = getColors( ).get( "VioletRed" );

    /**
     * <span style="background:#ff3e96">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed1 <br>
     */
    public static final RGBA VioletRed1 = getColors( ).get( "VioletRed1" );

    /**
     * <span style="background:#ee3a8c">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed2 <br>
     */
    public static final RGBA VioletRed2 = getColors( ).get( "VioletRed2" );

    /**
     * <span style="background:#cd3278">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed3 <br>
     */
    public static final RGBA VioletRed3 = getColors( ).get( "VioletRed3" );

    /**
     * <span style="background:#8b2252">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> VioletRed4 <br>
     */
    public static final RGBA VioletRed4 = getColors( ).get( "VioletRed4" );

    /**
     * <span style="background:#f5deb3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat <br>
     */
    public static final RGBA Wheat = getColors( ).get( "Wheat" );

    /**
     * <span style="background:#ffe7ba">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat1 <br>
     */
    public static final RGBA Wheat1 = getColors( ).get( "Wheat1" );

    /**
     * <span style="background:#eed8ae">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat2 <br>
     */
    public static final RGBA Wheat2 = getColors( ).get( "Wheat2" );

    /**
     * <span style="background:#cdba96">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat3 <br>
     */
    public static final RGBA Wheat3 = getColors( ).get( "Wheat3" );

    /**
     * <span style="background:#8b7e66">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Wheat4 <br>
     */
    public static final RGBA Wheat4 = getColors( ).get( "Wheat4" );

    /**
     * <span style="background:#ffffff">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> White <br>
     */
    public static final RGBA White = getColors( ).get( "White" );

    /**
     * <span style="background:#f5f5f5">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> WhiteSmoke <br>
     */
    public static final RGBA WhiteSmoke = getColors( ).get( "WhiteSmoke" );

    /**
     * <span style="background:#ffff00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow <br>
     */
    public static final RGBA Yellow = getColors( ).get( "Yellow" );

    /**
     * <span style="background:#eeee00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow2 <br>
     */
    public static final RGBA Yellow2 = getColors( ).get( "Yellow2" );

    /**
     * <span style="background:#cdcd00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow3 <br>
     */
    public static final RGBA Yellow3 = getColors( ).get( "Yellow3" );

    /**
     * <span style="background:#8b8b00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> Yellow4 <br>
     */
    public static final RGBA Yellow4 = getColors( ).get( "Yellow4" );

    /**
     * <span style="background:#9acd32">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span> YellowGreen <br>
     */
    public static final RGBA YellowGreen = getColors( ).get( "YellowGreen" );
}
