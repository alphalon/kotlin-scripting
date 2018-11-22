#!/usr/bin/env bash
#
# Project: Kotlin Scripting
# Created: Nov 12, 2018
#
# Copyright (c) 2018 Alphalon, LLC. All rights reserved.
#
# Kotlin script runner (requires kscript for compilation and execution)
#
# See: https://github.com/holgerbrandl/kscript

# TODO: implement bash completion!
# https://askubuntu.com/questions/68175/how-to-create-script-with-auto-complete
# symlink to /usr/local/etc/bash_completion.d

if [[ -z $1 ]]; then
  echo "usage: ko [options...] <command> [args...]"
  echo "Options: -v or --verbose for verbose output"
  exit 1
fi

VERBOSE=
if [[ $1 == "-v" ]] || [[ $1 == "--verbose" ]]; then
  VERBOSE=1
  shift
fi

# Find the directory containing this script
SOURCE="${BASH_SOURCE[0]}"
# resolve $SOURCE until the file is no longer a symlink
while [[ -h $SOURCE ]]; do
  KO_HOME="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$KO_HOME/$SOURCE"
done
KO_HOME="$(cd -P "$(dirname "$SOURCE")" && pwd)"

# Files or directories marking the repository root directory
REPO_MARKERS=(".git" ".svn")

# Files marking project root directories
PROJECT_MARKERS=("build.gradle" "build.gradle.kts" "pom.xml" "project.clj" "*.xcworkspace" "*.xcodeproj" "package.json" "build.boot" "deps.clj" "build.xml" "Makefile" "CMakeLists.txt" "setup.py" "gradlew" "build.sbt" "Gemfile" ".ko_project")

# Files marking module directories
MODULE_MARKERS=("build.gradle" "build.gradle.kts" "pom.xml" "*.xcodeproj" "package.json" ".ko_module")

# Subdirectories used for searching for scripts along the search roots
SEARCH_PATHS=("bin" "scripts")

# Searches upward for the top-most directory representing a code repository
function find-repository {
  # echo "Searching for repository"
  local dir="$PWD"
  while [[ $dir != $HOME ]]; do
    # echo "Searching for repo in $dir"
    for marker in "${REPO_MARKERS[@]}"; do
      # echo "Checking for marker $marker"
      if [[ -e "$dir/$marker" ]]; then
        export KO_REPO="$dir"
        # echo "Found repository at $KO_REPO"
        return
      fi
    done
    dir=$(dirname "$dir")
  done

  # echo "Did not find a repository"
}

