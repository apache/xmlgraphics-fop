/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 30/01/2004
 * $Id$
 */
package org.apache.fop.area.inline;

import org.apache.fop.area.ReferenceArea;
import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class InlineContainer
extends InlineReferenceArea
implements ReferenceArea {

    /**
     * @param parent
     * @throws IndexOutOfBoundsException
     */
    public InlineContainer(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object areaSync) {
        super(pageSeq, generatedBy, parent, areaSync);
        // TODO Auto-generated constructor stub
    }

}
