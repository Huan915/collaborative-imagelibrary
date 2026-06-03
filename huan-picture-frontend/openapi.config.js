//改为import即可
import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://localhost:8848/api/v2/api-docs', // 可以是.json文件，也可以是远程json地址
  serversPath: './src',
})
