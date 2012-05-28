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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * <p>Base class for all elements of an RTF file.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch)
 * and Andreas Putz (a.putz@skynamics.com).</p>
 */
public abstract class RtfElement {
    /** Writer to be used */
    protected final Writer writer;
    /** parent element */
    protected final RtfContainer parent;
    /** attributes of the element */
    protected final RtfAttributes attrib;
    private boolean written;
    private boolean closed;
    private final int id;
    private static int idCounter;

    /** Create an RTF element as a child of given container */
    RtfElement(RtfContainer parent, Writer w) throws IOException {
        this(parent, w, null);
    }

    /** Create an RTF element as a child of given container with given attributes */
    RtfElement(RtfContainer parent, Writer w, RtfAttributes attr) throws IOException {

        id = idCounter++;
        this.parent = parent;
        attrib = (attr != null ? attr : new RtfAttributes());
        if (this.parent != null) {
            this.parent.addChild(this);
        }
        writer = w;
        written = false;
    }

    /**
     * Does nothing, meant to allow elements to write themselves without waiting
     * for write(), but not implemented yet
     * @throws IOException for I/O problems
     */
    public final void close() throws IOException {
        closed = true;
    }

    /**
     * Write the RTF code of this element to our Writer
     * @throws IOException for I/O problems
     */
    public final void writeRtf() throws IOException {
        if (!written) {
            written = true;
            if (okToWriteRtf()) {
                writeRtfPrefix();
                writeRtfContent();
                writeRtfSuffix();
            }
        }
    }

    /**
     * Starts a new line in the RTF file being written. This is only to format
     * the RTF file itself (for easier debugging), not its content.
     * @throws IOException in case of an I/O problem
     */
    public void newLine() throws IOException {
        writer.write("\n");
    }

    /**
     * Write an RTF control word to our Writer
     * @param word RTF control word to write
     * @throws IOException for I/O problems
     */
    protected final void writeControlWord(String word)
    throws IOException {
        writer.write('\\');
        writer.write(word);
        writer.write(' ');
    }

    /**
     * Write an RTF control word to our Writer, preceeded by a star '*'
     * meaning "ignore this if you don't know what it means"
     * @param word RTF control word to write
     * @throws IOException for I/O problems
     */
    protected final void writeStarControlWord(String word)
    throws IOException {
        writer.write("\\*\\");
        writer.write(word);
        writer.write(' ');
    }

    /**
     * Same as writeStarControlWord(String word), except with no space behind it
     * @param word RTF control word to write
     * @throws IOException for I/O problems
     */
    protected final void writeStarControlWordNS(String word)
    throws IOException {
        writer.write("\\*\\");
        writer.write(word);
    }

    /**
     * Write rtf control word without the space behind it
     * @param word RTF control word to write
     * @throws IOException for I/O problems
     */
    protected final void writeControlWordNS(String word)
    throws IOException {
        writer.write('\\');
        writer.write(word);
    }

    /**
     * Called before writeRtfContent()
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
    }

    /**
     * Must be implemented to write RTF content to m_writer
     * @throws IOException for I/O problems
     */
    protected abstract void writeRtfContent() throws IOException;

    /**
     * Called after writeRtfContent()
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
    }

    /**
     * Write a start or end group mark
     * @param isStart set to true if this is a start mark
     * @throws IOException for I/O problems
     */
    protected final void writeGroupMark(boolean isStart)
    throws IOException {
        writer.write(isStart ? "{" : "}");
    }

    /**
     * Write given attribute values to our Writer
     * @param attr RtfAttributes to be written
     * @param nameList if given, only attribute names from this list are considered
     * @throws IOException for I/O problems
     */
    protected void writeAttributes(RtfAttributes attr, String [] nameList)
    throws IOException {
        if (attr == null) {
            return;
        }

        if (nameList != null) {
            // process only given attribute names
            for (int i = 0; i < nameList.length; i++) {
                final String name = nameList[i];
                if (attr.isSet(name)) {
                    writeOneAttribute(name, attr.getValue(name));
                }
            }
        } else {
            // process all defined attributes
            for (Iterator it = attr.nameIterator(); it.hasNext();) {
                final String name = (String)it.next();
                if (attr.isSet(name)) {
                    writeOneAttribute(name, attr.getValue(name));
                }
            }
        }
    }

