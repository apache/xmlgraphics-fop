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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import org.apache.fop.rtf.rtflib.exceptions.RtfStructureException;

/**  An RtfElement that can contain other elements.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfContainer extends RtfElement {
    private LinkedList m_children;  // 'final' removed by Boris Poudérous on 07/22/2002
    private RtfOptions m_options = new RtfOptions();
    private RtfElement m_lastChild;

    /** Create an RTF container as a child of given container */
    RtfContainer(RtfContainer parent, Writer w) throws IOException {
        this(parent, w, null);
    }

    /** Create an RTF container as a child of given container with given attributes */
    RtfContainer(RtfContainer parent, Writer w, RtfAttributes attr) throws IOException {
        super(parent, w, attr);
        m_children = new LinkedList();
    }

    /** set options */
    public void setOptions(RtfOptions opt) {
        m_options = opt;
    }

    /** add a child element to this */
    protected void addChild(RtfElement e)
    throws RtfStructureException {
        if (isClosed()) {
            // No childs should be added to a container that has been closed
            final StringBuffer sb = new StringBuffer();
            sb.append("addChild: container already closed (parent=");
            sb.append(this.getClass().getName());
            sb.append(" child=");
            sb.append(e.getClass().getName());
            sb.append(")");
            final String msg = sb.toString();

            // warn of this problem
            final RtfFile rf = getRtfFile();
//            if(rf.getLog() != null) {
//               rf.getLog().logWarning(msg);
//            }

            // TODO this should be activated to help detect XSL-FO constructs
            // that we do not handle properly.
            /*
            throw new RtfStructureException(msg);
             */
        }

        m_children.add(e);
        m_lastChild = e;
    }

    /** return a copy of our children's list */
    public List getChildren() {
        return (List)m_children.clone();
    }

    /** return the number of children */
    public int getChildCount() {
        return m_children.size();
    }

    /**
     * Add by Boris Poudérous on 07/22/2002
     * Set the children list
     */
    public boolean setChildren (List children) {
      if (children instanceof LinkedList) {
          this.m_children = (LinkedList)children;
          return true;
        }

      return false;
    }

    /** write RTF code of all our children */
    protected void writeRtfContent()
    throws IOException {
        for (Iterator it = m_children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            e.writeRtf();
        }
    }

    /** return our options */
    RtfOptions getOptions() {
        return m_options;
    }

    /** true if this (recursively) contains at least one RtfText object */
    boolean containsText() {
        boolean result = false;
        for (Iterator it = m_children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfText) {
                result = !e.isEmpty();
            } else if (e instanceof RtfContainer) {
                if (((RtfContainer)e).containsText()) {
                    result = true;
                }
            }
            if (result) {
                break;
            }
        }
        return result;
    }

    /** debugging to given Writer */
    void dump(Writer w, int indent)
    throws IOException {
        super.dump(w, indent);
        for (Iterator it = m_children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            e.dump(w, indent + 1);
        }
    }

    /** minimal debugging display */
    public String toString() {
        return super.toString() + " (" + getChildCount() + " children)";
    }

    /** don't write any RTF if empty of if our options block it */
    protected boolean okToWriteRtf() {
        boolean result = super.okToWriteRtf() && !isEmpty();
        if (result && !m_options.renderContainer(this)) {
            result = false;
        }
        return result;
    }

    /** true if this element would generate no "useful" RTF content.
     *  For an RtfContainer, true if it has no children where isEmpty() is false
     */
    public boolean isEmpty() {
        boolean result = true;
        for (Iterator it = m_children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (!e.isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }
}