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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.fo.FOUserAgent;

/**
 * This class acts as a factory for PDF encryption support. It enables the
 * feature to be optional to FOP depending on the availability of JCE.
 */
public class PDFEncryptionManager {

    /**
     * Indicates whether JCE is available.
     * @return boolean true if JCE is present
     */
    public static boolean isJCEAvailable() {
        try {
            Class clazz = Class.forName("javax.crypto.Cipher");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Checks whether the necessary algorithms are available.
     * @return boolean True if all necessary algorithms are present
     */
    public static boolean checkAvailableAlgorithms() {
        if (!isJCEAvailable()) {
            return false;
        } else {
            Provider[] providers;
            providers = Security.getProviders("Cipher.RC4");
            if (providers == null) {
                return false;
            }
            providers = Security.getProviders("MessageDigest.MD5");
            if (providers == null) {
                return false;
            }
            return true;
        }
    }
    

    /**
     * Sets up PDF encryption if PDF encryption is requested by registering
     * a <code>PDFEncryptionParams</code> object with the user agent and if
     * the necessary cryptographic support is available.
     * @param userAgent the user agent
     * @param pdf the PDF document to setup encryption for
     * @param log the logger to send warnings to
     */
    public static void setupPDFEncryption(FOUserAgent userAgent, 
                                          PDFDocument pdf,
                                          Logger log) {
        if (userAgent == null) {
            throw new NullPointerException("User agent must not be null");
        }
        if (pdf == null) {
            throw new NullPointerException("PDF document must not be null");
        }
        if (userAgent.getPDFEncryptionParams() != null) {
            if (!checkAvailableAlgorithms()) {
                if (isJCEAvailable()) {
                    log.warn("PDF encryption has been requested, JCE is "
                            + "available but there's no "
                            + "JCE provider available that provides the "
                            + "necessary algorithms. The PDF won't be "
                            + "encrypted.");
                } else {
                    log.warn("PDF encryption has been requested but JCE is "
                            + "unavailable! The PDF won't be encrypted.");
                }
            }
            pdf.setEncryption(userAgent.getPDFEncryptionParams());
        }
    }
    
    /**
     * Creates a new PDFEncryption instance if PDF encryption is available.
     * @param objnum PDF object number
     * @param params PDF encryption parameters
     * @return PDFEncryption the newly created instance, null if PDF encryption
     * is unavailable.
     */
    public static PDFEncryption newInstance(int objnum, PDFEncryptionParams params) {
        try {
            Class clazz = Class.forName("org.apache.fop.pdf.PDFEncryptionJCE");
            Method makeMethod = clazz.getMethod("make", 
                        new Class[] {int.class, PDFEncryptionParams.class});
            Object obj = makeMethod.invoke(null, 
                        new Object[] {new Integer(objnum), params});
            return (PDFEncryption)obj;
        } catch (ClassNotFoundException e) {
            if (checkAvailableAlgorithms()) {
                System.out.println("JCE and algorithms available, but the "
                    + "implementation class unavailable. Please do a full "
                    + "rebuild.");
            }
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
