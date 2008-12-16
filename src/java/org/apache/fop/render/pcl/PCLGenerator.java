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

package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.LookupOp;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.image.GraphicsUtil;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * This class provides methods for generating PCL print files.
 */
public class PCLGenerator {

    /** The ESC (escape) character */
    public static final char ESC = '\033';

    /** A list of all supported resolutions in PCL (values in dpi) */
    public static final int[] PCL_RESOLUTIONS = new int[] {75, 100, 150, 200, 300, 600};

    /** Selects a 4x4 Bayer dither matrix (17 grayscales) */
    public static final int DITHER_MATRIX_4X4 = 4;
    /** Selects a 8x8 Bayer dither matrix (65 grayscales) */
    public static final int DITHER_MATRIX_8X8 = 8;

    private final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    private final DecimalFormat df2 = new DecimalFormat("0.##", symbols);
    private final DecimalFormat df4 = new DecimalFormat("0.####", symbols);

    private final OutputStream out;

    private boolean currentSourceTransparency = true;
    private boolean currentPatternTransparency = true;

    private int maxBitmapResolution = PCL_RESOLUTIONS[PCL_RESOLUTIONS.length - 1];

    /**
     * true: Standard PCL shades are used (poor quality). false: user-defined pattern are used
     * to create custom dither patterns for better grayscale quality.
     */
    private final boolean usePCLShades = false;

    /**
     * Main constructor.
     * @param out the OutputStream to write the PCL stream to
     */
    public PCLGenerator(OutputStream out) {
        this.out = out;
    }

    /**
     * Main constructor.
     * @param out the OutputStream to write the PCL stream to
     * @param maxResolution the maximum resolution to encode bitmap images at
     */
    public PCLGenerator(OutputStream out, int maxResolution) {
        this(out);
        boolean found = false;
        for (int i = 0; i < PCL_RESOLUTIONS.length; i++) {
            if (PCL_RESOLUTIONS[i] == maxResolution) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Illegal value for maximum resolution!");
        }
        this.maxBitmapResolution = maxResolution;
    }

    /** @return the OutputStream that this generator writes to */
    public OutputStream getOutputStream() {
        return this.out;
    }

    /** @return the maximum resolution to encode bitmap images at */
    public int getMaximumBitmapResolution() {
        return this.maxBitmapResolution;
    }

    /**
     * Writes a PCL escape command to the output stream.
     * @param cmd the command (without the ESCAPE character)
     * @throws IOException In case of an I/O error
     */
    public void writeCommand(String cmd) throws IOException {
        out.write(27); //ESC
        out.write(cmd.getBytes("US-ASCII"));
    }

    /**
     * Writes raw text (in ISO-8859-1 encoding) to the output stream.
     * @param s the text
     * @throws IOException In case of an I/O error
     */
    public void writeText(String s) throws IOException {
        out.write(s.getBytes("ISO-8859-1"));
    }

    /**
     * Formats a double value with two decimal positions for PCL output.
     *
     * @param value value to format
     * @return the formatted value
     */
    public final String formatDouble2(double value) {
        return df2.format(value);
    }

    /**
     * Formats a double value with four decimal positions for PCL output.
     *
     * @param value value to format
     * @return the formatted value
     */
    public final String formatDouble4(double value) {
        return df4.format(value);
    }

    /**
     * Sends the universal end of language command (UEL).
     * @throws IOException In case of an I/O error
     */
    public void universalEndOfLanguage() throws IOException {
        writeCommand("%-12345X");
    }

    /**
     * Resets the printer and restores the user default environment.
     * @throws IOException In case of an I/O error
     */
    public void resetPrinter() throws IOException {
        writeCommand("E");
    }

    /**
     * Sends the job separation command.
     * @throws IOException In case of an I/O error
     */
    public void separateJobs() throws IOException {
        writeCommand("&l1T");
    }

    /**
     * Sends the form feed character.
     * @throws IOException In case of an I/O error
     */
    public void formFeed() throws IOException {
        out.write(12); //=OC ("FF", Form feed)
    }

    /**
     * Sets the unit of measure.
     * @param value the resolution value (units per inch)
     * @throws IOException In case of an I/O error
     */
    public void setUnitOfMeasure(int value) throws IOException {
        writeCommand("&u" + value + "D");
    }

