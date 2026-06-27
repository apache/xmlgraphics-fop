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

namespace Fop.Events;

/// <summary>
/// Central relay for events: receives events and forwards them to registered listeners,
/// and provides type-safe event producers.
/// <para>Port of <c>org.apache.fop.events.EventBroadcaster</c>.</para>
/// </summary>
public interface IEventBroadcaster
{
    /// <summary>Adds a listener. Listeners are invoked in registration order.</summary>
    void AddEventListener(IEventListener listener);

    /// <summary>Removes a listener. Does nothing if it is not registered.</summary>
    void RemoveEventListener(IEventListener listener);

    /// <summary>Indicates whether any listeners are registered.</summary>
    bool HasEventListeners { get; }

    /// <summary>Broadcasts an event to all registered listeners.</summary>
    void BroadcastEvent(Event @event);

    /// <summary>
    /// Returns a type-safe event-producer proxy for the given <see cref="IEventProducer"/> interface.
    /// </summary>
    /// <typeparam name="T">A descendant interface of <see cref="IEventProducer"/>.</typeparam>
    T GetEventProducerFor<T>() where T : class, IEventProducer;
}
