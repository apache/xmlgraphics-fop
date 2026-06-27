// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Text;
using System.Xml;

namespace Fop.Hyphenation;

/// <summary>
/// Reads and parses hyphenation patterns from an XML file (per the FOP <c>hyphenation.dtd</c>:
/// <c>hyphenation-info</c> &gt; <c>classes</c>, <c>exceptions?</c>, <c>patterns</c>), pushing the
/// parsed classes/exceptions/patterns into an <see cref="IPatternConsumer"/>.
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.PatternParser</c>. The Java original is a SAX
/// <c>DefaultHandler</c>; this port drives an <see cref="XmlReader"/> but preserves the SAX
/// tokenization (whitespace-separated tokens spanning text events) so the behaviour matches: a
/// <c>classes</c>/<c>patterns</c>/<c>exceptions</c> body is split on whitespace, a digit-bearing
/// pattern yields its interletter values, and a <c>hyphen</c> element inside <c>exceptions</c> marks
/// an explicit break point.
/// </para>
/// </summary>
public sealed class PatternParser : IPatternConsumer
{
    private const int ElemNone = 0;
    private const int ElemClasses = 1;
    private const int ElemExceptions = 2;
    private const int ElemPatterns = 3;
    private const int ElemHyphen = 4;

    private readonly IPatternConsumer consumer;
    private int currElement;
    private readonly StringBuilder token = new();
    private List<object> exception = new();
    private char hyphenChar = '-';    // default

    /// <summary>Construct a pattern parser feeding itself (test/echo mode).</summary>
    public PatternParser() => consumer = this;

    /// <summary>Construct a pattern parser feeding <paramref name="consumer"/>.</summary>
    /// <param name="consumer">a pattern consumer.</param>
    public PatternParser(IPatternConsumer consumer)
    {
        ArgumentNullException.ThrowIfNull(consumer);
        this.consumer = consumer;
    }

