import streamlit as st
from config import APP_TITLE, APP_ICON, DEFAULT_INVESTOR_ID, DEFAULT_USER_ID
from pages import (
    dashboard, fund_explorer, simulation,
    chatbot_panel, market_events, alerts_panel, onboarding
)

st.set_page_config(page_title=APP_TITLE, page_icon=APP_ICON, layout="wide", initial_sidebar_state="expanded")

st.markdown("""
<style>
[data-testid="stSidebar"] { background: #0f2027; }
[data-testid="stSidebar"] * { color: #e0e0e0 !important; }
.metric-card { background: #1e293b; border-radius: 12px; padding: 20px; border: 1px solid #334155; margin-bottom: 12px; }
.alert-warning { background: #451a03; border-left: 4px solid #f97316; padding: 12px; border-radius: 8px; margin: 8px 0; }
.alert-high { background: #450a0a; border-left: 4px solid #ef4444; padding: 12px; border-radius: 8px; margin: 8px 0; }
.alert-info { background: #082f49; border-left: 4px solid #38bdf8; padding: 12px; border-radius: 8px; margin: 8px 0; }
.chat-bubble-user { background: #1e40af; padding: 10px 16px; border-radius: 18px 18px 4px 18px; margin: 6px 0; max-width: 80%; margin-left: auto; color: white; }
.chat-bubble-ai { background: #1e293b; padding: 10px 16px; border-radius: 18px 18px 18px 4px; margin: 6px 0; max-width: 85%; border: 1px solid #334155; }
.fund-card { background: #1e293b; border-radius: 10px; padding: 16px; border: 1px solid #334155; margin: 8px 0; }
.positive-event { border-left: 4px solid #22c55e; }
.negative-event { border-left: 4px solid #ef4444; }
.neutral-event { border-left: 4px solid #94a3b8; }
</style>
""", unsafe_allow_html=True)

with st.sidebar:
    st.markdown("## 📈 MF Advisor")
    st.markdown("---")
    investor_id = st.number_input("Investor ID", value=DEFAULT_INVESTOR_ID, min_value=1, step=1)
    user_id = st.number_input("User ID (for Alerts)", value=DEFAULT_USER_ID, min_value=1, step=1)
    st.markdown("---")
    page = st.radio("Navigation", [
        "🏠 Dashboard", "🔍 Fund Explorer", "🧮 What-If Simulation",
        "🤖 AI Chatbot", "📰 Market Events", "🔔 Alerts Panel", "👤 Onboarding"
    ])
    st.markdown("---")
    st.caption("Nihilent Ltd · VP Demo")

if page == "🏠 Dashboard": dashboard.render(investor_id, user_id)
elif page == "🔍 Fund Explorer": fund_explorer.render(investor_id)
elif page == "🧮 What-If Simulation": simulation.render(investor_id)
elif page == "🤖 AI Chatbot": chatbot_panel.render(investor_id)
elif page == "📰 Market Events": market_events.render()
elif page == "🔔 Alerts Panel": alerts_panel.render(user_id)
elif page == "👤 Onboarding": onboarding.render()
