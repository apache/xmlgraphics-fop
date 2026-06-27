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

namespace Fop.Colors;

/// <summary>
/// The Standard OCA Color Value table entries used by OCA (Object Content
/// Architecture) documents, mostly to represent text foreground colour in AFP.
/// <para>
/// Port of the nested enum <c>org.apache.fop.util.OCAColor.OCAColorValue</c>.
/// The underlying integer values are the two-byte OCA codes and are preserved
/// exactly.
/// </para>
/// </summary>
public enum OCAColorValue
{
    Blue = 0x1,
    Red = 0x2,
    Magenta = 0x3,
    Green = 0x4,
    Cyan = 0x5,
    Yellow = 0x6,
    Black = 0x8,
    Brown = 0x10,
    DeviceDefault = 0xFF07,
    MediumColor = 0xFF08,
}