    /**
     * Write one attribute to our Writer
     * @param name name of attribute to write
     * @param value value of attribute to be written
     * @throws IOException for I/O problems
     */
    protected void writeOneAttribute(String name, Object value)
    throws IOException {
        String cw = name;
        if (value instanceof Integer) {
            // attribute has integer value, must write control word + value
            cw += value;
        } else if (value instanceof String) {
            cw += value;
        } else if (value instanceof RtfAttributes) {
            writeControlWord(cw);
            writeAttributes((RtfAttributes) value, null);
            return;
        }
        writeControlWord(cw);
    }

    /**
     * Write one attribute to our Writer without a space
     * @param name name of attribute to write
     * @param value value of attribute to be written
     * @throws IOException for I/O problems
     */
    protected void writeOneAttributeNS(String name, Object value)
    throws IOException {
        String cw = name;
        if (value instanceof Integer) {
            // attribute has integer value, must write control word + value
            cw += value;
        } else if (value instanceof String) {
            cw += value;
        } else if (value instanceof RtfAttributes) {
            writeControlWord(cw);
            writeAttributes((RtfAttributes) value, null);
            return;
        }
        writeControlWordNS(cw);
    }

    /**
     * can be overridden to suppress all RTF output
     * @return true if this object can be written into the RTF
     */
    protected boolean okToWriteRtf() {
        return true;
    }

    /** debugging to given PrintWriter */
    void dump(Writer w, int indent)
    throws IOException {
        for (int i = 0; i < indent; i++) {
            w.write(' ');
        }
        w.write(this.toString());
        w.write('\n');
        w.flush();
    }

    /**
     * minimal debugging display
     * @return String representation of object
     */
    public String toString() {
        return (this == null) ? "null" : (this.getClass().getName() + " #" + id);
    }

    /** true if close() has been called */
    boolean isClosed() {
        return closed;
    }

    /** access our RtfFile, which is always the topmost parent */
    RtfFile getRtfFile() {
        // go up the chain of parents until we find the topmost one
        RtfElement result = this;
        while (result.parent != null) {
            result = result.parent;
        }

        // topmost parent must be an RtfFile
        // a ClassCastException here would mean that the parent-child structure is not as expected
        return (RtfFile)result;
    }

    /** find the first parent where c.isAssignableFrom(parent.getClass()) is true
     *  @return null if not found
     */
    public RtfElement getParentOfClass(Class c) {
        RtfElement result = null;
        RtfElement current = this;
        while (current.parent != null) {
            current = current.parent;
            if (c.isAssignableFrom(current.getClass())) {
                result = current;
                break;
            }
        }
        return result;
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public abstract boolean isEmpty();

    /**
     * Make a visible entry in the RTF for an exception
     * @param ie Exception to flag
     * @throws IOException for I/O problems
     */
    protected void writeExceptionInRtf(Exception ie)
    throws IOException {
        writeGroupMark(true);
        writeControlWord("par");

        // make the exception message stand out so that the problem is visible
        writeControlWord("fs48");
//        RtfStringConverter.getInstance().writeRtfString(m_writer,
//                JForVersionInfo.getShortVersionInfo() + ": ");
        RtfStringConverter.getInstance().writeRtfString(writer, ie.getClass().getName());

        writeControlWord("fs20");
        RtfStringConverter.getInstance().writeRtfString(writer, " " + ie.toString());

        writeControlWord("par");
        writeGroupMark(false);
    }

    /**
     * Added by Normand Masse
     * Used for attribute inheritance
     * @return RtfAttributes
     */
    public RtfAttributes getRtfAttributes() {
        return attrib;
    }
}
