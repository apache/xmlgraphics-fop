package org.apache.fop.fo;

import org.apache.fop.fo.expr.PropertyException;

/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Base interface for all FOs which are generate reference areas.
 */

public interface ReferenceAreaFO {
    public void setWritingMode(int mode);
    public int getWritingMode();
}
