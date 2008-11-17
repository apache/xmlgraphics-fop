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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract class encapsulating an MODCA structured object
 */
public abstract class AbstractStructuredObject extends AbstractAFPObject {

    /**
     * Default constructor
     */
    protected AbstractStructuredObject() {
    }

    /**
     * Helper method to write the start of the Object.
     *
     * @param os The stream to write to
     * @throws IOException throws an I/O exception if one occurred
     */
    protected void writeStart(OutputStream os) throws IOException {
    }

    /**
     * Helper method to write the end of the Object.
     *
     * @param os The stream to write to
     * @throws IOException an I/O exception if one occurred
     */
    protected void writeEnd(OutputStream os) throws IOException {
    }

    /**
     * Helper method to write the contents of the Object.
     *
     * @param os The stream to write to
     * @throws IOException throws an I/O exception if one occurred
     */
    protected void writeContent(OutputStream os) throws IOException {
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        writeStart(os);
        writeContent(os);
        writeEnd(os);
    }
}
