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

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * generic PDF object.
 *
 * A PDF Document is essentially a collection of these objects. A PDF
 * Object has a number and a generation (although the generation will always
 * be 0 in new documents).
 */
public abstract class PDFObject implements PDFWritable {

    /** logger for all PDFObjects (and descendants) */
    protected static final Log log = LogFactory.getLog(PDFObject.class.getName());

    /**
     * the object's number
     */
    private int objnum;

    /**
     * the object's generation (0 in new documents)
     */
    private int generation;

    /**
     * the parent PDFDocument
     */
    private PDFDocument document;

    /** the parent PDFObject (may be null and may not always be set, needed for encryption) */
    private PDFObject parent;

    /**
     * Returns the object's number.
     * @return the PDF Object number
     */
    public int getObjectNumber() {
        if (this.objnum == 0) {
            throw new IllegalStateException("Object has no number assigned: " + this.toString());
        }
        return this.objnum;
    }

    /**
     * Default constructor.
     */
    public PDFObject() {
        //nop
    }

    /**
     * Constructor for direct objects.
     * @param parent the containing PDFObject instance
     */
    public PDFObject(PDFObject parent) {
        setParent(parent);
    }

    /**
     * Indicates whether this PDFObject has already been assigned an
     * object number.
     * @return True if it has an object number
     */
    public boolean hasObjectNumber() {
        return this.objnum > 0;
    }

    /**
     * Sets the object number
     * @param objnum the object number
     */
    public void setObjectNumber(int objnum) {
        this.objnum = objnum;
        PDFDocument doc = getDocument();
        setParent(null);
        setDocument(doc); //Restore reference to PDFDocument after setting parent to null
        if (log.isTraceEnabled()) {
            log.trace("Assigning " + this + " object number " + objnum);
        }
    }

    /**
     * Returns this object's generation.
     * @return the PDF Object generation
     */
    public int getGeneration() {
        return this.generation;
    }

    /**
     * Returns the parent PDFDocument if assigned.
     * @return the parent PDFDocument (May be null if the parent PDFDocument
     * has not been assigned)
     */
    public final PDFDocument getDocument() {
        if (this.document != null) {
            return this.document;
        } else if (getParent() != null) {
            return getParent().getDocument();
        } else {
            return null;
        }
    }

    /**
     * Returns the parent PDFDocument, but unlike <code>getDocument()</code>
     * it throws an informative Exception if the parent document is unavailable
     * instead of having a NullPointerException somewhere without a message.
     * @return the parent PDFDocument
     */
    public final PDFDocument getDocumentSafely() {
        final PDFDocument doc = getDocument();
        if (doc == null) {
            throw new IllegalStateException("Parent PDFDocument is unavailable on "
                    + getClass().getName());
        }
        return doc;
    }

    /**
     * Sets the parent PDFDocument.
     * @param doc the PDFDocument.
     */
    public void setDocument(PDFDocument doc) {
        this.document = doc;
    }

    /**
     * Returns this objects's parent. The parent is null if it is a "direct object".
     * @return the parent or null if there's no parent (or it hasn't been set)
     */
    public PDFObject getParent() {
        return this.parent;
    }

    /**
     * Sets the direct parent object.
     * @param parent the direct parent
     */
    public void setParent(PDFObject parent) {
        this.parent = parent;
    }

    /**
     * Returns the PDF representation of the Object ID.
     * @return the Object ID
     */
    public String getObjectID() {
        return getObjectNumber() + " " + getGeneration() + " obj\n";
    }

    /**
     * Returns the PDF representation of a reference to this object.
     * @return the reference string
     */
    public String referencePDF() {
        if (!hasObjectNumber()) {
            throw new IllegalArgumentException(
                    "Cannot reference this object. It doesn't have an object number");
        }
        String ref = getObjectNumber() + " " + getGeneration() + " R";
        return ref;
    }

    /**
     * Creates and returns a reference to this object.
     * @return the object reference
     */
    public PDFReference makeReference() {
        return new PDFReference(this);
    }

    /**
     * Write the PDF represention of this object
     *
     * @param stream the stream to write the PDF to
     * @throws IOException if there is an error writing to the stream
     * @return the number of bytes written
     */
    public int output(OutputStream stream) throws IOException {
        byte[] pdf = this.toPDF();
        stream.write(pdf);
        return pdf.length;
    }

