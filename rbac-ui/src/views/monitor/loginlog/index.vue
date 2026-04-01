<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="登录IP">
          <el-input v-model="queryParams.ip" placeholder="请输入登录IP" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
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
          <span>登录日志</span>
          <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">清除选中</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无数据" @selection-change="sel => selectedIds = sel.map(i => i.id)">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="用户名" prop="username" width="120" />
        <el-table-column label="登录IP" prop="ip" width="130" />
        <el-table-column label="登录地点" prop="location" width="120" />
        <el-table-column label="浏览器" prop="browser" width="130" show-overflow-tooltip />
        <el-table-column label="操作系统" prop="os" width="130" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提示消息" prop="message" min-width="150" show-overflow-tooltip />
        <el-table-column label="登录时间" prop="loginTime" width="170" align="center" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLoginLogList, deleteLoginLogs } from '@/api/log'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const queryParams = reactive({ username: '', ip: '', status: undefined, pageNum: 1, pageSize: 10 })

function handleQuery() {
  loading.value = true
  getLoginLogList(queryParams).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  Object.assign(queryParams, { username: '', ip: '', status: undefined, pageNum: 1 })
  handleQuery()
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确认清除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', { type: 'warning' })
    .then(() => deleteLoginLogs(selectedIds.value))
    .then(() => { ElMessage.success('清除成功'); handleQuery() })
}

onMounted(() => handleQuery())
</script>
