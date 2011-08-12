# JShot

## TODO

- use [launch4j](http://launch4j.sourceforge.net/) for packaging
- integrate launch4j with buildr
- or use [izpack](http://izpack.org/) for packaging


## Description

A java (SWT) based screenshot program.

JShot allows you to select the part of the screen to
take the screenshot from.

## Build

TODO: add a platform dependend build script

## Usage

- Launch jshot as explained in the section `Commandline`.
- Press the left mouse button to start the selection.
- Move the mouse with the left mouse button pressed to make the selection.
- Press `Enter` to make the screenshot or `Esc` to abort.

### Filename

- the default filename is `screenshot.png`
- an alternative filename can be given as first commandline parameter

### Commandline

To launch the programm you can use the following commandline:

<pre>
java -jar /tmp/jshot.jar
</pre>

### Examples

#### Mail a screenshot

<pre>
thunderbird $(java -jar /tmp/jshot.jar /tmp/myscreenshot.ng)
</pre>



