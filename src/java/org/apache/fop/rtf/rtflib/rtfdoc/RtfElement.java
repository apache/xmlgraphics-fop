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

import java.io.*;
import java.util.*;
//import org.apache.fop.rtf.rtflib.jfor.main.JForVersionInfo;
import org.xml.sax.Attributes;

/**  Base class for all elements of an RTF file.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 */
public abstract class RtfElement {
    protected final Writer m_writer;
    protected final RtfContainer m_parent;
    protected final RtfAttributes m_attrib;
    private boolean m_written;
    private boolean m_closed;
    private final int m_id;
    private static int m_idCounter;

    /** Create an RTF element as a child of given container */
    RtfElement(RtfContainer parent,Writer w) throws IOException {
        this(parent,w,null);
    }


    /** Create an RTF element as a child of given container with given attributes */
    RtfElement(RtfContainer parent,Writer w,RtfAttributes attr) throws IOException {

        m_id = m_idCounter++;
        m_parent = parent;
        m_attrib = (attr != null ? attr : new RtfAttributes());
        if(m_parent != null) m_parent.addChild(this);
        m_writer = w;
        m_written = false;
    }

    /** Does nothing, meant to allow elements to write themselves without waiting
     *  for write(), but not implemented yet */
    public final void close() throws IOException {
        m_closed = true;
    }

    /** write the RTF code of this element to our Writer */
    public final void writeRtf() throws IOException {
        if(!m_written) {
            m_written = true;
            if(okToWriteRtf()) {
                writeRtfPrefix();
                writeRtfContent();
                writeRtfSuffix();
            }
        }
    }

    /** write an RTF control word to our Writer */
    protected final void writeControlWord(String word)
    throws IOException {
        m_writer.write('\\');
        m_writer.write(word);
        m_writer.write(' ');
    }

    /** write an RTF control word to our Writer, preceeded by a star '*'
     *  meaning "ignore this if you don't know what it means"
     */
    protected final void writeStarControlWord(String word)
    throws IOException {
        m_writer.write("\\*\\");
        m_writer.write(word);
        m_writer.write(' ');
    }

    protected final void writeStarControlWordNS(String word)
    throws IOException {
        m_writer.write("\\*\\");
        m_writer.write(word);
    }

    /** write rtf control word without the space behind it */
    protected final void writeControlWordNS(String word)
    throws IOException {
        m_writer.write('\\');
        m_writer.write(word);
    }

    /** called before writeRtfContent() */
    protected void writeRtfPrefix() throws IOException {
    }

    /** must be implemented to write RTF content to m_writer */
    protected abstract void writeRtfContent() throws IOException;

    /** called after writeRtfContent() */
    protected void writeRtfSuffix() throws IOException {
    }

    /** Write a start or end group mark */
    protected final void writeGroupMark(boolean isStart)
    throws IOException {
        m_writer.write(isStart ? "{" : "}");
    }

    /** write given attribute values to our Writer
     *  @param nameList if given, only attribute names from this list are considered
     */
    protected void writeAttributes(RtfAttributes attr,String [] nameList)
    throws IOException {
        if(attr==null) return;

        if(nameList != null) {
            // process only given attribute names
            for(int i=0; i < nameList.length; i++) {
                final String name = nameList[i];
                if(attr.isSet(name)) {
                    writeOneAttribute(name,attr.getValue(name));
                }
            }
        } else {
            // process all defined attributes
            for(Iterator it = attr.nameIterator(); it.hasNext(); ) {
                final String name = (String)it.next();
                if(attr.isSet(name)) {
                    writeOneAttribute(name,attr.getValue(name));
                }
            }
        }
    }

    /** write one attribute to our Writer */
    protected void writeOneAttribute(String name,Object value)
    throws IOException {
        String cw = name;
        if(value instanceof Integer) {
            // attribute has integer value, must write control word + value
            cw += value;
        }else if(value instanceof String){
            cw += value;
        }
        writeControlWord(cw);
    }
    /** write one attribute to our Writer without a space*/
    protected void writeOneAttributeNS(String name,Object value)
    throws IOException {
        String cw = name;
        if(value instanceof Integer) {
            // attribute has integer value, must write control word + value
            cw += value;
        }else if(value instanceof String){
            cw += value;
        }
        writeControlWordNS(cw);
    }

    /** can be overridden to suppress all RTF output */
    protected boolean okToWriteRtf() {
        return true;
    }

    /** debugging to given PrintWriter */
    void dump(Writer w,int indent)
    throws IOException {
        for(int i=0; i < indent; i++) {
            w.write(' ');
        }
        w.write(this.toString());
        w.write('\n');
        w.flush();
    }

    /** minimal debugging display */
    public String toString() {
        return (this == null) ? "null" : (this.getClass().getName() + " #" + m_id);
    }

    /** true if close() has been called */
    boolean isClosed() {
        return m_closed;
    }

    /** access our RtfFile, which is always the topmost parent */
    RtfFile getRtfFile() {
        // go up the chain of parents until we find the topmost one
        RtfElement result = this;
        while(result.m_parent != null) {
            result = result.m_parent;
        }

        // topmost parent must be an RtfFile
        // a ClassCastException here would mean that the parent-child structure is not as expected
        return (RtfFile)result;
    }

    /** find the first parent where c.isAssignableFrom(parent.getClass()) is true
     *  @return null if not found
     */
    RtfElement getParentOfClass(Class c)
    {
        RtfElement result = null;
        RtfElement current = this;
        while(current.m_parent != null) {
            current = current.m_parent;
            if(c.isAssignableFrom(current.getClass())) {
                result = current;
                break;
            }
        }
        return result;
    }

    /** true if this element would generate no "useful" RTF content */
    public abstract boolean isEmpty();


    protected void writeExceptionInRtf(Exception ie)
    throws IOException {
        writeGroupMark(true);
        writeControlWord("par");

        // make the exception message stand out so that the problem is visible
        writeControlWord("fs48");
//        RtfStringConverter.getInstance().writeRtfString(m_writer,JForVersionInfo.getShortVersionInfo() + ": ");
        RtfStringConverter.getInstance().writeRtfString(m_writer,ie.getClass().getName());

        writeControlWord("fs20");
        RtfStringConverter.getInstance().writeRtfString(m_writer," " + ie.toString());

        writeControlWord("par");
        writeGroupMark(false);
    }

    // Added by Normand Masse
    // Used for attribute inheritance
    public RtfAttributes getRtfAttributes() {
        return m_attrib;
    }
}