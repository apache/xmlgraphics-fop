/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.fo.properties.*;
import org.apache.fop.svg.*;
import org.apache.fop.datatypes.*;

import org.apache.fop.apps.FOPException;

import org.xml.sax.Attributes;

/**
 * This is a property list builder that bypasses overhead.
 * The attribute list is made available directly so it can
 * be used to build a dom.
 * Note: there should be a better way to handle this and
 * the attribute list is only valid within the startElement
 * call of the sax events.
 */
public class DirectPropertyListBuilder extends PropertyListBuilder {

    public DirectPropertyListBuilder() {
    }

    public PropertyList makeList(String elementName, Attributes attributes,
                                 PropertyList parentPropertyList,
                                 FObj parentFO) throws FOPException {
        AttrPropertyList ret = new AttrPropertyList(attributes);
        return ret;
    }

    public class AttrPropertyList extends PropertyList {
        Attributes attributes;
        AttrPropertyList(Attributes attr) {
            super(null, null, null);
            attributes = attr;
        }

        public Attributes getAttributes() {
            return attributes;
        }
    }
}
