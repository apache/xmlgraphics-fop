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

public class QNameTests
{
    [Fact]
    public void Constructor_WithPrefix_BuildsQualifiedName()
    {
        var q = new QName("urn:ns", "p", "local");
        Assert.Equal("urn:ns", q.NamespaceUri);
        Assert.Equal("p", q.Prefix);
        Assert.Equal("local", q.LocalName);
        Assert.Equal("p:local", q.QualifiedName);
    }

    [Fact]
    public void Constructor_NullPrefix_QualifiedNameIsLocalName()
    {
        var q = new QName("urn:ns", null, "local");
        Assert.Null(q.Prefix);
        Assert.Equal("local", q.QualifiedName);
    }

    [Fact]
    public void Constructor_FromQualifiedName_SplitsPrefix()
    {
        var q = new QName("urn:ns", "p:local");
        Assert.Equal("p", q.Prefix);
        Assert.Equal("local", q.LocalName);
        Assert.Equal("p:local", q.QualifiedName);
    }

    [Fact]
    public void Constructor_FromQualifiedName_NoPrefix()
    {
        var q = new QName("urn:ns", "local");
        Assert.Null(q.Prefix);
        Assert.Equal("local", q.LocalName);
    }

    [Fact]
    public void Equals_IgnoresPrefix()
    {
        var a = new QName("urn:ns", "p1", "local");
        var b = new QName("urn:ns", "p2", "local");
        Assert.Equal(a, b);
        Assert.Equal(a.GetHashCode(), b.GetHashCode());
    }

    [Fact]
    public void Equals_DistinguishesNamespaceAndLocalName()
    {
        var a = new QName("urn:ns", "p", "local");
        Assert.NotEqual(a, new QName("urn:other", "p", "local"));
        Assert.NotEqual(a, new QName("urn:ns", "p", "other"));
    }

    [Fact]
    public void ToString_IncludesNamespaceWhenPresent()
        => Assert.Equal("{urn:ns}p:local", new QName("urn:ns", "p", "local").ToString());

    [Fact]
    public void ToString_OmitsNamespaceWhenNull()
        => Assert.Equal("p:local", new QName(null, "p", "local").ToString());
}
