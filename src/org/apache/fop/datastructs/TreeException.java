/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 */

package org.apache.fop.datastructs;

import org.apache.fop.apps.FOPException;

/**
 * Exceptions thrown during Tree/Node operations.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class TreeException extends FOPException {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public TreeException(String message) {
        super(message);
    }

    public TreeException(Throwable e) {
        super(e);
    }

}
