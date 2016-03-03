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

import org.apache.fop.afp.Streamable;
import org.apache.fop.afp.StructuredData;

/**
 * Triplet interface.
 */
public interface Triplet extends Streamable, StructuredData {

    /** maximum length */
    int MAX_LENGTH = 254;

    /** CODED_GRAPHIC_CHARACTER_SET_GLOBAL_IDENTIFIER identifier */
    byte CODED_GRAPHIC_CHARACTER_SET_GLOBAL_IDENTIFIER = 0x01;

    /** FULLY_QUALIFIED_NAME triplet identifier */
    byte FULLY_QUALIFIED_NAME = 0x02;
    /** MAPPING_OPTION triplet identifier */
    byte MAPPING_OPTION = 0x04;
    /** OBJECT_CLASSIFICATION triplet identifier */
    byte OBJECT_CLASSIFICATION = 0x10;
    /** MODCA_INTERCHANGE_SET triplet identifier */
    byte MODCA_INTERCHANGE_SET = 0x18;
    /** FONT_DESCRIPTOR_SPECIFICATION triplet identifier */
    byte FONT_DESCRIPTOR_SPECIFICATION = 0x1F;
    /** OBJECT_FUNCTION_SET_SPECIFICATION triplet identifier */
    byte OBJECT_FUNCTION_SET_SPECIFICATION = 0x21;
    /** EXTENDED_RESOURCE_LOCAL_IDENTIFIER triplet identifier */
    byte EXTENDED_RESOURCE_LOCAL_IDENTIFIER = 0x22;
    /** RESOURCE_LOCAL_IDENTIFIER triplet identifier */
    byte RESOURCE_LOCAL_IDENTIFIER = 0x24;
    /** RESOURCE_SECTION_NUMBER triplet identifier */
    byte RESOURCE_SECTION_NUMBER = 0x25;
    /** CHARACTER_ROTATION triplet identifier */
    byte CHARACTER_ROTATION = 0x26;
    /** OBJECT_BYTE_OFFSET triplet identifier */
    byte OBJECT_BYTE_OFFSET = 0x2D;
    /** ATTRIBUTE_VALUE triplet identifier */
    byte ATTRIBUTE_VALUE = 0x36;
    /** DESCRIPTOR_POSITION triplet identifier */
    byte DESCRIPTOR_POSITION = 0x43;
    /** MEDIA_EJECT_CONTROL triplet identifier */
    byte MEDIA_EJECT_CONTROL = 0x45;
    /** PAGE_OVERLAY_CONDITIONAL_PROCESSING triplet identifier */
    byte PAGE_OVERLAY_CONDITIONAL_PROCESSING = 0x46;
    /** RESOURCE_USAGE_ATTRIBUTE triplet identifier */
    byte RESOURCE_USAGE_ATTRIBUTE = 0x47;
    /** MEASUREMENT_UNITS triplet identifier */
    byte MEASUREMENT_UNITS = 0x4B;
    /** OBJECT_AREA_SIZE triplet identifier */
    byte OBJECT_AREA_SIZE = 0x4C;
    /** AREA_DEFINITION triplet identifier */
    byte AREA_DEFINITION = 0x4D;
    /** COLOR_SPECIFICATION triplet identifier */
    byte COLOR_SPECIFICATION = 0x4E;
    /** ENCODING_SCHEME_ID triplet identifier */
    byte ENCODING_SCHEME_ID = 0x50;
    /** MEDIUM_MAP_PAGE_NUMBER triplet identifier */
    byte MEDIUM_MAP_PAGE_NUMBER = 0x56;
    /** OBJECT_BYTE_EXTENT triplet identifier */
    byte OBJECT_BYTE_EXTENT = 0x57;
    /** OBJECT_STRUCTURED_FIELD_OFFSET triplet identifier */
    byte OBJECT_STRUCTURED_FIELD_OFFSET = 0x58;
    /** OBJECT_STRUCTURED_FIELD_EXTENT triplet identifier */
    byte OBJECT_STRUCTURED_FIELD_EXTENT = 0x59;
    /** OBJECT_OFFSET triplet identifier */
    byte OBJECT_OFFSET = 0x5A;
    /** FONT_HORIZONTAL_SCALE_FACTOR triplet identifier */
    byte FONT_HORIZONTAL_SCALE_FACTOR = 0x5D;
    /** OBJECT_COUNT triplet identifier */
    byte OBJECT_COUNT = 0x5E;
    /** OBJECT_DATE_AND_TIMESTAMP triplet identifier */
    byte OBJECT_DATE_AND_TIMESTAMP = 0x62;
    /** COMMENT triplet identifier */
    byte COMMENT = 0x65;
    /** MEDIUM_ORIENTATION triplet identifier */
    byte MEDIUM_ORIENTATION = 0x68;
    /** RESOURCE_OBJECT_INCLUDE triplet identifier */
    byte RESOURCE_OBJECT_INCLUDE = 0x6C;
    /** PRESENTATION_SPACE_RESET_MIXING triplet identifier */
    byte PRESENTATION_SPACE_RESET_MIXING = 0x70;
    /** PRESENTATION_SPACE_MIXING_RULE triplet identifier */
    byte PRESENTATION_SPACE_MIXING_RULE = 0x71;
    /** UNIVERSAL_DATE_AND_TIMESTAMP triplet identifier */
    byte UNIVERSAL_DATE_AND_TIMESTAMP = 0x72;
    /** TONER_SAVER triplet identifier */
    byte TONER_SAVER = 0x74;
    /** COLOR_FIDELITY triplet identifier */
    byte COLOR_FIDELITY = 0x75;
    /** FONT_FIDELITY triplet identifier */
    byte FONT_FIDELITY = 0x78;
    /** ATTRIBUTE_QUALIFIER triplet identifier */
    byte ATTRIBUTE_QUALIFIER = (byte)0x80;
    /** PAGE_POSITION_INFORMATION triplet identifier */
    byte PAGE_POSITION_INFORMATION = (byte)0x81;
    /** PARAMETER_VALUE triplet identifier */
    byte PARAMETER_VALUE = (byte)0x82;
    /** PRESENTATION_CONTROL triplet identifier */
    byte PRESENTATION_CONTROL = (byte)0x83;
    /** FONT_RESOLUTION_AND_METRIC_TECHNOLOGY triplet identifier */
    byte FONT_RESOLUTION_AND_METRIC_TECHNOLOGY = (byte)0x84;
    /** FINISHING_OPERATION triplet identifier */
    byte FINISHING_OPERATION = (byte)0x85;
    /** TEXT_FIDELITY triplet identifier */
    byte TEXT_FIDELITY = (byte)0x86;
    /** MEDIA_FIDELITY triplet identifier */
    byte MEDIA_FIDELITY = (byte)0x87;
    /** FINISHING_FIDELITY triplet identifier */
    byte FINISHING_FIDELITY = (byte)0x88;
    /** DATA_OBJECT_FONT_DESCRIPTOR triplet identifier */
    byte DATA_OBJECT_FONT_DESCRIPTOR = (byte)0x8B;
    /** LOCALE_SELECTOR triplet identifier */
    byte LOCALE_SELECTOR = (byte)0x8C;
    /** UP3I_FINISHING_OPERATION triplet identifier */
    byte UP3I_FINISHING_OPERATION = (byte)0x8E;
    /** COLOR_MANAGEMENT_RESOURCE_DESCRIPTOR triplet identifier */
    byte COLOR_MANAGEMENT_RESOURCE_DESCRIPTOR = (byte)0x91;
    /** RENDERING_INTENT triplet identifier */
    byte RENDERING_INTENT = (byte)0x95;
    /** CMR_TAG_FIDELITY triplet identifier */
    byte CMR_TAG_FIDELITY = (byte)0x96;
    /** DEVICE_APPEARANCE triplet identifier */
    byte DEVICE_APPEARANCE = (byte)0x97;
}
