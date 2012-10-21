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

package org.apache.fop.apps;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.Block;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.layoutmgr.ExternalDocumentLayoutManager;
import org.apache.fop.layoutmgr.FlowLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;
import org.apache.fop.layoutmgr.StaticContentLayoutManager;
import org.apache.fop.layoutmgr.inline.ContentLayoutManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link FopFactoryBuilder}.
 */
public class FopFactoryBuilderTestCase {

    private FopFactoryBuilder defaultBuilder;
    private static final String POST_SET_ERROR_MSG = "Should not be able to set any properties"
            + " once the builder has built a FopFactory.";

    @Before
    public void setUp() {
        defaultBuilder = new FopFactoryBuilder(URI.create("."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullParamsInConstructor() throws URISyntaxException {
        new FopFactoryBuilder(null, ResourceResolverFactory.createDefaultResourceResolver());
    }

    @Test
    public void testDefaultImplementation() {
        testDefaults(defaultBuilder.build(), URI.create("."));
    }

    private FopFactory buildFopFactory() {
        return defaultBuilder.build();
    }

    public static void testDefaults(FopFactory factory, URI baseURI) {
        assertFalse(factory.isAccessibilityEnabled());
        assertNull(factory.getLayoutManagerMakerOverride());
        assertEquals(FopFactoryConfig.DEFAULT_STRICT_FO_VALIDATION, factory.validateStrictly());
        assertEquals(FopFactoryConfig.DEFAULT_STRICT_USERCONFIG_VALIDATION,
                factory.validateUserConfigStrictly());
        assertEquals(FopFactoryConfig.DEFAULT_BREAK_INDENT_INHERITANCE,
                factory.isBreakIndentInheritanceOnReferenceAreaBoundary());
        assertEquals(FopFactoryConfig.DEFAULT_SOURCE_RESOLUTION, factory.getSourceResolution(),
                0.001f);
        assertEquals(FopFactoryConfig.DEFAULT_TARGET_RESOLUTION, factory.getTargetResolution(),
                0.001f);
        assertEquals(FopFactoryConfig.DEFAULT_PAGE_HEIGHT, factory.getPageHeight());
        assertEquals(FopFactoryConfig.DEFAULT_PAGE_WIDTH, factory.getPageWidth());
        assertFalse(factory.getRendererFactory().isRendererPreferred());
    }

    @Test
    public void testSetGetAccessibility() {
        runSetterTest(new Runnable() {
            public void run() {
                defaultBuilder.setAccessibility(true);
                assertTrue(buildFopFactory().isAccessibilityEnabled());
            }
        });
    }

    @Test
    public void testsetGetLMM() {
        runSetterTest(new Runnable() {
            public void run() {
                LayoutManagerMaker testLmm = new LayoutManagerMaker() {

                    public StaticContentLayoutManager makeStaticContentLayoutManager(
                            PageSequenceLayoutManager pslm, StaticContent sc, Block block) {
                        return null;
                    }

                    public StaticContentLayoutManager makeStaticContentLayoutManager(
                            PageSequenceLayoutManager pslm, StaticContent sc, SideRegion reg) {
                        return null;
                    }

                    public PageSequenceLayoutManager makePageSequenceLayoutManager(AreaTreeHandler ath,
                            PageSequence ps) {
                        return null;
                    }

                    public void makeLayoutManagers(FONode node, List lms) {
                    }

                    public LayoutManager makeLayoutManager(FONode node) {
                        return null;
                    }

                    public FlowLayoutManager makeFlowLayoutManager(PageSequenceLayoutManager pslm,
                            Flow flow) {
                        return null;
                    }

                    public ExternalDocumentLayoutManager makeExternalDocumentLayoutManager(
                            AreaTreeHandler ath, ExternalDocument ed) {
                        return null;
                    }

                    public ContentLayoutManager makeContentLayoutManager(PageSequenceLayoutManager pslm,
                            Title title) {
                        return null;
                    }
                };
                defaultBuilder.setLayoutManagerMakerOverride(testLmm);
                assertEquals(testLmm, buildFopFactory().getLayoutManagerMakerOverride());
            }
        });

    }

    @Test
    public void testSetGetBaseURI() {
        runSetterTest(new Runnable() {
            public void run() {
                URI nonDefaultURI = URI.create("./test/");
                defaultBuilder.setBaseURI(nonDefaultURI);
                assertEquals(nonDefaultURI, defaultBuilder.buildConfiguration().getBaseURI());
            }
        });
    }

    @Test
    public void testGetSetValidateFO() {
        runSetterTest(new Runnable() {
            public void run() {
                defaultBuilder.setStrictFOValidation(false);
                assertFalse(buildFopFactory().validateStrictly());
            }
        });
    }

    @Test
    public void testGetSetValidateUserConfig() {
        runSetterTest(new Runnable() {
            public void run() {
                defaultBuilder.setStrictUserConfigValidation(false);
                assertFalse(buildFopFactory().validateUserConfigStrictly());
            }
        });
    }

    @Test
    public void testGetSetBreakInheritance() {
        runSetterTest(new Runnable() {
            public void run() {
                defaultBuilder.setBreakIndentInheritanceOnReferenceAreaBoundary(true);
                assertTrue(buildFopFactory().isBreakIndentInheritanceOnReferenceAreaBoundary());
            }
        });
    }

    @Test
    public void testGetSetSourceRes() {
        runSetterTest(new Runnable() {
            public void run() {
                float testRes = 10f;
                defaultBuilder.setSourceResolution(testRes);
                assertEquals(testRes, buildFopFactory().getSourceResolution(), 0.0001);
            }
        });
    }

    @Test
    public void testGetSetTargetRes() {
        runSetterTest(new Runnable() {
            public void run() {
                float testRes = 10f;
                defaultBuilder.setTargetResolution(testRes);
                assertEquals(testRes, buildFopFactory().getTargetResolution(), 0.0001f);
            }
        });
    }

    @Test
    public void testGetSetPageHeight() {
        runSetterTest(new Runnable() {
            public void run() {
                String testString = "Purely for testing";
                defaultBuilder.setPageHeight(testString);
                assertEquals(testString, buildFopFactory().getPageHeight());
            }
        });
    }

    @Test
    public void testGetSetPageWidth() {
        runSetterTest(new Runnable() {
            public void run() {
                String testString = "Purely for testing";
                defaultBuilder.setPageWidth(testString);
                assertEquals(testString, buildFopFactory().getPageWidth());
            }
        });
    }

    @Test
    public void testGetSetIsNamespaceIgnored() {
        runSetterTest(new Runnable() {
            public void run() {
                String testString = "Purely for testing";
                defaultBuilder.ignoreNamespace(testString);
                assertTrue(buildFopFactory().isNamespaceIgnored(testString));
            }
        });
    }

    @Test
    public void testGetSetListNamespaceIgnored() {
        runSetterTest(new Runnable() {
            public void run() {
                List<String> strings = new ArrayList<String>();
                strings.add("1");
                strings.add("2");
                strings.add("3");
                defaultBuilder.ignoreNamespaces(strings);
                FopFactory factory = buildFopFactory();
                assertTrue(factory.isNamespaceIgnored("1"));
                assertTrue(factory.isNamespaceIgnored("2"));
                assertTrue(factory.isNamespaceIgnored("3"));
            }
        });
    }

    @Test
    public void testGetSetPreferRenderer() {
        runSetterTest(new Runnable() {
            public void run() {
                defaultBuilder.setPreferRenderer(true);
                assertTrue(buildFopFactory().getRendererFactory().isRendererPreferred());
            }
        });
    }

    private void runSetterTest(Runnable setterTest) {
        setterTest.run();
        try {
            setterTest.run();
            fail(POST_SET_ERROR_MSG);
        } catch (IllegalStateException e) {
            // Expected
        }
    }
}
