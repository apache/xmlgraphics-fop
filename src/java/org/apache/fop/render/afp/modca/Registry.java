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

import org.apache.commons.logging.LogFactory;

/**
 * MOD:CA object type registry
 */
public final class Registry {
    
    /** IOB supported object types */
    public static final byte IOCA_FS10 = 5;
    public static final byte IOCA_FS11 = 11;
    public static final byte IOCA_FS45 = 12;
    public static final byte DIB_WIN = 17; //device independent bitmap
    public static final byte DIB_OS2 = 18;
    public static final byte PCX = 19;
    public static final byte GIF = 22;
    public static final byte JFIF = 23; // jpeg file interchange format 
    public static final byte PDF_SINGLE_PAGE = 25; 
    public static final byte PCL_PAGE_OBJECT = 34; 
    public static final byte EPS_TRANS = 48;
    public static final byte PDF_SINGLE_PAGE_TRANS = 49;
    public static final byte JPEG2000 = 58;
    
    /** IOB unsupported object types */
    public static final byte EPS = 13;
    public static final byte TIFF = 14;
    public static final byte COM_SETUP_FILE = 15;
    public static final byte TAPE_LABEL_SETUP_FILE = 16;
    public static final byte CMT = 20; // color mapping table
    public static final byte ANACOMP_CONTROL_RECORD = 24; 
    public static final byte PDF_RESOURCE_OBJECT = 26; 
    public static final byte IOCA_FS42 = 45;
    public static final byte RESIDENT_COLOR_PROFILE = 46;
    public static final byte IOCA_FS45_TILE_RESOURCE = 47;
    public static final byte FONT = 51;
    public static final byte FONT_COLLECTION = 53;
    public static final byte RESOURCE_ACCESS_TABLE = 54; 
    public static final byte IOCA_FS40 = 55;
    public static final byte UP3I_PRINT_DATA = 56;
    public static final byte COLOR_MANAGEMENT_RESOURCE = 57;

    
    /** internal mapping array references */
    private static final int COMPONENT_ID = 0;
    private static final int OBJECT_TYPE_NAME = 1;
    private static final int OBJECT_ID = 2;
    private static final int CAN_BE_INCLUDED = 3;

    private byte[][][] componentIdMap;

    private static Registry instance = null;

