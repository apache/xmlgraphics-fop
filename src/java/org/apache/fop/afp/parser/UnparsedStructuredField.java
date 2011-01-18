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

package org.apache.fop.afp.parser;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

import org.apache.commons.io.HexDump;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an unparsed (generic) AFP structured field.
 */
public class UnparsedStructuredField {

    private static final Log LOG = LogFactory.getLog(UnparsedStructuredField.class);

    private static final int INTRODUCER_LENGTH = 8;

    private short sfLength;
    private byte sfClassCode;
    private byte sfTypeCode;
    private byte sfCategoryCode;
    private boolean sfiExtensionPresent;
    private boolean sfiSegmentedData;
    private boolean sfiPaddingPresent;
    private short extLength;
    private byte[] introducerData;
    private byte[] extData;
    private byte[] data;

    /**
     * Default constructor.
     */
    public UnparsedStructuredField() {
        //nop
    }

    /**
     * Reads a structured field from a {@link DataInputStream}. The resulting object can be
     * further interpreted be follow-up code.
     * @param din the stream to read from
     * @return the generic structured field
     * @throws IOException if an I/O error occurs
     */
    public static UnparsedStructuredField readStructuredField(DataInputStream din)
            throws IOException {
        UnparsedStructuredField sf = new UnparsedStructuredField();

        //Read introducer as byte array to preserve any data not parsed below
        din.mark(INTRODUCER_LENGTH);
        sf.introducerData = new byte[INTRODUCER_LENGTH]; //Length of introducer
        din.readFully(sf.introducerData);
        din.reset();

        //Parse the introducer
        short len;
        try {
            len = din.readShort();
        } catch (EOFException eof) {
            return null;
        }
        sf.sfLength = len;
        sf.sfClassCode = din.readByte();
        sf.sfTypeCode = din.readByte();
        sf.sfCategoryCode = din.readByte();

        //Flags
        byte f = din.readByte();
        sf.sfiExtensionPresent = (f & 0x01) != 0;
        sf.sfiSegmentedData = (f & 0x04) != 0;
        sf.sfiPaddingPresent = (f & 0x10) != 0;
        din.skip(2); //Reserved

        int dataLength = sf.sfLength - INTRODUCER_LENGTH;

        //Handle optional extension
        if (sf.sfiExtensionPresent) {
            sf.extLength = (short)(((short)din.readByte()) & 0xFF);
            if (sf.extLength > 0) {
                sf.extData = new byte[sf.extLength - 1];
                din.readFully(sf.extData);
                dataLength -= sf.extLength;
            }
        }

        //Read payload
        sf.data = new byte[dataLength];
        din.readFully(sf.data);

        if (LOG.isTraceEnabled()) {
            LOG.trace(sf);
        }

        return sf;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer("Structured Field: ");
        sb.append(Integer.toHexString(getSfTypeID()).toUpperCase());
        sb.append(", len=");
        sb.append(new DecimalFormat("00000").format(getSfLength()));
        sb.append(" ").append(getTypeCodeAsString());
        sb.append(" ").append(getCategoryCodeAsString());
        if (isSfiExtensionPresent()) {
            sb.append(", SFI extension present");
        }
        if (isSfiSegmentedData()) {
            sb.append(", segmented data");
        }
        if (isSfiPaddingPresent()) {
            sb.append(", with padding");
        }
        return sb.toString();
    }

    /**
     * Dump the structured field as hex data to the given {@link PrintStream}.
     * @param out the {@link PrintStream} to dump to
     * @throws IOException if an I/O error occurs
     */
    public void dump(PrintStream out) throws IOException {
        out.println(toString());
        HexDump.dump(getData(), 0, out, 0);
    }

    /**
     * Dump the structured field as hex data to <code>System.out</code>.
     * @throws IOException if an I/O error occurs
     */
    public void dump() throws IOException {
        dump(System.out);
    }

    /**
     * Returns type code function name for this field.
     * @return the type code function name
     */
    public String getTypeCodeAsString() {
        switch ((int)getSfTypeCode() & 0xFF) {
        case 0xA0: return "Attribute";
        case 0xA2: return "CopyCount";
        case 0xA6: return "Descriptor";
        case 0xA7: return "Control";
        case 0xA8: return "Begin";
        case 0xA9: return "End";
        case 0xAB: return "Map";
        case 0xAC: return "Position";
        case 0xAD: return "Process";
        case 0xAF: return "Include";
        case 0xB0: return "Table";
        case 0xB1: return "Migration";
        case 0xB2: return "Variable";
        case 0xB4: return "Link";
        case 0xEE: return "Data";
        default: return "Unknown:" + Integer.toHexString((int)getSfTypeCode()).toUpperCase();
        }
    }

