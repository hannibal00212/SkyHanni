#!/bin/bash

username="SkyHanni Updates"
avatarUrl="https://cdn.discordapp.com/icons/997079228510117908/ec44b7eced15f6f1276b6fe0fd469851.webp?size=240"

content=$(cat ${CHANGELOG_FILE})
splitText="--SPLIT--"

send_to_discord() {
  local message=$1
  json_payload=$(jq -n \
    --arg content "$message" \
    --arg username "$username" \
    --arg avatar_url "$avatarUrl" \
    '{content: $content, embeds: null, username: $username, avatar_url: $avatar_url, attachments: []}')
  curl -H "Content-Type: application/json" -d "$json_payload" $DISCORD_WEBHOOK
}

split_content=""
while IFS= read -r line || [[ -n "$line" ]]; do
    if [[ "$line" == "$splitText" ]]; then
        if [[ -n "$split_content" ]]; then
            send_to_discord "$split_content"
            split_content=""
        fi
    else
        split_content+="$line"$'\n'
    fi
done < "$CHANGELOG_FILE"

if [[ -n "$split_content" ]]; then
    send_to_discord "$split_content"
fi
