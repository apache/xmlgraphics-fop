/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import java.util.Date;

public class ActionInfo {
    Date startDate;
    Date endDate;
    String owner;
    String label;
    int type = TASK;
    public static final int TASK = 1;
    public static final int MILESTONE = 2;
    public static final int GROUPING = 3;
    String dependant = "";

    public void setType(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }

    public void setLabel(String str) {
        label = str;
    }

    public void setOwner(String str) {
        owner = str;
    }

    public void setStartDate(Date sd) {
        startDate = sd;
        if (endDate == null) {
            endDate = startDate;
        }
    }

    public void setEndDate(Date ed) {
        endDate = ed;
    }

    public String getLabel() {
        return label;
    }

    public String getOwner() {
        return owner;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

}
