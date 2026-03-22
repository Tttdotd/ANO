---
title: Default module
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# Default module

Base URLs:

* <a href="http://localhost:8080/api/v1">本地环境: http://localhost:8080/api/v1</a>

# Authentication

# Default

## POST 创建任务

POST /tasks

创建新任务

> Body 请求参数

```json
{
  "title": "string",
  "description": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 是 |none|
|» title|body|string| 是 |任务的简短标题，核心行动点|
|» description|body|string| 否 |任务的背景、目标或初始想法|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": "string",
    "title": "string",
    "state": 0,
    "createTime": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|object|true|none||none|
|»» id|string|true|none||任务id|
|»» title|string|true|none||任务标题|
|»» state|integer|true|none||任务状态|
|»» createTime|string|true|none||任务创建时间|
|» message|string|true|none||none|

## GET 查找任务

GET /tasks

查找任务

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|date|query|string| 否 |查询特定日期的任务 (格式：YYYY-MM-DD)|
|state|query|integer| 否 |按状态过滤|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": [
    {
      "id": "string",
      "title": "string",
      "description": "string",
      "state": 0,
      "version": 0,
      "createTime": "string"
    }
  ],
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|[[TaskDisplayVO](#schemataskdisplayvo)]|true|none||none|
|»» id|string|true|none||任务唯一标识|
|»» title|string|true|none||任务标题|
|»» description|string|true|none||任务描述|
|»» state|integer|true|none||任务状态|
|»» version|integer|true|none||版本号|
|»» createTime|string|true|none||任务创建时间|
|» message|string|true|none||none|

## POST 创建笔记

POST /notes

创建新笔记

> Body 请求参数

```json
{
  "task_id": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 是 |none|
|» task_id|body|string| 是 |关联任务ID|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": "string",
    "content": "string",
    "taskId": "string",
    "state": 0
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|object|true|none||none|
|»» id|string|true|none||笔记唯一标识|
|»» content|string|true|none||笔记内容|
|»» taskId|string|true|none||关联任务ID|
|»» state|integer|true|none||笔记状态|
|» message|string|true|none||none|

## GET 查看笔记

GET /notes

查看笔记

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|task_id|query|string| 是 |关联任务ID|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": "string",
    "content": "string",
    "taskId": "string",
    "state": 0
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|object|true|none||none|
|»» id|string|true|none||笔记唯一标识|
|»» content|string|true|none||笔记内容|
|»» taskId|string|true|none||关联任务ID|
|»» state|integer|true|none||笔记状态|
|» message|string|true|none||none|

## PUT 修改笔记

PUT /notes

修改笔记

> Body 请求参数

```json
{
  "id": "string",
  "content": "string",
  "state": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 是 |none|
|» id|body|string| 是 |笔记ID|
|» content|body|string| 是 |笔记内容|
|» state|body|integer| 是 |笔记状态|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": "string",
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|string|true|none||none|
|» message|string|true|none||none|

## POST 创建输出

POST /outputs

创建输出

> Body 请求参数

```json
{
  "taskId": "string",
  "platform": "string",
  "url": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 是 |none|
|» taskId|body|string| 是 |关联任务ID|
|» platform|body|string| 是 |发布平台名称|
|» url|body|string| 是 |输出url|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": "string",
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|string|true|none||none|
|» message|string|true|none||none|

## GET 查看输出

GET /outputs

查看任务输出

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|taskId|query|string| 否 |关联任务ID|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": "string",
    "url": "string",
    "state": 0,
    "platform": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» data|object|true|none||none|
|»» id|string|true|none||输出唯一标识|
|»» url|string|true|none||输出url|
|»» state|integer|true|none||输出状态|
|»» platform|string|true|none||输出平台|
|» message|string|true|none||none|

# 数据模型

<h2 id="tocS_Result">Result</h2>

<a id="schemaresult"></a>
<a id="schema_Result"></a>
<a id="tocSresult"></a>
<a id="tocsresult"></a>

```json
{
  "code": 0,
  "data": "string",
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|true|none||none|
|data|string|true|none||none|
|message|string|true|none||none|

<h2 id="tocS_TaskCreateDTO">TaskCreateDTO</h2>

<a id="schemataskcreatedto"></a>
<a id="schema_TaskCreateDTO"></a>
<a id="tocStaskcreatedto"></a>
<a id="tocstaskcreatedto"></a>

```json
{
  "title": "string",
  "description": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|title|string|true|none||任务的简短标题，核心行动点|
|description|string|false|none||任务的背景、目标或初始想法|

<h2 id="tocS_TaskCreateVO">TaskCreateVO</h2>

<a id="schemataskcreatevo"></a>
<a id="schema_TaskCreateVO"></a>
<a id="tocStaskcreatevo"></a>
<a id="tocstaskcreatevo"></a>

```json
{
  "id": "string",
  "title": "string",
  "state": 0,
  "createTime": "string"
}

```

创建任务返回VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|true|none||任务id|
|title|string|true|none||任务标题|
|state|integer|true|none||任务状态|
|createTime|string|true|none||任务创建时间|

<h2 id="tocS_TaskDisplayVO">TaskDisplayVO</h2>

<a id="schemataskdisplayvo"></a>
<a id="schema_TaskDisplayVO"></a>
<a id="tocStaskdisplayvo"></a>
<a id="tocstaskdisplayvo"></a>

```json
{
  "id": "string",
  "title": "string",
  "description": "string",
  "state": 0,
  "version": 0,
  "createTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|true|none||任务唯一标识|
|title|string|true|none||任务标题|
|description|string|true|none||任务描述|
|state|integer|true|none||任务状态|
|version|integer|true|none||版本号|
|createTime|string|true|none||任务创建时间|

<h2 id="tocS_NoteDisplayVO">NoteDisplayVO</h2>

<a id="schemanotedisplayvo"></a>
<a id="schema_NoteDisplayVO"></a>
<a id="tocSnotedisplayvo"></a>
<a id="tocsnotedisplayvo"></a>

```json
{
  "id": "string",
  "content": "string",
  "taskId": "string",
  "state": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|true|none||笔记唯一标识|
|content|string|true|none||笔记内容|
|taskId|string|true|none||关联任务ID|
|state|integer|true|none||笔记状态|

<h2 id="tocS_NoteUpdateDTO">NoteUpdateDTO</h2>

<a id="schemanoteupdatedto"></a>
<a id="schema_NoteUpdateDTO"></a>
<a id="tocSnoteupdatedto"></a>
<a id="tocsnoteupdatedto"></a>

```json
{
  "id": "string",
  "content": "string",
  "state": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|true|none||笔记ID|
|content|string|true|none||笔记内容|
|state|integer|true|none||笔记状态|

<h2 id="tocS_OutputCreateDTO">OutputCreateDTO</h2>

<a id="schemaoutputcreatedto"></a>
<a id="schema_OutputCreateDTO"></a>
<a id="tocSoutputcreatedto"></a>
<a id="tocsoutputcreatedto"></a>

```json
{
  "taskId": "string",
  "platform": "string",
  "url": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|taskId|string|true|none||关联任务ID|
|platform|string|true|none||发布平台名称|
|url|string|true|none||输出url|

<h2 id="tocS_OutputVO">OutputVO</h2>

<a id="schemaoutputvo"></a>
<a id="schema_OutputVO"></a>
<a id="tocSoutputvo"></a>
<a id="tocsoutputvo"></a>

```json
{
  "id": "string",
  "url": "string",
  "state": 0,
  "platform": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|true|none||输出唯一标识|
|url|string|true|none||输出url|
|state|integer|true|none||输出状态|
|platform|string|true|none||输出平台|

