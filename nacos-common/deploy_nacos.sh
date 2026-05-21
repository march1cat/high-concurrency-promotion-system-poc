# 1. 將檔案內容讀入變數
CONFIG_CONTENT=$(cat $1)
IP=192.168.0.186
PORT=8848


# 2. 發送請求
curl -X POST "http://${IP}:${PORT}/nacos/v1/cs/configs" \
  --data-urlencode "content=${CONFIG_CONTENT}" \
  -d "dataId=$1" \
  -d "group=DEFAULT_GROUP" \
  -d "type=yaml"