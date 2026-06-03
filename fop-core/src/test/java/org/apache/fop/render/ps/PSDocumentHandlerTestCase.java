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
package org.apache.fop.render.ps;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.ps.extensions.PSSetPageDevice;

public class PSDocumentHandlerTestCase {
    @Test
    public void testPSSetPageDeviceError() throws Exception {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        final Event[] events = new Event[1];
        userAgent.getEventBroadcaster().addEventListener(new EventListener() {
            public void processEvent(Event event) {
                events[0] = event;
            }
        });
        PSSetPageDevice setPageDevice = new PSSetPageDevice("test");
        new PSDocumentHandler(new IFContext(userAgent)).handleExtensionObject(setPageDevice);
        Assert.assertEquals("ps:ps-setpagedevice value: test", events[0].getParams().get("content"));
    }
}
