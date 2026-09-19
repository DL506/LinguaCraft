# 数据库脚本索引(backend/src/main/resources/db)

数据库:LinguaCraft 本地 MySQL 8.4.7,库名 `linguacraft`。

## 约定

- 结构脚本:`V<两位序号>__<描述>.sql`;数据脚本:`S<两位序号>__<描述>.sql`。
- **只增不改**:已交付的脚本禁止修改;后续任何结构变化(加字段、改类型、加索引、调整数据)均新建更高序号的 V/S 脚本,保证全历史可追溯、可复用。
- 序号即执行顺序;所有脚本要求幂等(建表 `IF NOT EXISTS`、种子 `ON DUPLICATE KEY UPDATE`),可重复执行。
- 编码统一 utf8mb4;时间列使用 `DATETIME`,应用时区 Asia/Shanghai。
- 每次新增脚本,同步在本文件的"索引"表中追加记录。

## 执行方式(PowerShell 必须使用长选项)

```powershell
# 建库脚本(V01,此时库尚不存在,不能传 --database)
Get-Content .\backend\src\main\resources\db\V01__create_database.sql -Raw |
  mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4

# 其余脚本(库名作为位置参数传入数据库)
Get-Content .\backend\src\main\resources\db\V02__create_user_auth_tables.sql -Raw |
  mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
```

> 注意:在 PowerShell 中,短选项 `-h127.0.0.1` 会导致主机名被错误解析为 `127`(报 `Unknown MySQL server host '127'`),必须使用长选项 `--host=` / `--user=` / `--password=`。

## 索引

| 编号 | 文件 | 内容 | 前置依赖 | 幂等 | 状态 |
|---|---|---|---|---|---|
| V01 | V01__create_database.sql | 创建数据库 linguacraft(utf8mb4) | MySQL 已启动 | 是 | 待执行 |

> 后续步骤(Step 2 起)新增的 V/S 脚本将在此表按序追加。