/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

//Author:       Eric SCHAEFFER
//Description:  Image Exception

package org.apache.fop.image;

public class FopImageException extends Exception {

    public FopImageException() {
        super();
    }

    public FopImageException(String message) {
        super(message);
    }
}
