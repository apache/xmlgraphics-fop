/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// FOP
import org.apache.fop.fo.FOUserAgent;

public interface ImageCache {
    public FopImage getImage(String url, FOUserAgent context);
    public void releaseImage(String url, FOUserAgent context);
    public void invalidateImage(String url, FOUserAgent context);
    public void removeContext(FOUserAgent context);
}