# Searches upward within a repository or user directory for the highest project directory
function find-project {
  # echo "Searching for project"
  local top="${KO_REPO-$HOME}"
  local dir="$PWD"
  while [[ ${#dir} -ge ${#top} ]]; do
    # echo "Searching for project in $dir"
    for marker in "${PROJECT_MARKERS[@]}"; do
      # echo "Checking for marker $marker"
      if compgen -G "$dir/$marker" > /dev/null; then
        export KO_PROJECT="$dir"
        # echo "Found possible project at $KO_PROJECT"
        break
      fi
    done
    dir=$(dirname "$dir")
  done
}

# Searches upward for the nearest module directory
function find-module {
  # echo "Searching for module"
  local top="${KO_PROJECT-$HOME}"
  local dir="$PWD"
  while [[ ${#dir} -ge ${#top} ]]; do
    # echo "Searching for module in ${dir}"
    for marker in "${MODULE_MARKERS[@]}"; do
      # echo "Checking for marker ${marker}"
      if compgen -G "$dir/$marker" > /dev/null; then
        export KO_MODULE="$dir"
        # echo "Found module at ${KO_MODULE}"
        return
      fi
    done
    dir=$(dirname "$dir")
  done

  export KO_MODULE="$KO_PROJECT"
  # echo "Did not find a module, using project"
}

# Joins an array of strings using the first arg as a separator
function join_by { local d=$1; shift; echo -n "$1"; shift; printf "%s" "${@/#/$d}"; }

# Builds an array of directories for searching via relative paths
function make-search-roots {
  # construct array of default roots
  local sr=()
  if [[ ! -z $KO_MODULE ]]; then sr+=("$KO_MODULE"); fi
  if [[ ! -z $KO_PROJECT ]]; then sr+=("$KO_PROJECT"); fi
  if [[ ! -z $KO_REPO ]]; then sr+=("$KO_REPO"); fi
  sr+=("$HOME" "$KO_HOME")
  # remove duplicates
  sr=($(echo ${sr[@]} | tr [:space:] '\n' | awk '!a[$0]++'))
  # construct multiple path string
  KO_SEARCH_ROOTS=$(join_by : "${sr[@]}")
}

# Adds additional search roots
function add-search-roots {
  IFS=':' read -ra sr <<< "$KO_SEARCH_ROOTS"
  IFS=':' read -ra asr <<< "$KO_ADDITIONAL_SEARCH_ROOTS"
  for r in "${asr[@]}"; do
    sr+=(r)
  done
  sr=($(echo ${sr[@]} | tr [:space:] '\n' | awk '!a[$0]++'))
  KO_SEARCH_ROOTS=$(join_by : "${sr[@]}")
}

# Adds search paths to each root and returns a path of directories contains scripts
function make-search-path {
  IFS=':' read -ra sr <<< "$KO_SEARCH_ROOTS"
  local sp=("$PWD")
  for r in "${sr[@]}"; do
    for p in "${SEARCH_PATHS[@]}"; do
      local dir="$r/$p"
      if [[ -d $dir ]]; then
        # echo "Adding $dir to search path"
        sp+=("$r/$p")
      fi
    done
  done
  # remove duplicates
  sp=($(echo ${sp[@]} | tr [:space:] '\n' | awk '!a[$0]++'))
  export KO_SEARCH_PATH=$(join_by : "${sp[@]}")
}

# Adds matches to the list of scripts
function find-script {
  if [[ -d $2 ]]; then
    files=( $(find "$2" \( -iname "$1*.kts" -o -iname "$1*.kt" \) -maxdepth 1 -type f) )
    for f in "${files[@]}"; do
      SCRIPTS+=("$f")
    done
  fi
}

# Checks the contents of the script for a run directory spec
function get-dir-from-script {
  DIR_SPEC=$(sed -n '/^\/\/DIR / s/\/\/DIR //p' "${KO_SCRIPT}")
  if [[ ! -z $DIR_SPEC ]]; then
    # echo "Found directory specification: {DIR_SPEC"
    if [[ $DIR_SPEC == \$* ]]; then
      # echo "Evaluating $DIR_SPEC"
      NAME=${DIR_SPEC:1}
      KO_DIR=${!NAME}
      # echo "Evaluated to $KO_DIR"

      if [[ -z $KO_DIR ]]; then
        echo "Unable to resolve directory specified by environment variable $NAME"
        exit 1
      fi
    elif [[ $DIR_SPEC == ~* ]]; then
      # echo "Evaluating $DIR_SPEC"
      eval KO_DIR=${DIR_SPEC}
      # echo "Evaluated to $KO_DIR"

      if [[ -z $KO_DIR ]]; then
        echo "Unable to resolve directory specified by environment variable $NAME"
        exit 1
      fi
    else
      # Trim leading and trailing whitespace
      KO_DIR=$(echo -e "$DIR_SPEC" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
    fi

    if [[ ! -d $KO_DIR ]]; then
      echo "Specified run directory does not exist: $DIR_SPEC"
      exit 1
    fi
    # echo "Running script in $KO_DIR"
  fi
}

# Find well-known locations
find-repository
find-project
find-module

# Construct search path
if [[ -z $KO_SEARCH_PATH ]]; then
  if [[ -z $KO_SEARCH_ROOTS ]]; then
    make-search-roots
  fi
  if [[ ! -z $KO_ADDITIONAL_SEARCH_ROOTS ]]; then
    add-search-roots
  fi
  make-search-path
fi

if [[ $VERBOSE -gt 0 ]]; then
  echo "Repo:        $KO_REPO"
  echo "Project:     $KO_PROJECT"
  echo "Module:      $KO_MODULE"
  echo "Search path: $KO_SEARCH_PATH"
fi

COMMAND=$1

# Search for script to execute
IFS=':' read -ra PATHS <<< "${KO_SEARCH_PATH}"
SCRIPTS=()
for p in "${PATHS[@]}"; do
  find-script "$COMMAND" "$p"
done

# Search for the first ko.kts|kt script when a matching script was not found
if [[ ${#SCRIPTS[@]} -eq 0 ]]; then
  for p in "${PATHS[@]}"; do
    if [[ -f "$p/ko.kts" ]]; then
      SCRIPTS+=("$p/ko.kts")
      break
    fi
    if [[ -f "$p/ko.kt" ]]; then
      SCRIPTS+=("$p/ko.kt")
      break
    fi
  done
else
  shift
fi

# Check to make sure we only found one script
if [[ ${#SCRIPTS[@]} -eq 0 ]]; then
  echo "Could not find script matching '$COMMAND'"
  exit 1
fi
if [[ ${#SCRIPTS[@]} -gt 1 ]]; then
  echo "Found too many scripts matching '$COMMAND':"
  for s in "${SCRIPTS[@]}"; do
    echo "$s"
  done
  exit 1
fi
export KO_SCRIPT="${SCRIPTS[0]}"

if [[ $VERBOSE -gt 0 ]]; then
  echo "Script:      $KO_SCRIPT"
fi

# Determine directory to run script
KO_DIR=
get-dir-from-script
if [[ -z $KO_DIR ]]; then
  KO_DIR="$PWD"
fi

if [[ $VERBOSE -gt 0 ]]; then
  echo "Run dir:     $KO_DIR"
fi

# Execute script
if [[ $KO_DIR != $PWD ]]; then
  pushd "$KO_DIR" >/dev/null
  kscript "$KO_SCRIPT" "$@"
  popd >/dev/null
else
  kscript "$KO_SCRIPT" "$@"
fi
