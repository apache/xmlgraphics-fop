/*
 * $Id$
 * 
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
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.apache.fop.pool.Poolable;

/**
 * A class for holding and passing a URI index and local name
 * pair, as used in the <tt>XmlEvent</tt> class.
 */
public class UriLocalName extends Poolable {
    protected int uriIndex;
    protected String localName;

    /**
     * @param uriIndex - the index of the namespace URI maintained in
     * the associated <tt>Namespaces</tt> object.
     * @param localName - the local name of the event.
     */
    public UriLocalName(int uriIndex, String localName, int sequence) {
        super(sequence);
        this.uriIndex = uriIndex;
        this.localName = localName;
    }
    
    public Poolable clear() {
        uriIndex = Namespaces.NO_NAMESPACE;
        localName = "";
        return this;
    }
}
