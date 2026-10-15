import { useState } from "react";
import axios from "axios";
import "./index.css";

const API = "http://localhost:8080";

export default function App() {
  const [token, setToken] = useState(
    () => localStorage.getItem("token") || ""
  );
  const [mode, setMode] = useState("login");
  const [page, setPage] = useState("analyze");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [file, setFile] = useState(null);
  const [jobDescription, setJobDescription] = useState("");
  const [numberOfQuestions, setNumberOfQuestions] = useState(10);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function logout() {
    localStorage.removeItem("token");
    setToken("");
    setResult(null);
    setError("");
  }

  async function authenticate(e) {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const endpoint =
        mode === "login" ? "/auth/login" : "/auth/register";

      const payload =
        mode === "login"
          ? { username, password }
          : { username, email, password };

      const response = await axios.post(API + endpoint, payload);
      const data = response.data;

      // Supports common JWT response property names.
      const jwt =
        data.token ||
        data.accessToken ||
        data.jwt ||
        data.access_token;

      if (mode === "login") {
        if (!jwt) {
          throw new Error(
            "Login succeeded but no JWT was found in the response. Check the /auth/login response."
          );
        }

        localStorage.setItem("token", jwt);
        setToken(jwt);
      } else {
        setMode("login");
        setError("Registration successful. Please log in.");
        setPassword("");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          err.message ||
          "Authentication failed."
      );
    } finally {
      setLoading(false);
    }
  }

  async function submitRequest(endpoint, fields) {
    if (!file) {
      setError("Please select a resume PDF first.");
      return;
    }

    setLoading(true);
    setError("");
    setResult(null);

    try {
      const formData = new FormData();
      formData.append("file", file);

      Object.entries(fields).forEach(([key, value]) => {
        formData.append(key, String(value));
      });

      const response = await axios.post(API + endpoint, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setResult(response.data);
    } catch (err) {
      if (err.response?.status === 401) {
        setError("Your session may have expired. Please log in again.");
      } else {
        setError(
          err.response?.data?.message ||
            err.response?.data?.error ||
            `Request failed (${err.response?.status || "network error"}). Check the backend console.`
        );
      }
    } finally {
      setLoading(false);
    }
  }

  function renderValue(value) {
    if (value === null || value === undefined) return "—";

    if (typeof value === "object") {
      if (Array.isArray(value)) {
        return (
          <ul>
            {value.map((item, index) => (
              <li key={index}>{renderValue(item)}</li>
            ))}
          </ul>
        );
      }

      return (
        <div className="nested">
          {Object.entries(value).map(([key, item]) => (
            <div className="result-field" key={key}>
              <strong>{key.replace(/([A-Z])/g, " $1")}</strong>
              {renderValue(item)}
            </div>
          ))}
        </div>
      );
    }

    return String(value);
  }

  if (!token) {
    return (
      <main className="auth-page">
        <section className="auth-card">
          <div className="brand-icon">AI</div>
          <p className="eyebrow">SMART CAREER TOOLS</p>
          <h1>Resume Analyzer</h1>
          <p className="muted">
            Analyze your resume. Find your gaps. Prepare for your next role.
          </p>

          <div className="tabs">
            <button
              className={mode === "login" ? "active" : ""}
              onClick={() => {
                setMode("login");
                setError("");
              }}
            >
              Login
            </button>
            <button
              className={mode === "register" ? "active" : ""}
              onClick={() => {
                setMode("register");
                setError("");
              }}
            >
              Register
            </button>
          </div>

          <form onSubmit={authenticate}>
            <label>Username</label>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
              placeholder="Enter username"
            />

            {mode === "register" && (
              <>
                <label>Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  placeholder="you@example.com"
                />
              </>
            )}

            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete={
                mode === "login" ? "current-password" : "new-password"
              }
              placeholder="Enter password"
            />

            {error && <p className="notice">{error}</p>}

            <button className="primary full" disabled={loading}>
              {loading
                ? "Please wait..."
                : mode === "login"
                  ? "Sign in"
                  : "Create account"}
            </button>
          </form>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-icon">AI</span>
          <span>CareerPilot</span>
        </div>

        <p className="side-label">WORKSPACE</p>

        {[
          ["analyze", "Resume Analysis", "◈"],
          ["match", "Job Matching", "⌕"],
          ["interview", "Interview Prep", "✧"],
        ].map(([key, label, icon]) => (
          <button
            key={key}
            className={`nav-item ${page === key ? "selected" : ""}`}
            onClick={() => {
              setPage(key);
              setResult(null);
              setError("");
            }}
          >
            <span>{icon}</span> {label}
          </button>
        ))}

        <div className="sidebar-bottom">
          <div className="user-chip">
            <span className="avatar">
              {username.charAt(0).toUpperCase() || "U"}
            </span>
            <div>
              <strong>{username || "User"}</strong>
              <small>Personal workspace</small>
            </div>
          </div>
          <button className="logout" onClick={logout}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="eyebrow">YOUR CAREER WORKSPACE</p>
            <h1>
              {page === "analyze"
                ? "Resume Analysis"
                : page === "match"
                  ? "Job Match Assistant"
                  : "Interview Preparation"}
            </h1>
          </div>
          <span className="status"><i /> AI services workspace</span>
        </header>

        <section className="intro">
          <div>
            <h2>
              {page === "analyze"
                ? "Understand your resume."
                : page === "match"
                  ? "See how you fit the role."
                  : "Prepare with confidence."}
            </h2>
            <p>
              {page === "analyze"
                ? "Upload your PDF to discover your strengths and areas to improve."
                : page === "match"
                  ? "Compare your experience and skills against a target job."
                  : "Generate role-specific questions using your resume."}
            </p>
          </div>
          <div className="decorative-icon">
            {page === "analyze" ? "▤" : page === "match" ? "⇄" : "✧"}
          </div>
        </section>

        <section className="workspace-grid">
          <div className="panel form-panel">
            <div className="panel-heading">
              <span className="step-number">01</span>
              <div>
                <h3>Your resume</h3>
                <p>Upload a readable PDF file.</p>
              </div>
            </div>

            <label className="upload-area">
              <span className="upload-icon">↑</span>
              <strong>{file ? file.name : "Choose your resume PDF"}</strong>
              <span>
                {file
                  ? `${(file.size / 1024).toFixed(0)} KB`
                  : "Click to browse files"}
              </span>
              <input
                type="file"
                accept=".pdf,application/pdf"
                onChange={(e) => {
                  setFile(e.target.files?.[0] || null);
                  setResult(null);
                  setError("");
                }}
              />
            </label>

            {page !== "analyze" && (
              <>
                <label className="field-label">Job description</label>
                <textarea
                  value={jobDescription}
                  onChange={(e) => setJobDescription(e.target.value)}
                  placeholder="Paste the full job description here..."
                  rows={7}
                />
              </>
            )}

            {page === "interview" && (
              <>
                <label className="field-label">Number of questions</label>
                <select
                  value={numberOfQuestions}
                  onChange={(e) =>
                    setNumberOfQuestions(Number(e.target.value))
                  }
                >
                  {[5, 10, 15, 20].map((n) => (
                    <option key={n} value={n}>
                      {n} questions
                    </option>
                  ))}
                </select>
              </>
            )}

            {error && <p className="notice">{error}</p>}

            <button
              className="primary full"
              disabled={loading}
              onClick={() => {
                if (page === "analyze") {
                  submitRequest("/resume/upload", {});
                } else if (page === "match") {
                  if (!jobDescription.trim()) {
                    setError("Please enter the job description.");
                    return;
                  }
                  submitRequest("/resume/match", { jobDescription });
                } else {
                  if (!jobDescription.trim()) {
                    setError("Please enter the job description.");
                    return;
                  }
                  submitRequest("/resume/interview-questions", {
                    jobDescription,
                    numberOfQuestions,
                  });
                }
              }}
            >
              {loading
                ? "Analyzing..."
                : page === "analyze"
                  ? "Analyze resume →"
                  : page === "match"
                    ? "Analyze job match →"
                    : "Generate questions →"}
            </button>
            <p className="privacy-note">
              Your request is sent to your configured backend.
            </p>
          </div>

          <div className="panel result-panel">
            <div className="panel-heading">
              <span className="step-number">02</span>
              <div>
                <h3>Results</h3>
                <p>Your AI-generated insights appear here.</p>
              </div>
            </div>

            {!result && !loading && (
              <div className="empty-state">
                <div className="empty-icon">✧</div>
                <h3>Ready when you are</h3>
                <p>
                  Complete the form and run your analysis to see your results.
                </p>
              </div>
            )}

            {loading && (
              <div className="empty-state">
                <div className="spinner" />
                <h3>Working on your request</h3>
                <p>This may take a little while while the AI responds.</p>
              </div>
            )}

            {result && (
              <div className="results">
                <div className="success-label">✓ Response received</div>
                {renderValue(result)}
              </div>
            )}
          </div>
        </section>

        <footer>
          CareerPilot · AI Resume Analyzer · Local development
        </footer>
      </main>
    </div>
  );
}