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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * The document is the highest level of the MO:DCA data-stream document
 * component hierarchy. Documents can be made up of pages, and the pages,
 * which are at the intermediate level, can be made up of objects. Objects
 * are at the lowest level, and can be bar codes, graphics, images, and
 * presentation text.
 *
 * At each level of the hierarchy certain sets of MO:DCA data structures,
 * called structured fields, are permissible. The document, pages and objects
 * are bounded by structured fields that define their beginnings and their ends.
 * These structured fields, called begin-end pairs, provide an envelope for the
 * data-stream components. This feature enables a processor of the data stream
 * that is not fully compliant with the architecture to bypass those objects
 * that are beyond its scope, and to process the data stream to the best of its
 * abilities.
 *
 * A presentation document is one that has been formatted and is intended for
 * presentation, usually on a printer or display device. A data stream containing
 * a presentation document should produce the same document content in the
 * same format on different printers or display devices dependent, however,
 * on the capabilities of each of the printers or display devices. A presentation
 * document can reference resources that are to be included as part of the
 * document to be presented.
 *
 */
public final class Document extends AbstractNamedAFPObject {

    /**
     * Ststic default name reference
     */
    private final static String DEFAULT_NAME = "DOC00001";

    /**
     * A list of the objects in the document
     */
    private ArrayList _objects = new ArrayList();

    /**
     * The document started state
     */
    private boolean _started = false;

    /**
     * The document completion state
     */
    private boolean _complete = false;

    /**
     * Default constructor for the document object.
     */
    public Document() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor for the document object.
     * @param name The name of the document
     */
    public Document(String name) {

        super(name);

    }

    /**
     * Adds a page to the document.
     * @param page - the Page object
     */
    public void addPage(PageObject page) {
        if (!_objects.contains(page)) {
            _objects.add(page);
        }
    }

    /**
     * Adds a PageGroup to the document.
     * @param pageGroup the PageGroup object
     */
    public void addPageGroup(PageGroup pageGroup) {
        _objects.add(pageGroup);
    }

    /**
     * Method to mark the end of the page group.
     */
    public void endDocument() {

        _complete = true;

    }

    /**
     * Returns an indication if the page group is complete
     */
    public boolean isComplete() {
        return _complete;
    }

    /**
     * Accessor method to write the AFP datastream for document.
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        if (!_started) {
            writeStart(os);
            _started = true;
        }

        for (Iterator it = _objects.iterator(); it.hasNext(); ) {
            AbstractAFPObject ao = (AbstractAFPObject)it.next();
            if (ao instanceof PageObject && ((PageObject)ao).isComplete()
                || ao instanceof PageGroup && ((PageGroup)ao).isComplete()) {
                ao.writeDataStream(os);
                it.remove();
            } else {
                break;
            }
        }

        if (_complete) {
            writeEnd(os);
        }

    }

    /**
     * Helper method to write the start of the Document
     * @param os The stream to write to
     */
    private void writeStart(OutputStream os)
        throws IOException {

        byte[] data = new byte[17];

        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0xA8; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

    /**
     * Helper method to write the end of the Document.
     * @param os The stream to write to
     */
    private void writeEnd(OutputStream os)
        throws IOException {

        byte[] data = new byte[17];

        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xA8; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

}