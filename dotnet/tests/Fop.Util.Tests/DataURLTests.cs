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

using System;
using System.IO;
using System.Text;
using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class DataURLTests
{
    [Fact]
    public void RoundTripThroughResolver()
    {
        byte[] payload = [0x00, 0x01, 0x02, 0xFF, 0x10, 0x42, 0x7F, 0x80];

        string url = DataURLUtil.CreateDataUrl(new MemoryStream(payload), "application/octet-stream");

        byte[] decoded = new DataURIResolver().Resolve(url);

        Assert.Equal(payload, decoded);
    }

    [Fact]
    public void CreateDataUrlHasExpectedShape()
    {
        byte[] payload = Encoding.ASCII.GetBytes("Hello");
        string expectedBase64 = Convert.ToBase64String(payload);

        string url = DataURLUtil.CreateDataUrl(new MemoryStream(payload), "text/plain");

        Assert.Equal("data:text/plain;base64," + expectedBase64, url);
    }

    [Fact]
    public void NullOrEmptyMediaTypeIsOmitted()
    {
        byte[] payload = Encoding.ASCII.GetBytes("x");

        string urlNull = DataURLUtil.CreateDataUrl(new MemoryStream(payload), null);
        string urlEmpty = DataURLUtil.CreateDataUrl(new MemoryStream(payload), "");

        Assert.StartsWith("data:;base64,", urlNull);
        Assert.StartsWith("data:;base64,", urlEmpty);
    }

    [Fact]
    public void WriteDataUrlWritesToProvidedWriter()
    {
        byte[] payload = Encoding.ASCII.GetBytes("AB");
        StringWriter writer = new();

        DataURLUtil.WriteDataUrl(new MemoryStream(payload), "text/plain", writer);

        Assert.Equal("data:text/plain;base64," + Convert.ToBase64String(payload), writer.ToString());
    }

    [Fact]
    public void ResolverDecodesNonBase64TextData()
    {
        byte[] decoded = new DataURIResolver().Resolve("data:text/plain,Hello%2C%20World");

        Assert.Equal("Hello, World", Encoding.ASCII.GetString(decoded));
    }

    [Fact]
    public void ResolveStreamReturnsPayload()
    {
        byte[] payload = Encoding.ASCII.GetBytes("stream-me");
        string url = DataURLUtil.CreateDataUrl(new MemoryStream(payload), "text/plain");

        using Stream stream = new DataURIResolver().ResolveStream(url);
        using MemoryStream buffer = new();
        stream.CopyTo(buffer);

        Assert.Equal(payload, buffer.ToArray());
    }

    [Theory]
    [InlineData("http://example.com/foo.png")]
    [InlineData("data:no-comma-here")]
    public void ResolverRejectsInvalidUris(string href)
    {
        Assert.Throws<ArgumentException>(() => new DataURIResolver().Resolve(href));
    }
}
