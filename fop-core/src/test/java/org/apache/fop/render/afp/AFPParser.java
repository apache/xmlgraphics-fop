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
package org.apache.fop.render.afp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.ptoca.PtocaBuilder;

import junit.framework.Assert;

public class AFPParser {
    private boolean readText;
    public AFPParser(boolean readText) {
        this.readText = readText;
    }

    public void read(InputStream bis, StringBuilder sb) throws IOException {
        while (bis.available() > 0) {
            readField(bis, sb);
        }
    }

    private void readField(InputStream bis, StringBuilder sb) throws IOException {
        bis.read();
        int len = getLength(bis.read(), bis.read());
        byte[] field = new byte[len - 2];
        bis.read(field);
        InputStream fieldStream = new ByteArrayInputStream(field);
        fieldStream.read();
        byte type = (byte) fieldStream.read();
        byte category = (byte) fieldStream.read();
        fieldStream.skip(3);
        String typeStr = TYPE_MAP.get(type & 0xFF);
        String catStr = CATEGORY_MAP.get(category & 0xFF);
        if (typeStr != null && catStr != null) {
            sb.append(typeStr + " " + catStr);
            if (typeStr.equals("BEGIN") || typeStr.equals("END")) {
                byte[] name = new byte[8];
                fieldStream.read(name);
                sb.append(" " + new String(name, AFPConstants.EBCIDIC_ENCODING));
                fieldStream.skip(2);
                readTriplet(fieldStream, sb);
            } else if (typeStr.equals("MAP")) {
                fieldStream.skip(2);
                readTriplet(fieldStream, sb);
            } else if (typeStr.equals("DESCRIPTOR") && catStr.equals("OBJECT_AREA")) {
                readTriplet(fieldStream, sb);
            } else if (typeStr.equals("DATA") && catStr.equals("PRESENTATION_TEXT") && readText) {
                readData(fieldStream, sb);
            }
            sb.append("\n");
        }
    }

    private void readData(InputStream bis, StringBuilder sb) throws IOException {
        Assert.assertEquals(bis.read(), 0x2B);
        Assert.assertEquals(bis.read(), 0xD3);
        while (bis.available() > 0) {
            int len = bis.read();
            int functionType = bis.read();

            sb.append(" " + PTOCA_MAP.get(functionType));

            if ("TRN".equals(PTOCA_MAP.get(functionType))) {
                byte[] data = new byte[len - 2];
                bis.read(data);
                sb.append(" " + new String(data, "UTF-16BE"));
            } else {
                bis.skip(len - 2);
            }
        }
    }

    private void readTriplet(InputStream des, StringBuilder sb) throws IOException {
        if (des.available() > 0) {
            sb.append(" Triplets: ");
        }
        while (des.available() > 0) {
            int len2 = des.read();
            int id = des.read();
            int b = id & 0xFF;
            if (TRIPLET_MAP.containsKey(b)) {
                sb.append(TRIPLET_MAP.get(b) + ",");
            } else {
                sb.append(String.format("0x%02X,", b));
            }
            des.skip(len2 - 2);
        }
    }

    private int getLength(int a, int b) {
        return (a * 256) + b;
    }

