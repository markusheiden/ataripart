package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.Partition;
import de.waldheinz.fs.BlockDevice;
import de.waldheinz.fs.ReadOnlyException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.READ;

/**
 * Block device for reading partition files.
 */
public class PartitionDisk implements BlockDevice {
    /**
     * Disk image.
     */
    private final FileChannel disk;

    /**
     * Partition in disk image.
     */
    private final Partition partition;

    /**
     * Constructor.
     *
     * @param disk Disk image.
     * @param partition Partition in disk image.
     */
    public PartitionDisk(Path disk, Partition partition) throws IOException {
        this.disk = FileChannel.open(disk, READ);
        this.partition = partition;
    }

    @Override
    public int getSectorSize() throws IOException {
        return 512;
    }

    @Override
    public long getSize() throws IOException {
        return partition.getLength();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void read(long devOffset, ByteBuffer dest) throws IOException {
        disk.read(dest, partition.getAbsoluteStart() +  devOffset);
    }

    @Override
    public void write(long devOffset, ByteBuffer src) throws ReadOnlyException, IOException, IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() throws IOException {
        // Not needed for read only.
    }

    @Override
    public boolean isClosed() {
        return !disk.isOpen();
    }

    @Override
    public void close() throws IOException {
        disk.close();
    }
}
