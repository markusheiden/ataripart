# AtariPart

Command line tool to extract partitions and files from Atari disk images.

## Prerequisites

* Java 8+
* [mcopy from mtools](https://www.gnu.org/software/mtools/manual/mtools.html#mcopy)
* Maven 3.5+ (just for building)

## Usage

```
java -jar Usage: AtariPart [options] [command] [command options]
  Options:
    --help
      Display help
  Commands:
    analyze: Search a whole disk image for root sectors
      Usage: analyze [Hard disk image]

    list: List all root sectors and their partitions, starting with the mbr
      Usage: list [options] [Hard disk image]
        Options:
          -b, --backup
            Display backup root sectors, if any
            Default: false

    partitions: Extract all partitions to a directory.
      Usage: partitions [options] [Hard disk image] [Directory to copy partition contents to]
        Options:
          -c, --convert
            Convert boot sectors to MS DOS format
            Default: false

    files: Extract all files from all partitions to a directory. Needs mtools installed.
      Usage: files [Hard disk image] [Directory to copy files to]

```

## Building

```
mvn clean install
```

This will create the `ataripart.jar`in the `target` directory.
