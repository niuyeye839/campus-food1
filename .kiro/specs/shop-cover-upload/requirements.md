# Requirements Document

## Introduction

在商家管理后台的"编辑店铺资料"弹窗中新增封面图片上传功能。商家可以上传或更换店铺封面图片，封面图片保存后将展示在店铺基础资料页面。

该功能复用已有的文件上传接口（`POST /api/upload/media`），将上传返回的图片 URL 存入 Shop 实体的 `images` 字段，并在前端店铺基础资料展示区域渲染封面图。

## Glossary

- **Cover_Uploader**：前端封面图片上传组件，嵌入"编辑店铺资料"弹窗中，负责图片选择、预览与上传。
- **Upload_API**：后端已有的文件上传接口，路径为 `POST /api/upload/media`，接受 `multipart/form-data` 格式，返回可访问的图片 URL 列表。
- **Shop_API**：后端店铺接口，路径为 `PUT /api/shops/{id}`，接受 `ShopDTO` 请求体，包含 `images` 字段用于存储封面图片 URL。
- **Shop_Info_Panel**：前端"店铺基础资料"标签页中展示店铺信息的区域。
- **ShopDTO**：后端店铺数据传输对象，包含 `images` 字段（字符串类型，存储图片 URL）。
- **Shop**：后端店铺实体，包含 `images` 字段（字符串类型，存储图片 URL）。

## Requirements

### Requirement 1：封面图片上传入口

**User Story:** 作为商家，我希望在"编辑店铺资料"弹窗中看到封面图片上传区域，以便我能够为店铺设置封面图片。

#### Acceptance Criteria

1. WHEN 商家打开"编辑店铺资料"弹窗，THE Cover_Uploader SHALL 在表单中展示封面图片上传区域。
2. WHEN 店铺已有封面图片，THE Cover_Uploader SHALL 在上传区域内展示当前封面图片的预览。
3. WHEN 店铺尚无封面图片，THE Cover_Uploader SHALL 展示默认的上传占位提示（图标 + "点击上传封面图"文字）。

---

### Requirement 2：封面图片选择与预览

**User Story:** 作为商家，我希望在上传前能预览所选图片，以便确认图片内容正确后再提交。

#### Acceptance Criteria

1. WHEN 商家点击上传区域，THE Cover_Uploader SHALL 打开系统文件选择对话框，且仅允许选择 jpg、jpeg、png、webp 格式的文件。
2. WHEN 商家选择的文件大小超过 10MB，THE Cover_Uploader SHALL 显示错误提示"图片大小不能超过 10MB"，并取消本次选择。
3. WHEN 商家选择合法图片文件，THE Cover_Uploader SHALL 在上传区域内立即展示该图片的本地预览，替换原有预览或占位提示。

---

### Requirement 3：封面图片上传至服务器

**User Story:** 作为商家，我希望点击"确定"提交表单时，封面图片能自动上传并与店铺资料一起保存，以便减少操作步骤。

#### Acceptance Criteria

1. WHEN 商家点击弹窗"确定"按钮且已选择新图片，THE Cover_Uploader SHALL 先调用 Upload_API 上传图片，再将返回的 URL 写入 `shopForm.images` 字段，然后调用 Shop_API 保存店铺资料。
2. WHEN Upload_API 返回成功，THE Cover_Uploader SHALL 将返回 URL 列表中的第一个 URL 作为封面图片 URL。
3. IF Upload_API 调用失败，THEN THE Cover_Uploader SHALL 显示错误提示"封面图片上传失败，请重试"，并终止后续的店铺资料保存操作。
4. WHILE 图片正在上传，THE Cover_Uploader SHALL 禁用"确定"按钮并显示加载状态，防止重复提交。

---

### Requirement 4：封面图片在店铺基础资料页面展示

**User Story:** 作为商家，我希望保存后能在店铺基础资料页面看到封面图片，以便确认图片已成功设置。

#### Acceptance Criteria

1. WHEN 店铺数据中 `images` 字段不为空，THE Shop_Info_Panel SHALL 展示封面图片，图片宽度为 100%，最大高度为 240px，使用 `object-fit: cover` 裁剪显示。
2. WHEN 店铺数据中 `images` 字段为空或未设置，THE Shop_Info_Panel SHALL 不渲染封面图片区域。
3. WHEN 商家成功提交编辑表单，THE Shop_Info_Panel SHALL 刷新并展示最新的封面图片。

---

### Requirement 5：后端 ShopDTO 支持封面图片字段

**User Story:** 作为开发者，我希望后端 ShopDTO 已包含 `images` 字段，以便前端能够通过现有的 `PUT /api/shops/{id}` 接口保存封面图片 URL。

#### Acceptance Criteria

1. THE ShopDTO SHALL 包含 `images` 字段（字符串类型），用于接收封面图片 URL。
2. WHEN Shop_API 收到包含 `images` 字段的请求，THE Shop_API SHALL 将 `images` 值持久化到数据库的 `shop.images` 列。
3. WHEN Shop_API 返回店铺数据，THE Shop_API SHALL 在响应体中包含 `images` 字段的当前值。
