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

using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class XMLConstantsTests
{
    [Fact]
    public void StringConstantsHaveExpectedValues()
    {
        Assert.Equal("CDATA", XMLConstants.Cdata);
        Assert.Equal("xml", XMLConstants.XmlPrefix);
        Assert.Equal("http://www.w3.org/XML/1998/namespace", XMLConstants.XmlNamespace);
        Assert.Equal("xmlns", XMLConstants.XmlnsPrefix);
        Assert.Equal("http://www.w3.org/2000/xmlns/", XMLConstants.XmlnsNamespaceUri);
        Assert.Equal("xlink", XMLConstants.XlinkPrefix);
        Assert.Equal("http://www.w3.org/1999/xlink", XMLConstants.XlinkNamespace);
    }

    [Fact]
    public void XmlSpaceQNameIsWellFormed()
    {
        Assert.Equal(XMLConstants.XmlNamespace, XMLConstants.XmlSpace.NamespaceUri);
        Assert.Equal(XMLConstants.XmlPrefix, XMLConstants.XmlSpace.Prefix);
        Assert.Equal("space", XMLConstants.XmlSpace.LocalName);
        Assert.Equal("xml:space", XMLConstants.XmlSpace.QualifiedName);
    }

    [Fact]
    public void XlinkHrefQNameIsWellFormed()
    {
        Assert.Equal(XMLConstants.XlinkNamespace, XMLConstants.XlinkHref.NamespaceUri);
        Assert.Equal(XMLConstants.XlinkPrefix, XMLConstants.XlinkHref.Prefix);
        Assert.Equal("href", XMLConstants.XlinkHref.LocalName);
        Assert.Equal("xlink:href", XMLConstants.XlinkHref.QualifiedName);
    }
}
