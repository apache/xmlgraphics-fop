/*
 * $Id$
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
