#! /usr/bin/env bash
# Static header fields.
HEADER='{
	"type": "JWT",
	"alg": "RS256"
}'

SUB=${1:-'abc@xyz.com'}

payload='{
}'

# Use jq to set the dynamic `iat` and `exp`
# fields on the payload using the current time.
# `iat` is set to now, and `exp` is now + 1 hour. Note: 3600 seconds = 1 hour
PAYLOAD=$(
	echo "${payload}" | jq --arg time_str "$(date +%s)" --arg sub_str $SUB \
	'
	($time_str | tonumber) as $time_num
	| .iat=$time_num
	| .exp=($time_num + 60 * 60 * 24 * 365)
	| .sub=$sub_str
	'
)

function b64enc() { openssl enc -base64 -A | tr '+/' '-_' | tr -d '='; }

function rs_sign() { openssl dgst -binary -sha256 -sign playback-auth-keys/private.pem ; }

JWT_HDR_B64="$(echo -n "$HEADER" | b64enc)"
JWT_PAY_B64="$(echo -n "$PAYLOAD" | b64enc)"
UNSIGNED_JWT="$JWT_HDR_B64.$JWT_PAY_B64"
SIGNATURE=$(echo -n "$UNSIGNED_JWT" | rs_sign | b64enc)

echo "$UNSIGNED_JWT.$SIGNATURE"