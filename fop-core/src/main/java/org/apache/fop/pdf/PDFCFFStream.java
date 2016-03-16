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

/**
 * Special PDFStream for embeddable CFF fonts.
 */
public class PDFCFFStream extends AbstractPDFFontStream {
    private byte[] cffData;
    private String type;

    public PDFCFFStream(String type) {
        this.type = type;
    }

    protected int getSizeHint() throws IOException {
        if (this.cffData != null) {
            return cffData.length;
        } else {
            return 0; //no hint available
        }
    }

    protected void outputRawStreamData(OutputStream out) throws IOException {
        out.write(this.cffData);
    }

    protected void populateStreamDict(Object lengthEntry) {
        put("Subtype", new PDFName(type));
        super.populateStreamDict(lengthEntry);
    }

    public void setData(byte[] data) throws IOException {
        cffData = data;
    }
}
