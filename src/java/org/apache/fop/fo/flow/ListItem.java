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

/* $Id:$ */

package org.apache.fop.fo.flow;

// Java
import java.util.List;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;

/**
 * Class modelling the fo:list-item object.
 */
public class ListItem extends FObj {

    private ListItemLabel label = null;
    private ListItemBody body = null;

    /**
     * @param parent FONode that is the parent of this object
     */
    public ListItem(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        getFOEventHandler().startListItem(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (list-item-label,list-item-body)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
            if (nsURI == FO_URI && localName.equals("marker")) {
                if (label != null) {
                    nodesOutOfOrderError(loc, "fo:marker", "fo:list-item-label");
                }
            } else if (nsURI == FO_URI && localName.equals("list-item-label")) {
                if (label != null) {
                    tooManyNodesError(loc, "fo:list-item-label");
                }
            } else if (nsURI == FO_URI && localName.equals("list-item-body")) {
                if (label == null) {
                    nodesOutOfOrderError(loc, "fo:list-item-label", "fo:list-item-body");
                } else if (body != null) {
                    tooManyNodesError(loc, "fo:list-item-body");
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     * @todo see if can/should rely on base class for this 
     *    (i.e., add to childNodes instead)
     */
    public void addChildNode(FONode child) {
        int nameId = ((FObj)child).getNameId();
        
        if (nameId == FO_LIST_ITEM_LABEL) {
            label = (ListItemLabel) child;
        } else if (nameId == FO_LIST_ITEM_BODY) {
            body = (ListItemBody) child;
        } else if (nameId == FO_MARKER) {
            addMarker((Marker) child);
        }
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws SAXParseException {
        if (label == null || body == null) {
            missingChildElementError("marker* (list-item-label,list-item-body)");
        }
        getFOEventHandler().endListItem(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {
        ListItemLayoutManager blm = new ListItemLayoutManager(this);
        list.add(blm);
    }

    /**
     * @return the label of the list item
     */
    public ListItemLabel getLabel() {
        return label;
    }

    /**
     * @return the body of the list item
     */
    public ListItemBody getBody() {
        return body;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:list-item";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_LIST_ITEM;
    }
}

