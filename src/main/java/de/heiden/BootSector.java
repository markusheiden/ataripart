package de.heiden;

import static de.heiden.IntUtils.*;

/**
 * Boot sector (BIOS parameter block) info.
 */
public class BootSector
{
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
   * @param bytesPerSector Bytes per sector, standard is 512
   * @param sectorsPerCluster Sectors per cluster, standard is 2
   * @param fileSystem Detected file system
   * @param type Specified file system type, optional for FAT12
   * @param label Partition label, optional for FAT12
   * @param serial Serial number (UID) of partition
   * @param checksum Checksum
   */
  public BootSector(int bytesPerSector, int sectorsPerCluster, FileSystem fileSystem, String type, String label, long serial, int checksum)
  {
    this.bytesPerSector = bytesPerSector;
    this.sectorsPerCluster = sectorsPerCluster;
    this.fileSystem = fileSystem;
    this.type = type != null? type.trim() : null;
    this.label = label != null? label.trim() : null;
    this.serial = serial;
    this.checksum = checksum;
  }

  /**
   * Bytes per sector, standard is 512.
   */
  public int getBytesPerSector()
  {
    return bytesPerSector;
  }

  /**
   * Sectors per cluster, standard is 2.
   */
  public int getSectorsPerCluster()
  {
    return sectorsPerCluster;
  }

  /**
   * Detected file system.
   */
  public FileSystem getFileSystem()
  {
    return fileSystem;
  }

  /**
   * Specified file system type.
   */
  public String getType()
  {
    return type;
  }

  /**
   * Partition label.
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * Serial number (UID) of partition.
   */
  public long getSerial()
  {
    return serial;
  }

  /**
   * Checksum.
   */
  public int getChecksum()
  {
    return checksum;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(256);
    result.append("Boot Sector\n");
    result.append("Sector  : ").append(getBytesPerSector()).append("\n");
    result.append("Cluster : ").append(getSectorsPerCluster()).append("\n");
    switch (getFileSystem())
    {
      case FAT12:
        result.append("Detected: FAT12/unknown\n");
        break;
      case FAT16:
      case FAT32:
        result.append("Detected: ").append(getFileSystem()).append("\n");
        result.append("FS type : ").append(getType()).append("\n");
        result.append("Label   : ").append(getLabel()).append("\n");
        result.append("Serial  : ").append(hex(getSerial(), 8)).append("\n");
        break;
    }
    result.append("Checksum: ").append(hex(getChecksum(), 4)).append("\n");

    return result.toString();
  }

  //
  // Parsing
  //

  /**
   * Parse a BIOS parameter block..
   *
   * @param disk Hard disk image part
   * @param index Index of BIOS parameter block in hard disk image part
   */
  public static BootSector parse(byte[] disk, int index)
  {
    int bytesPerSector = getInt16BigEndian(disk, index + 11);
    int sectorsPerCluster = getInt8(disk, index + 13);
    int checksum = checksumInt16LittleEndian(disk, index, 512);

    FileSystem fileSystem = FileSystem.FAT12;
    String type = null;
    String label = null;
    long serial = 0;
    if (disk[index + 38] == 0x29)
    {
      fileSystem = FileSystem.FAT16;
      type = new String(disk, index + 54, 8);
      label = new String(disk, index + 43, 11);
      serial = getInt32BigEndian(disk, index + 39);
    }
    else if (disk[index + 66] == 0x29)
    {
      fileSystem = FileSystem.FAT32;
      type = new String(disk, index + 82, 11);
      label = new String(disk, index + 71, 11);
      serial = getInt32BigEndian(disk, index + 67);
    }

//    System.out.println(hexDump(disk, index, 512));

    return new BootSector(bytesPerSector, sectorsPerCluster, fileSystem, type, label, serial, checksum);
  }
}