    /**
     * @return a single instance of Registry
     */
    public static Registry getInstance() {
        synchronized (instance) {
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
        try {
            componentIdMap = new byte[][][] {
            /* object type id (OID), object type name, compontent Id, can be included with IOB?, */ 
            {
                {IOCA_FS10},
                "IOCA FS10".getBytes(AFPConstants.EBCIDIC_ENCODING),
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x05},
                {1}
            },
            {
                {IOCA_FS11},
                "IOCA FS11".getBytes(AFPConstants.EBCIDIC_ENCODING),
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0B},
                {1}
            },
            {
                {IOCA_FS45},
                "IOCA FS45".getBytes(AFPConstants.EBCIDIC_ENCODING),
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0C},
                {1}
            },
            {
                {EPS},
                "Encapsulated Postscript".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0D},
                {0}
            },
            {
                {TIFF},
                "TIFF".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0E},
                {0}
            },
            {
                {COM_SETUP_FILE},
                "COM setup".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x0F},
                {0}
            },
            {
                {TAPE_LABEL_SETUP_FILE},
                "Tape Label setup".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x10},
                {0}
            },
            {
                {DIB_WIN},
                "DIB, Windows Version".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x11},
                {1}
            },
            {
                {DIB_OS2},
                "DIB, OS/2 PM Version".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x12},
                {1}
            },
            {
                {PCX},
                "PCX".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x13},
                {1}
            },
            {
                {CMT},
                "Color Mapping Table (CMT)".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x14},
                {0}
            },
            {
                {GIF},
                "GIF".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x16},
                {1}
            },
            {
                {JFIF},
                "JFIF".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x17},
                {1}
            },
            {
                {ANACOMP_CONTROL_RECORD},
                "AnaStak Control Record".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x18},
                {0}
            },
            {
                {PDF_SINGLE_PAGE},
                "PDF Single-page Object".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x19},
                {1}
            },
            {
                {PDF_RESOURCE_OBJECT},
                "PDF Resource Object".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x1A},
                {0}
            },
            {
                {PCL_PAGE_OBJECT},
                "PCL Page Object".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x22},
                {1}
            },
            {
                {IOCA_FS42},
                "IOCA FS42".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x2D},
                {0}
            },
            {
                {RESIDENT_COLOR_PROFILE},
                "Resident Color Profile".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x2E},
                {0}
            },
            {
                {IOCA_FS45_TILE_RESOURCE},
                "IOCA FS45".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x2E},
                {0}
            },
            {
                {EPS_TRANS},
                "EPS with Transparency".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x30},
                {1}
            },
            {
                {PDF_SINGLE_PAGE_TRANS},
                "PDF with Transparency".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x31},
                {1}
            },
            {
                {FONT},
                "TrueType/OpenType Font".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x33},
                {0}
            },
            {
                {FONT_COLLECTION},
                "TrueType/OpenType Font Collection".getBytes(
                        AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x35},
                {0}
            },
            {
                {RESOURCE_ACCESS_TABLE},
                "Resource Access Table".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x36},
                {0}
            },
            {
                {IOCA_FS40},
                "IOCA FS40".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x37},
                {0}
            },
            {
                {UP3I_PRINT_DATA},
                "IP3i Print Data".getBytes(AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x38},
                {0}
            },
            {
                {COLOR_MANAGEMENT_RESOURCE},
                "Color Management Resource (CMR)".getBytes(
                        AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x39},
                {0}
            },
            {
                {JPEG2000},
                "JPEG2000 (JP2) File Format".getBytes(
                        AFPConstants.EBCIDIC_ENCODING),                
                {0x06, 0x07, 0x2B, 0x12, 0x00, 0x04, 0x01, 0x01, 0x3A},
                {1}
            }
            };
        } catch (UnsupportedEncodingException e) {
            // should never happen!
            LogFactory.getLog("org.apache.fop.render.afp.modca.Registry").error(e.getMessage());
        }        
    }

    private byte[][][] getComponentIdMap() {
        if (componentIdMap == null) {
            init();
        }
        return componentIdMap;
    }

    private byte[][] getMapData(byte compontentId) {
        getComponentIdMap();
        for (int i = 0; i < componentIdMap.length; i++) {
            if (compontentId == componentIdMap[i][0][0]) {
                return componentIdMap[i];
            }
        }
        return null;
    }

    /**
     * Returns a registry Entry for a given componentId
     * @param compontentId a compontent id
     * @return the registry entry for a give componentId
     */
    public Entry getEntry(byte compontentId) {
        byte[][] data = getMapData(compontentId);
        if (data != null) {
            return new Registry.Entry(data);
        }
        return null;
    }

    /**
     * Encapsulates a MOD:CA Registry Entry
     */
    public final class Entry {
        private byte[][] data;

        /**
         * Main constructor
         * @param the map data structure array
         */
        private Entry(byte[][] data) {
            this.data = data;
        }
        
        /**
         * Returns a MOD:CA object type OID from a given a componentId
         * @return the corresponding object type id for a given component id
         * or null if the component id is unknown and the object type OID was not found.
         */
        public byte[] getOID() {
            return data[OBJECT_ID];
        }

        /**
         * @return the object type name for the given componentId 
         */
        public byte[] getObjectTypeName() {
            return data[OBJECT_TYPE_NAME];
        }

        /**
         * @return the compontentId for this entry
         */
        public byte[] getComponentId() {
            return data[COMPONENT_ID];
        }

        /**
         * @return true if this component can be included with an IOB structured field
         */
        public boolean canBeIncluded() {
            return data[CAN_BE_INCLUDED][0] == 1;
        }
    }
}
