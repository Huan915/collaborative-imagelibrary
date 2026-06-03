// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** sayHello GET /api/hello */
export async function sayHelloUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/api/hello', {
    method: 'GET',
    ...(options || {}),
  })
}
