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

package org.apache.fop.fo.flow;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;

/**
 * Class modelling the fo:footnote object. See Sec. 6.10.3 of the XSL-FO
 * Standard.
 */
public class Footnote extends FObj {

    private Inline inlineFO = null;
    private FootnoteBody body;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Footnote(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        getFOTreeControl().getFOInputHandler().startFootnote(this);
    }

    /**
     * @param child child FONode to be added to this object
     */
    public void addChild(FONode child) {
        String name = child.getName();
        if ("fo:inline".equals(name)) {
            inlineFO = (Inline)child;
        } else if ("fo:footnote-body".equals(name)) {
            body = (FootnoteBody)child;
        } else {
            getLogger().error("invalid child of footnote: " + name);
        }
    }

    /**
     * Public accessor for inline FO
     * @return the Inline object stored as inline FO
     */
    public Inline getInlineFO() {
        return inlineFO;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveFootnote(this);
    }
    
    protected void end() {
        super.end();
        getFOTreeControl().getFOInputHandler().endFootnote(this);
    }
}

