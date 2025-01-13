#!/bin/bash

preReleaseParam=""
if ! echo "${UPDATE_VERSION}" | grep -E '.*\.0$'>/dev/null; then
  preReleaseParam="--prerelease"
fi

# todo MULTI-VERSION
TARGET_NAME="build/libs/SkyHanni-${UPDATE_VERSION}-mc1.8.9.jar"

extra_notes=$(cat build/update-notes.txt)

if [ -f "${TARGET_NAME}" ]; then
  gh release create -t "SkyHanni ${UPDATE_VERSION}" --verify-tag "${UPDATE_VERSION}" ${preReleaseParam} --draft --notes "$extra_notes" "${TARGET_NAME}"
else
  echo "Error: File ${TARGET_NAME} does not exist."
  exit 1
fi
