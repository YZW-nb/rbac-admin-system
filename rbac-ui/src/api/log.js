import request from '@/utils/request'

export function getOperLogList(params) {
  return request.get('/system/operlog/list', { params })
}

export function deleteOperLogs(ids) {
  return request.delete(`/system/operlog/${ids}`)
}

export function getLoginLogList(params) {
  return request.get('/system/loginlog/list', { params })
}

export function deleteLoginLogs(ids) {
  return request.delete(`/system/loginlog/${ids}`)
}
