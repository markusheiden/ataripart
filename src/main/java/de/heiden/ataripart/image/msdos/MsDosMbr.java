package de.heiden.ataripart.image.msdos;

import de.heiden.ataripart.image.IntUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * MS DOS MBR.
 */
public class MsDosMbr {
    /**
     * Partitions.
     */
    private final List<MsDosPartition> partitions;

    /**
     * Constructor.
     *
     * @param partitions Partitions.
     */
    public MsDosMbr(MsDosPartition... partitions) {
        if (partitions.length > 4) {
            throw new IllegalArgumentException("MBR just supports 4 partitions.");
        }
        this.partitions = unmodifiableList(asList(partitions));
    }

    /**
     * Partitions.
     */
    public List<MsDosPartition> getPartitions() {
        return partitions;
    }

    /**
     * Create MBR.
     */
    public ByteBuffer createMbr() {
        ByteBuffer mbr = ByteBuffer.allocate(512);
        mbr.order(ByteOrder.LITTLE_ENDIAN);

        int index = 0x01BE;
        for (MsDosPartition partition : partitions) {
            ByteBuffer partitionEntry = partition.createPartitionEntry();
            mbr.position(index);
            mbr.put(partitionEntry);
            index += 0x10;
        }
        IntUtils.setInt8(mbr, 0x01FE, 0x55);
        IntUtils.setInt8(mbr, 0x01FF, 0xAA);

        mbr.position(0);
        return mbr;
    }
}
