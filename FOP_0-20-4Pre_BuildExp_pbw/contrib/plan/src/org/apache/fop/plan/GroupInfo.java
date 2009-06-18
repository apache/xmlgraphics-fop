/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import java.util.ArrayList;

public class GroupInfo {
    String name;
    ArrayList actions = new ArrayList();

    public GroupInfo(String n) {
        name = n;
    }

    public void addActionInfo(ActionInfo ai) {
        actions.add(ai);
    }

    public int getSize() {
        return actions.size();
    }

    public ActionInfo getActionInfo(int c) {
        return (ActionInfo) actions.get(c);
    }

    public String getName() {
        return name;
    }
}
