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

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.pdf.xref.CompressedObjectReference;

/**
 * Manages a collection of object streams, creating new streams as necessary to keep the
 * number of objects in each stream at the recommended value. Streams are related to each
 * other through the use of the Extends entry in the stream dictionary.
 */
class ObjectStreamManager {

    private static final int OBJECT_STREAM_CAPACITY = 100;

    private final PDFDocument pdfDocument;

    private final List<CompressedObjectReference> compressedObjectReferences;

    private int numObjectsInStream;

    private ObjectStream currentObjectStream;

    ObjectStreamManager(PDFDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
        createObjectStream();
        compressedObjectReferences = new ArrayList<CompressedObjectReference>();
    }

    void add(CompressedObject compressedObject) {
        if (numObjectsInStream++ == OBJECT_STREAM_CAPACITY) {
            createObjectStream();
            numObjectsInStream = 1;
        }
        compressedObjectReferences.add(currentObjectStream.addObject(compressedObject));
    }

    private void createObjectStream() {
        currentObjectStream = currentObjectStream == null
                ? new ObjectStream()
                : new ObjectStream(currentObjectStream);
        pdfDocument.assignObjectNumber(currentObjectStream);
        pdfDocument.addTrailerObject(currentObjectStream);
    }

    List<CompressedObjectReference> getCompressedObjectReferences() {
        return compressedObjectReferences;
    }
}
