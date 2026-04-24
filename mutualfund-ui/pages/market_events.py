import streamlit as st
import time
from api.client import get, post

GLOBAL_CSS = """
<style>
  .stApp { background-color: #f5f7fa; color: #1a1a2e; }
  .stMarkdown, .stText, p, span, label { color: #1a1a2e !important; }
  [data-testid="stSidebar"] { background-color: #1a1a2e; }
  [data-testid="stSidebar"] * { color: #ffffff !important; }
  .stButton button { background-color: #0066cc; color: white; border-radius: 8px; border: none; font-weight: 600; }
  .stButton button:hover { background-color: #004a9f; }
</style>
"""

IMPACT_CONFIG = {
    "POSITIVE": ("🟢", "#e6f9ed", "#2e7d32"),
    "NEGATIVE": ("🔴", "#fdecea", "#c62828"),
    "NEUTRAL":  ("🟡", "#fffde7", "#f57f17"),
}


def render():
    st.markdown(GLOBAL_CSS, unsafe_allow_html=True)
    st.title("📰 Market Events")
    tab1, tab2, tab3 = st.tabs(["🌐 Active Events", "➕ Ingest Event", "🔴 Live News Trigger"])

    with tab1:
        try:
            data, err = get("/api/v1/market-events/active")
        except Exception as e:
            st.error(f"Error: {str(e)}")
            st.stop()
            return

        if err:
            st.error(err)
        elif not data:
            st.info("No active market events.")
        else:
            for ev in (data if isinstance(data, list) else []):
                impact = ev.get("impactType", "NEUTRAL")
                icon, bg, color = IMPACT_CONFIG.get(impact, ("⚪", "#f9f9f9", "#555"))
                ingested = ev.get("ingestedAt", "")
                ingested_display = ingested[:10] if ingested else "N/A"
                st.markdown(
                    f'<div style="background:{bg}; border-left:5px solid {color};'
                    f' border-radius:8px; padding:16px; margin-bottom:12px;">'
                    f'<h4 style="color:{color}; margin:0;">{icon} {ev.get("eventTitle","")}</h4>'
                    f'<p style="color:#1a1a2e; margin:8px 0 4px 0;">{ev.get("eventDescription", "")}</p>'
                    f'<small style="color:#555;">'
                    f'Impact: {impact} | '
                    f'Credibility: {ev.get("credibilityScore","N/A")}/10 | '
                    f'Sectors: {ev.get("affectedSectorsCount",0)} | '
                    f'Funds: {ev.get("affectedFundsCount",0)} | '
                    f'Alerts: {ev.get("alertsGeneratedCount",0)}'
                    f'<br>Status: {ev.get("status","")} | {ingested_display}'
                    f'</small></div>',
                    unsafe_allow_html=True
                )

    with tab2:
        st.subheader("Manually Ingest a Market Event")
        with st.form("ingest_form"):
            title = st.text_input("Event Title", value="RBI Rate Cut 2026")
            desc = st.text_area("Description", value="RBI cuts repo rate by 50bps")
            impact_type = st.selectbox("Impact Type", ["POSITIVE", "NEGATIVE", "NEUTRAL"])
            credibility = st.slider("Credibility Score", 0.0, 1.0, 0.90, 0.01)
            sector = st.selectbox("Affected Sector", ["BANKING", "IT", "PHARMA", "ENERGY", "METALS", "REALESTATE"])
            severity = st.slider("Impact Severity", 0.0, 10.0, 7.5, 0.5)
            submitted = st.form_submit_button("📤 Ingest Event")

        if submitted:
            from datetime import datetime, timedelta
            payload = {
                "eventTitle": title,
                "eventDescription": desc,
                "impactType": impact_type,
                "credibilityScore": credibility,
                "impactDuration": "90 days",
                "sourceUrl": "https://rbi.org.in",
                "eventDate": datetime.now().isoformat(),
                "expiryDate": (datetime.now() + timedelta(days=90)).isoformat(),
                "affectedSectors": [{"sectorName": sector, "impactSeverity": severity}]
            }
            with st.spinner("Ingesting event..."):
                result, err = post("/api/v1/market-events/ingest", payload)
            if err:
                st.error(err)
            else:
                st.success(f"✅ Event ingested! Funds affected: {result.get('affectedFundsCount', 0)}, Alerts: {result.get('alertsGeneratedCount', 0)}")

    with tab3:
        st.subheader("🔴 Live NewsAPI Market Event Ingestion")
        st.info("Triggers the NewsAPI poller to fetch real headlines and ingest as market events.")
        if st.button("🚀 Trigger Live News Ingestion"):
            with st.spinner("Fetching live market news..."):
                _, err = post("/api/v1/market-events/live-news/trigger")
            if err:
                st.error(f"Live news failed: {err}. Check NewsAPI key or backend logs.")
            else:
                st.success("✅ Live news ingested and scored!")
                time.sleep(1.5)
                st.rerun()
