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
   * Checksum.
   */
  private final int checksum;

  /**
   * Constructor.
   *
   * @param bytesPerSector Bytes per sector, standard is 512
   * @param sectorsPerCluster Sectors per cluster, standard is 2
   * @param checksum Checksum
   */
  public BootSector(int bytesPerSector, int sectorsPerCluster, int checksum)
  {
    this.bytesPerSector = bytesPerSector;
    this.sectorsPerCluster = sectorsPerCluster;
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
    result.append("Bios Parameter\n");
    result.append("Sector  : ").append(getBytesPerSector()).append("\n");
    result.append("Cluster : ").append(getSectorsPerCluster()).append("\n");
    result.append("Checksum: $").append(hexPlain(getChecksum(), 4)).append("\n");

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

    int checksum = 0;
    for (int i = 0; i < 512; i += 2)
    {
      checksum += getInt16LittleEndian(disk, index + i);
    }

    return new BootSector(bytesPerSector, sectorsPerCluster, checksum & 0xFFFF);
  }
}
