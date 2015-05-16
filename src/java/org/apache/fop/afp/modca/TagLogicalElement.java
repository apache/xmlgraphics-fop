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
import org.apache.fop.afp.modca.triplets.EncodingTriplet;
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
 */
public class TagLogicalElement extends AbstractTripletStructuredObject {

    /**
     * the params of the TLE
     */
    private State state;

    /**
     * Construct a tag logical element with the name and value specified.
     *
     * @param state the state of the tag logical element
     */

    public TagLogicalElement(State state) {
        this.state = state;
    }

    private void setAttributeValue(String value) {
        addTriplet(new AttributeValueTriplet(value));
    }

    private void setEncoding(int encoding) {
        if (encoding != State.ENCODING_NONE) {
            addTriplet(new EncodingTriplet(encoding));
        }
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
                state.key);
        setAttributeValue(state.value);
        setEncoding(state.encoding);

        byte[] data = new byte[SF_HEADER_LENGTH];
        copySF(data, Type.ATTRIBUTE, Category.PROCESS_ELEMENT);

        int tripletDataLength = getTripletDataLength();
        byte[] l = BinaryUtils.convert(data.length + tripletDataLength - 1, 2);
        data[1] = l[0];
        data[2] = l[1];
        os.write(data);

        writeTriplets(os);
    }

    /**
     *
     * Holds the attribute state of a TLE
     *
     */
    public static class State {

        /**
         *  value  interpreted as no encoding
         */
        public static final int ENCODING_NONE = -1;
        /** The key attribute */
        private String key;

        /** The value attribute */
        private String value;

        /** The CCSID character et encoding attribute */
        private int encoding =  ENCODING_NONE;


        /**
         * Constructor
         *
         * @param key the key attribute
         * @param value the value attribute
         */
        public State(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         *
         * @param key the key attribute
         * @param value the value attribute
         * @param encoding the CCSID character set encoding attribute
         */
        public State(String key, String value, int encoding) {
            this.key = key;
            this.value = value;
            this.encoding = encoding;
        }


    }
}
