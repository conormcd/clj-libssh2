#!/bin/sh

set -o errexit
set -o nounset

abort() {
  echo "$@"
  exit 1
}

ensure_root_dir() {
  if [ -z "${ROOT:-}" ]; then
    ROOT="$(pwd)"
    export ROOT
    if [ ! -e "${ROOT}/test/script/setup.sh" ]; then
      abort "${ROOT} is not a valid test root."
    fi
  fi
}

find_tmp_dir() {
  ensure_root_dir
  TMP_DIR="${ROOT}/test/tmp"
  export TMP_DIR
}

kill_ssh_agent() {
  if [ -e "${TMP_DIR}/env" ]; then
    eval "$(cat "${TMP_DIR}/env")"
    if [ -n "${SSH_AGENT_PID:-}" ]; then
      kill -9 "${SSH_AGENT_PID}" || true
    fi
    if [ -n "${SSH_AUTH_SOCK:-}" ]; then
      rm -f "${SSH_AUTH_SOCK}"
    fi
  fi
}

kill_sshd() {
  if [ -e "${TMP_DIR}/env" ]; then
    eval "$(cat "${TMP_DIR}/env")"
    if [ -n "${SSHD_PID:-}" ]; then
      kill -9 "${SSHD_PID}" || true
    fi
  fi
}

ensure_root_dir
find_tmp_dir
if [ -d "${TMP_DIR}" ]; then
  kill_ssh_agent
  kill_sshd
  rm -r "${TMP_DIR}"
fi
