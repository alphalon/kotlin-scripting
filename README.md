# Kotlin Scripting

This project consists of a framework and a library, which can be used together or independently of each other. The framework provides support for running your scripts from the command line while the library provides APIs called from within your Kotlin scripts.

This Kotlin Scripting project builds on the excellent kscript command line tool hosted on [Github](https://github.com/holgerbrandl/kscript). See the kscript project for details on writing Kotlin scripts with just some of these advantages:

- Maven-style dependency management
- Cached compilation of scripts for faster subsequent execution
- File includes for sharing non-deployed code among scripts
- Specification of JVM command line arguments

This project has been designed to make using kscript easier without limiting the power or flexibility it provides. For example, scripts can still be run directly from the command lime (using shebang syntax) and still utilize this scripting library.

## Scripting Framework

The primary purpose of this wrapper script is to locate the Kotlin script to execute. The first argument specifies the command to execute which is matched against the Kotlin script filenames from multiple directories through a search path to well-known locations within a repository, project, or home directory.

This framework also makes it easy to document and discover the commands that are available for a particular project or context.

This document assumes the existence of a symlink or alias, named `ko`, references this project's `ko.sh` shell script. See Installation for details.

### Features

- Locates and executes an appropriate script for a given command
- Documents available commands based on the current directory
- Provides a shortcut to create new or edit existing scripts
- Runs specific scripts in a designated directory (i.e., the project directory)

### Concepts

- A _search root_ is a directory identified to possibly "contain" scripts. Examples includes the repository or project root directories, or the user home directory
- A _search directory_ is a subdirectory from a search root that actually contains the scripts to be executed
- The _search path_ is a list of directories that are searched in order to match a given command to a script

The search path is determined by constructing search roots based on the current working directory and your project and/or repository structure every time you execute a command using the `ko` script.

### Usage

Running `ko` without any arguments will print usage information with more options, but here are the highlights:

#### Executing a script

```bash
ko <command> [args...]
``` 

The command is used to identify which script should be called based on the search path. Only the beginning part of the script name needs to be specified, however it much be unambiguous.

#### Listing available commands

```bash
ko help
```

Lists the available commands based on the working directory. If scripts have been documented with a `//CMD <command> <description>` comment, the description will be displayed alongside the command.

#### Creating a new script

```bash
ko -c <command>
```

A new script will be created with the case-sensitive name <command>.kts, so it's important to provide the full command name. The directory where the script is created will be the first directory (excluding the current directory) of the search path. If the search path is empty, the current directory will be used.

The new script will be opened in your editor automatically where you can quickly delete the parts of the template that you don't need or want.

#### Edit an existing script

```bash
ko -e <command>
```

This uses the same search resolution as executing a script (partial command matching applies) and opens it using the command line editor specified by either of the `VISUAL` or `EDITOR` environment variables.

Alternatively, `ko -i <command>` leverages kscript to create a temporary project and edit the file using IntelliJ IDEA.

### Configuration

Several environment variables can be set to modify the behavior of the run script:

- `KO_ADDITIONAL_SEARCH_ROOTS` - color-separated list of search root directories
- `KO_ADDITIONAL_SEARCH_DIRS` - colon-separated list of subdirectories below search roots to find scripts
- `KO_ADDITIONAL_REPO_MARKERS` - additional markers used to locate the repository directory
- `KO_ADDITIONAL_PROJECT_MARKERS` - additional markers used to locate project directories
- `KO_ADDITIONAL_MODULE_MARKERS` - additional markers used to local modules within a project

#### Special marker files

The presence of the `.ko_repo`, `.ko_project`, and `.ko_module` files not only indicate their respective directories when determining the search roots, but can contain bash export commands to further configure the resolution process or provide additional environment variables for your running Kotlin scripts.

### Runtime environment

These environment variables are available to your Kotlin script when called via the `ko` script:

- `KO_HOME` - the directory of the `ko` script
- `KO_VERSION` - the version of the `ko` script
- `KO_SCRIPT` - the filename of the script being executed
- `KO_SCRIPT_DIR` - the directory of the script file being executed
- `KO_SEARCH_PATH` - the list of files and directories used to search for scripts
- `KO_REPO` - the top-level directory of the source code repository, optional
- `KO_PROJECT` - the top-level directory of current project, optional
- `KO_MODULE` - the nearest directory representing a module, optional
- `KO_PROJECT_FILE` - the primary build file used for the project

## Scripting Library

NOTE: The scripting library has not yet been developed, so the functionality is severely limited at this early stage.

Adding a dependency on the scripting library can be performed by adding the following to your script file:

```kotlin
@file:DependsOn("io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT")
```

See the [kscript](https://github.com/holgerbrandl/kscript) project for more details.

## Requirements

- Java 8
- Kotlin 1.3
- Maven 3.6
- kscript 2.6

(It looks like an upcoming version of kscript will not require Maven.)

## Installation

Installation is performed by cloning this repository and executing the `./install.sh` script. If you have Homebrew installed, any missing dependencies will also be installed (except Java).

If the install script detects a $HOME/bin directory, it will create a symlink `ko` to the `ko.sh` file in this repository, assuming this will be added to your execution path.

Alternatively, you can create an alias to this script, or copy the `ko.sh` file and `scripts` directory to any location on your executable path.

The `./install.sh` script also builds the scripting library and installs it in your local maven repository for resolution by kscript. This script can be run whenever you make changes to the library sources so that they become available to your scripts. 

## TODO

- Develop the library with useful stuff
- Publish the library to Maven Central
- Publish the framework to Homebrew

And lots of other things... feedback and contributions are welcome!

## License

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
