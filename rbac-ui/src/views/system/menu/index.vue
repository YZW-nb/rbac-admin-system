<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
          <span>菜单列表</span>
          <el-button type="primary" icon="Plus" v-permission="'sys:menu:add'" @click="handleAdd(null)">新增</el-button>
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
        <el-table-column label="菜单名称" prop="menuName" min-width="180" />
        <el-table-column label="图标" prop="icon" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="menuType" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTag(row.menuType)" size="small">{{ menuTypeLabel(row.menuType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="权限标识" prop="perms" width="200" show-overflow-tooltip />
        <el-table-column label="组件路径" prop="component" width="200" show-overflow-tooltip />
        <el-table-column label="路由地址" prop="path" width="160" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Plus" v-permission="'sys:menu:add'" @click="handleAdd(row)">新增子项</el-button>
            <el-button link type="primary" icon="Edit" v-permission="'sys:menu:edit'" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" v-permission="'sys:menu:remove'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="选择上级菜单（不选则为顶级）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <template v-if="form.menuType !== 3">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="图标" prop="icon">
                <el-input v-model="form.icon" placeholder="Element Plus 图标名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="路由地址" prop="path">
                <el-input v-model="form.path" placeholder="如：/system/user" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="组件路径" v-if="form.menuType === 2">
            <el-input v-model="form.component" placeholder="如：system/user/index" />
          </el-form-item>
          <el-form-item label="是否可见">
            <el-radio-group v-model="form.visible">
              <el-radio :value="1">显示</el-radio>
              <el-radio :value="0">隐藏</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <el-form-item label="权限标识" v-if="form.menuType !== 1">
          <el-input v-model="form.perms" placeholder="如：sys:user:list" />
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
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const menuTreeOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = reactive({ menuName: '', status: undefined })
const form = reactive({
  id: undefined, parentId: 0, menuName: '', menuType: 1,
  icon: '', path: '', component: '', perms: '', sort: 1,
  visible: 1, status: 1
})
const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{
    required: true,
    validator: (rule, value, callback) => {
      // 目录和菜单类型必填路由地址
      if (form.menuType !== 3 && !value) {
        callback(new Error('请输入路由地址'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }],
  component: [{
    required: true,
    validator: (rule, value, callback) => {
      // 菜单类型必填组件路径
      if (form.menuType === 2 && !value) {
        callback(new Error('请输入组件路径'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }],
  perms: [{
    required: true,
    validator: (rule, value, callback) => {
      // 菜单和按钮类型必填权限标识
      if (form.menuType !== 1 && !value) {
        callback(new Error('请输入权限标识'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
}

function menuTypeLabel(type) {
  return { 1: '目录', 2: '菜单', 3: '按钮' }[type] || '-'
}
function menuTypeTag(type) {
  return { 1: '', 2: 'success', 3: 'warning' }[type] || ''
}

async function handleQuery() {
  loading.value = true
  try {
    const res = await getMenuList(queryParams)
    tableData.value = res.data || []
    menuTreeOptions.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(queryParams, { menuName: '', status: undefined })
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined, parentId: 0, menuName: '', menuType: 1,
    icon: '', path: '', component: '', perms: '', sort: 1,
    visible: 1, status: 1
  })
}

function handleAdd(parent) {
  resetForm()
  if (parent) form.parentId = parent.id
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '修改菜单'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateMenu(form)
      ElMessage.success('修改成功')
    } else {
      await addMenu(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除菜单"${row.menuName}"及其子菜单吗？`, '提示', { type: 'warning' })
    .then(() => deleteMenu(row.id))
    .then(() => { ElMessage.success('删除成功'); handleQuery() })
}

onMounted(() => handleQuery())
</script>
