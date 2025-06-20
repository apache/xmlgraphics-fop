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

package org.apache.fop.hyphenation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A SAX document handler to read and parse hyphenation patterns
 * from a XML file.</p>
 *
 * <p>This work was authored by Carlos Villegas (cav@uniscope.co.jp).</p>
 */
public class PatternParser extends DefaultHandler implements PatternConsumer {

    private XMLReader parser;
    private int currElement;
    private PatternConsumer consumer;
    private StringBuffer token;
    private ArrayList exception;
    private char hyphenChar;
    private String errMsg;
    private boolean hasClasses;

    static final int ELEM_CLASSES = 1;
    static final int ELEM_EXCEPTIONS = 2;
    static final int ELEM_PATTERNS = 3;
    static final int ELEM_HYPHEN = 4;

    /**
     * Construct a pattern parser.
     * @throws HyphenationException if a hyphenation exception is raised
     */
    public PatternParser() throws HyphenationException {
        this.consumer = this;
        token = new StringBuffer();
        parser = createParser();
        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        hyphenChar = '-';    // default
    }

    /**
     * Construct a pattern parser.
     * @param consumer a pattern consumer
     * @throws HyphenationException if a hyphenation exception is raised
     */
    public PatternParser(PatternConsumer consumer) throws HyphenationException {
        this();
        this.consumer = consumer;
    }

    /**
     * Parses a hyphenation pattern file.
     * @param filename the filename
     * @throws HyphenationException In case of an exception while parsing
     */
    public void parse(String filename) throws HyphenationException {
        parse(new File(filename));
    }

    /**
     * Parses a hyphenation pattern file.
     * @param file the pattern file
     * @throws HyphenationException In case of an exception while parsing
     */
    public void parse(File file) throws HyphenationException {
        try {
            InputSource src = new InputSource(file.toURI().toURL().toExternalForm());
            parse(src);
        } catch (MalformedURLException e) {
            throw new HyphenationException("Error converting the File '" + file + "' to a URL: "
                    + e.getMessage());
        }
    }

    /**
     * Parses a hyphenation pattern file.
     * @param source the InputSource for the file
     * @throws HyphenationException In case of an exception while parsing
     */
    public void parse(InputSource source) throws HyphenationException {
        try {
            parser.parse(source);
        } catch (FileNotFoundException fnfe) {
            throw new HyphenationException("File not found: " + fnfe.getMessage());
        } catch (IOException ioe) {
            throw new HyphenationException(ioe.getMessage());
        } catch (SAXException e) {
            throw new HyphenationException(errMsg);
        }
    }

