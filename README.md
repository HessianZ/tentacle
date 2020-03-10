# README

### Request example
```json
{
  "version": "1.0",
  "session": {
    "is_new": false,
    "session_id": "553276583931288576_d580a91bcf684c6fb900ece681b88971",
    "application": {
      "app_id": "553276583931288576"
    },
    "user": {
      "user_id": "ScBdGDn1FeJuiB14YjC1nw==",
      "is_user_login": true,
      "gender": "unknown"
    }
  },
  "request": {
    "type": 0,
    "request_id": "745bce480c104155b4a945a8d703553f",
    "timestamp": 1583504218420,
    "intent": {
      "query": "启动电脑",
      "score": 0.800000011920929,
      "complete": true,
      "domain": "openplatform",
      "confidence": 1,
      "skillType": "Custom",
      "sub_domain": "1017191",
      "app_id": "553276583931288576",
      "request_type": "Start",
      "need_fetch_token": false,
      "slots": "{\"intent_name\":\"PowerOn\",\"slots\":[{\"name\":\"target\",\"value\":\"电脑\",\"raw_value\":\"电脑\"}]}"
    },
    "locale": "zh-CN",
    "slot_info": {
      "intent_name": "PowerOn",
      "slots": [
        {
          "name": "target",
          "value": "电脑",
          "raw_value": "电脑"
        }
      ]
    },
    "is_monitor": true
  },
  "query": "启动电脑",
  "context": {
    "device_id": "ooG6cAz4Df90nIq9/d7iNA==",
    "user_agent": "AHC/2.0",
    "device_category": "soundbox",
    "in_exp": false
  }
}
```

### Response Example

```json
{
  "is_session_end": false,
  "version": "1.0",
  "response": {
    "confidence": 0.8869365,
    "open_mic": true,
    "to_speak": {
      "type": 0,
      "text": "海浪的声音来咯"
    },
    "to_display": {
      "type": 0,
      "text": "海浪的声音来咯",
      "log_info": {}
    },
    "session_attributes": {
      "noticeFlag": 1,
      "replyKeyWord": "",
      "turn": 3,
      "miniSkillInfo": {
        "name": "LONGTAIL"
      },
      "session": {
        "sessionID": "f7579e8e-982e-4d4b-9387-980a3b09edef",
        "skillName": "",
        "skillSubName": "",
        "turn": 0,
        "data": [
          {
            "query": "海浪声",
            "reply": "海浪的声音来咯",
            "engine": "LONGTAIL"
          }
        ]
      },
      "longtailEngine": "SKILL",
      "replace": false,
      "latitude": 40.61924,
      "longtitude": 120.73009
    }
  }
}
```
