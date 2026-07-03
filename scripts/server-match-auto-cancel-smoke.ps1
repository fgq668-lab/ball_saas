param(
    [string]$BaseUrl = "http://1.69.143.156:18200",
    [int]$WaitSeconds = 75
)

$ErrorActionPreference = "Stop"

function Invoke-ApiPost($Path, $Body, $Headers = @{}) {
    Invoke-RestMethod -Uri "$BaseUrl$Path" -Method Post -ContentType "application/json" -Headers $Headers -Body ($Body | ConvertTo-Json -Depth 8) -TimeoutSec 30
}

function Invoke-ApiGet($Path, $Headers = @{}) {
    Invoke-RestMethod -Uri "$BaseUrl$Path" -Headers $Headers -TimeoutSec 30
}

$stamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$venue = Invoke-ApiPost "/api/admin/venues" @{
    name = "自动取消自测球馆-$stamp"
    sportTypes = "BADMINTON"
    address = "自动化测试地址"
    contactName = "测试联系人"
    contactPhone = "13800000000"
}
$venueId = $venue.data.id
Invoke-ApiPost "/api/admin/venues/$venueId/approve" @{} | Out-Null
$court = Invoke-ApiPost "/api/venue/courts" @{
    venueId = $venueId
    name = "自动取消A场"
    sportType = "BADMINTON"
    indoor = $true
} @{ "X-Venue-Id" = "$venueId" }
$courtId = $court.data.id
Invoke-ApiPost "/api/venue/price-rules" @{
    venueId = $venueId
    courtId = $courtId
    dayType = "WORKDAY"
    startTime = "08:00:00"
    endTime = "22:00:00"
    priceCent = 3000
    priority = 1
} @{ "X-Venue-Id" = "$venueId" } | Out-Null

$booking = Invoke-ApiPost "/api/app/bookings" @{
    venueId = $venueId
    courtId = $courtId
    userId = 43001
    startAt = "2026-07-13T08:00:00+08:00"
    endAt = "2026-07-13T09:00:00+08:00"
}
$prepay = Invoke-ApiPost "/api/app/payments/prepay" @{ bookingId = $booking.data.id }
Invoke-ApiPost "/api/pay/mock/success/$($prepay.data.paymentNo)" @{} | Out-Null
$match = Invoke-ApiPost "/api/app/matches" @{
    bookingOrderId = $booking.data.id
    creatorUserId = 43001
    minPlayers = 3
    maxPlayers = 4
}
$matchId = $match.data.id
$updateSql = "update match_room set start_at = now() - interval '2 hours', end_at = now() - interval '1 hour' where id = $matchId;"
$updateCommand = "docker exec ball-saas-postgres psql -U ball_saas -d ball_saas -c `"$updateSql`""
& ssh -p 2218 frank@1.69.143.156 $updateCommand | Out-Null

Invoke-ApiPost "/api/app/matches/$matchId/join" @{ userId = 43002 } | Out-Null
$share = Invoke-ApiPost "/api/app/matches/$matchId/share-payments" @{
    userId = 43002
    amountCent = 3000
}
Invoke-ApiPost "/api/pay/mock/success/$($share.data.paymentNo)" @{} | Out-Null
$before = Invoke-ApiGet "/api/app/matches/$matchId"
Start-Sleep -Seconds $WaitSeconds
$after = Invoke-ApiGet "/api/app/matches/$matchId"
$shares = Invoke-ApiGet "/api/app/matches/$matchId/share-payments"
$refunds = Invoke-ApiGet "/api/app/matches/$matchId/share-refunds"

if ($after.data.status -ne "NOT_FORMED_CANCELLED") {
    throw "定时自动取消未生效：before=$($before.data.status), after=$($after.data.status)"
}
$refundedShare = @($shares.data | Where-Object { $_.userId -eq 43002 })[0]
if ($refundedShare.status -ne "REFUNDED") {
    throw "自动取消后 AA 支付单未退款：$($refundedShare.status)"
}
if (@($refunds.data).Count -lt 1) {
    throw "自动取消后未生成 AA 退款记录"
}

[pscustomobject]@{
    VenueId = $venueId
    CourtId = $courtId
    MatchId = $matchId
    BeforeStatus = $before.data.status
    AfterStatus = $after.data.status
    ShareStatus = $refundedShare.status
    ShareRefundCount = @($refunds.data).Count
    WaitSeconds = $WaitSeconds
} | ConvertTo-Json -Depth 5
