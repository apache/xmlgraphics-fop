/*
 * $Id: UnknownXMLObj.java,v 1.7 2003/03/05 21:48:02 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

/**
 * Class for handling generic XML from a namespace not recognized by FOP
 */
public class UnknownXMLObj extends XMLObj {
    private String namespace;

    /**
     * Inner class for an UnknownXMLObj Maker
     */
    public static class Maker extends ElementMapping.Maker {
        private String space;

        /**
         * Construct the Maker
         * @param sp the namespace for this Maker
         */
        public Maker(String sp) {
            space = sp;
        }

        /**
         * Make an instance
         * @param parent FONode that is the parent of the object
         * @return the created UnknownXMLObj
         */
        public FONode make(FONode parent) {
            return new UnknownXMLObj(parent, space);
        }
    }

    /**
     * Constructs an unknown xml object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param space the namespace for this object
     */
    protected UnknownXMLObj(FONode parent, String space) {
        super(parent);
        this.namespace = space;
    }

    /**
     * @see XMLObj#getNameSpace
     */
    public String getNameSpace() {
        return this.namespace;
    }

    /**
     * @see XMLObj#addChild
     */
    protected void addChild(FONode child) {
        if (doc == null) {
            createBasicDocument();
        }
        super.addChild(child);
    }

    /**
     *  @see XMLObj#addCharacters
     */
    protected void addCharacters(char data[], int start, int length) {
        if (doc == null) {
            createBasicDocument();
        }
        super.addCharacters(data, start, length);
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveVisitor(this);
    }

}

