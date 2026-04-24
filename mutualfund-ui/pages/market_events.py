import streamlit as st
import time
from api.client import get, post

def render():
    st.title("📰 Market Events")
    tab1, tab2, tab3 = st.tabs(["🌐 Active Events", "➕ Ingest Event", "🔴 Live News Trigger"])

    with tab1:
        data, err = get("/api/v1/market-events/active")
        if err: st.error(err)
        elif not data: st.info("No active market events.")
        else:
            for ev in (data if isinstance(data, list) else []):
                impact = ev.get("impactType", "NEUTRAL")
                css = "positive-event" if impact == "POSITIVE" else "negative-event" if impact == "NEGATIVE" else "neutral-event"
                icon = "🟢" if impact == "POSITIVE" else "🔴" if impact == "NEGATIVE" else "⚪"
                ingested = ev.get("ingestedAt", "")
                ingested_display = ingested[:10] if ingested else "N/A"
                st.markdown(
                    f'<div class="fund-card {css}"><b>{icon} {ev.get("eventTitle","")}</b><br>'
                    f'Impact: {impact} | Credibility: {ev.get("credibilityScore","N/A")}<br>'
                    f'Sectors: {ev.get("affectedSectorsCount",0)} | Funds: {ev.get("affectedFundsCount",0)} | Alerts: {ev.get("alertsGeneratedCount",0)}<br>'
                    f'<small>Status: {ev.get("status","")} | {ingested_display}</small></div>',
                    unsafe_allow_html=True
                )

    with tab2:
        st.subheader("Manually Ingest a Market Event")
        with st.form("ingest_form"):
            title = st.text_input("Event Title", value="RBI Rate Cut 2026")
            desc = st.text_area("Description", value="RBI cuts repo rate by 50bps")
            impact_type = st.selectbox("Impact Type", ["POSITIVE", "NEGATIVE", "NEUTRAL"])
            credibility = st.slider("Credibility Score", 0.0, 1.0, 0.90, 0.01)
            sector = st.selectbox("Affected Sector", ["BANKING","IT","PHARMA","ENERGY","METALS","REALESTATE"])
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
            result, err = post("/api/v1/market-events/ingest", payload)
            if err: st.error(err)
            else:
                st.success(f"✅ Event ingested! Funds affected: {result.get('affectedFundsCount',0)}, Alerts: {result.get('alertsGeneratedCount',0)}")

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
