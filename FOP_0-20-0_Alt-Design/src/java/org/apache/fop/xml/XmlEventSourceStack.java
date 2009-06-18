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

/**
 * Defines the interface for a stack of <code>XmlEventSource</code>s.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public interface XmlEventSourceStack extends XmlEventSource {
    
    /**
     * Push a new source onto the this stack of sources.
     * @param source to push
     */
    void pushSource(XmlEventSource source);
    
}
