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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.parser.ResourceLister.DefaultResourceFilter;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Command line utility to convert enc files into metron's own internal enc format.
 * To use as a stand alone command line utility, may be easiest to hard code values
 * for dataDir and topLevelOutputDir in the main main method.
 *
 * The output directry is a multi directory format.  Given a enc 000 data file like:
 * data/enc/atlantic-medium-enc/ENC_ROOT/US3NY01M/US3NY01M.000
 * The resulting metron enc file will be written to:
 * TOPLEVELOUTPUTDIR/atlantic-medium-enc/US3NY01M/US3NY01M_bin.txt
 * The parent directory of the ENC_ROOT directory will be the name of output subdirectory.
 * The root name (file name minus .000 extension) will the grandchild output directory.
 *
 * @author john
 */
public class ENCHarvest {
    private static Logger logger = Logger.getLogger(ENCHarvest.class.toString());

    private OGRToENCParser encConverter;
    private boolean clearDumpLocation;

    public ENCHarvest() throws IOException {
        encConverter = new OGRToENCParser();
        clearDumpLocation = true;
    }

    /**
     * Recursively searches for ENC_ROOT directories starting from dataHome.
     * For each ENC_ROOT directory found, will call convertEncFile to read the .000 file
     * and convert it to a metron friendly _bin.txt file.
     *
     * @param topLevelOutputDir
     * @param dataHome
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public boolean recursivelyConvertEncFiles(File topLevelOutputDir, String dataHome) throws IOException, URISyntaxException {
        List<String> encDirList = new ArrayList<String>();
        System.out.println("datahome: " +  dataHome);
        String[] resources = ResourceLister.getRecursiveResourceListing(ENCHarvest.class, dataHome, new DefaultResourceFilter(true, false) {
            @Override
            public boolean acceptFileResource(String name) {
                return name.endsWith(".000");
            }
        });


        if (resources != null) {
            boolean isZip = new File(dataHome).isFile()
                    && dataHome.length() > 4
                    && dataHome.substring(dataHome.length()-4).toLowerCase().equals(".zip");

            for(String enc000File : resources) {
                if (isZip)
                    convertEncFile(topLevelOutputDir, dataHome, enc000File);
                else
                    convertEncFile(topLevelOutputDir, enc000File);
            }
            return true;
        }
        return false;
    }

    /**
     * Given an ENC_ROOT directory, will search for .000 files in the child directories
     * off the ENC_ROOT directory.  Will call gdal to parse the .000 file and create a
     * new metsci friendly _bin.txt file.
     *
     * @param topLevelOutputDir
     * @param encRootResource
     * @throws IOException
     * @throws URISyntaxException
     */
    public void convertEncFile(File topLevelOutputDir, String enc000Path) throws IOException, URISyntaxException {
        convertEncFile(topLevelOutputDir, null, enc000Path);
    }

