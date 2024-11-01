#!/bin/bash

# This script processes the Detekt SARIF file and outputs results in a format
# suitable for annotation in CI/CD systems, with the file paths fixed.

SARIF_FILE="$1"

# Check if SARIF file exists
if [ ! -f "$SARIF_FILE" ]; then
    echo "SARIF file not found: $SARIF_FILE"
    exit 1
fi

# Define jq command to parse SARIF file and fix the file path
read -r -d '' jq_command <<'EOF'
.runs[].results[] |
{
    # Adjust the path to remove the runner workspace prefix
    "full_path": (.locations[].physicalLocation.artifactLocation.uri | sub("file://.*/SkyHanni/"; "")),
    "file_name": (.locations[].physicalLocation.artifactLocation.uri | split("/") | last),
    "l": .locations[].physicalLocation,
    "level": .level,
    "message": .message.text,
    "ruleId": .ruleId
} |
(
    "::" + (.level) +
    " file=" + (.full_path) +
    ",line=" + (.l.region.startLine|tostring) +
    ",title=" + (.ruleId) +
    ",col=" + (.l.region.startColumn|tostring) +
    ",endColumn=" + (.l.region.endColumn|tostring) +
    "::" + (.message)
)
EOF

# Run jq command to format the output
jq -r "$jq_command" < "$SARIF_FILE"
