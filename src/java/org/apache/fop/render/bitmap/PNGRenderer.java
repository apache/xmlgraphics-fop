package org.apache.fop.render.bitmap;

import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.ext.awt.image.codec.PNGEncodeParam;
import org.apache.batik.ext.awt.image.codec.PNGImageEncoder;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.render.java2d.Java2DRenderer;

/**
 * PNG Renderer This class actually does not render itself, instead it extends
 * <code>org.apache.fop.render.java2D.Java2DRenderer</code> and just encode
 * rendering results into PNG format using Batik's image codec
 */
public class PNGRenderer extends Java2DRenderer {

    /** The MIME type for png-Rendering */
    public static final String MIME_TYPE = "image/png";

    /** The file syntax prefix, eg. "page" will output "page1.png" etc */
    private String fileSyntax;

    /** The output directory where images are to be written */
    private File outputDir;

    /** The PNGEncodeParam for the image */
    private PNGEncodeParam renderParams;

    /** The OutputStream for the first Image */
    private OutputStream firstOutputStream;

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** default constructor */
    public PNGRenderer() {}

    /** @see org.apache.fop.render.Renderer#startRenderer(java.io.OutputStream) */
    public void startRenderer(OutputStream outputStream) throws IOException {
        getLogger().info("rendering areas to PNG");
        setOutputDirectory();
        this.firstOutputStream = outputStream;
    }

    /**
     * Sets the output directory, either from the outfile specified on the
     * command line, or from the directory specified in configuration file. Also
     * sets the file name syntax, eg. "page"
     */
    private void setOutputDirectory() {

        // the file provided on the command line
        File f = getUserAgent().getOutputFile();

        outputDir = f.getParentFile();

        // extracting file name syntax
        String s = f.getName();
        int i = s.lastIndexOf(".");
        if (s.charAt(i - 1) == '1') {
            i--; // getting rid of the "1"
        }
        fileSyntax = s.substring(0, i);
    }

    public void stopRenderer() throws IOException {

        super.stopRenderer();

        for (int i = 0; i < pageViewportList.size(); i++) {

            // Do the rendering: get the image for this page
            RenderedImage image = (RenderedImage) getPageImage((PageViewport) pageViewportList
                    .get(i));

            // Encode this image
            getLogger().debug("Encoding  Page " + (i + 1));
            renderParams = PNGEncodeParam.getDefaultEncodeParam(image);
            OutputStream os = getCurrentOutputStream(i);
            PNGImageEncoder encoder = new PNGImageEncoder(os, renderParams);
            encoder.encode(image);
            os.flush();
        }
    }

    /**
     * Builds the OutputStream corresponding to this page
     * @param 0-based pageNumber
     * @return the corresponding OutputStream
     */
    private OutputStream getCurrentOutputStream(int pageNumber) {

        if (pageNumber == 0) {
            return firstOutputStream;
        }

        File f = new File(outputDir + File.separator + fileSyntax
                + (pageNumber + 1) + ".png");
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            return os;
        } catch (FileNotFoundException e) {
            new FOPException("Can't build the OutputStream\n" + e);
            return null;
        }
    }
}
