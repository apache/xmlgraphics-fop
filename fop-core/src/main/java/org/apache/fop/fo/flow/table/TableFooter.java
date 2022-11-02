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

package org.apache.fop.fo.flow.table;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;


/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_table-footer">
 * <code>fo:table-footer</code></a> object.
 */
public class TableFooter extends TablePart {

    /**
     * Create a TableFooter instance with the given {@link FONode}
     * as parent.
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public TableFooter(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startFooter(this);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endFooter(this);
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table-footer";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_TABLE_FOOTER}
     */
    public int getNameId() {
        return FO_TABLE_FOOTER;
    }

}
