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

// Java
import java.io.IOException;

/**
 * Abstract base class of PDF XObjects.
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * The dictionary just provides information like the stream length.
 * This outputs the image dictionary and the image data.
 * This is used as a reference for inserting the same image in the
 * document in another place.
 */
public abstract class PDFXObject extends AbstractPDFStream {

    /**
     * Create an XObject with the given number.
     */
    public PDFXObject() {
        super();
    }

    protected PDFXObject(PDFDictionary dictionary) {
        super(dictionary);
    }

    /**
     * Returns the XObject's name.
     * @return the name of the XObject
     */
    public PDFName getName() {
        return (PDFName)get("Name");
    }

    /** {@inheritDoc} */
    protected void populateStreamDict(Object lengthEntry) {
        put("Type", new PDFName("XObject"));
        super.populateStreamDict(lengthEntry);
    }

    /** {@inheritDoc} */
    protected int getSizeHint() throws IOException {
        return 0;
    }

}
