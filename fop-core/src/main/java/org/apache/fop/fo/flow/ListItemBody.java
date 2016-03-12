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

package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;

/**
 * Class modelling the <a href=http://www.w3.org/TR/xsl/#fo_list-item-body">
 * <code>fo:list-item-body object</code></a>.
 */
public class ListItemBody extends AbstractListItemPart {

    /**
     * @param parent FONode that is the parent of this object
     */
    public ListItemBody(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startListBody(this);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endListBody(this);
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "list-item-body";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_LIST_ITEM_BODY}
     */
    public int getNameId() {
        return FO_LIST_ITEM_BODY;
    }
}

