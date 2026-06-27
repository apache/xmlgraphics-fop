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

using Xunit;

namespace Fop.Cli.Tests;

/// <summary>Tests for <see cref="CommandLineOptions"/> argument parsing and validation.</summary>
public sealed class CommandLineOptionsTests
{
    [Fact]
    public void PositionalInputAndOutput()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(["in.fo", "out.pdf"], out string? error);
        Assert.Null(error);
        Assert.NotNull(o);
        Assert.Equal("in.fo", o!.FoFile);
        Assert.Equal("out.pdf", o.OutputFile);
    }

    [Fact]
    public void FlaggedFoAndPdf()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(["-fo", "a.fo", "-pdf", "b.pdf"], out string? error);
        Assert.Null(error);
        Assert.Equal("a.fo", o!.FoFile);
        Assert.Equal("b.pdf", o.OutputFile);
    }

    [Fact]
    public void XmlXsltPair()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(
            ["-xml", "d.xml", "-xsl", "s.xsl", "-pdf", "o.pdf"], out string? error);
        Assert.Null(error);
        Assert.Equal("d.xml", o!.XmlFile);
        Assert.Equal("s.xsl", o.XsltFile);
        Assert.Equal("o.pdf", o.OutputFile);
        Assert.Null(o.FoFile);
    }

    [Fact]
    public void NoArgsShowsHelp()
    {
        CommandLineOptions? o = CommandLineOptions.Parse([], out string? error);
        Assert.Null(error);
        Assert.True(o!.ShowHelp);
    }

    [Theory]
    [InlineData("-version")]
    [InlineData("-v")]
    public void VersionFlag(string flag)
    {
        CommandLineOptions? o = CommandLineOptions.Parse([flag], out string? error);
        Assert.Null(error);
        Assert.True(o!.ShowVersion);
    }

    [Fact]
    public void MissingOutputIsError()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(["-fo", "a.fo"], out string? error);
        Assert.Null(o);
        Assert.NotNull(error);
    }

    [Fact]
    public void XmlWithoutXsltIsError()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(["-xml", "d.xml", "-pdf", "o.pdf"], out string? error);
        Assert.Null(o);
        Assert.Contains("xsl", error, System.StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void FoAndXmlTogetherIsError()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(
            ["-fo", "a.fo", "-xml", "d.xml", "-xsl", "s.xsl", "-pdf", "o.pdf"], out string? error);
        Assert.Null(o);
        Assert.NotNull(error);
    }

    [Fact]
    public void UnknownOptionIsError()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(["-nope"], out string? error);
        Assert.Null(o);
        Assert.Contains("Unknown option", error);
    }

    [Fact]
    public void OptionRequiringValueAtEndIsError()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(["-fo"], out string? error);
        Assert.Null(o);
        Assert.Contains("requires a value", error);
    }

    [Fact]
    public void FontDirectoriesAreCollected()
    {
        CommandLineOptions? o = CommandLineOptions.Parse(
            ["in.fo", "out.pdf", "-fontdir", "/a", "-fontdir", "/b"], out string? error);
        Assert.Null(error);
        Assert.Equal(["/a", "/b"], o!.FontDirectories);
    }
}
