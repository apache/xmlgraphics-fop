/*
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
 * This file is part of the RTF library of the FOP project.
 */


package org.apache.fop.render.rtf.rtflib.rtfdoc;

// Java
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

// FOP
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfExternalGraphic;

/** 
 * Class which contains a linear text run. It has methods to add attributes, 
 * text, paragraph breaks....
 * @author Peter Herweg, pherweg@web.de
 */
public class RtfTextrun extends RtfContainer {
    private boolean bSuppressLastPar = false;
    private RtfListItem rtfListItem;
    
    /**  Class which represents the opening of a RTF group mark.*/
    private class RtfOpenGroupMark extends RtfElement {
                
        RtfOpenGroupMark(RtfContainer parent, Writer w, RtfAttributes attr)
                throws IOException {
            super(parent, w, attr);
        }
        
        /**
         * @return true if this element would generate no "useful" RTF content
         */
        public boolean isEmpty() {
            return false;
        }
        
        /**
         * write RTF code of all our children
         * @throws IOException for I/O problems
         */
        protected void writeRtfContent() throws IOException {
            writeGroupMark(true);
            writeAttributes(getRtfAttributes(), null);
        }
    }
    
    /**  Class which represents the closing of a RTF group mark.*/
    private class RtfCloseGroupMark extends RtfElement {
        
        RtfCloseGroupMark(RtfContainer parent, Writer w)
                throws IOException {
            super(parent, w);
        }
        
        /**
         * @return true if this element would generate no "useful" RTF content
         */
        public boolean isEmpty() {
            return false;
        }
        
        /**
         * write RTF code of all our children
         * @throws IOException for I/O problems
         */
        protected void writeRtfContent() throws IOException {
            writeGroupMark(false);
        }
    }

    /**  Class which represents a paragraph break.*/
    private class RtfParagraphBreak extends RtfElement {
        
        RtfParagraphBreak(RtfContainer parent, Writer w)
                throws IOException {
            super(parent, w);
        }
    
        /**
         * @return true if this element would generate no "useful" RTF content
         */
        public boolean isEmpty() {
            return false;
        }
    
        /**
         * write RTF code of all our children
         * @throws IOException for I/O problems
         */
        protected void writeRtfContent() throws IOException {
            writeControlWord("par");
        }
    }
              
    /** Create an RTF container as a child of given container */
    RtfTextrun(RtfContainer parent, Writer w, RtfAttributes attrs) throws IOException {
        super(parent, w, attrs);
    }
    
    public void pushAttributes(RtfAttributes attrs) throws IOException {
        RtfOpenGroupMark r = new RtfOpenGroupMark(this, writer, attrs);
    }
    
    public void popAttributes() throws IOException {
        RtfCloseGroupMark r = new RtfCloseGroupMark(this, writer);
    }
    
    public void addString(String s) throws IOException {
        RtfString r = new RtfString(this, writer, s);
    }
    
    public RtfFootnote addFootnote() throws IOException {
        return new RtfFootnote(this, writer);
    }
    
    public void addParagraphBreak() throws IOException {
        RtfParagraphBreak r = new RtfParagraphBreak(this, writer);
    }
    
    public void addPageNumber(RtfAttributes attr) throws IOException {
        RtfPageNumber r = new RtfPageNumber(this, writer, attr);
    }
    
    public RtfHyperLink addHyperlink(RtfAttributes attr) throws IOException {
        return new RtfHyperLink(this, writer, attr);
    }
    
    public RtfExternalGraphic newImage() throws IOException {
        return new RtfExternalGraphic(this, writer);
    }

