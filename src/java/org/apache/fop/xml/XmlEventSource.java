/*
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
 * Created on 16/01/2004
 * $Id$
 */
package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;

/**
 * Defines the interface of a source of <code>XmlEvent</code>s.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public interface XmlEventSource {
    
    /**
     * Get the next event from the source
     * @return the event
     */
    public XmlEvent getEvent() throws FOPException;
    
    /**
     * Push back an XmlEvent.  The next call to <code>get()</code> will
     * retrieve this event.  Only one level of pushback is supported.
     * @param event to be pushed back
     */
    public void pushBack(XmlEvent event);
    
    /**
     * @return true if source is exhausted
     */
    public boolean isExhausted();
    
}
