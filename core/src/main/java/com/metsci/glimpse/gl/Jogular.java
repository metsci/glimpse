/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.gl;


import java.io.File;
import java.util.logging.Logger;

import com.metsci.glimpse.util.jnlu.NativeLibUtils;
import com.metsci.glimpse.util.logging.LoggerUtils;

import static com.metsci.glimpse.util.jnlu.FileUtils.*;
import static com.metsci.glimpse.util.jnlu.NativeLibUtils.*;


/**
 * A utility that allows JOGL to load its native libraries from classpath
 * resources. This way, the native libs can be kept in a jar, and managed
 * like normal dependencies (with Maven, e.g.). Also, java.library.path does
 * not need to be specified on the java command line.
 *
 * To use, call Jogular.initJogl() before any Glimpse or OpenGL classes are
 * loaded. It is safest to do this on the first line of main(). For an RCP
 * application, the safest place is in a static block in the activator class.
 *
 * Use of this class may cause the creation of temporary files and/or directories.
 *
 * @author hogye
 * 
 * Deprecated this class. It will compile but nativeLibResourceSearchPath() 
 * needs updating for particular jar one is using.
 * 
 * @author ttran17
 */
public class Jogular
{
	private static final Logger logger = LoggerUtils.getLogger(Jogular.class);
	
    public static final String jogularPlatformProperty = "jogular.platform";

    public static String joglPlatformString()
    {
        String override = System.getProperty(jogularPlatformProperty);
        if (override != null) return override;

        if (onPlatform("win", "amd64"))     return "windows-amd64";
        if (onPlatform("win", "x86_64"))    return "windows-amd64";
        if (onPlatform("win", "x86"))       return "windows-i586";

        if (onPlatform("linux", "amd64"))   return "linux-amd64";
        if (onPlatform("linux", "x86_64"))  return "linux-amd64";
        if (onPlatform("linux", "x86"))     return "linux-i586";
        if (onPlatform("linux", "i386"))    return "linux-i586";

        if (onPlatform("mac", "i386"))      return "macosx-universal";
        if (onPlatform("mac", "x86_64"))    return "macosx-universal";
        if (onPlatform("mac", "ppc"))       return "macosx-ppc";

        if (onPlatform("sunos", "amd64"))   return "solaris-amd64";
        if (onPlatform("sunos", "x86_64"))  return "solaris-amd64";
        if (onPlatform("sunos", "x86"))     return "solaris-i586";
        if (onPlatform("sunos", "sparc"))   return "solaris-sparc";
        if (onPlatform("sunos", "sparcv9")) return "solaris-sparcv9";

        throw new RuntimeException("Failed to auto-detect JOGL platform (os.name = " + System.getProperty("os.name") + ", os.arch = " + System.getProperty("os.arch") + ") -- specify manually with system property \"" + jogularPlatformProperty + "\"");
    }

    public static String nativeLibResourceSearchPath()
    {
    	// TODO: this resource path isn't going to work with jogl-all-platforms jar ...
        return "META-INF/lib/" + joglPlatformString();
    }

    public static class JogularLoaderAction implements com.jogamp.common.jvm.JNILibLoaderBase.LoaderAction
    {
        protected final String resourceSearchPath;
        protected File tempDir;

        public JogularLoaderAction(String resourceSearchPath)
        {
            this.resourceSearchPath = resourceSearchPath;
            this.tempDir = null;
        }

		@Override
		public void loadLibrary(String libName, String[] preloads, boolean preloadIgnoreError, ClassLoader cl) 
		{
            if (preloads != null & preloads.length > 0)
            {
                for (String preload : preloads)
                {
                    try
                    {
                        loadLibrary(preload, false, cl);
                    }
                    catch (UnsatisfiedLinkError e)
                    {
                        if (!preloadIgnoreError && e.getMessage().contains("already loaded")) throw e;
                    }
                }
            }

            loadLibrary(libName, false, cl);
        }

		@Override
		public boolean loadLibrary(String libName, boolean ignoreError, ClassLoader cl) 
		{
            try
            {
                if (tempDir == null) tempDir = createTempDir("jogular");
                NativeLibUtils.loadLibs(resourceSearchPath, tempDir, libName);
                return true;
            }
            catch (Exception e)
            {
            	try {
            		System.loadLibrary(libName);
            		return true;
            	} catch (Exception e2) {
            		if (!ignoreError) throw e2;
            	}
            }
            return false;
		}
    }

    /**
     * The gluegen native lib is only used by JOGL's X11 implementation.
     */
    public static boolean needGluegen()
    {
        String override = System.getProperty("opengl.factory.class.name");
        if (override != null) return override.equals("com.sun.opengl.impl.x11.X11GLDrawableFactory");

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("wind")) return false;
        if (osName.startsWith("mac os x")) return false;
        return true;
    }

	/**
	 * JOGL 2.0 claims:
	 * 
	 * JOGL 2.0 has a brand new feature allowing to automatically extract the proper native libraries 
	 * required to use JOGL from JARs containing them without relying on the Java library path or any 
	 * platform-dependent environment variable allowing to set the location of native libraries. 
	 * <p>
	 * This allows desktop applications as well as traditional Applets as traditional Applets to 
	 * utilize the native library JAR files the same way Webstart/JNLP does.
	 * <p>
	 * To allow the native JAR file library loading to work, ensure that all JogAmp JAR files are left 
	 * unmodified within their common directory. In case the native library JAR files cannot be opened, 
	 * it falls back to the traditional native library loading mechanism via the java library path. 
	 * <p>
	 * This feature is enabled by default and - for whatever reason - it can be disabled by setting 
	 * the property jogamp.gluegen.UseTempJarCache to false 
	 * (as a VM argument, -Djogamp.gluegen.UseTempJarCache=false in command line).
	 * 
	 * @author ttran17
	 */
    @Deprecated
    public static void initJogl()
    {
    	// JOGL 2.0 native lib loader is enabled by default
    	// To turn it off, set the System property as shown below ...
    	// System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
    	if ( com.jogamp.common.os.Platform.USE_TEMP_JAR_CACHE ) {
    		LoggerUtils.logWarning(logger, "Jogular is deprecated. Using JOGL 2.0 built-in native lib loader functionality.");
    		return;
    	}
    	LoggerUtils.logWarning(logger, "Jogular is deprecated. Next time, why not give JOGL 2.0 auto native lib loader a try?");
    	
        JogularLoaderAction loader = new JogularLoaderAction(nativeLibResourceSearchPath());

        // There is no mechanism for customizing how gluegen loads its native lib,
        // so we have to do it manually.
        //
        com.jogamp.common.jvm.JNILibLoaderBase.disableLoading();

        // Gluegen's native lib doesn't work under 64-bit Windows 7 (it depends on
        // msvcr80.dll). This version of gluegen is out of date, and unlikely to get
        // fixed.
        //
        // Fortunately, the gluegen native lib is only used by the X11 implementation
        // of JOGL, so we don't have to load it at all on Windows.
        //
        if (needGluegen()) loader.loadLibrary("gluegen-rt", false, null);

        // Use JOGL's LoaderAction mechanism to customize how JOGL's native
        // libs are loaded.
        //
        com.jogamp.common.jvm.JNILibLoaderBase.setLoadingAction(loader);
        
    }

}
