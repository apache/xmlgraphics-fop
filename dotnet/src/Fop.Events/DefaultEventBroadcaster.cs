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

using System.Collections.Concurrent;
using System.Reflection;

namespace Fop.Events;

/// <summary>
/// Default <see cref="IEventBroadcaster"/>. Holds a list of listeners and produces type-safe
/// event-producer proxies.
/// <para>
/// The Java implementation built dynamic <c>java.lang.reflect.Proxy</c> instances driven by a
/// generated <c>event-model.xml</c>. This port uses <see cref="DispatchProxy"/> and reads event
/// metadata from <see cref="EventAttribute"/> and the producer method's parameter names directly,
/// so no generated model file is required.
/// </para>
/// </summary>
public class DefaultEventBroadcaster : IEventBroadcaster
{
    /// <summary>Holds all registered event listeners.</summary>
    protected CompositeEventListener Listeners { get; } = new();

    private readonly ConcurrentDictionary<Type, IEventProducer> proxies = new();

    /// <inheritdoc/>
    public void AddEventListener(IEventListener listener) => Listeners.AddEventListener(listener);

    /// <inheritdoc/>
    public void RemoveEventListener(IEventListener listener) => Listeners.RemoveEventListener(listener);

    /// <inheritdoc/>
    public bool HasEventListeners => Listeners.HasEventListeners;

    /// <inheritdoc/>
    public void BroadcastEvent(Event @event) => Listeners.ProcessEvent(@event);

    /// <inheritdoc/>
    public T GetEventProducerFor<T>() where T : class, IEventProducer
        => (T)proxies.GetOrAdd(typeof(T), _ => CreateProxyFor<T>());

    /// <summary>Creates a dispatch proxy that turns method calls on <typeparamref name="T"/> into events.</summary>
    protected virtual T CreateProxyFor<T>() where T : class, IEventProducer
    {
        if (!typeof(T).IsInterface)
        {
            throw new ArgumentException(
                $"Type must be an EventProducer interface: {typeof(T).FullName}");
        }

        var proxy = DispatchProxy.Create<T, EventProducerProxy>();
        ((EventProducerProxy)(object)proxy).Initialize(this, typeof(T));
        return proxy;
    }

    /// <summary>
    /// <see cref="DispatchProxy"/> that converts a producer method invocation into an
    /// <see cref="Event"/> broadcast, raising an exception for fatal events.
    /// </summary>
    public class EventProducerProxy : DispatchProxy
    {
        private DefaultEventBroadcaster broadcaster = null!;
        private Type interfaceType = null!;

        internal void Initialize(DefaultEventBroadcaster owner, Type producerInterface)
        {
            broadcaster = owner;
            interfaceType = producerInterface;
        }

        /// <inheritdoc/>
        protected override object? Invoke(MethodInfo? targetMethod, object?[]? args)
        {
            ArgumentNullException.ThrowIfNull(targetMethod);
            args ??= [];

            var methodParameters = targetMethod.GetParameters();
            if (methodParameters.Length < 1)
            {
                throw new InvalidOperationException(
                    $"EventProducer method '{interfaceType.FullName}.{targetMethod.Name}' must take "
                    + "'object source' as its first parameter.");
            }

            var attribute = targetMethod.GetCustomAttribute<EventAttribute>();
            var severity = attribute?.Severity ?? EventSeverity.Info;

            var builder = Event.Params();
            for (int i = 1; i < methodParameters.Length; i++)
            {
                builder.Param(methodParameters[i].Name ?? $"arg{i}", args[i]);
            }

            string eventId = $"{interfaceType.FullName}.{targetMethod.Name}";
            object source = args[0]
                ?? throw new ArgumentNullException(methodParameters[0].Name ?? "source");

            var @event = new Event(source, eventId, severity, builder.Build());
            broadcaster.BroadcastEvent(@event);

            if (@event.Severity == EventSeverity.Fatal)
            {
                throw CreateException(@event, attribute?.ExceptionType);
            }

            return null;
        }

        private static Exception CreateException(Event @event, Type? exceptionType)
        {
            string message = $"{@event.EventId}: {DescribeParams(@event)}";

            if (exceptionType is not null)
            {
                var instance = Activator.CreateInstance(exceptionType, message) as Exception;
                if (instance is not null)
                {
                    return instance;
                }
            }

            // If any parameter carries the original exception, surface it as the cause.
            foreach (var value in @event.Parameters.Values)
            {
                if (value is Exception cause)
                {
                    return new EventException(message, cause);
                }
            }

            return new EventException(message);
        }

        private static string DescribeParams(Event @event) =>
            string.Join(", ", @event.Parameters.Select(kvp => $"{kvp.Key}={kvp.Value}"));
    }
}
