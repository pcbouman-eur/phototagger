# Phototagger and Copier

## Introduction

The idea of this tool is that you can process large folders of photo's (i.e. after shooting a lot of pictures during a vacation) efficiently. 

### Phase 1 : Define tags

In the first phase, you define tags that will be mapped to the keys 1-9 on your keyboard. These tags will be later used as folder names, so it is advisably not too use any weird characters in there. Furthermore, you need to select a folder to process. When this is done, you can start tagging the photo's. If you already tagged the folder previously, it will continue where you left, rather than start at the beginning. If you want to restart, you should delete the `state.json` file from the folder containing the photo files.

### Phase 2 : Tagging photo's

During the tagging phase, you will see the photo's full screen. You can go to the next photo with the <kbd>&rarr;</kbd> and go to the previous photo with <kbd>&larr;</kbd>. You can tag a photo with keys <kbd>1</kbd>, <kbd>2</kbd>, &dots; <kbd>9</kbd>. Press one of those keys will toggle the tag: if the photo currently holds the tag, it is removed, and if it does not hold the tag, it is added. Note that a photo is allowed to have any number of tags, zero, one or more. With the <kbd>Esc</kbd> key you can stop tagging the photo's and this will take you to phase 3. Every action will be committed to a file `state.json` that is saved in the folder containing the pictures. This includes  so in principle, work will not be lost during this process.

### Phase 3 : Copying photo's

In the final phase, you can select a destination folder. If you start the copying process, a sub-folder will be created for every tag, and the tagged photo's will be copied to the folders accordingly. After the copying is done, the photo's will be thus be split out in these destination folders, ready for archiving, uploading to social media, distribution, etc.

## Using the software

You need a Java Runtime Environment (version 8 or later) that supports Java Swing to use this application. Under releases a version of the software can be obtained that should work using the Java Runtime Environment. Since Oracle has added very restrictive licensing to their Java binaries, it is probably advisable to switch to [AdoptOpenJDK](https://adoptopenjdk.net/) for your free Java needs.

## Building the software

You can build the software using [Maven](https://maven.apache.org/). It currently has three dependencies: [Thumbnailator](https://github.com/coobird/thumbnailator) to process and resize the images before they are shown on screen, [Jackson Databind](https://github.com/FasterXML/jackson-databind) to save the current state of the photo-tagging process to a `json` file, and finally [Commons IO](https://commons.apache.org/proper/commons-io/) to allow for easy copying of the files after the tagging is done. A runnable jar file containing all required dependencies can be build using Maven, with the `mvn package` command in the root of the project. This should generate a fat jar in the `target` output folder.
