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

package org.apache.fop.fo.pagination;

// SAX
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * Base PageMasterReference class. Provides implementation for handling the
 * master-reference attribute and containment within a PageSequenceMaster
 */
public abstract class PageMasterReference extends FObj
            implements SubSequenceSpecifier {

    private String masterName;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public PageMasterReference(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        if (getProperty(PR_MASTER_REFERENCE) != null) {
            this.masterName = getProperty(PR_MASTER_REFERENCE).getString();
        }
        validateParent(parent);
    }


    /**
     * Returns the "master-reference" attribute of this page master reference
     * @return the name of the page master
     */
    public String getMasterName() {
        return masterName;
    }

    /**
     * Checks that the parent is the right element. The default implementation
     * checks for fo:page-sequence-master.
     * @param parent parent node
     * @throws FOPException If the parent is invalid.
     */
    protected void validateParent(FONode parent) throws SAXParseException {
        if (parent.getName().equals("fo:page-sequence-master")) {
            PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)parent;

            if (getMasterName() == null) {
                getLogger().warn(getName()
                    + " does not have a master-reference and so is being ignored");
            } else {
                pageSequenceMaster.addSubsequenceSpecifier(this);
            }
        } else {
            throw new SAXParseException(getName() + " must be"
                                   + "child of fo:page-sequence-master, not "
                                   + parent.getName(), locator);
        }
    }

    public String getName() {
        return "fo:page-master-reference";
    }
}
