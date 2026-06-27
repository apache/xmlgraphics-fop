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

namespace Fop.Util;

/// <summary>
/// Factory interface implemented by classes that can instantiate content handlers which parse a SAX
/// stream into objects.
/// <para>Port of the Java interface <c>org.apache.fop.util.ContentHandlerFactory</c>.</para>
/// <para>
/// TODO: In Java <c>createContentHandler()</c> returns an <c>org.xml.sax.ContentHandler</c>. The SAX
/// <c>ContentHandler</c> binding (and the <c>SAXException</c> it may throw) lands with the parser
/// port; until then the create method is typed as <c>object</c> so the factory shape and its nested
/// <see cref="IObjectSource"/> / <see cref="IObjectBuiltListener"/> contracts can be expressed
/// without pulling in unported SAX infrastructure.
/// </para>
/// </summary>
public interface IContentHandlerFactory
{
    /// <summary>Gets the namespaces supported by the content handlers this factory creates.</summary>
    /// <returns>an array of supported namespace URIs.</returns>
    string[] GetSupportedNamespaces();

    /// <summary>
    /// Creates a new content handler to handle a SAX stream.
    /// </summary>
    /// <returns>a new content handler (a SAX <c>ContentHandler</c> once the parser port lands).</returns>
    object CreateContentHandler();
}

/// <summary>
/// Interface that content-handler implementations parsing objects from XML can implement to return
/// those objects.
/// <para>Port of the nested Java interface <c>ContentHandlerFactory.ObjectSource</c>.</para>
/// </summary>
public interface IObjectSource
{
    /// <summary>Gets the object parsed from the SAX stream (valid only after parsing).</summary>
    object? Object { get; }

    /// <summary>
    /// Sets a listener which gets notified when the object is fully built.
    /// </summary>
    /// <param name="listener">the listener to notify.</param>
    void SetObjectBuiltListener(IObjectBuiltListener listener);
}

/// <summary>
/// Listener interface for objects which want to be notified when a content handler implementing
/// <see cref="IObjectSource"/> has finished parsing.
/// <para>
/// Port of the nested Java interface <c>ContentHandlerFactory.ObjectBuiltListener</c>, which extends
/// <c>java.util.EventListener</c> (a bare marker interface with no .NET equivalent, so it is dropped).
/// </para>
/// </summary>
public interface IObjectBuiltListener
{
    /// <summary>
    /// Notifies the listener that the object is fully built.
    /// </summary>
    /// <param name="obj">the newly built object.</param>
    void NotifyObjectBuilt(object obj);
}
