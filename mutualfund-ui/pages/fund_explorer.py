import streamlit as st
import plotly.express as px
import pandas as pd
from api.client import get, post

GLOBAL_CSS = """
<style>
  .stApp { background-color: #f5f7fa; color: #1a1a2e; }
  .stMarkdown, .stText, p, span, label { color: #1a1a2e !important; }
  [data-testid="stSidebar"] { background-color: #1a1a2e; }
  [data-testid="stSidebar"] * { color: #ffffff !important; }
  .stDataFrame { background: white; }
  .stButton button { background-color: #0066cc; color: white; border-radius: 8px; border: none; font-weight: 600; }
  .stButton button:hover { background-color: #004a9f; }
  [data-testid="stMetricValue"] { color: #1a1a2e !important; }
</style>
"""


def render(investor_id: int):
    st.markdown(GLOBAL_CSS, unsafe_allow_html=True)
    st.title("🔍 Fund Explorer")
    tab1, tab2, tab3 = st.tabs(["📋 All Funds", "🆚 Compare Funds", "💡 AI Explanation"])

    with tab1:
        with st.spinner("Loading funds..."):
            try:
                data, err = get("/api/v1/funds")
            except Exception as e:
                st.error(f"Error: {str(e)}")
                st.stop()
                return
        if err:
            st.error(err)
        elif data:
            df = pd.DataFrame(data)
            if not df.empty:
                show_cols = ["fundName", "category", "riskLevel", "return1y", "return3y", "minimumInvestment"]
                existing = [c for c in show_cols if c in df.columns]
                st.dataframe(df[existing], use_container_width=True)
                if "return3y" in df.columns and "riskLevel" in df.columns:
                    fig = px.scatter(df, x="riskLevel", y="return3y", color="category",
                                     text="fundName", title="3Y Returns by Risk Level")
                    fig.update_traces(textposition="top center")
                    fig.update_layout(
                        plot_bgcolor='white', paper_bgcolor='white', font_color='#1a1a2e'
                    )
                    st.plotly_chart(fig, use_container_width=True)

    with tab2:
        all_funds, _ = get("/api/v1/funds")
        funds_list = all_funds if isinstance(all_funds, list) else []
        if len(funds_list) >= 2:
            fund_names = {f["fundName"]: f["fundId"] for f in funds_list if "fundName" in f}
            c1, c2 = st.columns(2)
            f1_name = c1.selectbox("Fund 1", list(fund_names.keys()), key="f1")
            f2_name = c2.selectbox("Fund 2", list(fund_names.keys()), index=1, key="f2")
            if st.button("Compare"):
                with st.spinner("Comparing funds..."):
                    result, err = get("/api/v1/funds/compare", params={"fund1": fund_names[f1_name], "fund2": fund_names[f2_name]})
                if err:
                    st.error(err)
                elif result:
                    st.info(result.get("comparisonNote", "Comparison complete"))
                    c1, c2 = st.columns(2)
                    for col, f_key in zip([c1, c2], ["fund1", "fund2"]):
                        if f_key in result:
                            fund = result[f_key]
                            with col:
                                st.markdown(f"### {fund.get('fundName', '')}")
                                st.write(f"**Category:** {fund.get('category', 'N/A')}")
                                st.write(f"**Risk Level:** {fund.get('riskLevel', 'N/A')}")
                                st.write(f"**1Y Return:** {fund.get('return1y', 'N/A')}%")
                                st.write(f"**3Y Return:** {fund.get('return3y', 'N/A')}%")
                                st.write(f"**Min Investment:** ₹{fund.get('minimumInvestment', 'N/A')}")

    with tab3:
        all_funds2, _ = get("/api/v1/funds")
        funds_list2 = all_funds2 if isinstance(all_funds2, list) else []
        fund_names2 = {f["fundName"]: f["fundId"] for f in funds_list2 if "fundName" in f}
        if fund_names2:
            selected_fund = st.selectbox("Select Fund for AI Explanation", list(fund_names2.keys()))
            if st.button("🤖 Generate AI Explanation"):
                with st.spinner("Generating AI explanation..."):
                    result, err = post(f"/api/v1/explain/investor/{investor_id}/fund/{fund_names2[selected_fund]}")
                if err:
                    st.error(err)
                elif result:
                    st.success("Explanation Generated")
                    col1, col2, col3 = st.columns(3)
                    col1.metric("Base Score", result.get("baseMatchScore", "N/A"))
                    col2.metric("Market Adjusted", result.get("marketAdjustedScore", "N/A"))
                    col3.metric("Effective Risk", result.get("effectiveRiskLevel", "N/A"))
                    st.markdown("---")
                    st.markdown(result.get("explanation", ""))
