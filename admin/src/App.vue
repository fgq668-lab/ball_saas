<template>
  <el-container class="layout">
    <el-aside width="232px" class="sidebar">
      <div class="brand">Ball SaaS</div>
      <el-menu :default-active="activeView" class="menu" @select="selectView">
        <el-menu-item index="dashboard">经营概览</el-menu-item>
        <el-menu-item index="venues">场馆审核</el-menu-item>
        <el-menu-item index="courts">场地价格</el-menu-item>
        <el-menu-item index="orders">预约订单</el-menu-item>
        <el-menu-item index="refunds">退款管理</el-menu-item>
        <el-menu-item index="matches">约战管理</el-menu-item>
        <el-menu-item index="audit">审计日志</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div>
          <strong>{{ currentTitle }}</strong>
          <el-tag size="small" type="success">测试环境</el-tag>
        </div>
        <div class="session-bar">
          <el-input-number v-model="activeVenueId" :min="1" size="small" controls-position="right" @change="refreshCurrent" />
          <el-button size="small" @click="login('platform')">平台登录</el-button>
          <el-button size="small" type="primary" @click="login('venue')">场馆登录</el-button>
          <el-button size="small" @click="login('staff')">员工登录</el-button>
          <el-tag v-if="session" size="small">{{ session.role }}</el-tag>
        </div>
      </el-header>

      <el-main>
        <el-alert v-if="loadError" class="mb" :title="loadError" type="warning" show-icon :closable="false" />

        <section v-if="activeView === 'dashboard'">
          <section class="metrics">
            <el-card v-for="item in metrics" :key="item.label" shadow="never">
              <div class="metric-label">{{ item.label }}</div>
              <div class="metric-value">{{ item.value }}</div>
            </el-card>
          </section>
          <el-card shadow="never">
            <template #header>
              <div class="card-header"><span>场馆视角</span><el-button @click="loadDashboard">刷新</el-button></div>
            </template>
            <el-descriptions :column="4" border>
              <el-descriptions-item label="场馆 ID">{{ activeVenueId }}</el-descriptions-item>
              <el-descriptions-item label="今日订单">{{ venueDashboard.todayOrders }}</el-descriptions-item>
              <el-descriptions-item label="今日交易额">{{ formatCent(venueDashboard.todayAmountCent) }}</el-descriptions-item>
              <el-descriptions-item label="待处理退款">{{ venueDashboard.pendingRefunds }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </section>

        <section v-if="activeView === 'venues'">
          <el-card shadow="never" class="mb">
            <template #header><div class="card-header"><span>新增入驻场馆</span><el-button type="primary" @click="submitVenue">提交</el-button></div></template>
            <el-form :model="venueForm" label-width="88px" class="form-grid">
              <el-form-item label="名称"><el-input v-model="venueForm.name" /></el-form-item>
              <el-form-item label="运动类型"><el-input v-model="venueForm.sportTypes" /></el-form-item>
              <el-form-item label="地址"><el-input v-model="venueForm.address" /></el-form-item>
              <el-form-item label="联系人"><el-input v-model="venueForm.contactName" /></el-form-item>
              <el-form-item label="电话"><el-input v-model="venueForm.contactPhone" /></el-form-item>
            </el-form>
          </el-card>
          <el-card shadow="never">
            <template #header><div class="card-header"><span>场馆审核列表</span><el-button @click="loadVenues">刷新</el-button></div></template>
            <el-table :data="venues" height="430">
              <el-table-column prop="id" label="ID" width="72" />
              <el-table-column prop="name" label="场馆" min-width="180" />
              <el-table-column prop="sportTypes" label="类型" width="130" />
              <el-table-column prop="address" label="地址" min-width="220" />
              <el-table-column prop="status" label="状态" width="130"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="170"><template #default="{ row }"><el-button size="small" @click="openVenueDetail(row)">详情</el-button><el-button size="small" type="primary" @click="approve(row.id)">通过</el-button></template></el-table-column>
            </el-table>
          </el-card>
        </section>

        <section v-if="activeView === 'courts'">
          <el-card shadow="never" class="mb">
            <template #header><div class="card-header"><span>场地与价格</span><el-button type="primary" @click="submitCourt">新增场地</el-button></div></template>
            <el-form :model="courtForm" label-width="88px" class="form-grid">
              <el-form-item label="场馆 ID"><el-input-number v-model="courtForm.venueId" :min="1" /></el-form-item>
              <el-form-item label="场地名"><el-input v-model="courtForm.name" /></el-form-item>
              <el-form-item label="类型"><el-input v-model="courtForm.sportType" /></el-form-item>
              <el-form-item label="室内"><el-switch v-model="courtForm.indoor" /></el-form-item>
            </el-form>
          </el-card>
          <el-card shadow="never" class="mb">
            <template #header><div class="card-header"><span>价格规则</span><el-button @click="submitPrice">保存价格</el-button></div></template>
            <el-form :model="priceForm" label-width="88px" class="form-grid">
              <el-form-item label="场馆 ID"><el-input-number v-model="priceForm.venueId" :min="1" /></el-form-item>
              <el-form-item label="场地 ID"><el-input-number v-model="priceForm.courtId" :min="1" /></el-form-item>
              <el-form-item label="日期类型"><el-select v-model="priceForm.dayType"><el-option label="工作日" value="WORKDAY" /><el-option label="周末" value="WEEKEND" /></el-select></el-form-item>
              <el-form-item label="开始"><el-input v-model="priceForm.startTime" /></el-form-item>
              <el-form-item label="结束"><el-input v-model="priceForm.endTime" /></el-form-item>
              <el-form-item label="价格分"><el-input-number v-model="priceForm.priceCent" :min="1" /></el-form-item>
            </el-form>
          </el-card>
          <el-card shadow="never">
            <template #header><div class="card-header"><span>场地列表</span><el-button @click="loadCourts">刷新</el-button></div></template>
            <el-table :data="courts" height="300">
              <el-table-column prop="id" label="ID" width="72" />
              <el-table-column prop="venueId" label="场馆" width="90" />
              <el-table-column prop="name" label="场地" />
              <el-table-column prop="sportType" label="类型" />
              <el-table-column prop="status" label="状态" />
            </el-table>
          </el-card>
        </section>

        <section v-if="activeView === 'orders'">
          <el-card shadow="never">
            <template #header><div class="card-header"><span>预约订单</span><el-button @click="loadBookings">刷新</el-button></div></template>
            <el-table :data="bookings" height="560">
              <el-table-column prop="orderNo" label="订单号" width="170" />
              <el-table-column prop="venueId" label="场馆" width="80" />
              <el-table-column prop="courtId" label="场地" width="80" />
              <el-table-column prop="userId" label="用户" width="90" />
              <el-table-column label="时间" min-width="230"><template #default="{ row }">{{ formatDate(row.startAt) }} - {{ formatDate(row.endAt) }}</template></el-table-column>
              <el-table-column label="金额" width="110"><template #default="{ row }">{{ formatCent(row.payableCent) }}</template></el-table-column>
              <el-table-column prop="status" label="状态" width="140"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="170"><template #default="{ row }"><el-button size="small" @click="openBookingDetail(row)">详情</el-button><el-button size="small" type="primary" :disabled="row.status !== 'RESERVED'" @click="checkIn(row.id)">核销</el-button></template></el-table-column>
            </el-table>
          </el-card>
        </section>

        <section v-if="activeView === 'refunds'">
          <el-card shadow="never">
            <template #header><div class="card-header"><span>退款管理</span><el-button @click="loadRefunds">刷新</el-button></div></template>
            <el-table :data="refunds" height="560">
              <el-table-column prop="refundNo" label="退款单号" width="170" />
              <el-table-column prop="paymentOrderId" label="支付单" width="90" />
              <el-table-column prop="venueId" label="场馆" width="80" />
              <el-table-column label="金额" width="110"><template #default="{ row }">{{ formatCent(row.amountCent) }}</template></el-table-column>
              <el-table-column prop="reason" label="原因" />
              <el-table-column prop="status" label="状态" width="120"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="100"><template #default="{ row }"><el-button size="small" @click="openRefundDetail(row)">详情</el-button></template></el-table-column>
            </el-table>
          </el-card>
        </section>

        <section v-if="activeView === 'matches'">
          <el-card shadow="never">
            <template #header><div class="card-header"><span>约战管理</span><el-button @click="loadMatches">刷新</el-button></div></template>
            <el-table :data="matches" height="560">
              <el-table-column prop="matchNo" label="约战号" width="170" />
              <el-table-column prop="venueId" label="场馆" width="80" />
              <el-table-column prop="courtId" label="场地" width="80" />
              <el-table-column label="人数" width="100"><template #default="{ row }">{{ row.currentPlayers }}/{{ row.maxPlayers }}</template></el-table-column>
              <el-table-column label="AA金额" width="110"><template #default="{ row }">{{ formatCent(row.perUserAmountCent) }}</template></el-table-column>
              <el-table-column label="时间" min-width="230"><template #default="{ row }">{{ formatDate(row.startAt) }} - {{ formatDate(row.endAt) }}</template></el-table-column>
              <el-table-column prop="status" label="状态" width="150"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="100"><template #default="{ row }"><el-button size="small" @click="openMatchDetail(row)">详情</el-button></template></el-table-column>
            </el-table>
          </el-card>
        </section>
        <section v-if="activeView === 'audit'">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>审计日志</span>
                <div class="header-actions">
                  <el-button @click="loadAuditLogs('platform')">平台视角</el-button>
                  <el-button type="primary" @click="loadAuditLogs('venue')">场馆视角</el-button>
                </div>
              </div>
            </template>
            <el-table :data="auditLogs" height="560">
              <el-table-column prop="createdAt" label="时间" width="180"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
              <el-table-column prop="action" label="操作" width="150" />
              <el-table-column prop="objectType" label="对象" width="150" />
              <el-table-column prop="objectId" label="对象ID" width="90" />
              <el-table-column prop="operatorRole" label="角色" width="150"><template #default="{ row }"><el-tag>{{ row.operatorRole || '-' }}</el-tag></template></el-table-column>
              <el-table-column prop="operatorUserId" label="操作人" width="100" />
              <el-table-column prop="venueId" label="场馆" width="90" />
              <el-table-column prop="summary" label="摘要" min-width="240" />
            </el-table>
          </el-card>
        </section>
        <el-drawer v-model="venueDrawerVisible" title="场馆详情" size="520px">
          <el-descriptions v-if="selectedVenue" :column="1" border>
            <el-descriptions-item label="场馆 ID">{{ selectedVenue.id }}</el-descriptions-item>
            <el-descriptions-item label="名称">{{ selectedVenue.name }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ selectedVenue.sportTypes }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ selectedVenue.address }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedVenue.status }}</el-descriptions-item>
          </el-descriptions>
        </el-drawer>

        <el-drawer v-model="bookingDrawerVisible" title="订单详情" size="640px">
          <el-descriptions v-if="selectedBooking" :column="2" border>
            <el-descriptions-item label="订单号">{{ selectedBooking.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedBooking.status }}</el-descriptions-item>
            <el-descriptions-item label="场馆">{{ selectedBooking.venueId }}</el-descriptions-item>
            <el-descriptions-item label="场地">{{ selectedBooking.courtId }}</el-descriptions-item>
            <el-descriptions-item label="用户">{{ selectedBooking.userId }}</el-descriptions-item>
            <el-descriptions-item label="金额">{{ formatCent(selectedBooking.payableCent) }}</el-descriptions-item>
            <el-descriptions-item label="开始">{{ formatDate(selectedBooking.startAt) }}</el-descriptions-item>
            <el-descriptions-item label="结束">{{ formatDate(selectedBooking.endAt) }}</el-descriptions-item>
            <el-descriptions-item label="支付单 ID">{{ selectedBooking.paymentOrderId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ selectedBooking.paymentStatus || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="selectedBooking" class="drawer-actions">
            <el-button type="primary" :disabled="selectedBooking.status !== 'RESERVED'" @click="checkIn(selectedBooking.id)">核销订单</el-button>
          </div>
        </el-drawer>

        <el-drawer v-model="refundDrawerVisible" title="退款详情" size="560px">
          <el-descriptions v-if="selectedRefund" :column="1" border>
            <el-descriptions-item label="退款单号">{{ selectedRefund.refundNo }}</el-descriptions-item>
            <el-descriptions-item label="支付单">{{ selectedRefund.paymentOrderId }}</el-descriptions-item>
            <el-descriptions-item label="场馆">{{ selectedRefund.venueId }}</el-descriptions-item>
            <el-descriptions-item label="金额">{{ formatCent(selectedRefund.amountCent) }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedRefund.status }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatDate(selectedRefund.requestedAt) }}</el-descriptions-item>
            <el-descriptions-item label="原因">{{ selectedRefund.reason }}</el-descriptions-item>
          </el-descriptions>
        </el-drawer>
        <el-drawer v-model="matchDrawerVisible" title="约战详情" size="760px">
          <template v-if="selectedMatch">
            <el-descriptions :column="2" border class="mb">
              <el-descriptions-item label="约战号">{{ selectedMatch.matchNo }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ selectedMatch.status }}</el-descriptions-item>
              <el-descriptions-item label="人数">{{ selectedMatch.currentPlayers }}/{{ selectedMatch.maxPlayers }}</el-descriptions-item>
              <el-descriptions-item label="AA金额">{{ formatCent(selectedMatch.perUserAmountCent) }}</el-descriptions-item>
            </el-descriptions>
            <h3>成员</h3>
            <el-table :data="matchPlayers" class="mb" height="180">
              <el-table-column prop="userId" label="用户" width="100" />
              <el-table-column prop="status" label="状态" width="130"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
              <el-table-column label="加入时间"><template #default="{ row }">{{ formatDate(row.joinedAt) }}</template></el-table-column>
            </el-table>
            <h3>AA支付</h3>
            <el-table :data="matchSharePayments" class="mb" height="180">
              <el-table-column prop="userId" label="用户" width="100" />
              <el-table-column prop="paymentNo" label="支付单号" min-width="170" />
              <el-table-column label="金额" width="110"><template #default="{ row }">{{ formatCent(row.amountCent) }}</template></el-table-column>
              <el-table-column prop="status" label="状态" width="130"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
            </el-table>
            <h3>AA退款</h3>
            <el-table :data="matchShareRefunds" height="180">
              <el-table-column prop="userId" label="用户" width="100" />
              <el-table-column label="金额" width="110"><template #default="{ row }">{{ formatCent(row.amountCent) }}</template></el-table-column>
              <el-table-column prop="status" label="状态" width="130"><template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
            </el-table>
          </template>
        </el-drawer>

      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  approveVenue,
  checkInBooking,
  createCourt,
  createPriceRule,
  createVenue,
  fetchPlatformDashboard,
  fetchVenueDashboard,
  listPlatformAuditLogs,
  listCourts,
  listMatches,
  listMatchPlayers,
  listMatchSharePayments,
  listMatchShareRefunds,
  listVenueBookings,
  listVenueAuditLogs,
  listVenueRefunds,
  listVenues,
  loginPlatform,
  loginVenue,
  setActiveVenueId,
  type AuditLog,
  type Booking,
  type Court,
  type DashboardMetric,
  type MatchPlayer,
  type MatchRoom,
  type SharePayment,
  type ShareRefund,
  type Refund,
  type SessionInfo,
  type Venue
} from './api'

