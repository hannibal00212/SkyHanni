#!/bin/bash

# todo MULTI-VERSION
TARGET_NAME="build/libs/SkyHanni-${UPDATE_VERSION}-mc1.8.9.jar"

read -r -d '' extra_notes <<EOF
Modrinth download: https://modrinth.com/mod/skyhanni/version/${UPDATE_VERSION}
Do **NOT** trust any mod just because they publish a checksum associated with it. These check sums are meant to verify only that two files are identical. They are not a certificate of origin, or a guarantee for the author of these files.
sha256sum: \`$(sha256sum "${TARGET_NAME}"|cut -f 1 -d ' '| tr -d '\n')\`
md5sum: \`$(md5sum "${TARGET_NAME}"|cut -f 1 -d ' '| tr -d '\n')\`
EOF

extra_notes+="\n\n$(cat build/changelog-GITHUB.txt)"

echo -e "${extra_notes}" > build/update-notes.txt

grep -v "Modrinth download" build/update-notes.txt | sed '1{/^$/d;}' > build/Changelog.md
