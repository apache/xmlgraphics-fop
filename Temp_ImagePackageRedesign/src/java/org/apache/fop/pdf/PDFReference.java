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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * Class representing a PDF object reference. The object holds a soft reference to the actual
 * PDF object so the garbage collector can free the object if it's not referenced elsewhere. The
 * important thing about the class is the reference information to the actual PDF object in the
 * PDF file.
 */
public class PDFReference implements PDFWritable {
    
    private String indirectReference;
    
    private Reference objReference;
    
    /**
     * Creates a new PDF reference.
     * @param obj the object to be referenced
     */
    public PDFReference(PDFObject obj) {
        this.indirectReference = obj.referencePDF();
        this.objReference = new SoftReference(obj);
    }
    
    /**
     * Creates a new PDF reference, but without a reference to the original object.
     * @param ref an object reference
     */
    public PDFReference(String ref) {
        if (ref == null) {
            throw new NullPointerException("ref must not be null");
        }
        this.indirectReference = ref;
    }

    /**
     * Returns the PDF object
     * @return the PDF object, or null if it has been released
     */
    public PDFObject getObject() {
        if (this.objReference != null) {
            PDFObject obj = (PDFObject)this.objReference.get();
            if (obj == null) {
                this.objReference = null;
            }
            return obj;
        } else {
            return null;
        }
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return this.indirectReference;
    }
    
    /** {@inheritDoc} */
    public void outputInline(OutputStream out, Writer writer) throws IOException {
        writer.write(toString());
    }
    
}
