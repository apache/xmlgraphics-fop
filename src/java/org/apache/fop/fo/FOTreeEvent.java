/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.fo;

//java
import java.util.EventObject;

//fop
import org.apache.fop.fo.pagination.PageSequence;

/**
 * An Event used for notification that various stages of the building of an
 * FO tree have been completed. Specifically, these are currently used to
 * notify Driver when a PageSequence has been completed.
 */

public class FOTreeEvent extends EventObject {

    private PageSequence pageSeq;

    /**
     * Constructor captures the object that fired the event.
     * @param source the Object that fired the event.
     */
    public FOTreeEvent (Object source) {
        super(source);
    }

    /**
     * Sets the PageSequence object for this event.
     * @param pageSeq the PageSequence object attached to this event.
     */
    public void setPageSequence(PageSequence pageSeq) {
        this.pageSeq = pageSeq;
    }

    /**
     * @return the PageSequence object attached to this event.
     */
    public PageSequence getPageSequence () {
        return pageSeq;
    }
}

