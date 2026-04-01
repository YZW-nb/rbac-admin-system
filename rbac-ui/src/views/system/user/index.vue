<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 + 表格 -->
    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="flex-between">
          <span>用户列表</span>
          <div>
            <el-button type="primary" icon="Plus" v-permission="'sys:user:add'" @click="handleAdd">新增</el-button>
            <el-button type="danger" icon="Delete" v-permission="'sys:user:remove'" :disabled="!selectedIds.length" @click="handleBatchDelete">删除</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        empty-text="暂无数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="用户名" prop="username" width="120" />
        <el-table-column label="昵称" prop="nickname" width="120" />
        <el-table-column label="部门" prop="deptName" width="120" />
        <el-table-column label="手机号" prop="phone" width="130" />
        <el-table-column label="邮箱" prop="email" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              v-permission="'sys:user:edit'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" v-permission="'sys:user:edit'" @click="handleEdit(row)">修改</el-button>
            <el-button link type="primary" icon="Delete" v-permission="'sys:user:remove'" @click="handleDelete(row)" :disabled="row.id === 1">删除</el-button>
            <el-button link type="warning" icon="Key" v-permission="'sys:user:resetPwd'" @click="handleResetPwd(row)">重置</el-button>
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
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="!form.id">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-input v-model="form.deptId" placeholder="部门ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { getUserList, addUser, updateUser, deleteUsers, changeStatus, resetPassword } from '@/api/user'
import { getRoleList } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const roleOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = reactive({
  username: '',
  phone: '',
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: undefined,
  username: '',
  nickname: '',
  password: '',
  phone: '',
  email: '',
  deptId: undefined,
  status: 1,
  roleIds: []
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

function handleQuery() {
  loading.value = true
  getUserList(queryParams).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

function resetQuery() {
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleQuery()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function resetForm() {
  form.id = undefined
  form.username = ''
  form.nickname = ''
  form.password = ''
  form.phone = ''
  form.email = ''
  form.deptId = undefined
  form.status = 1
  form.roleIds = []
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
  loadRoles()
}

async function handleEdit(row) {
  resetForm()
  dialogTitle.value = '修改用户'
  dialogVisible.value = true
  await loadRoles()
  // 回显数据
  Object.assign(form, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    phone: row.phone,
    email: row.email,
    deptId: row.deptId,
    status: row.status,
    roleIds: row.roleIds || []
  })
}

async function loadRoles() {
  if (roleOptions.value.length > 0) return
  const res = await getRoleList({ pageNum: 1, pageSize: 100 })
  roleOptions.value = res.data.list || []
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateUser(form)
      ElMessage.success('修改成功')
    } else {
      await addUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除用户"${row.username}"吗？`, '提示', { type: 'warning' })
    .then(() => deleteUsers([row.id]))
    .then(() => {
      ElMessage.success('删除成功')
      handleQuery()
    })
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个用户吗？`, '提示', { type: 'warning' })
    .then(() => deleteUsers(selectedIds.value))
    .then(() => {
      ElMessage.success('删除成功')
      handleQuery()
    })
}

function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '停用'
  ElMessageBox.confirm(`确认${text}用户"${row.username}"吗？`, '提示', { type: 'warning' })
    .then(() => changeStatus(row.id, row.status))
    .then(() => ElMessage.success(`${text}成功`))
    .catch(() => { row.status = row.status === 1 ? 0 : 1 })
}

function handleResetPwd(row) {
  ElMessageBox.confirm(`确认重置用户"${row.username}"的密码吗？重置后将生成随机密码。`, '提示', { type: 'warning' })
    .then(() => resetPassword(row.id))
    .then((res) => {
      const newPassword = res.data || '已生成'
      ElMessageBox.alert(
        `用户"${row.username}"的密码已重置为新密码，请通知用户尽快修改密码。`,
        '密码重置成功',
        { type: 'success' }
      )
    })
}

onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
.search-card {
  :deep(.el-card__body) {
    padding-bottom: 2px;
  }
}
</style>
