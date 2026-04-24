import streamlit as st
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


def render(investor_id: int):
    st.markdown(GLOBAL_CSS, unsafe_allow_html=True)
    st.title("🤖 AI Investment Chatbot")

    if "chat_messages" not in st.session_state:
        st.session_state.chat_messages = []

    if st.button("📜 Load Chat History"):
        with st.spinner("Loading chat history..."):
            history, err = get(f"/api/v1/chat/investor/{investor_id}/history")
        if err:
            st.error(err)
        elif history:
            st.session_state.chat_messages = []
            for item in reversed(history if isinstance(history, list) else [history]):
                st.session_state.chat_messages.append({"role": "user", "content": item.get("question", "")})
                st.session_state.chat_messages.append({"role": "assistant", "content": item.get("answer", "")})

    # Render existing chat history with bubble style
    for msg in st.session_state.chat_messages:
        if msg["role"] == "user":
            st.markdown(f"""
            <div style="text-align:right; margin:8px 0;">
              <span style="background:#0066cc; color:white;
                           padding:10px 16px; border-radius:18px 18px 4px 18px;
                           display:inline-block; max-width:75%;">
                {msg['content']}
              </span>
            </div>""", unsafe_allow_html=True)
        else:
            st.markdown(f"""
            <div style="text-align:left; margin:8px 0;">
              <span style="background:#ffffff; color:#1a1a2e;
                           border:1px solid #ddd;
                           padding:10px 16px; border-radius:18px 18px 18px 4px;
                           display:inline-block; max-width:75%;">
                {msg['content']}
              </span>
            </div>""", unsafe_allow_html=True)

    st.markdown("---")
    st.markdown("**Quick questions:**")
    suggestions = [
        "Why did my HDFC Technology Fund score drop?",
        "Which fund is best for long-term growth?",
        "Explain the impact of US Chip Ban on my portfolio",
        "What is my current risk profile?"
    ]
    cols = st.columns(2)
    for i, sug in enumerate(suggestions):
        if cols[i % 2].button(sug, key=f"sug_{i}"):
            st.session_state.pending_question = sug

    user_input = st.chat_input("Ask about any fund or investment...")
    question = user_input or st.session_state.pop("pending_question", None)

    if question:
        st.session_state.chat_messages.append({"role": "user", "content": question})
        with st.spinner("AI is thinking..."):
            try:
                result, err = post(f"/api/v1/chat/investor/{investor_id}", payload={"question": question})
            except Exception as e:
                err = str(e)
                result = None

        if err:
            st.warning("⚠️ AI Chat temporarily unavailable.")
            answer = "AI service is currently unstable. Please check alerts and recommendations, or try again shortly."
        else:
            answer = result.get("answer", "No response received") if result else "No response received"

        st.session_state.chat_messages.append({"role": "assistant", "content": answer})
        st.rerun()
