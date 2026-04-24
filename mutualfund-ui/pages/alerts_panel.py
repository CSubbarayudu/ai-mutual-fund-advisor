import streamlit as st
import time
from api.client import get, patch

GLOBAL_CSS = """
<style>
  .stApp { background-color: #f5f7fa; color: #1a1a2e; }
  .stMarkdown, .stText, p, span, label { color: #1a1a2e !important; }
  [data-testid="stSidebar"] { background-color: #1a1a2e; }
  [data-testid="stSidebar"] * { color: #ffffff !important; }
  .stButton button { background-color: #0066cc; color: white; border-radius: 8px; border: none; font-weight: 600; }
  .stButton button:hover { background-color: #004a9f; }
  [data-testid="stMetricValue"] { color: #1a1a2e !important; }
</style>
"""

SEVERITY_COLORS = {
    "CRITICAL": ("#fff0f0", "#cc0000"),
    "HIGH":     ("#fff0f0", "#cc0000"),
    "WARNING":  ("#fff8e1", "#ff8800"),
    "INFO":     ("#e8f4fd", "#0066cc"),
}


def render(user_id: int):
    st.markdown(GLOBAL_CSS, unsafe_allow_html=True)
    st.title("🔔 Alerts Panel")
    tab1, tab2 = st.tabs(["🔴 Unread Alerts", "📋 All Alerts"])

    with tab1:
        try:
            data, err = get(f"/api/v1/alerts/users/{user_id}/unread")
        except Exception as e:
            st.error(f"Error: {str(e)}")
            st.stop()
            return

        if err:
            st.error(err)
        elif not data:
            st.success("✅ No unread alerts! Portfolio is healthy.")
        else:
            alerts = data if isinstance(data, list) else []
            st.markdown(f"### {len(alerts)} Unread Alert(s) for User #{user_id}")
            for alert in alerts:
                severity = alert.get("severity", "INFO")
                bg, border = SEVERITY_COLORS.get(severity, ("#f9f9f9", "#999"))
                created = alert.get("createdAt", "")
                st.markdown(
                    f'<div style="background:{bg}; border-left:5px solid {border};'
                    f' border-radius:8px; padding:16px; margin-bottom:12px;">'
                    f'<strong>{alert.get("alertType", alert.get("fundName", "N/A"))}</strong>'
                    f'<span style="float:right; font-size:12px; color:#555;">{severity}</span>'
                    f'<p style="margin:8px 0 0 0; color:#1a1a2e;">{alert.get("alertMessage","")}</p>'
                    f'<small style="color:#555;">Event: {alert.get("eventTitle","")} | {created[:10] if created else "N/A"}</small>'
                    f'</div>',
                    unsafe_allow_html=True
                )
                if st.button(f"✅ Mark Read (Alert #{alert.get('alertId')})", key=f"read_{alert.get('alertId')}"):
                    _, perr = patch(f"/api/v1/alerts/{alert.get('alertId')}/users/{user_id}/mark-read")
                    if perr:
                        st.error(perr)
                    else:
                        st.success("Marked as read!")
                        time.sleep(1)
                        st.rerun()

    with tab2:
        try:
            data, err = get(f"/api/v1/alerts/users/{user_id}")
        except Exception as e:
            st.error(f"Error: {str(e)}")
            st.stop()
            return

        if err:
            st.error(err)
        else:
            alerts = data if isinstance(data, list) else []
            if not alerts:
                st.info("No alerts available.")
            else:
                for alert in alerts:
                    is_read = alert.get("isRead", False)
                    severity = alert.get("severity", "INFO")
                    with st.expander(f"{'✅' if is_read else '🔴'} {alert.get('fundName','N/A')} — {alert.get('alertType','')}"):
                        st.write(f"**Message:** {alert.get('alertMessage','')}")
                        st.write(f"**Event:** {alert.get('eventTitle','N/A')}")
                        st.write(f"**Severity:** {severity}")
                        st.write(f"**Read:** {'Yes' if is_read else 'No'}")
                        created = alert.get("createdAt", "")
                        st.write(f"**Created:** {created[:10] if created else 'N/A'}")
