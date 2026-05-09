# Android News 应用 Code Wiki

## 项目概述

这是一个基于 Android Java 原生开发的新闻应用案例，主要展示了 Android 开发中的核心功能和技术实现。

### 技术栈

| 类别 | 技术 |
|------|------|
| 开发语言 | Java |
| 最低 SDK | 24 (Android 7.0) |
| 目标 SDK | 35 |
| 编译 SDK | 35 |
| 网络请求 | OkHttp 4.11.0 |
| JSON 解析 | Gson 2.8.9 |
| 图片加载 | Glide 4.16.0 |
| 本地数据库 | SQLite |
| UI 组件 | Material Design Components |
| ViewPager2 | TabLayout + ViewPager2 滑动导航 |

---

## 项目架构

```
com.white.news/
├── Activity 层 (UI 控制器)
│   ├── MainActivity.java           # 主界面（新闻分类展示）
│   ├── LoginActivity.java          # 登录页面
│   ├── RegisterActivity.java       # 注册页面
│   ├── NewsDetailsActivity.java    # 新闻详情页
│   ├── HistoryListActivity.java    # 浏览历史页
│   ├── AboutActivity.java           # 关于页面
│   └── UpdatePwdActivity.java      # 修改密码页面
│
├── Fragment 层
│   └── TabNewsFragment.java        # 分类新闻列表 Fragment
│
├── Entity 层 (数据模型)
│   ├── UserInfo.java               # 用户信息实体
│   ├── NewsInfo.java               # 新闻信息实体
│   ├── HistoryInfo.java            # 历史记录实体
│   └── TitleInfo.java              # 分类标题实体
│
├── DB 层 (数据库操作)
│   ├── UserDbHelper.java          # 用户数据库操作类
│   └── HistoryDbHelper.java       # 历史记录数据库操作类
│
└── Adapter 层
    └── NewsListAdapter.java        # 新闻列表适配器
```

---

## 核心模块详解

### 1. Activity 模块

#### 1.1 MainActivity

**职责**: 应用主界面，负责展示新闻分类列表和抽屉导航。

**核心成员变量**:
```java
private List<TitleInfo> titles                    # 新闻分类标题列表
private TabLayout tab_layout                      # 顶部标签栏
private ViewPager2 viewPager                      # 页面切换器
private NavigationView nav_view                   # 侧边导航栏
private DrawerLayout drawer_layout                # 抽屉布局
```

**新闻分类配置**:
| 中文名称 | API 参数 | 说明 |
|----------|----------|------|
| 推荐 | top | 头条新闻 |
| 国内 | guonei | 国内新闻 |
| 国际 | guoji | 国际新闻 |
| 娱乐 | yule | 娱乐新闻 |
| 体育 | tiyu | 体育新闻 |
| 军事 | junshi | 军事新闻 |
| 科技 | keji | 科技新闻 |
| 财经 | caijing | 财经新闻 |
| 游戏 | youxi | 游戏新闻 |
| 汽车 | qiche | 汽车新闻 |
| 健康 | jiangkang | 健康新闻 |

**关键方法**:
- `onCreate()`: 初始化 TabLayout + ViewPager2 联动
- `onResume()`: 更新用户登录状态显示
- `onActivityResult()`: 处理密码修改后的回调

#### 1.2 LoginActivity

**职责**: 处理用户登录、记住密码和游客访问。

**核心逻辑**:
```
1. 从 SharedPreferences 读取保存的登录信息
2. 调用 UserDbHelper.login() 验证用户
3. 登录成功保存用户信息到 UserInfo 单例
4. 支持记住密码功能（使用 CheckBox）
```

**关键方法**:
- `login()`: 用户登录验证
- `visitorLogin()`: 游客登录（无需注册）

#### 1.3 RegisterActivity

**职责**: 处理新用户注册。

**关键方法**:
- `register()`: 调用 UserDbHelper.register() 创建新用户

#### 1.4 NewsDetailsActivity

**职责**: 展示新闻详情内容。

**核心逻辑**:
```
1. 接收 NewsInfo.ResultDTO.DataDTO 对象
2. 使用 WebView 加载新闻 URL
3. 将浏览记录保存到历史数据库
```

#### 1.5 HistoryListActivity

**职责**: 展示用户的浏览历史记录。

**核心逻辑**:
```
1. 从 HistoryDbHelper 查询历史记录
2. 使用 Gson 反序列化 JSON 数据
3. 通过 NewsListAdapter 展示列表
```

#### 1.6 UpdatePwdActivity

**职责**: 允许已登录用户修改密码。

**核心逻辑**:
```
1. 获取当前登录用户信息
2. 验证新密码和确认密码一致
3. 调用 UserDbHelper.updatePwd() 更新密码
```

---

### 2. Fragment 模块

#### 2.1 TabNewsFragment

**职责**: 各新闻分类的列表展示，通过 ViewPager2 滑动切换。