const activeView = ref('dashboard')
const activeVenueId = ref(1)
const session = ref<SessionInfo | null>(null)
const loadError = ref('')

const dashboard = ref<DashboardMetric>({ todayOrders: 0, todayAmountCent: 0, todayRefundCent: 0, pendingRefunds: 0, venues: 0 })
const venueDashboard = ref<DashboardMetric>({ todayOrders: 0, todayAmountCent: 0, todayRefundCent: 0, pendingRefunds: 0, venues: 0 })
const venues = ref<Venue[]>([])
const auditLogs = ref<AuditLog[]>([])
const courts = ref<Court[]>([])
const bookings = ref<Booking[]>([])
const refunds = ref<Refund[]>([])
const matches = ref<MatchRoom[]>([])
const venueDrawerVisible = ref(false)
const bookingDrawerVisible = ref(false)
const refundDrawerVisible = ref(false)
const matchDrawerVisible = ref(false)
const selectedVenue = ref<Venue | null>(null)
const selectedBooking = ref<Booking | null>(null)
const selectedRefund = ref<Refund | null>(null)
const selectedMatch = ref<MatchRoom | null>(null)
const matchPlayers = ref<MatchPlayer[]>([])
const matchSharePayments = ref<SharePayment[]>([])
const matchShareRefunds = ref<ShareRefund[]>([])

