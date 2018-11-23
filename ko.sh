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
  echo "Usage:"
  echo "  ko [options...] <command> [args...]"
  echo
  echo "The command can be an abbreviation for the script to execute, while"
  echo "the arguments are passed on to the discovered command script."
  echo
  echo "Options:"
  echo "  -c,--create     Creates and edits a new script named <command>.kts"
  echo "  -e,--edit       Edits the existing script for <command>"
  echo "  -v,--verbose    Prints information about the process"
  echo "  --version       Prints the version"
  echo
  exit 1
fi

# Process options
VERBOSE=
CREATE=
EDIT=
VERSION=

while [[ -n $1 && $1 = -* ]]; do
  case $1 in
  "-v" | "--verbose") VERBOSE=1; shift;;
  "-c" | "--create") CREATE=1; shift;;
  "-e" | "--edit") EDIT=1; shift;;
  "--version") VERSION=1; shift;;
  *) echo "Ignoring unknown option: $1";;
  esac
done

# Find the directory containing this script
SOURCE="${BASH_SOURCE[0]}"
# resolve until the file is no longer a symlink
while [[ -h $SOURCE ]]; do
  KO_HOME="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$KO_HOME/$SOURCE"
done
export KO_HOME="$(cd -P "$(dirname "$SOURCE")" && pwd)"

# Get version from build
if [[ -f "$KO_HOME/version.sh" ]]; then
  source "$KO_HOME/version.sh"
fi
if [[ -n $VERSION ]]; then
  echo "ko $KO_VERSION"
  exit 0
fi

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
  if [[ -n $KO_MODULE ]]; then sr+=("$KO_MODULE"); fi
  if [[ -n $KO_PROJECT ]]; then sr+=("$KO_PROJECT"); fi
  if [[ -n $KO_REPO ]]; then sr+=("$KO_REPO"); fi
  sr+=("$HOME")
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
  if [[ -f "$PWD/ko.kts" ]]; then sp+=("$PWD/ko.kts"); fi
  if [[ -f "$PWD/ko.kt" ]]; then sp+=("$PWD/ko.kt"); fi
  for r in "${sr[@]}"; do
    if [[ -f "$r/ko.kts" ]]; then sp+=("$r/ko.kts"); fi
    if [[ -f "$r/ko.kt" ]]; then sp+=("$r/ko.kt"); fi
    for p in "${SEARCH_PATHS[@]}"; do
      local dir="$r/$p"
      if [[ -d $dir ]]; then
        # echo "Adding $dir to search path"
        sp+=("$r/$p")
      fi
    done
  done
  if [[ -d "$KO_HOME/scripts" ]]; then
    sp+=("$KO_HOME/scripts")
  fi
  # remove duplicates
  sp=($(echo ${sp[@]} | tr [:space:] '\n' | awk '!a[$0]++'))
  export KO_SEARCH_PATH=$(join_by : "${sp[@]}")
}

# Adds matches to the list of scripts
# Args: path command
function find-script {
  if [[ -d $2 ]]; then
    # echo "Searching $2 for scripts matching $1"
    files=($(find "$2/" \( -iname "$1*.kts" -o -iname "$1*.kt" \) -maxdepth 1 -type f))
    for f in "${files[@]}"; do
      SCRIPTS+=("$(echo $f | sed 's,//,/,g')")
    done
  fi
}

# Adds matches if the script contains
# Args: command script
function find-command {
  # echo "Searching $2 for commands matching $1"
  :
}

