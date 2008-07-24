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

import org.apache.fop.render.afp.AFPConstants;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * A Tag Logical Element structured field assigns an attribute name and an
 * attribute value to a page or page group. The Tag Logical Element structured
 * field may be embedded directly in the page or page group, or it may reference
 * the page or page group from a document index. When a Tag Logical Element
 * structured field references a page or is embedded in a page following the
 * active environment group, it is associated with the page. When a Tag Logical
 * Element structured field references a page group or is embedded in a page
 * group following the Begin Named Page Group structured field, it is associated
 * with the page group. When a Tag Logical Element structured field is associated
 * with a page group, the parameters of the Tag Logical Element structured field
 * are inherited by all pages in the page group and by all other page groups
 * that are nested in the page group. The scope of a Tag Logical Element is
 * determined by its position with respect to other TLEs that reference, or are
 * embedded in, the same page or page group. The Tag Logical Element structured
 * field does not provide any presentation specifications and therefore has no
 * effect on the appearance of a document when it is presented.
 * <p/>
 */
public class TagLogicalElement extends AbstractAFPObject {

    /**
     * Name of the key, used within the TLE
     */
    private String name = null;

    /**
     * Value returned by the key
     */
    private String value = null;

    /**
     * Construct a tag logical element with the name and value specified.
     * 
     * @param name the name of the tag logical element
     * @param value the value of the tag logical element
     */
    public TagLogicalElement(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /** {@inheritDoc} */
    public void write(OutputStream os) throws IOException {

        byte[] data = new byte[17 + name.length() + value.length()];

        data[0] = 0x5A;
        // Set the total record length
        byte[] rl1
            = BinaryUtils.convert(16 + name.length() + value.length(), 2);
        //Ignore first byte
        data[1] = rl1[0];
        data[2] = rl1[1];

        // Structured field ID for a TLE
        data[3] = (byte) 0xD3;
        data[4] = (byte) Type.ATTRIBUTE;
        data[5] = (byte) Category.PROCESS_ELEMENT;

        data[6] = 0x00; // Reserved
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        //Use 2 triplets, attrubute name and value (the key for indexing)

        byte[] rl2 = BinaryUtils.convert(name.length() + 4, 1);
        data[9] = rl2[0]; // length of the triplet, including this field
        data[10] = 0x02; //Identifies it as a FQN triplet
        data[11] = 0x0B; // GID format
        data[12] = 0x00;

        byte[] tleByteName = null;
        byte[] tleByteValue = null;
        try {
            tleByteName = name.getBytes(AFPConstants.EBCIDIC_ENCODING);
            tleByteValue = value.getBytes(AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException usee) {
            tleByteName = name.getBytes();
            tleByteValue = value.getBytes();
            log.warn(
                "Constructor:: UnsupportedEncodingException translating the name "
                + name);
        }

        int pos = 13;
        for (int i = 0; i < tleByteName.length; i++) {
            data[pos++] = tleByteName[i];
        }

        byte[] rl3 = BinaryUtils.convert(tleByteValue.length + 4, 1);
        data[pos++] = rl3[0]; // length of the triplet, including this field
        data[pos++] = 0x36; //Identifies the triplet, attribute value
        data[pos++] = 0x00; // Reserved
        data[pos++] = 0x00; // Reserved

        for (int i = 0; i < tleByteValue.length; i++) {
            data[pos++] = tleByteValue[i];
        }
        os.write(data);
    }
}
