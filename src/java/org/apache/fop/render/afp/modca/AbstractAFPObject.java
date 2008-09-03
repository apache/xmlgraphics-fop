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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.Streamable;

/**
 * This is the base class for all data stream objects. Page objects are
 * responsible for building and generating the binary datastream in an
 * AFP format.
 */
public abstract class AbstractAFPObject implements Streamable {

    /** Static logging instance */
    protected static final Log log = LogFactory.getLog("org.apache.fop.render.afp.modca");

    /** the structured field class id */
    protected static final byte SF_CLASS = (byte)0xD3;

    private static final byte[] SF_HEADER = new byte[] {
        0x5A, // Structured field identifier
        0x00, // Length byte 1
        0x10, // Length byte 2
        SF_CLASS, // Structured field id byte 1
        (byte) 0x00, // Structured field id byte 2
        (byte) 0x00, // Structured field id byte 3
        0x00, // Flags
        0x00, // Reserved
        0x00, // Reserved
    };

    /**
     * Copies the template structured field data array to the given byte array
     *
     * @param data the structured field data byte array
     * @param type the type code
     * @param category the category code
     */
    protected void copySF(byte[] data, byte type, byte category) {
        copySF(data, SF_CLASS, type, category);
    }

    /**
     * Copies the template structured field data array to the given byte array
     *
     * @param data the structured field data byte array
     * @param clazz the class code
     * @param type the type code
     * @param category the category code
     */
    protected static void copySF(byte[] data, byte clazz, byte type, byte category) {
        System.arraycopy(SF_HEADER, 0, data, 0, SF_HEADER.length);
        data[3] = clazz;
        data[4] = type;
        data[5] = category;
    }

    /**
     * Help method to write a set of AFPObjects to the AFP datastream.
     *
     * @param objects a list of AFPObjects
     * @param os The stream to write to
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    protected void writeObjects(Collection/*<AbstractAFPObject>*/ objects, OutputStream os)
        throws IOException {
        if (objects != null && objects.size() > 0) {
            Iterator it = objects.iterator();
            while (it.hasNext()) {
                Object object = it.next();
                if (object instanceof Streamable) {
                    ((Streamable)object).writeToStream(os);
                    it.remove(); // once written, immediately remove the object
                }
            }
        }
    }

    /** structured field type codes */
    public interface Type {

        /** Attribute */
        byte ATTRIBUTE = (byte)0x0A;

        /** Copy Count */
        byte COPY_COUNT = (byte)0xA2;

        /** Descriptor */
        byte DESCRIPTOR = (byte)0xA6;

        /** Control */
        byte CONTROL = (byte)0xA7;

        /** Begin */
        byte BEGIN = (byte)0xA8;

        /** End */
        byte END = (byte)0xA9;

        /** Map */
        byte MAP = (byte)0xAB;

        /** Position */
        byte POSITION = (byte)0xAC;

        /** Process */
        byte PROCESS = (byte)0xAD;

        /** Include */
        byte INCLUDE = (byte)0xAF;

        /** Table */
        byte TABLE = (byte)0xB0;

        /** Migration */
        byte MIGRATION = (byte)0xB1;

        /** Variable */
        byte VARIABLE = (byte)0xB2;

        /** Link */
        byte LINK = (byte)0xB4;

        /** Data */
        byte DATA = (byte)0xEE;
    }

    /** structured field category codes */
    public interface Category {

        /** Page Segment */
        byte PAGE_SEGMENT = (byte)0x5F;

        /** Object Area */
        byte OBJECT_AREA = (byte)0x6B;

        /** Color Attribute Table */
        byte COLOR_ATTRIBUTE_TABLE = (byte)0x77;

        /** IM Image */
        byte IM_IMAGE = (byte)0x7B;

        /** Medium */
        byte MEDIUM = (byte)0x88;

        /** Coded Font */
        byte CODED_FONT = (byte)0x8A;

        /** Process Element */
        byte PROCESS_ELEMENT = (byte)0x90;

        /** Object Container */
        byte OBJECT_CONTAINER = (byte)0x92;

        /** Presentation Text */
        byte PRESENTATION_TEXT = (byte)0x9B;

        /** Index */
        byte INDEX = (byte)0xA7;

        /** Document */
        byte DOCUMENT = (byte)0xA8;

        /** Page Group */
        byte PAGE_GROUP = (byte)0xAD;

        /** Page */
        byte PAGE = (byte)0xAF;

        /** Graphics */
        byte GRAPHICS = (byte)0xBB;

        /** Data Resource */
        byte DATA_RESOURCE = (byte)0xC3;

        /** Document Environment Group (DEG) */
        byte DOCUMENT_ENVIRONMENT_GROUP = (byte)0xC4;

        /** Resource Group */
        byte RESOURCE_GROUP = (byte)0xC6;

        /** Object Environment Group (OEG) */
        byte OBJECT_ENVIRONMENT_GROUP = (byte)0xC7;

        /** Active Environment Group (AEG) */
        byte ACTIVE_ENVIRONMENT_GROUP = (byte)0xC9;

        /** Medium Map */
        byte MEDIUM_MAP = (byte)0xCC;

        /** Form Map */
        byte FORM_MAP = (byte)0xCD;

        /** Name Resource */
        byte NAME_RESOURCE = (byte)0xCE;

        /** Page Overlay */
        byte PAGE_OVERLAY = (byte)0xD8;

        /** Resource Environment Group (REG) */
        byte RESOURCE_ENVIROMENT_GROUP = (byte)0xD9;

        /** Overlay */
        byte OVERLAY = (byte)0xDF;

        /** Data Suppression */
        byte DATA_SUPRESSION = (byte)0xEA;

        /** Bar Code */
        byte BARCODE = (byte)0xEB;

        /** No Operation */
        byte NO_OPERATION = (byte)0xEE;

        /** Image */
        byte IMAGE = (byte)0xFB;
    }

}

