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
package com.metsci.glimpse.charts.vector.parser;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;


/**
 * This class dumps s57 features to a file using the gdal command line utility
 * 'ogrinfo'.
 *
 * Ubuntu users, you add ubuntu gis stable package to your source list.  The following page has all the goodness.
 * https://launchpad.net/~ubuntugis/+archive/ppa
 * deb http://ppa.launchpad.net/ubuntugis/ppa/ubuntu lucid main
 * deb-src http://ppa.launchpad.net/ubuntugis/ppa/ubuntu lucid main
 *
 * @author Cunningham
 * @author Boquet
 */
public class OGRInfo { 
	
    private static Logger logger = Logger.getLogger(OGRInfo.class.toString());
    private static final String s57parserLocation = "/usr/bin/ogrinfo";

    private File gdalConsoleFile;
    private BufferedReader gdalConsoleOutputReader;
    private BufferedWriter gdalConsoleOutputWriter;


    public OGRInfo() throws IOException  {
        this(File.createTempFile("ogrinfoconsolelog", "txt"));
        gdalConsoleFile.deleteOnExit();
    }

    public OGRInfo(File gdalConsoleFile) {
        this.gdalConsoleFile = gdalConsoleFile;
    }


    public List<GenericObject> addGeoSourceAndParse(String geoSourceFile) throws IOException, InterruptedException {
        addGeoSource(geoSourceFile);
        return parse();
    }

    public List<GenericObject> addZippedGeoSourceAndParse(String zipFilePath, String geoLogicalPath) throws IOException, InterruptedException {
        addZippedGeoSource(zipFilePath, geoLogicalPath);
        return parse();
    }


    public void addGeoSource(String geoSourceFile) throws IOException, InterruptedException {
        internalAddGeoSource(geoSourceFile);
    }

    public void addZippedGeoSource(String zipFilePath, String geoLogicalPath) throws IOException, InterruptedException {
        // invoking ogrinfo on a zipped file requires a special protocol when specifying datasource
        String geoSourceFile = "/vsizip/" + zipFilePath + "/" + geoLogicalPath;
        internalAddGeoSource(geoSourceFile);
    }

    public void addDNCGeoSource(String geoSourceFile) throws IOException, InterruptedException {
        // invoking ogrinfo on a dnc file requires a special protocol when specifying datasource
        geoSourceFile = "gltp:/vrf/" + geoSourceFile;
        internalAddGeoSource(geoSourceFile);
    }

    /**
     *
     * @param geoSourceFile enc .000 file or dnc directory
     * @param isDNC true if DNC, false otherwise
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean internalAddGeoSource(String geoSourceFile) throws IOException, InterruptedException {
        // Runs the gdal ogrinfo command and captures the output to .txt file
        if (gdalConsoleOutputWriter == null) {
            gdalConsoleOutputWriter = new BufferedWriter(new FileWriter(gdalConsoleFile));
        }

        String command = s57parserLocation + " -ro -al  " + geoSourceFile;
        logger.info(">" + command);
        //gdalConsoleOutputWriter.write("#" + _command + "\n");
        Process exec = Runtime.getRuntime().exec(command);

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String line = inputReader.readLine();
        while (line  != null) {
            gdalConsoleOutputWriter.write(line + "\n");
            line = inputReader.readLine();
        }
        inputReader.close();
        exec.waitFor();

        if (exec.exitValue() == 139) {
            //System.out.println("### ogrinfo crashed   on file " + geoSourceFile + ", rval: " + exec.exitValue());
            return false;
        } else {
            //System.out.println("!!! ogrinfo succeeded on file " + geoSourceFile + ", rval: " + exec.exitValue());
            return true;
        }
    }


    public void closeOutputWriter() throws IOException {
        if (gdalConsoleOutputWriter != null) {
            gdalConsoleOutputWriter.flush();
            gdalConsoleOutputWriter.close();
        }
    }

    public List<GenericObject> parse() throws IOException  {
        closeOutputWriter();
        if (gdalConsoleOutputReader == null)
            gdalConsoleOutputReader = new BufferedReader(new FileReader(gdalConsoleFile));
        return OGRReader.read(gdalConsoleOutputReader);
    }
}
