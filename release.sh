#!/bin/bash

set -o nounset

export GITHUB_USER=conormcd
export GITHUB_REPO=clj-libssh2

abort() {
  echo "$@"
  exit 1
}

current_version() {
  grep -m1 '(defproject' project.clj | \
    sed -e 's,^.*\([0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\).*$,\1,'
}

major() {
  current_version | cut -d. -f1
}

minor() {
  current_version | cut -d. -f2
}

patch() {
  if [ -n "${CIRCLE_BUILD_NUM:-}" ]; then
    echo "${CIRCLE_BUILD_NUM}"
  else
    current_version | cut -d. -f3
  fi
}

branch() {
  if [ -n "${CIRCLE_BRANCH:-}" ]; then
    echo "${CIRCLE_BRANCH}"
  else
    git rev-parse --abbrev-ref HEAD
  fi
}

sha() {
  if [ -n "${CIRCLE_SHA1:-}" ]; then
    echo "${CIRCLE_SHA1}"
  else
    git rev-parse HEAD
  fi
}

sha_short() {
  sha | sed -e 's,^\(........\).*,\1,'
}

new_version() {
  if [ "$(branch)" != "master" ]; then
    echo "$(major).$(minor).$(patch)-$(branch)-$(sha_short)"
  else
    echo "$(major).$(minor).$(patch)"
  fi
}

sed_inplace() {
  if sed --help 2>&1 | grep -q -m 1 GNU; then
    sed -i -e "$@"
  else
    sed -i '' -e "$@"
  fi
}

update_project_clj() {
  local _v=$1
  echo "Updating project.clj to version ${_v}"
  sed_inplace "s,\((defproject clj-libssh2 \)\"[^\"]*\",\1\"${_v}\"," project.clj
}

build_jars() {
  echo "Building JARs..."
  lein jar 2>&1 | sed -e 's,^,  ,'
}

save_artifacts() {
  if [ -n "${CIRCLE_ARTIFACTS:-}" ]; then
    echo "Copying JARs to CircleCI artifacts..."
    find . -name '*.jar' -exec cp {} "${CIRCLE_ARTIFACTS}" \;
  fi
}

ensure_github_release_tool_installed() {
  local _cwd
  _cwd=$(pwd)

  if [ -z "${GOPATH:-}" ]; then
    export GOPATH=${_cwd}/.go
  fi
  export PATH="${PATH}:${GOPATH}/bin"
  if ! which github-release > /dev/null 2>&1; then
    echo "Installing github-release..."
    go get github.com/aktau/github-release
  fi
}

github_release() {
  local _version
  _version=$1

  echo "Releasing ${_version}"
  github-release release \
    --user "${GITHUB_USER}" \
    --repo "${GITHUB_REPO}" \
    --tag "${_version}" \
    --target "$(sha)" \
    --description "Release version ${_version}"
  find . -name '*.jar' | while read -r _jar; do
    github-release upload \
      --user "${GITHUB_USER}" \
      --repo "${GITHUB_REPO}" \
      --tag "${_version}" \
      --name "$(basename "${_jar}")" \
      --file "${_jar}"
  done
}

clojars_deploy() {
  lein deploy clojars 2>&1 | sed -e 's,^,  ,'
}

v=$(new_version)
update_project_clj "${v}"
build_jars
save_artifacts
if [ "$(branch)" = "master" ]; then
  if [ -z "${CLOJARS_USERNAME:-}" ]; then
    abort "Missing environment variable: CLOJARS_USERNAME"
  fi
  if [ -z "${CLOJARS_PASSWORD:-}" ]; then
    abort "Missing environment variable: CLOJARS_PASSWORD"
  fi
  if [ -z "${GITHUB_TOKEN:-}" ]; then
    abort "Missing environment variable: GITHUB_TOKEN"
  fi

  # Make a GitHub release
  ensure_github_release_tool_installed
  github_release "${v}"

  # Push to clojars
  clojars_deploy
fi
