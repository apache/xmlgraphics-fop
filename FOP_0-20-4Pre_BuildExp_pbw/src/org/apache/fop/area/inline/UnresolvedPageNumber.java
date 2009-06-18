/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import java.util.ArrayList;

public class UnresolvedPageNumber extends Unresolved {
    String pageRefId;

    public UnresolvedPageNumber(String id) {
        pageRefId = id;
    }

    public String[] getIDs() {
        return new String[] {pageRefId};
    }

    public void resolve(String id, ArrayList pages) {
        resolved = true;
    }
}
