import streamlit as st
from api.client import get, post

def render(investor_id: int):
    st.title("🤖 AI Investment Chatbot")

    if "chat_messages" not in st.session_state:
        st.session_state.chat_messages = []

    if st.button("📜 Load Chat History"):
        history, err = get(f"/api/v1/chat/investor/{investor_id}/history")
        if err: st.error(err)
        elif history:
            st.session_state.chat_messages = []
            for item in reversed(history if isinstance(history, list) else [history]):
                st.session_state.chat_messages.append({"role": "user", "content": item.get("question", "")})
                st.session_state.chat_messages.append({"role": "assistant", "content": item.get("answer", "")})

    for msg in st.session_state.chat_messages:
        if msg["role"] == "user":
            st.markdown(f'<div class="chat-bubble-user">🧑 {msg["content"]}</div>', unsafe_allow_html=True)
        else:
            st.markdown(f'<div class="chat-bubble-ai">🤖 {msg["content"]}</div>', unsafe_allow_html=True)

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

    user_input = st.chat_input("Ask about your portfolio...")
    question = user_input or st.session_state.pop("pending_question", None)

    if question:
        st.session_state.chat_messages.append({"role": "user", "content": question})
        with st.spinner("AI thinking..."):
            result, err = post(f"/api/v1/chat/investor/{investor_id}", payload={"question": question})

        if err:
            # CHATBOT FALLBACK — UI never breaks even if embeddings fail
            st.warning("⚠️ AI Chat temporarily unavailable.")
            answer = "AI service is currently unstable. Please check alerts and recommendations, or try again shortly."
        else:
            answer = result.get("answer", "No response received")

        st.session_state.chat_messages.append({"role": "assistant", "content": answer})
        st.rerun()
