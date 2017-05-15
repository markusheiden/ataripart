# AtariPart

Command line tool to extract partitions and files from Atari disk images.

## Prerequisites

* Java
* [mcopy from mtools](https://www.gnu.org/software/mtools/manual/mtools.html#mcopy)
* Maven (just for building)

## Usage

```
java -jar ataripart.jar [options] [command] [command options]
  Options:
    --help
      Display help
  Commands:
    analyze: Search a whole disk image for root sectors
      Usage: analyze [Hard disk image]

    list: List all root sectors and their partitions, starting with the mbr 
      Usage: list [options] [Hard disk image]
        Options:
          -b
            Display backup root sectors, if any

    extract: Extract all partitions and their contents to a directory. 
            Needs dd and mtools installed.
      Usage: extract [Hard disk image] [Directory to copy partition contents to] 
```

## Building

```
mvn clean install
```

This will create the `ataripart.jar`in the `target` directory.
