#!/usr/bin/env bash
#
# Project: Kotlin Scripting
# Created: Dec 3, 2018
#
# KO - Kotlin Scripting REPL
#
# Copyright (c) 2018 Alphalon, LLC. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Run in project root only
if [[ ! -x ./gradlew || ! -x ko.sh ]]; then
  echo "This script must be run in the project root"
  exit 1
fi

# Make sure project is up to date
source scripts/ko-framework-version.sh
JAR="build/libs/scripting-$KO_VERSION.jar"
CP=$(./gradlew -q classpath 2>/dev/null | sed -n -e 's/^RUNTIME CLASSPATH: //p')

# Make sure the project was assembled
if [[ -z $CP || ! -e $JAR ]]; then
  echo "Could not find project jar file in build/libs directory"
  exit 1
fi

# Auto import the scripting library
if [[ ! -z $TMUX ]]; then
  tmux send-keys ":load repl.kts" Enter
fi

# Start the Kotlin REPL
kotlinc -classpath "$JAR:$CP" -jvm-target 1.8
