<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="公告标题">
          <el-input v-model="queryParams.title" placeholder="请输入公告标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="queryParams.noticeType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="正常" :value="1" />
            <el-option label="关闭" :value="0" />
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
          <span>公告列表</span>
          <div>
            <el-button type="primary" icon="Plus" v-permission="'sys:notice:add'" @click="handleAdd">新增</el-button>
            <el-button type="danger" icon="Delete" v-permission="'sys:notice:remove'" :disabled="!selectedIds.length" @click="handleBatchDelete">删除</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无数据" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="公告标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="公告类型" prop="noticeType" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 1 ? 'warning' : 'success'" size="small">
              {{ row.noticeType === 1 ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建者" prop="createBy" width="100" />
        <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" v-permission="'sys:notice:edit'" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" v-permission="'sys:notice:remove'" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio :value="1">通知</el-radio>
            <el-radio :value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getNoticeList, addNotice, updateNotice, deleteNotices } from '@/api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = reactive({ title: '', noticeType: undefined, status: undefined, pageNum: 1, pageSize: 10 })

const form = reactive({ id: undefined, title: '', noticeType: 1, status: 1, content: '' })

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

function handleQuery() {
  loading.value = true
  getNoticeList(queryParams).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  Object.assign(queryParams, { title: '', noticeType: undefined, status: undefined, pageNum: 1 })
  handleQuery()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(i => i.id)
}

function resetForm() {
  Object.assign(form, { id: undefined, title: '', noticeType: 1, status: 1, content: '' })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '修改公告'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateNotice(form)
      ElMessage.success('修改成功')
    } else {
      await addNotice(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除公告"${row.title}"吗？`, '提示', { type: 'warning' })
    .then(() => deleteNotices([row.id]))
    .then(() => { ElMessage.success('删除成功'); handleQuery() })
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条公告吗？`, '提示', { type: 'warning' })
    .then(() => deleteNotices(selectedIds.value))
    .then(() => { ElMessage.success('删除成功'); handleQuery() })
}

onMounted(() => handleQuery())
</script>
