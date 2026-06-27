namespace Fop.Pdf;

/// <summary>
/// A write-only stream wrapper that counts the number of bytes written through it, mirroring the
/// role of Apache Commons IO's <c>CountingOutputStream</c> used by FOP's PDF objects.
/// </summary>
internal sealed class CountingStream(Stream inner) : Stream
{
    private long _count;

    /// <summary>The number of bytes written through this stream.</summary>
    public long Count => _count;

    public override bool CanRead => false;
    public override bool CanSeek => false;
    public override bool CanWrite => true;
    public override long Length => throw new NotSupportedException();

    public override long Position
    {
        get => throw new NotSupportedException();
        set => throw new NotSupportedException();
    }

    public override void Write(byte[] buffer, int offset, int count)
    {
        inner.Write(buffer, offset, count);
        _count += count;
    }

    public override void WriteByte(byte value)
    {
        inner.WriteByte(value);
        _count++;
    }

    public override void Flush() => inner.Flush();

    public override int Read(byte[] buffer, int offset, int count) => throw new NotSupportedException();
    public override long Seek(long offset, SeekOrigin origin) => throw new NotSupportedException();
    public override void SetLength(long value) => throw new NotSupportedException();
}
