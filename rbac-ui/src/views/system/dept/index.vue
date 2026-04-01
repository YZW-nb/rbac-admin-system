<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="部门名称">
          <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
          <span>部门列表</span>
          <el-button type="primary" icon="Plus" v-permission="'sys:dept:add'" @click="handleAdd(null)">新增</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
        empty-text="暂无数据"
      >
        <el-table-column label="部门名称" prop="deptName" min-width="200" />
        <el-table-column label="负责人" prop="leader" width="120" />
        <el-table-column label="联系电话" prop="phone" width="130" />
        <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Plus" v-permission="'sys:dept:add'" @click="handleAdd(row)">新增子部门</el-button>
            <el-button link type="primary" icon="Edit" v-permission="'sys:dept:edit'" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" v-permission="'sys:dept:remove'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="选择上级部门（不选则为顶级）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" placeholder="请输入部门名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.leader" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { getDeptList, addDept, updateDept, deleteDept } from '@/api/dept'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const deptTreeOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = reactive({ deptName: '', status: undefined })
const form = reactive({ id: undefined, parentId: 0, deptName: '', sort: 1, leader: '', phone: '', email: '', status: 1 })
const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function handleQuery() {
  loading.value = true
  try {
    const res = await getDeptList(queryParams)
    tableData.value = res.data || []
    deptTreeOptions.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(queryParams, { deptName: '', status: undefined })
  handleQuery()
}

function resetForm() {
  Object.assign(form, { id: undefined, parentId: 0, deptName: '', sort: 1, leader: '', phone: '', email: '', status: 1 })
}

function handleAdd(parent) {
  resetForm()
  if (parent) form.parentId = parent.id
  dialogTitle.value = '新增部门'
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '修改部门'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateDept(form)
      ElMessage.success('修改成功')
    } else {
      await addDept(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除部门"${row.deptName}"吗？`, '提示', { type: 'warning' })
    .then(() => deleteDept(row.id))
    .then(() => { ElMessage.success('删除成功'); handleQuery() })
}

onMounted(() => handleQuery())
</script>
