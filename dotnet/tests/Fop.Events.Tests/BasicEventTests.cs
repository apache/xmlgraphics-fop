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

using Fop.Events;
using Xunit;

namespace Fop.Events.Tests;

/// <summary>
/// Port of <c>org.apache.fop.events.BasicEventTestCase</c>, verifying the same observable
/// behaviour as the original JUnit test.
/// </summary>
public class BasicEventTests
{
    [Fact]
    public void TestBasics()
    {
        var listener = new RecordingListener();

        IEventBroadcaster broadcaster = new DefaultEventBroadcaster();
        broadcaster.AddEventListener(listener);
        Assert.True(broadcaster.HasEventListeners);

        var ev = new Event(this, "123", EventSeverity.Info,
            Event.Params()
                .Param("reason", "I'm tired")
                .Param("blah", 23)
                .Build());
        broadcaster.BroadcastEvent(ev);

        ev = listener.Event;
        Assert.NotNull(ev);
        Assert.Equal("123", listener.Event!.EventId);
        Assert.Equal(EventSeverity.Info, listener.Event.Severity);
        Assert.Equal("I'm tired", ev!.GetParam("reason"));
        Assert.Equal(23, ev.GetParam("blah"));

        broadcaster.RemoveEventListener(listener);
        Assert.False(broadcaster.HasEventListeners);

        // Just check that there are no exceptions when no listeners are present.
        broadcaster.BroadcastEvent(ev);
    }

    [Fact]
    public void TestEventProducer()
    {
        var listener = new RecordingListener();

        IEventBroadcaster broadcaster = new DefaultEventBroadcaster();
        broadcaster.AddEventListener(listener);
        Assert.True(broadcaster.HasEventListeners);

        var producer = broadcaster.GetEventProducerFor<ITestEventProducer>();
        producer.Complain(this, "I'm tired", 23);

        var ev = listener.Event;
        Assert.NotNull(ev);
        Assert.Equal($"{typeof(ITestEventProducer).FullName}.Complain", ev!.EventId);
        Assert.Equal(EventSeverity.Warn, ev.Severity);
        Assert.Equal("I'm tired", ev.GetParam("reason"));
        Assert.Equal(23, ev.GetParam("blah"));

        broadcaster.RemoveEventListener(listener);
        Assert.False(broadcaster.HasEventListeners);

        broadcaster.BroadcastEvent(ev);
    }

    [Fact]
    public void ProducerProxyIsCached()
    {
        IEventBroadcaster broadcaster = new DefaultEventBroadcaster();
        var first = broadcaster.GetEventProducerFor<ITestEventProducer>();
        var second = broadcaster.GetEventProducerFor<ITestEventProducer>();
        Assert.Same(first, second);
    }

    [Fact]
    public void FatalEventThrows()
    {
        IEventBroadcaster broadcaster = new DefaultEventBroadcaster();
        var producer = broadcaster.GetEventProducerFor<ITestEventProducer>();
        Assert.Throws<EventException>(() => producer.Explode(this, "boom"));
    }

    private sealed class RecordingListener : IEventListener
    {
        public Event? Event { get; private set; }

        public void ProcessEvent(Event @event)
        {
            if (Event is not null)
            {
                Assert.Fail("Multiple events received");
            }

            Event = @event;
        }
    }
}

/// <summary>Test event producer, mirroring the Java <c>TestEventProducer</c> fixture.</summary>
public interface ITestEventProducer : IEventProducer
{
    [Event(Severity = EventSeverity.Warn)]
    void Complain(object source, string reason, int blah);

    [Event(Severity = EventSeverity.Fatal)]
    void Explode(object source, string reason);
}
