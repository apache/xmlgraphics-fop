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
/// Represents a qualified name of an XML element or an XML attribute.
/// <para>
/// Note: this class can carry a namespace prefix, but the prefix is not used in
/// <see cref="Equals(object)"/> or <see cref="GetHashCode"/>.
/// </para>
/// <para>
/// In Java, <c>org.apache.fop.util.QName</c> is a thin (deprecated) subclass of
/// <c>org.apache.xmlgraphics.util.QName</c>. As xmlgraphics-commons is not part of this port,
/// the behaviour of the base class is folded directly into this self-contained type.
/// </para>
/// </summary>
public class QName : IEquatable<QName>
{
    private readonly int hashCode;

    /// <summary>
    /// Main constructor.
    /// </summary>
    /// <param name="namespaceUri">the namespace URI (may be <c>null</c>).</param>
    /// <param name="prefix">the namespace prefix (may be <c>null</c>).</param>
    /// <param name="localName">the local name.</param>
    public QName(string? namespaceUri, string? prefix, string localName)
    {
        ArgumentNullException.ThrowIfNull(localName);
        NamespaceUri = namespaceUri;
        Prefix = prefix;
        LocalName = localName;
        QualifiedName = prefix is null ? localName : prefix + ':' + localName;
        hashCode = ComputeHashCode(namespaceUri, localName);
    }

    /// <summary>
    /// Convenience constructor taking the fully qualified name.
    /// </summary>
    /// <param name="namespaceUri">the namespace URI (may be <c>null</c>).</param>
    /// <param name="qualifiedName">the qualified name (<c>prefix:localName</c> or just
    /// <c>localName</c>).</param>
    public QName(string? namespaceUri, string qualifiedName)
    {
        ArgumentNullException.ThrowIfNull(qualifiedName);
        NamespaceUri = namespaceUri;
        int colon = qualifiedName.IndexOf(':');
        if (colon > 0)
        {
            Prefix = qualifiedName[..colon];
            LocalName = qualifiedName[(colon + 1)..];
        }
        else
        {
            Prefix = null;
            LocalName = qualifiedName;
        }

        QualifiedName = qualifiedName;
        hashCode = ComputeHashCode(namespaceUri, LocalName);
    }

    /// <summary>Gets the namespace URI of this qualified name (may be <c>null</c>).</summary>
    public string? NamespaceUri { get; }

    /// <summary>Gets the namespace prefix of this qualified name (may be <c>null</c>).</summary>
    public string? Prefix { get; }

    /// <summary>Gets the local name of this qualified name.</summary>
    public string LocalName { get; }

    /// <summary>Gets the fully qualified name (<c>prefix:localName</c> if a prefix is set).</summary>
    public string QualifiedName { get; }

    /// <inheritdoc/>
    public override int GetHashCode() => hashCode;

    /// <inheritdoc/>
    // Equality (and hashing) deliberately ignore the prefix, matching the Java original.
    public bool Equals(QName? other) =>
        other is not null
        && string.Equals(NamespaceUri, other.NamespaceUri, StringComparison.Ordinal)
        && string.Equals(LocalName, other.LocalName, StringComparison.Ordinal);

    /// <inheritdoc/>
    public override bool Equals(object? obj) =>
        ReferenceEquals(this, obj) || (obj is QName other && Equals(other));

    /// <inheritdoc/>
    public override string ToString() =>
        NamespaceUri is null ? QualifiedName : $"{{{NamespaceUri}}}{QualifiedName}";

    private static int ComputeHashCode(string? namespaceUri, string localName)
    {
        // Mirrors the xmlgraphics-commons QName hash: 31-based, prefix excluded.
        int hash = 7;
        hash = 31 * hash + (namespaceUri?.GetHashCode(StringComparison.Ordinal) ?? 0);
        hash = 31 * hash + localName.GetHashCode(StringComparison.Ordinal);
        return hash;
    }
}