    private static final Map<Integer, String> TYPE_MAP = new HashMap<Integer, String>();
    private static final Map<Integer, String> CATEGORY_MAP = new HashMap<Integer, String>();
    private static final Map<Integer, String> TRIPLET_MAP = new HashMap<Integer, String>();
    private static final Map<Integer, String> PTOCA_MAP = new HashMap<Integer, String>();
    static {
        PTOCA_MAP.put(0xC2 | PtocaBuilder.CHAIN_BIT, "SIA");
        PTOCA_MAP.put(0xC4 | PtocaBuilder.CHAIN_BIT, "SVI");
        PTOCA_MAP.put(0xC6 | PtocaBuilder.CHAIN_BIT, "AMI");
        PTOCA_MAP.put(0xC8 | PtocaBuilder.CHAIN_BIT, "RMI");
        PTOCA_MAP.put(0xD2 | PtocaBuilder.CHAIN_BIT, "AMB");
        PTOCA_MAP.put(0xDA | PtocaBuilder.CHAIN_BIT, "TRN");
        PTOCA_MAP.put(0xE4 | PtocaBuilder.CHAIN_BIT, "DIR");
        PTOCA_MAP.put(0xE6 | PtocaBuilder.CHAIN_BIT, "DBR");
        PTOCA_MAP.put(0x80 | PtocaBuilder.CHAIN_BIT, "SEC");
        PTOCA_MAP.put(0xF0 | PtocaBuilder.CHAIN_BIT, "SCFL");
        PTOCA_MAP.put(0xF6 | PtocaBuilder.CHAIN_BIT, "STO");
        PTOCA_MAP.put(0xF8 | PtocaBuilder.CHAIN_BIT, "NOP");

        TYPE_MAP.put(0xA0, "ATTRIBUTE");
        TYPE_MAP.put(0xA2, "COPY_COUNT");
        TYPE_MAP.put(0xA6, "DESCRIPTOR");
        TYPE_MAP.put(0xA7, "CONTROL");
        TYPE_MAP.put(0xA8, "BEGIN");
        TYPE_MAP.put(0xA9, "END");
        TYPE_MAP.put(0xAB, "MAP");
        TYPE_MAP.put(0xAC, "POSITION");
        TYPE_MAP.put(0xAD, "PROCESS");
        TYPE_MAP.put(0xAF, "INCLUDE");
        TYPE_MAP.put(0xB0, "TABLE");
        TYPE_MAP.put(0xB1, "MIGRATION");
        TYPE_MAP.put(0xB2, "VARIABLE");
        TYPE_MAP.put(0xB4, "LINK");
        TYPE_MAP.put(0xEE, "DATA");

        CATEGORY_MAP.put(0x5F, "PAGE_SEGMENT");
        CATEGORY_MAP.put(0x6B, "OBJECT_AREA");
        CATEGORY_MAP.put(0x77, "COLOR_ATTRIBUTE_TABLE");
        CATEGORY_MAP.put(0x7B, "IM_IMAGE");
        CATEGORY_MAP.put(0x88, "MEDIUM");
        CATEGORY_MAP.put(0x8A, "CODED_FONT");
        CATEGORY_MAP.put(0x90, "PROCESS_ELEMENT");
        CATEGORY_MAP.put(0x92, "OBJECT_CONTAINER");
        CATEGORY_MAP.put(0x9B, "PRESENTATION_TEXT");
        CATEGORY_MAP.put(0xA7, "INDEX");
        CATEGORY_MAP.put(0xA8, "DOCUMENT");
        CATEGORY_MAP.put(0xAD, "PAGE_GROUP");
        CATEGORY_MAP.put(0xAF, "PAGE");
        CATEGORY_MAP.put(0xBB, "GRAPHICS");
        CATEGORY_MAP.put(0xC3, "DATA_RESOURCE");
        CATEGORY_MAP.put(0xC4, "DOCUMENT_ENVIRONMENT_GROUP");
        CATEGORY_MAP.put(0xC6, "RESOURCE_GROUP");
        CATEGORY_MAP.put(0xC7, "OBJECT_ENVIRONMENT_GROUP");
        CATEGORY_MAP.put(0xC9, "ACTIVE_ENVIRONMENT_GROUP");
        CATEGORY_MAP.put(0xCC, "MEDIUM_MAP");
        CATEGORY_MAP.put(0xCD, "FORM_MAP");
        CATEGORY_MAP.put(0xCE, "NAME_RESOURCE");
        CATEGORY_MAP.put(0xD8, "PAGE_OVERLAY");
        CATEGORY_MAP.put(0xD9, "RESOURCE_ENVIROMENT_GROUP");
        CATEGORY_MAP.put(0xDF, "OVERLAY");
        CATEGORY_MAP.put(0xEA, "DATA_SUPRESSION");
        CATEGORY_MAP.put(0xEB, "BARCODE");
        CATEGORY_MAP.put(0xEE, "NO_OPERATION");
        CATEGORY_MAP.put(0xFB, "IMAGE");

        TRIPLET_MAP.put(0x02, "FULLY_QUALIFIED_NAME");
        TRIPLET_MAP.put(0x04, "MAPPING_OPTION");
        TRIPLET_MAP.put(0x10, "OBJECT_CLASSIFICATION");
        TRIPLET_MAP.put(0x18, "MODCA_INTERCHANGE_SET");
        TRIPLET_MAP.put(0x1F, "FONT_DESCRIPTOR_SPECIFICATION");
        TRIPLET_MAP.put(0x21, "OBJECT_FUNCTION_SET_SPECIFICATION");
        TRIPLET_MAP.put(0x22, "EXTENDED_RESOURCE_LOCAL_IDENTIFIER");
        TRIPLET_MAP.put(0x24, "RESOURCE_LOCAL_IDENTIFIER");
        TRIPLET_MAP.put(0x25, "RESOURCE_SECTION_NUMBER");
        TRIPLET_MAP.put(0x26, "CHARACTER_ROTATION");
        TRIPLET_MAP.put(0x2D, "OBJECT_BYTE_OFFSET");
        TRIPLET_MAP.put(0x36, "ATTRIBUTE_VALUE");
        TRIPLET_MAP.put(0x43, "DESCRIPTOR_POSITION");
        TRIPLET_MAP.put(0x45, "MEDIA_EJECT_CONTROL");
        TRIPLET_MAP.put(0x46, "PAGE_OVERLAY_CONDITIONAL_PROCESSING");
        TRIPLET_MAP.put(0x47, "RESOURCE_USAGE_ATTRIBUTE");
        TRIPLET_MAP.put(0x4B, "MEASUREMENT_UNITS");
        TRIPLET_MAP.put(0x4C, "OBJECT_AREA_SIZE");
        TRIPLET_MAP.put(0x4D, "AREA_DEFINITION");
        TRIPLET_MAP.put(0x4E, "COLOR_SPECIFICATION");
        TRIPLET_MAP.put(0x50, "ENCODING_SCHEME_ID");
        TRIPLET_MAP.put(0x56, "MEDIUM_MAP_PAGE_NUMBER");
        TRIPLET_MAP.put(0x57, "OBJECT_BYTE_EXTENT");
        TRIPLET_MAP.put(0x58, "OBJECT_STRUCTURED_FIELD_OFFSET");
        TRIPLET_MAP.put(0x59, "OBJECT_STRUCTURED_FIELD_EXTENT");
        TRIPLET_MAP.put(0x5A, "OBJECT_OFFSET");
        TRIPLET_MAP.put(0x5D, "FONT_HORIZONTAL_SCALE_FACTOR");
        TRIPLET_MAP.put(0x5E, "OBJECT_COUNT");
        TRIPLET_MAP.put(0x62, "OBJECT_DATE_AND_TIMESTAMP");
        TRIPLET_MAP.put(0x65, "COMMENT");
        TRIPLET_MAP.put(0x68, "MEDIUM_ORIENTATION");
        TRIPLET_MAP.put(0x6C, "RESOURCE_OBJECT_INCLUDE");
        TRIPLET_MAP.put(0x70, "PRESENTATION_SPACE_RESET_MIXING");
        TRIPLET_MAP.put(0x71, "PRESENTATION_SPACE_MIXING_RULE");
        TRIPLET_MAP.put(0x72, "UNIVERSAL_DATE_AND_TIMESTAMP");
        TRIPLET_MAP.put(0x74, "TONER_SAVER");
        TRIPLET_MAP.put(0x75, "COLOR_FIDELITY");
        TRIPLET_MAP.put(0x78, "FONT_FIDELITY");
        TRIPLET_MAP.put(0x80, "ATTRIBUTE_QUALIFIER");
        TRIPLET_MAP.put(0x81, "PAGE_POSITION_INFORMATION");
        TRIPLET_MAP.put(0x82, "PARAMETER_VALUE");
        TRIPLET_MAP.put(0x83, "PRESENTATION_CONTROL");
        TRIPLET_MAP.put(0x84, "FONT_RESOLUTION_AND_METRIC_TECHNOLOGY");
        TRIPLET_MAP.put(0x85, "FINISHING_OPERATION");
        TRIPLET_MAP.put(0x86, "TEXT_FIDELITY");
        TRIPLET_MAP.put(0x87, "MEDIA_FIDELITY");
        TRIPLET_MAP.put(0x88, "FINISHING_FIDELITY");
        TRIPLET_MAP.put(0x8B, "DATA_OBJECT_FONT_DESCRIPTOR");
        TRIPLET_MAP.put(0x8C, "LOCALE_SELECTOR");
        TRIPLET_MAP.put(0x8E, "UP3I_FINISHING_OPERATION");
        TRIPLET_MAP.put(0x91, "COLOR_MANAGEMENT_RESOURCE_DESCRIPTOR");
        TRIPLET_MAP.put(0x95, "RENDERING_INTENT");
        TRIPLET_MAP.put(0x96, "CMR_TAG_FIDELITY");
        TRIPLET_MAP.put(0x97, "DEVICE_APPEARANCE");
    }
}
