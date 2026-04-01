<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="操作模块">
          <el-input v-model="queryParams.title" placeholder="请输入操作模块" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="正常" :value="0" />
            <el-option label="异常" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="flex-between">
          <span>操作日志</span>
          <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">清除选中</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无数据" @selection-change="sel => selectedIds = sel.map(i => i.id)">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="操作模块" prop="title" width="120" />
        <el-table-column label="请求方式" prop="requestMethod" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="methodTag(row.requestMethod)" size="small">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人员" prop="operName" width="100" />
        <el-table-column label="请求地址" prop="operUrl" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作IP" prop="operIp" width="130" />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="operTime" width="170" align="center" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="操作日志详情" v-model="detailVisible" width="640px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ current.title }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ current.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ current.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ current.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ current.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="方法名称" :span="2">{{ current.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="detail-pre">{{ current.operParam }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="current.status === 0 ? 'success' : 'danger'" size="small">
            {{ current.status === 0 ? '正常' : '异常' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ current.operTime }}</el-descriptions-item>
        <el-descriptions-item v-if="current.errorMsg" label="错误消息" :span="2">
          <pre class="detail-pre error-text">{{ current.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOperLogList, deleteOperLogs } from '@/api/log'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const detailVisible = ref(false)
const current = ref({})

const queryParams = reactive({ title: '', operName: '', status: undefined, pageNum: 1, pageSize: 10 })

function methodTag(method) {
  return { GET: 'info', POST: 'success', PUT: 'warning', DELETE: 'danger' }[method] || ''
}

function handleQuery() {
  loading.value = true
  getOperLogList(queryParams).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  Object.assign(queryParams, { title: '', operName: '', status: undefined, pageNum: 1 })
  handleQuery()
}

function handleDetail(row) {
  current.value = row
  detailVisible.value = true
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确认清除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', { type: 'warning' })
    .then(() => deleteOperLogs(selectedIds.value))
    .then(() => { ElMessage.success('清除成功'); handleQuery() })
}

onMounted(() => handleQuery())
</script>

<style lang="scss" scoped>
.detail-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  max-height: 200px;
  overflow-y: auto;
}
.error-text {
  color: #f56c6c;
}
</style>
