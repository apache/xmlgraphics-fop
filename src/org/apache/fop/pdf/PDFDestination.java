/*
 * $Id$
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
package org.apache.fop.pdf;

import org.apache.fop.datatypes.IDReferences;

/**
 * This represents a single destination object in a PDF. Destinations allow
 * specific locations within a PDF document to be referenced by other PDF
 * documents (e.g. to link to a location within a document.)
 *
 * @author Stefan Wachter (based on work by Lloyd McKenzie)
 *
 */
public class PDFDestination {

    /**
     * References that are used to resolve the destination.
     */
    private IDReferences _idReferences;

    /**
     * the name that is used to reference the destination
     */
    private String _destinationName;

    /**
     * id of the internal destination that is referenced
     */
    private String _internalDestination;

    /**
     * @param idReferences the id nodes container that is used to resolve the
     * internal destination
     * @param destinationName the name under which this destination is referenced
     * @param internalDestination the internal destination that is referenced
     */
    public PDFDestination(IDReferences idReferences, String destinationName, String internalDestination) {
        _idReferences = idReferences;
        _destinationName = destinationName;
        _internalDestination = internalDestination;
    }

    /**
     * Represent the object in PDF. Outputs a key/value pair in a name tree structure.
     * The key is the destination name the value is an array addressing the corresponding
     * page/position.
     */
    protected String toPDF() {
        StringBuffer result = new StringBuffer();
        String destinationRef = _idReferences.getDestinationRef(_internalDestination);
        result.append(" (" + _destinationName + ") ").append(destinationRef);
        return result.toString();
    }

}