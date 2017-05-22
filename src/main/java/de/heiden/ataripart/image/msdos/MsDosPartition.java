package de.heiden.ataripart.image.msdos;

import de.heiden.ataripart.image.IntUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * MS DOS partition.
 */
public class MsDosPartition {
    /**
     * Bootable?.
     */
    private final boolean bootable;

    /**
     * (File system) type.
     */
    private final int type;

    /**
     * Start sector.
     */
    private final long startSector;

    /**
     * Number of sectors.
     */
    private final long numSectors;

    /**
     * Constructor.
     *
     * @param bootable Bootable?.
     * @param type (File system) type.
     * @param startSector Start sector.
     * @param numSectors Number of sectors.
     */
    public MsDosPartition(boolean bootable, int type, long startSector, long numSectors) {
        this.bootable = bootable;
        this.type = type;
        this.startSector = startSector;
        this.numSectors = numSectors;
    }

    /**
     * Bootable?.
     */
    public boolean isBootable() {
        return bootable;
    }

    /**
     * (File system) type.
     */
    public int getType() {
        return type;
    }

    /**
     * Start sector.
     */
    public long getStartSector() {
        return startSector;
    }

    /**
     * Number of sectors.
     */
    public long getNumSectors() {
        return numSectors;
    }

    /**
     * Create partition entry.
     */
    public ByteBuffer createPartitionEntry() {
        ByteBuffer partitionEntry = ByteBuffer.allocate(16);
        partitionEntry.order(ByteOrder.LITTLE_ENDIAN);

        // Flags.
        IntUtils.setInt8(partitionEntry, 0x00, bootable? 0x80 : 0x00);
        // CHS start sector, unused.
        IntUtils.setInt8(partitionEntry, 0x01, 0x00);
        IntUtils.setInt8(partitionEntry, 0x02, 0x00);
        IntUtils.setInt8(partitionEntry, 0x03, 0x00);
        // File system type.
        IntUtils.setInt8(partitionEntry, 0x04, type);
        // CHS end sector, unused.
        IntUtils.setInt8(partitionEntry, 0x05, 0x00);
        IntUtils.setInt8(partitionEntry, 0x06, 0x00);
        IntUtils.setInt8(partitionEntry, 0x07, 0x00);
        // Start sector.
        IntUtils.setInt32(partitionEntry, 0x08, startSector);
        // LBA number of sectors.
        IntUtils.setInt32(partitionEntry, 0x0C, numSectors);

        return partitionEntry;
    }
}
