# Java 版本切换器 (Java Switcher)

Linux 下图形化 Java 版本切换工具，扫描本地所有 Java 安装，一键切换。

## 使用方法

```bash
chmod +x java-switcher.sh
./java-switcher.sh
```

一键启动：自动编译（如需要）并打开 GUI。选中版本，点击「应用」即可。

## 功能

- 自动扫描 `/usr/lib/jvm`、sdkman candidates 等常见 Java 安装路径
- 单选框展示所有已安装版本，绿色 ● 标记当前版本
- 选中后自动更新 `~/.profile` 的 `JAVA_HOME` 和 sdkman `current` 符号链接
- 新增 Java 版本无需任何配置，下次打开自动识别

## 环境要求

- JDK 17+ 用于运行 GUI（自动从 sdkman 或 PATH 检测）
- Linux + Swing 图形环境

## 不需要

- 不需要 root 权限
- 看不懂英文也能用
