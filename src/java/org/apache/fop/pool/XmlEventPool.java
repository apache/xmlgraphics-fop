/*
 * $Id$
 * 
 *
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.pool;

import org.apache.fop.xml.XmlEvent;

/**
 * This class provides a pool of <tt>XmlEvent</tt> objects.
 */

public class XmlEventPool extends FopPool {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Required argument for constructing new <tt>XmlEvent</tt>s. */
    //protected final Namespaces namespaces;
    /**
     * The zero-argument constructor
     */
    public XmlEventPool() {
    }
    
    /**
     * Construct a pool with a given initial size.
     * @param initialSize - the initial size of the pool.
     */
    public XmlEventPool(int initialSize) {
        super(initialSize);
    }

    /**
     * Acquire an <tt>XmlEvent</tt>.
     * @return an <tt>XmlEvent</tt>.
     */
    public synchronized XmlEvent acquireXMLEvent() {
        return (XmlEvent)acquirePoolable();
    }

}
