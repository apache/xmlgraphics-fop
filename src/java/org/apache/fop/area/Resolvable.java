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

package org.apache.fop.area;

import java.util.List;

/**
 * Resolvable Interface.  Classes that implement this interface contain
 * idrefs (see Section 5.11 of spec for definition of <idref> datatype)
 * that are resolved when their target IDs are added to the area tree.
 */
public interface Resolvable {

    /**
     * Check if this area has been resolved.
     *
     * @return true once this area is resolved
     */
    boolean isResolved();

    /**
     * Get the array of idrefs of this resolvable object.
     * If this object contains child resolvables that are
     * resolved through this then it should return the idref's of
     * the child also.
     *
     * @return the id references for resolving this object
     */
    String[] getIDRefs();

    /**
     * This method allows the Resolvable object to resolve one of
     * its unresolved idrefs with the actual set of PageViewports
     * containing the target ID.  The Resolvable object initially
     * identifies to the AreaTreeHandler which idrefs it needs
     * resolved.  After the idrefs are resolved, the ATH calls this
     * method to allow the Resolvable object to update itself with
     * the PageViewport information.
     *
     * @param id an ID matching one of the Resolvable object's
     *      unresolved idref's.
     * @param pages the list of PageViewports with the given ID
     *
     */
    void resolveIDRef(String id, List pages);
}
