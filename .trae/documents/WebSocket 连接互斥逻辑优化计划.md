我将修改 `WebSocketServiceImpl` 的 `online` 方法逻辑，确保同一用户的同一终端类型（如 WEB 端）只能有一个活跃连接。当检测到旧连接存在时，不仅更新映射，还要**主动关闭旧的 Channel**，防止产生“幽灵连接”。

### **具体修改计划**

1.  **修改 `online` 方法**:
    *   **位置**: `WebSocketServiceImpl.java` 中的 `online` 方法。
    *   **逻辑变更**:
        *   在 `ONLINE_UID_MAP` 中查找当前用户是否已有该终端类型的连接。
        *   如果存在旧连接 (`oldChannel`)：
            1.  从 `ONLINE_UID_MAP` 中移除该旧条目。
            2.  从 `ONLINE_CHANNEL_MAP` 中移除该旧 Channel。
            3.  **关键步骤**: 调用 `oldChannel.close()` 主动断开旧连接。
        *   将新连接添加到 `terminals` 列表。
        *   更新 `ONLINE_UID_MAP` 和 `ONLINE_CHANNEL_MAP`。

### **代码变更预览**

```java
// 原逻辑：只是替换了对象引用，没有关闭旧连接
if (oldChannel.size() > 0) {
    oldChannel.get(0).setChannel(channel);
}

// 新逻辑：主动关闭旧连接并清理
List<WSTerminalChanelExtraDTO> oldChannels = terminals.stream()
    .filter(terminal -> terminal.getTerminalType().equals(terminalType))
    .toList();

if (!oldChannels.isEmpty()) {
    for (WSTerminalChanelExtraDTO old : oldChannels) {
        // 1. 关闭旧的物理连接
        old.getChannel().close();
        // 2. 清理旧连接的映射关系
        ONLINE_CHANNEL_MAP.remove(old.getChannel());
        // 3. 从列表中移除
        terminals.remove(old);
    }
}
// 4. 添加新连接
terminals.add(new WSTerminalChanelExtraDTO(channel, terminalType));
```

这样修改后，每次用户重新连接（例如刷新页面或重新登录），后端都会强制清理掉之前的连接，确保系统状态的一致性。
