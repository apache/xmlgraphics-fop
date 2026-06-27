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

using System.Xml;

namespace Fop.Configuration;

/// <summary>
/// Builds a <see cref="DefaultConfiguration"/> tree by parsing an XML stream or file.
/// <para>Port of <c>org.apache.fop.configuration.DefaultConfigurationBuilder</c>.</para>
/// <para>
/// The Java original used <c>javax.xml.parsers.DocumentBuilder</c>; this port loads an
/// <see cref="XmlDocument"/> via an <see cref="XmlReader"/> configured to match the Java
/// factory: non-validating, comments ignored, entity references expanded. (commons-logging
/// is dropped; the Java LOG field was unused.)
/// </para>
/// </summary>
public class DefaultConfigurationBuilder
{
    private static XmlReaderSettings CreateReaderSettings() => new()
    {
        // Validating(false): no DTD validation. ProhibitDtd would reject inline DTDs that
        // the Java parser tolerated, so DTDs are parsed but not validated.
        DtdProcessing = DtdProcessing.Parse,
        // setIgnoringComments(true)
        IgnoreComments = true,
        // setExpandEntityReferences(true): the default XmlReader behaviour expands entities.
        // setIgnoringElementContentWhitespace(true) only has effect with a DTD/validation,
        // which is disabled here, so whitespace is preserved (matching the Java factory).
        IgnoreWhitespace = false,
        XmlResolver = null,
    };

    /// <summary>Builds a configuration from the given XML stream. The stream is closed afterwards.</summary>
    /// <exception cref="ConfigurationException">If the XML cannot be parsed.</exception>
    public DefaultConfiguration Build(Stream confStream)
    {
        ArgumentNullException.ThrowIfNull(confStream);
        try
        {
            XmlDocument document = DefaultConfiguration.NewDocument();
            using XmlReader reader = XmlReader.Create(confStream, CreateReaderSettings());
            document.Load(reader);
            return new DefaultConfiguration(RequireDocumentElement(document));
        }
        catch (Exception e) when (e is XmlException or IOException)
        {
            throw new ConfigurationException("xml parse error", e);
        }
        finally
        {
            // Mirrors the Java finally block which closed the stream (rethrowing any
            // close failure as an unchecked exception).
            confStream.Dispose();
        }
    }

    /// <summary>Builds a configuration by reading and parsing the given file.</summary>
    /// <exception cref="ConfigurationException">If the file cannot be read or parsed.</exception>
    public DefaultConfiguration BuildFromFile(FileInfo file)
    {
        ArgumentNullException.ThrowIfNull(file);
        try
        {
            XmlDocument document = DefaultConfiguration.NewDocument();
            using FileStream stream = file.OpenRead();
            using XmlReader reader = XmlReader.Create(stream, CreateReaderSettings());
            document.Load(reader);
            return new DefaultConfiguration(RequireDocumentElement(document));
        }
        catch (Exception e) when (e is XmlException or IOException)
        {
            throw new ConfigurationException("xml parse error", e);
        }
    }

    private static XmlElement RequireDocumentElement(XmlDocument document) =>
        document.DocumentElement
            ?? throw new ConfigurationException("xml parse error: no document element");
}
