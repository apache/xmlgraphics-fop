/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import org.apache.fop.fo.*;

import java.util.HashMap;

public class PlanElementMapping implements ElementMapping {

    public static final String URI = "http://xml.apache.org/fop/plan";

    private static HashMap foObjs = null;

    private static synchronized void setupPlan() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("plan", new PE());
            foObjs.put(DEFAULT, new PlanMaker());
        }
    }

    public void addToBuilder(FOTreeBuilder builder) {
        setupPlan();
        builder.addMapping(URI, foObjs);
    }

    static class PlanMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PlanObj(parent);
        }
    }

    static class PE extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PlanElement(parent);
        }
    }

}
