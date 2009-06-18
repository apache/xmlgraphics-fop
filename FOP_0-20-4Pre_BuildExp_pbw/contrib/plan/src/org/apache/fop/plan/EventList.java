/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import java.util.ArrayList;

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
