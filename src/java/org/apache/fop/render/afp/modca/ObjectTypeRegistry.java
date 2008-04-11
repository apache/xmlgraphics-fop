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

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.ImageObjectInfo;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * MOD:CA object type registry
 */
public final class ObjectTypeRegistry {
    /** IOB supported object types */    
    private static final byte COMPID_GIF = 22;
    private static final byte COMPID_JFIF = 23; // jpeg file interchange format 
    private static final byte COMPID_PDF_SINGLE_PAGE = 25; 
    private static final byte COMPID_PCL_PAGE_OBJECT = 34; 
    
    /** IOB unsupported object types */
    private static final byte COMPID_EPS = 13;
    private static final byte COMPID_TIFF = 14;

    /** mime type entry mapping */
    private java.util.Map/*<String, ObjectTypeRegistry.Entry>*/ mimeEntryMap
        = Collections.synchronizedMap(
                Collections.unmodifiableMap(
                        new java.util.HashMap/*<String, ObjectTypeRegistry.Entry>*/()));

    private static ObjectTypeRegistry instance = null;

    /**
     * @return a single instance of Registry
     */
    public static ObjectTypeRegistry getInstance() {
        synchronized (instance) {
            if (instance == null) {
                instance = new ObjectTypeRegistry();
            }            
        }
        return instance;
    }
    
    private ObjectTypeRegistry() {
        init();
    }
    
    private void init() {
        mimeEntryMap.put(
                MimeConstants.MIME_EPS,
                new ObjectType(
                        COMPID_EPS,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0D},
                        "Encapsulated Postscript",
                        false
                )
        );
        mimeEntryMap.put(
                MimeConstants.MIME_TIFF,
                new ObjectType(
                        COMPID_TIFF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0E},
                        "TIFF",
                        false
                )
        );
        mimeEntryMap.put(
                MimeConstants.MIME_GIF,
                new ObjectType(
                        COMPID_GIF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x16},
                        "GIF",
                        true
                )
        );
        mimeEntryMap.put(
                MimeConstants.MIME_JPEG,
                new ObjectType(
                        COMPID_JFIF,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x17},
                        "JFIF",
                        true
                )
        );
        mimeEntryMap.put(MimeConstants.MIME_PDF,
                new ObjectType(
                        COMPID_PDF_SINGLE_PAGE,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x19},
                        "PDF Single-page Object",
                        true
                )
        );         
        mimeEntryMap.put(
                MimeConstants.MIME_PCL,
                new ObjectType(
                        COMPID_PCL_PAGE_OBJECT,
                        new byte[] {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x22},
                        "PCL Page Object",
                        true
                )
        );
    }

    /**
     * Returns the registry Entry for a given data object info
     * 
     * @param info
     *            the data object info
     * @return the registry Entry for a given data object info
     */
    public ObjectType getObjectType(DataObjectInfo info) {
        ObjectType entry = null;
        if (info instanceof ImageObjectInfo) {
            ImageObjectInfo imageInfo = (ImageObjectInfo)info;
            String mimeType = imageInfo.getMimeType();
            entry = (ObjectType)mimeEntryMap.get(mimeType);
        }
        return entry;
    }
    
    /**
     * Encapsulates a MOD:CA Registry Object Type entry
     */
    public class ObjectType {
        private byte componentId; 
        private byte[] oid;
        private byte[] name;
        private boolean canBeIncluded;
        
        /**
         * Main constructor
         * @param componentId the component id of this object type
         * @param oid the object id of this object type
         * @param objectTypeName the object type name
         * @param canBeIncluded true if this object can be included with an IOB structured field
         */
        public ObjectType(byte componentId, byte[] oid, String objectTypeName,
                boolean canBeIncluded) {
            this.componentId = componentId;
            this.oid = oid;
            try {
                this.name = objectTypeName.getBytes(AFPConstants.EBCIDIC_ENCODING);
            } catch (UnsupportedEncodingException e) {
                // should never happen!
                LogFactory.getLog("org.apache.fop.render.afp.modca.Registry.Entry").error(
                        "character encoding error occurred on componentId "
                        + componentId
                        + " : "
                        + e.getMessage());
            }
            this.canBeIncluded = canBeIncluded;
        }
                
        /**
         * Returns a MOD:CA object type OID from a given a componentId
         * @return the corresponding object type id for a given component id
         * or null if the component id is unknown and the object type OID was not found.
         */
        public byte[] getOID() {
            return this.oid;
        }

        /**
         * @return the object type name for the given componentId 
         */
        public byte[] getName() {
            return this.name;
        }

        /**
         * @return the compontentId for this entry
         */
        public byte getComponentId() {
            return this.componentId;
        }

        /**
         * @return true if this component can be included with an IOB structured field
         */
        public boolean canBeIncluded() {
            return this.canBeIncluded;
        }
    }
}