    /** {@inheritDoc} */
    public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
        if (hasObjectNumber()) {
            textBuffer.append(referencePDF());
        } else {
            PDFDocument.flushTextBuffer(textBuffer, out);
            output(out);
        }
    }

    /**
     * Encodes the object as a byte array for output to a PDF file.
     *
     * @return PDF string
     */
    protected byte[] toPDF() {
        return encode(toPDFString());
    }


    /**
     * This method returns a String representation of the PDF object. The result
     * is normally converted/encoded to a byte array by toPDF(). Only use
     * this method to implement the serialization if the object can be fully
     * represented as text. If the PDF representation of the object contains
     * binary content use toPDF() or output(OutputStream) instead. This applies
     * to any object potentially containing a string object because string object
     * are encrypted and therefore need to be binary.
     * @return String the String representation
     */
    protected String toPDFString() {
        throw new UnsupportedOperationException("Not implemented. "
                    + "Use output(OutputStream) instead.");
    }

    /**
     * Converts text to a byte array for writing to a PDF file.
     * @param text text to convert/encode
     * @return byte[] the resulting byte array
     */
    public static final byte[] encode(String text) {
        return PDFDocument.encode(text);
    }

    /**
     * Encodes a Text String (3.8.1 in PDF 1.4 specs)
     * @param text the text to encode
     * @return byte[] the encoded text
     */
    protected byte[] encodeText(String text) {
        if (getDocumentSafely().isEncryptionActive()) {
            final byte[] buf = PDFText.toUTF16(text);
            return PDFText.escapeByteArray(
                getDocument().getEncryption().encrypt(buf, this));
        } else {
            return encode(PDFText.escapeText(text, false));
        }
    }

    /**
     * Encodes a String (3.2.3 in PDF 1.4 specs)
     * @param string the string to encode
     * @return byte[] the encoded string
     */
    protected byte[] encodeString(String string) {
        return encodeText(string);
    }

    /**
     * Encodes binary data as hexadecimal string object.
     * @param data the binary data
     * @param out the OutputStream to write the encoded object to
     * @throws IOException if an I/O error occurs
     */
    protected void encodeBinaryToHexString(byte[] data, OutputStream out) throws IOException {
        out.write('<');
        if (getDocumentSafely().isEncryptionActive()) {
            data = getDocument().getEncryption().encrypt(data, this);
        }
        String hex = PDFText.toHex(data, false);
        byte[] encoded = hex.getBytes("US-ASCII");
        out.write(encoded);
        out.write('>');
    }

    /**
     * Formats an object for serialization to PDF.
     * <p>
     * IMPORTANT: If you need to write out binary output, call
     * {@link PDFDocument#flushTextBuffer(StringBuilder, OutputStream)} before writing any content
     * to the {@link OutputStream}!
     * @param obj the object
     * @param out the OutputStream to write to
     * @param textBuffer a text buffer for text output
     * @throws IOException If an I/O error occurs
     */
    protected void formatObject(Object obj, OutputStream out, StringBuilder textBuffer)
                throws IOException {
        if (obj == null) {
            textBuffer.append("null");
        } else if (obj instanceof PDFWritable) {
            ((PDFWritable)obj).outputInline(out, textBuffer);
        } else if (obj instanceof Number) {
            if (obj instanceof Double || obj instanceof Float) {
                textBuffer.append(PDFNumber.doubleOut(((Number)obj).doubleValue()));
            } else {
                textBuffer.append(obj.toString());
            }
        } else if (obj instanceof Boolean) {
            textBuffer.append(obj.toString());
        } else if (obj instanceof byte[]) {
            PDFDocument.flushTextBuffer(textBuffer, out);
            encodeBinaryToHexString((byte[])obj, out);
        } else {
            PDFDocument.flushTextBuffer(textBuffer, out);
            out.write(encodeText(obj.toString()));
        }
    }

    /**
     * Check if the other PDFObject has the same content as the current object.
     * <p>
     * Note: This function has a contract which is less binding than
     * {@link #equals(Object)}. Whereas equals would require all values to be
     * identical, this method is not required to check everything. In the case
     * of PDFObjects, this means that the overriding function does not have to
     * check for {@link #getObjectID()}.
     *
     * @param o
     *            object to compare to.
     * @return true if the other object has the same content.
     */
    protected boolean contentEquals(PDFObject o) {
        return this.equals(o);
    }
}
