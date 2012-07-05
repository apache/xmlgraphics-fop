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
import java.util.Date;

/**
 * This class represents an embedded file stream.
 */
public class PDFEmbeddedFile extends PDFStream {

    /**
     * Creates a new embedded file stream.
     */
    public PDFEmbeddedFile() {
        super();
        put("Type", new PDFName("EmbeddedFile"));
        PDFDictionary params = new PDFDictionary();
        params.put("CreationDate", PDFInfo.formatDateTime(new Date()));
        put("Params", params);
    }

    /** {@inheritDoc} */
    protected boolean isEncodingOnTheFly() {
        //Acrobat doesn't like an indirect /Length object in this case,
        //but only when the embedded file is a PDF file.
        return false;
    }

    /** {@inheritDoc} */
    protected void populateStreamDict(Object lengthEntry) {
        super.populateStreamDict(lengthEntry);
        try {
            PDFDictionary dict = (PDFDictionary)get("Params");
            dict.put("Size", new Integer(data.getSize()));
        } catch (IOException ioe) {
            //ignore and just skip this entry as it's optional
        }
    }

}