    /**
     * Sets the raster graphics resolution
     * @param value the resolution value (units per inch)
     * @throws IOException In case of an I/O error
     */
    public void setRasterGraphicsResolution(int value) throws IOException {
        writeCommand("*t" + value + "R");
    }

    /**
     * Selects the page size.
     * @param selector the integer representing the page size
     * @throws IOException In case of an I/O error
     */
    public void selectPageSize(int selector) throws IOException {
        writeCommand("&l" + selector + "A");
    }

    /**
     * Selects the paper source. The parameter is usually printer-specific. Usually, "1" is the
     * default tray, "2" is the manual paper feed, "3" is the manual envelope feed, "4" is the
     * "lower" tray and "7" is "auto-select". Consult the technical reference for your printer
     * for all available values.
     * @param selector the integer representing the paper source/tray
     * @throws IOException In case of an I/O error
     */
    public void selectPaperSource(int selector) throws IOException {
        writeCommand("&l" + selector + "H");
    }

    /**
     * Selects the duplexing mode for the page.
     * The parameter is usually printer-specific.
     * "0" means Simplex,
     * "1" means Duplex, Long-Edge Binding,
     * "2" means Duplex, Short-Edge Binding.
     * @param selector the integer representing the duplexing mode of the page
     * @throws IOException In case of an I/O error
     */
    public void selectDuplexMode(int selector) throws IOException {
        writeCommand("&l" + selector + "S");
    }

    /**
     * Clears the horizontal margins.
     * @throws IOException In case of an I/O error
     */
    public void clearHorizontalMargins() throws IOException {
        writeCommand("9");
    }

    /**
     * The Top Margin command designates the number of lines between
     * the top of the logical page and the top of the text area.
     * @param numberOfLines the number of lines (See PCL specification for details)
     * @throws IOException In case of an I/O error
     */
    public void setTopMargin(int numberOfLines) throws IOException {
        writeCommand("&l" + numberOfLines + "E");
    }

    /**
     * The Text Length command can be used to define the bottom border. See the PCL specification
     * for details.
     * @param numberOfLines the number of lines
     * @throws IOException In case of an I/O error
     */
    public void setTextLength(int numberOfLines) throws IOException {
        writeCommand("&l" + numberOfLines + "F");
    }

    /**
     * Sets the Vertical Motion Index (VMI).
     * @param value the VMI value
     * @throws IOException In case of an I/O error
     */
    public void setVMI(double value) throws IOException {
        writeCommand("&l" + formatDouble4(value) + "C");
    }

    /**
     * Sets the cursor to a new absolute coordinate.
     * @param x the X coordinate (in millipoints)
     * @param y the Y coordinate (in millipoints)
     * @throws IOException In case of an I/O error
     */
    public void setCursorPos(double x, double y) throws IOException {
        if (x < 0) {
            //A negative x value will result in a relative movement so go to "0" first.
            //But this will most probably have no effect anyway since you can't paint to the left
            //of the logical page
            writeCommand("&a0h" + formatDouble2(x / 100) + "h" + formatDouble2(y / 100) + "V");
        } else {
            writeCommand("&a" + formatDouble2(x / 100) + "h" + formatDouble2(y / 100) + "V");
        }
    }

    /**
     * Pushes the current cursor position on a stack (stack size: max 20 entries)
     * @throws IOException In case of an I/O error
     */
    public void pushCursorPos() throws IOException {
        writeCommand("&f0S");
    }

    /**
     * Pops the current cursor position from the stack.
     * @throws IOException In case of an I/O error
     */
    public void popCursorPos() throws IOException {
        writeCommand("&f1S");
    }

    /**
     * Changes the current print direction while maintaining the current cursor position.
     * @param rotate the rotation angle (counterclockwise), one of 0, 90, 180 and 270.
     * @throws IOException In case of an I/O error
     */
    public void changePrintDirection(int rotate) throws IOException {
        writeCommand("&a" + rotate + "P");
    }

