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

using System.IO;
using System.Text;
using Fop.Render.Pdf;
using Fop.Render.Pdf.Native;
using PdfSharp.Pdf.IO;
using PdfSharp.Pdf.Security;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// Tests for the standard (RC4-128) PDF encryption in the native renderer. Correctness of the key /
/// owner / user computation is checked by re-opening the encrypted file with PdfSharp's reader, which
/// independently runs the standard security handler.
/// </summary>
public class NativeEncryptionTests
{
    private const string Doc = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="300pt" page-height="200pt">
              <fo:region-body/></fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body">
            <fo:block>Secret text.</fo:block>
            <fo:block><fo:basic-link external-destination="https://example.org">link</fo:basic-link></fo:block>
          </fo:flow></fo:page-sequence>
        </fo:root>
        """;

    private static byte[] Encrypted(PdfEncryptionOptions options)
    {
        using var input = new MemoryStream(Encoding.UTF8.GetBytes(Doc));
        using var output = new MemoryStream();
        new FopProcessor().ConvertNative(input, output, options);
        return output.ToArray();
    }

    [Fact]
    public void EncryptedDocumentDeclaresStandardHandler()
    {
        byte[] pdf = Encrypted(new PdfEncryptionOptions());
        string text = Encoding.Latin1.GetString(pdf);
        Assert.Contains("/Encrypt", text);
        Assert.Contains("/Filter /Standard", text);
        Assert.Contains("/V 2 /R 3 /Length 128", text);
        Assert.Contains("/ID [", text);
    }

    [Fact]
    public void EmptyUserPasswordOpensInPdfSharp()
    {
        // PdfSharp independently runs the standard security handler; opening with no password proves the
        // O/U entries and key were computed correctly.
        byte[] pdf = Encrypted(new PdfEncryptionOptions());
        using var input = new MemoryStream(pdf);
        using var doc = PdfReader.Open(input, PdfDocumentOpenMode.Import);
        Assert.Equal(1, doc.PageCount);
    }

    [Fact]
    public void UserPasswordIsRequiredAndAccepted()
    {
        byte[] pdf = Encrypted(new PdfEncryptionOptions(OwnerPassword: "owner", UserPassword: "open-sesame"));

        // The correct user password opens it.
        using (var ok = new MemoryStream(pdf))
        using (var doc = PdfReader.Open(ok, "open-sesame", PdfDocumentOpenMode.Import))
        {
            Assert.Equal(1, doc.PageCount);
        }

        // A wrong password is rejected.
        using var bad = new MemoryStream(pdf);
        Assert.ThrowsAny<System.Exception>(() => PdfReader.Open(bad, "wrong", PdfDocumentOpenMode.Import));
    }

    [Fact]
    public void PermissionsAreCarriedThrough()
    {
        byte[] pdf = Encrypted(new PdfEncryptionOptions(UserPassword: "", AllowPrinting: false));
        using var input = new MemoryStream(pdf);
        using var doc = PdfReader.Open(input, PdfDocumentOpenMode.Import);
        Assert.False(doc.SecuritySettings.PermitPrint);
    }

    [Fact]
    public void Rc4IsItsOwnInverse()
    {
        byte[] key = Encoding.ASCII.GetBytes("a-secret-key-1234");
        byte[] plain = Encoding.ASCII.GetBytes("The quick brown fox jumps over the lazy dog.");
        byte[] cipher = StandardSecurityHandler.Rc4(key, plain);
        Assert.NotEqual(plain, cipher);
        Assert.Equal(plain, StandardSecurityHandler.Rc4(key, cipher));
    }
}
