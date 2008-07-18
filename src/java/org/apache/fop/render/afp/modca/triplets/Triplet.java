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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.modca.AbstractAFPObject;

/**
 * A simple implementation of a MOD:CA triplet
 */
public class Triplet extends AbstractAFPObject {
    public static final byte CODED_GRAPHIC_CHARACTER_SET_GLOBAL_IDENTIFIER = 0x01;
    
    /** Triplet identifiers */
    public static final byte FULLY_QUALIFIED_NAME = 0x02;    
    public static final byte MAPPING_OPTION = 0x04;  
    public static final byte OBJECT_CLASSIFICATION = 0x10;    
    public static final byte MODCA_INTERCHANGE_SET = 0x18;    
    public static final byte FONT_DESCRIPTOR_SPECIFICATION = 0x1F;    
    public static final byte OBJECT_FUNCTION_SET_SPECIFICATION = 0x21;    
    public static final byte EXTENDED_RESOURCE_LOCAL_IDENTIFIER = 0x22;    
    public static final byte RESOURCE_LOCAL_IDENTIFIER = 0x24;   
    public static final byte RESOURCE_SECTION_NUMBER = 0x25;   
    public static final byte CHARACTER_ROTATION = 0x26;   
    public static final byte OBJECT_BYTE_OFFSET = 0x2D;   
    public static final byte ATTRIBUTE_VALUE = 0x36;   
    public static final byte DESCRIPTOR_POSITION = 0x43;   
    public static final byte MEDIA_EJECT_CONTROL = 0x45;  
    public static final byte PAGE_OVERLAY_CONDITIONAL_PROCESSING = 0x46;  
    public static final byte RESOURCE_USAGE_ATTRIBUTE = 0x47;  
    public static final byte MEASUREMENT_UNITS = 0x4B;  
    public static final byte OBJECT_AREA_SIZE = 0x4C;  
    public static final byte AREA_DEFINITION = 0x4D;  
    public static final byte COLOR_SPECIFICATION = 0x4E;  
    public static final byte ENCODING_SCHEME_ID = 0x50;  
    public static final byte MEDIUM_MAP_PAGE_NUMBER = 0x56;  
    public static final byte OBJECT_BYTE_EXTENT = 0x57;  
    public static final byte OBJECT_STRUCTURED_FIELD_OFFSET = 0x58;  
    public static final byte OBJECT_STRUCTURED_FIELD_EXTENT = 0x59;  
    public static final byte OBJECT_OFFSET = 0x5A;  
    public static final byte FONT_HORIZONTAL_SCALE_FACTOR = 0x5D;  
    public static final byte OBJECT_COUNT = 0x5E;  
    public static final byte OBJECT_DATE_AND_TIMESTAMP = 0x62;  
    public static final byte COMMENT = 0x65;  
    public static final byte MEDIUM_ORIENTATION = 0x68;  
    public static final byte RESOURCE_OBJECT_INCLUDE = 0x6C;  
    public static final byte PRESENTATION_SPACE_RESET_MIXING = 0x70;  
    public static final byte PRESENTATION_SPACE_MIXING_RULE = 0x71;  
    public static final byte UNIVERSAL_DATE_AND_TIMESTAMP = 0x72;  
    public static final byte TONER_SAVER = 0x74;
    public static final byte COLOR_FIDELITY = 0x75;
    public static final byte FONT_FIDELITY = 0x78;
    public static final byte ATTRIBUTE_QUALIFIER = (byte)0x80;
    public static final byte PAGE_POSITION_INFORMATION = (byte)0x81;
    public static final byte PARAMETER_VALUE = (byte)0x82;
    public static final byte PRESENTATION_CONTROL = (byte)0x83;
    public static final byte FONT_RESOLUTION_AND_METRIC_TECHNOLOGY = (byte)0x84;
    public static final byte FINISHING_OPERATION = (byte)0x85;
    public static final byte TEXT_FIDELITY = (byte)0x86;
    public static final byte MEDIA_FIDELITY = (byte)0x87;
    public static final byte FINISHING_FIDELITY = (byte)0x88;
    public static final byte DATA_OBJECT_FONT_DESCRIPTOR = (byte)0x8B;
    public static final byte LOCALE_SELECTOR = (byte)0x8C;
    public static final byte UP3I_FINISHING_OPERATION = (byte)0x8E;
    public static final byte COLOR_MANAGEMENT_RESOURCE_DESCRIPTOR = (byte)0x91;
    public static final byte RENDERING_INTENT = (byte)0x95;
    public static final byte CMR_TAG_FIDELITY = (byte)0x96;
    public static final byte DEVICE_APPEARANCE = (byte)0x97;

    /** the triplet identifier */
    private byte id;

    /** the triplet's data contents */
    private byte[] data;

    /**
     * Main constructor
     * 
     * @param id the triplet identifier (see static definitions above)
     * @param data the data item contained in this triplet
     */
    public Triplet(byte id, byte[] data) {
        this(id);
        setData(data);
    }

    /**
     * Constructor
     * 
     * @param id the triplet identifier (see static definitions above)
     */
    public Triplet(byte id) {
        this.id = id;
    }

    /**
     * Constructor
     * 
     * @param id the triplet identifier (see static definitions above)
     * @param content the content byte data
     */
    public Triplet(byte id, byte content) {
        this(id, new byte[] {content});
    }

    /**
     * Constructor
     * 
     * @param id the triplet identifier (see static definitions above)
     * @param data the data item (in String form) contained in this triplet
     * @throws UnsupportedEncodingException EBCIDIC encoding is not supported
     */
    public Triplet(byte id, String data) throws UnsupportedEncodingException {
        this(id, data.getBytes(AFPConstants.EBCIDIC_ENCODING));
    }
        
    /** {@inheritDoc} */
    public void write(OutputStream os) throws IOException {
        os.write((byte)data.length + 2);
        os.write(id);
        os.write(data);
    }
    
    /**
     * Returns the triplet identifier
     * 
     * @return the triplet identifier
     */
    public byte getId() {
        return this.id;
    }

    /**
     * Sets the data contents of this triplet
     * 
     * @param data the data contents
     */
    protected void setData(byte[] data) {
        this.data = data;
    }
}
