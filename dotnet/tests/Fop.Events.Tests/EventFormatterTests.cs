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
using Fop.Events;
using Xunit;

namespace Fop.Events.Tests;

/// <summary>
/// Tests for <see cref="EventFormatter"/> and <see cref="EventExceptionManager"/>. There is no
/// JUnit equivalent (the Java test depended on XML resource bundles); these cover the ported
/// behaviour: template rendering with event parameters, the pluggable message source, includes,
/// the <c>lookup</c> part, and converting an event into an exception.
/// </summary>
[Collection("EventFormatter")]
public class EventFormatterTests
{
    [Fact]
    public void FormatsPatternWithEventParameters()
    {
        var ev = new Event(this, "org.example.Producer.missingProperty", EventSeverity.Error,
            Event.Params()
                .Param("elementName", "fo:external-graphic")
                .Param("propertyName", "src")
                .Build());

        string msg = EventFormatter.Format(ev,
            "Element \"{elementName}\" is missing[ required property \"{propertyName}\"]!");
        Assert.Equal(
            "Element \"fo:external-graphic\" is missing required property \"src\"!", msg);
    }

    [Fact]
    public void ConditionalSubGroupIsSkippedWhenParameterMissing()
    {
        var ev = new Event(this, "org.example.Producer.missingProperty", EventSeverity.Error,
            Event.Params()
                .Param("elementName", "fo:external-graphic")
                .Build());

        string msg = EventFormatter.Format(ev,
            "Element \"{elementName}\" is missing[ required property \"{propertyName}\"]!");
        Assert.Equal("Element \"fo:external-graphic\" is missing!", msg);
    }

    [Fact]
    public void ResolvesTemplateFromMessageSource()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource()
                .Add("org.example.Producer", "missingProperty",
                    "Property \"{propertyName}\" is required.");

            var ev = new Event(this, "org.example.Producer.missingProperty", EventSeverity.Error,
                Event.Params().Param("propertyName", "src").Build());

            string msg = EventFormatter.Format(ev);
            Assert.Equal("Property \"src\" is required.", msg);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void MissingTemplateRendersFallback()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource();

            var ev = new Event(this, "org.example.Producer.unknownKey", EventSeverity.Error,
                Event.Params().Build());

            string msg = EventFormatter.Format(ev);
            Assert.Equal("Missing bundle. Can't lookup event key: 'unknownKey'.", msg);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void ProcessesIncludesFromMessageSource()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource()
                .Add("org.example.Producer", "greeting", "Hello {{name}}!")
                .Add("org.example.Producer", "name", "World");

            var ev = new Event(this, "org.example.Producer.greeting", EventSeverity.Info,
                Event.Params().Build());

            string msg = EventFormatter.Format(ev);
            Assert.Equal("Hello World!", msg);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void LookupPartResolvesAgainstMessageSource()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource()
                .Add("org.example.Producer", "color.red", "rouge");

            var ev = new Event(this, "org.example.Producer.colorEvent", EventSeverity.Info,
                Event.Params().Param("colorKey", "color.red").Build());

            string msg = EventFormatter.Format(ev, "The color is {colorKey,lookup}.");
            Assert.Equal("The color is rouge.", msg);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void FormatUsesGivenCulture()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource()
                .Add("org.example.Producer", "hello", "Bonjour {who}!");

            var ev = new Event(this, "org.example.Producer.hello", EventSeverity.Info,
                Event.Params().Param("who", "Marie").Build());

            string msg = EventFormatter.Format(ev, CultureInfo.GetCultureInfo("fr-FR"));
            Assert.Equal("Bonjour Marie!", msg);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void ThrowExceptionWithoutClassThrowsEventExceptionWithFormattedMessage()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource()
                .Add("org.example.Producer", "fatalError", "Cannot continue: {detail}.");

            var ev = new Event(this, "org.example.Producer.fatalError", EventSeverity.Fatal,
                Event.Params().Param("detail", "disk full").Build());

            var ex = Assert.Throws<EventException>(
                () => EventExceptionManager.ThrowException(ev, null));
            Assert.Equal("Cannot continue: disk full.", ex.Message);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void ThrowExceptionSurfacesCauseFromParameters()
    {
        var previous = EventFormatter.MessageSource;
        try
        {
            EventFormatter.MessageSource = new InMemoryEventModelMessageSource()
                .Add("org.example.Producer", "ioFailure", "I/O failed.");

            var cause = new InvalidOperationException("boom");
            var ev = new Event(this, "org.example.Producer.ioFailure", EventSeverity.Fatal,
                Event.Params().Param("cause", cause).Build());

            var ex = Assert.Throws<EventException>(
                () => EventExceptionManager.ThrowException(ev, null));
            Assert.Equal("I/O failed.", ex.Message);
            Assert.Same(cause, ex.InnerException);
        }
        finally
        {
            EventFormatter.MessageSource = previous;
        }
    }

    [Fact]
    public void ThrowExceptionUsesRegisteredFactory()
    {
        EventExceptionManager.RegisterExceptionFactory(new CustomExceptionFactory());

        var ev = new Event(this, "org.example.Producer.custom", EventSeverity.Fatal,
            Event.Params().Param("what", "details").Build());

        var ex = Assert.Throws<CustomException>(
            () => EventExceptionManager.ThrowException(ev, typeof(CustomException).FullName));
        Assert.Equal("custom: details", ex.Message);
    }

    [Fact]
    public void ThrowExceptionWithUnknownClassThrowsArgumentException()
    {
        var ev = new Event(this, "org.example.Producer.custom", EventSeverity.Fatal,
            Event.Params().Build());

        Assert.Throws<ArgumentException>(
            () => EventExceptionManager.ThrowException(ev, "no.such.Exception"));
    }

    private sealed class CustomException(string message) : Exception(message);

    private sealed class CustomExceptionFactory : IExceptionFactory
    {
        public Exception CreateException(Event @event)
            => new CustomException("custom: " + @event.GetParam("what"));

        public Type ExceptionType => typeof(CustomException);
    }
}

/// <summary>
/// Serializes tests that mutate the static <see cref="EventFormatter.MessageSource"/> so they do
/// not interfere with one another.
/// </summary>
[CollectionDefinition("EventFormatter")]
public sealed class EventFormatterCollection
{
}
