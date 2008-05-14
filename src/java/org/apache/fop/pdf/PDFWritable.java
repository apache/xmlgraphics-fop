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

/**
 * This interface is implemented by classes that can be serialized to a PDF file either by
 * serializing the object or by writing a indirect reference to the actual object.
 */
public interface PDFWritable {
    
    /**
     * Writes a "direct object" (inline object) representation to the stream. A Writer is given
     * for optimized encoding of text content. Since the Writer is buffered, make sure
     * <code>flush()</code> is called before any direct calls to <code>out</code> are made.
     * @param out the OutputStream (for binary content)
     * @param writer the Writer (for text content, wraps the above OutputStream)
     * @throws IOException if an I/O error occurs
     */
    void outputInline(OutputStream out, Writer writer) throws IOException;
    
}
