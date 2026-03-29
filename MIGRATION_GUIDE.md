# 注册功能修改说明

## 修改内容

已将注册功能从"学号"改为"电话号码 + 邮箱"，电话号码作为登录账号。

## 已修改的文件

### 前端 (campus-food-frontend)
1. `src/views/Register.vue` - 注册表单改为电话号码和邮箱
2. `src/views/Login.vue` - 登录提示支持电话号码
3. `src/views/Admin.vue` - 用户列表显示电话号码和邮箱

### 后端 (campus-food-platform)
1. `src/main/java/com/campus/foodplatform/dto/RegisterDTO.java` - 使用 phone 和 email 字段
2. `src/main/java/com/campus/foodplatform/entity/User.java` - 移除 studentId 字段
3. `src/main/java/com/campus/foodplatform/service/AuthService.java` - 注册和登录逻辑更新
4. `src/main/java/com/campus/foodplatform/mapper/UserMapper.java` - 添加 selectByPhone 方法
5. `src/main/resources/mapper/UserMapper.xml` - 搜索功能支持 phone
6. `src/main/resources/schema.sql` - 数据库表结构更新

## 数据库迁移

如果你已有现有数据库，请执行迁移脚本：
```bash
mysql -u root -p < campus-food-platform/src/main/resources/migration_remove_student_id.sql
```

或者重新创建数据库：
```bash
mysql -u root -p < campus-food-platform/src/main/resources/schema.sql
```

## 功能说明

- 注册时必填：电话号码（11位）、邮箱、姓名、密码
- 登录支持：电话号码、邮箱、用户名
- 电话号码格式验证：1[3-9]开头的11位数字
- 邮箱格式验证：标准邮箱格式
- 数据库唯一约束：phone、email、username

## 注意事项

- student_id 字段已完全移除
- phone 字段现在是必填且唯一的
- 商家注册仍使用统一社会信用代码
