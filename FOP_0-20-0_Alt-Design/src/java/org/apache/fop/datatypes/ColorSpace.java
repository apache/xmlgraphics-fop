/*
 * $Id$
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.datatypes;

public class ColorSpace {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
	private boolean hasICCProfile;
	private byte[] iccProfile;
	private int numComponents;

    // Ok... so I had some grand purpose for this, but I can't recall.
    // I'm just writing it

    public static int DEVICE_UNKNOWN = -1;
    public static int DEVICE_GRAY = 1;
    // what's the *official* spelling?
    // public static int DEVICE_GREY = 1;
    public static int DEVICE_RGB = 2;
    public static int DEVICE_CMYK = 3;


    // Are there any others?

    protected int currentColorSpace = -1;

    public ColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
		hasICCProfile = false;
		numComponents = calculateNumComponents();
    }

	private int calculateNumComponents() {
		if (currentColorSpace == DEVICE_GRAY)
			return 1;
		else if (currentColorSpace == DEVICE_RGB)
			return 3;
		else if (currentColorSpace == DEVICE_CMYK)
			return 4;
		else
			return 0;
	}

	public void setColorSpace(int theColorSpace) {
		this.currentColorSpace = theColorSpace;
		numComponents = calculateNumComponents();
	}

	public boolean hasICCProfile() {
		return hasICCProfile;
	}

	public byte[] getICCProfile() {
		if (hasICCProfile)
			return iccProfile;
		else
			return new byte[0];
	}

	public void setICCProfile(byte[] iccProfile) {
		this.iccProfile = iccProfile;
		hasICCProfile = true;
	}

    public int getColorSpace() {
        return (this.currentColorSpace);
    }

	public int getNumComponents() {
		return numComponents;
	}

    public String getColorSpacePDFString() {    // this is for PDF Output. Does anyone else need a string representation?


        // shouldn't this be a select-case? I can never remember
        // the syntax for that.
        if (this.currentColorSpace == ColorSpace.DEVICE_RGB) {
            return ("DeviceRGB");
        } else if (this.currentColorSpace == ColorSpace.DEVICE_CMYK) {
            return ("DeviceCMYK");
        } else if (this.currentColorSpace == ColorSpace.DEVICE_GRAY) {
            return ("DeviceGray");
        } else {    // unknown... Error. Tell them it's RGB and hope they don't notice.
            return ("DeviceRGB");
        }
    }

}
