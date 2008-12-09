/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.intermediate.extensions;

import java.util.Iterator;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.xmlgraphics.util.XMLizable;

/**
 * This class manages actions and action references. Some action (like {@link GoToXYAction}s)
 * cannot be fully resolved at the time they are needed, so they are deferred. This class
 * helps manages the references and resolution.
 */
public class ActionSet implements XMLizable {

    private int lastGeneratedID = 0;
    private Map actionRegistry = new java.util.HashMap();

    public synchronized String generateNewID(AbstractAction action) {
        this.lastGeneratedID++;
        String prefix = action.getIDPrefix();
        if (prefix == null) {
            throw new IllegalArgumentException("Action class is not compatible");
        }
        return prefix + this.lastGeneratedID;
    }

    public AbstractAction get(String id) {
        return (AbstractAction)this.actionRegistry.get(id);
    }

    public AbstractAction put(AbstractAction action) {
        if (!action.hasID()) {
            action.setID(generateNewID(action));
        }
        AbstractAction effAction = normalize(action);
        if (effAction == action) {
            this.actionRegistry.put(action.getID(), action);
        }
        return effAction;
    }

    public void clear() {
        this.actionRegistry.clear();
    }

    public AbstractAction normalize(AbstractAction action) {
        Iterator iter = this.actionRegistry.values().iterator();
        while (iter.hasNext()) {
            AbstractAction a = (AbstractAction)iter.next();
            if (a.isSame(action)) {
                return a;
            }
        }
        return action;
    }

    public Iterator getActions() {
        return this.actionRegistry.values().iterator();
    }

    public void toSAX(ContentHandler handler) throws SAXException {
        // TODO Auto-generated method stub

    }
}
