/** -- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.fop.layout.hyphenation;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

// Java
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.net.URL;

/**
 * A SAX document handler to read and parse hyphenation patterns
 * from a XML file.
 *
 * @author Carlos Villegas <cav@uniscope.co.jp>
 */
public class PatternParser extends DefaultHandler implements PatternConsumer {

      XMLReader parser;
      int currElement;
      PatternConsumer consumer;
      StringBuffer token;
      Vector exception;
      char hyphenChar;
      String errMsg;

      static final int ELEM_CLASSES = 1;
      static final int ELEM_EXCEPTIONS = 2;
      static final int ELEM_PATTERNS = 3;
      static final int ELEM_HYPHEN = 4;
      
      public PatternParser()
         throws HyphenationException
      {
         token = new StringBuffer();
         parser = createParser();               
         parser.setContentHandler(this);
         parser.setErrorHandler(this);
         hyphenChar = '-'; // default

      }

      public PatternParser(PatternConsumer consumer)
         throws HyphenationException
      {
         this();
         this.consumer = consumer;
      }

      public void setConsumer(PatternConsumer consumer)
      {
         this.consumer = consumer;
      }
      
      public void parse(String filename)
         throws HyphenationException
      {
         InputSource uri = fileInputSource(filename);

         try {
            parser.parse(uri);
         } catch ( SAXException e ) {
            throw new HyphenationException(errMsg);
         } catch ( IOException e ) {
            throw new HyphenationException(e.getMessage());
         } catch ( NullPointerException e) {
            throw new HyphenationException("SAX parser not available");
         }
      }
      
      /**
       * creates a SAX parser, using the value of org.xml.sax.parser
       * defaulting to org.apache.xerces.parsers.SAXParser
       *
       * @return the created SAX parser
       */
      static XMLReader createParser()
         throws HyphenationException
      {
         String parserClassName = System.getProperty("org.xml.sax.parser");
         if (parserClassName == null) {
	    parserClassName = "org.apache.xerces.parsers.SAXParser";
         }
         // System.out.println("using SAX parser " + parserClassName);

         try {
	    return (XMLReader)
               Class.forName(parserClassName).newInstance();
         } catch (ClassNotFoundException e) {
	    throw new HyphenationException("Could not find " + parserClassName);
         } catch (InstantiationException e) {
	    throw new HyphenationException("Could not instantiate " + parserClassName);
         } catch (IllegalAccessException e) {
	    throw new HyphenationException("Could not access " + parserClassName);
         } catch (ClassCastException e) {
	    throw new HyphenationException(parserClassName + " is not a SAX driver"); 
         }
      }

      /**
       * create an InputSource from a file name
       *
       * @param filename the name of the file
       * @return the InputSource created
       */
      protected static InputSource fileInputSource(String filename)
         throws HyphenationException
      {
	
         /* this code adapted from James Clark's in XT */
         File file = new File(filename);
         String path = file.getAbsolutePath();
         String fSep = System.getProperty("file.separator");
         if (fSep != null && fSep.length() == 1)
	    path = path.replace(fSep.charAt(0), '/');
         if (path.length() > 0 && path.charAt(0) != '/')
	    path = '/' + path;
         try {
	    return new InputSource(new URL("file", null, path).toString());
         }
         catch (java.net.MalformedURLException e) {
	    throw new HyphenationException("unexpected MalformedURLException");
         }
      }

      protected String readToken(StringBuffer chars)
      {
         String word;
         boolean space = false;
         int i;
         for(i=0; i<chars.length(); i++)
            if ( Character.isWhitespace(chars.charAt(i)) )
               space = true;
            else
               break;
         if ( space ) {
            //chars.delete(0,i);
			for ( int countr = i ; countr < chars.length() ; countr++ )
				chars.setCharAt(countr - i, chars.charAt(countr));
			chars.setLength(chars.length() - i);
            if ( token.length() > 0 ) {
               word = token.toString();
               token.setLength(0);
               return word;
            }
         }
         space = false;
         for(i=0; i<chars.length(); i++) {
            if ( Character.isWhitespace(chars.charAt(i)) ) {
               space = true;
               break;
            }
         }
         token.append(chars.toString().substring(0,i));
         //chars.delete(0,i);
		 for ( int countr = i ; countr < chars.length() ; countr++ )
			chars.setCharAt(countr - i, chars.charAt(countr));
		 chars.setLength(chars.length() - i);
         if ( space ) {
            word = token.toString();
            token.setLength(0);
            return word;
         }
         token.append(chars);
         return null;
      }

      protected static String getPattern(String word)
      {
         StringBuffer pat = new StringBuffer();
         int len = word.length();
         for(int i=0; i<len; i++)
            if ( ! Character.isDigit(word.charAt(i)) )
               pat.append(word.charAt(i));
         return pat.toString();
      }

      protected Vector normalizeException(Vector ex)
      {
         Vector res = new Vector();
         for(int i=0; i<ex.size(); i++) {
            Object item = ex.elementAt(i);
            if ( item instanceof String ) {
               String str = (String)item;
               StringBuffer buf = new StringBuffer();
               for(int j=0; j<str.length(); j++) {
                  char c = str.charAt(j);
                  if ( c != hyphenChar )
                     buf.append(c);
                  else {
                     res.addElement(buf.toString());
                     buf.setLength(0);
                     char[] h = new char[1];
                     h[0] = hyphenChar;
                     // we use here hyphenChar which is not necessarily
                     // the one to be printed
                     res.addElement(new Hyphen(new String(h),null,null));
                  }
               }
               if ( buf.length() > 0 )
                  res.addElement(buf.toString());
            } else
               res.addElement(item);
         }
         return res;
      }
      
