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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class acts as a factory for PDF encryption support. It enables the
 * feature to be optional to FOP depending on the availability of JCE.
 */
public class PDFEncryptionManager {

    /** logging instance */
    protected static Log log = LogFactory.getLog(PDFEncryptionManager.class);

    /**
     * Indicates whether JCE is available.
     * @return boolean true if JCE is present
     */
    public static boolean isJCEAvailable() {
        try {
            /*Class clazz =*/ Class.forName("javax.crypto.Cipher");
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
                log.warn("Cipher provider for RC4 not available.");
                return false;
            }
            providers = Security.getProviders("MessageDigest.MD5");
            if (providers == null) {
                log.warn("MessageDigest provider for MD5 not available.");
                return false;
            }
            return true;
        }
    }


    /**
     * Sets up PDF encryption if PDF encryption is requested by registering
     * a <code>PDFEncryptionParams</code> object with the user agent and if
     * the necessary cryptographic support is available.
     * @param params the PDF encryption params or null to disable encryption
     * @param pdf the PDF document to setup encryption for
     */
    public static void setupPDFEncryption(PDFEncryptionParams params,
                                          PDFDocument pdf) {
        if (pdf == null) {
            throw new NullPointerException("PDF document must not be null");
        }
        if (params != null) {
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
            pdf.setEncryption(params);
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
                log.warn("JCE and algorithms available, but the "
                    + "implementation class unavailable. Please do a full "
                    + "rebuild.");
            }
            return null;
        } catch (NoSuchMethodException e) {
            log.error(e);
            return null;
        } catch (IllegalAccessException e) {
            log.error(e);
            return null;
        } catch (InvocationTargetException e) {
            log.error(e);
            return null;
        }
    }

}