const venueForm = reactive({ name: '新入驻羽毛球馆', sportTypes: 'BADMINTON', address: '测试地址', contactName: '联系人', contactPhone: '13800000000' })
const courtForm = reactive({ venueId: 1, name: '2号场', sportType: 'BADMINTON', indoor: true })
const priceForm = reactive({ venueId: 1, courtId: 1, dayType: 'WORKDAY', startTime: '08:00:00', endTime: '22:00:00', priceCent: 3000, priority: 1 })

const currentTitle = computed(() => ({ dashboard: '经营概览', venues: '场馆审核', courts: '场地价格', orders: '预约订单', refunds: '退款管理', matches: '约战管理', audit: '审计日志' }[activeView.value] ?? '经营后台'))
const metrics = computed(() => [
  { label: '今日订单', value: String(dashboard.value.todayOrders) },
  { label: '今日交易额', value: formatCent(dashboard.value.todayAmountCent) },
  { label: '今日退款', value: formatCent(dashboard.value.todayRefundCent) },
  { label: '入驻场馆', value: String(dashboard.value.venues) }
])

onMounted(async () => {
  await login('platform')
  await refreshCurrent()
})

function selectView(view: string) {
  activeView.value = view
  void refreshCurrent()
}

async function login(type: 'platform' | 'venue' | 'staff') {
  session.value = type === 'platform' ? await loginPlatform('admin', 'admin') : type === 'staff' ? await loginVenue('staff', 'staff') : await loginVenue('venue', 'venue')
  if (session.value.venueId) activeVenueId.value = session.value.venueId
  ElMessage.success(`${session.value.role} 已登录`)
}

