package de.heiden.ataripart.image;

/**
 * Known file system types.
 */
public enum FileSystem {
    FAT12(0x01),
    FAT16(0x0E),
    FAT32(0x0C);

    /**
     * File system type.
     */
    private final int type;

    /**
     * Constructor.
     *
     * @param type File system type.
     */
    FileSystem(int type) {
        this.type = type;
    }

    /**
     * File system type.
     */
    public int getType() {
        return type;
    }
}
