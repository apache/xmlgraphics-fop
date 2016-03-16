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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
* Represents an unparsed (generic) AFP structured field.
*/
public final class UnparsedStructuredField {

   private final Introducer introducer;
   private final byte[] extData;
   private final byte[] data;

   /**
    *
    * @param Structured field introducer
    * @param data Structured field data
    * @param extData Structured field extension data
    */
   UnparsedStructuredField(Introducer introducer,
           byte[] data, byte[] extData) {
       this.introducer = introducer;
       this.data = data;
       if (extData != null) {
           this.extData = extData;
       } else {
           this.extData = null;
       }
   }

   @Override
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
    * Returns type code function name for this field.
    * @return the type code function name
    */
   private String getTypeCodeAsString() {
       switch (getSfTypeCode() & 0xFF) {
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
       default: return "Unknown:" + Integer.toHexString(getSfTypeCode()).toUpperCase();
       }
   }

   /**
    * Returns category code function name for this field.
    * @return the category code function name
    */
   private String getCategoryCodeAsString() {
       switch (getSfCategoryCode() & 0xFF) {
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
       default: return "Unknown:" + Integer.toHexString(getSfTypeCode()).toUpperCase();
       }
   }

   /**
    * Returns the structured field's length.
    * @return the field length
    */
   public short getSfLength() {
       return introducer.length;
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
       return introducer.classCode;
   }

   /**
    * Returns the structured field's type code.
    * @return the type code
    */
   public byte getSfTypeCode() {
       return introducer.typeCode;
   }

   /**
    * Returns the structured field's category code.
    * @return the sfCategoryCode
    */
   public byte getSfCategoryCode() {
       return introducer.categoryCode;
   }

   /**
    * Indicates whether an field introducer extension is present.
    * @return true if an field introducer extension is present
    */
   public boolean isSfiExtensionPresent() {
       return introducer.extensionPresent && (this.extData != null);
   }

   /**
    * Indicates whether segmented data is present.
    * @return true if the data is segmented
    */
   public boolean isSfiSegmentedData() {
       return introducer.segmentedData;
   }

   /**
    * Indicates whether the data is padded.
    * @return true if the data is padded
    */
   public boolean isSfiPaddingPresent() {
       return introducer.paddingPresent;
   }

   /**
    * Returns the length of the extension if present.
    * @return the length of the extension (or 0 if no extension is present)
    */
   public short getExtLength() {
       return (extData != null) ? (short)(extData.length + 1) : 0;

   }

   /**
    * Returns the extension data if present.
    * @return the extension data (or null if no extension is present)
    */
   byte[] getExtData() {
       if (this.extData == null) {
           return new byte[0];
       }
       byte[] rtn = new byte[this.extData.length];
       System.arraycopy(this.extData, 0, rtn, 0, rtn.length);
       return rtn;
   }

   /**
    * Returns the structured field's payload.
    * @return the field's data
    */
   public byte[] getData() {
       if (this.data == null) {
           return new byte[0];
       }
       byte[] rtn = new byte[this.data.length];
       System.arraycopy(this.data, 0, rtn, 0, rtn.length);
       return rtn;
   }

   //For unit testing
   byte[] getIntroducerData() {
       return introducer.getIntroducerData();
   }

   /**
    * Returns the complete structured field as a byte array.
    * @return the complete field data
    */
   public byte[] getCompleteFieldAsBytes() {

       ByteArrayOutputStream baos = new ByteArrayOutputStream(getSfLength());
       try {
           writeTo(baos);
       } catch (IOException ioe) {
           //nop
       }
       return baos.toByteArray();

   }

   /**
    * Writes this structured field to the given {@link OutputStream}.
    * @param out the output stream
    * @throws IOException if an I/O error occurs
    */
   public void writeTo(OutputStream out) throws IOException {
       out.write(introducer.introducerData);
       if (isSfiExtensionPresent()) {
           out.write(this.extData.length + 1);
           out.write(this.extData);
       }
       out.write(this.data);
   }

   static final class Introducer {

       private final short length;
       private final byte classCode;
       private final byte typeCode;
       private final byte categoryCode;
       private final boolean extensionPresent;
       private final boolean segmentedData;
       private final boolean paddingPresent;
       private final byte[] introducerData;

       Introducer(byte[] introducerData) throws IOException {

           this.introducerData = introducerData;

           // Parse the introducer; the 8 bytes have already been read from the stream just
           // before, so we parse the introducer from the byte array
           DataInputStream iis = new DataInputStream(
                   new ByteArrayInputStream(introducerData));

           length = iis.readShort();

           classCode = iis.readByte();
           typeCode = iis.readByte();
           categoryCode = iis.readByte();

           //Flags
           byte f = iis.readByte();

           extensionPresent = (f & 0x01) != 0;
           segmentedData = (f & 0x04) != 0;
           paddingPresent = (f & 0x10) != 0;

       }


       public short getLength() {
           return length;
       }

       public byte getClassCode() {
           return classCode;
       }

       public byte getTypeCode() {
           return typeCode;
       }

       public byte getCategoryCode() {
           return categoryCode;
       }

       public boolean isExtensionPresent() {
           return extensionPresent;
       }


       public boolean isSegmentedData() {
           return segmentedData;
       }

       public boolean isPaddingPresent() {
           return paddingPresent;
       }

       public byte[] getIntroducerData() {
           byte[] rtn = new byte[introducerData.length];
           System.arraycopy(introducerData, 0, rtn, 0, rtn.length);
           return rtn;
       }
   }

}
