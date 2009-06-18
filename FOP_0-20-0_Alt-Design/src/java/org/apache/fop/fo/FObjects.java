/*
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * $Id$
 */

package org.apache.fop.fo;

//import java.lang.reflect.Constructor;
//import java.util.HashMap;
//import java.util.StringTokenizer;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
//import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.flow.*;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;

/**
 * Data class for common data and methods relating to Flow Objects.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

public class FObjects {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String packageNamePrefix = Fop.fopPackage;

    /**
     * FObjects cannot be instantiated
     */
    private FObjects() {}


    /**
     * This method generates generates new FO objects, except for FoPcdata
     * objects, which require an XmlEvent argument.  Use only when it is
     * known that no CHARACTERS event will be passed.
     * @param foTree the FO tree to which this node is being added
     * @param pageSequence the ancestor page sequence of this node
     * @param parent of this node
     * @param event the <code>FoXmlEvent</code> event that triggered the
     * generation of this FO
     * @param stateFlags set conditons determined by the ancestry of this
     * node.  These conditions are used to test validity of this node at this
     * point in the FO tree.
     * @return the new FO node
     * @throws FOPException
     */
    public static Object makePageSeqFOChild(
            FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
            FoXmlEvent event, int stateFlags)
        throws FOPException
    {
        int foType = event.getFoType();
        switch (foType) {
        case FObjectNames.BASIC_LINK:
            return new FoBasicLink(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.BIDI_OVERRIDE:
            return new FoBidiOverride(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.BLOCK:
            return new FoBlock(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.BLOCK_CONTAINER:
            return new FoBlockContainer(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.CHARACTER:
            return new FoCharacter(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.EXTERNAL_GRAPHIC:
            return new FoExternalGraphic(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.FLOAT:
            return new FoFloat(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.FOOTNOTE:
            return new FoFootnote(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.FOOTNOTE_BODY:
            return new FoFootnoteBody(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INITIAL_PROPERTY_SET:
            return new FoInitialPropertySet(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INLINE:
            return new FoInline(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INLINE_CONTAINER:
            return new FoInlineContainer(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INSTREAM_FOREIGN_OBJECT:
            return new FoInstreamForeignObject(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LEADER:
            return new FoLeader(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_BLOCK:
            return new FoListBlock(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_ITEM:
            return new FoListItem(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_ITEM_BODY:
            return new FoListItemBody(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_ITEM_LABEL:
            return new FoListItemLabel(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MARKER:
            return new FoMarker(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_CASE:
            return new FoMultiCase(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_PROPERTIES:
            return new FoMultiProperties(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_PROPERTY_SET:
            return new FoMultiPropertySet(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_SWITCH:
            return new FoMultiSwitch(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_TOGGLE:
            return new FoMultiToggle(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.PAGE_NUMBER:
            return new FoPageNumber(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.PAGE_NUMBER_CITATION:
            return new FoPageNumberCitation(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.RETRIEVE_MARKER:
            return new FoRetrieveMarker(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE:
            return new FoTable(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_AND_CAPTION:
            return new FoTableAndCaption(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_BODY:
            return new FoTableBody(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_CAPTION:
            return new FoTableCaption(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_CELL:
            return new FoTableCell(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_COLUMN:
            return new FoTableColumn(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_FOOTER:
            return new FoTableFooter(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_HEADER:
            return new FoTableHeader(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_ROW:
            return new FoTableRow(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.WRAPPER:
            return new FoWrapper(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.PCDATA:
            throw new FOPException(
                    "Attempt to make FoPcdata() with FoXmlEvent");
        default:
            throw new FOPException
            ("FO type value: " + foType + " not supported in makePageSeqFOChild");
            
        }
    }

    /**
     * This method generates generates new FO objects, including FoPcdata
     * objects.  It is more general in this sense than the overloaded
     * version which takes the <code>FoXmlEvent event</code> parameter.
     * @param foTree
     * @param parent
     * @param event the <code>XmlEvent</code> which triggered the generation
     * of this fo
     * @param stateFlags
     * @return
     * @throws FOPException
     */
    public static Object makePageSeqFOChild(
            FOTree foTree, FoPageSequence pageSequence,
            FOPageSeqNode parent, XmlEvent event, int stateFlags)
    throws FOPException
    {
        if (event instanceof FoXmlEvent) {
            return makePageSeqFOChild(
                    foTree, pageSequence, parent, (FoXmlEvent)event, stateFlags);
        }
        if (event.getType() != XmlEvent.CHARACTERS) {
            throw new FOPException(
                    "Attempt to makeFlowObject() with XmlEvent for event type "
                    + XmlEvent.eventTypeName(event.getType()));
        }
        return new FoPcdata(foTree, pageSequence, parent, event, stateFlags);
    }

}