    /**
     * Enters the HP GL/2 mode.
     * @param restorePreviousHPGL2Cursor true if the previous HP GL/2 pen position should be
     *                                   restored, false if the current position is maintained
     * @throws IOException In case of an I/O error
     */
    public void enterHPGL2Mode(boolean restorePreviousHPGL2Cursor) throws IOException {
        if (restorePreviousHPGL2Cursor) {
            writeCommand("%0B");
        } else {
            writeCommand("%1B");
        }
    }

    /**
     * Enters the PCL mode.
     * @param restorePreviousPCLCursor true if the previous PCL cursor position should be restored,
     *                                 false if the current position is maintained
     * @throws IOException In case of an I/O error
     */
    public void enterPCLMode(boolean restorePreviousPCLCursor) throws IOException {
        if (restorePreviousPCLCursor) {
            writeCommand("%0A");
        } else {
            writeCommand("%1A");
        }
    }

    /**
     * Generate a filled rectangle at the current cursor position.
     *
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param col the fill color
     * @throws IOException In case of an I/O error
     */
    protected void fillRect(int w, int h, Color col) throws IOException {
        if ((w == 0) || (h == 0)) {
            return;
        }
        if (h < 0) {
            h *= -1;
        } else {
            //y += h;
        }
        setPatternTransparencyMode(false);
        if (usePCLShades
                || Color.black.equals(col)
                || Color.white.equals(col)) {
            writeCommand("*c" + formatDouble4(w / 100.0) + "h"
                              + formatDouble4(h / 100.0) + "V");
            int lineshade = convertToPCLShade(col);
            writeCommand("*c" + lineshade + "G");
            writeCommand("*c2P"); //Shaded fill
        } else {
            defineGrayscalePattern(col, 32, DITHER_MATRIX_4X4);

            writeCommand("*c" + formatDouble4(w / 100.0) + "h"
                              + formatDouble4(h / 100.0) + "V");
            writeCommand("*c32G");
            writeCommand("*c4P"); //User-defined pattern
        }
        // Reset pattern transparency mode.
        setPatternTransparencyMode(true);
    }

    //Bayer dither matrices (4x4 and 8x8 are derived from the 2x2 matrix)
    private static final int[] BAYER_D2 = new int[] {0, 2, 3, 1};
    private static final int[] BAYER_D4;
    private static final int[] BAYER_D8;

    static {
        BAYER_D4 = deriveBayerMatrix(BAYER_D2);
        BAYER_D8 = deriveBayerMatrix(BAYER_D4);
    }

    private static void setValueInMatrix(int[] dn, int half, int part, int idx, int value) {
        int xoff = (part & 1) * half;
        int yoff = (part & 2) * half * half;
        int matrixIndex = yoff + ((idx / half) * half * 2) + (idx % half) + xoff;
        dn[matrixIndex] = value;
    }

    private static int[] deriveBayerMatrix(int[] d) {
        int[] dn = new int[d.length * 4];
        int half = (int)Math.sqrt(d.length);
        for (int part = 0; part < 4; part++) {
            for (int i = 0, c = d.length; i < c; i++) {
                setValueInMatrix(dn, half, part, i, d[i] * 4 + BAYER_D2[part]);
            }
        }
        return dn;
    }

