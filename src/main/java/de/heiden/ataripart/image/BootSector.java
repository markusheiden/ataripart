package de.heiden.ataripart.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static de.heiden.ataripart.image.IntUtils.*;

/**
 * Boot sector (BIOS parameter block) info.
 */
public class BootSector {
    /**
     * Number of sectors.
     */
    private final long sectors;

    /**
     * Bytes per sector, standard is 512.
     */
    private final int bytesPerSector;

    /**
     * Sectors per cluster, standard is 2.
     */
    private final int sectorsPerCluster;

    /**
     * Detected file system.
     */
    private final FileSystem fileSystem;

    /**
     * Specified file system type.
     */
    private final String type;

    /**
     * Partition label.
     */
    private final String label;

    /**
     * Serial number (UID) of partition.
     */
    private final long serial;

    /**
     * Checksum.
     */
    private final int checksum;

    /**
     * Constructor.
     *
     * @param sectors Number of sectors.
     * @param bytesPerSector Bytes per sector, standard is 512
     * @param sectorsPerCluster Sectors per cluster, standard is 2
     * @param fileSystem Detected file system
     * @param type Specified file system type, optional for FAT12
     * @param label Partition label, optional for FAT12
     * @param serial Serial number (UID) of partition
     * @param checksum Checksum
     */
    public BootSector(long sectors, int bytesPerSector, int sectorsPerCluster, FileSystem fileSystem, String type, String label, long serial, int checksum) {
        this.sectors = sectors;
        this.bytesPerSector = bytesPerSector;
        this.sectorsPerCluster = sectorsPerCluster;
        this.fileSystem = fileSystem;
        this.type = type != null ? type.trim() : null;
        this.label = label != null ? label.trim() : null;
        this.serial = serial;
        this.checksum = checksum;
    }

    /**
     * Number of sectors.
     */
    public long getSectors() {
        return sectors;
    }

    /**
     * Bytes per sector, standard is 512.
     */
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    /**
     * Sectors per cluster, standard is 2.
     */
    public int getSectorsPerCluster() {
        return sectorsPerCluster;
    }

    /**
     * Detected file system.
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Specified file system type.
     */
    public String getType() {
        return type;
    }

    /**
     * Partition label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Serial number (UID) of partition.
     */
    public long getSerial() {
        return serial;
    }

    /**
     * Checksum.
     */
    public int getChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(256);
        result.append("Boot Sector\n");
        result.append("S/Cluster : ").append(getSectorsPerCluster()).append("\n");
        result.append("Sectors   : ").append(getSectors()).append("\n");
        result.append("Bytes/S   : ").append(getBytesPerSector()).append("\n");
        result.append("Capacity  : ").append(getSectors() * getBytesPerSector()).append("\n");
        switch (getFileSystem()) {
            case FAT12:
                result.append("Detected: FAT12/unknown\n");
                break;
            case FAT16:
            case FAT32:
                result.append("Detected  : ").append(getFileSystem()).append("\n");
                result.append("FS type   : ").append(getType()).append("\n");
                result.append("Label     : ").append(getLabel()).append("\n");
                result.append("Serial    : ").append(hex(getSerial(), 8)).append("\n");
                break;
        }
        result.append("Checksum  : ").append(hex(getChecksum(), 4));
        if (getChecksum() == 0x1234) {
            result.append(" (executable)");
        }
        result.append("\n");

        return result.toString();
    }

    //
    // Parsing
    //

    /**
     * Parse a BIOS parameter block.
     *
     * @param disk Hard disk image part. The buffer position has to be set to the start of the boot sector.
     */
    public static BootSector parse(ByteBuffer disk) {
        // All boot sector values are little endian, due to MS DOS compatibility.
        ByteBuffer bootSector = disk.slice();
        bootSector.order(ByteOrder.LITTLE_ENDIAN);

        String systemName = StringUtils.getString(bootSector, 0x03, 8);
        int bytesPerSector = getInt16(bootSector, 0x0B);
        int sectorsPerCluster = getInt8(bootSector, 0x0D);
        int reservedSectors = getInt16(bootSector, 0x0E);
        int numFATs = getInt8(bootSector, 0x10);
        int maxDirectoryEntries = getInt16(bootSector, 0x11);
        int sectors16 = getInt16(bootSector, 0x13); // 0x00: Use value at 0x20.
        int mediaDecriptor = getInt8(bootSector, 0x15); // 0xF8: hard disk, 0xF* floppy disk.
        long sectorsPerFAT = getInt16(bootSector, 0x16);
        int sectorsPerTrack = getInt16(bootSector, 0x18);
        int heads = getInt16(bootSector, 0x1A); // floppy disk: sides.
        long hiddenSectors = getInt32(bootSector, 0x1C);
        long sectors32 = getInt32(bootSector, 0x20); // 0x00: Use value at 0x20.
        long sectors = sectors16 == 0 ? sectors32 : sectors16;

        // Checksum is big endian, because it is Atari specific.
        int checksum = checksumInt16(disk, 0, 512);

        FileSystem fileSystem = FileSystem.FAT12;
        int driveNumber;
        String type = null;
        String label = null;
        long serial = 0;
        if (IntUtils.getInt8(bootSector, 0x26) == 0x29) {
            // (FAT12/)FAT16 detected due to "magic byte" 0x29 at index 0x26
            fileSystem = FileSystem.FAT16;
            driveNumber = getInt8(bootSector, 0x24); // 0x00: floppy disk, 0x80+: hard disk.
            // 0x25: unused.
            serial = getInt32(bootSector, 0x27);
            label = StringUtils.getString(bootSector, 0x2B, 11);
            type = StringUtils.getString(bootSector, 0x36, 8);
            // BPB ends at 0x3E.
        }
        if (IntUtils.getInt8(bootSector, 0x42) == 0x29) {
            // FAT32 detected due to "magic byte" 0x29 at index 0x42
            fileSystem = FileSystem.FAT32;
            sectorsPerFAT = getInt16(bootSector, 0x24);
            driveNumber = getInt8(bootSector, 0x40);
            // 0x41: unused.
            serial = getInt32(bootSector, 0x43);
            label = StringUtils.getString(bootSector, 0x47, 11);
            type = StringUtils.getString(bootSector, 0x52, 8);
            // BPB ends at 0x5A.
        }


//        System.out.println(hexDump(disk, 512));

        return new BootSector(sectors, bytesPerSector, sectorsPerCluster, fileSystem, type, label, serial, checksum);
    }
}
