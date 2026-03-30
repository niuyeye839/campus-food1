# Implementation Plan: shop-cover-upload

## Overview

所有改动集中在 `campus-food-frontend/src/views/Merchant.vue`，分三步完成：
1. 在编辑弹窗中新增封面上传区域（状态 + UI + 文件校验逻辑）
2. 修改 `submitShop` 实现先上传图片再保存店铺
3. 在店铺基础资料面板展示封面图片

需要安装 `fast-check` 和 `vitest` 用于属性测试。

## Tasks

- [x] 1. 安装测试依赖并配置 Vitest
  - 在 `campus-food-frontend` 目录下安装 `vitest`、`@vue/test-utils`、`fast-check`
  - 在 `campus-food-frontend/vite.config.js` 中添加 `test` 配置块
  - _Requirements: 测试基础设施_

- [x] 2. 在 Merchant.vue 中新增封面上传状态与文件校验逻辑
  - [x] 2.1 新增响应式状态变量
    - 在 `<script setup>` 中添加 `coverFile`、`coverPreviewUrl`、`coverUploading`、`fileInputRef`
    - 在 `shopForm` 初始化和 `fetchShop` 中加入 `images` 字段
    - _Requirements: 1.1, 1.2, 1.3_
  - [x] 2.2 实现 `onFileChange` 文件校验函数
    - 校验文件格式（jpg/jpeg/png/webp），不合法时 `ElMessage.error` 并 return
    - 校验文件大小 ≤ 10MB，超出时 `ElMessage.error` 并清空选择
    - 合法时设置 `coverFile` 并用 `URL.createObjectURL` 生成 `coverPreviewUrl`
    - _Requirements: 2.1, 2.2, 2.3_
  - [ ]* 2.3 为文件校验逻辑编写属性测试
    - **Property 2: 超大文件被拒绝** — 生成 size > 10MB 的 File mock，验证 `coverFile` 保持 null
    - **Property 3: 合法文件生成预览** — 生成合法格式+合法大小的 File mock，验证 `coverFile` 非 null 且 `coverPreviewUrl` 非空
    - **Validates: Requirements 2.2, 2.3**

- [x] 3. 在编辑弹窗中添加封面上传 UI
  - [x] 3.1 在 `<el-form>` 末尾添加封面上传表单项
    - 添加 `<el-form-item label="封面图片">` 包含点击触发的上传区域
    - 有预览时显示 `<img>`，无预览时显示 Plus 图标 + 占位文字
    - 添加隐藏的 `<input type="file" ref="fileInputRef" accept=".jpg,.jpeg,.png,.webp">`
    - 实现 `triggerFileInput` 函数
    - _Requirements: 1.1, 1.2, 1.3, 2.1_
  - [x] 3.2 在弹窗关闭时重置封面状态
    - 监听 `shopDialogVisible` 变为 false 时，重置 `coverFile = null`、`coverPreviewUrl = ''`
    - 同时在 `openEditShop` 中初始化 `coverPreviewUrl = shopData.value?.images || ''`
    - _Requirements: 1.2, 1.3_
  - [ ]* 3.3 为弹窗预览初始化编写属性测试
    - **Property 1: 已有封面图片时弹窗预览初始化** — 生成随机非空 images URL，挂载组件，验证 `coverPreviewUrl` 等于 `shopData.images`
    - **Validates: Requirements 1.2**

- [x] 4. 实现 `uploadCover` 并修改 `submitShop`
  - [x] 4.1 实现 `uploadCover` 函数
    - 构造 `FormData`，调用 `upload.media(formData)`
    - 返回响应中 `data[0]` 的 URL；失败时抛出错误
    - _Requirements: 3.1, 3.2_
  - [x] 4.2 修改 `submitShop` 函数
    - 若 `coverFile` 不为 null，设置 `coverUploading = true`，`await uploadCover()`
    - 上传失败时 `ElMessage.error('封面图片上传失败，请重试')`，`coverUploading = false`，return
    - 上传成功后将 URL 写入 `shopForm.value.images`，再继续原有保存逻辑
    - 在 `finally` 中重置 `coverUploading = false`
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  - [ ]* 4.3 为上传与提交流程编写属性测试
    - **Property 4: 上传成功后 images 字段使用第一个 URL** — mock Upload_API 返回随机 URL 列表，验证 `shopForm.images = urls[0]`
    - **Property 5: 上传失败时终止保存** — mock Upload_API 抛出异常，验证 `shop.update` 未被调用
    - **Property 6: 上传中禁用确定按钮** — 设置 `coverUploading=true`，验证按钮 `loading` 属性为 true
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4**

- [x] 5. 检查点 — 确保所有测试通过
  - 确保所有测试通过，如有问题请向用户反馈。

- [x] 6. 在店铺基础资料面板展示封面图片
  - [x] 6.1 在 `<template v-if="shopData">` 块顶部添加封面展示区域
    - `v-if="shopData.images"` 时渲染 `<img>` 元素，`src` 绑定 `shopData.images`
    - 样式：`width: 100%`、`max-height: 240px`、`object-fit: cover`
    - `images` 为空或未设置时不渲染该区域
    - _Requirements: 4.1, 4.2, 4.3_
  - [ ]* 6.2 为封面展示逻辑编写属性测试
    - **Property 7: 封面图片展示与 images 字段一致** — 生成随机 `shopData`（images 有值/无值），验证渲染结果
    - **Property 8: 提交成功后展示最新封面** — mock 完整提交流程，验证 `shopData.images` 更新
    - **Validates: Requirements 4.1, 4.2, 4.3**

- [x] 7. 最终检查点 — 确保所有测试通过
  - 确保所有测试通过，ask the user if questions arise.

## Notes

- 标有 `*` 的子任务为可选项，可跳过以加快 MVP 交付
- 每个任务均引用了具体需求条款以保证可追溯性
- 属性测试使用 fast-check，每个属性最少运行 100 次迭代
- 后端无需任何修改，`ShopDTO.images` 和 `UploadController` 均已就绪
