package org.apache.fop.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DefaultConfigurationBuilder {

    private static final Log LOG = LogFactory.getLog(DefaultConfigurationBuilder.class.getName());

    public DefaultConfiguration build(InputStream confStream) throws ConfigurationException {
        try {
            DocumentBuilder builder = DefaultConfiguration.dbf.newDocumentBuilder();
            Document document = builder.parse(confStream);
            LOG.info(DefaultConfiguration.toString(document));
            return new DefaultConfiguration(document.getDocumentElement());
        } catch (DOMException | SAXException | IOException | ParserConfigurationException e) {
            throw new ConfigurationException("xml parse error", e);
        } finally {
            try {
                confStream.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public DefaultConfiguration buildFromFile(File file) throws ConfigurationException {
        try {
            DocumentBuilder builder = DefaultConfiguration.dbf.newDocumentBuilder();
            Document document = builder.parse(file);
            LOG.info(DefaultConfiguration.toString(document));
            return new DefaultConfiguration(document.getDocumentElement());
        } catch (DOMException | SAXException | IOException | ParserConfigurationException e) {
            throw new ConfigurationException("xml parse error", e);
        }
    }
}
