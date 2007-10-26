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
 * Pages contain the data objects that comprise a presentation document. Each
 * page has a set of data objects associated with it. Each page within a
 * document is independent from any other page, and each must establish its own
 * environment parameters.
 *
 * The page is the level in the document component hierarchy that is used for
 * printing or displaying a document's content. The data objects contained in
 * the page envelope in the data stream are presented when the page is
 * presented. Each data object has layout information associated with it that
 * directs the placement and orientation of the data on the page. In addition,
 * each page contains layout information that specifies the measurement units,
 * page width, and page depth.
 *
 * A page is initiated by a begin page structured field and terminated by an end
 * page structured field. Structured fields that define objects and active
 * environment groups or that specify attributes of the page may be encountered
 * in page state.
 *
 */
public class PageObject extends AbstractPageObject {

    /**
     * The resource group object
     */
    private ResourceGroup _resourceGroup = null;

    /**
     * Construct a new page object for the specified name argument, the page
     * name should be an 8 character identifier.
     *
     * @param name
     *            the name of the page.
     * @param width
     *            the width of the page.
     * @param height
     *            the height of the page.
     * @param rotation
     *            the rotation of the page.
     */
    public PageObject(String name, int width, int height, int rotation) {

        super(name, width, height, rotation);

    }

    /**
     * Adds an overlay to the page resources
     * @param overlay the overlay to add
     */
    public void addOverlay(Overlay overlay) {
        if (_resourceGroup == null) {
            _resourceGroup = new ResourceGroup();
        }
        _resourceGroup.addOverlay(overlay);
    }

    /**
     * Creates an IncludePageOverlay on the page.
     *
     * @param name
     *            the name of the overlay
     * @param x
     *            the x position of the overlay
     * @param y
     *            the y position of the overlay
     * @param orientation
     *            the orientation required for the overlay
     */
    public void createIncludePageOverlay(String name, int x, int y, int orientation) {

        IncludePageOverlay ipo = new IncludePageOverlay(name, x, y, orientation);
        _objects.add(ipo);

    }

    /**
     * Accessor method to write the AFP datastream for the page.
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        writeStart(os);

        if (_resourceGroup != null) {
            _resourceGroup.writeDataStream(os);
        }

        _activeEnvironmentGroup.writeDataStream(os);

        writeObjectList(_segments, os);

        writeObjectList(_tagLogicalElements, os);

        writeObjectList(_objects, os);

        writeEnd(os);

    }

    /**
     * Helper method to write the start of the page.
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
        data[5] = (byte) 0xAF; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

    /**
     * Helper method to write the end of the page.
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
        data[5] = (byte) 0xAF; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

}