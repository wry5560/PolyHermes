package com.wrbug.polymarketbot.enums

/**
 * 错误码枚举
 * 按业务模块划分错误码范围
 * 
 * 错误码范围划分：
 * - 1001-1999: 参数错误 (Param Error)
 * - 2001-2999: 认证/权限错误 (Auth Error)
 * - 3001-3999: 资源不存在 (Not Found)
 * - 4001-4999: 业务逻辑错误 (Business Error)
 *   - 4001-4099: Leader 管理
 *   - 4101-4199: 模板管理
 *   - 4201-4299: 跟单管理
 *   - 4301-4399: 订单相关
 *   - 4401-4499: 市场相关
 *   - 4501-4599: 仓位相关
 * - 5001-5999: 服务器内部错误 (Server Error)
 */
enum class ErrorCode(
    val code: Int,
    val message: String
) {
    // ==================== 参数错误 (1001-1999) ====================
    PARAM_ERROR(1001, "参数错误"),
    PARAM_EMPTY(1002, "参数不能为空"),
    PARAM_INVALID(1003, "参数无效"),
    
    // 账户相关参数错误
    PARAM_PRIVATE_KEY_EMPTY(1101, "私钥不能为空"),
    PARAM_WALLET_ADDRESS_EMPTY(1102, "钱包地址不能为空"),
    PARAM_WALLET_ADDRESS_INVALID(1103, "钱包地址格式无效"),
    PARAM_ACCOUNT_ID_INVALID(1104, "账户ID无效"),
    PARAM_ACCOUNT_NAME_EMPTY(1105, "账户名称不能为空"),
    
    // Leader 相关参数错误
    PARAM_LEADER_ADDRESS_EMPTY(1201, "Leader 地址不能为空"),
    PARAM_LEADER_ADDRESS_INVALID(1202, "Leader 地址格式无效"),
    PARAM_LEADER_ID_INVALID(1203, "Leader ID 无效"),
    PARAM_LEADER_NAME_EMPTY(1204, "Leader 名称不能为空"),
    PARAM_CATEGORY_INVALID(1205, "分类无效，只支持 sports 或 crypto"),
    
    // 模板相关参数错误
    PARAM_TEMPLATE_NAME_EMPTY(1301, "模板名称不能为空"),
    PARAM_TEMPLATE_ID_INVALID(1302, "模板 ID 无效"),
    PARAM_COPY_MODE_INVALID(1303, "copyMode 必须是 RATIO 或 FIXED"),
    PARAM_COPY_RATIO_INVALID(1304, "跟单比例无效"),
    PARAM_FIXED_AMOUNT_INVALID(1305, "固定金额无效"),
    
    // 跟单相关参数错误
    PARAM_COPY_TRADING_ID_INVALID(1401, "跟单关系ID无效"),
    PARAM_ORDER_TYPE_INVALID(1402, "订单类型无效"),
    PARAM_ORDER_TYPE_MUST_BE_MARKET_OR_LIMIT(1403, "订单类型必须是MARKET或LIMIT"),
    PARAM_QUANTITY_EMPTY(1404, "数量不能为空"),
    PARAM_PRICE_EMPTY(1405, "限价订单必须提供价格"),
    PARAM_SIDE_EMPTY(1406, "方向不能为空"),
    PARAM_MARKET_ID_EMPTY(1407, "市场ID不能为空"),
    PARAM_ORDER_TYPE_EMPTY(1408, "订单类型不能为空"),
    
    // 市场相关参数错误
    PARAM_TOKEN_ID_EMPTY(1501, "tokenId 不能为空"),
    PARAM_CONDITION_ID_EMPTY(1502, "conditionId 不能为空"),
    PARAM_REDEEM_POSITIONS_EMPTY(1503, "赎回仓位列表不能为空"),
    PARAM_INDEX_SETS_INVALID(1504, "结果索引无效"),
    
    // 统计相关参数错误
    PARAM_ORDER_TYPE_INVALID_FOR_TRACKING(1601, "订单类型无效，必须是: buy, sell, matched"),
    
    // ==================== 认证/权限错误 (2001-2999) ====================
    AUTH_ERROR(2001, "认证失败"),
    AUTH_TOKEN_INVALID(2002, "认证令牌无效"),
    AUTH_TOKEN_EXPIRED(2003, "认证令牌已过期"),
    AUTH_PERMISSION_DENIED(2004, "权限不足"),
    AUTH_API_KEY_INVALID(2005, "API Key 无效"),
    AUTH_API_SECRET_INVALID(2006, "API Secret 无效"),
    AUTH_API_PASSPHRASE_INVALID(2007, "API Passphrase 无效"),
    AUTH_API_CREDENTIALS_MISSING(2008, "API 凭证未配置"),
    AUTH_USERNAME_OR_PASSWORD_ERROR(2009, "用户名或密码错误"),
    AUTH_RESET_KEY_INVALID(2010, "重置密钥错误"),
    AUTH_RESET_PASSWORD_RATE_LIMIT(2011, "频率限制：1分钟内最多尝试3次，请稍后再试"),
    AUTH_USER_NOT_FOUND(2012, "用户不存在"),
    AUTH_PASSWORD_WEAK(2013, "密码长度不符合要求，至少6位"),
    
    // ==================== 资源不存在 (3001-3999) ====================
    NOT_FOUND(3001, "资源不存在"),
    ACCOUNT_NOT_FOUND(3002, "账户不存在"),
    LEADER_NOT_FOUND(3003, "Leader 不存在"),
    TEMPLATE_NOT_FOUND(3004, "模板不存在"),
    COPY_TRADING_NOT_FOUND(3005, "跟单关系不存在"),
    MARKET_NOT_FOUND(3006, "市场不存在"),
    ORDER_NOT_FOUND(3007, "订单不存在"),
    POSITION_NOT_FOUND(3008, "仓位不存在"),
    
    // ==================== 业务逻辑错误 (4001-4999) ====================
    BUSINESS_ERROR(4001, "业务逻辑错误"),
    
    // Leader 管理 (4001-4099)
    LEADER_ALREADY_EXISTS(4001, "该 Leader 地址已存在"),
    LEADER_ADDRESS_SAME_AS_ACCOUNT(4002, "Leader 地址不能与自己的账户地址相同"),
    LEADER_HAS_COPY_TRADINGS(4003, "该 Leader 还有跟单关系，请先删除跟单关系"),
    
    // 模板管理 (4101-4199)
    TEMPLATE_NAME_ALREADY_EXISTS(4101, "模板名称已存在"),
    TEMPLATE_HAS_COPY_TRADINGS(4102, "该模板还有跟单关系在使用，请先删除跟单关系"),
    
    // 跟单管理 (4201-4299)
    COPY_TRADING_ALREADY_EXISTS(4201, "该跟单关系已存在"),
    COPY_TRADING_DISABLED(4202, "跟单关系已禁用"),
    COPY_TRADING_ENABLED(4203, "跟单关系已启用"),
    NO_ENABLED_COPY_TRADINGS(4204, "没有启用的跟单关系"),
    
    // 订单相关 (4301-4399)
    ORDER_CREATE_FAILED(4301, "创建订单失败"),
    ORDER_CANCEL_FAILED(4302, "取消订单失败"),
    ORDER_NOT_MATCHED(4303, "订单未匹配"),
    ORDER_ALREADY_FILLED(4304, "订单已成交"),
    ORDER_INSUFFICIENT_BALANCE(4305, "余额不足"),
    ORDER_AMOUNT_TOO_SMALL(4306, "订单金额低于最小限制"),
    ORDER_AMOUNT_TOO_LARGE(4307, "订单金额超过最大限制"),
    ORDER_PRICE_INVALID(4308, "订单价格无效"),
    ORDER_QUANTITY_INVALID(4309, "订单数量无效"),
    
    // 市场相关 (4401-4499)
    MARKET_PRICE_FETCH_FAILED(4401, "获取市场价格失败"),
    MARKET_ORDERBOOK_EMPTY(4402, "订单簿为空"),
    MARKET_TOKEN_ID_INVALID(4403, "Token ID 无效"),
    
    // 仓位相关 (4501-4599)
    POSITION_REDEEM_FAILED(4501, "赎回仓位失败"),
    POSITION_NOT_REDEEMABLE(4502, "仓位不可赎回"),
    POSITION_INSUFFICIENT(4503, "仓位不足"),
    POSITION_ALREADY_REDEEMED(4504, "仓位已赎回"),
    
    // 账户相关业务错误 (4601-4699)
    ACCOUNT_ALREADY_EXISTS(4601, "账户已存在"),
    ACCOUNT_IS_DEFAULT(4602, "账户已是默认账户"),
    ACCOUNT_HAS_ACTIVE_ORDERS(4603, "账户有活跃订单"),
    ACCOUNT_IS_LAST_ONE(4604, "不能删除最后一个账户"),
    ACCOUNT_API_KEY_CREATE_FAILED(4605, "自动获取 API Key 失败"),
    ACCOUNT_PROXY_ADDRESS_FETCH_FAILED(4606, "获取代理地址失败"),
    ACCOUNT_BALANCE_FETCH_FAILED(4607, "查询账户余额失败"),
    ACCOUNT_POSITIONS_FETCH_FAILED(4608, "查询仓位列表失败"),
    
    // 统计相关 (4701-4799)
    STATISTICS_FETCH_FAILED(4701, "获取统计信息失败"),
    ORDER_LIST_FETCH_FAILED(4702, "查询订单列表失败"),
    
    // ==================== 服务器内部错误 (5001-5999) ====================
    SERVER_ERROR(5001, "服务器内部错误"),
    SERVER_DATABASE_ERROR(5002, "数据库错误"),
    SERVER_NETWORK_ERROR(5003, "网络错误"),
    SERVER_TIMEOUT(5004, "请求超时"),
    SERVER_EXTERNAL_API_ERROR(5005, "外部API调用失败"),
    SERVER_RPC_ERROR(5006, "RPC调用失败"),
    SERVER_WEBSOCKET_ERROR(5007, "WebSocket连接错误"),
    SERVER_ENCRYPTION_ERROR(5008, "加密/解密错误"),
    SERVER_SIGNATURE_ERROR(5009, "签名错误"),
    
    // 账户服务错误 (5101-5199)
    SERVER_ACCOUNT_IMPORT_FAILED(5101, "导入账户失败"),
    SERVER_ACCOUNT_UPDATE_FAILED(5102, "更新账户失败"),
    SERVER_ACCOUNT_DELETE_FAILED(5103, "删除账户失败"),
    SERVER_ACCOUNT_LIST_FETCH_FAILED(5104, "查询账户列表失败"),
    SERVER_ACCOUNT_DETAIL_FETCH_FAILED(5105, "查询账户详情失败"),
    SERVER_ACCOUNT_BALANCE_FETCH_FAILED(5106, "查询账户余额失败"),
    SERVER_ACCOUNT_DEFAULT_SET_FAILED(5107, "设置默认账户失败"),
    SERVER_ACCOUNT_POSITIONS_FETCH_FAILED(5108, "查询仓位列表失败"),
    SERVER_ACCOUNT_ORDER_CREATE_FAILED(5109, "创建卖出订单失败"),
    SERVER_ACCOUNT_REDEEM_POSITIONS_FAILED(5110, "赎回仓位失败"),
    
    // Leader 服务错误 (5201-5299)
    SERVER_LEADER_ADD_FAILED(5201, "添加 Leader 失败"),
    SERVER_LEADER_UPDATE_FAILED(5202, "更新 Leader 失败"),
    SERVER_LEADER_DELETE_FAILED(5203, "删除 Leader 失败"),
    SERVER_LEADER_LIST_FETCH_FAILED(5204, "查询 Leader 列表失败"),
    SERVER_LEADER_DETAIL_FETCH_FAILED(5205, "查询 Leader 详情失败"),
    
    // 模板服务错误 (5301-5399)
    SERVER_TEMPLATE_CREATE_FAILED(5301, "创建模板失败"),
    SERVER_TEMPLATE_UPDATE_FAILED(5302, "更新模板失败"),
    SERVER_TEMPLATE_DELETE_FAILED(5303, "删除模板失败"),
    SERVER_TEMPLATE_COPY_FAILED(5304, "复制模板失败"),
    SERVER_TEMPLATE_LIST_FETCH_FAILED(5305, "查询模板列表失败"),
    SERVER_TEMPLATE_DETAIL_FETCH_FAILED(5306, "查询模板详情失败"),
    
    // 跟单服务错误 (5401-5499)
    SERVER_COPY_TRADING_CREATE_FAILED(5401, "创建跟单失败"),
    SERVER_COPY_TRADING_UPDATE_FAILED(5402, "更新跟单失败"),
    SERVER_COPY_TRADING_DELETE_FAILED(5403, "删除跟单失败"),
    SERVER_COPY_TRADING_LIST_FETCH_FAILED(5404, "查询跟单列表失败"),
    SERVER_COPY_TRADING_TEMPLATES_FETCH_FAILED(5405, "查询钱包绑定的模板失败"),
    
    // 市场服务错误 (5501-5599)
    SERVER_MARKET_PRICE_FETCH_FAILED(5501, "获取市场价格失败"),
    SERVER_MARKET_LATEST_PRICE_FETCH_FAILED(5502, "获取最新价失败"),
    
    // 统计服务错误 (5601-5699)
    SERVER_STATISTICS_FETCH_FAILED(5601, "获取统计信息失败"),
    SERVER_ORDER_TRACKING_LIST_FETCH_FAILED(5602, "查询订单列表失败"),
    
    // 区块链服务错误 (5701-5799)
    SERVER_BLOCKCHAIN_RPC_ERROR(5701, "区块链RPC调用失败"),
    SERVER_BLOCKCHAIN_PROXY_ADDRESS_FETCH_FAILED(5702, "获取代理地址失败"),
    SERVER_BLOCKCHAIN_BALANCE_FETCH_FAILED(5703, "查询余额失败"),
    SERVER_BLOCKCHAIN_POSITIONS_FETCH_FAILED(5704, "查询仓位失败"),
    SERVER_BLOCKCHAIN_REDEEM_FAILED(5705, "赎回仓位交易失败"),
    
    // WebSocket 服务错误 (5801-5899)
    SERVER_WEBSOCKET_CONNECTION_FAILED(5801, "WebSocket连接失败"),
    SERVER_WEBSOCKET_MESSAGE_SEND_FAILED(5802, "WebSocket消息发送失败"),
    SERVER_WEBSOCKET_SUBSCRIBE_FAILED(5803, "WebSocket订阅失败"),
    
    // 订单跟踪服务错误 (5901-5999)
    SERVER_ORDER_TRACKING_PROCESS_FAILED(5901, "处理订单跟踪失败"),
    SERVER_ORDER_TRACKING_BUY_FAILED(5902, "处理买入订单失败"),
    SERVER_ORDER_TRACKING_SELL_FAILED(5903, "处理卖出订单失败"),
    SERVER_ORDER_TRACKING_MATCH_FAILED(5904, "订单匹配失败");
    
    companion object {
        /**
         * 根据错误码查找枚举
         */
        fun fromCode(code: Int): ErrorCode? {
            return values().find { it.code == code }
        }
        
        /**
         * 根据错误码获取错误消息
         */
        fun getMessage(code: Int): String {
            return fromCode(code)?.message ?: "未知错误"
        }
    }
}

