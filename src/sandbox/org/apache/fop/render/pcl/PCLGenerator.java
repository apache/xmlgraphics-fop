/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class provides methods for generating PCL print files.
 */
public class PCLGenerator {

    /** The ESC (escape) character */
    public static final char ESC = '\033';
    
    /** A list of all supported resolutions in PCL (values in dpi) */
    public static final int[] PCL_RESOLUTIONS = new int[] {75, 100, 150, 200, 300, 600};
    
    private final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); 
    private final DecimalFormat df2 = new DecimalFormat("0.##", symbols);
    private final DecimalFormat df4 = new DecimalFormat("0.####", symbols);
    
    private OutputStream out;
    
    /**
     * Main constructor.
     * @param out the OutputStream to write the PCL stream to
     */
    public PCLGenerator(OutputStream out) {
        this.out = out;
    }
    
    /** @return the OutputStream that this generator writes to */
    public OutputStream getOutputStream() {
        return this.out;
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
     * Sets the cursor to a new absolute coordinate.
     * @param x the X coordinate (in millipoints)
     * @param y the Y coordinate (in millipoints)
     * @throws IOException In case of an I/O error
     */
    public void setCursorPos(int x, int y) throws IOException {
        writeCommand("*p" + (x / 100) + "h" + (y / 100) + "V");
    }

    /**
     * Generate a filled rectangle
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param col the fill color
     * @throws IOException In case of an I/O error
     */
    protected void fillRect(int x, int y, int w, int h, Color col) throws IOException {
        if ((w == 0) || (h == 0)) {
            return;
        }
        if (h < 0) {
            h *= -1;
        } else {
            //y += h;
        }

        int xpos = (x / 100);
        if (xpos < 0) {
            //A negative x coordinate can lead to a displaced rectangle (xpos must be >= 0) 
            w += x;
            xpos = 0;
        }
        writeCommand("*v1O");
        writeCommand("&a" + formatDouble4(xpos) + "h" 
                          + formatDouble4(y / 100) + "V");
        writeCommand("*c" + formatDouble4(w / 100) + "h" 
                          + formatDouble4(h / 100) + "V");
        int lineshade = convertToPCLShade(col);
        writeCommand("*c" + lineshade + "G");
        writeCommand("*c2P");
        // Reset pattern transparency mode.
        writeCommand("*v0O");
    }

    /**
     * Sets the pattern transparency mode.
     * @param transparent true if transparent, false for opaque
     * @throws IOException In case of an I/O error
     */
    public void setPatternTransparencyMode(boolean transparent) throws IOException {
        if (transparent) {
            writeCommand("*v0O");
        } else {
            writeCommand("*v1O");
        }
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
     * Select the current pattern
     * @param patternID the pattern ID (<ESC>*c#G command)
     * @param pattern the pattern type (<ESC>*v#T command)
     * @throws IOException In case of an I/O error
     */
    public void selectCurrentPattern(int patternID, int pattern) throws IOException {
        writeCommand("*c" + patternID + "G");
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
        for (int i = PCL_RESOLUTIONS.length - 2; i >= 0; i--) {
            if (resolution > PCL_RESOLUTIONS[i]) {
                int idx = i + 1;
                if (idx < PCL_RESOLUTIONS.length - 2) {
                    idx += increased ? 2 : 0;
                } else if (idx < PCL_RESOLUTIONS.length - 1) {
                    idx += increased ? 1 : 0;
                }
                return PCL_RESOLUTIONS[idx];
            }
        }
        return PCL_RESOLUTIONS[increased ? 2 : 0];
    }
    
    private boolean isValidPCLResolution(int resolution) {
        return resolution == calculatePCLResolution(resolution);
    }
    
    private Dimension getAdjustedDimension(Dimension orgDim, int orgResolution, 
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
    
    /**
     * Paint a bitmap at the current cursor position. The bitmap is converted to a monochrome
     * (1-bit) bitmap image.
     * @param img the bitmap image
     * @param resolution the original resolution of the image (in dpi)
     * @throws IOException In case of an I/O error
     */
    public void paintBitmap(RenderedImage img, int resolution) throws IOException {
        boolean monochrome = isMonochromeImage(img);
        if (!monochrome) {
            int effResolution = calculatePCLResolution(resolution, true);
            Dimension orgDim = new Dimension(img.getWidth(), img.getHeight());
            Dimension effDim = getAdjustedDimension(orgDim, resolution, effResolution);
            boolean scaled = !orgDim.equals(effDim);
            BufferedImage src = null;
            if (img instanceof BufferedImage && !scaled) {
                if (!isGrayscaleImage(img)) {
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

            long start = System.currentTimeMillis();
            BufferedImage buf = (BufferedImage)converter.convertToMonochrome(src);
            long duration = System.currentTimeMillis() - start;
            System.out.println(duration + " ms");
            
            RenderedImage red = buf;
            paintMonochromeBitmap(red, effResolution);
        } else {
            int effResolution = calculatePCLResolution(resolution);
            paintMonochromeBitmap(img, effResolution);
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
        writeCommand("*t" + resolution + "R");
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
        /*
        int xres = (iw * 72000) / w;
        int yres = (ih * 72000) / h;
        int resolution = xres;
        if (yres > xres)
            resolution = yres;

        if (resolution > 300)
            resolution = 600;
        else if (resolution > 150)
            resolution = 300;
        else if (resolution > 100)
            resolution = 150;
        else if (resolution > 75)
            resolution = 100;
        else
            resolution = 75;
            */

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