    /// <summary>Parses a hyphenation pattern document from <paramref name="reader"/>.</summary>
    /// <param name="reader">an <see cref="XmlReader"/> positioned at the document start.</param>
    /// <exception cref="HyphenationException">if parsing fails.</exception>
    public void Parse(XmlReader reader)
    {
        ArgumentNullException.ThrowIfNull(reader);
        try
        {
            while (reader.Read())
            {
                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:
                        StartElement(reader);
                        break;
                    case XmlNodeType.Text:
                    case XmlNodeType.SignificantWhitespace:
                    case XmlNodeType.Whitespace:
                    case XmlNodeType.CDATA:
                        Characters(reader.Value);
                        break;
                    case XmlNodeType.EndElement:
                        EndElement(reader.LocalName);
                        break;
                }
            }
        }
        catch (XmlException e)
        {
            throw new HyphenationException(e.Message, e);
        }
    }

    /// <summary>
    /// Reads the next whitespace-delimited token out of <paramref name="chars"/>, accumulating partial
    /// tokens across calls in <see cref="token"/>. Returns the completed token, or <c>null</c> when the
    /// buffer was exhausted mid-token. Faithful port of the SAX <c>readToken</c>.
    /// </summary>
    private string? ReadToken(StringBuilder chars)
    {
        bool space = false;
        int i;
        for (i = 0; i < chars.Length; i++)
        {
            if (char.IsWhiteSpace(chars[i]))
            {
                space = true;
            }
            else
            {
                break;
            }
        }

        if (space)
        {
            for (int countr = i; countr < chars.Length; countr++)
            {
                chars[countr - i] = chars[countr];
            }

            chars.Length -= i;
            if (token.Length > 0)
            {
                string word = token.ToString();
                token.Length = 0;
                return word;
            }
        }

        space = false;
        for (i = 0; i < chars.Length; i++)
        {
            if (char.IsWhiteSpace(chars[i]))
            {
                space = true;
                break;
            }
        }

        token.Append(chars.ToString(0, i));
        for (int countr = i; countr < chars.Length; countr++)
        {
            chars[countr - i] = chars[countr];
        }

        chars.Length -= i;
        if (space)
        {
            string word = token.ToString();
            token.Length = 0;
            return word;
        }

        token.Append(chars);
        return null;
    }

    /// <summary>Strips the digit interletter values from a pattern, leaving only the letters.</summary>
    private static string GetPattern(string word)
    {
        var pat = new StringBuilder();
        foreach (char ch in word)
        {
            if (!char.IsDigit(ch))
            {
                pat.Append(ch);
            }
        }

        return pat.ToString();
    }

    /// <summary>
    /// Normalizes an exception list, splitting each string on the hyphen char into substrings separated
    /// by simple <see cref="Hyphen"/> markers.
    /// </summary>
    private List<object> NormalizeException(List<object> ex)
    {
        var res = new List<object>();
        foreach (object item in ex)
        {
            if (item is string str)
            {
                var buf = new StringBuilder();
                foreach (char c in str)
                {
                    if (c != hyphenChar)
                    {
                        buf.Append(c);
                    }
                    else
                    {
                        res.Add(buf.ToString());
                        buf.Length = 0;
                        // use hyphenChar here, which is not necessarily the one to be printed
                        res.Add(new Hyphen(hyphenChar.ToString(), null, null));
                    }
                }

                if (buf.Length > 0)
                {
                    res.Add(buf.ToString());
                }
            }
            else
            {
                res.Add(item);
            }
        }

        return res;
    }

    /// <summary>Reconstructs the (unhyphenated) word from a normalized exception list.</summary>
    private static string GetExceptionWord(List<object> ex)
    {
        var res = new StringBuilder();
        foreach (object item in ex)
        {
            if (item is string str)
            {
                res.Append(str);
            }
            else if (item is Hyphen { NoBreak: { } noBreak })
            {
                res.Append(noBreak);
            }
        }

        return res.ToString();
    }

    /// <summary>
    /// Extracts the interletter value string from a pattern: a digit before a letter is its value, the
    /// absence of a digit is value '0'. A dummy trailing letter acts as a sentinel.
    /// </summary>
    private static string GetInterletterValues(string pat)
    {
        var il = new StringBuilder();
        string word = pat + "a";    // add dummy letter to serve as sentinel
        int len = word.Length;
        for (int i = 0; i < len; i++)
        {
            char c = word[i];
            if (char.IsDigit(c))
            {
                il.Append(c);
                i++;
            }
            else
            {
                il.Append('0');
            }
        }

        return il.ToString();
    }

    private void StartElement(XmlReader reader)
    {
        string local = reader.LocalName;
        if (local == "hyphen-char")
        {
            string? h = reader.GetAttribute("value");
            if (h is { Length: 1 })
            {
                hyphenChar = h[0];
            }
        }
        else if (local == "classes")
        {
            currElement = ElemClasses;
        }
        else if (local == "patterns")
        {
            currElement = ElemPatterns;
        }
        else if (local == "exceptions")
        {
            currElement = ElemExceptions;
            exception = new List<object>();
        }
        else if (local == "hyphen")
        {
            if (token.Length > 0)
            {
                exception.Add(token.ToString());
            }

            exception.Add(new Hyphen(reader.GetAttribute("pre"), reader.GetAttribute("no"),
                reader.GetAttribute("post")));
            currElement = ElemHyphen;
        }

        bool empty = reader.IsEmptyElement;
        token.Length = 0;

        // An empty element (e.g. <hyphen .../>) raises no separate end event from XmlReader, so close
        // it here to mirror the SAX start/end pairing the original relies on.
        if (empty)
        {
            EndElement(local);
        }
    }

    private void EndElement(string local)
    {
        if (token.Length > 0)
        {
            string word = token.ToString();
            switch (currElement)
            {
                case ElemClasses:
                    consumer.AddClass(word);
                    break;
                case ElemExceptions:
                    exception.Add(word);
                    exception = NormalizeException(exception);
                    consumer.AddException(GetExceptionWord(exception), new List<object>(exception));
                    break;
                case ElemPatterns:
                    consumer.AddPattern(GetPattern(word), GetInterletterValues(word));
                    break;
                case ElemHyphen:
                    // nothing to do
                    break;
            }

            if (currElement != ElemHyphen)
            {
                token.Length = 0;
            }
        }

        if (currElement == ElemHyphen)
        {
            currElement = ElemExceptions;
        }
        else
        {
            currElement = ElemNone;
        }

        _ = local;
    }

    private void Characters(string value)
    {
        var chars = new StringBuilder(value);
        string? word = ReadToken(chars);
        while (word != null)
        {
            switch (currElement)
            {
                case ElemClasses:
                    consumer.AddClass(word);
                    break;
                case ElemExceptions:
                    exception.Add(word);
                    exception = NormalizeException(exception);
                    consumer.AddException(GetExceptionWord(exception), new List<object>(exception));
                    exception.Clear();
                    break;
                case ElemPatterns:
                    consumer.AddPattern(GetPattern(word), GetInterletterValues(word));
                    break;
            }

            word = ReadToken(chars);
        }
    }

    // ----- IPatternConsumer (echo/test mode) ------------------------------------------------

    /// <summary>Echoes a class (test/echo mode only).</summary>
    void IPatternConsumer.AddClass(string charGroup) => TestOut?.WriteLine("class: " + charGroup);

    /// <summary>Echoes an exception (test/echo mode only).</summary>
    void IPatternConsumer.AddException(string word, IList<object> hyphenatedWord) =>
        TestOut?.WriteLine("exception: " + word);

    /// <summary>Echoes a pattern (test/echo mode only).</summary>
    void IPatternConsumer.AddPattern(string pattern, string values) =>
        TestOut?.WriteLine("pattern: " + pattern + " : " + values);

    /// <summary>An optional sink for the echo/test consumer mode.</summary>
    public TextWriter? TestOut { get; set; }
}
