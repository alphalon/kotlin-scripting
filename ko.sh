#!/usr/bin/env bash
#
# Project: Kotlin Scripting
# Created: Nov 12, 2018
#
# Kotlin script runner (requires kscript for compilation and execution)
#
# See: https://github.com/holgerbrandl/kscript
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

# NOTE: Reserve options for -r, -p, and -m. These may be used for scoping
# operations to the repository, project, or module in the future.

export KO_PREFIX=$(basename "$0")

# Keep documented options in sync with ko-completion.sh
if [[ -z $1 || $1 == "-h" || $1 == "--help" ]]; then
  echo "Usage: $KO_PREFIX [options] command [args...]"
  echo
  echo "The command can be an abbreviation for matching the script to execute,"
  echo "while the args are passed on to that script."
  echo
  echo "Options:"
  echo "  -c, --create         Creates and edits a new script named <command>.kts"
  echo "  -e, --edit           Edits the existing script matching <command>"
  echo "  -i, --interactive    Starts an interactive session for a script"
  echo "  -s, --search-path    Prints the search path for the current directory"
  echo "  -f, --file           Prints the filename of the matching script"
  echo "  -d, --dir            Prints the directory containing the matching script"
  echo "  -v, --version        Prints the version"
  echo "  --verbose            Prints information about the script resolution process"
  echo "  --clean              Clears the kscript compilation cache"
  exit 0
fi

# Process options
while [[ -n $1 && $1 == -* ]]; do
  case $1 in
    "-c" | "--create") CREATE_SCRIPT=1; shift;;
    "-e" | "--edit") EDIT_SCRIPT=1; shift;;
    "-i" | "--interactive") INTERACTIVE=1; shift;;
    "-i" | "--idea") IDEA_SCRIPT=1; shift;;
    "-s" | "--search-path") PRINT_SEARCH=1; shift;;
    "-f" | "--file") PRINT_FILE=1; shift;;
    "-d" | "--dir") PRINT_DIR=1; shift;;
    "--clean") CLEAR_CACHE=1; shift;;
    "-v" | "--version") PRINT_VERSION=1; shift;;
    "--verbose") VERBOSE=1; shift;;
    "--debug") DEBUG=1; shift;;
    "--completion") COMPLETION=1; shift;;
    *) echo "Ignoring unknown option: $1"; shift;;
  esac
done

if [[ $DEBUG -gt 0 ]]; then
  VERBOSE=1
fi

# Clear the kscript cache
if [[ $CLEAR_CACHE -gt 0 ]]; then
  kscript --clear-cache
fi

# Allow nested calls
export KO_COMMAND=

# Find the directory containing this script
SOURCE="${BASH_SOURCE[0]}"
while [[ -h $SOURCE ]]; do
  KO_HOME="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$KO_HOME/$SOURCE"
done
export KO_HOME="$(cd -P "$(dirname "$SOURCE")" && pwd)"

# Get version from build
if [[ -f "$KO_HOME/scripts/ko-framework-version.sh" ]]; then
  source "$KO_HOME/scripts/ko-framework-version.sh"
  export KO_VERSION
fi
if [[ -n $PRINT_VERSION ]]; then
  echo "$KO_VERSION"
  exit 0
fi

# Adds additional markers/paths to the array
# Args: array-name var-name
function add-to-array {
  if [[ -n ${!2} ]]; then
    IFS=':' read -ra ms <<< "${!2}"
    for m in "${ms[@]}"; do
      eval "${1}+=("$m")"
    done
  fi
}

# Files or directories marking the repository root directory
REPO_MARKERS=(".git" ".svn" ".hg" ".fslckout" "_FOSSIL_" ".bzr" "_darcs" "CVS" ".ko_repo")
add-to-array "REPO_MARKERS" "KO_ADDITIONAL_REPO_MARKERS"

# Files marking project root directories
PROJECT_MARKERS=("build.gradle" "build.gradle.kts" "project.clj" "pom.xml" "*.xcworkspace" "*.xcodeproj" "*.iml" "build.boot" "deps.clj" "build.xml" "Makefile" "CMakeLists.txt" "setup.py" "Pipfile" "requirements.txt" ".csproj" ".vbproj" ".nuspec" "build.sbt" "package.json" "Gemfile" "*.gemspec" ".ko_project")
add-to-array "PROJECT_MARKERS" "KO_ADDITIONAL_PROJECT_MARKERS"

