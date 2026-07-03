import axios from 'axios'

export const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export interface SessionInfo {
  token: string
  userId: number
  venueId: number | null
  role: string
}

let session: SessionInfo | null = null
let activeVenueId = 1

export function setSession(next: SessionInfo | null) {
  session = next
  if (next?.venueId) activeVenueId = next.venueId
}

export function setActiveVenueId(venueId: number) {
  activeVenueId = venueId
}

api.interceptors.request.use((config) => {
  config.headers = config.headers ?? {}
  if (session?.token) config.headers.Authorization = `Bearer ${session.token}`
  config.headers['X-Venue-Id'] = String(activeVenueId)
  return config
})

export interface DashboardMetric {
  todayOrders: number
  todayAmountCent: number
  todayRefundCent: number
  pendingRefunds: number
  venues: number
}

export interface Venue {
  id: number
  name: string
  sportTypes: string
  address: string
  status: string
}

export interface Court {
  id: number
  venueId: number
  name: string
  sportType: string
  status: string
}

export interface Booking {
  id: number
  orderNo: string
  venueId: number
  courtId: number
  userId: number
  startAt: string
  endAt: string
  payableCent: number
  status: string
  paymentOrderId: number | null
  paymentNo: string | null
  paymentStatus: string | null
}

export interface Refund {
  id: number
  refundNo: string
  paymentOrderId: number
  venueId: number
  amountCent: number
  reason: string
  status: string
  requestedAt: string
}

export interface MatchRoom {
  id: number
  matchNo: string
  bookingOrderId: number
  venueId: number
  courtId: number
  creatorUserId: number
  startAt: string
  endAt: string
  minPlayers: number
  maxPlayers: number
  currentPlayers: number
  amountCent: number
  perUserAmountCent: number
  payMode: string
  status: string
}

export interface MatchPlayer {
  id: number
  matchRoomId: number
  userId: number
  status: string
  paymentOrderId: number | null
  joinedAt: string
  leftAt: string | null
}

export interface SharePayment {
  id: number
  matchRoomId: number
  userId: number
  paymentOrderId: number
  paymentNo: string | null
  amountCent: number
  status: string
}

export interface ShareRefund {
  id: number
  matchRoomId: number
  userId: number
  refundOrderId: number | null
  amountCent: number
  status: string
}

export async function loginPlatform(username: string, password: string) {
  const response = await api.post<{ data: SessionInfo }>('/admin/auth/login', { username, password })
  setSession(response.data.data)
  return response.data.data
}

export async function loginVenue(username: string, password: string) {
  const response = await api.post<{ data: SessionInfo }>('/venue/auth/login', { username, password })
  setSession(response.data.data)
  return response.data.data
}

export async function fetchPlatformDashboard() {
  const response = await api.get<{ data: DashboardMetric }>('/admin/dashboard')
  return response.data.data
}

export async function fetchVenueDashboard(venueId: number) {
  setActiveVenueId(venueId)
  const response = await api.get<{ data: DashboardMetric }>('/venue/dashboard')
  return response.data.data
}

export async function listVenues() {
  const response = await api.get<{ data: Venue[] }>('/admin/venues')
  return response.data.data
}

export async function createVenue(data: { name: string; sportTypes: string; address: string; contactName: string; contactPhone: string }) {
  const response = await api.post<{ data: Venue }>('/admin/venues', data)
  return response.data.data
}

export async function approveVenue(id: number) {
  const response = await api.post<{ data: Venue }>(`/admin/venues/${id}/approve`)
  return response.data.data
}

export async function listCourts(venueId: number) {
  setActiveVenueId(venueId)
  const response = await api.get<{ data: Court[] }>('/venue/courts')
  return response.data.data
}

export async function createCourt(data: { venueId: number; name: string; sportType: string; indoor: boolean }) {
  setActiveVenueId(data.venueId)
  const response = await api.post<{ data: Court }>('/venue/courts', data)
  return response.data.data
}

export async function createPriceRule(data: { venueId: number; courtId: number; dayType: string; startTime: string; endTime: string; priceCent: number; priority: number }) {
  setActiveVenueId(data.venueId)
  const response = await api.post<{ data: { id: number; priceCent: number } }>('/venue/price-rules', data)
  return response.data.data
}

export async function listVenueBookings(venueId: number) {
  setActiveVenueId(venueId)
  const response = await api.get<{ data: Booking[] }>('/venue/bookings')
  return response.data.data
}

export async function checkInBooking(venueId: number, bookingId: number) {
  setActiveVenueId(venueId)
  const response = await api.post<{ data: Booking }>(`/venue/bookings/${bookingId}/checkin`)
  return response.data.data
}

export async function listVenueRefunds(venueId: number) {
  setActiveVenueId(venueId)
  const response = await api.get<{ data: Refund[] }>('/venue/refunds')
  return response.data.data
}

export async function listMatches(venueId?: number) {
  const response = await api.get<{ data: MatchRoom[] }>('/app/matches', { params: venueId ? { venueId } : {} })
  return response.data.data
}

export async function listMatchPlayers(matchId: number) {
  const response = await api.get<{ data: MatchPlayer[] }>(`/app/matches/${matchId}/players`)
  return response.data.data
}

export async function listMatchSharePayments(matchId: number) {
  const response = await api.get<{ data: SharePayment[] }>(`/app/matches/${matchId}/share-payments`)
  return response.data.data
}

export async function listMatchShareRefunds(matchId: number) {
  const response = await api.get<{ data: ShareRefund[] }>(`/app/matches/${matchId}/share-refunds`)
  return response.data.data
}

export interface AuditLog {
  id: number
  action: string
  objectType: string
  objectId: number | null
  operatorUserId: number | null
  operatorRole: string | null
  venueId: number | null
  summary: string
  createdAt: string
}

export async function listPlatformAuditLogs() {
  const response = await api.get<{ data: AuditLog[] }>('/admin/audit-logs')
  return response.data.data
}

export async function listVenueAuditLogs(venueId: number) {
  setActiveVenueId(venueId)
  const response = await api.get<{ data: AuditLog[] }>('/venue/audit-logs')
  return response.data.data
}
