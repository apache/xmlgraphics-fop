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

using System.Globalization;
using System.Text;
using System.Xml;

namespace Fop.Configuration;

/// <summary>
/// The default, XML-backed implementation of <see cref="IConfiguration"/>. Each instance
/// wraps a single <see cref="XmlElement"/> in a DOM tree; children, attributes and the
/// element text are exposed through the typed accessors.
/// <para>Port of <c>org.apache.fop.configuration.DefaultConfiguration</c>.</para>
/// <para>
/// The Java original used <c>org.w3c.dom</c> via <c>javax.xml.parsers</c>. This port uses
/// <see cref="XmlDocument"/> / <see cref="XmlElement"/> from <c>System.Xml</c> because the
/// behaviour relies on DOM semantics that map one-to-one: parent navigation (for
/// <see cref="GetLocation"/>), element text content, and node importing (for
/// <see cref="AddChild"/>).
/// </para>
/// </summary>
public class DefaultConfiguration : IConfiguration
{
    /// <summary>
    /// Creates a DOM document configured the way the Java <c>DocumentBuilderFactory</c> was:
    /// non-namespace-aware, non-validating, comments ignored, entity references expanded.
    /// </summary>
    internal static XmlDocument NewDocument()
    {
        // NamespaceAware(false): node names are read verbatim, matching getNodeName().
        // Validating(false) and IgnoringComments(true) are honoured at load time
        // (see DefaultConfigurationBuilder); a freshly created document carries no DTD.
        return new XmlDocument
        {
            // Whitespace is preserved so that getTextContent() matches Java semantics.
            PreserveWhitespace = true,
        };
    }

    private readonly XmlElement element;

    /// <summary>Creates a new configuration whose root element has the given name.</summary>
    public DefaultConfiguration(string key)
    {
        ArgumentNullException.ThrowIfNull(key);
        XmlDocument doc = NewDocument();
        // create the root element node
        element = doc.CreateElement(key);
        doc.AppendChild(element);
    }

    internal DefaultConfiguration(XmlElement element) => this.element = element;

    internal XmlElement Element => element;

    /// <summary>Imports the given configuration's element and appends it as a child of this element.</summary>
    public void AddChild(DefaultConfiguration configuration)
    {
        ArgumentNullException.ThrowIfNull(configuration);
        XmlNode node = element.OwnerDocument.ImportNode(configuration.Element, deep: true);
        element.AppendChild(node);
    }

    private string GetValue0()
    {
        // XmlElement.InnerText is the concatenated text of descendants, mirroring
        // org.w3c.dom Node.getTextContent(); it is never null (empty string instead).
        return element.InnerText ?? "";
    }

    public IConfiguration GetChild(string key)
    {
        foreach (XmlNode n in element.ChildNodes)
        {
            if (n.Name == key)
            {
                return new DefaultConfiguration((XmlElement)n);
            }
        }
        return NullConfiguration.Instance;
    }

    public IConfiguration? GetChild(string key, bool required)
    {
        IConfiguration result = GetChild(key);
        if (!required && ReferenceEquals(result, NullConfiguration.Instance))
        {
            return null;
        }
        if (required && (result == null || ReferenceEquals(result, NullConfiguration.Instance)))
        {
            // throw new IllegalStateException("No child '" + key + "'");
            return NullConfiguration.Instance;
        }
        return result;
    }

    public IConfiguration[] GetChildren(string key)
    {
        List<IConfiguration> result = new(1);
        foreach (XmlNode n in element.ChildNodes)
        {
            if (n.Name == key)
            {
                result.Add(new DefaultConfiguration((XmlElement)n));
            }
        }
        return result.ToArray();
    }

    public string[] GetAttributeNames()
    {
        XmlAttributeCollection nnm = element.Attributes;
        string[] result = new string[nnm.Count];
        for (int i = 0; i < nnm.Count; ++i)
        {
            result[i] = nnm[i].Name;
        }
        return result;
    }

