package org.apache.fop.tagging;

import org.apache.fop.check.Check;
import org.w3c.dom.Document;

/**
 * Check interface for PDF tagging checks.
 */
public interface PdfTaggingCheck extends Check {

    /**
     * Called to perform the check.
     * @param pdfTagging the PDF tagging tree, as defined by accessibility tags
     */
    void check(Document pdfTagging);

}