    /**
     * Returns category code function name for this field.
     * @return the category code function name
     */
    public String getCategoryCodeAsString() {
        switch ((int)getSfCategoryCode() & 0xFF) {
        case 0x5F: return "Page Segment";
        case 0x6B: return "Object Area";
        case 0x77: return "Color Attribute Table";
        case 0x7B: return "IM Image";
        case 0x88: return "Medium";
        case 0x89: return "Font";
        case 0x8A: return "Coded Font";
        case 0x90: return "Process Element";
        case 0x92: return "Object Container";
        case 0x9B: return "Presentation Text";
        case 0xA7: return "Index";
        case 0xA8: return "Document";
        case 0xAD: return "Page Group";
        case 0xAF: return "Page";
        case 0xBB: return "Graphics";
        case 0xC3: return "Data Resource";
        case 0xC4: return "Document Environment Group (DEG)";
        case 0xC6: return "Resource Group";
        case 0xC7: return "Object Environment Group (OEG)";
        case 0xC9: return "Active Environment Group (AEG)";
        case 0xCC: return "Medium Map";
        case 0xCD: return "Form Map";
        case 0xCE: return "Name Resource";
        case 0xD8: return "Page Overlay";
        case 0xD9: return "Resource Environment Group (REG)";
        case 0xDF: return "Overlay";
        case 0xEA: return "Data Supression";
        case 0xEB: return "Bar Code";
        case 0xEE: return "No Operation";
        case 0xFB: return "Image";
        default: return "Unknown:" + Integer.toHexString((int)getSfTypeCode()).toUpperCase();
        }
    }

    /**
     * Returns the structured field's length.
     * @return the field length
     */
    public short getSfLength() {
        return this.sfLength;
    }

    /**
     * Returns the structured field's identifier.
     * @return the field identifier
     */
    public int getSfTypeID() {
        return ((getSfClassCode() & 0xFF) << 16)
                | ((getSfTypeCode() & 0xFF) << 8)
                | (getSfCategoryCode() & 0xFF);
    }

    /**
     * Returns the structured field's class code.
     * @return the field class code
     */
    public byte getSfClassCode() {
        return this.sfClassCode;
    }

    /**
     * Returns the structured field's type code.
     * @return the type code
     */
    public byte getSfTypeCode() {
        return this.sfTypeCode;
    }

    /**
     * Returns the structured field's category code.
     * @return the sfCategoryCode
     */
    public byte getSfCategoryCode() {
        return this.sfCategoryCode;
    }

    /**
     * Indicates whether an field introducer extension is present.
     * @return true if an field introducer extension is present
     */
    public boolean isSfiExtensionPresent() {
        return this.sfiExtensionPresent && (this.extData != null);
    }

    /**
     * Indicates whether segmented data is present.
     * @return true if the data is segmented
     */
    public boolean isSfiSegmentedData() {
        return this.sfiSegmentedData;
    }

    /**
     * Indicates whether the data is padded.
     * @return true if the data is padded
     */
    public boolean isSfiPaddingPresent() {
        return this.sfiPaddingPresent;
    }

    /**
     * Returns the length of the extension if present.
     * @return the length of the extension (or 0 if no extension is present)
     */
    public short getExtLength() {
        return this.extLength;
    }

    /**
     * Returns the extension data if present.
     * @return the extension data (or null if no extension is present)
     */
    public byte[] getExtData() {
        return this.extData;
    }

    /**
     * Returns the structured field's payload.
     * @return the field's data
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Returns the structured field's introducer data.
     * @return the introducer data
     */
    public byte[] getIntroducerData() {
        return this.introducerData;
    }

    /**
     * Returns the complete structured field as a byte array.
     * @return the complete field data
     */
    public byte[] getCompleteFieldAsBytes() {
        int len = INTRODUCER_LENGTH;
        if (isSfiExtensionPresent()) {
            len += getExtLength();
        }
        len += getData().length;
        byte[] bytes = new byte[len];
        int pos = 0;
        System.arraycopy(getIntroducerData(), 0, bytes, pos, INTRODUCER_LENGTH);
        pos += INTRODUCER_LENGTH;
        if (isSfiExtensionPresent()) {
            System.arraycopy(getExtData(), 0, bytes, pos, getExtLength());
            pos += getExtLength();
        }
        System.arraycopy(getData(), 0, bytes, pos, getData().length);
        return bytes;
    }

    /**
     * Writes this structured field to the given {@link OutputStream}.
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(this.introducerData);
        if (isSfiExtensionPresent()) {
            out.write(this.extData);
        }
        out.write(this.data);
    }
}