    /**
     * Generates a user-defined pattern for a dithering pattern matching the grayscale value
     * of the color given.
     * @param col the color to create the pattern for
     * @param patternID the pattern ID to use
     * @param ditherMatrixSize the size of the Bayer dither matrix to use (4 or 8 supported)
     * @throws IOException In case of an I/O error
     */
    public void defineGrayscalePattern(Color col, int patternID, int ditherMatrixSize)
            throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baout);
        data.writeByte(0); //Format
        data.writeByte(0); //Continuation
        data.writeByte(1); //Pixel Encoding
        data.writeByte(0); //Reserved
        data.writeShort(8); //Width in Pixels
        data.writeShort(8); //Height in Pixels
        //data.writeShort(600); //X Resolution (didn't manage to get that to work)
        //data.writeShort(600); //Y Resolution
        int gray255 = convertToGray(col.getRed(), col.getGreen(), col.getBlue());

        byte[] pattern;
        if (ditherMatrixSize == 8) {
            int gray65 = gray255 * 65 / 255;

            pattern = new byte[BAYER_D8.length / 8];

            for (int i = 0, c = BAYER_D8.length; i < c; i++) {
                boolean dot = !(BAYER_D8[i] < gray65 - 1);
                if (dot) {
                    int byteIdx = i / 8;
                    pattern[byteIdx] |= 1 << (i % 8);
                }
            }
        } else {
            int gray17 = gray255 * 17 / 255;

            //Since a 4x4 pattern did not work, the 4x4 pattern is applied 4 times to an
            //8x8 pattern. Maybe this could be changed to use an 8x8 bayer dither pattern
            //instead of the 4x4 one.
            pattern = new byte[BAYER_D4.length / 8 * 4];

            for (int i = 0, c = BAYER_D4.length; i < c; i++) {
                boolean dot = !(BAYER_D4[i] < gray17 - 1);
                if (dot) {
                    int byteIdx = i / 4;
                    pattern[byteIdx] |= 1 << (i % 4);
                    pattern[byteIdx] |= 1 << ((i % 4) + 4);
                    pattern[byteIdx + 4] |= 1 << (i % 4);
                    pattern[byteIdx + 4] |= 1 << ((i % 4) + 4);
                }
            }
        }
        data.write(pattern);
        if ((baout.size() % 2) > 0) {
            baout.write(0);
        }
        writeCommand("*c" + patternID + "G");
        writeCommand("*c" + baout.size() + "W");
        baout.writeTo(this.out);
        writeCommand("*c4Q"); //temporary pattern
    }

    /**
     * Sets the source transparency mode.
     * @param transparent true if transparent, false for opaque
     * @throws IOException In case of an I/O error
     */
    public void setSourceTransparencyMode(boolean transparent) throws IOException {
        setTransparencyMode(transparent, currentPatternTransparency);
    }

    /**
     * Sets the pattern transparency mode.
     * @param transparent true if transparent, false for opaque
     * @throws IOException In case of an I/O error
     */
    public void setPatternTransparencyMode(boolean transparent) throws IOException {
        setTransparencyMode(currentSourceTransparency, transparent);
    }

    /**
     * Sets the transparency modes.
     * @param source source transparency: true if transparent, false for opaque
     * @param pattern pattern transparency: true if transparent, false for opaque
     * @throws IOException In case of an I/O error
     */
    public void setTransparencyMode(boolean source, boolean pattern) throws IOException {
        if (source != currentSourceTransparency && pattern != currentPatternTransparency) {
            writeCommand("*v" + (source ? '0' : '1') + "n" + (pattern ? '0' : '1') + "O");
        } else if (source != currentSourceTransparency) {
            writeCommand("*v" + (source ? '0' : '1') + "N");
        } else if (pattern != currentPatternTransparency) {
            writeCommand("*v" + (pattern ? '0' : '1') + "O");
        }
        this.currentSourceTransparency = source;
        this.currentPatternTransparency = pattern;
    }

    /**
     * Convert an RGB color value to a grayscale from 0 to 100.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return the gray value
     */
    public final int convertToGray(int r, int g, int b) {
        return (r * 30 + g * 59 + b * 11) / 100;
    }

    /**
     * Convert a Color value to a PCL shade value (0-100).
     * @param col the color
     * @return the PCL shade value (100=black)
     */
    public final int convertToPCLShade(Color col) {
        float gray = convertToGray(col.getRed(), col.getGreen(), col.getBlue()) / 255f;
        return (int)(100 - (gray * 100f));
    }

    /**
     * Selects the current grayscale color (the given color is converted to grayscales).
     * @param col the color
     * @throws IOException In case of an I/O error
     */
    public void selectGrayscale(Color col) throws IOException {
        if (Color.black.equals(col)) {
            selectCurrentPattern(0, 0); //black
        } else if (Color.white.equals(col)) {
            selectCurrentPattern(0, 1); //white
        } else {
            if (usePCLShades) {
                selectCurrentPattern(convertToPCLShade(col), 2);
            } else {
                defineGrayscalePattern(col, 32, DITHER_MATRIX_4X4);
                selectCurrentPattern(32, 4);
            }
        }
    }

    /**
     * Select the current pattern
     * @param patternID the pattern ID (<ESC>*c#G command)
     * @param pattern the pattern type (<ESC>*v#T command)
     * @throws IOException In case of an I/O error
     */
    public void selectCurrentPattern(int patternID, int pattern) throws IOException {
        if (pattern > 1) {
            writeCommand("*c" + patternID + "G");
        }
        writeCommand("*v" + pattern + "T");
    }

    /**
     * Indicates whether an image is a monochrome (b/w) image.
     * @param img the image
     * @return true if it's a monochrome image
     */
    public static boolean isMonochromeImage(RenderedImage img) {
        ColorModel cm = img.getColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            return icm.getMapSize() == 2;
        } else {
            return false;
        }
    }

    /**
     * Indicates whether an image is a grayscale image.
     * @param img the image
     * @return true if it's a grayscale image
     */
    public static boolean isGrayscaleImage(RenderedImage img) {
        return (img.getColorModel().getColorSpace().getNumComponents() == 1);
    }

    private MonochromeBitmapConverter createMonochromeBitmapConverter() {
        MonochromeBitmapConverter converter = null;
        try {
            String clName = "org.apache.fop.render.pcl.JAIMonochromeBitmapConverter";
            Class clazz = Class.forName(clName);
            converter = (MonochromeBitmapConverter)clazz.newInstance();
        } catch (ClassNotFoundException cnfe) {
            // Class was not compiled so is not available. Simply ignore.
        } catch (LinkageError le) {
            // This can happen if fop was build with support for a
            // particular provider (e.g. a binary fop distribution)
            // but the required support files (i.e. JAI) are not
            // available in the current runtime environment.
            // Simply continue with the backup implementation.
        } catch (InstantiationException e) {
            // Problem instantiating the class, simply continue with the backup implementation
        } catch (IllegalAccessException e) {
            // Problem instantiating the class, simply continue with the backup implementation
        }
        if (converter == null) {
            converter = new DefaultMonochromeBitmapConverter();
        }
        return converter;
    }

    private int calculatePCLResolution(int resolution) {
        return calculatePCLResolution(resolution, false);
    }

    /**
     * Calculates the ideal PCL resolution for a given resolution.
     * @param resolution the input resolution
     * @param increased true if you want to go to a higher resolution, for example if you
     *                  convert grayscale or color images to monochrome images so dithering has
     *                  a chance to generate better quality.
     * @return the resulting PCL resolution (one of 75, 100, 150, 200, 300, 600)
     */
    private int calculatePCLResolution(int resolution, boolean increased) {
        int choice = -1;
        for (int i = PCL_RESOLUTIONS.length - 2; i >= 0; i--) {
            if (resolution > PCL_RESOLUTIONS[i]) {
                int idx = i + 1;
                if (idx < PCL_RESOLUTIONS.length - 2) {
                    idx += increased ? 2 : 0;
                } else if (idx < PCL_RESOLUTIONS.length - 1) {
                    idx += increased ? 1 : 0;
                }
                choice = idx;
                break;
                //return PCL_RESOLUTIONS[idx];
            }
        }
        if (choice < 0) {
            choice = (increased ? 2 : 0);
        }
        while (choice > 0 && PCL_RESOLUTIONS[choice] > getMaximumBitmapResolution()) {
            choice--;
        }
        return PCL_RESOLUTIONS[choice];
    }

    private boolean isValidPCLResolution(int resolution) {
        return resolution == calculatePCLResolution(resolution);
    }

    private Dimension getAdjustedDimension(Dimension orgDim, double orgResolution,
            int pclResolution) {
        if (orgResolution == pclResolution) {
            return orgDim;
        } else {
            Dimension result = new Dimension();
            result.width = (int)Math.round((double)orgDim.width * pclResolution / orgResolution);
            result.height = (int)Math.round((double)orgDim.height * pclResolution / orgResolution);
            return result;
        }
    }

    //Threshold table to convert an alpha channel (8-bit) into a clip mask (1-bit)
    private static final byte[] THRESHOLD_TABLE = new byte[256];
    static { // Initialize the arrays
        for (int i = 0; i < 256; i++) {
            THRESHOLD_TABLE[i] = (byte) ((i < 240) ? 255 : 0);
        }
    }

    private RenderedImage getMask(RenderedImage img, Dimension targetDim) {
        ColorModel cm = img.getColorModel();
        if (cm.hasAlpha()) {
            BufferedImage alpha = new BufferedImage(img.getWidth(), img.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY);
            Raster raster = img.getData();
            GraphicsUtil.copyBand(raster, cm.getNumColorComponents(), alpha.getRaster(), 0);

            BufferedImageOp op1 = new LookupOp(new ByteLookupTable(0, THRESHOLD_TABLE), null);
            BufferedImage alphat = op1.filter(alpha, null);

            BufferedImage mask;
            if (true) {
                mask = new BufferedImage(targetDim.width, targetDim.height,
                        BufferedImage.TYPE_BYTE_BINARY);
            } else {
                byte[] arr = {(byte)0, (byte)0xff};
                ColorModel colorModel = new IndexColorModel(1, 2, arr, arr, arr);
                WritableRaster wraster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                                                   targetDim.width, targetDim.height, 1, 1, null);
                mask = new BufferedImage(colorModel, wraster, false, null);
            }

            Graphics2D g2d = mask.createGraphics();
            try {
                AffineTransform at = new AffineTransform();
                double sx = targetDim.getWidth() / img.getWidth();
                double sy = targetDim.getHeight() / img.getHeight();
                at.scale(sx, sy);
                g2d.drawRenderedImage(alphat, at);
            } finally {
                g2d.dispose();
            }
            /*
            try {
                BatchDiffer.saveAsPNG(alpha, new java.io.File("D:/out-alpha.png"));
                BatchDiffer.saveAsPNG(mask, new java.io.File("D:/out-mask.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            return mask;
        } else {
            return null;
        }
    }

    /**
     * Paint a bitmap at the current cursor position. The bitmap is converted to a monochrome
     * (1-bit) bitmap image.
     * @param img the bitmap image
     * @param targetDim the target Dimention (in mpt)
     * @param sourceTransparency true if the background should not be erased
     * @throws IOException In case of an I/O error
     */
    public void paintBitmap(RenderedImage img, Dimension targetDim, boolean sourceTransparency)
                throws IOException {
        double targetResolution = img.getWidth() / UnitConv.mpt2in(targetDim.width);
        int resolution = (int)Math.round(targetResolution);
        int effResolution = calculatePCLResolution(resolution, true);
        Dimension orgDim = new Dimension(img.getWidth(), img.getHeight());
        Dimension effDim = getAdjustedDimension(orgDim, targetResolution, effResolution);
        boolean scaled = !orgDim.equals(effDim);

        boolean monochrome = isMonochromeImage(img);
        if (!monochrome) {
            //Transparency mask disabled. Doesn't work reliably
            final boolean transparencyDisabled = true;
            RenderedImage mask = (transparencyDisabled ? null : getMask(img, effDim));
            if (mask != null) {
                pushCursorPos();
                selectCurrentPattern(0, 1); //Solid white
                setTransparencyMode(true, true);
                paintMonochromeBitmap(mask, effResolution);
                popCursorPos();
            }

            BufferedImage src = null;
            if (img instanceof BufferedImage && !scaled) {
                if (!isGrayscaleImage(img) || img.getColorModel().hasAlpha()) {
                    src = new BufferedImage(effDim.width, effDim.height,
                            BufferedImage.TYPE_BYTE_GRAY);
                    ColorConvertOp op = new ColorConvertOp(
                            ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                    op.filter((BufferedImage)img, src);
                } else {
                    src = (BufferedImage)img;
                }
            }
            if (src == null) {
                src = new BufferedImage(effDim.width, effDim.height,
                        BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g2d = src.createGraphics();
                try {
                    g2d.setBackground(Color.white);
                    g2d.setColor(Color.black);
                    g2d.clearRect(0, 0, effDim.width, effDim.height);

                    AffineTransform at = new AffineTransform();
                    double sx = effDim.getWidth() / orgDim.getWidth();
                    double sy = effDim.getHeight() / orgDim.getHeight();
                    at.scale(sx, sy);
                    g2d.drawRenderedImage(img, at);
                } finally {
                    g2d.dispose();
                }
            }
            MonochromeBitmapConverter converter = createMonochromeBitmapConverter();
            converter.setHint("quality", "false");

            BufferedImage buf = (BufferedImage)converter.convertToMonochrome(src);

            RenderedImage red = buf;
            selectCurrentPattern(0, 0); //Solid black
            setTransparencyMode(sourceTransparency || mask != null, true);
            paintMonochromeBitmap(red, effResolution);
        } else {
            //TODO untested!
            RenderedImage effImg = img;
            if (scaled) {
                BufferedImage buf = new BufferedImage(effDim.width, effDim.height,
                        BufferedImage.TYPE_BYTE_BINARY);
                Graphics2D g2d = buf.createGraphics();
                try {
                    AffineTransform at = new AffineTransform();
                    double sx = effDim.getWidth() / orgDim.getWidth();
                    double sy = effDim.getHeight() / orgDim.getHeight();
                    at.scale(sx, sy);
                    g2d.drawRenderedImage(img, at);
                } finally {
                    g2d.dispose();
                }
                effImg = buf;
            }
            setSourceTransparencyMode(sourceTransparency);
            selectCurrentPattern(0, 0); //Solid black
            paintMonochromeBitmap(effImg, effResolution);
        }
    }

    /**
     * Paint a bitmap at the current cursor position. The bitmap must be a monochrome
     * (1-bit) bitmap image.
     * @param img the bitmap image (must be 1-bit b/w)
     * @param resolution the resolution of the image (must be a PCL resolution)
     * @throws IOException In case of an I/O error
     */
    public void paintMonochromeBitmap(RenderedImage img, int resolution) throws IOException {
        if (!isValidPCLResolution(resolution)) {
            throw new IllegalArgumentException("Invalid PCL resolution: " + resolution);
        }
        setRasterGraphicsResolution(resolution);
        writeCommand("*r0f" + img.getHeight() + "t" + img.getWidth() + "s1A");
        Raster raster = img.getData();
        boolean monochrome = isMonochromeImage(img);
        if (!monochrome) {
            throw new IllegalArgumentException("img must be a monochrome image");
        }

        int x = 0;
        int y = 0;
        int imgw = img.getWidth();
        int imgh = img.getHeight();
        int bytewidth = (imgw / 8);
        if ((imgw % 8) != 0) {
            bytewidth++;
        }
        byte ib;
        byte[] rle = new byte[bytewidth * 2]; //compressed (RLE)
        byte[] uncompressed = new byte[bytewidth]; //uncompressed
        int lastcount = -1;
        byte lastbyte = 0;
        int rlewidth = 0;

        // Transfer graphics data
        for (y = 0; y < imgh; y++) {
            ib = 0;
            for (x = 0; x < imgw; x++) {
                int sample = raster.getSample(x, y, 0);
                //Set image bit for black
                if ((sample == 0)) {
                    ib |= (1 << (7 - (x % 8)));
                }

                //RLE encoding
                if ((x % 8) == 7 || ((x + 1) == imgw)) {
                    if (rlewidth < bytewidth) {
                        if (lastcount >= 0) {
                            if (ib == lastbyte) {
                                lastcount++;
                            } else {
                                rle[rlewidth++] = (byte)(lastcount & 0xFF);
                                rle[rlewidth++] = lastbyte;
                                lastbyte = ib;
                                lastcount = 0;
                            }
                        } else {
                            lastbyte = ib;
                            lastcount = 0;
                        }
                        if (lastcount == 255 || ((x + 1) == imgw)) {
                            rle[rlewidth++] = (byte)(lastcount & 0xFF);
                            rle[rlewidth++] = lastbyte;
                            lastbyte = 0;
                            lastcount = -1;
                        }
                    }
                    uncompressed[x / 8] = ib;
                    ib = 0;
                }
            }
            if (rlewidth < bytewidth) {
                writeCommand("*b1m" + rlewidth + "W");
                this.out.write(rle, 0, rlewidth);
            } else {
                writeCommand("*b0m" + bytewidth + "W");
                this.out.write(uncompressed);
            }
            lastcount = -1;
            rlewidth = 0;
        }

        // End raster graphics
        writeCommand("*rB");
    }

}
