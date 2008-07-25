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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.CountingOutputStream;

/**
 * Class representing a PDF dictionary object
 */
public class PDFDictionary extends PDFObject {

    /**
     * the entry map
     */
    protected Map entries = new java.util.HashMap();

    /**
     * maintains the order of the entries added to the entry map. Whenever you modify
     * "entries", always make sure you adjust this list accordingly.
     */
    protected List order = new java.util.ArrayList();

    /**
     * Create a new dictionary object.
     */
    public PDFDictionary() {
        super();
    }

    /**
     * Create a new dictionary object.
     * @param parent the object's parent if any
     */
    public PDFDictionary(PDFObject parent) {
        super(parent);
    }

    /**
     * Puts a new name/value pair.
     * @param name the name
     * @param value the value
     */
    public void put(String name, Object value) {
        if (value instanceof PDFObject) {
            PDFObject pdfObj = (PDFObject)value;
            if (!pdfObj.hasObjectNumber()) {
                pdfObj.setParent(this);
            }
        }
        if (!entries.containsKey(name)) {
            this.order.add(name);
        }
        this.entries.put(name, value);
    }

    /**
     * Puts a new name/value pair.
     * @param name the name
     * @param value the value
     */
    public void put(String name, int value) {
        if (!entries.containsKey(name)) {
            this.order.add(name);
        }
        this.entries.put(name, new Integer(value));
    }

    /**
     * Returns the value given a name.
     * @param name the name of the value
     * @return the value or null, if there's no value with the given name.
     */
    public Object get(String name) {
        return this.entries.get(name);
    }

    /** {@inheritDoc} */
    protected int output(OutputStream stream) throws IOException {
        CountingOutputStream cout = new CountingOutputStream(stream);
        Writer writer = PDFDocument.getWriterFor(cout);
        if (hasObjectNumber()) {
            writer.write(getObjectID());
        }

        writeDictionary(cout, writer);

        if (hasObjectNumber()) {
            writer.write("\nendobj\n");
        }

        writer.flush();
        return cout.getCount();
    }

    /**
     * Writes the contents of the dictionary to a StringBuffer.
     * @param out the OutputStream (for binary content)
     * @param writer the Writer (for text content, wraps the above OutputStream)
     * @throws IOException if an I/O error occurs
     */
    protected void writeDictionary(OutputStream out, Writer writer) throws IOException {
        writer.write("<<");
        boolean compact = (this.order.size() <= 2);
        Iterator iter = this.order.iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            if (compact) {
                writer.write(' ');
            } else {
                writer.write("\n  ");
            }
            writer.write('/');
            writer.write(key);
            writer.write(' ');
            Object obj = this.entries.get(key);
            formatObject(obj, out, writer);
        }
        if (compact) {
            writer.write(' ');
        } else {
            writer.write('\n');
        }
        writer.write(">>\n");
    }

}
