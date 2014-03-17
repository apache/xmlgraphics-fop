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

package org.apache.fop.fo;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.flow.table.ColumnNumberManager;
import org.apache.fop.fo.flow.table.ColumnNumberManagerHolder;

/**
 * A helper class for creating mocks of {@link FONode} and its descendants.
 */
public final class FONodeMocks {

    private FONodeMocks() { }

    /**
     * Creates and returns a mock {@link FONode} configured with a mock
     * {@link FOEventHandler}. The FO event handler returns a mock {@link FOUserAgent},
     * which in turn returns a mock {@link org.apache.fop.apps.FopFactory}, which returns a mock
     * {@link ImageManager}.
     *
     * @return a mock FO node
     */
    public static FONode mockFONode() {
        FONode mockFONode = mock(FONode.class);
        mockGetFOEventHandler(mockFONode);
        mockGetImageManager(mockFONode.getFOEventHandler().getUserAgent());
        return mockFONode;
    }

    public static FOEventHandler mockGetFOEventHandler(FONode mockFONode) {
        FOEventHandler mockFOEventHandler = mock(FOEventHandler.class);
        mockGetUserAgent(mockFOEventHandler);
        when(mockFONode.getFOEventHandler()).thenReturn(mockFOEventHandler);
        return mockFOEventHandler;
    }

    public static FOUserAgent mockGetUserAgent(FOEventHandler mockFOEventHandler) {
        FOUserAgent mockFOUserAgent = mock(FOUserAgent.class);
        when(mockFOEventHandler.getUserAgent()).thenReturn(mockFOUserAgent);
        return mockFOUserAgent;
    }

    public static EventBroadcaster mockGetEventBroadcaster(FOUserAgent mockFOUserAgent) {
        EventBroadcaster mockBroadcaster = mock(EventBroadcaster.class);
        when(mockFOUserAgent.getEventBroadcaster()).thenReturn(mockBroadcaster);
        return mockBroadcaster;
    }

    public static ImageManager mockGetImageManager(FOUserAgent mockFOUserAgent) {
        try {
            ImageManager mockImageManager = mock(ImageManager.class);
            when(mockImageManager.getImageInfo(anyString(), any(ImageSessionContext.class)))
                    .thenReturn(null);
            when(mockFOUserAgent.getImageManager()).thenReturn(mockImageManager);
            return mockImageManager;
        } catch (ImageException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ColumnNumberManager mockGetColumnNumberManager(ColumnNumberManagerHolder mock) {
        ColumnNumberManager mockColumnNumberManager = mock(ColumnNumberManager.class);
        when(mock.getColumnNumberManager()).thenReturn(mockColumnNumberManager);
        return mockColumnNumberManager;
    }

}
