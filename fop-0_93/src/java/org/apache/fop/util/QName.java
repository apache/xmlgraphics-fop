/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.util;

import java.io.Serializable;

/**
 * Represents a qualified name of an XML element or an XML attribute.
 * <p>
 * Note: This class allows to carry a namespace prefix but it is not used in the equals() and 
 * hashCode() methods.
 */
public class QName implements Serializable {

    private static final long serialVersionUID = -5225376740044770690L;
    
    private String namespaceURI;
    private String localName;
    private String prefix;
    private int hashCode;
    
    /**
     * Main constructor.
     * @param namespaceURI the namespace URI
     * @param prefix the namespace prefix, may be null
     * @param localName the local name
     */
    public QName(String namespaceURI, String prefix, String localName) {
        if (localName == null) {
            throw new NullPointerException("Parameter localName must not be null");
        }
        if (localName.length() == 0) {
            throw new IllegalArgumentException("Parameter localName must not be empty");
        }
        this.namespaceURI = namespaceURI;
        this.prefix = prefix;
        this.localName = localName;
        this.hashCode = toHashString().hashCode();
    }
    
    /**
     * Main constructor.
     * @param namespaceURI the namespace URI
     * @param qName the qualified name
     */
    public QName(String namespaceURI, String qName) {
        if (qName == null) {
            throw new NullPointerException("Parameter localName must not be null");
        }
        if (qName.length() == 0) {
            throw new IllegalArgumentException("Parameter localName must not be empty");
        }
        this.namespaceURI = namespaceURI;
        int p = qName.indexOf(':');
        if (p > 0) {
            this.prefix = qName.substring(0, p);
            this.localName = qName.substring(p + 1);
        } else {
            this.prefix = null;
            this.localName = qName;
        }
        this.hashCode = toHashString().hashCode();
    }
    
    /** @return the namespace URI */
    public String getNamespaceURI() {
        return this.namespaceURI;
    }
    
    /** @return the namespace prefix */
    public String getPrefix() {
        return this.prefix;
    }
    
    /** @return the local name */
    public String getLocalName() {
        return this.localName;
    }
    
    /** @return the fully qualified name */
    public String getQName() {
        return getPrefix() != null ? getPrefix() + ':' + getLocalName() : getLocalName();
    }

    /** @see java.lang.Object#hashCode() */
    public int hashCode() {
        return this.hashCode;
    }

    /** @see java.lang.Object#equals(java.lang.Object) */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            if (obj instanceof QName) {
                QName other = (QName)obj;
                if ((getNamespaceURI() == null && other.getNamespaceURI() == null)
                        || getNamespaceURI().equals(other.getNamespaceURI())) {
                    return getLocalName().equals(other.getLocalName());
                }
            }
        }
        return false;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        return prefix != null
                ? (prefix + ":" + localName)
                : toHashString();
    }

    private String toHashString() {
        return (namespaceURI != null 
                ? ("{" + namespaceURI + "}" + localName) 
                : localName);
    }

}
