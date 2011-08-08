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

/**
 * This class holds the parameters for PDF encryption.
 */
public class PDFEncryptionParams {

    private String userPassword = ""; //May not be null
    private String ownerPassword = ""; //May not be null

    private boolean allowPrint = true;
    private boolean allowCopyContent = true;
    private boolean allowEditContent = true;
    private boolean allowEditAnnotations = true;
    private boolean allowFillInForms = true;
    private boolean allowAccessContent = true;
    private boolean allowAssembleDocument = true;
    private boolean allowPrintHq = true;

    private int encryptionLengthInBits = 40;

    /**
     * Creates a new instance.
     * @param userPassword the user password
     * @param ownerPassword the owner password
     * @param allowPrint true if printing is allowed
     * @param allowCopyContent true if copying content is allowed
     * @param allowEditContent true if editing content is allowed
     * @param allowEditAnnotations true if editing annotations is allowed
     */
    public PDFEncryptionParams(String userPassword, String ownerPassword,
                               boolean allowPrint,
                               boolean allowCopyContent,
                               boolean allowEditContent,
                               boolean allowEditAnnotations) {
        setUserPassword(userPassword);
        setOwnerPassword(ownerPassword);
        setAllowPrint(allowPrint);
        setAllowCopyContent(allowCopyContent);
        setAllowEditContent(allowEditContent);
        setAllowEditAnnotations(allowEditAnnotations);
    }

    /**
     * Default constructor initializing to default values.
     */
    public PDFEncryptionParams() {
        //nop
    }

    /**
     * Creates a copy of the given encryption parameters.
     *
     * @param source source encryption parameters
     */
    public PDFEncryptionParams(PDFEncryptionParams source) {
        setUserPassword(source.getUserPassword());
        setOwnerPassword(source.getOwnerPassword());
        setAllowPrint(source.isAllowPrint());
        setAllowCopyContent(source.isAllowCopyContent());
        setAllowEditContent(source.isAllowEditContent());
        setAllowEditAnnotations(source.isAllowEditAnnotations());
        setAllowAssembleDocument(source.isAllowAssembleDocument());
        setAllowAccessContent(source.isAllowAccessContent());
        setAllowFillInForms(source.isAllowFillInForms());
        setAllowPrintHq(source.isAllowPrintHq());
        setEncryptionLengthInBits(source.getEncryptionLengthInBits());
    }

    /**
     * Indicates whether copying content is allowed.
     * @return true if copying is allowed
     */
    public boolean isAllowCopyContent() {
        return allowCopyContent;
    }

    /**
     * Indicates whether editing annotations is allowed.
     * @return true is editing annotations is allowed
     */
    public boolean isAllowEditAnnotations() {
        return allowEditAnnotations;
    }

    /**
     * Indicates whether editing content is allowed.
     * @return true if editing content is allowed
     */
    public boolean isAllowEditContent() {
        return allowEditContent;
    }

    /**
     * Indicates whether printing is allowed.
     * @return true if printing is allowed
     */
    public boolean isAllowPrint() {
        return allowPrint;
    }

    /**
     * Indicates whether revision 3 filling in forms is allowed.
     * @return true if revision 3 filling in forms is allowed
     */
    public boolean isAllowFillInForms() {
        return allowFillInForms;
    }

    /**
     * Indicates whether revision 3 extracting text and graphics is allowed.
     * @return true if revision 3 extracting text and graphics is allowed
     */
    public boolean isAllowAccessContent() {
        return allowAccessContent;
    }

    /**
     * Indicates whether revision 3 assembling document is allowed.
     * @return true if revision 3 assembling document is allowed
     */
    public boolean isAllowAssembleDocument() {
        return allowAssembleDocument;
    }

    /**
     * Indicates whether revision 3 printing to high quality is allowed.
     * @return true if revision 3 printing to high quality is allowed
     */
    public boolean isAllowPrintHq() {
        return allowPrintHq;
    }

    /**
     * Returns the owner password.
     * @return the owner password, an empty string if no password applies
     */
    public String getOwnerPassword() {
        return ownerPassword;
    }

    /**
     * Returns the user password.
     * @return the user password, an empty string if no password applies
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Sets the permission for copying content.
     * @param allowCopyContent true if copying content is allowed
     */
    public void setAllowCopyContent(boolean allowCopyContent) {
        this.allowCopyContent = allowCopyContent;
    }

    /**
     * Sets the permission for editing annotations.
     * @param allowEditAnnotations true if editing annotations is allowed
     */
    public void setAllowEditAnnotations(boolean allowEditAnnotations) {
        this.allowEditAnnotations = allowEditAnnotations;
    }

    /**
     * Sets the permission for editing content.
     * @param allowEditContent true if editing annotations is allowed
     */
    public void setAllowEditContent(boolean allowEditContent) {
        this.allowEditContent = allowEditContent;
    }

    /**
     * Sets the permission for printing.
     * @param allowPrint true if printing is allowed
     */
    public void setAllowPrint(boolean allowPrint) {
        this.allowPrint = allowPrint;
    }

    /**
     * Sets whether revision 3 filling in forms is allowed.
     * @param allowFillInForms true if revision 3 filling in forms is allowed.
     */
    public void setAllowFillInForms(boolean allowFillInForms) {
        this.allowFillInForms = allowFillInForms;
    }

    /**
     * Sets whether revision 3 extracting text and graphics is allowed.
     * @param allowAccessContent true if revision 3 extracting text and graphics is allowed
     */
    public void setAllowAccessContent(boolean allowAccessContent) {
        this.allowAccessContent = allowAccessContent;
    }

    /**
     * Sets whether revision 3 assembling document is allowed.
     * @param allowAssembleDocument true if revision 3 assembling document is allowed
     */
    public void setAllowAssembleDocument(boolean allowAssembleDocument) {
        this.allowAssembleDocument = allowAssembleDocument;
    }

    /**
     * Sets whether revision 3 printing to high quality is allowed.
     * @param allowPrintHq true if revision 3 printing to high quality is allowed
     */
    public void setAllowPrintHq(boolean allowPrintHq) {
        this.allowPrintHq = allowPrintHq;
    }

    /**
     * Sets the owner password.
     * @param ownerPassword The owner password to set, null or an empty String
     * if no password is applicable
     */
    public void setOwnerPassword(String ownerPassword) {
        if (ownerPassword == null) {
            this.ownerPassword = "";
        } else {
            this.ownerPassword = ownerPassword;
        }
    }

    /**
     * Sets the user password.
     * @param userPassword The user password to set, null or an empty String
     * if no password is applicable
     */
    public void setUserPassword(String userPassword) {
        if (userPassword == null) {
            this.userPassword = "";
        } else {
            this.userPassword = userPassword;
        }
    }

    /**
     * Returns the encryption length.
     * @return the encryption length
     */
    public int getEncryptionLengthInBits() {
        return encryptionLengthInBits;
    }

    /**
     * Sets the encryption length.
     *
     * @param encryptionLength the encryption length
     */
    public void setEncryptionLengthInBits(int encryptionLength) {
        this.encryptionLengthInBits = encryptionLength;
    }

}
