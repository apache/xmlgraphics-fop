/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;

/**
 * Abstract base class for PageMasterReference classes. Provides
 * implementation for handling the master-reference attribute and
 * containment within a PageSequenceMaster
 */
public abstract class PageMasterReference extends FObj
    implements SubSequenceSpecifier {

    protected String masterName;

    public PageMasterReference(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
    }

    public String getMasterName() {
        return masterName;
    }
  
    public abstract String getNextPageMasterName(boolean isOddPage,
                                                 boolean isFirstPage,
                                                 boolean isEmptyPage)
      throws FOPException;

    public abstract void reset();
}
