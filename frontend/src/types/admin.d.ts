// 后台管理端类型(阶段 A1 起):与后端 /api/admin/** 响应一一对应

/** 管理端权限探针响应(GET /api/admin/ping) */
export interface AdminPing {
  /** 当前管理员 ID(后端 Long 经全局序列化为字符串) */
  userId: string
  /** 角色:能拿到响应即为 admin */
  role: 'student' | 'admin'
  /** 服务器当前时间(ISO 本地日期时间) */
  serverTime: string
}