    public void convertEncFile(File topLevelOutputDir, String zipPath, String enc000Path) throws IOException, URISyntaxException {
        String encRootResource = null; // todo remove?
        logger.fine("Started ENC Repository Parsing: " + (new Date(System.currentTimeMillis())).toString());

        String encDir = "."; // dir of .000 file
        String name = enc000Path; // name of .000 file
        int lastSeparator = enc000Path.lastIndexOf("/");
        if (lastSeparator > 0) {
            encDir = enc000Path.substring(0, lastSeparator);
            name = enc000Path.substring(lastSeparator+1);
        }
        String encTitle = name.substring(0, name.length() - 4);

        File dataLevelOutputDir = new File(topLevelOutputDir, "/" + encTitle);
        System.out.println("outputdir: " + dataLevelOutputDir);
        prepareOutputDir(dataLevelOutputDir, clearDumpLocation);
        OGRInfo ogrInfo = new OGRInfo(new File(dataLevelOutputDir, encTitle + ".txt"));
        try {
            List<GenericObject> geoObjects = null;
            if (zipPath != null)
                geoObjects = ogrInfo.addZippedGeoSourceAndParse(zipPath, enc000Path);
            else
                geoObjects = ogrInfo.addGeoSourceAndParse(enc000Path);
            List<ENCObject> encObjects = new LinkedList<ENCObject>();
            encConverter.parseENC(encDir, geoObjects, encObjects);
            File metsciOutFile = new File(dataLevelOutputDir, encTitle + ".enc");
            System.out.println("outputFile: " + metsciOutFile);
            DataOutputStream metsciOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metsciOutFile)));
            writeENCObjectsToFile(metsciOutStream, encObjects);
            metsciOutStream.close();
        } catch (InterruptedException ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }
    public void goodconvertEncFile(File topLevelOutputDir, String encRootResource) throws IOException, URISyntaxException {
        logger.fine("Started ENC Repository Parsing: " + (new Date(System.currentTimeMillis())).toString());
        String [] encRootSubDirs = ResourceLister.getResourceListing(OGRInfo.class, encRootResource, new DefaultResourceFilter(true, false));
        for(int i = 0; i < encRootSubDirs.length; i++) {
            String encRootSubDirPath = encRootResource + '/' + encRootSubDirs[i]; // dir of .000 file
            String [] enc000Files = ResourceLister.getResourceListing(OGRInfo.class, encRootSubDirPath, new DefaultResourceFilter(false, true) {
                @Override
                public boolean acceptFileResource(String name) {
                    return name.endsWith("000");
                }
            });

            String encSubDirPath = encRootResource + "/" + encRootSubDirs[i];
            for (int j = 0; j < enc000Files.length; j++) {
                System.out.println(enc000Files[j]);
                if (1 == 1) continue;
                String encTitle = enc000Files[j].substring(0, enc000Files[j].length()-4);
                File dataLevelOutputDir = new File(topLevelOutputDir, encRootSubDirs[i] + "/" + encTitle);
                prepareOutputDir(dataLevelOutputDir, clearDumpLocation);
                String enc000Path = encSubDirPath + "/" + enc000Files[j];
                OGRInfo ogrInfo = new OGRInfo(new File(dataLevelOutputDir, encTitle + ".txt"));
                try {
                    List<GenericObject> geoObjects = ogrInfo.addGeoSourceAndParse(enc000Path);
                    List<ENCObject> encObjects = new LinkedList<ENCObject>();
                    encConverter.parseENC(encSubDirPath, geoObjects, encObjects);
                    File metsciOutFile = new File(dataLevelOutputDir, encTitle + "_bin.txt");
                    DataOutputStream metsciOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metsciOutFile)));
                    writeENCObjectsToFile(metsciOutStream, encObjects);
                    metsciOutStream.close();
                } catch (InterruptedException ex) {
                    logger.log(Level.WARNING, null, ex);
                }
            }
        }
    }

    private void writeENCObjectsToFile(DataOutputStream stream, List<ENCObject> encObjects) throws IOException {
        for (ENCObject enc : encObjects) {
            ENCObject.write(stream, enc);
        }
    }

    /**
     * Given a output dir, will create it if it deson't exist.  Will clear it if it is not
     * empty and clearLocation parameter is true.
     *
     * @throws IOException if unable to find or create the given output dir
     */
    private void prepareOutputDir(File outputDir, boolean clearLocation) throws IOException {
        boolean dirAlreadyExists = outputDir.exists();
        if (!dirAlreadyExists)
            outputDir.mkdirs();
        if(! outputDir.exists())
            throw new IOException("Output folder doesn't exist and can't be created: " + outputDir.getPath());
        if(clearLocation && dirAlreadyExists){
            File[] _rmContents = outputDir.listFiles();
            for(int i = 0; i < _rmContents.length; i++){
                if(!_rmContents[i].isDirectory())
                    _rmContents[i].delete();
            }
        }
    }


    public static void main(String[] args) {
        String dataDir = null;
        if (args.length == 0) {
            System.out.println("USAGE: java " + ENCHarvest.class.toString() + " dir");
            System.exit(1);
        }
        else {
            dataDir = args[0];
        }

        System.out.println("starting from " +dataDir);
        File topLevelOutputDir = new File("output");
        System.out.println("output: " + topLevelOutputDir.getAbsolutePath());
        try {
            ENCHarvest harvest = new ENCHarvest();
            boolean success = harvest.recursivelyConvertEncFiles(topLevelOutputDir, dataDir);
            if (! success) {
                logger.severe("No resources to convert");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

}