**核心成员变量**:
```java
private static final String ARG_PARAM = "title"    # 分类参数键
private String title                               # 当前分类标识
private String key = "226c6defa51c0f3fc7b9b0e9c7bfe78f"  # API Key
private String url = "http://v.juhe.cn/toutiao/index?key=..."  # API 地址
private Handler mHandler                           # UI 线程消息处理
private RecyclerView recyclerView                 # 新闻列表
private NewsListAdapter mNewsListAdapter          # 列表适配器
```

**网络请求流程**:
```
1. 创建 OkHttpClient 实例
2. 构建 Request 对象（URL = baseUrl + 分类参数）
3. Call.enqueue() 异步请求
4. 在 Callback.onResponse() 获取响应数据
5. 通过 Handler 切换到主线程
6. Gson 解析 JSON 为 NewsInfo 对象
7. 更新适配器数据
```

**关键方法**:
- `newInstance(String param)`: 创建 Fragment 实例，携带分类参数
- `getHttpData()`: 发起网络请求获取新闻数据
- `onActivityCreated()`: 初始化适配器并触发数据加载

---

### 3. Entity 模块

#### 3.1 UserInfo

**职责**: 用户信息数据模型（单例模式）。

```java
private int user_id          # 用户ID（主键）
private String username      # 用户名
private String nickname      # 昵称
private String password      # 密码

public static UserInfo sUserInfo  # 单例实例
public static UserInfo getsUserInfo()     # 获取单例
public static void setsUserInfo(UserInfo) # 设置单例
```

#### 3.2 NewsInfo

**职责**: 新闻数据模型，与聚合新闻 API 响应结构对应。

**嵌套结构**:
```
NewsInfo
├── String reason                    # 请求结果描述
├── int error_code                   # 错误码（0=成功）
├── ResultDTO result
│   ├── String stat                  # 状态
│   ├── List<DataDTO> data           # 新闻列表
│   ├── String page                   # 当前页
│   └── String pageSize               # 每页数量
│
└── ResultDTO.DataDTO (implements Serializable)
    ├── String uniquekey             # 唯一标识
    ├── String title                 # 新闻标题
    ├── String date                  # 发布日期
    ├── String category              # 分类
    ├── String author_name           # 作者/来源
    ├── String url                   # 详情页 URL
    ├── String thumbnail_pic_s       # 缩略图1
    ├── String thumbnail_pic_s02     # 缩略图2
    └── String thumbnail_pic_s03     # 缩略图3
```

#### 3.3 HistoryInfo

**职责**: 历史浏览记录数据模型。

```java
private int history_id       # 记录ID（主键）
private String uniquekey     # 新闻唯一标识
private String username      # 用户名（可为null表示游客）
private String new_json      # 新闻数据JSON
```

#### 3.4 TitleInfo

**职责**: 新闻分类标题数据模型。

```java
private String title         # 显示标题（中文）
private String py_title      # API参数（拼音）
```

---

### 4. Database 模块

#### 4.1 UserDbHelper

**职责**: 用户表（user_table）的数据库操作类。

**数据库配置**:
```
数据库名: user.db
版本号: 1
表名: user_table

字段:
- user_id INTEGER PRIMARY KEY AUTOINCREMENT
- username TEXT
- password TEXT
- nickname TEXT
```

**核心方法**:
| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getInstance(Context)` | UserDbHelper | 单例获取实例 |
| `register(username, password)` | int | 注册用户（返回新记录ID） |
| `login(username)` | UserInfo | 根据用户名查询用户 |
| `updatePwd(user_id, password)` | int | 修改用户密码 |
| `delete(user_id)` | int | 删除用户 |

#### 4.2 HistoryDbHelper

**职责**: 历史记录表（history_table）的数据库操作类。

**数据库配置**:
```
数据库名: history.db
版本号: 1
表名: history_table

字段:
- history_id INTEGER PRIMARY KEY AUTOINCREMENT
- username TEXT
- uniquekey TEXT
- new_json TEXT
```

**核心方法**:
| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getInstance(Context)` | HistoryDbHelper | 单例获取实例 |
| `addHistory(username, uniquekey, new_json)` | int | 添加历史记录 |
| `isHistory(username, uniquekey)` | boolean | 检查记录是否存在 |
| `queryHistoryListData(username)` | List<HistoryInfo> | 查询历史列表 |

---

### 5. Adapter 模块

#### 5.1 NewsListAdapter

**职责**: RecyclerView 适配器，用于展示新闻列表项。

**泛型**: `RecyclerView.Adapter<NewsListAdapter.MyHolder>`

**ViewHolder 组件**:
```
ImageView thumbnail_pic_s    # 新闻缩略图
TextView title              # 新闻标题
TextView author_name         # 来源/作者
TextView date               # 发布日期
```

**核心方法**:
| 方法 | 说明 |
|------|------|
| `setListData(List<DataDTO>)` | 设置列表数据并刷新 |
| `setmOnItemClickListener(onItemClickListener)` | 设置列表项点击监听 |

