#!/bin/sh
set -e

HOOK_SRC="$(dirname "$0")/pre-push"
HOOK_DST=".git/hooks/pre-push"

if [ ! -d ".git" ]; then
  echo "Execute este script na raiz do repositório."
  exit 1
fi

cp "$HOOK_SRC" "$HOOK_DST"
chmod +x "$HOOK_DST"
echo "Hook pre-push instalado com sucesso."