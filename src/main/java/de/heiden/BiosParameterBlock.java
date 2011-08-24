package de.heiden;

import static de.heiden.IntUtils.getInt16BigEndian;
import static de.heiden.IntUtils.getInt8;

/**
 * BIOS parameter block info.
 */
public class BiosParameterBlock
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
   * Constructor.
   *
   * @param bytesPerSector Bytes per sector, standard is 512
   * @param sectorsPerCluster Sectors per cluster, standard is 2
   */
  public BiosParameterBlock(int bytesPerSector, int sectorsPerCluster)
  {
    this.bytesPerSector = bytesPerSector;
    this.sectorsPerCluster = sectorsPerCluster;
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

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(256);
    result.append("Bios Parameter\n");
    result.append("Sector  : ").append(getBytesPerSector()).append("\n");
    result.append("Cluster : ").append(getSectorsPerCluster()).append("\n");

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
  public static BiosParameterBlock parse(byte[] disk, int index)
  {
    int bytesPerSector = getInt16BigEndian(disk, index + 11);
    int sectorsPerCluster = getInt8(disk, index + 13);

    return new BiosParameterBlock(bytesPerSector, sectorsPerCluster);
  }
}
