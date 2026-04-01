import request from '@/utils/request'

export function getMenuList(params) {
  return request.get('/system/menu/list', { params })
}

export function getMenuDetail(id) {
  return request.get(`/system/menu/${id}`)
}

export function addMenu(data) {
  return request.post('/system/menu', data)
}

export function updateMenu(data) {
  return request.put('/system/menu', data)
}

export function deleteMenu(id) {
  return request.delete(`/system/menu/${id}`)
}

export function getMenuTreeSelect() {
  return request.get('/system/menu/treeselect')
}
