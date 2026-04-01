import request from '@/utils/request'

export function getRoleList(params) {
  return request.get('/system/role/list', { params })
}

export function getRoleDetail(id) {
  return request.get(`/system/role/${id}`)
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRoles(ids) {
  return request.delete(`/system/role/${ids}`)
}

export function changeRoleStatus(roleId, status) {
  return request.put('/system/role/changeStatus', { roleId, status })
}

export function assignMenu(roleId, menuIds) {
  return request.put('/system/role/assignMenu', { roleId, menuIds })
}
