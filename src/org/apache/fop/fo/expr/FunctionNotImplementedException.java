/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

public class FunctionNotImplementedException extends PropertyException {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public FunctionNotImplementedException(String detail) {
        super(detail);
    }

    public FunctionNotImplementedException(Throwable e) {
        super(e);
    }

}
