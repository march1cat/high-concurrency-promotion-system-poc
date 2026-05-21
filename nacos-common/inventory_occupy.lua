-- KEYS[1]: 庫存的 Key (例如 "item:1001:stock")
-- ARGV[1]: 想要扣減的數量 (例如 2)

local stockKey = KEYS[1]
local quantityToSubtract = tonumber(ARGV[1])

-- 1. 獲取當前庫存量
local currentStock = redis.call('GET', stockKey)

-- 2. 檢查庫存是否存在
if not currentStock then
    return -1 -- 代表商品不存在
end

-- 3. 將文字轉為數字進行判斷
currentStock = tonumber(currentStock)

if currentStock < quantityToSubtract then
    return 0 -- 代表庫存不足，扣量失敗
else
    -- 4. 庫存足夠，進行扣減並寫回 Redis
    redis.call('DECRBY', stockKey, quantityToSubtract)
    return 1 -- 代表扣減成功
end