# Files marking module directories
MODULE_MARKERS=("build.gradle" "build.gradle.kts" "pom.xml" "*.xcodeproj" "package.json" ".ko_module")
add-to-array "MODULE_MARKERS" "KO_ADDITIONAL_MODULE_MARKERS"

# Subdirectories used for searching for scripts along the search roots
SEARCH_DIRS=("scripts" "bin")
add-to-array "SEARCH_DIRS" "KO_ADDITIONAL_SEARCH_DIRS"

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
        export KO_REPO_FILE="$dir/$marker"
        # echo "Found repository at $KO_REPO"
        if [[ -f "$dir/.ko_repo" ]]; then
          source "$dir/.ko_repo"
        fi
        break
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
        export KO_PROJECT_FILE="$(compgen -G "$dir/$marker")"
        # echo "Found possible project at $KO_PROJECT"
        if [[ -f "$dir/.ko_project" ]]; then
          source "$dir/.ko_project"
        fi
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
        if [[ -f "$dir/.ko_module" ]]; then
          source "$dir/.ko_module"
        fi
        return
      fi
    done
    dir=$(dirname "$dir")
  done

  if [[ -n $KO_PROJECT ]]; then
    # echo "Did not find a module, using project"
    export KO_MODULE="$KO_PROJECT"
  fi
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
  sr=($(echo "${sr[@]}" | tr [:space:] '\n' | awk '!a[$0]++'))
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
  sr=($(echo "${sr[@]}" | tr [:space:] '\n' | awk '!a[$0]++'))
  KO_SEARCH_ROOTS=$(join_by : "${sr[@]}")
}

# Adds search paths to each root and returns a path of directories contains scripts
function make-search-path {
  IFS=':' read -ra sr <<< "$KO_SEARCH_ROOTS"
  local sp=("$PWD")
  if [[ -f "$PWD/ko.kts" ]]; then sp+=("$PWD/ko.kts"); fi
  for r in "${sr[@]}"; do
    if [[ -f "$r/ko.kts" ]]; then sp+=("$r/ko.kts"); fi
    for p in "${SEARCH_DIRS[@]}"; do
      local dir="$r/$p"
      if [[ -d $dir ]]; then
        # echo "Adding $dir to search path"
        sp+=("$dir")
        if [[ -f "$dir/ko.kts" ]]; then sp+=("$dir/ko.kts"); fi
      fi
    done
  done
  if [[ -d "$KO_HOME/scripts" ]]; then
    sp+=("$KO_HOME/scripts")
  fi
  # remove duplicates
  sp=($(echo "${sp[@]}" | tr [:space:] '\n' | awk '!a[$0]++'))
  export KO_SEARCH_PATH=$(join_by : "${sp[@]}")
}

# Adds matches to the list of scripts
# Args: path command
function find-script {
  if [[ -d $2 ]]; then
    # echo "Searching $2 for scripts matching $1"
    files=($(find "$2/" -maxdepth 1 -iname "$1*.kts" -type f | sort))
    for f in "${files[@]}"; do
      local path=$(echo $f | sed 's,//,/,g')
      local filename=$(basename "$f")
      local name="${filename%.*}"

      # Look for matching command to get proper case
      local found=0
      local cmds=($(sed -n '/^\/\/CMD / s/\/\/CMD //p' "$path" | awk '{print $1;}'))
      shopt -s nocasematch
      for cmd in "${cmds[@]}"; do
        if [[ $cmd == $name ]]; then
          # echo "Matched command $cmd in script $2"
          SCRIPTS+=("$cmd|$path")
          found=1
          break
        fi
      done
      shopt -u nocasematch
    done
  fi
}

