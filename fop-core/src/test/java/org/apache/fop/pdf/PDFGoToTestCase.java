package org.apache.fop.pdf;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PDFGoToTestCase {

    @Test
    public void test() {
        PDFGoTo pdfGoTo = new PDFGoTo("destination", true);
        String expected = "<< /Type /Action\n"
                + "/S /GoTo\n/D (destination)\n"
                + ">>";
        assertEquals(expected, pdfGoTo.toPDFString());
    }
}
