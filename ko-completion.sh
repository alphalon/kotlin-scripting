
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
  local cmd=
  local help=0
  local args=0
  shopt -s nocasematch
  for w in "${COMP_WORDS[@]}"; do
    if [[ -z $cmd ]]; then
      if [[ $w == "help" ]]; then
        help=1
      elif [[ $w != "ko" && $w != -* ]]; then
        cmd="$w"
      fi
    else
      args=1
      break
    fi
  done
  shopt -u nocasematch

  local cword="${COMP_WORDS[COMP_CWORD]}"

  if [[ $args -eq 0 ]]; then
    if [[ $cword == --* ]]; then
      if [[ $help -eq 0 ]]; then
        COMPREPLY=($(compgen -W "--create --edit --search-path --file --dir --version --verbose --help" -- "$cword"))
      fi
    elif [[ $cword == -* ]]; then
      if [[ $help -eq 0 ]]; then
        COMPREPLY=($(compgen -W "-c -e -s -f -d -v -h" -- "$cword"))
      fi
    else
      if [[ $help -gt 0 && -z $cmd && $COMP_LINE == *help ]]; then
        cmd="help"
      fi

      COMPREPLY=($(ko --completion "$cmd"))
    fi
  fi
}

complete -F ko-completions ko