async function refreshCurrent() {
  setActiveVenueId(activeVenueId.value)
  try {
    loadError.value = ''
    if (activeView.value === 'dashboard') await loadDashboard()
    if (activeView.value === 'venues') await loadVenues()
    if (activeView.value === 'courts') await loadCourts()
    if (activeView.value === 'orders') await loadBookings()
    if (activeView.value === 'refunds') await loadRefunds()
    if (activeView.value === 'matches') await loadMatches()
    if (activeView.value === 'audit') await loadAuditLogs(session.value?.role === 'PLATFORM_ADMIN' ? 'platform' : 'venue')
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载失败'
  }
}

async function loadDashboard() {
  dashboard.value = await fetchPlatformDashboard()
  venueDashboard.value = await fetchVenueDashboard(activeVenueId.value)
}
async function loadVenues() { venues.value = await listVenues() }
async function loadCourts() { courts.value = await listCourts(activeVenueId.value) }
async function loadBookings() { bookings.value = await listVenueBookings(activeVenueId.value) }
async function loadRefunds() { refunds.value = await listVenueRefunds(activeVenueId.value) }
async function loadMatches() { matches.value = await listMatches(activeVenueId.value) }
async function loadAuditLogs(scope: 'platform' | 'venue') { auditLogs.value = scope === 'platform' ? await listPlatformAuditLogs() : await listVenueAuditLogs(activeVenueId.value) }
function openVenueDetail(row: Venue) { selectedVenue.value = row; venueDrawerVisible.value = true }
function openBookingDetail(row: Booking) { selectedBooking.value = row; bookingDrawerVisible.value = true }
function openRefundDetail(row: Refund) { selectedRefund.value = row; refundDrawerVisible.value = true }
async function openMatchDetail(row: MatchRoom) {
  selectedMatch.value = row
  matchDrawerVisible.value = true
  const [players, payments, refunds] = await Promise.all([
    listMatchPlayers(row.id),
    listMatchSharePayments(row.id),
    listMatchShareRefunds(row.id)
  ])
  matchPlayers.value = players
  matchSharePayments.value = payments
  matchShareRefunds.value = refunds
}

