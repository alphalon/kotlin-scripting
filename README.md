# Kotlin Scripting

This project consists of two parts: a shell script for running scripts written in Kotlin and a Kotlin library providing a DSL for common scripting functionality.

The Kotlin scripts are compiled and executed by the kscript command line tool whose project is located on [Github](https://github.com/holgerbrandl/kscript). See the kscript project for details on writing scripts in either `.kt` or '.kts`' files and specifying things like dependencies on other libraries including this one.

## Scripting Framework

The primary purpose of this wrapper script is to locate the Kotlin script to execute. The first argument specifies the command to execute which is matched against the Kotlin script filenames from multiple directories through a search path to well-known locations within a repository or project.

### Configuration

Several environment variables can be set to modify the behavior of the run script:

- `KO_ADDITIONAL_SEARCH_ROOTS` - color-separated list of search root directories
- `KO_ADDITIONAL_SEARCH_DIRS` - colon-separated list of subdirectories below search roots to find scripts
- `KO_ADDITIONAL_REPO_MARKERS` - additional markers used to locate the repository directory
- `KO_ADDITIONAL_PROJECT_MARKERS` - additional markers used to locate project directories
- `KO_ADDITIONAL_MODULE_MARKERS` - additional markers used to local modules within a project

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

## Requirements

- Java 8
- Gradle 4.10 - for building this project
- Kotlin 1.3 - for compiling this project and scripts
- Maven 3.6 - required by kscript
- kscript 1.2

## Installation

Installation is as simple as copying the ko script and scripting directory to a location on your shell PATH.

### Installing kscript

If you have Homebrew installed, ko will install kscript for you automatically the first time it is run. Otherwise, execute:

```bash
brew install holgerbrandl/tap/kscript
``` 

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
