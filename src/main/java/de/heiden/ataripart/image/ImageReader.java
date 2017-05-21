package de.heiden.ataripart.image;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Hard disk image reader.
 */
public class ImageReader {
    /**
     * File with hard disk image.
     */
    private File file;

    /**
     * File with hard disk image.
     */
    private RandomAccessFile image;

    /**
     * Constructor.
     *
     * @param file The file with the hard disk image.
     */
    public ImageReader(File file) throws IOException {
        this.file = file;
        this.image = new RandomAccessFile(file.getCanonicalFile(), "r");
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Close this tool.
     * <p>
     * This will release the underlying hard disk image file.
     */
    public void close() throws IOException {
        image.close();
    }

    /**
     * Hard disk image file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Hard disk image file channel.
     */
    public FileChannel getChannel() {
        return image.getChannel();
    }

    /**
     * Read from image at the given position to the buffer.
     * <p>
     * Sets buffer position to 0.
     *
     * @param position Absolute position in hard disk image.
     * @param buffer Buffer to read to.
     * @return Number of bytes read.
     */
    public int read(long position, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int num = image.getChannel().position(position).read(buffer);
        buffer.position(0);
        return num;
    }

    /**
     * Read master root sector and all following xgm root sectors.
     */
    public List<RootSector> readRootSectors() throws IOException {
        List<RootSector> result = new ArrayList<>();

        RootSector rootSector = readRootSector(0, 0, 0);
        result.add(rootSector);

        for (Partition partition : rootSector.getPartitions()) {
            if (partition.isXGM()) {
                // remember the offset of the (first) xgm root sector.
                readXGMRootSectors(partition.getAbsoluteStart(), partition.getAbsoluteStart(), result);

                // only one xgm partition per root sector is allowed
                break;
            }
        }

        return result;
    }

    /**
     * Read xgm root sector and all following xgm root sectors.
     *
     * @param xgmOffset Absolute offset of the (first) xgm root sector.
     * @param offset Absolute offset to read the root sector from.
     * @param result Resulting list with all root sectors.
     */
    private void readXGMRootSectors(long xgmOffset, long offset, List<RootSector> result) throws IOException {
        RootSector rootSector = readRootSector(xgmOffset, offset, offset);
        result.add(rootSector);

        for (Partition partition : rootSector.getPartitions()) {
            if (partition.isXGM()) {
                // the offsets of all following xgm root sectors are relative to the first xgm root sector.
                readXGMRootSectors(xgmOffset, xgmOffset + partition.getStart(), result);

                // only one xgm partition per root sector is allowed
                break;
            }
        }
    }

    /**
     * Read root sector (non-recursively).
     *
     * @param xgmOffset Absolute offset of the (first) xgm root sector.
     * @param offset Logical offset in disk image, normally should be set to diskOffset.
     * @param diskOffset Offset in disk image to read first root sector from.
     */
    public RootSector readRootSector(long xgmOffset, long offset, long diskOffset) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read root sector with partitions.
        read(diskOffset, buffer);
        RootSector result = RootSector.parse(xgmOffset, offset, buffer);

        // Read BIOS parameter blocks for real partitions.
        for (Partition partition : result.getRealPartitions()) {
            if (partition.getAbsoluteStart() + 512 <= image.length()) {
                read(partition.getAbsoluteStart(), buffer);
                partition.setBootSector(BootSector.parse(buffer));
            }
        }

        return result;
    }
}
