import json
import urllib.request
import urllib.error

BASE = "http://localhost:8080"

def call(method, path, body=None, token=None):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"code": e.code, "message": e.read().decode("utf-8")[:200]}
    except Exception as e:
        return {"code": -1, "message": str(e)}

def login(username, password):
    r = call("POST", "/api/auth/login", {"username": username, "password": password})
    return r

print("=" * 60)
print("[1] 管理员登录 admin")
r = login("admin", "admin123")
print("  code:", r.get("code"), "role:", r.get("data", {}).get("user", {}).get("role"))
admin = r["data"]["token"]

print("[2] 普通用户登录 user1")
r = login("user1", "user123")
print("  code:", r.get("code"), "nickname:", r.get("data", {}).get("user", {}).get("nickname"))
user1 = r["data"]["token"]

print("[3] 低信誉用户登录 user2")
r = login("user2", "user123")
print("  code:", r.get("code"), "信誉分:", r.get("data", {}).get("user", {}).get("reputationScore"))
user2 = r["data"]["token"]

print("=" * 60)
print("[4] 智能问答：推荐附近火锅店")
r = call("POST", "/api/agent/chat", {"question": "推荐附近的火锅店", "longitude": 116.4, "latitude": 39.9}, user1)
ans = r.get("data", {}).get("answer", r.get("message"))
print("  answer:", (ans or "").split("\n")[0][:120])

print("[5] 智能问答：全聚德的评价")
r = call("POST", "/api/agent/chat", {"question": "全聚德烤鸭店的评价怎么样"}, user1)
ans = r.get("data", {}).get("answer", r.get("message"))
print("  answer:", (ans or "").split("\n")[0][:120])

print("=" * 60)
print("[6] 发布正常动态（应通过）")
r = call("POST", "/api/posts", {"title": "周末探店", "content": "这家老北京铜锅涮肉真的很不错，羊肉新鲜", "images": []}, user1)
print("  status:", r.get("data", {}).get("status"), "(1=通过)")

print("[7] 发布敏感词动态（应拒绝）")
r = call("POST", "/api/posts", {"title": "测试", "content": "这里有赌博和色情的信息", "images": []}, user1)
print("  status:", r.get("data", {}).get("status"), "(2=拒绝)")

print("[8] 发布带图动态（图像审核未配置，应转人工）")
r = call("POST", "/api/posts", {"title": "带图", "content": "分享一张照片", "images": ["https://example.com/photo.jpg"]}, user1)
print("  status:", r.get("data", {}).get("status"), "(3=人工复审)")

print("[9] 低信誉用户发布动态（信誉<60，应转人工）")
r = call("POST", "/api/posts", {"title": "小强探店", "content": "这家店性价比很高", "images": []}, user2)
print("  status:", r.get("data", {}).get("status"), "(3=人工复审)")

print("[10] 提交商家评价（应通过）")
r = call("POST", "/api/reviews", {"merchantId": 1, "content": "味道正宗，服务好", "rating": 5, "images": []}, user1)
print("  status:", r.get("data", {}).get("status"), "(1=通过)")

print("=" * 60)
print("[11] 多条件商家搜索：评分>=4.5 且人均<=100")
r = call("GET", "/api/merchants?minRating=4.5&maxPrice=100&limit=5", token=user1)
merchants = r.get("data") or []
print("  结果数:", len(merchants), " 前3:", [m["name"] for m in merchants[:3]])

print("[12] 附近商家（按距离排序）")
r = call("GET", "/api/merchants?longitude=116.4&latitude=39.9&limit=3", token=user1)
for m in (r.get("data") or []):
    print("  ", m["name"], "距离", round(m.get("distanceKm") or 0, 2), "km")

print("=" * 60)
print("[13] 管理员查看待人工复审列表")
r = call("GET", "/api/admin/audits?status=MANUAL", token=admin)
records = r.get("data", [])
print("  待复审数:", len(records))
for a in records[:3]:
    print("   -", a["record"]["targetType"], "id=" + str(a["record"]["targetId"]), a["record"]["riskLevel"], "->", a["targetContent"][:30])

print("[14] 管理员人工复审第一条：通过")
if records:
    aid = records[0]["record"]["id"]
    r = call("POST", f"/api/admin/audits/{aid}/review", {"decision": "APPROVED", "comment": "人工确认无违规"}, admin)
    print("  code:", r.get("code"), "auditStatus:", r.get("data", {}).get("record", {}).get("auditStatus"))

print("[15] 非管理员访问审核工作台（应 403）")
r = call("GET", "/api/admin/audits?status=MANUAL", token=user1)
print("  code:", r.get("code"), "message:", r.get("message"))

print("=" * 60)
print("[16] 商家评价列表（已通过）")
r = call("GET", "/api/reviews?merchantId=1", token=user1)
print("  评价数:", len(r.get("data") or []))
print("SMOKE TEST DONE")