    /**
     * Creates a SAX parser using JAXP
     * @return the created SAX parser
     */
    static XMLReader createParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create XMLReader: " + e.getMessage());
        }
    }

    private String readToken(StringBuffer chars) {
        String word;
        boolean space = false;
        int i;
        for (i = 0; i < chars.length(); i++) {
            if (Character.isWhitespace(chars.charAt(i))) {
                space = true;
            } else {
                break;
            }
        }
        if (space) {
            // chars.delete(0,i);
            for (int countr = i; countr < chars.length(); countr++) {
                chars.setCharAt(countr - i, chars.charAt(countr));
            }
            chars.setLength(chars.length() - i);
            if (token.length() > 0) {
                word = token.toString();
                token.setLength(0);
                return word;
            }
        }
        space = false;
        for (i = 0; i < chars.length(); i++) {
            if (Character.isWhitespace(chars.charAt(i))) {
                space = true;
                break;
            }
        }
        token.append(chars.toString().substring(0, i));
        // chars.delete(0,i);
        for (int countr = i; countr < chars.length(); countr++) {
            chars.setCharAt(countr - i, chars.charAt(countr));
        }
        chars.setLength(chars.length() - i);
        if (space) {
            word = token.toString();
            token.setLength(0);
            return word;
        }
        token.append(chars);
        return null;
    }

    private static String getPattern(String word) {
        StringBuffer pat = new StringBuffer();
        int len = word.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(word.charAt(i))) {
                pat.append(word.charAt(i));
            }
        }
        return pat.toString();
    }

    private ArrayList normalizeException(ArrayList ex) {
        ArrayList res = new ArrayList();
        for (Object item : ex) {
            if (item instanceof String) {
                String str = (String) item;
                StringBuffer buf = new StringBuffer();
                for (int j = 0; j < str.length(); j++) {
                    char c = str.charAt(j);
                    if (c != hyphenChar) {
                        buf.append(c);
                    } else {
                        res.add(buf.toString());
                        buf.setLength(0);
                        char[] h = new char[1];
                        h[0] = hyphenChar;
                        // we use here hyphenChar which is not necessarily
                        // the one to be printed
                        res.add(new Hyphen(new String(h), null, null));
                    }
                }
                if (buf.length() > 0) {
                    res.add(buf.toString());
                }
            } else {
                res.add(item);
            }
        }
        return res;
    }

    private String getExceptionWord(ArrayList ex) {
        StringBuffer res = new StringBuffer();
        for (Object item : ex) {
            if (item instanceof String) {
                res.append((String) item);
            } else {
                if (((Hyphen) item).noBreak != null) {
                    res.append(((Hyphen) item).noBreak);
                }
            }
        }
        return res.toString();
    }

    private static String getInterletterValues(String pat) {
        StringBuffer il = new StringBuffer();
        String word = pat + "a";    // add dummy letter to serve as sentinel
        int len = word.length();
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if (Character.isDigit(c)) {
                il.append(c);
                i++;
            } else {
                il.append('0');
            }
        }
        return il.toString();
    }

    /** @throws SAXException if not caught */
    protected void getExternalClasses() throws SAXException {
        XMLReader mainParser = parser;
        parser = createParser();
        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        InputStream stream = PatternParser.class.getResourceAsStream("classes.xml");
        InputSource source = new InputSource(stream);
        try {
            parser.parse(source);
        } catch (IOException ioe) {
            throw new SAXException(ioe.getMessage());
        } finally {
            parser = mainParser;
        }
    }

    //
    // ContentHandler methods
    //

    /**
     * {@inheritDoc}
     * @throws SAXException
     */
    public void startElement(String uri, String local, String raw,
                             Attributes attrs) throws SAXException {
        if (local.equals("hyphen-char")) {
            String h = attrs.getValue("value");
            if (h != null && h.length() == 1) {
                hyphenChar = h.charAt(0);
            }
        } else if (local.equals("classes")) {
            currElement = ELEM_CLASSES;
        } else if (local.equals("patterns")) {
            if (!hasClasses) {
                getExternalClasses();
            }
            currElement = ELEM_PATTERNS;
        } else if (local.equals("exceptions")) {
            if (!hasClasses) {
                getExternalClasses();
            }
            currElement = ELEM_EXCEPTIONS;
            exception = new ArrayList();
        } else if (local.equals("hyphen")) {
            if (token.length() > 0) {
                exception.add(token.toString());
            }
            exception.add(new Hyphen(attrs.getValue("pre"),
                                            attrs.getValue("no"),
                                            attrs.getValue("post")));
            currElement = ELEM_HYPHEN;
        }
        token.setLength(0);
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String local, String raw) {

        if (token.length() > 0) {
            String word = token.toString();
            switch (currElement) {
            case ELEM_CLASSES:
                consumer.addClass(word);
                break;
            case ELEM_EXCEPTIONS:
                exception.add(word);
                exception = normalizeException(exception);
                consumer.addException(getExceptionWord(exception),
                                      (ArrayList)exception.clone());
                break;
            case ELEM_PATTERNS:
                consumer.addPattern(getPattern(word),
                                    getInterletterValues(word));
                break;
            case ELEM_HYPHEN:
                // nothing to do
                break;
            default:
                break;
            }
            if (currElement != ELEM_HYPHEN) {
                token.setLength(0);
            }
        }
        if (currElement == ELEM_CLASSES) {
            hasClasses = true;
        }
        if (currElement == ELEM_HYPHEN) {
            currElement = ELEM_EXCEPTIONS;
        } else {
            currElement = 0;
        }

    }

    /**
     * {@inheritDoc}
     */
    public void characters(char[] ch, int start, int length) {
        StringBuffer chars = new StringBuffer(length);
        chars.append(ch, start, length);
        String word = readToken(chars);
        while (word != null) {
            // System.out.println("\"" + word + "\"");
            switch (currElement) {
            case ELEM_CLASSES:
                consumer.addClass(word);
                break;
            case ELEM_EXCEPTIONS:
                exception.add(word);
                exception = normalizeException(exception);
                consumer.addException(getExceptionWord(exception),
                                      (ArrayList)exception.clone());
                exception.clear();
                break;
            case ELEM_PATTERNS:
                consumer.addPattern(getPattern(word),
                                    getInterletterValues(word));
                break;
            default:
                break;
            }
            word = readToken(chars);
        }

    }

    //
    // ErrorHandler methods
    //

    /**
     * {@inheritDoc}
     */
    public void warning(SAXParseException ex) {
        errMsg = "[Warning] " + getLocationString(ex) + ": "
                 + ex.getMessage();
    }

    /**
     * {@inheritDoc}
     */
    public void error(SAXParseException ex) {
        errMsg = "[Error] " + getLocationString(ex) + ": " + ex.getMessage();
    }

    /**
     * {@inheritDoc}
     */
    public void fatalError(SAXParseException ex) throws SAXException {
        errMsg = "[Fatal Error] " + getLocationString(ex) + ": "
                 + ex.getMessage();
        throw ex;
    }

    /**
     * Returns a string of the location.
     */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    }    // getLocationString(SAXParseException):String


    /**
     * For testing purposes only.
     * {@inheritDoc}
     */
    public void addClass(String c) {
        testOut.println("class: " + c);
    }

    /**
     * For testing purposes only.
     * {@inheritDoc}
     */
    public void addException(String w, ArrayList e) {
        testOut.println("exception: " + w + " : " + e.toString());
    }

    /**
     * For testing purposes only.
     * {@inheritDoc}
     */
    public void addPattern(String p, String v) {
        testOut.println("pattern: " + p + " : " + v);
    }

    private PrintStream testOut = System.out;

    /**
     * Set test out stream.
     * @param testOut the testOut to set
     */
    public void setTestOut(PrintStream testOut) {
        this.testOut = testOut;
    }

    /**
     * Close test out file.
     */
    public void closeTestOut() {
        testOut.flush();
        testOut.close();
    }

    /**
     * Main entry point when used as an application.
     * @param args array of command line arguments
     * @throws Exception in case of uncaught exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            PatternParser pp = new PatternParser();
            PrintStream p = null;
            if (args.length > 1) {
                FileOutputStream f = new FileOutputStream(args[1]);
                p = new PrintStream(f, false, StandardCharsets.UTF_8.name());
                pp.setTestOut(p);
            }
            pp.parse(args[0]);
            if (pp != null) {
                pp.closeTestOut();
            }
        }
    }


}
