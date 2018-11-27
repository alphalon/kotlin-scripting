# KO - Kotlin Shell Scripting

This project consists of a framework and a library, which can be used together or independently of each other. The framework provides support for running your scripts from the command line while the library provides APIs for your Kotlin scripts.

This project builds on the excellent [kscript command line tool](https://github.com/holgerbrandl/kscript). See that project for details on writing Kotlin scripts with just some of these advantages:

- Maven-style dependency management
- Automatic compilation of scripts
- File includes for sharing non-deployed code among scripts
- Specification of JVM command line arguments

This project has been designed to make using kscript easier without limiting the features or flexibility it provides. For example, scripts can still be run directly from the command line while utilizing (most of) this scripting library.

## Scripting Framework

The primary purpose of this framework is to provide a wrapper script, named `ko.sh`, that locates and executes Kotlin scripts. The first argument, which specifies the command or script to execute, is matched against Kotlin scripts through a search path created from multiple directories to well-known locations within a repository, project, or home directory.

This framework also makes it easy to document and discover the commands that are available for a particular context which is determined by the current directory.

This document assumes the existence of a symlink or alias, named `ko`, that references this project's `ko.sh` shell script, and this will be used throughout this document.

### Features

- Locates and executes an appropriate script for a given command
- Documents available commands based on the current directory (context)
- Provides a shortcut to create new or edit existing scripts
- Runs specific scripts in a designated directory (i.e., the project directory)
- A meta script, `ko.kts`, supports implementing several commands in one file

### Concepts

- A _search root_ is a directory identified to provide a context for running scripts. Examples include the repository or project root directories, or even the user's home directory
- A _search directory_ is a subdirectory within a search root that contains the scripts to execute
- The _search path_ is a list of directories that are searched in order to match a given command to a script

The search path is determined by constructing search roots based on the current working directory and your project and/or repository structure every time you execute a command using the `ko` script.

The default search roots include the module directory, project directory, repository directory, and home directory, searched in that order. The search dirs within these directories default to `bin` and `scripts`.

In addition, the search path starts with the current directory and ends with the `scripts` located alongside the `ko` script.

### Getting Started

Step 1: Follow the instructions in the [Installation](#installation) section.

Step 2: Create a `scripts` subdirectory in your project's root directory.

Step 3: Create your first script and add `println("Hello World")` to the end:

```bash
ko -c MyFirstScript
```

The new script will be created from a template and placed in your `scripts` directory automatically. It will also be opened in your preferred editor so you can get started writing.

Step 4: Run your script from any directory within your project:

```bash
ko MyFirstScript
```

### Usage

Running `ko` without any arguments or with the `--help` option will print usage information with more details, but here are the highlights:

#### Executing a script

```bash
ko <command> [args...]
``` 

The command is used to identify which `.kts` script should be called based on the search path. Only the beginning part of the script name needs to be specified, however it much be unambiguous in order for the script to be executed.

What you enter for the command is matched against the beginning of all available commands (case-insensitive).

Successful selection (for execution, editing, etc) requires one of the following:

- A single match
- An exact match (shadows other matches)
- Multiple partial matches to identical commands

In the case of successfully matching against multiple scripts (or implementations of the same command), the first one on the search path is chosen.

If multiple possible matches are found, no processing will be performed but the matching commands are displayed to enable the user to disambiguate between them easily. (There is a non-zero exit code to detect this situation from other shell scripts.)

#### Listing available commands

```bash
ko help
```

Lists the available commands that can be executed from the current directory. If scripts have been documented with a `//CMD <command> <description>` comment, its description will be displayed alongside the command name.

Run the `ko help --help` command for information about additional options for the help command.

```bash
ko help <command>
```

Detailed information will be output to the console when a command is matched against a script. Scripts that support the `--help` argument will output additional information.

You can also list the commands defined in the current project:

```bash
ko help -p
```

#### Creating a new script

```bash
ko -c <command>
```

A new script will be created with the case-sensitive name `<command>.kts`, so it's important to provide the full command name. The directory where the script is created will be the first directory (excluding the current directory) of the search path. If the search path is empty, the current directory will be used.

The new script will be opened in your editor automatically where you can quickly delete the parts of the template that you don't need or want.

#### Edit an existing script

```bash
ko -e <command>
```

This uses the same search resolution as executing a script (partial command matching applies) and opens the discovered script using the editor specified by either of the `VISUAL` or `EDITOR` environment variables.

Alternatively, `ko --idea <command>` leverages kscript to create a temporary project and edit the file using IntelliJ IDEA.

#### Upgrading dependencies

Since each script is a standalone program, it must declare its own dependencies. To ease the pain of keeping versions the same across scripts, this framework includes a command to upgrade your Kotlin scripts:

```bash
ko upgradeDependency [--repo|--project|--module] [version [groupId:artifactId]]
```

Without any arguments, this command will upgrade your nearby scripts to the latest version of this Scripting Library. You can change the scope of the updates using the --repo, --project, or --module options, specify a specific version, and/or upgrade dependencies to other libraries.

Note: The kscript `DependsOn` annotation is supported, but each one must reside on a single line and contain only string literals.

### Script conventions

The framework defines several special comments that can be placed in your Kotlin scripts:

```kotlin
//DIR <dir-spec>
```

The DIR comment specifies in which directory the script should be executed. If not present, scripts will be executed in the working directory.

The _dir-spec_ supports an absolute path, a path relative to the home directory using the tilde notation, and variable substitution to an absolute path using the dollar sign ($).

For example, to always a particular script run in the project directory, add the `//DIR $KO_PROJECT` comment to your file.

Note: This only applies when executing the script with the `ko` script.

```kotlin
//CMD <command> <multi-word description>
```

The `//CMD` comment is used for documenting the available commands. If not present, the command name is taken from the script name and no description will be shown when the commands are listed.

This comment is required for command resolution in `ko.kts` scripts, which support implementing multiple commands in a single file. For these scripts, the command is passed as the first argument. These special scripts can also be located in search root directories so creating a `scripts` subdirectory in your project is not necessary.

The presence of a `//HELP` comment indicates the script supports being called with the `--help` argument to output usage information to the console. 

#### Naming

While not strictly required, it is recommended to name script files using upper camel-case and commands with lower camel-case. Naming commands consistently will not only reduce memory fatigue but also allow for better matching when identical command names are shadowed.

#### Script files

While kscript supports executing `.kt` files, this framework only calls scripts with the `.kts` extension. This allows for a clean separation of files intended to be invoked directly and those that implement shared functionality and can be included in other scripts using a comment directive:

```kotlin
//INCLUDE util.kt
```

### Configuration

Several environment variables can be set to configure the behavior of the run script:

- `KO_ADDITIONAL_SEARCH_ROOTS` - list of search root directories
- `KO_ADDITIONAL_SEARCH_DIRS` - list of subdirectories below search roots to find scripts
- `KO_ADDITIONAL_REPO_MARKERS` - additional markers used to locate the repository directory
- `KO_ADDITIONAL_PROJECT_MARKERS` - additional markers used to locate project directories
- `KO_ADDITIONAL_MODULE_MARKERS` - additional markers used to local modules within a project

All of these settings are multi-valued and use colons (:) as the delimiter.

#### Special marker files

The presence of the `.ko_repo`, `.ko_project`, and `.ko_module` files not only indicate their respective directories when determining the search roots, but can contain bash export commands to further configure the resolution process or provide additional environment variables for your running Kotlin scripts.

### Runtime environment

These environment variables are available to your Kotlin script when called via the `ko` script:

- `KO_HOME` - the directory of the `ko` script
- `KO_VERSION` - the version of the `ko` script
- `KO_COMMAND` - the resolved command name
- `KO_SCRIPT` - the filename of the script being executed
- `KO_SCRIPT_DIR` - the directory of the script file being executed
- `KO_SEARCH_PATH` - the list of files and directories used to search for scripts
- `KO_REPO` - the top-level directory of the source code repository, optional
- `KO_PROJECT` - the top-level directory of current project, optional
- `KO_MODULE` - the nearest directory representing a module, optional
- `KO_PROJECT_FILE` - the primary build file used for the project

The `Frameowrk` class in the scripting library provides convenient access to the runtime environment setup by the framework.

## Scripting Library

NOTE: The scripting library has not yet been developed, so the functionality is limited at this early stage.

Adding a dependency on the scripting library can be performed by adding the following preamble to your script file (using the appropriate version, of course):

```kotlin
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.0
```

See the [kscript](https://github.com/holgerbrandl/kscript) project for more details.

### Pre-release

The pre-1.0 releases may be subject to API changes. You have been warned!

It is likely this library will grow organically while a style for writing Kotlin scripts is being established and existing shell scripts are converted over.

It is intended for this library to include support for the most commonly used features, such as:

- Command line argument parsing (geared toward self documentation)
- Kotlin-idiomatic way for calling other programs or build scripts
- Parsing and manipulation of JSON and XML documents
- Asynchronous HTTP client (based on ktor)
- Equivalents for tools such as find, grep, sed, awk, etc

Another goal of this library is to help reduce the number of dependencies and imports each script needs to maintain, lowering the overall ceremony involved when writing scripts.

## Requirements

- Java 8
- Kotlin 1.3
- Maven 3.6
- kscript 2.6

(It looks like an upcoming version of kscript will no longer require Maven.)

## Installation

Installation is performed by cloning this repository and executing the `./install.sh` script. If you have Homebrew installed, any missing dependencies will also be installed (except Java). Development is done on the master branch, so you may want to checkout a released version.

```bash
git clone https://github.com/alphalon/kotlin-scripting
git checkout tags/v0.1.0
./install.sh
```

If the install script detects a $HOME/bin directory, it will create a symlink `ko` to the `ko.sh` file in this repository, assuming this will be added to your execution path.

Alternatively, you can create an alias to this script, or copy the `ko.sh` file and `scripts` directory to any location on your executable path.

The `./install.sh` script also builds the scripting library and installs it in your local maven repository for resolution by kscript. This script can be run whenever you make changes to the library sources so that they become available to your scripts through kscript dependency resolution.

## Example Scripts

The built-in commands are implemented as Kotlin scripts and located in this project's `script` directory:

- [Help](scripts/Help.kts) - Provides information about available commands
- [Upgrade Dependency](scripts/UpgradeDependency.kts) - Modifies dependency versions in existing scripts

In addition, the `bin` directory contains automation scripts for this project.

## Diagnostics

Run the `ko` script with the `--verbose` option to output information regarding the script runtime environment.

## TODO

- Develop the library with useful stuff
- Publish the library to Maven Central
- Publish the framework to Homebrew

And lots of other things... feedback and contributions are welcome!

## License

Copyright (c) 2018 Alphalon, LLC. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
