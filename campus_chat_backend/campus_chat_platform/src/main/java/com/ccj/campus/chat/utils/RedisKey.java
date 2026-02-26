package com.ccj.campus.chat.utils;

/**
 * @Author ccj
 * @Date 2024-04-07 00:10
 * @Description
 */
public class RedisKey {

    public static final long LOGIN_TOKEN_EXPIRE_TIME = 7;

    public static final int PHONE_CODE_EXPIRE_TIME = 5;

    public static final int MESSAGE_EXPIRE_TIME = 3;
    public static final int TEMP_REPEAT_MESSAGE_TIME = 2;

    /**
     * 缓存前缀【项目名】【用来区分项目】
     */
    public static final String BASE_KEY = "campus-chat:";

    /**
     * 用户token的key, term为终端类型
     */
    public static final String USER_TOKEN = "userToken:uid_%d_term_%d";

    /**
     * 聊天室key
     */
    public static final String ROOM_INFO = "roomInfo:roomId_%d";

    /**
     * 群聊房间key
     */
    public static final String ROOM_GROUP_INFO = "roomGroup:roomGroupId_%d";

    /**
     * 单聊房间key
     */
    public static final String ROOM_FRIEND_INFO = "roomFriend:roomFriendId_%d";

    /**
     * 某个房间的所有成员key
     */
    public static final String GROUP_MEMBER = "groupMember:roomId_%d";


    /**
     * 某个房间的所有管理员key
     */
    public static final String GROUP_MANAGER = "groupManager:groupIdId_%d";

    /**
     * 消息id
     */
    public static final String MESSAGE_KEY = "message:msgId_%d";

    /**
     * 用户信息key
     */
    public static final String USER_INFO = "userInfo:uid_%d";

    /**
     * 用户背包key
     */
    public static final String USER_BACKPACK = "userBackpack:uid_%d";


    /**
     * 用户的信息更新时间
     */
    public static final String USER_MODIFY_TIME = "userModifyTime:uid_%d";

    /**
     * 用户的信息汇总
     */
    public static final String USER_SUMMERY = "userSummery:uid_%d";

    /**
     * 手机验证码
     */
    public static final String PHONE_CODE = "phoneCode:%s_%s";

    /**
     * 群id
     */
    public static final String ROOM_GROUP = "roomGroup:groupId_%d";

    public static final String FRIEND_KEY = "%d_friend_%d";

    public static final String GROUP_MEMBER_LOCK = "lock:groupMember:roomId_%d";

    /**
     * 用户暂存消息判断1分钟内是否重复发送消息了【单｜群聊】
     * roomId + "_" + timestamp + "_" + msgSeq + "_" + random;
     */
    public static final String TEMP_SINGLE_REPEAT_MESSAGE = "%d_%d_%d_%d";

    /**
     * 用户GPT聊天次数
     */

    public static final String TONG_YI_QIAN_WEN_CHAT_CONTEXT = "useChatGPTContext:uid_%d_roomId_%d";

    public static final String TONG_YI_QIAN_WEN_LIMIT_KEY = "limit:tongYiQianWen:uid_%d";

    public static final long TONG_YI_QIAN_WEN_CONTEXT_TT = 1L;

    public static final String CHECKIN_SESSION_CODE = "checkIn:code:sessionId_%d";

    public static final String CHECKIN_CODE_SESSION = "checkIn:code:%s";

    public static final String CHECKIN_CODE_SESSIONS = "checkIn:code:sessions:%s";

    public static final String CHECKIN_QR_TOKEN_SESSION = "checkIn:qr:token:%s";


    public static String getKey(String key, Object... o) {
        String format = String.format(key, o);
        return BASE_KEY + format;
    }


}
