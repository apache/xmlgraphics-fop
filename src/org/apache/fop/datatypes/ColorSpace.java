/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

public class ColorSpace {
    //Ok... so I had some grand purpose for this, but I can't recall.
    //I'm just writing it

    public static int DEVICE_UNKNOWN = -1;
    public static int DEVICE_GRAY = 1;
    //what's the *official* spelling?
    //public static int DEVICE_GREY = 1;
    public static int DEVICE_RGB = 2;
    public static int DEVICE_CMYK = 3;

    //Are there any others?

    protected int currentColorSpace = -1;

    public ColorSpace (int theColorSpace) {
        this.currentColorSpace = theColorSpace;

    }
    public int getColorSpace() {
        return (this.currentColorSpace);
    }
    public void setColorSpace(int theColorSpace) {
        this.currentColorSpace = theColorSpace;
    }

    public String getColorSpacePDFString() {//this is for PDF Output. Does anyone else need a string representation?


        //shouldn't this be a select-case? I can never remember
        //the syntax for that.
        if (this.currentColorSpace == this.DEVICE_RGB) {
            return("DeviceRGB");
        } else if (this.currentColorSpace == this.DEVICE_CMYK) {
            return("DeviceCMYK");
        } else if (this.currentColorSpace == this.DEVICE_GRAY) {
            return("DeviceGray");
        } else {//unknown... Error. Tell them it's RGB and hope they don't notice.
            return("DeviceRGB");
        }
    }
}
