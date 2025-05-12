#!/bin/sh
set -e


echo "Starting stripe listen..."

# https://github.com/stripe/stripe-cli/issues/423
# Verification is skipped because the certificate is self-signed
stripe listen --forward-to https://host.docker.internal:8443/payment/success --skip-verify &
sleep 3

echo "Triggering payment_intent.succeeded..."
stripe trigger payment_intent.succeeded

update-ca-certificates

tail -f /dev/null
