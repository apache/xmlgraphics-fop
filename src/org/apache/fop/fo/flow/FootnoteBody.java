/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.AreaClass;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;

// Java
import java.util.Iterator;

public class FootnoteBody extends FObj {

    int align;
    int alignLast;
    int lineHeight;
    int startIndent;
    int endIndent;
    int textIndent;

    public FootnoteBody(FONode parent) {
        super(parent);
    }

}
