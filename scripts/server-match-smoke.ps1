param(
    [string]$BaseUrl = "http://1.69.143.156:18200"
)

$ErrorActionPreference = "Stop"

function Invoke-ApiPost($Path, $Body, $Headers = @{}) {
    Invoke-RestMethod -Uri "$BaseUrl$Path" -Method Post -ContentType "application/json" -Headers $Headers -Body ($Body | ConvertTo-Json -Depth 8) -TimeoutSec 30
}

function Invoke-ApiGet($Path, $Headers = @{}) {
    Invoke-RestMethod -Uri "$BaseUrl$Path" -Headers $Headers -TimeoutSec 30
}

function New-TestVenue {
    $stamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
    $venue = Invoke-ApiPost "/api/admin/venues" @{
        name = "约战自测球馆-$stamp"
        sportTypes = "BADMINTON"
        address = "自动化测试地址"
        contactName = "测试联系人"
        contactPhone = "13800000000"
    }
    $venueId = $venue.data.id
    Invoke-ApiPost "/api/admin/venues/$venueId/approve" @{} | Out-Null
    $court = Invoke-ApiPost "/api/venue/courts" @{
        venueId = $venueId
        name = "约战A场"
        sportType = "BADMINTON"
        indoor = $true
    } @{ "X-Venue-Id" = "$venueId" }
    Invoke-ApiPost "/api/venue/price-rules" @{
        venueId = $venueId
        courtId = $court.data.id
        dayType = "WORKDAY"
        startTime = "08:00:00"
        endTime = "22:00:00"
        priceCent = 3000
        priority = 1
    } @{ "X-Venue-Id" = "$venueId" } | Out-Null
    [pscustomobject]@{ VenueId = $venueId; CourtId = $court.data.id }
}

function New-PaidBooking($VenueId, $CourtId, $UserId, $StartAt, $EndAt) {
    $booking = Invoke-ApiPost "/api/app/bookings" @{
        venueId = $VenueId
        courtId = $CourtId
        userId = $UserId
        startAt = $StartAt
        endAt = $EndAt
    }
    $prepay = Invoke-ApiPost "/api/app/payments/prepay" @{ bookingId = $booking.data.id }
    Invoke-ApiPost "/api/pay/mock/success/$($prepay.data.paymentNo)" @{} | Out-Null
    [pscustomobject]@{ BookingId = $booking.data.id; PaymentId = $prepay.data.id }
}

$fixture = New-TestVenue
$venueId = $fixture.VenueId
$courtId = $fixture.CourtId

$formedBooking = New-PaidBooking $venueId $courtId 41001 "2026-07-12T08:00:00+08:00" "2026-07-12T09:00:00+08:00"
$formedMatch = Invoke-ApiPost "/api/app/matches" @{
    bookingOrderId = $formedBooking.BookingId
    creatorUserId = 41001
    minPlayers = 2
    maxPlayers = 4
}
$formedMatchId = $formedMatch.data.id
Invoke-ApiPost "/api/app/matches/$formedMatchId/join" @{ userId = 41002 } | Out-Null
$share = Invoke-ApiPost "/api/app/matches/$formedMatchId/share-payments" @{
    userId = 41002
    amountCent = 3000
}
if ([string]::IsNullOrWhiteSpace($share.data.paymentNo)) {
    throw "AA 支付单未返回 paymentNo"
}
Invoke-ApiPost "/api/pay/mock/success/$($share.data.paymentNo)" @{} | Out-Null
$formedDetail = Invoke-ApiGet "/api/app/matches/$formedMatchId"
$formedPlayers = Invoke-ApiGet "/api/app/matches/$formedMatchId/players"
$formedShares = Invoke-ApiGet "/api/app/matches/$formedMatchId/share-payments"
if ($formedDetail.data.status -ne "FORMED") {
    throw "AA 支付成局失败：当前状态 $($formedDetail.data.status)"
}
$paidPlayer = @($formedPlayers.data | Where-Object { $_.userId -eq 41002 })[0]
if ($paidPlayer.status -ne "PAID") {
    throw "AA 支付后成员状态错误：$($paidPlayer.status)"
}
$paidShare = @($formedShares.data | Where-Object { $_.userId -eq 41002 })[0]
if ($paidShare.status -ne "PAID") {
    throw "AA 支付单状态错误：$($paidShare.status)"
}

$cancelBooking = New-PaidBooking $venueId $courtId 42001 "2026-07-12T09:00:00+08:00" "2026-07-12T10:00:00+08:00"
$cancelMatch = Invoke-ApiPost "/api/app/matches" @{
    bookingOrderId = $cancelBooking.BookingId
    creatorUserId = 42001
    minPlayers = 3
    maxPlayers = 4
}
$cancelMatchId = $cancelMatch.data.id
Invoke-ApiPost "/api/app/matches/$cancelMatchId/join" @{ userId = 42002 } | Out-Null
$cancelShare = Invoke-ApiPost "/api/app/matches/$cancelMatchId/share-payments" @{
    userId = 42002
    amountCent = 3000
}
Invoke-ApiPost "/api/pay/mock/success/$($cancelShare.data.paymentNo)" @{} | Out-Null
$beforeCancel = Invoke-ApiGet "/api/app/matches/$cancelMatchId"
if ($beforeCancel.data.status -eq "FORMED") {
    throw "未达到最低人数的约战不应成局"
}
$cancelled = Invoke-ApiPost "/api/app/matches/$cancelMatchId/cancel-not-formed" @{}
$cancelShares = Invoke-ApiGet "/api/app/matches/$cancelMatchId/share-payments"
$shareRefunds = Invoke-ApiGet "/api/app/matches/$cancelMatchId/share-refunds"
if ($cancelled.data.status -ne "NOT_FORMED_CANCELLED") {
    throw "未成局取消失败：$($cancelled.data.status)"
}
$refundedShare = @($cancelShares.data | Where-Object { $_.userId -eq 42002 })[0]
if ($refundedShare.status -ne "REFUNDED") {
    throw "未成局取消后 AA 支付单未退款：$($refundedShare.status)"
}
if (@($shareRefunds.data).Count -lt 1) {
    throw "未成局取消后未生成 AA 退款记录"
}

[pscustomobject]@{
    VenueId = $venueId
    CourtId = $courtId
    FormedMatchId = $formedMatchId
    FormedMatchStatus = $formedDetail.data.status
    PaidPlayerStatus = $paidPlayer.status
    PaidShareStatus = $paidShare.status
    CancelledMatchId = $cancelMatchId
    BeforeCancelStatus = $beforeCancel.data.status
    CancelledMatchStatus = $cancelled.data.status
    CancelledShareStatus = $refundedShare.status
    ShareRefundCount = @($shareRefunds.data).Count
} | ConvertTo-Json -Depth 5
