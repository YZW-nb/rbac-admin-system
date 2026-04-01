<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
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

    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="flex-between">
          <span>角色列表</span>
          <div>
            <el-button type="primary" icon="Plus" v-permission="'sys:role:add'" @click="handleAdd">新增</el-button>
            <el-button type="danger" icon="Delete" v-permission="'sys:role:remove'" :disabled="!selectedIds.length" @click="handleBatchDelete">删除</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无数据" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="角色名称" prop="roleName" width="140" />
        <el-table-column label="角色编码" prop="roleCode" width="140" />
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
              v-permission="'sys:role:edit'" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" v-permission="'sys:role:edit'" @click="handleEdit(row)">修改</el-button>
            <el-button link type="primary" icon="Menu" v-permission="'sys:role:edit'" @click="handleAssignMenu(row)">分配菜单</el-button>
            <el-button link type="danger" icon="Delete" v-permission="'sys:role:remove'" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码（英文）" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog title="分配菜单权限" v-model="menuDialogVisible" width="480px" destroy-on-close>
      <el-scrollbar height="400px">
        <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedMenuIds"
          :props="{ label: 'menuName', children: 'children' }"
        />
      </el-scrollbar>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleMenuSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, getRoleDetail, addRole, updateRole, deleteRoles, changeRoleStatus, assignMenu } from '@/api/role'
import { getMenuList } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const menuDialogVisible = ref(false)
const menuTreeData = ref([])
const checkedMenuIds = ref([])
const currentRoleId = ref(null)
const formRef = ref()
const menuTreeRef = ref()

const queryParams = reactive({ roleName: '', roleCode: '', status: undefined, pageNum: 1, pageSize: 10 })

const form = reactive({ id: undefined, roleName: '', roleCode: '', sort: 1, status: 1, remark: '' })

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z_]+$/, message: '角色编码只能包含英文字母和下划线', trigger: 'blur' }
  ]
}

function handleQuery() {
  loading.value = true
  getRoleList(queryParams).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  Object.assign(queryParams, { roleName: '', roleCode: '', status: undefined, pageNum: 1 })
  handleQuery()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(i => i.id)
}

function resetForm() {
  Object.assign(form, { id: undefined, roleName: '', roleCode: '', sort: 1, status: 1, remark: '' })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '修改角色'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateRole(form)
      ElMessage.success('修改成功')
    } else {
      await addRole(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除角色"${row.roleName}"吗？`, '提示', { type: 'warning' })
    .then(() => deleteRoles([row.id]))
    .then(() => { ElMessage.success('删除成功'); handleQuery() })
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个角色吗？`, '提示', { type: 'warning' })
    .then(() => deleteRoles(selectedIds.value))
    .then(() => { ElMessage.success('删除成功'); handleQuery() })
}

function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '停用'
  ElMessageBox.confirm(`确认${text}角色"${row.roleName}"吗？`, '提示', { type: 'warning' })
    .then(() => changeRoleStatus(row.id, row.status))
    .then(() => ElMessage.success(`${text}成功`))
    .catch(() => { row.status = row.status === 1 ? 0 : 1 })
}

async function handleAssignMenu(row) {
  currentRoleId.value = row.id
  // 加载菜单树
  const res = await getMenuList({})
  menuTreeData.value = res.data || []
  // 加载角色已有菜单
  const menuRes = await getRoleDetail(row.id)
  checkedMenuIds.value = menuRes.data?.menuIds || []
  menuDialogVisible.value = true
}

async function handleMenuSubmit() {
  submitLoading.value = true
  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    await assignMenu(currentRoleId.value, [...checkedKeys, ...halfCheckedKeys])
    ElMessage.success('分配成功')
    menuDialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => handleQuery())
</script>
