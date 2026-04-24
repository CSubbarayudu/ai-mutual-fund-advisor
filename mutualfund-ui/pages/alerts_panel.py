import streamlit as st
import time
from api.client import get, patch

def render(user_id: int):
    st.title("🔔 Alerts Panel")
    tab1, tab2 = st.tabs(["🔴 Unread Alerts", "📋 All Alerts"])

    with tab1:
        data, err = get(f"/api/v1/alerts/users/{user_id}/unread")
        if err: st.error(err)
        elif not data: st.success("✅ No unread alerts! Portfolio is healthy.")
        else:
            alerts = data if isinstance(data, list) else []
            st.markdown(f"### {len(alerts)} Unread Alert(s) for User #{user_id}")
            for alert in alerts:
                severity = alert.get("severity", "INFO")
                css = "alert-warning" if severity == "WARNING" else "alert-high" if severity in ("HIGH","CRITICAL") else "alert-info"
                created = alert.get("createdAt","")
                st.markdown(
                    f'<div class="{css}"><b>{alert.get("fundName","N/A")}</b> — {alert.get("alertMessage","")}<br>'
                    f'<small>Event: {alert.get("eventTitle","")} | Severity: {severity} | {created[:10]}</small></div>',
                    unsafe_allow_html=True
                )
                if st.button(f"✅ Mark Read (Alert #{alert.get('alertId')})", key=f"read_{alert.get('alertId')}"):
                    _, perr = patch(f"/api/v1/alerts/{alert.get('alertId')}/users/{user_id}/mark-read")
                    if perr: st.error(perr)
                    else:
                        st.success("Marked as read!")
                        time.sleep(1)
                        st.rerun()

    with tab2:
        data, err = get(f"/api/v1/alerts/users/{user_id}")
        if err: st.error(err)
        else:
            alerts = data if isinstance(data, list) else []
            for alert in alerts:
                is_read = alert.get("isRead", False)
                with st.expander(f"{'✅' if is_read else '🔴'} {alert.get('fundName','N/A')} — {alert.get('alertType','')}"):
                    st.write(f"**Message:** {alert.get('alertMessage','')}")
                    st.write(f"**Event:** {alert.get('eventTitle','N/A')}")
                    st.write(f"**Severity:** {alert.get('severity','N/A')}")
                    st.write(f"**Read:** {'Yes' if is_read else 'No'}")
                    created = alert.get("createdAt","")
                    st.write(f"**Created:** {created[:10] if created else 'N/A'}")
