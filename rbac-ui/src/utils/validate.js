/**
 * 表单验证规则工具
 * 统一管理项目中的表单验证规则
 */

// 必填验证
export const validateRequired = (message = '此项为必填项') => ({
  required: true,
  message,
  trigger: ['blur', 'change']
})

// 用户名验证（字母、数字、下划线，4-16位）
export const validateUsername = (required = true) => ({
  required,
  min: 4,
  max: 16,
  pattern: /^[a-zA-Z0-9_]+$/,
  message: '用户名由字母、数字、下划线组成，4-16位',
  trigger: ['blur', 'change']
})

// 密码验证（至少8位，包含大小写字母和数字）
export const validatePassword = (required = true) => ({
  required,
  min: 8,
  max: 32,
  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,32}$/,
  message: '密码至少8位，包含大小写字母和数字',
  trigger: ['blur', 'change']
})

// 手机号验证
export const validatePhone = (required = false) => ({
  required,
  pattern: /^1[3-9]\d{9}$/,
  message: '请输入正确的手机号',
  trigger: ['blur', 'change']
})

// 邮箱验证
export const validateEmail = (required = false) => ({
  required,
  pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  message: '请输入正确的邮箱地址',
  trigger: ['blur', 'change']
})

// 身份证号验证
export const validateIdCard = (required = false) => ({
  required,
  pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
  message: '请输入正确的身份证号',
  trigger: ['blur', 'change']
})

// URL 验证
export const validateUrl = (required = false) => ({
  required,
  pattern: /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i,
  message: '请输入正确的 URL 地址',
  trigger: ['blur', 'change']
})

// IP 地址验证
export const validateIp = (required = false) => ({
  required,
  pattern: /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
  message: '请输入正确的 IP 地址',
  trigger: ['blur', 'change']
})

// 数字范围验证
export const validateNumberRange = (min, max, required = false) => ({
  required,
  min,
  max,
  message: `数值必须在 ${min} 到 ${max} 之间`,
  trigger: ['blur', 'change']
})

// 长度验证
export const validateLength = (min, max, required = false) => ({
  required,
  min,
  max,
  message: `长度必须在 ${min} 到 ${max} 个字符之间`,
  trigger: ['blur', 'change']
})

// 正整数验证
export const validatePositiveInt = (required = false) => ({
  required,
  pattern: /^[1-9]\d*$/,
  message: '请输入正整数',
  trigger: ['blur', 'change']
})

// 中文验证
export const validateChinese = (required = false) => ({
  required,
  pattern: /^[\u4e00-\u9fa5]+$/,
  message: '只能输入中文',
  trigger: ['blur', 'change']
})

// 邮政编码验证
export const validatePostalCode = (required = false) => ({
  required,
  pattern: /^[1-9]\d{5}$/,
  message: '请输入正确的邮政编码',
  trigger: ['blur', 'change']
})

// 自定义正则验证
export const validatePattern = (pattern, message, required = false) => ({
  required,
  pattern,
  message,
  trigger: ['blur', 'change']
})

// 异步验证示例（用于需要远程验证的场景）
export const createAsyncValidator = (validatorFn, message = '验证失败') => ({
  asyncValidator: (rule, value, callback) => {
    validatorFn(value).then(valid => {
      if (valid) {
        callback()
      } else {
        callback(new Error(message))
      }
    }).catch(() => {
      callback(new Error('验证出错'))
    })
  },
  trigger: ['blur', 'change']
})

// 通用表单验证规则
export const commonRules = {
  username: [
    validateUsername(true),
    { pattern: /^[a-zA-Z]/, message: '用户名必须以字母开头', trigger: ['blur', 'change'] }
  ],
  password: [
    validatePassword(true)
  ],
  phone: [
    validatePhone(false)
  ],
  email: [
    validateEmail(false)
  ],
  nickname: [
    { required: false, min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: ['blur', 'change'] }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 20, message: '菜单名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 30, message: '部门名称长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' },
    { pattern: /^[a-z_]+$/, message: '字典类型只能包含小写字母和下划线', trigger: 'blur' }
  ],
  dictLabel: [
    { required: true, message: '请输入字典标签', trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '请输入字典值', trigger: 'blur' }
  ],
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' }
  ],
  noticeTitle: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  noticeContent: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

// 表单重置辅助函数
export function resetForm(formRef) {
  if (formRef) {
    formRef.value?.resetFields()
  }
}

// 表单验证辅助函数
export function validateForm(formRef) {
  return new Promise((resolve, reject) => {
    if (formRef && formRef.value) {
      formRef.value.validate((valid) => {
        if (valid) {
          resolve()
        } else {
          reject(new Error('表单验证失败'))
        }
      })
    } else {
      resolve()
    }
  })
}

// 表单提交辅助函数（验证 + 提交）
export async function submitForm(formRef, submitFn) {
  if (!formRef || !formRef.value) {
    throw new Error('表单引用未定义')
  }

  try {
    await formRef.value.validate()
    await submitFn()
  } catch (error) {
    if (error instanceof Error && error.message !== '表单验证失败') {
      throw error
    }
  }
}
