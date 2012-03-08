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

import com.metsci.glimpse.charts.vector.iteration.DNCComparatorByFeatureName;
import com.metsci.glimpse.charts.vector.parser.DNCHarvest.LibraryType;
import com.metsci.glimpse.charts.vector.parser.ResourceLister.DefaultResourceFilter;
import com.metsci.glimpse.charts.vector.parser.ResourceLister.ResourceFilter;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;


/**
 * Command line utility to convert dnc files into metron's own internal dnc format.
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
public class DNCHarvest {
    private static Logger logger = Logger.getLogger(DNCHarvest.class.toString());
    private static String consoleLogSuffix = "-console.txt";

    private static LibraryType [] allLibraryTypes = LibraryType.values();

    public enum LibraryType {
        Harbor("h", 7),
        Coastal("coa", 3),
        General("gen", 2, 3),
        Approach("a", 7);

        private String prefix;
        private int nonPrefixMinLength;
        private int nonPrefixMaxLength;

        private LibraryType(String prefix, int nonprefixLength) {
            this(prefix, nonprefixLength, nonprefixLength);
        }

        private LibraryType(String prefix, int nonPrefixMinLength, int nonPrefixMaxLength) {
            this.prefix = prefix;
            this.nonPrefixMinLength = nonPrefixMinLength;
            this.nonPrefixMaxLength = nonPrefixMaxLength;
        }

        /**
         * Determines whether a given directory is a directory of this library type
         *
         * @param candidate
         * @return true/false
         */
        public boolean qualify(String candidate) {
            if (candidate.isEmpty()) {
                return false;
            }

            int candidateNonprefixLength = candidate.length() - prefix.length();
            if (candidateNonprefixLength < nonPrefixMinLength || candidateNonprefixLength > nonPrefixMaxLength) {
                return false;
            }

            String candidatePrefix = candidate.substring(0, prefix.length());
            if (!candidatePrefix.equalsIgnoreCase(prefix)) {
                return false;
            }

            return true;
        }
    };

    private OGRToDNCParser dncConverter;
    private boolean clearDumpLocation = false;

    public DNCHarvest() throws IOException {
        dncConverter = new OGRToDNCParser();
    }


    public boolean recursivelyConvertDncFiles(File topLevelOutputDir, String dataHome) throws IOException, URISyntaxException, InterruptedException {
        System.out.println("dataHome: " + dataHome);
        // search for the 'lat' file.  Nothing special about it, but it is located at a dnc database level.  If we found it, we have
        // found a dnc database to parse.
        String[] resources = ResourceLister.getRecursiveResourceListing(DNCHarvest.class, dataHome, new DefaultResourceFilter(false, true) {
            @Override
            public boolean acceptFileResource(String name) {
                return name.endsWith("lat");
            }
        });

        if (resources != null) {
            for(String latFile : resources) {
                int slashIndex = latFile.lastIndexOf("/");
                if (slashIndex == -1)
                    continue;
                String dncDir = latFile.substring(0, slashIndex);
                convertDncDB(topLevelOutputDir, dncDir);
            }
            return true;
        }
        return false;
    }

    /**
     * Given an dnc db directory, will parse it and create a metron dnc formatted file.
     */
    public void convertDncDB(File topLevelOutputDir, String dncRootResource) throws IOException, URISyntaxException, InterruptedException {
        convertDncFile(topLevelOutputDir, dncRootResource, LibraryType.values());
    }

    public void convertDncFile(File topLevelOutputDir, String dncRootResource, LibraryType ... libraryTypes) throws IOException, URISyntaxException, InterruptedException {
        //System.out.println("topLevelOutputDir: " + topLevelOutputDir);
        //%topLevelOutputDir: output
        //System.out.println("dncRootResource: " + dncRootResource);
        //%dncRootResource: /home/john/Downloads/dnc/dnc10
        int slashIndex = dncRootResource.lastIndexOf("/");
        if (slashIndex == -1) {
            return;
        }
        String outputSubDirName = dncRootResource.substring(slashIndex+1);
        //System.out.println("outputSubDirName: " + outputSubDirName);
        //%outputSubDirName: dnc10
        File dataLevelOutputDir = new File(topLevelOutputDir, outputSubDirName);
        //%output/dnc10
        prepareOutputDir(dataLevelOutputDir, clearDumpLocation);

        String outputFileRootName = createDNCOutputFileRootName( outputSubDirName, libraryTypes );
        File gdalDumpFile = new File(dataLevelOutputDir, outputFileRootName + consoleLogSuffix);
        System.out.println("writing to " + gdalDumpFile.getAbsolutePath());
        //gdalDumpFile.deleteOnExit();
        OGRInfo ogrInfo = new OGRInfo(gdalDumpFile);

        //logger.fine("Started DNC Repository Parsing: " + (new Date(System.currentTimeMillis())).toString());
        ResourceFilter filter = new LibraryResourceFilter(libraryTypes);
        String [] dncRootSubDirs = ResourceLister.getResourceListing(OGRInfo.class, dncRootResource, filter);
        Set<String> filesDumpedSet = new HashSet<String>(dncRootSubDirs.length);
        for(int i = 0; i < dncRootSubDirs.length; i++) {
            //ogrinfo -al -ro gltp:/vrf/home/john/Downloads/dnc/dnc10/H1048590
            String dncRootSubDirPath = dncRootResource + '/' + dncRootSubDirs[i]; // dir of .000 file

            File dncRootSubDirFile = new File(dncRootSubDirPath);
            if (dncRootSubDirFile.exists()) {
                // A DNC distribution can have two links per coverage directory.  The real directory in lowercase and
                // a symbolic link directory in upper case that points to the real directory.  We only want to process
                // one of those links otherwise we would end up processing the same directory twice.  The java File
                // class's canonical path will translate a symbolic path to the actual real path.  We'll keep track
                // of the real paths processed, whether processed through real path and symbolic link path, to ensure
                // that we don't process the same directory twice.
                String dncRootSubDirCanonicalPath = dncRootSubDirFile.getCanonicalPath();
                boolean newToProcess = filesDumpedSet.add(dncRootSubDirCanonicalPath);
                if (! newToProcess) {
                    //System.out.println("Skipping " + dncRootSubDirPath + " as it was already processed.");
                    continue;
                }
            }
            //System.out.println("dncRootSubDirPath: " + dncRootSubDirPath);
            //dncRootSubDirPath: /home/john/Downloads/dnc/dnc10/a1048282

            ogrInfo.addDNCGeoSource(dncRootSubDirPath);
        }

        //ogrInfo.closeOutputWriter();
        List<GenericObject> obList = ogrInfo.parse();
        List<DNCObject> dncObjects = new ArrayList<DNCObject>(obList.size());
        dncConverter.parseDNC(null, obList, dncObjects);


        File metsciOutFile = new File(dataLevelOutputDir, outputFileRootName + ".dnc");
        DataOutputStream metsciOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metsciOutFile)));
        writeDNCObjectsToFile(metsciOutStream, dncObjects);
        metsciOutStream.close();
    }

    public void createDNCFileFromOGRInfoConsoleFile(File gdalConsoleFile) throws IOException {
        //"dnc10-harbor-console.txt"
        String consoleLogName = gdalConsoleFile.getName();
        String dncFileName = null;
        if (consoleLogName.endsWith(consoleLogSuffix)) {
            //"dnc10-harbor.dnc"
            dncFileName = consoleLogName.substring(0, consoleLogName.length() - consoleLogSuffix.length()) + ".dnc";
        } else {
            // user not following convention, gets the whatever name we choose
            dncFileName = gdalConsoleFile + ".dnc";
        }

        File metsciOutFile = new File(gdalConsoleFile.getParentFile(), dncFileName);
        createDNCFileFromOGRInfoConsoleFile(gdalConsoleFile, metsciOutFile);
    }

    public void createDNCFileFromOGRInfoConsoleFile(File gdalConsoleFile, File metsciOutFile) throws IOException {
        List<GenericObject> genericObjects = OGRReader.read(gdalConsoleFile);
        List<DNCObject> dncObjects = new ArrayList<DNCObject>(genericObjects.size());
        dncConverter.parseDNC(null, genericObjects, dncObjects);

        //File metsciOutFile = new File(dataLevelOutputDir, outputFileRootName + ".dnc");
        DataOutputStream metsciOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metsciOutFile)));
        writeDNCObjectsToFile(metsciOutStream, dncObjects);
        metsciOutStream.close();
    }


    /**
     * This is a method not part of the normal havest process. This methods will create a new metron formatted dnc file from an already
     * existing ogrinfo console file
     *
     * @param outputSubDirName
     * @param libraryTypes
     * @return
     */
    private String createDNCOutputFileRootName( String outputSubDirName, LibraryType [] libraryTypes ) {
        StringBuilder sb = new StringBuilder( );

        Set<LibraryType> librariesIncluded = EnumSet.<LibraryType>noneOf(LibraryType.class);
        for (LibraryType type : libraryTypes) {
            boolean added = librariesIncluded.add(type);
            if (added) {
                sb.append("-");
                sb.append(type.name().toLowerCase());
            }
        }

        // if all library types included
        if (librariesIncluded.size() == allLibraryTypes.length) {
            return outputSubDirName + "-all";
        } else {
            return outputSubDirName + sb.toString();
        }
    }

    private void writeDNCObjectsToFile(DataOutputStream stream, List<DNCObject> dncObjects) throws IOException {
        Collections.sort(dncObjects, new DNCComparatorByFeatureName() );
        for (DNCObject DNC : dncObjects) {
            DNCObject.write(stream, DNC);
        }
    }

    /**
     * Given a output dir, will create it if it doesn't exist.  Will clear it if it is not
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


    static class LibraryResourceFilter extends DefaultResourceFilter implements ResourceFilter {
        private LibraryType [] libraries;

        public LibraryResourceFilter(LibraryType ... libraries) {
            super(true, false);

            this.libraries = libraries;
        }

        @Override
        public boolean acceptDirResource(String name) {

            for (LibraryType libraryType : libraries) {
                if (libraryType.qualify(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void specialBatch() {
        String topLevelDataDir = "/home/john/Downloads/dnc/";

        System.out.println("starting from " + topLevelDataDir);
        File topLevelOutputDir = new File("output");
        System.out.println("output: " + topLevelOutputDir.getAbsolutePath());

        for (int i = 9; i < 12; i++) {
            if (i == 10)
                continue;
            String dataDir = String.format("%sdnc%02d", topLevelDataDir, i);
            for (LibraryType libraryType : LibraryType.values()) {
                try {
                    System.out.println("==> working on " + libraryType + " @" + dataDir);
                    DNCHarvest harvest = new DNCHarvest();
                    harvest.convertDncFile(topLevelOutputDir, dataDir, libraryType);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        }
    }
    public static void main(String[] args) {
        if (1==0) {
            specialBatch();
            return;
        }
        String dataDir = null;
        if (args.length == 0) {
            //System.out.println("USAGE: java " + ENCHarvest.class.toString() + " dir");
            //System.exit(1);
            dataDir = "/home/john/Downloads/dnc/dnc10";
            //dataDir = "/home/john/Downloads/dnc/DNC16";
        }
        else {
            dataDir = args[0];
        }

        System.out.println("starting from " +dataDir);
        File topLevelOutputDir = new File("output");
        System.out.println("output: " + topLevelOutputDir.getAbsolutePath());
        try {
            DNCHarvest harvest = new DNCHarvest();
//			boolean success = harvest.recursivelyConvertDncFiles(topLevelOutputDir, dataDir);
//			if (! success) {
//				logger.severe("No resources to convert");
//			}
            harvest.convertDncFile(topLevelOutputDir, dataDir, LibraryType.Harbor);
            //File file = new File("output/dnc09/dnc09-harbor-console.txt");
            //System.out.println(file.exists());
            //harvest.createDNCFileFromOGRInfoConsoleFile(file);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
}