# Adds matches if the script contains a //CMD matching the specified command
# Args: command script
function find-command {
  # echo "Searching $2 for commands matching $1"
  local cmds=($(sed -n '/^\/\/CMD / s/\/\/CMD //p' "$2" | awk '{print $1;}'))
  # echo "Found: ${cmds[@]}"

  shopt -s nocasematch
  for cmd in "${cmds[@]}"; do
    if [[ $cmd == $1* ]]; then
      # echo "Matched command $cmd in script $2"
      SCRIPTS+=("$cmd|$2")
    fi
  done
  shopt -u nocasematch
}

# Searches directories and files for command matches
# Args: command
function find-script-or-command {
  for p in "${PATHS[@]}"; do
    if [[ -d $p ]]; then
      # echo "Searching for $1 in directory $p"
      find-script "$1" "$p"
    elif [[ -f $p ]]; then
      # echo "Searching for $1 in script $p"
      find-command "$1" "$p"
    fi
  done
}

# Retrieves the description for the command
# Args: command script
function get-description {
  local desc=$(sed -n -e "s/^\/\/CMD *$1\([ -]*\)\(.*\)/\2/p" "$2")
  desc=$(echo -e "$desc" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
  # echo "Command description: $desc"

  export KO_DESC="$desc"
}

# Checks the search results for partial and exact matches
# Args: command
function check-results {
  IDENTICAL=1
  local base=
  shopt -s nocasematch
  for mr in "${SCRIPTS[@]}"; do
    # echo "Checking result for $mr"
    IFS='|' read -ra PARSED <<< "$mr"
    local cmd="${PARSED[0]}"

    # Are all of the results for the same command?
    if [[ -z $base ]]; then
      base="$cmd"
    elif [[ "$cmd" != "$base" ]]; then
      IDENTICAL=0
    fi

    # Save the first exact match
    if [[ "$cmd" == "$1" ]]; then
      if [[ -z $EXACT_COMMAND ]]; then
        EXACT_COMMAND="$cmd"
        EXACT_SCRIPT="${PARSED[1]}"
      fi
    fi
  done
  shopt -u nocasematch

  if [[ $DEBUG -gt 0 ]]; then
    echo "Identical:      $IDENTICAL"
    echo "Exact Command:  $EXACT_COMMAND"
    echo "Exact Script:   $EXACT_SCRIPT"
  fi
}

# Checks the contents of the script for a run directory spec
# Args: script
function get-dir-from-script {
  local spec=$(sed -n '/^\/\/DIR / s/\/\/DIR //p' "$1")
  spec=$(echo -e "$spec" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
  if [[ -n $spec ]]; then
    # echo "Found directory specification: $spec"
    if [[ $spec == \$* ]]; then
      # echo "Evaluating $spec"
      local name=${spec:1}
      KO_DIR=${!name}
      # echo "Evaluated to $KO_DIR"

      if [[ -z $KO_DIR ]]; then
        echo "ERROR: unable to resolve directory specified by environment variable $name in script"
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
      echo "ERROR: directory specified in script does not exist: $spec"
      exit 1
    fi
    # echo "Running script in $KO_DIR"
  fi
}

# Tries to locate a template in the current directory and along the search roots
function find-script-template {
  if [[ -f .ko_template ]]; then
    KO_TEMPLATE=".ko_template"
    return
  fi

  # Check search roots
  IFS=':' read -ra sr <<< "$KO_SEARCH_ROOTS"
  for r in "${sr[@]}"; do
    if [[ -f "$r/.ko_template" ]]; then
      KO_TEMPLATE="$r/.ko_template"
      return
    fi
  done
}

# Create a script in the nearest location on the search path
# Args: command
function create-script {
  IFS=':' read -ra sp <<< "$KO_SEARCH_PATH"
  local sd=()
  for p in "${sp[@]}"; do
    if [[ -d $p ]]; then sd+=("$p"); fi
  done
  unset 'sd[${#sd[@]}-1]'

  local extension="${1##*.}"
  if [[ -n $extension && $extension != $1 ]]; then
    # Create and edit specified file
    local script=$1
    export KO_COMMAND=$1
    export KO_SCRIPT=$1
  else
    # Don't prefer current directory
    if [[ ${#sd[@]} -gt 1 ]]; then
      local dir="${sd[1]}"
    else
      local dir="${sd[0]}"
    fi
    local script="$dir/$1.kts"
  fi

  if [[ ! -e "$script" ]]; then
    # Locate script template
    if [[ -z $KO_TEMPLATE ]]; then
      find-script-template
    fi
    if [[ ! -f $KO_TEMPLATE ]]; then
      KO_TEMPLATE="$KO_HOME/scripts/kts.template"
    fi
    if [[ ! -f $KO_TEMPLATE ]]; then
      echo "ERROR: unable to locate script template"
      exit 1
    fi

    # Create new script file
    cp "$KO_TEMPLATE" "$script"
    if [[ -n $KO_PROJECT ]]; then
      sed -i.tmp "s:<project>:$(basename "$KO_PROJECT"):g" "$script"
    elif [[ -n $KO_REPO ]]; then
      sed -i.tmp "s:<project>:$(basename "$KO_REPO"):g" "$script"
    fi
    sed -i.tmp "s:<file>:$(basename "$script"):g" "$script"
    if [[ $COMMAND != "ko" && $COMMAND != "ko.kts" ]]; then
      local name="$(tr '[:upper:]' '[:lower:]' <<< ${COMMAND:0:1})${COMMAND:1}"
      sed -i.tmp "s/<command>/$name/g" "$script"
    fi
    if [[ -n $KO_VERSION ]]; then
      sed -i.tmp "s/<version>/$KO_VERSION/g" "$script"
    fi
    rm -f "${script}.tmp"

    EDIT_SCRIPT=2
  else
    echo "ERROR: Script $script already exists"
    exit 1
  fi

  if [[ $VERBOSE -gt 0 ]]; then
    echo "Created new script: $script"
  fi
}

# Edits the script with the preferred editor
# Args: file
function edit-script {
  if [[ -n $KO_EDITOR ]]; then
    $KO_EDITOR $KO_SCRIPT
  elif [[ -n $VISUAL ]]; then
    $VISUAL $KO_SCRIPT
  elif [[ -n $EDITOR ]]; then
    $EDITOR $KO_SCRIPT
  else
    echo "ERROR: editor not specified by either VISUAL or EDITOR environment variable"
    echo "Your script file is located at ${KO_SCRIPT}"
    exit 1
  fi

  # Make executable if '#!' is present
  if [[ "$(head -c 2 "$KO_SCRIPT")" == "#!" && ! -x "$KO_SCRIPT" ]]; then
    chmod u+x "$KO_SCRIPT"
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
export KO_SEARCH_DIRS=$(join_by : "${SEARCH_DIRS[@]}")

if [[ $PRINT_SEARCH -gt 0 ]]; then
  echo "$KO_SEARCH_PATH"
  exit 0
fi

if [[ $VERBOSE -gt 0 ]]; then
  echo "Repository:     $KO_REPO"
  echo "Project:        $KO_PROJECT"
  echo "Module:         $KO_MODULE"
  echo "Project File:   $KO_PROJECT_FILE"
  echo "Search path:    $KO_SEARCH_PATH"
fi

COMMAND=$1
if [[ -z $COMMAND && $COMPLETION -eq 0 ]]; then
  if [[ $VERBOSE -gt 0 || $CLEAR_CACHE -gt 0 ]]; then
    exit 0
  fi

  echo "ERROR: the command parameter has not been specified"
  exit 1
fi

# Create new script in closest location
if [[ $CREATE_SCRIPT == 1 ]]; then
  create-script "$COMMAND"
fi

if [[ -z $KO_COMMAND ]]; then
  # Search for matching script
  IFS=':' read -ra PATHS <<< "${KO_SEARCH_PATH}"
  SCRIPTS=()
  find-script-or-command "$COMMAND"

  # Try fuzzy search when nothing is found
  if [[ ${#SCRIPTS[@]} -eq 0 ]]; then
    WORD_FUZZY=$(echo "$COMMAND" | sed -e 's/\([[:upper:]]\)/*\1/g')
    find-script-or-command "$WORD_FUZZY"
  fi
  if [[ ${#SCRIPTS[@]} -eq 0 ]]; then
    LETTER_FUZZY=$(echo "$COMMAND" | sed -e 's/\([[:alpha:]]\)/*\1/g')
    find-script-or-command "$LETTER_FUZZY"
  fi

  if [[ $DEBUG -gt 0 ]]; then
    echo "All matches:"
    for s in "${SCRIPTS[@]}"; do
      IFS='|' read -ra PARSED <<< "$s"
      printf "%-23s %s\n" "${PARSED[0]}" "${PARSED[1]}"
    done
  fi

  # Return completions
  if [[ $COMPLETION -gt 0 ]]; then
    for s in "${SCRIPTS[@]}"; do
      IFS='|' read -ra PARSED <<< "$s"
      CMD="${PARSED[0]}"
      if [[ ! $CMD = *.gradle ]]; then
        echo "$CMD"
      fi
    done
    exit 0
  fi

  # Check to make sure we only found one script
  if [[ ${#SCRIPTS[@]} -eq 0 && $CREATE_SCRIPT -eq 0 ]]; then
    echo "ERROR: could not find script matching '$COMMAND'"
    exit 1
  fi
  if [[ ${#SCRIPTS[@]} -gt 1 ]]; then
    check-results "$COMMAND"
    if [[ -n $EXACT_COMMAND ]]; then
      export KO_COMMAND="$EXACT_COMMAND"
      export KO_SCRIPT="$EXACT_SCRIPT"
    elif [[ $IDENTICAL -eq 0 ]]; then
      echo "ERROR: found too many scripts matching '$COMMAND':"
      echo
      printf "%s\n" "${SCRIPTS[@]}" | column -t -s \|
      exit 1
    fi
  fi
  if [[ -z $KO_COMMAND ]]; then
    IFS='|' read -ra PARSED <<< "${SCRIPTS[0]}"
    export KO_COMMAND="${PARSED[0]}"
    export KO_SCRIPT="${PARSED[1]}"
  fi
  get-description "$KO_COMMAND" "$KO_SCRIPT"
fi
export KO_SCRIPT_DIR="$(basename "$KO_SCRIPT")"

if [[ $VERBOSE -gt 0 ]]; then
  echo "Command:        $KO_COMMAND"
  echo "Script:         $KO_SCRIPT"
fi

# Determine directory to run script
KO_DIR=
get-dir-from-script "${KO_SCRIPT}"
if [[ -z $KO_DIR ]]; then
  KO_DIR="$PWD"
fi

# Pass command as first argument to ko.kts scripts
if [[ $(basename "$KO_SCRIPT") == "ko.kts" ]]; then
  set -- "$KO_COMMAND" "${@:2}"
else
  shift
fi

if [[ $VERBOSE -gt 0 ]]; then
  echo "Run dir:        $KO_DIR"
  if [[ "${#@}" -gt 0 ]]; then
    echo "Script args:    $@"
  fi
  echo
fi

KSCRIPT_OPTS=
if [[ $INTERACTIVE -gt 0 ]]; then
  KSCRIPT_OPTS="--interactive"
fi

shopt -s checkwinsize
if hash tput 2>/dev/null; then
  export LINES=$(tput lines)
  export COLUMNS=$(tput cols)
fi

if [[ $IDEA_SCRIPT -gt 0 ]]; then
  # Edit found script in IDEA
  pushd "$(dirname "$KO_SCRIPT")" >/dev/null
  kscript --idea "$(basename "$KO_SCRIPT")"
  popd >/dev/null
elif [[ $EDIT_SCRIPT -gt 0 ]]; then
  # Edit found script
  edit-script "$KO_SCRIPT"
elif [[ $PRINT_FILE -gt 0 ]]; then
  echo "$KO_SCRIPT"
elif [[ $PRINT_DIR -gt 0 ]]; then
  echo "$(dirname "$KO_SCRIPT")"
elif [[ $KO_DIR != $PWD ]]; then
  # Execute script in specified directory
  pushd "$KO_DIR" >/dev/null
  kscript $KSCRIPT_OPTS "$KO_SCRIPT" "$@"
  popd >/dev/null
else
  # Execute script in current directory
  kscript $KSCRIPT_OPTS "$KO_SCRIPT" "$@"
fi
