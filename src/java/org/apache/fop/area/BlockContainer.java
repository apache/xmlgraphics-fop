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
package org.apache.fop.area;

import org.apache.fop.datastructs.Node;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class BlockContainer extends BlockArea implements ReferenceArea {

    /**
     * @param parent
     * @param index
     * @throws IndexOutOfBoundsException
     */
    public BlockContainer(Node parent, int index)
        throws IndexOutOfBoundsException {
        super(parent, index);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param parent
     * @throws IndexOutOfBoundsException
     */
    public BlockContainer(Node parent) throws IndexOutOfBoundsException {
        super(parent);
        // TODO Auto-generated constructor stub
    }

}
