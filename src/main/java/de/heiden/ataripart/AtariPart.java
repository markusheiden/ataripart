package de.heiden.ataripart;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.heiden.ataripart.commands.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

/**
 * Atari partition analyzer.
 */
public class AtariPart {
    /**
     * File with hard disk image.
     */
    private final File file;

    /**
     * File with hard disk image.
     */
    private final RandomAccessFile image;

    /**
     * Start this tool.
     *
     * @param args args[0] has to hold the hard disk image file
     */
    public static void main(String[] args) {
        JCommander commander = new JCommander();
        commander.setProgramName(AtariPart.class.getSimpleName());
        HelpOption help = new HelpOption();
        commander.addObject(help);
        AnalyzeCommand analyze = new AnalyzeCommand();
        commander.addCommand("analyze", analyze);
        ListCommand list = new ListCommand();
        commander.addCommand("list", list);
        PartitionsCommand partitions = new PartitionsCommand();
        commander.addCommand("partitions", partitions);
        FilesCommand files = new FilesCommand();
        commander.addCommand("files", files);

        try {
            commander.parse(args);

            if (help.isHelp()) {
                help.help(commander);
                return;
            }

            switch (commander.getParsedCommand()) {
                case "analyze":
                    analyze.analyze();
                    return;
                case "list":
                    list.list();
                    return;
                case "partitions":
                    partitions.extract();
                    return;
                case "files":
                    files.extract();
                    return;
            }
        } catch (ParameterException | NullPointerException e) {
            System.err.println(e.getLocalizedMessage());
            help.help(commander);
            System.exit(-1);
            return;
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
            return;
        }
    }

    /**
     * Display all detected valid partitions.
     *
     * @param backup Display backup root sectors?
     */
    public void list(boolean backup) throws IOException {
        List<RootSector> rootSectors = readRootSectors();
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
    public long displayPartitions(List<RootSector> rootSectors) {
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

    /**
     * Scan disk image for root sectors.
     * Does NOT evaluate partition information to follow xgm partitions.
     */
    public void analyze() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024 * 1024);

        long diskOffset = 0;
        for (int num; (num = readFromImage(diskOffset, buffer)) >= 0;) {
            for (int bufferOffset = 0; bufferOffset + 512 <= num; bufferOffset += 512, diskOffset += 512) {
                buffer.position(bufferOffset);
                RootSector rootSector = RootSector.parse(diskOffset, diskOffset, buffer);
                if (rootSector.hasValidPartitions()) {
                    out.print(diskOffset + bufferOffset);
                    out.print(": Possible ");
                    out.println(rootSector.toString());

                    for (Partition partition : rootSector.getPartitions()) {
                        out.println(partition.toString());
                    }
                }
            }
            buffer.clear();
        }
    }

    //
    //
    //

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
     * @param xgmOffset Absolute offset of the (first) xgm root sector
     * @param offset Absolute offset to read the root sector from
     * @param result Resulting list with all root sectors
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
    private RootSector readRootSector(long xgmOffset, long offset, long diskOffset) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read root sector with partitions.
        readFromImage(diskOffset, buffer);
        RootSector result = RootSector.parse(xgmOffset, offset, buffer);

        // Read BIOS parameter blocks for real partitions.
        for (Partition partition : result.getRealPartitions()) {
            if (partition.getAbsoluteStart() + 512 <= image.length()) {
                readFromImage(partition.getAbsoluteStart(), buffer);
                partition.setBootSector(BootSector.parse(buffer));
            }
        }

        return result;
    }

    /**
     * Read from image at the given position to the buffer.
     * Sets buffer position to 0.
     *
     * @param position Absolute position in hard disk image.
     * @param buffer Buffer to read to.
     * @return Number of bytes read.
     */
    private int readFromImage(long position, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int num = image.getChannel().position(position).read(buffer);
        buffer.position(0);
        return num;
    }

    //
    //
    //

    /**
     * Constructor.
     *
     * @param file The file with the hard disk image
     */
    public AtariPart(File file) throws IOException {
        this.file = file;
        this.image = new RandomAccessFile(file.getCanonicalFile(), "r");
    }

    /**
     * File with the hard disk image.
     */
    public File getFile() {
        return file;
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
     * This will release the underlying hard disk image file.
     */
    public void close() throws IOException {
        image.close();
    }
}