      protected String getExceptionWord(Vector ex)
      {
         StringBuffer res = new StringBuffer();
         for(int i=0; i<ex.size(); i++) {
            Object item = ex.elementAt(i);
            if ( item instanceof String )
               res.append((String)item);
            else {
               if ( ((Hyphen)item).noBreak != null )
                  res.append(((Hyphen)item).noBreak);
            }
         }
         return res.toString();
      }
      
      protected static String getInterletterValues(String pat)
      {
         StringBuffer il = new StringBuffer();
         String word = pat + "a"; // add dummy letter to serve as sentinel
         int len = word.length();
         for(int i=0;i<len;i++) {
            char c = word.charAt(i);
            if ( Character.isDigit(c) ) {
               il.append(c);
               i++;
            } else il.append('0');
         }
         return il.toString();
      }
      
      //
      // DocumentHandler methods
      //

      /** Start element. */
      public void startElement(String uri, String local, String raw, Attributes attrs)
      {
         if ( local.equals("hyphen-char") ) {
            String h = attrs.getValue("value");
            if ( h != null && h.length() == 1 )
               hyphenChar = h.charAt(0);
         } else if ( local.equals("classes") )
            currElement = ELEM_CLASSES;
         else if ( local.equals("patterns") )
            currElement = ELEM_PATTERNS;
         else if ( local.equals("exceptions") ) {
            currElement = ELEM_EXCEPTIONS;
            exception = new Vector();
         }
         else if ( local.equals("hyphen") ) {
            if ( token.length() > 0 ) {
               exception.addElement(token.toString());
            }
            exception.addElement(new Hyphen(attrs.getValue("pre"),
                                            attrs.getValue("no"),
                                            attrs.getValue("post")));
            currElement = ELEM_HYPHEN;
         }
         token.setLength(0);
      }

      public void endElement(String uri, String local, String raw)
      {

         if ( token.length() > 0 ) {
            String word = token.toString();
            switch ( currElement ) {
               case ELEM_CLASSES:
                  consumer.addClass(word);
                  break;
               case ELEM_EXCEPTIONS:
                  exception.addElement(word);
                  exception = normalizeException(exception);
                  consumer.addException(getExceptionWord(exception),
                                        (Vector)exception.clone());
                  break;
               case ELEM_PATTERNS:
                  consumer.addPattern(getPattern(word), getInterletterValues(word));
                  break;
               case ELEM_HYPHEN:
                  // nothing to do 
                  break;
            }
            if ( currElement != ELEM_HYPHEN )
               token.setLength(0);
         }
         if ( currElement == ELEM_HYPHEN )
            currElement = ELEM_EXCEPTIONS;
         else
            currElement = 0;

      }

      /** Characters. */
      public void characters(char ch[], int start, int length)
      {
         StringBuffer chars = new StringBuffer(length);
         chars.append(ch, start, length);
         String word = readToken(chars);
         while ( word != null ) {
            // System.out.println("\"" + word + "\"");
            switch ( currElement ) {
               case ELEM_CLASSES:
                  consumer.addClass(word);
                  break;
               case ELEM_EXCEPTIONS:
                  exception.addElement(word);
                  exception = normalizeException(exception);
                  consumer.addException(getExceptionWord(exception),
                                        (Vector)exception.clone());
                  exception.removeAllElements();
                  break;
               case ELEM_PATTERNS:
                  consumer.addPattern(getPattern(word), getInterletterValues(word));
                  break;
            }
            word = readToken(chars);
         }
               
      }
      
      //
      // ErrorHandler methods
      //

      /** Warning. */
      public void warning(SAXParseException ex) {
         errMsg = "[Warning] "+ getLocationString(ex)+": "+ ex.getMessage();
      }

      /** Error. */
      public void error(SAXParseException ex) {
         errMsg = "[Error] "+ getLocationString(ex)+": "+ ex.getMessage();
      }

      /** Fatal error. */
      public void fatalError(SAXParseException ex) throws SAXException {
         errMsg = "[Fatal Error] "+getLocationString(ex)+": "+ ex.getMessage();
         throw ex;
      }

      /** Returns a string of the location. */
      private String getLocationString(SAXParseException ex) {
         StringBuffer str = new StringBuffer();

         String systemId = ex.getSystemId();
         if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) 
               systemId = systemId.substring(index + 1);
            str.append(systemId);
         }
         str.append(':');
         str.append(ex.getLineNumber());
         str.append(':');
         str.append(ex.getColumnNumber());

         return str.toString();

      } // getLocationString(SAXParseException):String


      // PatternConsumer implementation for testing purposes
      public void addClass(String c)
      {
         System.out.println("class: " + c);
      }

      public void addException(String w, Vector e)
      {
         System.out.println("exception: " + w + " : " + e.toString());
      }

      public void addPattern(String p, String v)
      {
         System.out.println("pattern: " + p + " : " + v);
      }
      
      public static void main(String[] args)
         throws Exception
      {
         if ( args.length > 0  ){
            PatternParser pp = new PatternParser();
            pp.setConsumer(pp);
            pp.parse(args[0]);
         }
      }
}
