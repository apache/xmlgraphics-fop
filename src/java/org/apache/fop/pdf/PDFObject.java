/*
 * $Id: PDFObject.java,v 1.12 2003/03/07 08:25:47 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * generic PDF object.
 *
 * A PDF Document is essentially a collection of these objects. A PDF
 * Object has a number and a generation (although the generation will always
 * be 0 in new documents).
 */
public abstract class PDFObject {

    /**
     * the object's number
     */
    private int objnum;

    /**
     * the object's generation (0 in new documents)
     */
    private int generation = 0;

    /**
     * the parent PDFDocument
     */
    private PDFDocument document;

    /**
     * Create a PDFObject
     */
    public PDFObject() {
        // do nothing
    }

    /**
     * Returns the object's number.
     * @return the PDF Object number
     */
    public int getObjectNumber() {
        if (this.objnum == 0) {
            throw new IllegalStateException("Object has no number assigned: " + this.toString());
            //System.out.println("Object has no number assigned: " + this.toString());
        }
        return this.objnum;
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
        //System.out.println("Assigning "+this+" object number "+objnum);
    }

    /**
     * Returns the object's generation.
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
        return this.document;
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
            throw new IllegalStateException("Parent PDFDocument is unavailable");
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
        String ref = getObjectNumber() + " " + getGeneration() + " R";
        return ref;
    }

    /**
     * Write the PDF represention of this object
     *
     * @param stream the stream to write the PDF to
     * @throws IOException if there is an error writing to the stream
     * @return the number of bytes written
     */
    protected int output(OutputStream stream) throws IOException {
        byte[] pdf = this.toPDF();
        stream.write(pdf);
        return pdf.length;
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
     * binary content use toPDF() or output(OutputStream) instead.
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
            return encode(PDFText.escapeText(text, true));
        }
    }
    
    /**
     * Encodes a String (3.2.3 in PDF 1.4 specs)
     * @param string the string to encode
     * @return byte[] the encoded string
     */
    protected byte[] encodeString(String string) {
        return encodeText(string);
        /*
        final byte[] buf = encode(PDFText.escapeString(string));
        if (getDocumentSafely().isEncryptionActive()) {
            return PDFText.escapeByteArray(
                getDocument().getEncryption().encrypt(buf, this));
        } else {
            return buf;
        }*/
    }

}
