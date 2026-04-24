import requests
from config import BASE_URL

def get(path: str, params: dict = None):
    try:
        r = requests.get(f"{BASE_URL}{path}", params=params, timeout=10)
        try:
            body = r.json()
        except Exception:
            return None, f"Invalid JSON response: {r.text}"
        if body.get("success"):
            return body.get("data"), None
        return None, body.get("message", "Unknown error")
    except requests.exceptions.ConnectionError:
        return None, "❌ Backend not running. Start Spring Boot first."
    except Exception as e:
        return None, str(e)

def post(path: str, payload: dict = None):
    try:
        r = requests.post(f"{BASE_URL}{path}", json=payload, timeout=15)
        try:
            body = r.json()
        except Exception:
            return None, f"Invalid JSON response: {r.text}"
        if body.get("success"):
            return body.get("data"), None
        return None, body.get("message", "Backend error")
    except requests.exceptions.ConnectionError:
        return None, "❌ Backend not running. Start Spring Boot first."
    except Exception as e:
        return None, str(e)

def patch(path: str, payload: dict = None):
    try:
        r = requests.patch(f"{BASE_URL}{path}", json=payload, timeout=10)
        try:
            body = r.json()
        except Exception:
            return None, "Invalid JSON response"
        if body.get("success"):
            return body.get("data"), None
        return None, body.get("message", "Unknown error")
    except requests.exceptions.ConnectionError:
        return None, "❌ Backend not running. Start Spring Boot first."
    except Exception as e:
        return None, str(e)

def safe_sync_nav():
    # Backend exposes POST /api/v1/market-events/funds/sync-nav (POST only, no GET)
    return post("/api/v1/market-events/funds/sync-nav")
