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

package org.apache.fop.afp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.modca.AbstractAFPObject.Category;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;

/**
 * TODO better docs
 * Utility for AFP resource handling
 *
 *
 * A utility class to read structured fields from a MO:DCA document. Each
 * component of a mixed object document is explicitly defined and delimited
 * in the data. This is accomplished through the use of MO:DCA data structures,
 * called structured fields. Structured fields are used to envelop document
 * components and to provide commands and information to applications using
 * the data. Structured fields may contain one or more parameters. Each
 * parameter provides one value from a set of values defined by the architecture.
 * <p/>
 * MO:DCA structured fields consist of two parts: an introducer that identifies
 * the length and type of the structured field, and data that provides the
 * structured field's effect. The data is contained in a set of parameters,
 * which can consist of other data structures and data elements. The maximum
 * length of a structured field is 32767 bytes.
 * <p/>
 */
public final class AFPResourceUtil {

    private static final byte TYPE_CODE_BEGIN = (byte)(0xA8 & 0xFF);
    private static final byte TYPE_CODE_END = (byte)(0xA9 & 0xFF);

    private static final Log LOG = LogFactory.getLog(AFPResourceUtil.class);

    private AFPResourceUtil() {
        //nop
    }

    /**
     * Get the next structured field as identified by the identifier
     * parameter (this must be a valid MO:DCA structured field).
     * @param identifier the three byte identifier
     * @param inputStream the inputStream
     * @throws IOException if an I/O exception occurred
     * @return the next structured field or null when there are no more
     */
    public static byte[] getNext(byte[] identifier, InputStream inputStream) throws IOException {
        MODCAParser parser = new MODCAParser(inputStream);
        while (true) {
            UnparsedStructuredField field = parser.readNextStructuredField();
            if (field == null) {
                return null;
            }
            if (field.getSfClassCode() == identifier[0]
                    && field.getSfTypeCode() == identifier[1]
                    && field.getSfCategoryCode() == identifier[2]) {
                return field.getCompleteFieldAsBytes();
            }
        }
    }

    private static String getResourceName(UnparsedStructuredField field)
            throws UnsupportedEncodingException {
        //The first 8 bytes of the field data represent the resource name
        byte[] nameBytes = new byte[8];
        System.arraycopy(field.getData(), 0, nameBytes, 0, 8);
        String asciiName;
        asciiName = new String(nameBytes, AFPConstants.EBCIDIC_ENCODING);
        return asciiName;
    }

    /**
     * Copy a complete resource file to a given {@link OutputStream}.
     * @param in external resource input
     * @param out output destination
     * @throws IOException if an I/O error occurs
     */
    public static void copyResourceFile(final InputStream in, OutputStream out)
                throws IOException {
        MODCAParser parser = new MODCAParser(in);
        while (true) {
            UnparsedStructuredField field = parser.readNextStructuredField();
            if (field == null) {
                break;
            }
            out.write(MODCAParser.CARRIAGE_CONTROL_CHAR);
            field.writeTo(out);
        }
    }

    /**
     * Copy a named resource to a given {@link OutputStream}. The MO:DCA fields read from the
     * {@link InputStream} are scanned for the resource with the given name.
     * @param name name of structured field
     * @param in external resource input
     * @param out output destination
     * @throws IOException if an I/O error occurs
     */
    public static void copyNamedResource(String name,
            final InputStream in, final OutputStream out) throws IOException {
        final MODCAParser parser = new MODCAParser(in);
        Collection resourceNames = new java.util.HashSet();

        //Find matching "Begin" field
        final UnparsedStructuredField fieldBegin;
        while (true) {
            UnparsedStructuredField field = parser.readNextStructuredField();
            if (field == null) {
                throw new IOException("Requested resource '" + name
                        + "' not found. Encountered resource names: " + resourceNames);
            }

            if (field.getSfTypeCode() != TYPE_CODE_BEGIN) { //0xA8=Begin
                continue; //Not a "Begin" field
            }
            String resourceName = getResourceName(field);
            resourceNames.add(resourceName);
            if (resourceName.equals(name)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Start of requested structured field found:\n"
                            + field);
                }
                fieldBegin = field;
                break; //Name doesn't match
            }
        }

        //Decide whether the resource file has to be wrapped in a resource object
        boolean wrapInResource;
        if (fieldBegin.getSfCategoryCode() == Category.PAGE_SEGMENT) {
            //A naked page segment must be wrapped in a resource object
            wrapInResource = true;
        } else if (fieldBegin.getSfCategoryCode() == Category.NAME_RESOURCE) {
            //A resource object can be copied directly
            wrapInResource = false;
        } else {
            throw new IOException("Cannot handle resource: " + fieldBegin);
        }

        //Copy structured fields (wrapped or as is)
        if (wrapInResource) {
            ResourceObject resourceObject =  new ResourceObject(name) {
                protected void writeContent(OutputStream os) throws IOException {
                    copyStructuredFields(name, fieldBegin, parser, out);
                }
            };
            resourceObject.setType(ResourceObject.TYPE_PAGE_SEGMENT);
            resourceObject.writeToStream(out);
        } else {
            copyStructuredFields(name, fieldBegin, parser, out);
        }
    }

    private static void copyStructuredFields(String name, UnparsedStructuredField fieldBegin,
            MODCAParser parser, OutputStream out) throws IOException {
        boolean inRequestedResource;

        //The "Begin" field first
        out.write(MODCAParser.CARRIAGE_CONTROL_CHAR);
        fieldBegin.writeTo(out);
        UnparsedStructuredField field;

        //Then the rest of the fields until the corresponding "End" field
        inRequestedResource = true;
        do {
            field = parser.readNextStructuredField();
            if (field == null) {
                break; //Unexpected EOF
            }

            if (field.getSfTypeCode() == TYPE_CODE_END) {
                String resourceName = getResourceName(field);
                if (resourceName.equals(name)) {
                    inRequestedResource = false; //Signal end of loop
                }
            }
            out.write(MODCAParser.CARRIAGE_CONTROL_CHAR);
            field.writeTo(out);
        } while (inRequestedResource);
        if (inRequestedResource) {
            throw new IOException("Ending structured field not found for resource " + name);
        }
    }

}
