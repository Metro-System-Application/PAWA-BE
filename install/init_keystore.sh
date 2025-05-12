#!/bin/sh

set -e

RESOURCES_FOLDER="/Users/phantrongnguyen/coding/new_etp/PAWA-BE/application/src/main/resources"
CA_NAME="PAWA_BE_CN"

# Create CA certificate
openssl genrsa -out "${RESOURCES_FOLDER}/${CA_NAME}.key" 2048
openssl req -x509 -new -nodes -key \
  "${RESOURCES_FOLDER}/${CA_NAME}.key" -sha256 -days 3650 \
  -out "${RESOURCES_FOLDER}/${CA_NAME}.pem" -subj "/CN=PAWA Backend"

# Create a Private Key and Certificate Signing Request (CSR)
openssl genrsa -out "${RESOURCES_FOLDER}/server.key" 2048
openssl req -new -key "${RESOURCES_FOLDER}/server.key" -out \
  "${RESOURCES_FOLDER}/server.csr" -subj "/CN=localhost"

# Check for SAN config file
SAN_CONFIG="${RESOURCES_FOLDER}/san.cnf"
if [ ! -f "$SAN_CONFIG" ]; then
  echo "[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = localhost

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1" > "$SAN_CONFIG"
fi

# Sign CSR with CA
openssl x509 -req -in "${RESOURCES_FOLDER}/server.csr" \
  -CA "${RESOURCES_FOLDER}/${CA_NAME}.pem" \
  -CAkey "${RESOURCES_FOLDER}/${CA_NAME}.key" \
  -CAcreateserial \
  -out "${RESOURCES_FOLDER}/server.crt" \
  -days 825 \
  -sha256 -extfile "$SAN_CONFIG" -extensions v3_req

# Convert to PKCS12
openssl pkcs12 -export \
  -in "${RESOURCES_FOLDER}/server.crt" \
  -inkey "${RESOURCES_FOLDER}/server.key" \
  -out "${RESOURCES_FOLDER}/keystore.p12" \
  -name httpskey \
  -CAfile "${RESOURCES_FOLDER}/${CA_NAME}.pem" -caname root \
  -passout pass:123123

# --- JWT Key Generation using keytool ---

echo "[INFO] Generating JWT key..."

keytool -genkeypair \
  -alias jwtkey \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore "${RESOURCES_FOLDER}/keystore.p12" \
  -storetype PKCS12 \
  -dname "CN=JWT, O=PAWA, L=HCMC, C=VN" \
  -storepass 123123

# --- Trust the CA certificate ---

echo "[INFO] Attempting to trust CA certificate..."

OS_TYPE="$(uname)"

case "$OS_TYPE" in
  Darwin)
    # macOS
    sudo security add-trusted-cert -d -r trustRoot \
      -k /Library/Keychains/System.keychain "${RESOURCES_FOLDER}/${CA_NAME}.pem"
    echo "[INFO] CA trusted on macOS."
    ;;
  Linux)
    # Linux
    if [ -d "/etc/ca-certificates/trust-source/anchors" ]; then
      sudo cp "${RESOURCES_FOLDER}/${CA_NAME}.pem" "/etc/ca-certificates/trust-source/anchors/${CA_NAME}.pem"
      sudo update-ca-trust
      echo "[INFO] CA trusted on Linux."
    else
      echo "[WARN] Could not find system trust directory. Install manually."
    fi
    ;;
  MINGW* | MSYS* | CYGWIN* | Windows_NT)
    # Windows Git Bash or similar
    echo "[INFO] On Windows, double-click '${RESOURCES_FOLDER}/${CA_NAME}.pem' and install it:"
    echo "  -> Local Machine"
    echo "  -> Trusted Root Certification Authorities"
    ;;
  *)
    echo "[WARN] Unknown OS. Trust the CA manually."
    ;;
esac

echo "[INFO] Script execution completed successfully."
