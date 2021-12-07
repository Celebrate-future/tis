/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.indexbuilder.utils;

import com.qlangtech.tis.indexbuilder.IndexBuilderTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServiceClassLoader extends URLClassLoader {

    public static final Logger logger = LoggerFactory.getLogger(IndexBuilderTask.class);

    /**
     * @param array
     * @param parent
     */
    public ServiceClassLoader(URL[] array, ClassLoader parent) {
        super(array, parent);
    }

    // TODO Auto-generated constructor stub
    public static ServiceClassLoader loadJar(String jarFullName, String dest, ClassLoader parent) throws IOException {
        /*
		 * JarFile jarFile; try { jarFile = new JarFile(fileName); }
		 * catch(IOException io) { throw new
		 * IOException("Error opening job jar: " + fileName) .initCause(io); }
		 */
        File jarFile = new File(jarFullName);
        ArrayList<URL> classPath = new ArrayList<URL>();
        if (!jarFile.isDirectory()) {
            File tmpDir = new File(dest + File.separator + "unjar");
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            /*
			 * if (!tmpDir.isDirectory()) {
			 * System.err.println("Mkdirs failed to create " + tmpDir);
			 * System.exit(-1); }
			 */
            /*
			 * final File workDir = File.createTempFile("hadoop-unjar", "", tmpDir);
			 * workDir.delete(); workDir.mkdirs(); if (!workDir.isDirectory()) {
			 * System.err.println("Mkdirs failed to create " + workDir);
			 * System.exit(-1); }
			 */
            /*
			 * Runtime.getRuntime().addShutdownHook(new Thread() { public void run()
			 * { try { FileUtil.fullyDelete(workDir); } catch (IOException e) { } }
			 * });
			 */
            unJar(jarFile, tmpDir);
            classPath.add(new File(tmpDir + "/").toURL());
            classPath.add(jarFile.toURL());
            classPath.add(new File(tmpDir, "classes/").toURL());
            File[] libs = new File(tmpDir, "lib").listFiles();
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURL());
                }
            }
            logger.warn("user_classPath:" + classPath);
            // if (loader == null) {
            return new ServiceClassLoader(classPath.toArray(new URL[0]), parent);
        // }
        } else {
            classPath.add(new File(jarFile + "/").toURL());
            classPath.add(jarFile.toURL());
            // classPath.add(new File(jarFile, "classes/").toURL());
            File[] libs = jarFile.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    // TODO Auto-generated method stub
                    return !name.endsWith(".xml");
                }
            });
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURL());
                }
            }
            libs = jarFile.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    // TODO Auto-generated method stub
                    return name.endsWith(".xml");
                }
            });
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURL());
                }
            }
            logger.warn("user_classPath:" + classPath);
            // if (loader == null) {
            return new ServiceClassLoader(classPath.toArray(new URL[0]), parent);
        // }
        }
    }

    /**
     * Unpack a jar file into a directory.
     */
    public static void unJar(File jarFile, File toDir) throws IOException {
        JarFile jar = new JarFile(jarFile);
        try {
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream in = jar.getInputStream(entry);
                    try {
                        File file = new File(toDir, entry.getName());
                        if (!file.getParentFile().mkdirs()) {
                            if (!file.getParentFile().isDirectory()) {
                                throw new IOException("Mkdirs failed to create " + file.getParentFile().toString());
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        try {
                            byte[] buffer = new byte[8192];
                            int i;
                            while ((i = in.read(buffer)) != -1) {
                                out.write(buffer, 0, i);
                            }
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            jar.close();
        }
    }
}
