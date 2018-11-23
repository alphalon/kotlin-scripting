#!/usr/bin/env bash
#
# Builds and installs Kotlin Scripting for use on the local machine

# Make sure we run this in the correct directory
if [[ ! -f "ko.sh" ]]; then
  echo "This script must be run in the Kotlin Scripting project directory."
  exit 1
fi

# Checks whether a command is present on the path, installs otherwise
# Arguments: name package command
function install-package {
  if ! hash $3 2>/dev/null; then
    echo "Installing $1"
    if ! brew install $2; then
      echo "Error installing $1"
      exit 2
    fi
  fi
}

# Install dependencies using Homebrew (if available)
if hash brew 2>/dev/null ; then
  # echo "Installing dependencies"
  install-package Maven maven mvn
  install-package Kotlin kotlin kotlinc
  install-package kscript holgerbrandl/tap/kscript kscript
fi

# Check that dependencies are installed
ERROR=
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
if [[ $ERROR != "" ]]; then
  exit $ERROR
fi

# Create symlink to `ko` script in user bin directory
if [[ -d "$HOME/bin" ]]; then
  if [[ ! -e "$HOME/bin/ko" ]] && [[ ! -L "$HOME/bin/ko" ]]; then
    ln -s "$(realpath ko.sh)" "$HOME/bin/ko"
  fi
fi

# Build project library and install in local maven repository
./gradlew publishToMavenLocal

# Save version number
version=$(./gradlew properties -q 2>/dev/null | grep "version:" | awk '{print $2}' | tr -d '[:space:]')
echo "export KO_VERSION=\"$version\"" >version.sh
