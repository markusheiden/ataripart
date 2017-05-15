package de.heiden.ataripart;

import java.util.ArrayList;
import java.util.List;

/**
 * Root sector info.
 */
public class RootSector {
    /**
     * Cylinders.
     */
    private final int cylinders;

    /**
     * Heads.
     */
    private final int heads;

    /**
     * Sectors.
     */
    private final int sectors;

    /**
     * Absolute offset in bytes of root sector in disk.
     */
    private final long offset;

    /**
     * Size of disk in bytes.
     */
    private final long size;

    /**
     * Checksum.
     */
    private final int checksum;

    /**
     * Partitions defined by this root sector.
     */
    private final List<Partition> partitions = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param cylinders Cylinders.
     * @param heads Heads.
     * @param sectors Sectors.
     * @param offset Absolute offset in bytes of root sector in disk
     * @param size Size of disk in bytes
     * @param checksum Checksum
     */
    public RootSector(int cylinders, int heads, int sectors, long offset, long size, int checksum) {
        this.cylinders = cylinders;
        this.heads = heads;
        this.sectors = sectors;
        this.offset = offset;
        this.size = size;
        this.checksum = checksum;
    }

    /**
     * Cylinders.
     */
    public int getCylinders() {
        return cylinders;
    }

    /**
     * Heads.
     */
    public int getHeads() {
        return heads;
    }

    /**
     * Sectors.
     */
    public int getSectors() {
        return sectors;
    }

    /**
     * Absolute offset in bytes of root sector in disk.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Is this root sector an xgm root sector?.
     * Currently it is assumed that it is an xgm root sector, if the offset is greater than 0.
     */
    public boolean isXGM() {
        return offset > 0;
    }

    /**
     * Size of disk in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Absolute offset in bytes where the disk ends.
     */
    public long getEnd() {
        return offset + size;
    }

    /**
     * Checksum.
     */
    public int getChecksum() {
        return checksum;
    }

    /**
     * All partitions defined by this root sector.
     */
    public List<Partition> getAllPartitions() {
        return partitions;
    }

    /**
     * All valid and active partitions defined by this root sector.
     */
    public List<Partition> getPartitions() {
        List<Partition> result = new ArrayList<>(partitions.size());
        for (Partition partition : partitions) {
            if (partition.isValid() && partition.isActive()) {
                result.add(partition);
            }
        }

        return result;
    }

    /**
     * All valid and active partitions defined by this root sector, except the xgm ones.
     */
    public List<Partition> getRealPartitions() {
        List<Partition> result = new ArrayList<>(partitions.size());
        for (Partition partition : partitions) {
            if (partition.isValid() && partition.isActive() && !partition.isXGM()) {
                result.add(partition);
            }
        }

        return result;
    }

    /**
     * Add a partition.
     *
     * @param partition Partition
     */
    protected void add(Partition partition) {
        partitions.add(partition);
    }

    /**
     * Check, if this root sectors contains at least one valid active partition.
     *
     * @see Partition#isValid()
     * @see Partition#isActive()
     */
    public boolean hasValidPartitions() {
        for (Partition partition : partitions) {
            if (partition.isValid() && partition.isActive()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(1024);
        if (isXGM()) {
            result.append("XGM ");
        }
        result.append("Root sector\n");
        result.append("CHS     : ").append(getCylinders()).append("/").append(getSectors()).append("/").append(getSectors()).append("\n");
        result.append("Start   : ").append(getOffset()).append("\n");
        result.append("First   : ").append(getOffset() + 512).append("\n");
        if (!isXGM()) {
            result.append("Size    : ").append(getSize()).append("\n");
            result.append("End     : ").append(getEnd()).append("\n");
        }
        result.append("Checksum: ").append(IntUtils.hex(getChecksum(), 4)).append("\n");

        return result.toString();
    }

    //
    // Parsing
    //

    /**
     * Parse sector as root sector.
     *
     * @param xgmOffset Absolute offset of the (first) xgm root sector
     * @param offset Absolute offset in bytes of sector
     * @param sector Sector image
     */
    public static RootSector parse(long xgmOffset, long offset, byte[] sector) {
        return parse(xgmOffset, offset, sector, 0);
    }

    /**
     * Parse a given sector as root sector.
     *
     * @param xgmOffset Absolute offset of the (first) xgm root sector
     * @param offset Absolute offset in bytes of disk image part
     * @param disk Disk image part
     * @param index Index of root sector in disk image part
     */
    public static RootSector parse(long xgmOffset, long offset, byte[] disk, int index) {
        int cyclinders = IntUtils.getInt16LittleEndian(disk, index + 0x1B6);
        int heads = IntUtils.getInt8(disk, index + 0x1B8);
        int sectors = IntUtils.getInt8(disk, index + 0x1C1);
        long size = IntUtils.getInt32LittleEndian(disk, index + 0x1C2) * 512;
        int checksum = IntUtils.checksumInt16LittleEndian(disk, index, 512);

        RootSector result = new RootSector(cyclinders, heads, sectors, offset, size, checksum);

        for (int i = 0; i < 4; i++) {
            result.add(parse(xgmOffset, offset, disk, index, 0x1C6 + i * 12, i));
        }

        for (int i = 0; i < 8; i++) {
            result.add(parse(xgmOffset, offset, disk, index, 0x156 + i * 12, i + 4));
        }

        return result;
    }

    /**
     * Parse a partition in a given root sector.
     *
     * @param xgmOffset Absolute offset of the (first) xgm root sector
     * @param offset Absolute offset in bytes of disk image part
     * @param disk Disk image part
     * @param index Index of root sector in disk image part
     * @param pos Offset of partition info in root
     * @param i nNumber of partition
     * @return Parsed Partition
     */
    private static Partition parse(long xgmOffset, long offset, byte[] disk, int index, int pos, int i) {
        Partition partition = Partition.parse(i, disk, index + pos);
        partition.setOffset(partition.isXGM() ? xgmOffset + index : offset + index);
        return partition;
    }
}
