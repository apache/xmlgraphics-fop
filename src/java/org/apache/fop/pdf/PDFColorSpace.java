/*
 * $Id: PDFColorSpace.java,v 1.5 2003/03/07 08:25:47 jeremias Exp $
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
 * PDF Color space.
 */
public class PDFColorSpace {
    private boolean hasICCProfile;
    private byte[] iccProfile;
    private int numComponents;

    // Ok... so I had some grand purpose for this, but I can't recall.
    // I'm just writing it

    /**
     * Unknown colorspace
     */
    public static final int DEVICE_UNKNOWN = -1;

    /**
     * Gray colorspace
     */
    public static final int DEVICE_GRAY = 1;

    /**
     * RGB colorspace
     */
    public static final int DEVICE_RGB = 2;

    /**
     * CMYK colorspace
     */
    public static final int DEVICE_CMYK = 3;

    // Are there any others?

    /**
     * Current color space value.
     */
    protected int currentColorSpace = DEVICE_UNKNOWN;

    /**
     * Create a PDF colorspace object.
     *
     * @param theColorSpace the current colorspace
     */
    public PDFColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        hasICCProfile = false;
        numComponents = calculateNumComponents();
    }

    private int calculateNumComponents() {
        if (currentColorSpace == DEVICE_GRAY) {
            return 1;
        } else if (currentColorSpace == DEVICE_RGB) {
            return 3;
        } else if (currentColorSpace == DEVICE_CMYK) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * Set the current colorspace.
     *
     * @param theColorSpace the new color space value
     */
    public void setColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
        numComponents = calculateNumComponents();
    }

    /**
     * Check if this colorspace has an ICC profile.
     *
     * @return true if this has an ICC profile
     */
    public boolean hasICCProfile() {
        return hasICCProfile;
    }

    /**
     * Get the ICC profile for this colorspace
     *
     * @return the byte array containing the ICC profile data
     */
    public byte[] getICCProfile() {
        if (hasICCProfile) {
            return iccProfile;
        } else {
            return new byte[0];
        }
    }

    /**
     * Set the ICC profile for this colorspace.
     *
     * @param iccProfile the ICC profile data
     */
    public void setICCProfile(byte[] iccProfile) {
        this.iccProfile = iccProfile;
        hasICCProfile = true;
    }

    /**
     * Get the colorspace value
     *
     * @return the colorspace value
     */
    public int getColorSpace() {
        return (this.currentColorSpace);
    }

    /**
     * Get the number of color components for this colorspace
     *
     * @return the number of components
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * Get the PDF string for this colorspace.
     *
     * @return the PDF string for the colorspace
     */
    public String getColorSpacePDFString() {
        // shouldn't this be a select-case? I can never remember
        // the syntax for that.
        switch (currentColorSpace) {
            case DEVICE_CMYK:
                return "DeviceCMYK";
            //break;
            case DEVICE_GRAY:
                return "DeviceGray";
            //break;
            case DEVICE_RGB:
            default:
                // unknown... Error. Tell them it's RGB and hope they
                // don't notice.
                return ("DeviceRGB");
            //break;
        }
    }

}
