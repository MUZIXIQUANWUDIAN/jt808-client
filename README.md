# jt808-client

JT/T 808 终端模拟与调试工具：支持 **JT808 客户端/服务端模拟**、**纯 TCP 客户端/服务端**、**报文解析**，并覆盖 JT1078、苏标主动安全等常见扩展场景。

## 功能概览

| 界面 Tab | 说明 |
|----------|------|
| JT808 客户端 | 连接平台，注册/鉴权/位置上报（0x0200 等）、视频与苏标附件等联动逻辑 |
| JT808 服务端 | 简易 TCP 服务端，接收终端报文并做基础应答演示 |
| JT808 报文解析 | 粘贴完整 7E…7E 十六进制报文，解析展示 |
| TCP 客户端 / TCP 服务端 | 与协议无关的字节流收发，便于联调非 808 通道 |
| 广告信息 | 广告相关配置界面 |

## 协议支持

| 协议名称 | 版本 | 支持 | 备注 |
|----------|------|------|------|
| JT/T 808 | 2011 | 是 | |
| JT/T 808 | 2013 | 是 | |
| JT/T 808 | 2019 | 是 | 终端号长度等与 2011/2013 不同 |
| JT/T 1078 | 2016 | 是 | 视频/回放等 |
| T/JSATL 12（苏标主动安全） | 2017 | 是 | |

## 运行环境

- **JDK**：1.8 及以上（与 `pom.xml` 中 `maven-compiler-plugin` 一致）
- **构建**：Apache Maven 3.x
- **配置**：首次运行前将项目根目录下的 **`config.example.properties`** 复制为 **`config.properties`**；界面中修改 IP、端口、终端号等后会写回 `config.properties`

## 编译与启动

```bash
mvn clean compile
```

**图形界面（推荐）**

- 主类：`com.lingx.jtools.ui.JttoolsFrame`
- IDE：直接运行上述类的 `main` 方法
- 命令行示例（需本机已配置 Maven 与依赖）：

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=com.lingx.jtools.ui.JttoolsFrame
```

在 **Windows PowerShell** 中若 `-Dexec.mainClass=...` 被错误解析，可直接用 IDE 运行 `JttoolsFrame`，或改用 **cmd** 执行上述命令。

**无界面命令行（可选）**

- 主类：`com.lingx.jt808.App`  
- 用途：脚本化连指定 IP/端口并触发上报（参数：`ip port tid`，可省略使用内置默认）

### 批量模拟模式（新增）

`App` 支持批量启动多个终端连接，便于压测 Traccar 写入与在线并发能力。

```bash
java -cp <classpath> com.lingx.jt808.App --batch <ip> <port> <startTid> <count> [intervalSec] [connectDelayMs] [version]
```

参数说明：

- `ip` / `port`：Traccar 协议端口（例如 `8800`）
- `startTid`：起始设备号（自动递增）
- `count`：模拟终端数量
- `intervalSec`：0x0200 上报间隔秒，默认 `15`
- `connectDelayMs`：每个终端启动间隔毫秒，默认 `30`
- `version`：可选，含 `2019` 字样时按 20 位终端号，否则按 12 位

示例：

```bash
java -cp "<classpath>" com.lingx.jt808.App --batch 121.40.187.223 8800 130000000000 500 30 20 jt808-2011
```

## 工程结构（简要）

| 包路径 | 作用 |
|--------|------|
| `com.lingx.jtools.ui` | Swing 界面、布局、配置读写 |
| `com.lingx.jt808` | 808 组包/解析、`JT808ClientContext` 会话、命令与消息处理 |
| `com.lingx.jt808.netty` | JT808 客户端 Netty 连接与解码 |
| `com.lingx.gps.netty` | 通用 TCP 服务端；非 808 客户端在 `gps.netty.nojt808` |

## 使用提示

- **JT808 客户端**连接前请确认：服务器 IP/端口、设备号、协议版本（2011/2013/2019）与平台一致。  
- **位置上报线程**：断线后线程会在等待重连间隔内休眠，避免空转占 CPU；停止客户端时请使用界面 **停止** 以释放线程与连接。  
- **0x8201 等应答**：若平台下发位置查询，需在 **「位置设置」** 中填写并保存经纬度、间隔等参数，避免配置为空导致解析异常。

## 代码仓库

- 当前 GitHub：<https://github.com/MUZIXIQUANWUDIAN/jt808-client>
- 上游 Gitee：<https://gitee.com/lingxcom/jt808-client>

## 界面预览

基于 Swing + FlatLaf，主窗口为多 Tab 工具集。

![index](readme/20250415145524.png "index.png")
