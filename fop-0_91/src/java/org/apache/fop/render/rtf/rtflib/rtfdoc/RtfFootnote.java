/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

//Java
import java.io.Writer;
import java.io.IOException;

//FOP
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;

/**  Model of an RTF footnote
 *  @author Peter Herweg, pherweg@web.de
 *  @author Marc Wilhelm Kuester
 */
public class RtfFootnote extends RtfContainer
        implements IRtfTextrunContainer, IRtfListContainer {
    RtfTextrun textrunInline = null;
    RtfContainer body = null;
    RtfList list = null;
    boolean bBody = false;

    /** Create an RTF list item as a child of given container with default attributes */
    RtfFootnote(RtfContainer parent, Writer w) throws IOException {
        super(parent, w);
        textrunInline = new RtfTextrun(this, writer, null);
        body = new RtfContainer(this, writer);
    }

    public RtfTextrun getTextrun() throws IOException {
        if (bBody) {
            RtfTextrun textrun = RtfTextrun.getTextrun(body, writer, null);
            textrun.setSuppressLastPar(true);
            
            return textrun;
        } else {
            return textrunInline;
        }
    }

    /**
    * write RTF code of all our children
    * @throws IOException for I/O problems
    */
    protected void writeRtfContent() throws IOException {
        textrunInline.writeRtfContent();
        
        writeGroupMark(true);
        writeControlWord("footnote");
        writeControlWord("ftnalt");      
        
        body.writeRtfContent();
        
        writeGroupMark(false);
    }
    
    public RtfList newList(RtfAttributes attrs) throws IOException {
        if (list != null) {
            list.close();
        }

        list = new RtfList(body, writer, attrs);

        return list;
    }
    
    public void startBody() {
        bBody = true;
    }
    
    public void endBody() {
        bBody = false;
    }
}