**接口回调**:
```java
public interface onItemClickListener {
    void onItemClick(NewsInfo.ResultDTO.DataDTO dataDTO, int position);
}
```

**图片加载**: 使用 Glide 加载缩略图，错误时显示 `R.mipmap.img_error`

---

## 依赖关系图

```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ TabNewsFrag  │  │NavigationView│  │    ViewPager2      │ │
│  │   ment       │  │             │  │                     │ │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬──────────┘ │
└─────────┼────────────────┼──────────────────────┼────────────┘
          │                │                      │
          ▼                ▼                      ▼
┌─────────────────┐ ┌─────────────┐      ┌──────────────────┐
│ TabNewsFragment │ │  Activity   │      │NewsListAdapter   │
│  ┌───────────┐  │ │             │      │                  │
│  │ OkHttp    │  │ │LoginActivity│      │ ┌──────────────┐ │
│  │           │  │ │RegisterAct  │◄─────┤ │  RecyclerView│ │
│  │ Gson      │  │ │HistoryList  │      │ └──────────────┘ │
│  └───────────┘  │ │UpdatePwdAct │      └──────────────────┘
│                 │ └──────┬──────┘
└────────┬────────┘        │
         │                 │
         ▼                 ▼
┌────────────────┐  ┌──────────────────┐
│   NewsInfo     │  │    UserInfo      │
│   (Gson解析)   │  │   (单例模式)      │
└────────────────┘  └────────┬─────────┘
                             │
                             ▼
                    ┌────────────────┐
                    │  UserDbHelper  │
                    │   (SQLite)     │
                    └────────────────┘
                            
┌────────────────────────────────────────────────────────────┐
│                   NewsDetailsActivity                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐│
│  │   WebView   │  │  HistoryDb  │  │       Gson          ││
│  │             │  │   Helper    │  │                     ││
│  └─────────────┘  └─────────────┘  └─────────────────────┘│
└────────────────────────────────────────────────────────────┘
```

---

## 运行方式

### 环境要求
- Android Studio Ladybug 或更高版本
- JDK 11+
- Android SDK 35
- Gradle 8.x

### 构建步骤

1. **打开项目**
   ```
   File → Open → 选择 /workspace 目录
   ```

2. **同步 Gradle**
   ```
   Android Studio 会自动同步依赖
   或执行: ./gradlew sync
   ```

3. **运行应用**
   ```
   点击 Run 按钮 或 Shift + F10
   选择模拟器或真机设备
   ```

### 网络配置

**API 信息**:
- 提供商: 聚合数据 (juhe.cn)
- API Key: `226c6defa51c0f3fc7b9b0e9c7bfe78f`
- 接口地址: `http://v.juhe.cn/toutiao/index`
- 请求方式: GET
- 参数: `key` (API密钥) + `type` (新闻分类)

**网络安全配置** (已在项目中配置):
```xml
<!-- res/xml/network_security_config.xml -->
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">v.juhe.cn</domain>
</domain-config>
```

**权限声明**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 数据流转

### 新闻获取流程
```
用户滑动Tab → TabNewsFragment → OkHttp异步请求 
    → API返回JSON → Gson解析 → NewsInfo对象 
    → NewsListAdapter.setListData() → RecyclerView展示
```

### 登录注册流程
```
用户输入信息 → LoginActivity/RegisterActivity 
    → UserDbHelper SQLite操作 → UserInfo单例存储 
    → 跳转MainActivity
```

### 浏览历史流程
```
用户点击新闻 → NewsDetailsActivity 
    → WebView加载URL → Gson转JSON 
    → HistoryDbHelper存储 → 历史页面查询展示
```

---

## 关键设计模式

### 1. 单例模式
- `UserDbHelper.getInstance(Context)`
- `HistoryDbHelper.getInstance(Context)`
- `UserInfo.sUserInfo` (静态单例)

### 2. 工厂模式
- `TabNewsFragment.newInstance(String param)` (Fragment 实例创建)

### 3. 适配器模式
- `NewsListAdapter` (RecyclerView 适配器)

### 4. MVC 模式
- **Model**: Entity 类 (UserInfo, NewsInfo, HistoryInfo)
- **View**: Layout XML 文件
- **Controller**: Activity/Fragment

---

## 注意事项

1. **Serializable 必要性**: `NewsInfo.ResultDTO.DataDTO` 必须实现 `Serializable` 接口才能通过 Intent 传递

2. **UI 线程限制**: 网络请求必须在子线程执行，UI 更新必须回到主线程（使用 Handler）

3. **数据库关闭**: 每次数据库操作后需调用 `db.close()` 释放资源

4. **Glide 使用**: 图片加载需传入 Context，建议使用 Activity/Fragment 级别的 Context 防止内存泄漏

5. **API Key**: 当前使用聚合数据免费 API，有调用频率限制，生产环境需申请正式 Key
