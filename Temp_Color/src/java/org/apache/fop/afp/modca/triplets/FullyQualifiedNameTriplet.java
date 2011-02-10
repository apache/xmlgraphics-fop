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

package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.fop.afp.AFPConstants;

/**
 * A Fully Qualified Name triplet enable the identification and referencing of
 * objects using Gloabl Identifiers (GIDs).
 */
public class FullyQualifiedNameTriplet extends AbstractTriplet {

    // Specifies how the GID will be used

    /** This GID replaces the first parameter in the structured field that contains a GID name. */
    public static final byte TYPE_REPLACE_FIRST_GID_NAME = 0x01;

    /** This triplet contains the name of a font family. */
    public static final byte TYPE_FONT_FAMILY_NAME = 0x07;

    /** This triplet contains the name of a font typeface. */
    public static final byte TYPE_FONT_TYPEFACE_NAME = 0x08;

    /** This triplet specifies a reference to the MO:DCA resource hierarchy. */
    public static final byte TYPE_MODCA_RESOURCE_HIERARCHY_REF = 0x09;

    /** The triplet contains a GID reference to a begin resource group structured field. */
    public static final byte TYPE_BEGIN_RESOURCE_GROUP_REF = 0x0A;

    /** The triplet contains a GID reference to a document attribute. */
    public static final byte TYPE_ATTRIBUTE_GID = 0x0B;

    /** The triplet contains the GID of a process element. */
    public static final byte TYPE_PROCESS_ELEMENT_GID = 0x0C;

    /** The triplet contains a reference to a begin page group structured field. */
    public static final byte TYPE_BEGIN_PAGE_GROUP_REF = 0x0D;

    /** The triplet contains a reference to a media type. */
    public static final byte TYPE_MEDIA_TYPE_REF = 0x11;

    /** The triplet contains a reference to a color management resource. */
    public static final byte TYPE_COLOR_MANAGEMENT_RESOURCE_REF = 0x41;

    /** The triplet contains a reference to a data-object font file that defines a base font. */
    public static final byte TYPE_DATA_OBJECT_FONT_BASE_FONT_ID = 0x6E;

    /** The triplet contains a reference to a data-object font file that defines a linked font. */
    public static final byte TYPE_DATA_OBJECT_FONT_LINKED_FONT_ID = 0x7E;

    /** The triplet contains a reference to a begin document structured field. */
    public static final byte TYPE_BEGIN_DOCUMENT_REF = (byte)0x83;

    /**
     * The triplet contains a reference to a begin structured field associated with a resource;
     * or contains a GID reference to a coded font.
     */
    public static final byte TYPE_BEGIN_RESOURCE_OBJECT_REF = (byte)0x84;

    /**
     * The triplet contains a GID reference to a code page that specifies the code points and
     * graphic character names for a coded font.
     */
    public static final byte TYPE_CODE_PAGE_NAME_REF = (byte)0x85;

    /**
     * The triplet contains a GID name reference to a font character set that specifies
     * a set of graphics characters.
     */
    public static final byte TYPE_FONT_CHARSET_NAME_REF = (byte)0x86;

    /** The triplet contains a GID reference to a begin page structured field. */
    public static final byte TYPE_BEGIN_PAGE_REF = (byte)0x87;

    /** The triplet contains a GID reference to a begin medium map structured field. */
    public static final byte TYPE_BEGIN_MEDIUM_MAP_REF = (byte)0x8D;

    /**
     * The triplet contains a GID reference to a coded font, which identifies a specific
     * code page and a specific font character set.
     */
    public static final byte TYPE_CODED_FONT_NAME_REF = (byte)0x8E;

    /** The triplet contains a GID reference to a begin document index structured field. */
    public static final byte TYPE_BEGIN_DOCUMENT_INDEX_REF = (byte)0x98;

    /** The triplet contains a GID reference to a begin overlay structured field. */
    public static final byte TYPE_BEGIN_OVERLAY_REF = (byte)0xB0;

    /** The triplet contains a GID reference to a resource used by a data object. */
    public static final byte TYPE_DATA_OBJECT_INTERNAL_RESOURCE_REF = (byte)0xBE;

    /** The triplet contains a GID reference to an index element structured field. */
    public static final byte TYPE_INDEX_ELEMENT_GID = (byte)0xCA;

    /**
     * The triplet contains a reference to other object data which may or may
     * not be defined by an IBM presentation architecture.
     */
    public static final byte TYPE_OTHER_OBJECT_DATA_REF = (byte)0xCE;

    /**
     * The triplet contains a reference to a resource used by a data object.
     * The GID may be a filename or any other identifier associated with the
     * resource and is used to located the resource object in the resource hierarchy.
     * The data object that uses the resource may or may not be defined by an
     * IBM presentation architecture.
     */
    public static final byte TYPE_DATA_OBJECT_EXTERNAL_RESOURCE_REF = (byte)0xDE;


    // GID Format

    /** The GID is a character encoded name. */
    public static final byte FORMAT_CHARSTR = (byte)0x00;

    /** the GID is a ASN.1 object identifier (OID). */
    public static final byte FORMAT_OID = (byte)0x10;

    /** the GID is a uniform resource locator (URL). */
    public static final byte FORMAT_URL = (byte)0x20;

    /** the fully qualified name type */
    private final byte type;

    /** the fully qualified name format */
    private final byte format;

    /** the actual fully qualified name */
    private final String fqName;

    /**
     * Main constructor
     *
     * @param type the fully qualified name type
     * @param format the fully qualified name format
     * @param fqName the fully qualified name
     */
    public FullyQualifiedNameTriplet(byte type, byte format, String fqName) {
        super(FULLY_QUALIFIED_NAME);
        this.type = type;
        this.format = format;
        this.fqName = fqName;
    }

    /**
     * Returns the actual fully qualified name
     *
     * @return the actual fully qualified name
     */
    public String getFullyQualifiedName() {
        return fqName;
    }

    /** {@inheritDoc} */
    public String toString() {
        return this.fqName;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 4 + fqName.length();
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        data[2] = type;
        data[3] = format;

        // FQName
        byte[] fqNameBytes;
        String encoding = AFPConstants.EBCIDIC_ENCODING;
        if (format == FORMAT_URL) {
            encoding = AFPConstants.US_ASCII_ENCODING;
        }
        try {
            fqNameBytes = fqName.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(
                    encoding + " encoding failed");
        }
        System.arraycopy(fqNameBytes, 0, data, 4, fqNameBytes.length);

        os.write(data);
    }
}