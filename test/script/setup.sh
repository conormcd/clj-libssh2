#!/bin/sh

set -o errexit
set -o nounset

abort() {
  echo "$@"
  exit 1
}

emit_env_vars() {
  cat > "${TMP_DIR}/env" <<EOF
SSH_AGENT_PID=${SSH_AGENT_PID}
SSH_AUTH_SOCK=${SSH_AUTH_SOCK}
SSHD_PID=${SSHD_PID}
EOF
  cat "${TMP_DIR}/env"
}

ensure_clean_tmp_dir() {
  ensure_root_dir
  TMP_DIR="${ROOT}/test/tmp"
  if [ -e "${TMP_DIR}" ]; then
    abort "${TMP_DIR} exists, previous test run may not have torn down cleanly"
  fi
  mkdir -p "${TMP_DIR}"
  export TMP_DIR
}

ensure_root_dir() {
  if [ -z "${ROOT:-}" ]; then
    ROOT="$(pwd)"
    if [ ! -e "${ROOT}/test/script/setup.sh" ]; then
      abort "${ROOT} is not a valid test root."
    fi
    export ROOT
  fi
}

generate_sshd_host_keys() {
  require_executable ssh-keygen
  ssh-keygen -N '' -t rsa1 -f "${TMP_DIR}/ssh_host_key" > /dev/null
  ssh-keygen -N '' -t dsa -f "${TMP_DIR}/ssh_host_dsa_key" > /dev/null
  ssh-keygen -N '' -t rsa -f "${TMP_DIR}/ssh_host_rsa_key" > /dev/null
}

generate_user_keys() {
  require_executable ssh-add
  require_executable ssh-keygen

  # The key to be used for most tests.
  ssh-keygen -N '' -t rsa -f "${TMP_DIR}/id_rsa" > /dev/null
  ssh-add "${TMP_DIR}/id_rsa" > /dev/null 2>&1

  # A key to be used for tests to ensure we can pass a passphrase.
  ssh-keygen -N 'correct horse battery staple' \
    -t rsa -f "${TMP_DIR}/id_rsa_with_passphrase" > /dev/null

  # A key that will never be in authorized_keys.
  ssh-keygen -N '' -t rsa -f "${TMP_DIR}/id_rsa_never_authorised" > /dev/null

  # Generate authorized_keys
  cat "${TMP_DIR}/id_rsa.pub" \
      "${TMP_DIR}/id_rsa_with_passphrase.pub" > "${TMP_DIR}/authorized_keys"
}

launch_ssh_agent() {
  require_executable ssh-agent
  eval "$(ssh-agent -s -a "${TMP_DIR}/agent.sock")" > /dev/null
  ssh-add -D > /dev/null 2>&1
}

launch_sshd() {
  local sshd
  local host
  local port

  host=${1:-127.0.0.1}
  port=${2:-2222}

  require_executable sshd
  generate_sshd_host_keys
  generate_user_keys
  sshd=$(which sshd)
  ${sshd} -f /dev/null \
          -h "${TMP_DIR}/ssh_host_key" \
          -h "${TMP_DIR}/ssh_host_dsa_key" \
          -h "${TMP_DIR}/ssh_host_rsa_key" \
          -o "AuthorizedKeysFile=${TMP_DIR}/authorized_keys" \
          -o "ListenAddress=${host}:${port}"
  SSHD_PID=$(pgrep -f "${TMP_DIR}/ssh_host_dsa_key")
  export SSHD_PID
}

require_executable() {
  if ! which "$1" > /dev/null 2>&1; then
    abort "Missing required executable: $1"
  fi
}

teardown() {
  sh "${ROOT}/test/script/teardown.sh"
}

ensure_root_dir
teardown
ensure_clean_tmp_dir
launch_ssh_agent
launch_sshd "$@"
emit_env_vars
