我将修改 `CollectionInterceptor` 的 `preHandle` 方法，以修复退出登录时 `CollectionInterceptor` "未生效"（实际是因参数缺失导致报错）的问题。

### **问题分析**
您在前端发起 `/capi/users/logout` 请求时，可能没有传递 `Authorization` 或 `terminalType` 头，或者传递的值不符合要求。而 `CollectionInterceptor` 在处理请求时，**强制要求**这些头必须存在且格式正确（使用了 `AssertUtil` 进行强校验），否则会直接抛出异常，导致请求中断，看起来就像是拦截器“没生效”或者报错了。

### **解决方案**
对于 `/capi/users/logout` 接口，我们需要允许拦截器在缺少参数的情况下**也能通过**，或者进行特殊处理。但更合理的做法是让前端在调用 logout 接口时，**必须**带上 Token 和 terminalType，因为后端需要这些信息来识别是哪个用户、哪个终端要退出。

然而，如果您希望拦截器更加健壮，我们可以修改 `CollectionInterceptor`，使其对部分校验失败的情况更加宽容，或者明确捕获异常并返回更友好的错误信息。但根据您之前的问题（退出登录报 NPE），核心原因是 `logout` 接口被 `InterceptionConfig` 排除在外，导致 `RequestHolder` 为空。**现在您已经移除了排除配置**，如果现在还是有问题，极有可能是前端发送的请求头不完整。

**检查点**：
1.  前端 `request.js` 在发送 `/logout` 请求时，是否自动带上了 `Authorization` 和 `terminalType`？
2.  `CollectionInterceptor` 中的 `AssertUtil` 是否抛出了异常？

**计划**：
1.  **修改 `CollectionInterceptor`**: 将强校验逻辑改为**非阻塞式**或针对特定路径的宽松处理？不，最标准的做法是**保持强校验**，因为没有 Token 就无法知道是谁在退出。
2.  **关键修正**: 仔细检查 `getValidUid` 方法。如果 `tokenInRedis` 为空（用户已过期或未登录），它返回 `null`。
    *   在 `preHandle` 中：`Long uid = getValidUid(...)`。
    *   如果 `uid` 为 `null`，后续代码 `new RequestHolderInfo(uid, ...)` 会将 `null` 存入。
    *   但是，`RequestHolderInfo` 的构造函数可能允许 `uid` 为 null。
    *   **潜在隐患**: 如果 `uid` 为 null，`logout` 方法中的 `RequestHolder.get().getUid()` 依然会报空指针（如果 `RequestHolderInfo` 对象存在但 `uid` 字段为 null）。

**更正方案**：
在 `CollectionInterceptor` 中，如果 `getValidUid` 返回 `null`（说明 Token 无效或已过期），应该直接拦截请求并返回 401，而不是放行（导致后续 NPE）。或者，对于 `logout` 接口，如果 Token 无效，说明用户已经退出了，可以直接返回成功。

但为了解决您“拦截器没有生效”的疑惑（实际上是生效了但可能抛错了），我将优化 `CollectionInterceptor` 的逻辑：
1.  当 `uid` 为 `null` 时，直接返回 `false` (拦截请求)，并写入 HTTP 401 响应。这样前端能收到明确的“未登录”错误，而不是后端的 NPE。

**具体代码变更**：
在 `CollectionInterceptor.java` 中：
```java
Long uid = getValidUid(token, terminalType);
if (uid == null) {
    // Token 无效或过期，直接拦截，避免后续 Controller 报 NPE
    response.setStatus(401);
    return false; 
}
```
这样可以防止 `logout` 方法拿到 null uid。

**注意**: 您之前提到“退出登录时 CollectionInterceptor 没有生效”，可能是指您期望它拦截但没拦截，或者期望它通过但没通过。根据上下文，您之前是因为被排除导致 NPE。现在如果还有问题，很可能是因为 Token 校验失败导致 `uid` 为 null，然后放行了，结果 Controller 又报 NPE。

**最终计划**:
修改 `CollectionInterceptor`，增加对 `uid` 为空的判断，确保只有合法的请求才能进入 Controller。
