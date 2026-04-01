import request from '@/utils/request'

export function getUserList(params) {
  return request.get('/system/user/list', { params })
}

export function getUserDetail(id) {
  return request.get(`/system/user/${id}`)
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUsers(ids) {
  return request.delete(`/system/user/${ids}`)
}

export function changeStatus(userId, status) {
  return request.put('/system/user/changeStatus', { userId, status })
}

export function resetPassword(userId) {
  return request.put(`/system/user/resetPwd/${userId}`)
}

export function changePassword(data) {
  return request.put('/system/user/changePassword', data)
}
