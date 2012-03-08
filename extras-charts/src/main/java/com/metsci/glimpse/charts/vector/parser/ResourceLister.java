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

/*
 * Original source © 2002-2009 Greg Briggs
 * (http://www.uofr.net/~greg/java/get-resource-listing.html)
 */
package com.metsci.glimpse.charts.vector.parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Copied from http://www.uofr.net/~greg/java/get-resource-listing.html and then modified
 * - fixed bugs
 * - added recursive search
 * - added filtering
 *
 * @author john
 */
public class ResourceLister {

    public interface ResourceFilter {
        boolean acceptFileResource(String name);
        boolean acceptDirResource(String name);
    }

    public static class DefaultResourceFilter implements ResourceFilter {
        private boolean acceptAllDirs;
        private boolean acceptAllFiles;

        public DefaultResourceFilter(boolean acceptDirs, boolean acceptFiles) {
            this.acceptAllDirs = acceptDirs;
            this.acceptAllFiles = acceptFiles;
        }

        @Override
        public boolean acceptFileResource(String name) {
            return acceptAllFiles;
        }

        @Override
        public boolean acceptDirResource(String name) {
            return acceptAllDirs;
        }
    }

    public static class FilenameFilterAdapter implements FilenameFilter {
        private ResourceFilter innerFilter;

        public FilenameFilterAdapter(ResourceFilter filter) {
            this.innerFilter = filter;
        }

        @Override
        public boolean accept(File dir, String name) {
            File file = new File(dir, name);
            if (file.isDirectory())
                return innerFilter.acceptDirResource(name);
            else
                return innerFilter.acceptFileResource(name);
        }
    }


    public static String[] getResourceListing(String path) throws URISyntaxException, IOException {
        return getResourceListing(ResourceLister.class, path, new DefaultResourceFilter(true, true));
    }

    public static String[] getResourceListing(String path, ResourceFilter filter) throws URISyntaxException, IOException {
        return getResourceListing(ResourceLister.class, path, filter);
    }

    /**
     *
     * @param clazz determines which classloader to use to find resource
     * @param path resource name
     * @return Array of resources if successful, array might be 0.  Null if it could not find resource
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        return getResourceListing(clazz, path, new DefaultResourceFilter(true, true));
    }

    public static String[] getResourceListing(Class clazz, String path, ResourceFilter filter) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            return new File(dirURL.toURI()).list(new FilenameFilterAdapter(filter));
        }

        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return file.list(new FilenameFilterAdapter(filter));
        }

        if (dirURL == null) {
            /* In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String fullJarPathName = jarEntry.getName();
                if (fullJarPathName.startsWith(path)) { //filter according to the path
                    String name = fullJarPathName.substring(path.length()+1);
                    if (name.isEmpty())
                        continue;
                    int checkSubdir = name.indexOf("/");
                    if (checkSubdir >= 0) {
                        // Ignore files in subdirectories.  If there are two or more slashes,
                        // it is a subdirectory.
                        if (name.lastIndexOf('/') != checkSubdir) {
                            continue;
                        }
                        name = name.substring(0, checkSubdir);
                    }

                    if (jarEntry.isDirectory()) {
                        if (filter.acceptDirResource(name)) {
                            result.add(name);
                        }
                    } else {
                        if (filter.acceptFileResource(name)) {
                            result.add(name);
                        }
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }

        return null;
    }

    private static List<String> walkFile(File dir, ResourceFilter filter, List<String> foundList) {
        File [] files = dir.listFiles();
        for (File file : files) {
            String filePath = file.getPath();
            if (file.isDirectory()) {
                if (filter.acceptDirResource(filePath))
                    foundList.add(filePath);
                walkFile(file, filter, foundList);
            } else {
                if (filter.acceptFileResource(filePath))
                    foundList.add(filePath);
            }
        }
        return foundList;
    }

    public static String[] getRecursiveResourceListing(String path) throws URISyntaxException, IOException {
        return getRecursiveResourceListing(ResourceLister.class, path);
    }

    public static String[] getRecursiveResourceListing(String path, ResourceFilter filter) throws URISyntaxException, IOException {
        return getRecursiveResourceListing(ResourceLister.class, path, filter);
    }

    public static String[] getRecursiveResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        return getRecursiveResourceListing(clazz, path, new DefaultResourceFilter(true, true));
    }

    public static String[] getRecursiveResourceListing(Class clazz, String path, ResourceFilter filter) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            List<String> fileList = walkFile(new File(dirURL.toURI()), filter, new ArrayList<String>());
            return fileList.toArray(new String[fileList.size()]);
        }

        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                List<String> fileList = walkFile(file, filter, new ArrayList<String>());
                return fileList.toArray(new String[fileList.size()]);
            } else if (file.getName().toLowerCase().endsWith(".zip")) {
                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries(); //gives ALL entries in jar
                Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    //if (name.startsWith(path)) { //filter according to the path
                        if (entry.isDirectory()) {
                            if (filter.acceptDirResource(name)) {
                                result.add(name);
                            }
                        } else {
                            if (filter.acceptFileResource(name)) {
                                result.add(name);
                            }
                        }
                    //}
                }
                return result.toArray(new String[result.size()]);
            }
        }

        if (dirURL == null) {
            /* In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(path)) { //filter according to the path
                    if (entry.isDirectory()) {
                        if (filter.acceptDirResource(name)) {
                            result.add(name);
                        }
                    } else {
                        if (filter.acceptFileResource(name)) {
                            result.add(name);
                        }
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }

        return null;
    }

    public static void main(String[] args) {
        try {
            String start = "data";
            if (args.length > 0)
                start = args[0];
            System.out.println("start: " + start);
            String[] resources = getResourceListing(ResourceLister.class, start);
            System.out.println("resources: " + Arrays.toString(resources));
        } catch (Throwable t) {
            t.printStackTrace();
            //logger.log(Level.SEVERE, "", t);
        }
    }
}
