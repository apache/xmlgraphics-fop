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

package org.apache.fop.pdf.xref;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A representation of the cross-reference data to be output at the end of a PDF file.
 */
public abstract class CrossReferenceObject {

    protected final TrailerDictionary trailerDictionary;

    protected final long startxref;

    CrossReferenceObject(TrailerDictionary trailerDictionary, long startxref) {
        this.trailerDictionary = trailerDictionary;
        this.startxref = startxref;
    }

    /**
     * Writes the cross reference data to a PDF stream
     *
     * @param stream the stream to write the cross reference to
     * @throws IOException if an I/O exception occurs while writing the data
     */
    public abstract void output(OutputStream stream) throws IOException;
}
