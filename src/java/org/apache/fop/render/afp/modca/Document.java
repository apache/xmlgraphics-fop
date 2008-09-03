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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The document is the highest level of the MO:DCA data-stream document
 * component hierarchy. Documents can be made up of pages, and the pages, which
 * are at the intermediate level, can be made up of objects. Objects are at the
 * lowest level, and can be bar codes, graphics, images, and presentation text.
 *
 * At each level of the hierarchy certain sets of MO:DCA data structures, called
 * structured fields, are permissible. The document, pages and objects are
 * bounded by structured fields that define their beginnings and their ends.
 * These structured fields, called begin-end pairs, provide an envelope for the
 * data-stream components. This feature enables a processor of the data stream
 * that is not fully compliant with the architecture to bypass those objects
 * that are beyond its scope, and to process the data stream to the best of its
 * abilities.
 *
 * A presentation document is one that has been formatted and is intended for
 * presentation, usually on a printer or display device. A data stream
 * containing a presentation document should produce the same document content
 * in the same format on different printers or display devices dependent,
 * however, on the capabilities of each of the printers or display devices. A
 * presentation document can reference resources that are to be included as part
 * of the document to be presented.
 *
 */
public final class Document extends AbstractResourceEnvironmentGroupContainer {

    /**
     * Constructor for the document object.
     *
     * @param factory
     *            the object factory
     * @param name
     *            the name of the document
     */
    public Document(Factory factory, String name) {
        super(factory, name);
    }

    /**
     * Method to mark the end of the page group.
     */
    public void endDocument() {
        complete = true;
    }

    /**
     * Returns an indication if the page group is complete
     *
     * @return whether or not this page group is complete
     */
    public boolean isComplete() {
        return complete;
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.DOCUMENT);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.DOCUMENT);
        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return this.name;
    }

}