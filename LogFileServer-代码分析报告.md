# LogFileServer 代码分析报告

## 一、已确认 Bug 清单

### Bug-1 [编译错误] OtherParser 引用不存在的外部包
**文件**：`fileparser/impl/OtherParser.kt` 第 3-5、30、65 行
引用 `com.example` 包的 `ServiceApplication` 和 `Utils`，当前项目不存在，编译失败。

### Bug-2 [运行时/严重] parserAndSaveFile 强转不安全
**文件**：`SaveAppFileServiceImpl.kt` 第 81-82 行
`pairResult.first as AppInfo`：上传图片/其他文件时，`onParser` 返回 `ImageFileModel`/`OtherFileModel`，强转抛 `ClassCastException`。

### Bug-3 [运行时/严重] Jenkins 上传接口参数顺序与签名不匹配
**文件**：`AppController.kt` 第 327-330 行 vs `SaveAppFileService.kt` 第 39-48 行
Controller 传 `buildType, versionName, versionCode`（第4/5/6位），Service 期望 `versionName, versionCode, buildType`。三字段值互换，文件名错误，静默数据损坏。

### Bug-4 [运行时/崩溃] queryLastTimeAndMaxVersionByPlatform SQL 执行失败
**文件**：`AppInfoMapper.kt` 第 50-63 行
**SQLite 实测报错**：`Parse error: sub-select returns 2 columns - expected 1`
最内层子查询 `SELECT bundle_id, max(version_name)` 返回 2 列，被 `version_name IN (...)` 引用，SQLite 要求 IN 子查询只能返回 1 列。**方法调用时直接抛 SQL 异常，完全不可用。**
即使修复列数错误仍不正确：`max(version_name)` 是字典序（实测 `"9.0" > "10.0"` 返回 `9.0`）；不同 bundleId 相同 version_name 会交叉匹配。
**推荐修复**：用 `version_code`（整数）代替 `version_name`（字符串）比较版本：
```sql
SELECT a.* FROM app_info a
JOIN (
    SELECT bundle_id, MAX(version_code) AS max_vc, MAX(update_time) AS max_ut
    FROM app_info WHERE platform = #{platform} GROUP BY bundle_id
) t ON a.bundle_id = t.bundle_id AND a.version_code = t.max_vc AND a.update_time = t.max_ut
WHERE a.platform = #{platform} ORDER BY a.update_time DESC
```

### Bug-5 [运行时/中等] AppInfoSqlProvider null 参数处理错误
**文件**：`AppInfoSqlProvider.kt` 第 28-30 行
`params["buildType"].toString()` 当值为 null 时返回 `"null"`（非空），`isNotEmpty()` 通过，SQL 错误添加条件。

### Bug-6 [运行时/中等] IpaParser 临时文件泄漏
**文件**：`IpaParser.kt` 第 60-141 行
清理操作不在 finally 块中，解析异常时临时文件不会被清理。

### Bug-7 [中等] Controller 用 NPE 表达业务错误
**文件**：`AppController.kt` 第 94、126、228 行
应改用 `IllegalArgumentException` 或自定义异常。

### Bug-8 [低] Utils 中 subSequence 结果被丢弃
**文件**：`Utils.kt` 第 71、86 行
`sb.subSequence(...)` 返回值未使用，错误消息末尾多出 "and"。

### Bug-9 [低] saveAppFile 重复判空
**文件**：`SaveAppFileServiceImpl.kt` 第 179-184 行
第 179 行 `isNullOrEmpty()` 已覆盖，第 182 行 `isEmpty()` 是死代码。

### Bug-10 [低] like 查询未转义通配符
**文件**：`AppInfosServiceImpl.kt` 第 49-53 行
MyBatis-Plus `like()` 用 `#{}` 参数化绑定（防注入安全），但不转义 LIKE 通配符。`%`、`_`、`\` 会被当作通配符解析，搜索 `_` 生成 `LIKE '%_%'` 返回全表。
**推荐修复**：`fun String.escapeLikeWildcards() = this.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")`

---

## 二、createParser 泛型改进方案

**当前问题**：`parserAndSaveFile` 将所有文件统一 `as AppInfo` 强转，但 APK/IPA 需版本校验+数据库持久化，图片/其他文件只需文件保存。

| 方案 | 思路 | 优点 | 缺点 |
|------|------|------|------|
| **sealed class** | `ParseResult` sealed 子类区分 App/Image/Other | 编译期类型安全，新增类型编译器强制处理 | 需改造返回类型 |
| **路由方法（推荐）** | 按文件类型分发到 `parseAndSaveAppFile` / `ImageFile` / `OtherFile` | 改动小，类型安全，逻辑独立 | 新增类型需加方法 |
| **运行时判断** | `val model = parser.onParser(p).first; if (model is AppInfo) {...}` | 改动最小 | 依赖运行时检查 |

---

## 三、关键文件路径

| 文件 | 路径 |
|------|------|
| BaseFileParser | `.../fileparser/BaseFileParser.kt` |
| ApkParser | `.../fileparser/impl/ApkParser.kt` |
| IpaParser | `.../fileparser/impl/IpaParser.kt` |
| ImagesParser | `.../fileparser/impl/ImagesParser.kt` |
| OtherParser | `.../fileparser/impl/OtherParser.kt` |
| FileModel | `.../entity/FileModel.kt` |
| AppInfo | `.../entity/appinfo/AppInfo.kt` |
| ImageFileModel | `.../entity/filemeta/ImageFileModel.kt` |
| OtherFileModel | `.../entity/filemeta/OtherFileModel.kt` |
| AppInfoMapper | `.../mappers/appinfo/AppInfoMapper.kt` |
| AppInfoSqlProvider | `.../mappers/appinfo/AppInfoSqlProvider.kt` |
| SaveAppFileServiceImpl | `.../services/appinfo/impl/SaveAppFileServiceImpl.kt` |
| AppInfosServiceImpl | `.../services/appinfo/impl/AppInfosServiceImpl.kt` |
| AppController | `.../controller/AppController.kt` |
| Utils | `.../utils/Utils.kt` |

> 所有路径基于 `/Users/dingpeihua/JavaProjects/LogFileServer/src/main/kotlin/com/peihua8858/logfileserver/`
