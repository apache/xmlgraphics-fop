/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;

import org.apache.batik.dom.svg.SVGDOMImplementation;

public class EventList {
    ArrayList data = new ArrayList();

    public void addGroupInfo(GroupInfo set) {
        data.add(set);
    }

    public int getSize() {
        return data.size();
    }

    public GroupInfo getGroupInfo(int count) {
        return (GroupInfo) data.get(count);
    }
}
