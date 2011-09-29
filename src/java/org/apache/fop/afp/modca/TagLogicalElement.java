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

import org.apache.fop.afp.modca.triplets.AttributeQualifierTriplet;
import org.apache.fop.afp.modca.triplets.AttributeValueTriplet;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.util.BinaryUtils;

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
public class TagLogicalElement extends AbstractTripletStructuredObject {

    /**
     * Name of the key, used within the TLE
     */
    private String name = null;

    /**
     * Value returned by the key
     */
    private String value = null;

    /**
     * Sequence of TLE within document
     */
    private int tleID;

    /**
     * Construct a tag logical element with the name and value specified.
     *
     * @param name the name of the tag logical element
     * @param value the value of the tag logical element
     * @param tleID unique identifier for TLE within AFP stream
     */
    public TagLogicalElement(String name, String value, int tleID) {
        this.name = name;
        this.value = value;
        this.tleID = tleID;
    }

    /**
     * Sets the attribute value of this structured field
     *
     * @param value the attribute value
     */
    public void setAttributeValue(String value) {
        addTriplet(new AttributeValueTriplet(value));
    }

    /**
     * Sets the attribute qualifier of this structured field
     *
     * @param seqNumber the attribute sequence number
     * @param levNumber the attribute level number
     */
    public void setAttributeQualifier(int seqNumber, int levNumber) {
        addTriplet(new AttributeQualifierTriplet(seqNumber, levNumber));
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        setFullyQualifiedName(
                FullyQualifiedNameTriplet.TYPE_ATTRIBUTE_GID,
                FullyQualifiedNameTriplet.FORMAT_CHARSTR,
                name);
        setAttributeValue(value);
        setAttributeQualifier(tleID, 1);

        byte[] data = new byte[SF_HEADER_LENGTH];
        copySF(data, Type.ATTRIBUTE, Category.PROCESS_ELEMENT);

        int tripletDataLength = getTripletDataLength();
        byte[] l = BinaryUtils.convert(data.length + tripletDataLength - 1, 2);
        data[1] = l[0];
        data[2] = l[1];
        os.write(data);

        writeTriplets(os);
    }
}
