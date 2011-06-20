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

/* $Id:$ */

package org.apache.fop.render.intermediate;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.RendererEventProducer;
import org.apache.fop.render.intermediate.util.IFDocumentHandlerProxy;

/**
 * A filter that uses the Event Notification System to broadcast IF events.
 *
 */
public class EventProducingFilter extends IFDocumentHandlerProxy {

    private int pageNumberEnded;

    private FOUserAgent userAgent;

    /**
     * Constructor
     * @param ifDocumentHandler the IFDocumentHandler to filter
     * @param userAgent the FOUerAgent
     */
    public EventProducingFilter(IFDocumentHandler ifDocumentHandler, FOUserAgent userAgent) {
        super(ifDocumentHandler);
        this.userAgent = userAgent;
    }

    @Override
    public void endPage() throws IFException {
        super.endPage();
        pageNumberEnded++;
        RendererEventProducer.Provider.get(userAgent.getEventBroadcaster())
                .endPage(this, pageNumberEnded);
    }

}