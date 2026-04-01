<template>
  <div class="app-container">
    <!-- 字典类型列表 + 字典数据列表左右布局 -->
    <el-row :gutter="16">
      <!-- 左侧：字典类型 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="flex-between">
              <span>字典类型</span>
              <el-button type="primary" size="small" icon="Plus" v-permission="'sys:dict:add'" @click="handleAddType">新增</el-button>
            </div>
          </template>

          <el-input v-model="typeSearch" placeholder="搜索字典类型" clearable class="mb-16" />

          <el-table
            v-loading="typeLoading"
            :data="filteredTypeList"
            border
            stripe
            highlight-current-row
            @current-change="handleTypeSelect"
            size="small"
            empty-text="暂无数据"
          >
            <el-table-column label="字典名称" prop="dictName" min-width="120" show-overflow-tooltip />
            <el-table-column label="字典类型" prop="dictType" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click.stop="handleEditType(row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click.stop="handleDeleteType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：字典数据 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="flex-between">
              <span>字典数据{{ currentType ? `（${currentType.dictName}）` : '' }}</span>
              <el-button type="primary" size="small" icon="Plus" v-permission="'sys:dict:add'" :disabled="!currentType" @click="handleAddData">新增</el-button>
            </div>
          </template>

          <el-empty v-if="!currentType" description="请先选择左侧字典类型" />
          <el-table v-else v-loading="dataLoading" :data="dataList" border stripe size="small" empty-text="暂无数据">
            <el-table-column label="字典标签" prop="dictLabel" width="120" />
            <el-table-column label="字典值" prop="dictValue" width="100" />
            <el-table-column label="排序" prop="sort" width="70" align="center" />
            <el-table-column label="样式属性" prop="cssClass" width="100" />
            <el-table-column label="状态" prop="status" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="handleEditData(row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDeleteData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型弹窗 -->
    <el-dialog :title="typeDialogTitle" v-model="typeDialogVisible" width="480px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeFormRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="请输入字典类型（英文）" :disabled="!!typeForm.id" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleTypeSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog :title="dataDialogTitle" v-model="dataDialogVisible" width="480px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataFormRules" label-width="90px">
        <el-form-item label="字典类型">
          <el-input :value="currentType?.dictType" disabled />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="样式属性">
          <el-input v-model="dataForm.cssClass" placeholder="如：primary / success / warning" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleDataSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getDictTypeList, addDictType, updateDictType, deleteDictTypes, getDictDataList, addDictData, updateDictData, deleteDictData } from '@/api/dict'
import { ElMessage, ElMessageBox } from 'element-plus'

const typeLoading = ref(false)
const dataLoading = ref(false)
const submitLoading = ref(false)
const typeList = ref([])
const dataList = ref([])
const typeSearch = ref('')
const currentType = ref(null)

const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const typeFormRef = ref()
const dataFormRef = ref()

const typeForm = reactive({ id: undefined, dictName: '', dictType: '', status: 1, remark: '' })
const dataForm = reactive({ id: undefined, dictType: '', dictLabel: '', dictValue: '', sort: 1, cssClass: '', status: 1 })

const typeFormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}
const dataFormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

const filteredTypeList = computed(() => {
  if (!typeSearch.value) return typeList.value
  return typeList.value.filter(t =>
    t.dictName.includes(typeSearch.value) || t.dictType.includes(typeSearch.value)
  )
})

async function loadTypeList() {
  typeLoading.value = true
  try {
    const res = await getDictTypeList({ pageNum: 1, pageSize: 200 })
    typeList.value = res.data.list || []
  } finally {
    typeLoading.value = false
  }
}

async function handleTypeSelect(row) {
  if (!row) return
  currentType.value = row
  dataLoading.value = true
  try {
    const res = await getDictDataList({ dictType: row.dictType, pageNum: 1, pageSize: 200 })
    dataList.value = res.data.list || []
  } finally {
    dataLoading.value = false
  }
}

// 字典类型操作
function handleAddType() {
  Object.assign(typeForm, { id: undefined, dictName: '', dictType: '', status: 1, remark: '' })
  typeDialogTitle.value = '新增字典类型'
  typeDialogVisible.value = true
}

function handleEditType(row) {
  Object.assign(typeForm, row)
  typeDialogTitle.value = '修改字典类型'
  typeDialogVisible.value = true
}

async function handleTypeSubmit() {
  const valid = await typeFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (typeForm.id) {
      await updateDictType(typeForm)
    } else {
      await addDictType(typeForm)
    }
    ElMessage.success('操作成功')
    typeDialogVisible.value = false
    loadTypeList()
  } finally {
    submitLoading.value = false
  }
}

function handleDeleteType(row) {
  ElMessageBox.confirm(`确认删除字典类型"${row.dictName}"吗？`, '提示', { type: 'warning' })
    .then(() => deleteDictTypes([row.id]))
    .then(() => { ElMessage.success('删除成功'); loadTypeList() })
}

// 字典数据操作
function handleAddData() {
  Object.assign(dataForm, { id: undefined, dictType: currentType.value?.dictType, dictLabel: '', dictValue: '', sort: 1, cssClass: '', status: 1 })
  dataDialogTitle.value = '新增字典数据'
  dataDialogVisible.value = true
}

function handleEditData(row) {
  Object.assign(dataForm, row)
  dataDialogTitle.value = '修改字典数据'
  dataDialogVisible.value = true
}

async function handleDataSubmit() {
  const valid = await dataFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (dataForm.id) {
      await updateDictData(dataForm)
    } else {
      await addDictData(dataForm)
    }
    ElMessage.success('操作成功')
    dataDialogVisible.value = false
    handleTypeSelect(currentType.value)
  } finally {
    submitLoading.value = false
  }
}

function handleDeleteData(row) {
  ElMessageBox.confirm(`确认删除字典数据"${row.dictLabel}"吗？`, '提示', { type: 'warning' })
    .then(() => deleteDictData([row.id]))
    .then(() => { ElMessage.success('删除成功'); handleTypeSelect(currentType.value) })
}

onMounted(() => loadTypeList())
</script>
