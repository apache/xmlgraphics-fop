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
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * Class modelling the fo:footnote-body object. See Sec. 6.10.4 of the XSL-FO
 * Standard.
 */
public class FootnoteBody extends FObj {

    private int align;
    private int alignLast;
    private int lineHeight;
    private int startIndent;
    private int endIndent;
    private int textIndent;

    /**
     * @param parent FONode that is the parent of this object
     */
    public FootnoteBody(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        getFOInputHandler().startFootnoteBody(this);
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveFootnoteBody(this);
    }

    protected void end() {
        super.end();
        getFOInputHandler().endFootnoteBody(this);
    }
    
    public String getName() {
        return "fo:footnote-body";
    }
}
