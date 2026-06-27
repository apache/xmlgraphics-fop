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

using Fop.DataTypes;
using Xunit;

namespace Fop.DataTypes.Tests;

/// <summary>
/// Tests for <see cref="URISpecification"/>. Port of
/// <c>org.apache.fop.datatypes.URISpecificationTestCase</c>.
/// </summary>
public class URISpecificationTests
{
    [Fact]
    public void TestGetURL()
    {
        Assert.Equal("http://localhost/test", URISpecification.GetURL("http://localhost/test"));
        Assert.Equal("http://localhost/test", URISpecification.GetURL("url(http://localhost/test)"));
        Assert.Equal("http://localhost/test", URISpecification.GetURL("url('http://localhost/test')"));
        Assert.Equal("http://localhost/test", URISpecification.GetURL("url(\"http://localhost/test\")"));
    }

    [Fact]
    public void TestEscapeURI()
    {
        Assert.Equal("http://localhost/test", URISpecification.EscapeURI("http://localhost/test"));
        Assert.Equal("http://localhost/test%20test", URISpecification.EscapeURI("http://localhost/test%20test"));
        Assert.Equal("http://localhost/test%20test", URISpecification.EscapeURI("http://localhost/test test"));
        Assert.Equal(
            "http://localhost/test%20test.pdf#page=6",
            URISpecification.EscapeURI("http://localhost/test test.pdf#page=6"));
        Assert.Equal(
            "http://localhost/test%5Etest.pdf",
            URISpecification.EscapeURI("http://localhost/test^test.pdf"));
    }
}
