
#!/usr/bin/env bash
#
# Project: Kotlin Scripting
# Created: Nov 27, 2018
#
# Bash completion for the Kotlin script runner
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

# https://iridakos.com/tutorials/2018/03/01/bash-programmable-completion-tutorial.html

function ko-completions {
  local cmd
  local cmdw
  local helpw

  local i=0
  shopt -s nocasematch
  for w in "${COMP_WORDS[@]}"; do
    if [[ $w == "help" ]]; then
      cmdw=$i
      helpw=$i
    elif [[ $w != "ko" && $w != -* ]]; then
      cmd="$w"
      cmdw=$i
      break
    fi
    i=$((i + 1))
  done

  local cword="${COMP_WORDS[COMP_CWORD]}"

  if [[ $cmdw -eq 0 || ($COMP_CWORD -lt $cmdw && ($helpw -eq 0 || $COMP_CWORD -lt $helpw)) ]]; then
    # Get matching options (for ko.sh script)
    if [[ $cword == --* ]]; then
      COMPREPLY=($(compgen -W "--create --edit --search-path --file --dir --version --verbose --help" -- "$cword"))
    elif [[ $cword == -* ]]; then
      COMPREPLY=($(compgen -W "-c -e -s -f -d -v -h" -- "$cword"))
    fi
    COMPREPLY=("${COMPREPLY[@]/%/ }")
  elif [[ $COMP_CWORD -eq $cmdw ]]; then
    # Get matching scripts for command
    if [[ $helpw -gt 0 && $cword == "help" ]]; then
      cmd="help"
    fi

    COMPREPLY=($(ko --completion "$cmd"))
    COMPREPLY=("${COMPREPLY[@]/%/ }")
  else
    # Get matching files and directories
    COMPREPLY=($(compgen -f  -- "${COMP_WORDS[${COMP_CWORD}]}" ))

    for ((ff=0; ff<${#COMPREPLY[@]}; ff++)); do
      test -d "${COMPREPLY[$ff]}" && COMPREPLY[$ff]="${COMPREPLY[$ff]}/"
      test -f "${COMPREPLY[$ff]}" && COMPREPLY[$ff]="${COMPREPLY[$ff]} "
    done
  fi

  shopt -u nocasematch
}

complete -o bashdefault -o nospace -F ko-completions ko
