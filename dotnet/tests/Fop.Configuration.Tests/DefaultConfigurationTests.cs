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
using Fop.Configuration;
using Xunit;

namespace Fop.Configuration.Tests;

/// <summary>
/// Port of <c>org.apache.fop.configuration.DefaultConfigurationTestCase</c>. The XML formerly
/// loaded from the <c>sample_config.xml</c> resource is reproduced verbatim below and fed to the
/// builder through a stream, so the scenarios and assertions match the Java original.
/// </summary>
public class DefaultConfigurationTests
{
    // Contents of the original test resource sample_config.xml, verbatim.
    private const string SampleConfigXml =
        "<fop version=\"1.0\">\n" +
        "\n" +
        "  <renderers>\n" +
        "    <renderer mime=\"application/pdf\">\n" +
        "      <fonts> \n" +
        "        <auto-detect/>\n" +
        "      </fonts>\n" +
        "    </renderer>\n" +
        "  </renderers>\n" +
        "\n" +
        "  <!-- A substitution can map a font family to another. -->\n" +
        "  <fonts>\n" +
        "    <substitutions>  \n" +
        "        <substitution>\n" +
        "            <from font-family='courierNew' font-style='normal' font-weight='400'/>" +
        "   <to font-family='Courier New'/> \n" +
        "         </substitution> \n" +
        "     </substitutions>\n" +
        "  </fonts>\n" +
        "\n" +
        "</fop>";

    private readonly DefaultConfiguration configuration;

    public DefaultConfigurationTests()
    {
        DefaultConfigurationBuilder builder = new();
        configuration = builder.Build(new MemoryStream(Encoding.UTF8.GetBytes(SampleConfigXml)));
    }

    [Fact]
    public void TestGetChild()
    {
        IConfiguration fontsConfig = configuration.GetChild("fonts");
        Assert.Equal("fop/fonts", fontsConfig.GetLocation());
    }

    [Fact]
    public void TestGetChildren()
    {
        IConfiguration[] fontsConfig = configuration.GetChildren("fonts");
        Assert.Single(fontsConfig);
        Assert.Equal("fop/fonts", fontsConfig[0].GetLocation());
    }
}
