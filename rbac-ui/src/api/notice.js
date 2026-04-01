import request from '@/utils/request'

export function getNoticeList(params) {
  return request.get('/system/notice/list', { params })
}

export function getNoticeDetail(id) {
  return request.get(`/system/notice/${id}`)
}

export function addNotice(data) {
  return request.post('/system/notice', data)
}

export function updateNotice(data) {
  return request.put('/system/notice', data)
}

export function deleteNotices(ids) {
  return request.delete(`/system/notice/${ids}`)
}