async function submitVenue() { await createVenue({ ...venueForm }); ElMessage.success('场馆已提交'); await loadVenues() }
async function approve(id: number) { await approveVenue(id); ElMessage.success('已审核通过'); await loadVenues() }
async function submitCourt() { await createCourt({ ...courtForm }); ElMessage.success('场地已创建'); await loadCourts() }
async function submitPrice() { await createPriceRule({ ...priceForm }); ElMessage.success('价格已保存') }
async function checkIn(id: number) { await checkInBooking(activeVenueId.value, id); ElMessage.success('核销成功'); await loadBookings() }

function formatCent(value: number) { return `¥${(value / 100).toFixed(2)}` }
function formatDate(value: string) { return new Date(value).toLocaleString('zh-CN', { hour12: false }) }

</script>

<style scoped>
.layout { min-height: 100vh; background: #f6f8fa; }
.sidebar { background: #ffffff; border-right: 1px solid #e5e7eb; }
.brand { height: 56px; display: flex; align-items: center; padding: 0 20px; font-weight: 700; color: #111827; }
.menu { border-right: 0; }
.header { display: flex; align-items: center; justify-content: space-between; background: #ffffff; border-bottom: 1px solid #e5e7eb; }
.session-bar { display: flex; align-items: center; gap: 10px; }
.metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-bottom: 16px; }
.metric-label { color: #6b7280; font-size: 13px; }
.metric-value { margin-top: 8px; font-size: 24px; font-weight: 700; color: #111827; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.header-actions { display: flex; gap: 10px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.mb { margin-bottom: 16px; }
.drawer-actions { margin-top: 18px; display: flex; justify-content: flex-end; }
</style>





