package org.apache.fop.layoutmgr.inline;

import java.awt.Dimension;

import org.junit.Before;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.properties.EnumLength;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.Property;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageLayoutTestCase {

    private AbstractGraphics gfxProps;

    /**
     * Initializes the resources needed to run the scaling unit test
     */
    @Before
    public void setUp() {
        gfxProps = mock(AbstractGraphics.class);

        Property enumProp = EnumProperty.getInstance(9, "AUTO");
        Length len = new EnumLength(enumProp);
        len.getEnum();

        LengthRangeProperty lenRangeProp = mock(LengthRangeProperty.class);
        when(lenRangeProp.getMinimum(null)).thenReturn(enumProp);
        when(lenRangeProp.getMaximum(null)).thenReturn(enumProp);
        when(lenRangeProp.getOptimum(null)).thenReturn(enumProp);

        when(gfxProps.getBlockProgressionDimension()).thenReturn(lenRangeProp);
        when(gfxProps.getInlineProgressionDimension()).thenReturn(lenRangeProp);

        //All values should be set to AUTO to test when no content-width or height has been specified
        when(gfxProps.getBlockProgressionDimension().getOptimum(null)).thenReturn(enumProp);
        when(gfxProps.getBlockProgressionDimension().getOptimum(null).getLength()).thenReturn(len);
        when(gfxProps.getBlockProgressionDimension().getMinimum(null)).thenReturn(enumProp);
        when(gfxProps.getBlockProgressionDimension().getMinimum(null).getLength()).thenReturn(len);
        when(gfxProps.getBlockProgressionDimension().getMaximum(null)).thenReturn(enumProp);
        when(gfxProps.getBlockProgressionDimension().getMaximum(null).getLength()).thenReturn(len);
        when(gfxProps.getInlineProgressionDimension().getOptimum(null)).thenReturn(enumProp);
        when(gfxProps.getInlineProgressionDimension().getOptimum(null).getLength()).thenReturn(len);
        when(gfxProps.getInlineProgressionDimension().getMinimum(null)).thenReturn(enumProp);
        when(gfxProps.getInlineProgressionDimension().getMinimum(null).getLength()).thenReturn(len);
        when(gfxProps.getInlineProgressionDimension().getMaximum(null)).thenReturn(enumProp);
        when(gfxProps.getInlineProgressionDimension().getMaximum(null).getLength()).thenReturn(len);

        when(gfxProps.getContentWidth()).thenReturn(len);
        when(gfxProps.getContentHeight()).thenReturn(len);
    }

    /**
     * Tests different levels of scaling to see if they match an expected result
     */
    @Test
    public void testImageScaling() {
        testScaling(114.0f, new Dimension(990000, 765000), 625263.0f, 483157.0f);
        testScaling(96.0f, new Dimension(990000, 765000), 742500.0f, 573750.0f);
        testScaling(72.0f, new Dimension(990000, 765000), 990000.0f, 765000.0f);
    }

    private void testScaling(float sourceResolution, Dimension intrinsicSize,
            float targetWidth, float targetHeight) {
        FOUserAgent userAgent = mock(FOUserAgent.class);
        when(userAgent.getSourceResolution()).thenReturn(sourceResolution);
        when(gfxProps.getUserAgent()).thenReturn(userAgent);

        ImageLayout imgLayout = new ImageLayout(gfxProps, null, intrinsicSize);
        assertEquals(imgLayout.getViewportSize().getWidth(), targetWidth, 0.0f);
        assertEquals(imgLayout.getViewportSize().getHeight(), targetHeight, 0.0f);
    }
}
