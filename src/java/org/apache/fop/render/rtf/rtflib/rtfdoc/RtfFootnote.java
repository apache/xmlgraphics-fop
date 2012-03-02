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

//Java
import java.io.Writer;
import java.io.IOException;

/**  Model of an RTF footnote
 *  @author Peter Herweg, pherweg@web.de
 *  @author Marc Wilhelm Kuester
 */
public class RtfFootnote extends RtfContainer
        implements IRtfTextrunContainer, IRtfListContainer {
    RtfTextrun textrunInline = null;                            // CSOK: VisibilityModifier
    RtfContainer body = null;                                   // CSOK: VisibilityModifier
    RtfList list = null;                                        // CSOK: VisibilityModifier
    boolean bBody = false;                                      // CSOK: VisibilityModifier

    /**
     * Create an RTF list item as a child of given container with default attributes.
     * @param parent a container
     * @param w a writer
     * @throws IOException if not caught
     */
    RtfFootnote(RtfContainer parent, Writer w) throws IOException {
        super(parent, w);
        textrunInline = new RtfTextrun(this, writer, null);
        body = new RtfContainer(this, writer);
    }

    /**
     * @return a text run
     * @throws IOException if not caught
     */
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

    /**
     * @param attrs some attributes
     * @return an rtf list
     * @throws IOException if not caught
     */
    public RtfList newList(RtfAttributes attrs) throws IOException {
        if (list != null) {
            list.close();
        }

        list = new RtfList(body, writer, attrs);

        return list;
    }

    /** start body */
    public void startBody() {
        bBody = true;
    }

    /** end body */
    public void endBody() {
        bBody = false;
    }
}