# Checks the contents of the script for a run directory spec
# Args: script
function get-dir-from-script {
  local spec=$(sed -n '/^\/\/DIR / s/\/\/DIR //p' "$1")
  if [[ -n $spec ]]; then
    # echo "Found directory specification: $spec"
    if [[ $spec == \$* ]]; then
      # echo "Evaluating $spec"
      local name=${spec:1}
      KO_DIR=${!name}
      # echo "Evaluated to $KO_DIR"

      if [[ -z $KO_DIR ]]; then
        echo "Unable to resolve directory specified by environment variable $name"
        exit 1
      fi
    elif [[ $spec == ~* ]]; then
      # echo "Evaluating $spec"
      eval KO_DIR=$spec
      # echo "Evaluated to $KO_DIR"
    else
      # Trim leading and trailing whitespace
      KO_DIR=$(echo -e "$spec" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
    fi

    if [[ ! -d $KO_DIR ]]; then
      echo "Specified run directory does not exist: $spec"
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
  if [[ -n $KO_ADDITIONAL_SEARCH_ROOTS ]]; then
    add-search-roots
  fi
  make-search-path
fi

if [[ $VERBOSE -gt 0 ]]; then
  echo "Repository:  $KO_REPO"
  echo "Project:     $KO_PROJECT"
  echo "Module:      $KO_MODULE"
  echo "Search path: $KO_SEARCH_PATH"
fi

COMMAND=$1
if [[ -z $COMMAND ]]; then
  echo "The command parameter has not been specified"
  exit 1
fi

# Create new script in closest location
if [[ $CREATE == 1 ]]; then
  IFS=':' read -ra sp <<< "$KO_SEARCH_PATH"
  local sd=()
  for p in "${sp[@]}"; do
    if [[ -d $p ]]; then sd+=("$p"); fi
  done
  unset 'sd[${#sd[@]}-1]'

  if [[ ${#sd[@]} -gt 1 ]]; then
    local dir="${sd[1]}"
  else
    local dir="${sd[0]}"
  fi
  local script="$dir/$COMMAND.kts"

  if [[ ! -e "$script" ]]; then
    # Create new script file
    # echo "Creating new script: $script"
    cp "$KO_HOME/template.kts" "$script"
    chmod ug+x "$script"
    sed -i .tmp "s:<file>:$(basename "$script"):g" "$script"
    sed -i .tmp "s/<command>/$COMMAND/g" "$script"
    if [[ -n $KO_VERSION ]]; then
      sed -i .tmp "s/<version>/$KO_VERSION/g" "$script"
    fi
    rm -f "${script}.tmp"

    EDIT=2
  else
    echo "ERROR: Script $script already exists"
    exit 1
  fi

  if [[ $VERBOSE -gt 0 ]]; then
    echo "Created new script: $script"
  fi
fi

# Search for matching script
IFS=':' read -ra PATHS <<< "${KO_SEARCH_PATH}"
SCRIPTS=()
for p in "${PATHS[@]}"; do
  if [[ -d $p ]]; then
    # echo "Searching for $COMMAND in directory $p"
    find-script "$COMMAND" "$p"
  elif [[ -f $p ]]; then
    # echo "Searching for $COMMAND in script $p"
    find-command "$COMMAND" "$p"
  fi
done

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
export KO_SCRIPT_DIR="$(basename "$KO_SCRIPT")"

if [[ $VERBOSE -gt 0 ]]; then
  echo "Script:      $KO_SCRIPT"
fi

# Determine directory to run script
KO_DIR=
get-dir-from-script "${KO_SCRIPT}"
if [[ -z $KO_DIR ]]; then
  KO_DIR="$PWD"
fi

# Pass command as first argument to ko.kt{s} scripts
if [[ $KO_SCRIPT != "ko.kts" && $KO_SCRIPT !=  "ko.kt" ]]; then
  shift
fi

if [[ $VERBOSE -gt 0 ]]; then
  echo "Run dir:     $KO_DIR"
  if [[ "${#@}" -gt 0 ]]; then
    echo "Arguments:   $@"
  fi
  echo
fi

if [[ $EDIT == 1 ]]; then
  # Edit found script
  if [[ -n $VISUAL ]]; then
    $VISUAL $KO_SCRIPT
  elif [[ -n $EDITOR ]]; then
    $EDITOR $KO_SCRIPT
  else
    echo "Editor not specified by VISUAL or EDITOR environment variable"
    exit 1
  fi
elif [[ $KO_DIR != $PWD ]]; then
  # Execute script in specified directory
  pushd "$KO_DIR" >/dev/null
  kscript "$KO_SCRIPT" "$@"
  popd >/dev/null
else
  # Execute script in current directory
  kscript "$KO_SCRIPT" "$@"
fi
