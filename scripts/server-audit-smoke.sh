set -euo pipefail
BASE=http://127.0.0.1:18200
TOKEN=$(curl -s -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin"}' "$BASE/api/admin/auth/login" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["token"])')
NAME="审计验证馆$(date +%s)"
VENUE_JSON=$(curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "{\"name\":\"$NAME\",\"sportTypes\":\"BADMINTON\",\"address\":\"audit-check\",\"contactName\":\"audit\",\"contactPhone\":\"13800000000\"}" "$BASE/api/admin/venues")
VENUE_ID=$(printf '%s' "$VENUE_JSON" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["id"])')
curl -s -H "Authorization: Bearer $TOKEN" -X POST "$BASE/api/admin/venues/$VENUE_ID/approve" >/dev/null
PLATFORM_JSON=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE/api/admin/audit-logs")
VENUE_AUDIT_JSON=$(curl -s -H "Authorization: Bearer $TOKEN" -H "X-Venue-Id: $VENUE_ID" "$BASE/api/venue/audit-logs")
printf '%s' "$PLATFORM_JSON" | python3 -c 'import json,sys; data=json.load(sys.stdin)["data"]; print("platform_api_count="+str(len(data)))'
printf '%s' "$VENUE_AUDIT_JSON" | python3 -c 'import json,sys; data=json.load(sys.stdin)["data"]; print("venue_api_rows="+str([(x["action"], x["venueId"], x["operatorRole"]) for x in data[:5]]))'
echo "venue_id=$VENUE_ID"
docker exec ball-saas-postgres psql -U ball_saas -d ball_saas -t -A -c "select action, object_type, object_id, operator_role, venue_id from audit_log where venue_id=$VENUE_ID order by id;"
