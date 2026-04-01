import request from '@/utils/request'

export function getDeptList(params) {
  return request.get('/system/dept/list', { params })
}

export function getDeptDetail(id) {
  return request.get(`/system/dept/${id}`)
}

export function addDept(data) {
  return request.post('/system/dept', data)
}

export function updateDept(data) {
  return request.put('/system/dept', data)
}

export function deleteDept(id) {
  return request.delete(`/system/dept/${id}`)
}

export function getDeptTreeSelect() {
  return request.get('/system/dept/treeselect')
}
