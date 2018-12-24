#!/usr/bin/env bash
#
# Builds and installs Kotlin Scripting for use on the local machine
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

# Make sure we run this in the correct directory
if [[ ! -f "ko.sh" ]]; then
  echo "This script must be run in the Kotlin Scripting root project directory."
  exit 1
fi

# Checks whether a command is present on the path, installs otherwise
# Args: cmd name package command
function install-package {
  if ! hash $4 2>/dev/null; then
    echo "Installing $2"
    if ! $1 install $3; then
      echo "Error installing $2"
      exit 2
    fi
  fi
}

# Install dependencies using Homebrew (https://brew.sh)
if hash brew 2>/dev/null; then
  # echo "Installing dependencies with Homebrew"
  install-package brew Maven maven mvn
  install-package brew Kotlin kotlin kotlinc
  install-package brew kscript holgerbrandl/tap/kscript kscript
fi

# Install dependencies using SDKMAN! (https://sdkman.io)
if hash sdk 2>/dev/null; then
  # echo "Installing dependencies with SDKMAN!"
  install-package sdk Java java java
  install-package sdk Maven maven mvn
  install-package sdk Kotlin kotlin kotlinc
  install-package sdk kscript kscript kscript
fi

# Check that dependencies are installed
if ! hash java 2>/dev/null; then
  echo "ERROR: Java is not installed"
  ERROR=1
fi
if ! hash mvn 2>/dev/null; then
  echo "ERROR: Maven is not installed"
  ERROR=1
fi
if ! hash kotlinc 2>/dev/null; then
  echo "ERROR: Kotlin is not installed"
  ERROR=1
fi
if ! hash kscript 2>/dev/null; then
  echo "ERROR: kscript is not installed"
  ERROR=1
fi
if [[ $ERROR -gt 0 ]]; then
  exit $ERROR
fi

# Create symlink to `ko` script in user bin directory
if [[ -d "$HOME/bin" ]]; then
  if [[ ! -e "$HOME/bin/ko" && ! -L "$HOME/bin/ko" ]]; then
    ln -s "$(realpath ko.sh)" "$HOME/bin/ko"
  fi
fi

# Configure bash completions
if [[ -d "/usr/local/etc/bash_completion.d" ]]; then
   if [[ ! -e "/usr/local/etc/bash_completion.d/ko-completion.sh" ]]; then
     ln -s "$(realpath ko-completion.sh)" "/usr/local/etc/bash_completion.d/ko-completion.sh"
   fi
fi

# Build project library and install in local maven repository
./gradlew publishToMavenLocal

# Save version number
version=$(./gradlew properties -q 2>/dev/null | grep "version:" | awk '{print $2}' | tr -d '[:space:]')
echo -e "#!/usr/bin/env bash\nKO_VERSION=\"$version\"" >scripts/ko-framework-version.sh
