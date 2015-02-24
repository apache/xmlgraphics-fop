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

import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * Class representing a PDF object reference. The object holds a soft reference to the actual
 * PDF object so the garbage collector can free the object if it's not referenced elsewhere. The
 * important thing about the class is the reference information to the actual PDF object in the
 * PDF file.
 */
public class PDFReference implements PDFWritable {

    private PDFObjectNumber objectNumber;
    private int generation;

    private Reference<PDFObject> objReference;

    /**
     * Creates a new PDF reference.
     * @param obj the object to be referenced
     */
    public PDFReference(PDFObject obj) {
        this.objectNumber = obj.getObjectNumber();
        this.generation = obj.getGeneration();
        this.objReference = new SoftReference<PDFObject>(obj);
    }

    /**
     * Creates a new PDF reference, but without a reference to the original object.
     * @param ref an object reference
     */
    public PDFReference(String ref) {
        if (ref == null) {
            throw new NullPointerException("ref must not be null");
        }
        String[] parts = ref.split(" ");
        assert parts.length == 3;
        this.objectNumber = new PDFObjectNumber(Integer.parseInt(parts[0]));
        this.generation = Integer.parseInt(parts[1]);
        assert "R".equals(parts[2]);
    }

    /**
     * Returns the PDF object
     * @return the PDF object, or null if it has been released
     */
    public PDFObject getObject() {
        if (this.objReference != null) {
            PDFObject obj = this.objReference.get();
            if (obj == null) {
                this.objReference = null;
            }
            return obj;
        } else {
            return null;
        }
    }

    /**
     * Returns the object number.
     * @return the object number
     */
    public PDFObjectNumber getObjectNumber() {
        return this.objectNumber;
    }

    /**
     * Returns the generation.
     * @return the generation
     */
    public int getGeneration() {
        return this.generation;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder textBuffer = new StringBuilder();
        outputInline(null, textBuffer);
        return textBuffer.toString();
    }

    /** {@inheritDoc} */
    public void outputInline(OutputStream out, StringBuilder textBuffer) {
        textBuffer.append(getObjectNumber().getNumber()).append(' ').append(getGeneration()).append(" R");
    }

}