    public string? GetAttribute(string key)
    {
        // XmlElement.GetAttribute returns "" for a missing attribute, like the DOM original.
        string? result = element.GetAttribute(key);
        if (result == "")
        {
            result = null;
        }
        return result;
    }

    public string GetAttribute(string key, string defaultValue)
    {
        string? result = GetAttribute(key);
        if (result == null || result == "")
        {
            result = defaultValue;
        }
        return result;
    }

    public bool GetAttributeAsBoolean(string key, bool defaultValue)
    {
        string? result = GetAttribute(key);
        if (result == null || result == "")
        {
            return defaultValue;
        }
        return string.Equals(result, "true", StringComparison.OrdinalIgnoreCase)
            || string.Equals(result, "yes", StringComparison.OrdinalIgnoreCase);
    }

    public float GetAttributeAsFloat(string key)
    {
        // Mirrors Java Float.parseFloat(getAttribute(key)): a missing attribute (null)
        // throws, as does a non-numeric value.
        return ParseFloat(GetAttribute(key));
    }

    public float GetAttributeAsFloat(string key, float defaultValue)
    {
        string? result = GetAttribute(key);
        if (result == null || result == "")
        {
            return defaultValue;
        }
        return ParseFloat(result);
    }

    public int GetAttributeAsInteger(string key, int defaultValue)
    {
        string? result = GetAttribute(key);
        if (result == null || result == "")
        {
            return defaultValue;
        }
        return ParseInt(result);
    }

    public string GetValue()
    {
        string result = GetValue0();
        if (result == "")
        {
            throw new ConfigurationException("No value in " + element.Name);
        }
        return result;
    }

    public string GetValue(string defaultValue)
    {
        string result = GetValue0();
        if (result == "")
        {
            result = defaultValue;
        }
        return result;
    }

    public bool GetValueAsBoolean() => ParseBoolean(GetValue0());

    public bool GetValueAsBoolean(bool defaultValue)
    {
        string result = GetValue0().Trim();
        if (result == "")
        {
            return defaultValue;
        }
        return ParseBoolean(result);
    }

    public int GetValueAsInteger()
    {
        try
        {
            return ParseInt(GetValue0());
        }
        catch (FormatException e)
        {
            throw new ConfigurationException("Not an integer", e);
        }
    }

    public int GetValueAsInteger(int defaultValue)
    {
        string result = GetValue0();
        if (result == "")
        {
            return defaultValue;
        }
        return ParseInt(result);
    }

    public float GetValueAsFloat()
    {
        try
        {
            return ParseFloat(GetValue0());
        }
        catch (FormatException e)
        {
            throw new ConfigurationException("Not a float", e);
        }
    }

    public float GetValueAsFloat(float defaultValue)
    {
        string result = GetValue0();
        if (result == "")
        {
            return defaultValue;
        }
        return ParseFloat(GetValue0());
    }

    public string GetLocation()
    {
        List<string> path = [];
        for (XmlNode? el = element; el != null; el = el.ParentNode)
        {
            if (el is XmlElement e)
            {
                path.Add(e.Name);
            }
        }
        path.Reverse();

        StringBuilder sb = new();
        foreach (string s in path)
        {
            if (sb.Length > 0)
            {
                sb.Append('/');
            }
            sb.Append(s);
        }
        return sb.ToString();
    }

    // Java Float.parseFloat / Integer.parseInt are culture-independent and throw
    // NumberFormatException on bad input; the invariant-culture parse below matches that,
    // surfacing a FormatException (the .NET analogue) which callers translate as needed.
    private static float ParseFloat(string? s) =>
        float.Parse(s ?? throw new FormatException("null"), CultureInfo.InvariantCulture);

    private static int ParseInt(string s) =>
        int.Parse(s, CultureInfo.InvariantCulture);

    // Java Boolean.parseBoolean: true only for a case-insensitive "true", false otherwise.
    private static bool ParseBoolean(string s) =>
        string.Equals(s, "true", StringComparison.OrdinalIgnoreCase);
}
