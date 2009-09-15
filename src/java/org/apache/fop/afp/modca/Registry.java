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

import java.util.Collections;

import org.apache.xmlgraphics.util.MimeConstants;

/**
 * MOD:CA Registry of object types
 */
public final class Registry {
    /** IOB supported object types */
    private static final byte COMPID_IOCA_FS10 = 5;
    private static final byte COMPID_IOCA_FS11 = 11;
    private static final byte COMPID_IOCA_FS40 = 55;
    private static final byte COMPID_IOCA_FS45 = 12;
    private static final byte COMPID_EPS = 13;
    private static final byte COMPID_TIFF = 14;
    private static final byte COMPID_GIF = 22;
    private static final byte COMPID_JFIF = 23; // jpeg file interchange format
    private static final byte COMPID_PDF_SINGLE_PAGE = 25;
    private static final byte COMPID_PCL_PAGE_OBJECT = 34;

    private static final byte COMPID_TRUETYPE_OPENTYPE_FONT_RESOURCE_OBJECT = 51;
    private static final byte COMPID_TRUETYPE_OPENTYPE_FONT_COLLECTION_RESOURCE_OBJECT = 53;

    /** mime type entry mapping */
    private final java.util.Map/*<String, ObjectType>*/ mimeObjectTypeMap
        = Collections.synchronizedMap(
                new java.util.HashMap/*<String, ObjectType>*/());

    /** singleton instance */
    private static Registry instance = null;

    /**
     * Returns a single instance of a MO:DCA Registry
     *
     * @return a single instance of an MO:DCA Registry
     */
    public static Registry getInstance() {
        synchronized (Registry.class) {
            if (instance == null) {
                instance = new Registry();
            }
        }
        return instance;
    }

    /**
     * private constructor
     */
    private Registry() {
        init();
    }

    /**
     * Initializes the mimetype map
     */
    private void init() {
        mimeObjectTypeMap.put(
                MimeConstants.MIME_AFP_IOCA_FS10,
                new ObjectType(
                        COMPID_IOCA_FS10,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x05},
                        "IOCA FS10",
                        true,
                        MimeConstants.MIME_AFP_IOCA_FS10
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_AFP_IOCA_FS11,
                new ObjectType(
                        COMPID_IOCA_FS11,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x11},
                        "IOCA FS11",
                        true,
                        MimeConstants.MIME_AFP_IOCA_FS11
                )
        );
//      mimeObjectTypeMap.put(
//      MimeConstants.MIME_AFP_IOCA_FS40,
//      new ObjectType(
//              COMPID_IOCA_FS40,
//              new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x37},
//              "IOCA FS40",
//              true,
//              MimeConstants.MIME_AFP_IOCA_FS40
//      )
//);
        mimeObjectTypeMap.put(
                MimeConstants.MIME_AFP_IOCA_FS45,
                new ObjectType(
                        COMPID_IOCA_FS45,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x12},
                        "IOCA FS45",
                        true,
                        MimeConstants.MIME_AFP_IOCA_FS45
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_EPS,
                new ObjectType(
                        COMPID_EPS,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0D},
                        "Encapsulated Postscript",
                        true,
                        MimeConstants.MIME_EPS
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_TIFF,
                new ObjectType(
                        COMPID_TIFF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0E},
                        "TIFF",
                        true,
                        MimeConstants.MIME_TIFF
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_GIF,
                new ObjectType(
                        COMPID_GIF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x16},
                        "GIF",
                        true,
                        MimeConstants.MIME_GIF
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_JPEG,
                new ObjectType(
                        COMPID_JFIF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x17},
                        "JFIF",
                        true,
                        MimeConstants.MIME_JPEG
                )
        );
        mimeObjectTypeMap.put(MimeConstants.MIME_PDF,
                new ObjectType(
                        COMPID_PDF_SINGLE_PAGE,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x19},
                        "PDF Single-page Object",
                        true,
                        MimeConstants.MIME_PDF
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_PCL,
                new ObjectType(
                        COMPID_PCL_PAGE_OBJECT,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x22},
                        "PCL Page Object",
                        true,
                        MimeConstants.MIME_PCL
                )
        );
//        mimeObjectTypeMap.put(
//                null,
//                new ObjectType(
//                        COMPID_TRUETYPE_OPENTYPE_FONT_RESOURCE_OBJECT,
//                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x33},
//                        "TrueType/OpenType Font Resource Object",
//                        true,
//                        null
//                )
//        );
//        mimeObjectTypeMap.put(
//                null,
//                new ObjectType(
//                        COMPID_TRUETYPE_OPENTYPE_FONT_COLLECTION_RESOURCE_OBJECT,
//                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x35},
//                        "TrueType/OpenType Font Collection Resource Object",
//                        true,
//                        null
//                )
//        );
    }

    /**
     * Returns the MOD:CA object type given a mimetype
     *
     * @param mimeType the object mimetype
     * @return the MOD:CA object type
     */
    public ObjectType getObjectType(String mimeType) {
        return (ObjectType)mimeObjectTypeMap.get(mimeType);
    }

    /**
     * Encapsulates a MOD:CA Registry Object Type entry
     */
    public class ObjectType {
        private final byte componentId;
        private final byte[] oid;
        private final String name;
        private final boolean includable;
        private final String mimeType;

        /**
         * Main constructor
         *
         * @param componentId the component id of this object type
         * @param oid the object id of this object type
         * @param name the object type name
         * @param includable true if this object can be included with an IOB structured field
         * @param mimeType the mime type associated with this object type
         */
        public ObjectType(byte componentId, byte[] oid, String name,
                boolean includable, String mimeType) {
            this.componentId = componentId;
            this.oid = oid;
            this.name = name;
            this.includable = includable;
            this.mimeType = mimeType;
        }

        /**
         * Returns a MOD:CA object type OID from a given a componentId
         *
         * @return the corresponding object type id for a given component id
         * or null if the component id is unknown and the object type OID was not found.
         */
        public byte[] getOID() {
            return this.oid;
        }

        /**
         * Returns the object type name for the given componentId
         *
         * @return the object type name for the given componentId
         */
        public String getName() {
            return this.name;
        }

        /**
         * Returns the compontentId for this entry
         *
         * @return the compontentId for this entry
         */
        public byte getComponentId() {
            return this.componentId;
        }

        /**
         * Returns true if this component can be included with an IOB structured field
         *
         * @return true if this component can be included with an IOB structured field
         */
        public boolean isIncludable() {
            return this.includable;
        }

        /**
         * Returns the mime type associated with this object type
         *
         * @return the mime type associated with this object type
         */
        public String getMimeType() {
            return this.mimeType;
        }

        /** {@inheritDoc} */
        public String toString() {
            return this.getName();
        }
    }
}