    /**
     * Adds a new RtfTextrun to the given container if necessary, and returns it.
     * @param container RtfContainer, which is the parent of the returned RtfTextrun
     * @param writer Writer of the given RtfContainer
     * @param attrs RtfAttributes which are to write at the beginning of the RtfTextrun
     * @throws IOException for I/O problems
     */
    public static RtfTextrun getTextrun(RtfContainer container, Writer writer, RtfAttributes attrs)
            throws IOException {
        Object obj;
        List list = container.getChildren();
                
        if (list.size() == 0) {
            //add a new RtfTextrun
            RtfTextrun textrun = new RtfTextrun(container, writer, attrs);
            list.add(textrun);

            return textrun;
        } else if ((obj = list.get(list.size() - 1)) instanceof RtfTextrun ) {
            //if the last child is a RtfTextrun, return it
            return (RtfTextrun)obj;
        }

        //add a new RtfTextrun as the last child
        RtfTextrun textrun = new RtfTextrun(container, writer, attrs);
        list.add(textrun);
        
        return textrun;
    }
   
    /**
     * specify, if the last paragraph control word (\par) should be suppressed.
     * @param bSuppress true, if the last \par should be suppressed
     */    
    public void setSuppressLastPar(boolean bSuppress) {
        bSuppressLastPar = bSuppress;
    }
   
    /**
     * write RTF code of all our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {
        /**
         *TODO: The textrun's children are iterated twice:
         * 1. To determine the last RtfParagraphBreak
         * 2. To write the children
         * Maybe this can be done more efficient.
         */

        //determine, if this RtfTextrun is the last child of its parent
        boolean bLast = false;
        for (Iterator it = parent.getChildren().iterator(); it.hasNext();) {
            if (it.next() == this) {
                bLast = !it.hasNext();
                break;
            }
        }
        
        //get last RtfParagraphBreak, which is not followed by any visible child
        RtfParagraphBreak lastParagraphBreak = null;
        if (bLast) {
            for (Iterator it = getChildren().iterator(); it.hasNext();) {
                final RtfElement e = (RtfElement)it.next();
                if (e instanceof RtfParagraphBreak) {
                    lastParagraphBreak = (RtfParagraphBreak)e;
                } else {
                    if (!(e instanceof RtfOpenGroupMark)
                            && !(e instanceof RtfCloseGroupMark)
                            && e.isEmpty()) {
                        lastParagraphBreak = null;
                    }
                }
            }
        }
        
        //may contain for example \intbl
        writeAttributes(attrib, null);
        
        if (rtfListItem != null) {
            rtfListItem.getRtfListStyle().writeParagraphPrefix(this);
        }
        
        //write all children
        boolean bPrevPar = false;
        boolean bFirst = true;
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            final boolean bRtfParagraphBreak = (e instanceof RtfParagraphBreak);

            /**
             * -Write RtfParagraphBreak only, if the previous visible child
             * was't also a RtfParagraphBreak.
             * -Write RtfParagraphBreak only, if it is not the first visible
             * child.
             * -If the RtfTextrun is the last child of its parent, write a
             * RtfParagraphBreak only, if it is not the last child.
             */
            boolean bHide = false;
            bHide = bRtfParagraphBreak;
            bHide = bHide 
                && (bPrevPar 
                    || bFirst 
                    || (bSuppressLastPar && bLast && lastParagraphBreak != null 
                        && e == lastParagraphBreak));
                
            if (!bHide) {
                e.writeRtf(); 
                
                if (rtfListItem != null && e instanceof RtfParagraphBreak) {
                    rtfListItem.getRtfListStyle().writeParagraphPrefix(this);
                }
            }
            
            if (e instanceof RtfParagraphBreak) {
                bPrevPar = true;
            } else if (e instanceof RtfCloseGroupMark) {
                //do nothing
            } else if (e instanceof RtfOpenGroupMark) {
                //do nothing
            } else {
                bPrevPar = bPrevPar && e.isEmpty();
                bFirst = bFirst && e.isEmpty();
            }
        }
    }
    
    public void setRtfListItem(RtfListItem listItem) {
        rtfListItem = listItem;
    }
    
    public RtfListItem getRtfListItem() {
        return rtfListItem;
    }
}

