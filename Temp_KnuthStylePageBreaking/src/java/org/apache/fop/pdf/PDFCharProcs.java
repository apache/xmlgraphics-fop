/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Map;
import java.util.HashMap;     

/**
 * class representing a /CharProcs dictionary for Type3 fonts.
 *
 * <p><b>CAUTION: this is not yet fully implemented!!!!!!!</b>
 * I miss an exemple of <i>how</i> to output this dictionary.
 * </p>
 *
 * Type3 fonts are specified on page 206 and onwards of the PDF 1.3 spec.
 */
public class PDFCharProcs extends PDFObject {

    /**
     * the (character name, drawing stream) pairs for a Type3 font
     */
    protected Map keys;

    /**
     * Create a new PDF char proc store.
     */
    public PDFCharProcs() {
        keys = new HashMap();
    }

    /**
     * add a character definition in the dictionary
     *
     * @param name the character name
     * @param stream the stream that draws the character
     */
    public void addCharacter(String name, PDFStream stream) {
        keys.put(name, stream);
    }

    /**
     * not done yet
     * @return the pdf byte array
     */
    public byte[] toPDF() {
        // TODO: implement this org.apache.fop.pdf.PDFObject abstract method
        return new byte[0];
    }

}
