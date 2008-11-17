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

import java.io.UnsupportedEncodingException;

import org.apache.fop.afp.AFPConstants;

/**
 * This is the base class for all named data stream objects.
 * A named data stream object has an 8 byte EBCIDIC name.
 */
public abstract class AbstractNamedAFPObject extends AbstractTripletStructuredObject {

    private static final int DEFAULT_NAME_LENGTH = 8;

    /**
     * The actual name of the object
     */
    protected String name = null;

    /**
     * Default constructor
     */
    protected AbstractNamedAFPObject() {
    }

    /**
     * Constructor for the ActiveEnvironmentGroup, this takes a
     * name parameter which should be 8 characters long.
     *
     * @param name the object name
     */
    protected AbstractNamedAFPObject(String name) {
        this.name = name;
    }

    /**
     * Returns the name length
     *
     * @return the name length
     */
    protected int getNameLength() {
        return DEFAULT_NAME_LENGTH;
    }

    /**
     * Returns the name as a byte array in EBCIDIC encoding
     *
     * @return the name as a byte array in EBCIDIC encoding
     */
    protected byte[] getNameBytes() {
        int afpNameLen = getNameLength();
        int nameLen = name.length();
        if (nameLen < afpNameLen) {
            name = (name + "       ").substring(0, afpNameLen);
        } else if (name.length() > afpNameLen) {
            String truncatedName = name.substring(nameLen - afpNameLen, nameLen);
            log.warn("Constructor:: name '" + name + "'"
                    + " truncated to " + afpNameLen + " chars"
                    + " ('" + truncatedName + "')");
            name = truncatedName;
        }
        byte[] nameBytes = null;
        try {
            nameBytes = name.getBytes(AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException usee) {
            nameBytes = name.getBytes();
            log.warn(
                "Constructor:: UnsupportedEncodingException translating the name "
                + name);
        }
        return nameBytes;
    }

    /** {@inheritDoc} */
    protected void copySF(byte[] data, byte type, byte category) {
        super.copySF(data, type, category);
        byte[] nameData = getNameBytes();
        System.arraycopy(nameData, 0, data, 9, nameData.length);
    }

    /**
     * Returns the name of this object
     *
     * @return the name of this object
     */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }
}
