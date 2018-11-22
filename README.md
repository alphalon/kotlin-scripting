# Kotlin Scripting

This project consists of two parts: a shell script for running scripts written in Kotlin and a Kotlin library providing a DSL for common scripting functionality.

The Kotlin scripts are compiled and executed by the kscript command line tool whose project is located on [Github](https://github.com/holgerbrandl/kscript). See the kscript project for details on writing scripts in either `.kt` or '.kts`' files and specifying things like dependencies on other libraries including this one.

## The ko script

The primary purpose of this wrapper script is to locate the Kotlin script to execute. The first argument specifies the command to execute which is matched against the Kotlin script filenames from multiple directories through a search path to well-known locations within a repository or project.



## The Kotlin Scripting library

## Requirements

- Java 8
- Kotlin 1.3
- Maven 3.6

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
