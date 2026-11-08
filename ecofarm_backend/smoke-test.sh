#!/bin/bash
BASE_URL="http://localhost:8080/ecofarm_backend"

echo "Testing anchor status endpoint..."
curl -s "$BASE_URL/api/v1/batches/1/anchor-status" | jq . && echo "✓ Anchor Status OK"

echo "Testing reconciliation endpoint..."
curl -s "$BASE_URL/api/v1/admin/reconciliation" | jq . && echo "✓ Reconciliation OK"

echo "All endpoints reachable."
