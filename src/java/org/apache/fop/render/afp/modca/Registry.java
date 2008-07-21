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

/* $Id: $ */

package org.apache.fop.render.afp.modca;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * MOD:CA Registry of object types 
 */
public final class Registry {
    /** IOB supported object types */    
    private static final byte COMPID_GIF = 22;
    private static final byte COMPID_JFIF = 23; // jpeg file interchange format 
    private static final byte COMPID_PDF_SINGLE_PAGE = 25; 
    private static final byte COMPID_PCL_PAGE_OBJECT = 34; 
    
    /** IOB unsupported object types */
    private static final byte COMPID_EPS = 13;
    private static final byte COMPID_TIFF = 14;

    /** mime type entry mapping */
    private java.util.Map/*<String, Registry.ObjectType>*/ mimeObjectTypeMap
        = Collections.synchronizedMap(
                new java.util.HashMap/*<String, Registry.ObjectType>*/());

    /** singleton instance */
    private static Registry instance = null;

    /**
     * @return a single instance of an ObjectTypeRegistry
     */
    public static Registry getInstance() {
        synchronized (Registry.class) {
            if (instance == null) {
                instance = new Registry();
            }
        }
        return instance;
    }
    
    private Registry() {
        init();
    }
    
    private void init() {
        mimeObjectTypeMap.put(
                MimeConstants.MIME_EPS,
                new ObjectType(
                        COMPID_EPS,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0D},
                        "Encapsulated Postscript",
                        false,
                        MimeConstants.MIME_EPS
                )
        );
        mimeObjectTypeMap.put(
                MimeConstants.MIME_TIFF,
                new ObjectType(
                        COMPID_TIFF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0E},
                        "TIFF",
                        false,
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
    }

    /**
     * Returns the Registry ObjectType for a given data object info
     * or null if not registered
     * 
     * @param dataObjectInfo the data object info
     * @return the Registry ObjectType for a given data object info
     * or null if not registered
     */
    public Registry.ObjectType getObjectType(DataObjectInfo dataObjectInfo) {
        String mimeType = dataObjectInfo.getMimeType();
        if (mimeType != null) {
            return (Registry.ObjectType)mimeObjectTypeMap.get(mimeType);
        }
        return null; 
    }

    /**
     * Encapsulates a MOD:CA Registry Object Type entry
     */
    public class ObjectType {
        private byte componentId; 
        private byte[] oid;
        private String name;
        private boolean canBeIncluded;
        private String mimeType;

        /**
         * Main constructor
         * 
         * @param componentId the component id of this object type
         * @param oid the object id of this object type
         * @param name the object type name
         * @param canBeIncluded true if this object can be included with an IOB structured field
         * @param mimeType the mime type associated with this object type
         */
        public ObjectType(byte componentId, byte[] oid, String name,
                boolean canBeIncluded, String mimeType) {
            this.name = name;
            this.canBeIncluded = canBeIncluded;
            this.mimeType = mimeType;
            this.componentId = componentId;
            this.oid = oid;
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
        public boolean canBeIncluded() {
            return this.canBeIncluded;
        }

        /**
         * Returns the mime type associated with this object type
         * 
         * @return the mime type associated with this object type
         */
        public String getMimeType() {
            return this.mimeType;
        }

        /**
         * Returns true if this is an image type
         * 
         * @return true if this is an image type
         */
        public boolean isImage() {
            return mimeType == MimeConstants.MIME_TIFF
            || mimeType == MimeConstants.MIME_GIF
            || mimeType == MimeConstants.MIME_JPEG;
        }

        /**
         * Returns true if this is a graphic type
         * 
         * @return true if this is a graphic type
         */
        public boolean isGraphic() {
            return mimeType == MimeConstants.MIME_SVG;
        }

        /** {@inheritDoc} */
        public String toString() {
            return this.getName();
        }
    }
}
