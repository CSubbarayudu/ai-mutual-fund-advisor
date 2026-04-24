import streamlit as st
import plotly.express as px
import pandas as pd
import time
from api.client import get, post

GLOBAL_CSS = """
<style>
  .stApp { background-color: #f5f7fa; color: #1a1a2e; }
  .stMarkdown, .stText, p, span, label { color: #1a1a2e !important; }
  .metric-card {
    background: #ffffff; border-radius: 12px; padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08); border-left: 4px solid #0066cc; margin-bottom: 12px;
  }
  [data-testid="stSidebar"] { background-color: #1a1a2e; }
  [data-testid="stSidebar"] * { color: #ffffff !important; }
  .stDataFrame { background: white; }
  .stButton button { background-color: #0066cc; color: white; border-radius: 8px; border: none; font-weight: 600; }
  .stButton button:hover { background-color: #004a9f; }
  [data-testid="stMetricValue"] { color: #1a1a2e !important; }
  [data-testid="stMetricLabel"] { color: #555 !important; }
</style>
"""


def render(investor_id: int, user_id: int):
    st.markdown(GLOBAL_CSS, unsafe_allow_html=True)
    st.title("📊 AI Investment Dashboard")

    col1, col2 = st.columns([3, 1])
    with col1:
        investor_id_input = st.number_input("Investor ID", min_value=1, value=int(investor_id), step=1, key="dash_inv_id")
    with col2:
        st.markdown("<br>", unsafe_allow_html=True)
        load_btn = st.button("Load Recommendations")

    col_a, col_b, _ = st.columns([1, 1, 2])
    with col_a:
        if st.button("🔄 Trigger Scoring Cycle"):
            with st.spinner("Triggering scoring cycle..."):
                _, e = post("/api/v1/scoring/trigger-cycle")
            if e:
                st.error(e)
            else:
                st.success("Scoring cycle triggered!")
                time.sleep(1)
                st.rerun()

    st.markdown("---")

    if load_btn or st.session_state.get("dash_loaded"):
        st.session_state["dash_loaded"] = True
        with st.spinner("Fetching recommendations..."):
            try:
                data, err = get(f"/api/v1/recommendations/{investor_id_input}")
            except Exception as e:
                st.error(f"Error: {str(e)}")
                st.stop()
                return

        if err:
            st.error(f"Failed: {err}")
            return

        if not data:
            st.info("No data available yet. Try triggering the scoring cycle first.")
            return

        rows = data if isinstance(data, list) else []
        if not rows:
            st.info("No recommendations yet. Run the scoring cycle.")
            return

        df = pd.DataFrame(rows)

        # KPI metrics row
        kpi1, kpi2, kpi3 = st.columns(3)
        with kpi1:
            st.metric("Total Funds", len(df))
        with kpi2:
            if 'marketAdjustedScore' in df.columns:
                st.metric("Top Score", round(float(df['marketAdjustedScore'].max()), 2))
        with kpi3:
            if 'marketAdjustedScore' in df.columns:
                st.metric("Avg Score", round(float(df['marketAdjustedScore'].mean()), 1))

        st.markdown("---")

        # Plotly bar chart
        if 'fundName' in df.columns and 'marketAdjustedScore' in df.columns:
            fig = px.bar(
                df.head(8),
                x='fundName',
                y='marketAdjustedScore',
                color='marketAdjustedScore',
                color_continuous_scale='Blues',
                title='Market Adjusted Scores by Fund',
                labels={'fundName': 'Fund', 'marketAdjustedScore': 'Score'}
            )
            fig.update_layout(
                plot_bgcolor='white',
                paper_bgcolor='white',
                font_color='#1a1a2e',
                xaxis_tickangle=-30
            )
            st.plotly_chart(fig, use_container_width=True)

        # Full recommendations table
        st.subheader("📋 Full Recommendations")
        display_cols = ['fundName', 'category', 'fundRiskLevel',
                        'baseMatchScore', 'marketAdjustedScore']
        existing = [c for c in display_cols if c in df.columns]
        st.dataframe(df[existing], use_container_width=True)

        # Top 3 funds
        st.subheader("🏆 Top 3 Funds")
        for i, (_, row) in enumerate(df.head(3).iterrows()):
            fund_name = row.get('fundName', 'N/A')
            category = row.get('category', 'N/A')
            risk = row.get('fundRiskLevel', 'N/A')
            score = row.get('marketAdjustedScore', 'N/A')
            st.markdown(f"""
            <div class="metric-card">
              <h4>#{i+1} {fund_name}</h4>
              <p>Category: {category} | Risk: {risk}</p>
              <p>Score: <strong>{score}</strong></p>
            </div>
            """, unsafe_allow_html=True)

        # Unread alerts banner
        alerts, aerr = get(f"/api/v1/alerts/users/{user_id}/unread")
        if not aerr and alerts:
            st.markdown("---")
            alert_list = alerts if isinstance(alerts, list) else []
            st.subheader(f"🔔 {len(alert_list)} Unread Alert(s)")
            for alert in alert_list[:3]:
                severity = alert.get("severity", "INFO")
                if severity in ("HIGH", "CRITICAL"):
                    bg, border = "#fff0f0", "#cc0000"
                elif severity == "WARNING":
                    bg, border = "#fff8e1", "#ff8800"
                else:
                    bg, border = "#e8f4fd", "#0066cc"
                st.markdown(
                    f'<div style="background:{bg}; border-left:4px solid {border}; '
                    f'padding:12px; border-radius:8px; margin:8px 0;">'
                    f'<b>{alert.get("fundName","")}</b> — {alert.get("alertMessage","")}'
                    f'<br><small>{alert.get("eventTitle","")} | {severity}</small></div>',
                    unsafe_allow_html=True
                )
