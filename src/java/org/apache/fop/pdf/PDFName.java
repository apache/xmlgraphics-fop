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

package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.io.output.CountingOutputStream;

/**
 * Class representing a PDF name object.
 */
public class PDFName extends PDFObject {

    private String name;

    /**
     * Creates a new PDF name object.
     * @param name the name value
     */
    public PDFName(String name) {
        super();
        this.name = escapeName(name);
    }


    /**
     * Escapes a PDF name. It adds the leading slash and escapes characters as necessary.
     * @param name the name
     * @return the escaped name
     */
    static String escapeName(String name) {
        StringBuffer sb = new StringBuffer(Math.min(16, name.length() + 4));
        boolean skipFirst = false;
        sb.append('/');
        if (name.startsWith("/")) {
            skipFirst = true;
            skipFirst = false;
        }
        for (int i = (skipFirst ? 1 : 0), c = name.length(); i < c; i++) {
            char ch = name.charAt(i);
            if (ch < 33 || ch > 126 || ch == 0x2F) {
                sb.append('#');
                toHex(ch, sb);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static final char[] DIGITS
        = {'0', '1', '2', '3', '4', '5', '6', '7',
           '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static void toHex(char ch, StringBuffer sb) {
        if (ch >= 256) {
            throw new IllegalArgumentException(
                    "Only 8-bit characters allowed by this implementation");
        }
        sb.append(DIGITS[ch >>> 4 & 0x0F]);
        sb.append(DIGITS[ch & 0x0F]);
    }

    /** {@inheritDoc} */
    public String toString() {
        return this.name;
    }

    /**
     * Returns the name without the leading slash.
     * @return the name without the leading slash
     */
    public String getName() {
        return this.name.substring(1);
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (!(obj instanceof PDFName)) {
            return false;
        }
        PDFName other = (PDFName)obj;
        return this.name.equals(other.name);
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return name.hashCode();
    }


    /** {@inheritDoc} */
    protected int output(OutputStream stream) throws IOException {
        CountingOutputStream cout = new CountingOutputStream(stream);
        Writer writer = PDFDocument.getWriterFor(cout);
        if (hasObjectNumber()) {
            writer.write(getObjectID());
        }

        writer.write(toString());

        if (hasObjectNumber()) {
            writer.write("\nendobj\n");
        }

        writer.flush();
        return cout.getCount();
    }

    /** {@inheritDoc} */
    public void outputInline(OutputStream out, Writer writer) throws IOException {
        if (hasObjectNumber()) {
            writer.write(referencePDF());
        } else {
            writer.write(toString());
        }
    }

}
