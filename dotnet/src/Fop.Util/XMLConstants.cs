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

namespace Fop.Util;

/// <summary>
/// A collection of constants for XML handling.
/// <para>
/// Port of the Java interface <c>org.apache.fop.util.XMLConstants</c>. Java declares these as
/// constants on an interface (an "interface as constant holder"); in idiomatic C# the same values
/// are exposed as <c>const</c> / <c>static readonly</c> members of a static class.
/// </para>
/// </summary>
public static class XMLConstants
{
    /// <summary>"CDATA" constant.</summary>
    public const string Cdata = "CDATA";

    /// <summary>XML namespace prefix.</summary>
    public const string XmlPrefix = "xml";

    /// <summary>XML namespace URI.</summary>
    public const string XmlNamespace = "http://www.w3.org/XML/1998/namespace";

    /// <summary>xml:space attribute.</summary>
    public static readonly QName XmlSpace = new(XmlNamespace, XmlPrefix, "space");

    /// <summary>XMLNS namespace prefix.</summary>
    public const string XmlnsPrefix = "xmlns";

    /// <summary>XMLNS namespace URI.</summary>
    public const string XmlnsNamespaceUri = "http://www.w3.org/2000/xmlns/";

    /// <summary>Namespace prefix for XLink.</summary>
    public const string XlinkPrefix = "xlink";

    /// <summary>XML namespace for XLink.</summary>
    public const string XlinkNamespace = "http://www.w3.org/1999/xlink";

    /// <summary>xlink:href attribute.</summary>
    public static readonly QName XlinkHref = new(XlinkNamespace, XlinkPrefix, "href");
}
