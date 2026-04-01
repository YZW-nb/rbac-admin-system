import request from '@/utils/request'

export function getDictTypeList(params) {
  return request.get('/system/dict/type/list', { params })
}

export function addDictType(data) {
  return request.post('/system/dict/type', data)
}

export function updateDictType(data) {
  return request.put('/system/dict/type', data)
}

export function deleteDictTypes(ids) {
  return request.delete(`/system/dict/type/${ids}`)
}

export function getDictDataList(params) {
  return request.get('/system/dict/data/list', { params })
}

export function addDictData(data) {
  return request.post('/system/dict/data', data)
}

export function updateDictData(data) {
  return request.put('/system/dict/data', data)
}

export function deleteDictData(ids) {
  return request.delete(`/system/dict/data/${ids}`)
}

export function getDictDataByType(dictType) {
  return request.get(`/system/dict/data/type/${dictType}`)
}
