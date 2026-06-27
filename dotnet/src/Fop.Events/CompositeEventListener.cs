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
/// An <see cref="IEventListener"/> that forwards events to zero or more other listeners,
/// in registration order.
/// <para>Port of <c>org.apache.fop.events.CompositeEventListener</c>.</para>
/// </summary>
public class CompositeEventListener : IEventListener
{
    private readonly List<IEventListener> listeners = new();
    private readonly Lock sync = new();

    /// <summary>Adds a listener (appended to the calling order).</summary>
    public void AddEventListener(IEventListener listener)
    {
        lock (sync)
        {
            listeners.Add(listener);
        }
    }

    /// <summary>Removes a listener. Does nothing if it is not registered.</summary>
    public void RemoveEventListener(IEventListener listener)
    {
        lock (sync)
        {
            listeners.Remove(listener);
        }
    }

    /// <summary>Indicates whether any listeners have been registered.</summary>
    public bool HasEventListeners
    {
        get
        {
            lock (sync)
            {
                return listeners.Count > 0;
            }
        }
    }

    /// <inheritdoc/>
    public void ProcessEvent(Event @event)
    {
        IEventListener[] snapshot;
        lock (sync)
        {
            snapshot = listeners.ToArray();
        }

        foreach (var listener in snapshot)
        {
            listener.ProcessEvent(@event);
        }
    }
}
