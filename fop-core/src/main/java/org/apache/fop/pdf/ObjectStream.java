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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.pdf.xref.CompressedObjectReference;

/**
 * An object stream, as described in section 3.4.6 of the PDF 1.5 Reference.
 */
public class ObjectStream extends PDFStream {

    private static final PDFName OBJ_STM = new PDFName("ObjStm");

    private List<CompressedObject> objects = new ArrayList<CompressedObject>();

    private int firstObjectOffset;

    ObjectStream() {
        super(false);
    }

    ObjectStream(ObjectStream previous) {
        this();
        put("Extends", previous);
    }

    CompressedObjectReference addObject(CompressedObject obj) {
        if (obj == null) {
            throw new NullPointerException("obj must not be null");
        }
        CompressedObjectReference reference = new CompressedObjectReference(obj.getObjectNumber(),
                getObjectNumber(), objects.size());
        objects.add(obj);
        return reference;
    }

    @Override
    protected void outputRawStreamData(OutputStream out) throws IOException {
        int currentOffset = 0;
        StringBuilder offsetsPart = new StringBuilder();
        ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
        for (CompressedObject object : objects) {
            offsetsPart.append(object.getObjectNumber())
                    .append(' ')
                    .append(currentOffset)
                    .append('\n');
            currentOffset += object.output(streamContent);
        }
        byte[] offsets = PDFDocument.encode(offsetsPart.toString());
        firstObjectOffset = offsets.length;
        out.write(offsets);
        streamContent.writeTo(out);
    }

    @Override
    protected void populateStreamDict(Object lengthEntry) {
        put("Type", OBJ_STM);
        put("N", objects.size());
        put("First", firstObjectOffset);
        super.populateStreamDict(lengthEntry);
    }
}
