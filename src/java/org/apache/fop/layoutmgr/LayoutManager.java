/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.layoutmgr;

import org.apache.fop.apps.FOUserAgent;
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
    void setFObj(FObj obj);

    /**
     * Set the user agent. For resolving user agent values
     * and getting logger.
     *
     * @param ua the user agent
     */
    void setUserAgent(FOUserAgent ua);

    /**
     * Get the user agent.
     *
     * @return the user agent
     */
    FOUserAgent getUserAgent();

}
