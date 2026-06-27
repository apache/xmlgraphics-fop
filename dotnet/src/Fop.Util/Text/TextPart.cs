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

using System.Text;

namespace Fop.Util.Text;

/// <summary>A constant text part. Port of the nested <c>AdvancedMessageFormat.TextPart</c>.</summary>
internal sealed class TextPart(string text) : IPart
{
    private readonly string text = text;

    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters) => sb.Append(text);

    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters) => true;

    public override string ToString() => text;
}
