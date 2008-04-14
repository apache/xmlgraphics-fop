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

package org.apache.fop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.util.io.Base64EncodeStream;

/**
 * Utility classes for generating RFC 2397 data URLs.
 */
public class DataURLUtil {

    /**
     * Creates a new data URL and returns it as a String.
     * @param in the InputStream to read the data from
     * @param mediatype the MIME type of the content, or null
     * @return the newly created data URL
     * @throws IOException if an I/O error occurs
     */
    public static String createDataURL(InputStream in, String mediatype) throws IOException {
        StringWriter writer = new StringWriter();
        writeDataURL(in, mediatype, writer);
        return writer.toString();
    }
    
    /**
     * Generates a data URL and writes it to a Writer.
     * @param in the InputStream to read the data from
     * @param mediatype the MIME type of the content, or null
     * @param writer the Writer to write to
     * @throws IOException if an I/O error occurs
     */
    public static void writeDataURL(InputStream in, String mediatype, Writer writer)
            throws IOException {
        writer.write("data:");
        if (mediatype != null) {
            writer.write(mediatype);
        }
        writer.write(";base64,");
        Base64EncodeStream out = new Base64EncodeStream(
                new WriterOutputStream(writer, "US-ASCII"));
        IOUtils.copy(in, out);
        out.flush();
    }
}
