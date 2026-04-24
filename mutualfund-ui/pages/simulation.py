import streamlit as st
import plotly.graph_objects as go
import pandas as pd
from api.client import get, post

def render(investor_id: int):
    st.title("🧮 What-If Simulation")
    risk_choice = st.selectbox("Simulate with Risk Level:", ["LOW", "MODERATE", "HIGH"])
    col1, col2 = st.columns(2)

    with col1:
        st.subheader("Current Holdings")
        holdings, err = get(f"/api/v1/holdings/{investor_id}")
        if err: st.error(err)
        elif not holdings: st.info("No holdings found for this investor.")
        else:
            for h in (holdings if isinstance(holdings, list) else [holdings]):
                st.markdown(
                    f'<div class="fund-card"><b>{h.get("fundName","N/A")}</b>'
                    f'<br><small>Status: {h.get("holdingStatus","N/A")}</small></div>',
                    unsafe_allow_html=True
                )

    with col2:
        st.subheader("Simulation Result")
        if st.button(f"▶️ Run Simulation ({risk_choice})"):
            with st.spinner("Running simulation..."):
                result, err = post(f"/api/v1/simulation/what-if/{investor_id}", payload={"riskLevel": risk_choice})
            if err: st.error(err)
            elif result:
                df = pd.DataFrame(result if isinstance(result, list) else [result])
                if not df.empty and "fundName" in df.columns:
                    fig = go.Figure()
                    if "baseMatchScore" in df.columns:
                        fig.add_trace(go.Bar(name="Base Score", x=df["fundName"], y=df["baseMatchScore"], marker_color="#3b82f6"))
                    if "marketAdjustedScore" in df.columns:
                        fig.add_trace(go.Bar(name="Simulated Adjusted Score", x=df["fundName"], y=df["marketAdjustedScore"], marker_color="#f59e0b"))
                    fig.update_layout(barmode="group", title=f"Simulation: {risk_choice} Risk",
                                      paper_bgcolor="#0f172a", plot_bgcolor="#0f172a", font_color="#e2e8f0")
                    st.plotly_chart(fig, use_container_width=True)
                    show = ["fundName","baseMatchScore","marketAdjustedScore","confidenceScore"]
                    existing = [c for c in show if c in df.columns]
                    st.dataframe(df[existing], use_container_width=True)
