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
        name = "并发自测球馆-$stamp"
        sportTypes = "BADMINTON"
        address = "自动化测试地址"
        contactName = "测试联系人"
        contactPhone = "13800000000"
    }
    $venueId = $venue.data.id
    Invoke-ApiPost "/api/admin/venues/$venueId/approve" @{} | Out-Null
    $court = Invoke-ApiPost "/api/venue/courts" @{
        venueId = $venueId
        name = "A场"
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

$fixture = New-TestVenue
$venueId = $fixture.VenueId
$courtId = $fixture.CourtId

$start = "2026-07-10T10:00:00+08:00"
$end = "2026-07-10T11:00:00+08:00"
$jobs = 1..10 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($BaseUrl, $VenueId, $CourtId, $UserId, $StartAt, $EndAt)
        try {
            $body = @{
                venueId = $VenueId
                courtId = $CourtId
                userId = $UserId
                startAt = $StartAt
                endAt = $EndAt
            } | ConvertTo-Json
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/app/bookings" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 30
            [pscustomobject]@{ Success = $true; BookingId = $response.data.id; UserId = $UserId }
        } catch {
            [pscustomobject]@{ Success = $false; BookingId = $null; UserId = $UserId }
        }
    } -ArgumentList $BaseUrl, $venueId, $courtId, (20000 + $_), $start, $end
}
$results = $jobs | Receive-Job -Wait -AutoRemoveJob
$successes = @($results | Where-Object Success)
if ($successes.Count -ne 1) {
    throw "并发预约测试失败：成功数 $($successes.Count)，期望 1"
}

$bookingId = $successes[0].BookingId
$prepay = Invoke-ApiPost "/api/app/payments/prepay" @{ bookingId = $bookingId }
Invoke-ApiPost "/api/pay/mock/success/$($prepay.data.paymentNo)" @{} | Out-Null

$booking90 = Invoke-ApiPost "/api/app/bookings" @{
    venueId = $venueId
    courtId = $courtId
    userId = 30001
    startAt = "2026-07-10T11:00:00+08:00"
    endAt = "2026-07-10T12:30:00+08:00"
}
$prepay90 = Invoke-ApiPost "/api/app/payments/prepay" @{ bookingId = $booking90.data.id }
Invoke-ApiPost "/api/pay/mock/success/$($prepay90.data.paymentNo)" @{} | Out-Null

$refundFull = Invoke-ApiPost "/api/app/refunds" @{
    paymentOrderId = $prepay.data.id
    venueId = $venueId
    amountCent = 6000
    reason = "全额退款释放时间槽"
}

$bookingPartial = Invoke-ApiPost "/api/app/bookings" @{
    venueId = $venueId
    courtId = $courtId
    userId = 30002
    startAt = "2026-07-10T12:30:00+08:00"
    endAt = "2026-07-10T13:30:00+08:00"
}
$prepayPartial = Invoke-ApiPost "/api/app/payments/prepay" @{ bookingId = $bookingPartial.data.id }
Invoke-ApiPost "/api/pay/mock/success/$($prepayPartial.data.paymentNo)" @{} | Out-Null
$refundPartial = Invoke-ApiPost "/api/app/refunds" @{
    paymentOrderId = $prepayPartial.data.id
    venueId = $venueId
    amountCent = 3000
    reason = "部分退款不释放时间槽"
}

$dashboard = Invoke-ApiGet "/api/venue/dashboard" @{ "X-Venue-Id" = "$venueId" }

[pscustomobject]@{
    VenueId = $venueId
    CourtId = $courtId
    ConcurrentAttempts = 10
    ConcurrentSuccess = $successes.Count
    SixtyMinuteBookingId = $bookingId
    NinetyMinuteBookingId = $booking90.data.id
    FullRefundId = $refundFull.data.id
    PartialRefundId = $refundPartial.data.id
    VenueTodayOrders = $dashboard.data.todayOrders
    VenueTodayAmountCent = $dashboard.data.todayAmountCent
    VenueTodayRefundCent = $dashboard.data.todayRefundCent
} | ConvertTo-Json -Depth 5
