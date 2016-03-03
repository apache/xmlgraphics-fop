/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.bitmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This utility class helps renderers who generate one file per page,
 * like the PNG renderer.
 */
public class MultiFileRenderingUtil {

    /** The file syntax prefix, eg. "page" will output "page1.png" etc */
    private String filePrefix;

    private String fileExtension;

    /** The output directory where images are to be written */
    private File outputDir;

    /**
     * Creates a new instance.
     * <p>
     * The file name must not have an extension, or must have extension "png",
     * and its last period must not be at the start (empty file prefix).
     * @param ext the extension to be used
     * @param outputFile the output file or null if there's no such information
     */
    public MultiFileRenderingUtil(String ext, File outputFile) {
        this.fileExtension = ext;
        // the file provided on the command line
        if (outputFile == null) {
            //No filename information available. Only the first page will be rendered.
            outputDir = null;
            filePrefix = null;
        } else {
            outputDir = outputFile.getParentFile();

            // extracting file name syntax
            String s = outputFile.getName();
            int i = s.lastIndexOf(".");
            if (i > 0) {
                // Make sure that the file extension was "png"
                String extension = s.substring(i + 1).toLowerCase();
                if (!ext.equals(extension)) {
                    throw new IllegalArgumentException("Invalid file extension ('"
                                          + extension + "') specified");
                }
            } else if (i == -1) {
                i = s.length();
            } else { // i == 0
                throw new IllegalArgumentException("Invalid file name ('"
                                      + s + "') specified");
            }
            if (s.charAt(i - 1) == '1') {
                i--; // getting rid of the "1"
            }
            filePrefix = s.substring(0, i);
        }
    }

    /**
     * Creates a new {@link OutputStream} for the given page number.
     * @param pageNumber the page number (zero-based)
     * @return the output stream for the page
     * @throws IOException if there's an I/O error while setting up the output stream
     */
    public OutputStream createOutputStream(int pageNumber) throws IOException {
        if (filePrefix == null) {
            return null;
        } else {
            File f = new File(outputDir,
                    filePrefix + (pageNumber + 1) + "." + fileExtension);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            return os;
        }
    }

}
