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
 * Represents a PDF object that may appear in an object stream. An object stream is a PDF
 * stream whose content is a sequence of PDF objects. See Section 3.4.6 of the PDF 1.5
 * Reference.
 */
interface CompressedObject {

    /**
     * Returns the object number of this indirect object. Note that a compressed object
     * must have a generation number of 0.
     *
     * @return the object number.
     */
    int getObjectNumber();

    /**
     * Outputs this object's content into the given stream.
     *
     * @param outputStream a stream, likely to be provided by the containing object stream
     * @return the number of bytes written to the stream
     * @throws IOException
     */
    int output(OutputStream outputStream) throws IOException;

}
