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

namespace Fop.Util.Text;

/// <summary>
/// Implementations of this interface parse a field part and return message parts.
/// <para>Port of the nested <c>AdvancedMessageFormat.PartFactory</c> interface.</para>
/// </summary>
public interface IPartFactory
{
    /// <summary>Creates a new part by parsing the values parameter to configure the part.</summary>
    /// <param name="fieldName">The field name.</param>
    /// <param name="values">The unparsed parameter values (may be <c>null</c>).</param>
    /// <returns>The new message part.</returns>
    IPart NewPart(string fieldName, string? values);

    /// <summary>The name of the message part format.</summary>
    string Format { get; }
}
