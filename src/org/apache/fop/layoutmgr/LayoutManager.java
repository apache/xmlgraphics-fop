/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.fo.FObj;

/**
 * The interface for all LayoutManagers.
 */
public interface LayoutManager {

    /**
     * Set the FO object for this layout manager.
     * For layout managers that are created without an FO
     * this may not be called.
     *
     * @param obj the FO object for this layout manager
     */
    public void setFObj(FObj obj);

    /**
     * Set the user agent. For resolving user agent values
     * and getting logger.
     *
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua);

    /**
     * Get the user agent.
     *
     * @return the user agent
     */
    public FOUserAgent getUserAgent();

}
