import streamlit as st
from config import APP_TITLE, APP_ICON, DEFAULT_INVESTOR_ID, DEFAULT_USER_ID
from pages import (
    dashboard, fund_explorer, simulation,
    chatbot_panel, market_events, alerts_panel, onboarding
)

st.set_page_config(page_title=APP_TITLE, page_icon=APP_ICON, layout="wide", initial_sidebar_state="expanded")

st.markdown("""
<style>
  /* Force readable text on all surfaces */
  .stApp { background-color: #f5f7fa; color: #1a1a2e; }
  .stMarkdown, .stText, p, span, label { color: #1a1a2e !important; }

  /* Cards */
  .metric-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    border-left: 4px solid #0066cc;
    margin-bottom: 12px;
  }

  /* Sidebar */
  [data-testid="stSidebar"] {
    background-color: #1a1a2e;
  }
  [data-testid="stSidebar"] * {
    color: #ffffff !important;
  }

  /* Dataframe/Table */
  .stDataFrame { background: white; }

  /* Buttons */
  .stButton button {
    background-color: #0066cc;
    color: white;
    border-radius: 8px;
    border: none;
    font-weight: 600;
  }
  .stButton button:hover { background-color: #004a9f; }

  /* Alert cards */
  .alert-warning { background: #fff8e1; border-left: 4px solid #ff8800; padding: 12px; border-radius: 8px; margin: 8px 0; }
  .alert-high { background: #fff0f0; border-left: 4px solid #cc0000; padding: 12px; border-radius: 8px; margin: 8px 0; }
  .alert-info { background: #e8f4fd; border-left: 4px solid #0066cc; padding: 12px; border-radius: 8px; margin: 8px 0; }

  /* Chat bubbles */
  .chat-bubble-user { background: #0066cc; padding: 10px 16px; border-radius: 18px 18px 4px 18px; margin: 6px 0; max-width: 80%; margin-left: auto; color: white !important; }
  .chat-bubble-user * { color: white !important; }
  .chat-bubble-ai { background: #ffffff; padding: 10px 16px; border-radius: 18px 18px 18px 4px; margin: 6px 0; max-width: 85%; border: 1px solid #ddd; }

  /* Fund cards */
  .fund-card { background: #ffffff; border-radius: 10px; padding: 16px; border: 1px solid #e0e0e0; margin: 8px 0; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
  .positive-event { border-left: 4px solid #2e7d32; }
  .negative-event { border-left: 4px solid #c62828; }
  .neutral-event { border-left: 4px solid #f57f17; }

  /* Metrics */
  [data-testid="stMetricValue"] { color: #1a1a2e !important; }
  [data-testid="stMetricLabel"] { color: #555 !important; }
</style>
""", unsafe_allow_html=True)

with st.sidebar:
    st.markdown("## 🏦 MutualFund AI Advisor")
    st.markdown("---")
    investor_id = st.number_input("Investor ID", value=DEFAULT_INVESTOR_ID, min_value=1, step=1)
    user_id = st.number_input("User ID (for Alerts)", value=DEFAULT_USER_ID, min_value=1, step=1)
    st.markdown("---")
    page = st.radio("Navigation", [
        "🏠 Dashboard", "🔍 Fund Explorer", "🧮 What-If Simulation",
        "🤖 AI Chatbot", "📰 Market Events", "🔔 Alerts Panel", "👤 Onboarding"
    ])

if page == "🏠 Dashboard": dashboard.render(investor_id, user_id)
elif page == "🔍 Fund Explorer": fund_explorer.render(investor_id)
elif page == "🧮 What-If Simulation": simulation.render(investor_id)
elif page == "🤖 AI Chatbot": chatbot_panel.render(investor_id)
elif page == "📰 Market Events": market_events.render()
elif page == "🔔 Alerts Panel": alerts_panel.render(user_id)
elif page == "👤 Onboarding": onboarding.render()
