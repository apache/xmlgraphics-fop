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
package org.apache.fop.render.afp.modca.triplets;

import java.io.UnsupportedEncodingException;

import org.apache.fop.render.afp.modca.AFPConstants;


/**
 * A Fully Qualified Name triplet enable the identification and referencing of
 * objects using Gloabl Identifiers (GIDs). 
 */
public class FullyQualifiedNameTriplet extends Triplet {
    // Specifies how the GID will be used
    public static final byte TYPE_REPLACE_FIRST_GID_NAME = 0x01;
    public static final byte TYPE_FONT_FAMILY_NAME = 0x07;
    public static final byte TYPE_FONT_TYPEFACE_NAME = 0x08;
    public static final byte TYPE_MODCA_RESOURCE_HIERARCHY_REF = 0x09;
    public static final byte TYPE_BEGIN_RESOURCE_GROUP_REF = 0x0A;
    public static final byte TYPE_ATTRIBUTE_GID = 0x0B;
    public static final byte TYPE_PROCESS_ELEMENT_GID = 0x0C;
    public static final byte TYPE_BEGIN_PAGE_GROUP_REF = 0x0D;
    public static final byte TYPE_MEDIA_TYPE_REF = 0x11;
    public static final byte TYPE_COLOR_MANAGEMENT_RESOURCE_REF = 0x41;
    public static final byte TYPE_DATA_OBJECT_FONT_BASE_FONT_ID = 0x6E;
    public static final byte TYPE_DATA_OBJECT_FONT_LINKED_FONT_ID = 0x7E;
    public static final byte TYPE_BEGIN_DOCUMENT_REF = (byte)0x83;
    public static final byte TYPE_BEGIN_RESOURCE_OBJECT_REF = (byte)0x84;
    public static final byte TYPE_CODE_PAGE_NAME_REF = (byte)0x85;        
    public static final byte TYPE_FONT_CHARSET_NAME_REF = (byte)0x86;
    public static final byte TYPE_BEGIN_PAGE_REF = (byte)0x87;
    public static final byte TYPE_BEGIN_MEDIUM_MAP_REF = (byte)0x8D;
    public static final byte TYPE_CODED_FONT_NAME_REF = (byte)0x8E;        
    public static final byte TYPE_BEGIN_DOCUMENT_INDEX_REF = (byte)0x98;        
    public static final byte TYPE_BEGIN_OVERLAY_REF = (byte)0xB0;
    public static final byte TYPE_DATA_OBJECT_INTERNAL_RESOURCE_REF = (byte)0xBE;
    public static final byte TYPE_INDEX_ELEMENT_GID = (byte)0xCA;
    public static final byte TYPE_OTHER_OBJECT_DATA_REF = (byte)0xCE;
    public static final byte TYPE_DATA_OBJECT_EXTERNAL_RESOURCE_REF = (byte)0xDE;
    
    // GID Format
    public static final byte FORMAT_CHARSTR = (byte)0x00;
    public static final byte FORMAT_OID = (byte)0x10;
    public static final byte FORMAT_URL = (byte)0x20;
    
    private byte[] nameBytes;
    
    /**
     * @return the actual fully qualified name of this triplet
     */
    public byte[] getFullyQualifiedName() {
        return nameBytes;
    }

    /**
     * Main constructor
     * @param type the fully qualified name type
     * @param format the fully qualified name format
     * @param name the fully qualified name
     */
    public FullyQualifiedNameTriplet(byte type, byte format, byte[] name) {
        super(FULLY_QUALIFIED_NAME);
        this.nameBytes = name;
        super.data = new byte[2 + name.length];
        data[0] = type;
        data[1] = format;
        // FQName
        System.arraycopy(name, 0, data, 2, name.length);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        try {
            return new String(nameBytes, AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
}