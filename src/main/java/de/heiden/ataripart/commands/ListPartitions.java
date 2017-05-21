package de.heiden.ataripart.commands;

import de.heiden.ataripart.Partition;
import de.heiden.ataripart.RootSector;

import java.io.File;
import java.io.IOException;

import static java.lang.System.out;

/**
 * The list command list all root sectors and its partitions, starting with the mbr.
 */
public class ListPartitions extends AbstractCommand {
    /**
     * Display all detected valid partitions.
     *
     * @param backup Display backup root sectors?
     */
    public void list(File file, boolean backup) throws IOException {
        init(file);

        java.util.List<RootSector> rootSectors = readRootSectors();
        if (rootSectors.isEmpty()) {
            out.println("No valid root sectors found");
            return;
        }
        RootSector masterRootSector = rootSectors.get(0);

        long maxOffset = displayPartitions(rootSectors);

        out.println("Disk ends at " + masterRootSector.getSize());

        if (backup) {
            out.println();
            displayFirstBackupRootSector(masterRootSector);
            displayLastBackupRootSector(masterRootSector, maxOffset);
        }
    }

    /**
     * Display all detected valid partitions.
     *
     * @param rootSectors Detected root sectors
     * @return Maximum offset, that is used by any detected partition
     */
    public long displayPartitions(java.util.List<RootSector> rootSectors) {
        char partitionName = 'C';
        long maxOffset = 0;
        for (RootSector rootSector : rootSectors) {
            out.println(rootSector);

            for (Partition partition : rootSector.getAllPartitions()) {
                if (!partition.isValid()) {
                    continue;
                }

                if (partition.isXGM()) {
                    out.println(partition.toString("container"));
                } else {
                    if (partition.getAbsoluteEnd() > maxOffset) {
                        maxOffset = partition.getAbsoluteEnd();
                    }
                    out.println(partition.toString(Character.toString(partitionName++)));
                }
            }
        }
        return maxOffset;
    }

    /**
     * Output first backup root sector only if existing and valid.
     *
     * @param masterRootSector First (master) root sector
     */
    public void displayFirstBackupRootSector(RootSector masterRootSector) throws IOException {
        long offset = masterRootSector.getOffset() + 512;
        if (!masterRootSector.getRealPartitions().isEmpty() && offset < masterRootSector.getRealPartitions().get(0).getAbsoluteStart()) {
            RootSector backupRootSector = readRootSector(0, 0, offset);
            if (backupRootSector.hasValidPartitions()) {
                out.println("First (backup) " + backupRootSector);

                for (Partition backupPartition : backupRootSector.getAllPartitions()) {
                    if (backupPartition.isValid()) {
                        out.println(backupPartition.toString());
                    }
                }
            }
        }
    }

    /**
     * Output last backup root sector only if existing and valid.
     *
     * @param masterRootSector First (master) root sector
     * @param maxOffset Maximum offset that it used by any partition
     */
    public void displayLastBackupRootSector(RootSector masterRootSector, long maxOffset) throws IOException {
        long size = masterRootSector.getSize();
        if (maxOffset < size) {
            RootSector backupRootSector = readRootSector(0, 0, size - 512);
            if (backupRootSector.hasValidPartitions()) {
                out.println("Last (backup) " + backupRootSector);

                for (Partition backupPartition : backupRootSector.getAllPartitions()) {
                    if (backupPartition.isValid()) {
                        out.println(backupPartition.toString());
                    }
                }
            }
        }
    }
}
