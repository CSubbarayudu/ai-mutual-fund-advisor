import streamlit as st
from api.client import get, post

def render():
    st.title("👤 Investor Onboarding")
    tab1, tab2 = st.tabs(["🔎 View Profile", "➕ Register New Investor"])

    with tab1:
        inv_id = st.number_input("Enter Investor ID to View", value=3, min_value=1)
        if st.button("🔎 Fetch Profile"):
            result, err = get(f"/api/v1/onboarding/investor-profiles/{inv_id}")
            if err: st.error(err)
            elif result:
                # user details may be nested under "user" object
                user_obj = result.get("user", {}) or {}
                full_name = user_obj.get("fullName", result.get("fullName", "N/A"))
                col1, col2 = st.columns(2)
                col1.metric("Full Name", full_name)
                col1.metric("Age", result.get("age", "N/A"))
                col1.metric("Occupation", result.get("occupation", "N/A"))
                col2.metric("Investment Goal", result.get("investmentGoal", "N/A"))
                col2.metric("Horizon", result.get("investmentHorizon", "N/A"))
                col2.metric("Experience", result.get("investmentExperience", "N/A"))

    with tab2:
        st.subheader("Step 1: Create User Account")
        with st.form("user_form"):
            full_name = st.text_input("Full Name")
            email = st.text_input("Email")
            mobile = st.text_input("Mobile (10 digits)")
            submitted1 = st.form_submit_button("Create User")
        if submitted1:
            result, err = post("/api/v1/onboarding/users", {
                "fullName": full_name, "email": email, "mobile": mobile,
                "role": "INVESTOR", "status": "ACTIVE"
            })
            if err: st.error(err)
            else:
                st.success(f"✅ User created! User ID: {result.get('userId')}")
                st.session_state["new_user_id"] = result.get("userId")

        if "new_user_id" in st.session_state:
            st.subheader("Step 2: Create Investor Profile")
            with st.form("profile_form"):
                age = st.number_input("Age", 18, 80, 28)
                annual_income = st.number_input("Annual Income (₹)", 100000, 10000000, 700000, 50000)
                occupation = st.text_input("Occupation", value="Engineer")
                goal = st.selectbox("Investment Goal", ["WEALTH_CREATION","RETIREMENT","INCOME","TAX_SAVING"])
                horizon = st.selectbox("Investment Horizon", ["SHORT_TERM","MEDIUM_TERM","LONG_TERM"])
                liquidity = st.selectbox("Liquidity Preference", ["LOW","MEDIUM","HIGH"])
                experience = st.selectbox("Investment Experience", ["BEGINNER","MODERATE","EXPERIENCED"])
                submitted2 = st.form_submit_button("Create Profile")
            if submitted2:
                result, err = post("/api/v1/onboarding/investor-profiles", {
                    "userId": st.session_state["new_user_id"],
                    "age": age, "annualIncome": annual_income, "occupation": occupation,
                    "investmentGoal": goal, "investmentHorizon": horizon,
                    "liquidityPreference": liquidity, "investmentExperience": experience
                })
                if err: st.error(err)
                else:
                    st.success(f"✅ Profile created! Investor ID: {result.get('investorId')}")
                    st.balloons()
