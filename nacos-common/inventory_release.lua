-- KEYS[1]: 庫存的 Key (例如 "item:1001:stock")
-- ARGV[1]: 想要加回的數量 (例如 2)

local stockKey = KEYS[1]
local quantityToAdd = tonumber(ARGV[1])

-- 1. 檢查庫存是否存在（避免憑空生出不存在的商品庫存）
local exists = redis.call('EXISTS', stockKey)

if exists == 0 then
    return -1 -- 代表商品不存在，拒絕加回
end

-- 2. 庫存存在，進行加回並寫回 Redis
redis.call('INCRBY', stockKey, quantityToAdd)
return 1 -- 代表加回成功