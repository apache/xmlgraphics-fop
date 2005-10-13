/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
     * Sets the persmission for printing.
     * @param allowPrint true if printing is allowed
     */
    public void setAllowPrint(boolean allowPrint) {
        this.allowPrint = allowPrint;
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

}
