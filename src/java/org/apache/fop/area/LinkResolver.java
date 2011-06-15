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

// Java
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Link resolving for resolving internal links.
 */
public class LinkResolver implements Resolvable, Serializable {

    private static final long serialVersionUID = -7102134165192960718L;

    private boolean resolved = false;
    private String idRef;
    private Area area;
    private transient List<Resolvable> dependents = null;

    /**
     * Create a new link resolver.
     *
     * @param id the id to resolve
     * @param a the area that will have the link attribute
     */
    public LinkResolver(String id, Area a) {
        idRef = id;
        area = a;
    }

    /**
     * @return true if this link is resolved
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Get the references for this link.
     *
     * @return the String array of references.
     */
    public String[] getIDRefs() {
        return new String[] {idRef};
    }

    /**
     * Resolve by adding an internal link to the first PageViewport in the list.
     *
     * {@inheritDoc}
     */
    public void resolveIDRef(String id, List<PageViewport> pages) {
        resolveIDRef(id, pages.get(0));
    }

    /**
     * Resolve by adding an InternalLink trait to the area
     *
     * @param id the target id (should be equal to the object's idRef)
     * @param pv the PageViewport containing the first area with the given id
     */
    public void resolveIDRef(String id, PageViewport pv) {
        if (idRef.equals(id) && pv != null) {
            resolved = true;
            if ( area != null ) {
                Trait.InternalLink iLink = new Trait.InternalLink(pv.getKey(), idRef);
                area.addTrait(Trait.INTERNAL_LINK, iLink);
                area = null; // break circular reference from basic link area to this resolver
            }
            resolveDependents(id, pv);
        }
    }

    /**
     * Add dependent resolvable. Used to resolve second-order resolvables that
     * depend on resolution of this resolver.
     * @param dependent resolvable
     */
    public void addDependent(Resolvable dependent) {
        if ( dependents == null ) {
            dependents = new ArrayList<Resolvable>();
        }
        dependents.add(dependent);
    }

    private void resolveDependents(String id, PageViewport pv) {
        if ( dependents != null ) {
            List<PageViewport> pages = new ArrayList<PageViewport>();
            pages.add(pv);
            for ( Resolvable r : dependents ) {
                r.resolveIDRef(id, pages);
            }
        }
    }

}
