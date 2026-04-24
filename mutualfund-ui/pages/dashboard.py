import streamlit as st
import plotly.graph_objects as go
import pandas as pd
import time
from api.client import get, post

def render(investor_id: int, user_id: int):
    st.title("🏠 Investor Dashboard")

    col1, col2, col3 = st.columns([1, 1, 1])
    with col1: st.metric("Investor ID", f"#{investor_id}")
    with col2: st.metric("User ID", f"#{user_id}")
    with col3:
        if st.button("🔄 Trigger Scoring Cycle"):
            _, e = post("/api/v1/scoring/trigger-cycle")
            if e: st.error(e)
            else:
                st.success("Scoring cycle triggered!")
                time.sleep(1)
                st.rerun()

    st.markdown("---")

    # scoring endpoint returns a LIST of ScoringResultDto
    data, err = get(f"/api/v1/scoring/investor/{investor_id}")
    if err or not data:
        st.warning(err or "No scoring data found. Run scoring cycle first.")
        if st.button("🔄 Force Trigger Scoring Cycle"):
            _, e = post("/api/v1/scoring/trigger-cycle")
            if not e:
                st.success("Triggered! Reload page in 2 seconds.")
                time.sleep(2)
                st.rerun()
        return

    # data is a list of scoring rows
    rows = data if isinstance(data, list) else []
    if not rows:
        st.info("No recommendations yet. Run the scoring cycle.")
        return

    st.subheader("📊 Fund Recommendations")
    df = pd.DataFrame(rows)

    display_cols = ["fundName", "baseMatchScore", "marketAdjustedScore", "confidenceScore", "recommendationStatus"]
    existing = [c for c in display_cols if c in df.columns]
    st.dataframe(df[existing], use_container_width=True)

    if "fundName" in df.columns and "baseMatchScore" in df.columns:
        fig = go.Figure()
        fig.add_trace(go.Bar(
            name="Base Score",
            x=df["fundName"],
            y=df["baseMatchScore"],
            marker_color="#3b82f6"
        ))
        if "marketAdjustedScore" in df.columns:
            fig.add_trace(go.Bar(
                name="Market Adjusted Score",
                x=df["fundName"],
                y=df["marketAdjustedScore"],
                marker_color="#22c55e"
            ))
        fig.update_layout(
            barmode="group",
            title="Base Score vs Market Adjusted Score",
            paper_bgcolor="#0f172a",
            plot_bgcolor="#0f172a",
            font_color="#e2e8f0",
            xaxis_tickangle=-30,
            legend=dict(bgcolor="#1e293b")
        )
        st.plotly_chart(fig, use_container_width=True)

    # Unread alerts banner
    alerts, aerr = get(f"/api/v1/alerts/users/{user_id}/unread")
    if not aerr and alerts:
        st.markdown("---")
        st.subheader(f"🔔 {len(alerts)} Unread Alert(s) for User #{user_id}")
        for alert in alerts[:3]:
            severity = alert.get("severity", "INFO")
            css = "alert-warning" if severity == "WARNING" else "alert-high" if severity in ("HIGH", "CRITICAL") else "alert-info"
            st.markdown(
                f'<div class="{css}"><b>{alert.get("fundName","")}</b> — {alert.get("alertMessage","")}'
                f'<br><small>{alert.get("eventTitle","")} | {severity}</small></div>',
                unsafe_allow_html=True
            )
