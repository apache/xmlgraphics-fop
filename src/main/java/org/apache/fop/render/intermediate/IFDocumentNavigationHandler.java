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

package org.apache.fop.render.intermediate;

import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.NamedDestination;


/**
 * Interface to handle document navigation features. This is an optional interface for
 * document handler implementations which support document navigation features.
 */
public interface IFDocumentNavigationHandler {

    /**
     * Renders a named destination.
     * @param destination the named destination
     * @throws IFException if an error occurs while handling this event
     */
    void renderNamedDestination(NamedDestination destination) throws IFException;

    /**
     * Render the bookmark tree.
     * @param tree the bookmark tree
     * @throws IFException if an error occurs while handling this event
     */
    void renderBookmarkTree(BookmarkTree tree) throws IFException;

    /**
     * @param link a link
     * @throws IFException of not caught
     */
    void renderLink(Link link) throws IFException;

    /**
     * @param action an action
     * @throws IFException of not caught
     */
    void addResolvedAction(AbstractAction action) throws IFException;

}
