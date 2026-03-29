<!-- 高德地图组件，展示店铺位置并提供一键导航功能 -->
<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  longitude: { type: Number, default: null },
  latitude: { type: Number, default: null },
  name: { type: String, default: '' },
  address: { type: String, default: '' }
})

const mapContainer = ref(null)
const navigating = ref(false)
let map = null
let userMarker = null
let destMarker = null

const AMAP_KEY = 'bc8a0abeba7621e7558d042c7bdf0b58'
const AMAP_VERSION = '2.0'

function loadAmapScript() {
  return new Promise((resolve, reject) => {
    if (window.AMap) return resolve()
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=${AMAP_VERSION}&key=${AMAP_KEY}&callback=__amap_init__`
    script.onerror = reject
    window.__amap_init__ = () => { delete window.__amap_init__; resolve() }
    document.head.appendChild(script)
  })
}

async function initMap() {
  if (!mapContainer.value) return
  await loadAmapScript()

  const center = (props.longitude && props.latitude)
    ? [props.longitude, props.latitude]
    : [114.5700, 30.4350]

  map = new window.AMap.Map(mapContainer.value, {
    zoom: 16,
    center,
    mapStyle: 'amap://styles/fresh'
  })

  if (props.longitude && props.latitude) {
    destMarker = new window.AMap.Marker({
      position: [props.longitude, props.latitude],
      title: props.name,
      animation: 'AMAP_ANIMATION_DROP'
    })
    map.add(destMarker)

    const infoWindow = new window.AMap.InfoWindow({
      content: `<div style="padding:8px;min-width:160px">
        <div style="font-weight:bold;font-size:14px;margin-bottom:4px">${props.name}</div>
        <div style="color:#666;font-size:12px">${props.address}</div>
      </div>`,
      offset: new window.AMap.Pixel(0, -30)
    })
    infoWindow.open(map, destMarker.getPosition())
    destMarker.on('click', () => infoWindow.open(map, destMarker.getPosition()))
  }
}

// 获取用户当前位置（浏览器 Geolocation）
function getUserLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持定位'))
      return
    }
    navigator.geolocation.getCurrentPosition(
      pos => resolve({ lng: pos.coords.longitude, lat: pos.coords.latitude }),
      err => {
        const msgs = {
          1: '定位权限被拒绝，请在浏览器设置中允许定位',
          2: '无法获取位置信息',
          3: '定位超时，请重试'
        }
        reject(new Error(msgs[err.code] || '定位失败'))
      },
      { timeout: 8000, enableHighAccuracy: true }
    )
  })
}

// 在地图上显示用户位置并绘制路线
async function showUserOnMap(userLng, userLat) {
  if (!map) return
  if (userMarker) map.remove(userMarker)

  userMarker = new window.AMap.Marker({
    position: [userLng, userLat],
    title: '我的位置',
    icon: new window.AMap.Icon({
      size: new window.AMap.Size(24, 24),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
      imageSize: new window.AMap.Size(24, 24)
    })
  })
  map.add(userMarker)

  // 调整地图视野同时包含用户位置和店铺位置
  if (props.longitude && props.latitude && destMarker) {
    map.setFitView([userMarker, destMarker])
  } else {
    map.setFitView([userMarker])
  }
}

// 一键导航：先获取位置，再跳转高德导航（带起点+终点）
async function navigate() {
  if (!props.longitude || !props.latitude) return
  navigating.value = true
  try {
    const { lng, lat } = await getUserLocation()

    // 在地图上标记用户位置
    await showUserOnMap(lng, lat)

    // 构造高德导航 URI，包含起点（用户位置）和终点（店铺）
    const from = `${lng},${lat},${encodeURIComponent('我的位置')}`
    const to = `${props.longitude},${props.latitude},${encodeURIComponent(props.name)}`
    const url = `https://uri.amap.com/navigation?from=${from}&to=${to}&mode=walk&callnative=1`
    window.open(url, '_blank')
  } catch (e) {
    ElMessage.warning(e.message || '定位失败，将使用默认起点导航')
    // 定位失败时降级：不带起点，让高德自动定位
    const url = `https://uri.amap.com/navigation?to=${props.longitude},${props.latitude},${encodeURIComponent(props.name)}&mode=walk&callnative=1`
    window.open(url, '_blank')
  } finally {
    navigating.value = false
  }
}

// 在高德地图网页版查看
function openInAmap() {
  if (!props.longitude || !props.latitude) return
  const url = `https://uri.amap.com/marker?position=${props.longitude},${props.latitude}&name=${encodeURIComponent(props.name)}&callnative=1`
  window.open(url, '_blank')
}

onMounted(initMap)

watch(() => [props.longitude, props.latitude], () => {
  if (map) { map.destroy(); map = null }
  destMarker = null
  initMap()
})

onUnmounted(() => {
  if (map) { map.destroy(); map = null }
  destMarker = null
})
</script>

<template>
  <div class="shop-map-wrapper">
    <div ref="mapContainer" class="map-container" />
    <div class="map-actions">
      <el-button type="primary" :loading="navigating" @click="navigate" :disabled="!longitude || !latitude">
        {{ navigating ? '定位中...' : '一键导航' }}
      </el-button>
      <el-button @click="openInAmap" :disabled="!longitude || !latitude">
        在高德地图中查看
      </el-button>
      <span v-if="!longitude || !latitude" class="no-location-tip">
        该店铺暂未设置坐标
      </span>
    </div>
  </div>
</template>

<style scoped>
.shop-map-wrapper {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.map-container {
  width: 100%;
  height: 300px;
  background: #f5f5f5;
}

.map-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.no-location-tip {
  color: #999;
  font-size: 12px;
}
</style>
