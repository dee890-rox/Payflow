
import { useState } from "react";
import "./App.css";

import {
  registerUser,
  loginUser,
  getWallet,
  getTransactionHistory,
  sendMoney
} from "./services/api";
const formatAmount = (amount, currency = "INR") => {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: currency,
  }).format(amount);
};

const formatDate = (date) => {
  return new Date(date).toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};
function App() {
  const [page, setPage] = useState("login");
  const [token, setToken] = useState("");
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [user, setUser] = useState(null);

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    phone: ""
  });

  const [transfer, setTransfer] = useState({
    receiverEmail: "",
    amount: "",
    description: ""
  });

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function refreshDashboard(userToken) {
    const [walletData, historyData] = await Promise.all([
      getWallet(userToken),
      getTransactionHistory(userToken)
    ]);

    setWallet(walletData);

    const history = Array.isArray(historyData)
      ? historyData
      : historyData.transactions || historyData.content || [];

    setTransactions(history);
  }

  async function handleRegister(e) {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      await registerUser(form);
      setMessage("Account created successfully. Please log in.");
      setPage("login");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleLogin(e) {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const result = await loginUser({
        email: form.email,
        password: form.password
      });

      const userToken = result.accessToken;

      localStorage.setItem("token", userToken);
      setToken(userToken);
      setUser(result.user || { email: form.email });

      await refreshDashboard(userToken);
      setPage("dashboard");
      setMessage("");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleTransfer(e) {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      await sendMoney(
        token,
        {
          receiverEmail: transfer.receiverEmail,
          amount: Number(transfer.amount),
          description: transfer.description
        },
        crypto.randomUUID()
      );

      await refreshDashboard(token);

      setTransfer({
        receiverEmail: "",
        amount: "",
        description: ""
      });

      setMessage("Transfer completed successfully!");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem("token");
    setToken("");
    setWallet(null);
    setTransactions([]);
    setUser(null);
    setPage("login");
    setMessage("You have logged out.");
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: wallet?.currency || "INR",
      minimumFractionDigits: 2
    }).format(Number(amount || 0));
  }

  if (page === "dashboard") {
    return (
      <div className="app-shell">
        <aside className="sidebar">
          <div className="brand">
            <div className="brand-icon">P</div>
            <span>PayFlow</span>
          </div>

          <p className="sidebar-label">MENU</p>

          <div className="sidebar-link active">
            <span>▦</span> Overview
          </div>
          <div className="sidebar-link">
            <span>↗</span> Transfers
          </div>
          <div className="sidebar-link">
            <span>↻</span> Transactions
          </div>

          <div className="sidebar-bottom">
            <div className="security-note">
              <span>✦</span>
              <strong>Your money is protected</strong>
              <p>Secure digital wallet powered by PayFlow.</p>
            </div>
            <button className="logout-button" onClick={logout}>
              ↪ Log out
            </button>
          </div>
        </aside>

        <main className="main-content">
          <header className="topbar">
            <div>
              <p className="eyebrow">YOUR PERSONAL WALLET</p>
              <h1>Dashboard</h1>
            </div>

            <div className="profile">
              <div className="profile-avatar">
                {(user?.fullName || user?.name || user?.email || "U")
                  .charAt(0)
                  .toUpperCase()}
              </div>
              <div>
                <strong>{user?.fullName || user?.name || "Welcome back"}</strong>
                <p>{user?.email || ""}</p>
              </div>
            </div>
          </header>

          <section className="welcome-row">
            <div>
              <h2>Good to see you again!</h2>
              <p>Here's what's happening with your wallet today.</p>
            </div>
            <span className="secure-pill">● Secure account</span>
          </section>

          <section className="overview-grid">
            <div className="balance-card">
              <div className="balance-heading">
                <span>Available balance</span>
                <span className="balance-symbol">₹</span>
              </div>

              <h2>{formatCurrency(wallet?.balance)}</h2>
              <p>Your current wallet balance</p>

              <div className="balance-footer">
                <span>WALLET ID</span>
                <strong>{wallet?.walletId || "—"}</strong>
              </div>
            </div>

            <div className="info-card">
              <div className="info-icon">↗</div>
              <p>Quick action</p>
              <h3>Send money</h3>
              <span>Transfer funds securely to another PayFlow user.</span>
              <a href="#send-money">Make a transfer →</a>
            </div>

            <div className="info-card">
              <div className="info-icon history-icon">↻</div>
              <p>Activity</p>
              <h3>{transactions.length} transactions</h3>
              <span>Your recent wallet activity in one place.</span>
              <a href="#transactions">View activity →</a>
            </div>
          </section>

          <section className="content-grid">
            <div className="panel transfer-panel" id="send-money">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">QUICK PAYMENT</p>
                  <h2>Send money</h2>
                </div>
                <span className="panel-icon">↗</span>
              </div>

              <form onSubmit={handleTransfer}>
                <label>Receiver email</label>
                <input
                  type="email"
                  placeholder="name@example.com"
                  value={transfer.receiverEmail}
                  onChange={(e) =>
                    setTransfer({ ...transfer, receiverEmail: e.target.value })
                  }
                  required
                />

                <label>Amount (₹)</label>
                <input
                  type="number"
                  placeholder="0.00"
                  min="0.01"
                  step="0.01"
                  value={transfer.amount}
                  onChange={(e) =>
                    setTransfer({ ...transfer, amount: e.target.value })
                  }
                  required
                />

                <label>Description <span className="optional">(optional)</span></label>
                <input
                  placeholder="What's this payment for?"
                  value={transfer.description}
                  onChange={(e) =>
                    setTransfer({ ...transfer, description: e.target.value })
                  }
                />

                <button className="primary-button" disabled={loading}>
                  {loading ? "Processing..." : "Continue transfer →"}
                </button>
              </form>
            </div>

            <div className="panel transactions-panel" id="transactions">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">YOUR ACTIVITY</p>
                  <h2>Recent transactions</h2>
                </div>
                <span className="transaction-count">{transactions.length}</span>
              </div>

              {transactions.length === 0 ? (
                <div className="empty-state">
                  <div>↻</div>
                  <strong>No transactions yet</strong>
                  <p>Your completed transfers will appear here.</p>
                </div>
              ) : (
                <div className="transaction-list">
  {transactions.map((item) => {
  const isSent = item.senderEmail === user?.email;

  return (
    <div className="transaction-item" key={item.reference}>
      <div className="transaction-info">
        <h4>{isSent ? "Money Sent" : "Money Received"}</h4>

        <p>
          {isSent
            ? `To: ${item.receiverEmail}`
            : `From: ${item.senderEmail}`}
        </p>

        <p>{item.description || "No description"}</p>

        <small>{formatDate(item.createdAt)}</small>

        <span>{item.status}</span>
      </div>

      <div className={`transaction-amount ${isSent ? "sent" : "received"}`}>
        {isSent ? "-" : "+"}
        {formatAmount(item.amount, item.currency)}
      </div>
    </div>
  );
})}
                </div>
              )}
            </div>
          </section>

          {message && (
            <div className="feedback-message" role="status">
              {message}
            </div>
          )}

          <footer className="footer">
            © 2026 PayFlow. Digital payments made simple.
          </footer>
        </main>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="auth-brand">
        <div className="brand-icon">P</div>
        <span>PayFlow</span>
      </div>

      <div className="auth-layout">
        <section className="auth-intro">
          <span className="intro-tag">YOUR MONEY. YOUR CONTROL.</span>
          <h1>Payments made<br />simple.</h1>
          <p>
            A smarter way to manage your wallet and move money.
            Experience simple, secure digital payments with PayFlow.
          </p>

          <div className="intro-feature">
            <span>✓</span> Secure account access
          </div>
          <div className="intro-feature">
            <span>✓</span> Easy money transfers
          </div>
          <div className="intro-feature">
            <span>✓</span> Track your transactions
          </div>
        </section>

        <section className="auth-card">
          <div className="auth-tabs">
            <button
              className={page === "login" ? "selected" : ""}
              onClick={() => { setPage("login"); setMessage(""); }}
            >
              Sign in
            </button>
            <button
              className={page === "register" ? "selected" : ""}
              onClick={() => { setPage("register"); setMessage(""); }}
            >
              Create account
            </button>
          </div>

          {page === "login" ? (
            <form onSubmit={handleLogin}>
              <p className="eyebrow">WELCOME BACK</p>
              <h2>Sign in to PayFlow</h2>
              <p className="form-subtitle">Enter your details to access your wallet.</p>

              <label>Email address</label>
              <input
                name="email"
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={handleChange}
                required
              />

              <label>Password</label>
              <input
                name="password"
                type="password"
                placeholder="Enter your password"
                value={form.password}
                onChange={handleChange}
                required
              />

              <button className="primary-button" disabled={loading}>
                {loading ? "Signing in..." : "Sign in →"}
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister}>
              <p className="eyebrow">GET STARTED</p>
              <h2>Create your account</h2>
              <p className="form-subtitle">Join PayFlow and manage your money simply.</p>

              <label>Full name</label>
              <input
                name="fullName"
                placeholder="Your full name"
                value={form.fullName}
                onChange={handleChange}
                required
              />

              <label>Email address</label>
              <input
                name="email"
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={handleChange}
                required
              />

              <label>Password</label>
              <input
                name="password"
                type="password"
                placeholder="Create a password"
                value={form.password}
                onChange={handleChange}
                required
              />

              <label>Phone number</label>
              <input
                name="phone"
                placeholder="Your phone number"
                value={form.phone}
                onChange={handleChange}
                required
              />

              <button className="primary-button" disabled={loading}>
                {loading ? "Creating account..." : "Create account →"}
              </button>
            </form>
          )}

          {message && <div className="feedback-message">{message}</div>}

          <p className="auth-security">♢ Your account is protected with secure authentication.</p>
        </section>
      </div>
    </div>
  );
}

export default App;
