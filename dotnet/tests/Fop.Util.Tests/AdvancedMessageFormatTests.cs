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

using Fop.Util.Text;
using Xunit;

namespace Fop.Util.Tests;

/// <summary>
/// Port of <c>org.apache.fop.util.AdvancedMessageFormatTestCase</c>, verifying the same observable
/// behaviour (same patterns, same parameters, same assertions) as the original JUnit test.
/// </summary>
public class AdvancedMessageFormatTests
{
    [Fact]
    public void TestFormatting()
    {
        string msg;
        AdvancedMessageFormat format;

        string pattern =
            "Element \"{elementName}\" is missing[ required property \"{propertyName}\"]!";
        format = new AdvancedMessageFormat(pattern);

        var parameters = new Dictionary<string, object?>
        {
            ["node"] = new object(),
            ["elementName"] = "fo:external-graphic",
            ["propertyName"] = "src",
        };

        msg = format.Format(parameters);
        Assert.Equal("Element \"fo:external-graphic\" is missing required property \"src\"!", msg);

        parameters.Remove("propertyName");
        msg = format.Format(parameters);
        Assert.Equal("Element \"fo:external-graphic\" is missing!", msg);

        pattern = "Testing \\{escaped \\[characters\\], now a normal field {elementName}!";
        format = new AdvancedMessageFormat(pattern);
        msg = format.Format(parameters);
        Assert.Equal("Testing {escaped [characters], now a normal field fo:external-graphic!", msg);

        pattern = "Multi-conditional: [case1: {var1}|case2: {var2}|case3: {var3}]";
        format = new AdvancedMessageFormat(pattern);

        parameters = new Dictionary<string, object?>();
        msg = format.Format(parameters);
        Assert.Equal("Multi-conditional: ", msg);

        parameters["var3"] = "value3";
        msg = format.Format(parameters);
        Assert.Equal("Multi-conditional: case3: value3", msg);
        parameters["var1"] = "value1";
        msg = format.Format(parameters);
        Assert.Equal("Multi-conditional: case1: value1", msg);
    }

    [Fact]
    public void TestObjectFormatting()
    {
        string msg;
        AdvancedMessageFormat format;

        string pattern = "Here's a Locator: {locator}";
        format = new AdvancedMessageFormat(pattern);

        var parameters = new Dictionary<string, object?>();
        var loc = new LocatorImpl { ColumnNumber = 7, LineNumber = 12 };
        parameters["locator"] = loc;

        msg = format.Format(parameters);
        Assert.Equal("Here's a Locator: 12:7", msg);
    }

    [Fact]
    public void TestIfFormatting()
    {
        string msg;
        AdvancedMessageFormat format;

        format = new AdvancedMessageFormat("You are{isBad,if, not} nice!");

        var parameters = new Dictionary<string, object?>();

        parameters["isBad"] = false;
        msg = format.Format(parameters);
        Assert.Equal("You are nice!", msg);

        parameters["isBad"] = true;
        msg = format.Format(parameters);
        Assert.Equal("You are not nice!", msg);

        format = new AdvancedMessageFormat("You are{isGood,if, very, not so} nice!");

        parameters = new Dictionary<string, object?>();

        msg = format.Format(parameters); // isGood is missing
        Assert.Equal("You are not so nice!", msg);

        parameters["isGood"] = false;
        msg = format.Format(parameters);
        Assert.Equal("You are not so nice!", msg);

        parameters["isGood"] = true;
        msg = format.Format(parameters);
        Assert.Equal("You are very nice!", msg);

        format = new AdvancedMessageFormat("You are{isGood,if, very\\, very} nice!");

        parameters = new Dictionary<string, object?>();

        msg = format.Format(parameters); // isGood is missing
        Assert.Equal("You are nice!", msg);

        parameters["isGood"] = false;
        msg = format.Format(parameters);
        Assert.Equal("You are nice!", msg);

        parameters["isGood"] = true;
        msg = format.Format(parameters);
        Assert.Equal("You are very, very nice!", msg);
    }

    [Fact]
    public void TestEqualsFormatting()
    {
        string msg;
        AdvancedMessageFormat format;

        format = new AdvancedMessageFormat(
            "Error{severity,equals,EventSeverity:FATAL,,\nSome explanation!}");

        var parameters = new Dictionary<string, object?>();

        // The Java test used org.apache.fop.events.model.EventSeverity, whose toString() returns
        // "EventSeverity:<NAME>". Fop.Util cannot reference Fop.Events, so this test stands in a
        // local value whose ToString() reproduces exactly that contract (the only thing the
        // 'equals' part observes).
        parameters["severity"] = FakeEventSeverity.Fatal;
        msg = format.Format(parameters);
        Assert.Equal("Error", msg);

        parameters["severity"] = FakeEventSeverity.Warn;
        msg = format.Format(parameters);
        Assert.Equal("Error\nSome explanation!", msg);
    }

    [Fact]
    public void TestChoiceFormatting()
    {
        string msg;
        AdvancedMessageFormat format;

        format = new AdvancedMessageFormat(
            "You have {amount,choice,0#nothing|0<{amount} bucks|100<more than enough}.");

        var parameters = new Dictionary<string, object?>();

        parameters["amount"] = 0;
        msg = format.Format(parameters);
        Assert.Equal("You have nothing.", msg);

        parameters["amount"] = 7;
        msg = format.Format(parameters);
        Assert.Equal("You have 7 bucks.", msg);

        parameters["amount"] = 140;
        msg = format.Format(parameters);
        Assert.Equal("You have more than enough.", msg);
    }

    /// <summary>Minimal <see cref="ILocator"/> stand-in for the SAX <c>LocatorImpl</c>.</summary>
    private sealed class LocatorImpl : ILocator
    {
        public int LineNumber { get; set; }

        public int ColumnNumber { get; set; }
    }

    /// <summary>
    /// Stand-in for the Java <c>EventSeverity</c> typesafe enum: its <see cref="ToString"/>
    /// reproduces the original "EventSeverity:&lt;NAME&gt;" contract.
    /// </summary>
    private sealed class FakeEventSeverity(string name)
    {
        public static readonly FakeEventSeverity Fatal = new("FATAL");
        public static readonly FakeEventSeverity Warn = new("WARN");

        private readonly string name = name;

        public override string ToString() => "EventSeverity:" + name;
    }
}
