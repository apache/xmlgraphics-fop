/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 */

package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;

/**
 * Exception thrown when scanning for one of a set of STARTELEMENTs.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class UnexpectedStartElementException extends FOPException {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public UnexpectedStartElementException(String message) {
        super(message);
    }

    public UnexpectedStartElementException(Throwable e) {
        super(e);
    }

}
