#!/usr/bin/env python3
"""
Academic Publication Figure Generator for ProjectSQA
Designed by Member 4 (Infrastructure & Data Analysis Lead)

Generates 4 publication-ready figures for Chapter 5 of the final report:
- Figure 1: Code Coverage Comparison across 4 Testing Techniques
- Figure 2: Fault Detection Rate (FDR %) Distribution (5 Standard Levels)
- Figure 3: Project-by-Project Coverage Breakdown
- Figure 4: AI Economics & Efficiency Comparison (Claude vs Gemini)
"""

import os
import sys
import csv
import math
from collections import defaultdict

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

import matplotlib
matplotlib.use("Agg")  # Headless backend
import matplotlib.pyplot as plt

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPTS_DIR)
RESULTS_DIR = os.path.join(PROJECT_ROOT, "results")
MASTER_CSV = os.path.join(RESULTS_DIR, "master_benchmark_summary.csv")
CSV_PATH = MASTER_CSV if os.path.exists(MASTER_CSV) else os.path.join(RESULTS_DIR, "benchmark_results.csv")

FIG1_PATH = os.path.join(RESULTS_DIR, "figure1_coverage_comparison.png")
FIG2_PATH = os.path.join(RESULTS_DIR, "figure2_fdr_distribution.png")
FIG3_PATH = os.path.join(RESULTS_DIR, "figure3_projects_breakdown.png")
FIG4_PATH = os.path.join(RESULTS_DIR, "figure4_ai_economics.png")

# Styling settings
plt.rcParams.update({
    "font.size": 11,
    "axes.labelsize": 12,
    "axes.titlesize": 13,
    "xtick.labelsize": 10,
    "ytick.labelsize": 10,
    "figure.titlesize": 14,
    "axes.grid": True,
    "grid.alpha": 0.3,
    "grid.linestyle": "--"
})

def load_csv_data():
    if not os.path.exists(CSV_PATH):
        print(f"[!] Error: Results file not found at {CSV_PATH}")
        sys.exit(1)
    rows = []
    with open(CSV_PATH, "r", encoding="utf-8", errors="replace") as f:
        reader = csv.DictReader(f)
        for r in reader:
            rows.append(r)
    return rows

def is_complete_snapshot(rows):
    attempted = {"DONE", "COMPILE_ERROR", "TIMEOUT"}
    unresolved = {"NOT_RUN", "STALE_RESULT", "CHECKOUT_ERROR", "INVALID_SUITE", "RUN_ERROR"}
    if any(r.get("Execution_Status") in unresolved for r in rows):
        return False
    return all(r.get("Execution_Status") in attempted for r in rows if r.get("Suite_Available") == "YES")

def generate_figure1_coverage(rows):
    """Figure 1: Average Line & Branch Coverage across the 4 techniques."""
    tech_line = defaultdict(list)
    tech_branch = defaultdict(list)
    
    for r in rows:
        if r.get("Execution_Status") != "DONE":
            continue
        tech = r.get("Technique", "").strip()
        try:
            l_cov = float(r["Line_Coverage_%"])
            b_cov = float(r["Branch_Coverage_%"])
            tech_line[tech].append(l_cov)
            tech_branch[tech].append(b_cov)
        except Exception:
            pass
            
    desired_order = ["IPO (Native IPO)", "MIO (EvoSuite SBST)", "DeepSeek V4 Flash", "Gemini 3.8 Flash"]
    techniques = [t for t in desired_order if t in tech_line]
    if not techniques:
        techniques = list(tech_line.keys())
        
    avg_lines = [sum(tech_line[t])/len(tech_line[t]) if tech_line[t] else 0.0 for t in techniques]
    avg_branches = [sum(tech_branch[t])/len(tech_branch[t]) if tech_branch[t] else 0.0 for t in techniques]
    
    x = range(len(techniques))
    width = 0.35
    
    fig, ax = plt.subplots(figsize=(10, 6))
    rects1 = ax.bar([i - width/2 for i in x], avg_lines, width, label="Line Coverage (%)", color="#2b5c8f", alpha=0.9)
    rects2 = ax.bar([i + width/2 for i in x], avg_branches, width, label="Branch Coverage (%)", color="#e27c3e", alpha=0.9)
    
    ax.set_ylabel("Coverage Percentage (%)")
    snapshot = "complete" if is_complete_snapshot(rows) else "provisional snapshot"
    ax.set_title(f"Figure 1: Target-Class Coverage (Defects4J; {snapshot})")
    ax.set_xticks(list(x))
    ax.set_xticklabels([f"{t}\n(n={len(tech_line[t])})" for t in techniques], rotation=10, ha="right")
    ax.set_ylim(0, 105)
    ax.legend(loc="upper left")
    
    for rect in rects1:
        h = rect.get_height()
        ax.annotate(f"{h:.1f}%", xy=(rect.get_x() + rect.get_width() / 2, h),
                    xytext=(0, 3), textcoords="offset points", ha="center", va="bottom", fontsize=9, fontweight="bold")
    for rect in rects2:
        h = rect.get_height()
        ax.annotate(f"{h:.1f}%", xy=(rect.get_x() + rect.get_width() / 2, h),
                    xytext=(0, 3), textcoords="offset points", ha="center", va="bottom", fontsize=9, fontweight="bold")
                    
    plt.tight_layout()
    plt.savefig(FIG1_PATH, dpi=300)
    plt.close()
    print(f"✅ Generated: {FIG1_PATH}")

def generate_figure2_fdr(rows):
    """Figure 2: Stacked Bar Chart of 5 Standard Fault Detection Statuses."""
    standard_statuses = ["BUG_DETECTED", "NOT_DETECTED", "FLAKY_OR_REGRESSION", "COMPILE_ERROR", "TIMEOUT"]
    colors = {
        "BUG_DETECTED": "#2ca02c",        # Green
        "NOT_DETECTED": "#7f7f7f",        # Grey
        "FLAKY_OR_REGRESSION": "#ff7f0e",  # Orange
        "COMPILE_ERROR": "#d62728",       # Red
        "TIMEOUT": "#9467bd"              # Purple
    }
    
    def normalize_status(val):
        val_str = str(val).upper()
        if val_str == "BUG_DETECTED":
            return "BUG_DETECTED"
        elif val_str == "FLAKY_OR_REGRESSION":
            return "FLAKY_OR_REGRESSION"
        elif val_str == "COMPILE_ERROR":
            return "COMPILE_ERROR"
        elif val_str == "TIMEOUT":
            return "TIMEOUT"
        elif val_str == "NOT_DETECTED":
            return "NOT_DETECTED"
        return None
            
    tech_counts = defaultdict(lambda: defaultdict(int))
    for r in rows:
        if r.get("Execution_Status") not in {"DONE", "COMPILE_ERROR", "TIMEOUT"}:
            continue
        tech = r.get("Technique", "").strip()
        status_val = r.get("Fault_Detection_Status", r.get("Fault_Detected", ""))
        norm = normalize_status(status_val)
        if norm:
            tech_counts[tech][norm] += 1
        
    desired_order = ["IPO (Native IPO)", "MIO (EvoSuite SBST)", "DeepSeek V4 Flash", "Gemini 3.8 Flash"]
    techniques = [t for t in desired_order if t in tech_counts]
    if not techniques:
        techniques = list(tech_counts.keys())
        
    fig, ax = plt.subplots(figsize=(11, 6))
    bottoms = [0.0] * len(techniques)
    
    for st in standard_statuses:
        percentages = []
        for t in techniques:
            tot = sum(tech_counts[t].values())
            pct = (tech_counts[t][st] / tot * 100.0) if tot > 0 else 0.0
            percentages.append(pct)
            
        ax.bar(techniques, percentages, bottom=bottoms, label=st, color=colors[st], width=0.55, edgecolor="white")
        bottoms = [b + p for b, p in zip(bottoms, percentages)]
        
    ax.set_ylabel("Proportion of Evaluated Bugs (%)")
    snapshot = "complete" if is_complete_snapshot(rows) else "provisional snapshot"
    ax.set_title(f"Figure 2: Fault Detection Distribution (Bug-Level; {snapshot})")
    ax.set_ylim(0, 105)
    ax.set_xticks(range(len(techniques)))
    ax.set_xticklabels([f"{t}\n(n={sum(tech_counts[t].values())})" for t in techniques], rotation=10, ha="right")
    ax.legend(title="Classification", bbox_to_anchor=(1.02, 1), loc="upper left")
    
    plt.tight_layout()
    plt.savefig(FIG2_PATH, dpi=300)
    plt.close()
    print(f"✅ Generated: {FIG2_PATH}")

def generate_figure3_projects(rows):
    """Figure 3: Project-by-Project Coverage Breakdown."""
    proj_tech_cov = defaultdict(lambda: defaultdict(list))
    for r in rows:
        if r.get("Execution_Status") != "DONE":
            continue
        p = r.get("Project", "").strip()
        t = r.get("Technique", "").strip()
        try:
            cov = float(r["Line_Coverage_%"])
            proj_tech_cov[p][t].append(cov)
        except Exception:
            pass
            
    projects = sorted({r.get("Project", "").strip() for r in rows if r.get("Project")})
    desired_order = ["IPO (Native IPO)", "MIO (EvoSuite SBST)", "DeepSeek V4 Flash", "Gemini 3.8 Flash"]
    
    fig, ax = plt.subplots(figsize=(13, 6))
    x = list(range(len(projects)))
    width = 0.18
    
    for idx, t in enumerate(desired_order):
        vals = []
        for p in projects:
            covs = proj_tech_cov[p].get(t, [])
            vals.append(sum(covs)/len(covs) if covs else math.nan)
        pos = [i + (idx - 1.5) * width for i in x]
        ax.bar(pos, vals, width=width, label=t)
        
    ax.set_ylabel("Line Coverage (%)")
    snapshot = "complete" if is_complete_snapshot(rows) else "provisional snapshot"
    ax.set_title(f"Figure 3: Target-Class Coverage by Project ({snapshot}; missing values omitted)")
    ax.set_xticks(x)
    ax.set_xticklabels(projects, rotation=25, ha="right")
    ax.set_ylim(0, 105)
    ax.legend(title="Technique", bbox_to_anchor=(1.02, 1), loc="upper left")
    
    plt.tight_layout()
    plt.savefig(FIG3_PATH, dpi=300)
    plt.close()
    print(f"✅ Generated: {FIG3_PATH}")

def generate_figure4_ai_economics():
    """Figure 4: Token Economics & Generation Latency (DeepSeek vs Gemini)."""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    
    deepseek_csv = os.path.join(RESULTS_DIR, "Deepseek_vs_Gemini_Economics.csv")
    claude_csv = os.path.join(RESULTS_DIR, "Claude_vs_Gemini_Economics.csv")
    econ_csv = deepseek_csv if os.path.exists(deepseek_csv) else (claude_csv if os.path.exists(claude_csv) else None)
    
    model_data = defaultdict(lambda: {"tokens": [], "latencies": []})
    if econ_csv:
        with open(econ_csv, "r", encoding="utf-8", errors="replace") as f:
            reader = csv.DictReader(f)
            for r in reader:
                m = r.get("Model", "").strip()
                try:
                    tot_tok = int(r.get("Total_Tokens", 0) or 0)
                    raw_sec = r.get("Generation_Time_Sec") or r.get("Generation_Time_s") or "0.0"
                    sec = float(raw_sec)
                    if tot_tok > 0:
                        model_data[m]["tokens"].append(tot_tok)
                    if sec > 0:
                        model_data[m]["latencies"].append(sec)
                except Exception:
                    pass

    models = []
    avg_tokens = []
    avg_latency = []
    
    # Priority order
    model_order = ["deepseek-v4-flash", "gemini-3.8-flash", "claude-sonnet-5"]
    for m in model_order:
        if m in model_data and model_data[m]["tokens"]:
            disp = "DeepSeek V4 Flash" if "deepseek" in m else ("Gemini 3.8 Flash" if "gemini" in m else "Claude Sonnet 5")
            models.append(f"{disp}\n(n={len(model_data[m]['tokens'])})")
            avg_tokens.append(int(sum(model_data[m]["tokens"]) / len(model_data[m]["tokens"])))
            lats = model_data[m]["latencies"]
            avg_latency.append(round(sum(lats) / len(lats), 1) if lats else 0.0)
            
    if not models:
        for ax in (ax1, ax2):
            ax.set_axis_off()
            ax.text(0.5, 0.5, "No measured AI generation records", ha="center", va="center", transform=ax.transAxes)
        plt.suptitle("Figure 4: AI Model Economics & Latency Comparison (source records only)", fontsize=13)
        plt.tight_layout()
        plt.savefig(FIG4_PATH, dpi=300)
        plt.close()
        print(f"✅ Generated: {FIG4_PATH}")
        return
        
    colors = ["#4a90e2", "#1a73e8"] if len(models) == 2 else ["#4a90e2", "#1a73e8", "#7c5295"][:len(models)]
    
    ax1.bar(models, avg_tokens, color=colors, width=0.5, alpha=0.9)
    ax1.set_ylabel("Average Tokens per Test Suite")
    ax1.set_title("Average Token Consumption")
    for i, v in enumerate(avg_tokens):
        ax1.text(i, v + max(avg_tokens) * 0.02, f"{v:,} tokens", ha="center", fontweight="bold")
    ax1.set_ylim(0, max(avg_tokens) * 1.2 if avg_tokens else 100)
    
    ax2.bar(models, avg_latency, color=colors, width=0.5, alpha=0.9)
    ax2.set_ylabel("Average Latency (Seconds)")
    ax2.set_title("Generation Latency (Speed)")
    for i, v in enumerate(avg_latency):
        ax2.text(i, v + max(avg_latency) * 0.02, f"{v:.1f}s", ha="center", fontweight="bold")
    ax2.set_ylim(0, max(avg_latency) * 1.2 if avg_latency else 10)
    
    plt.suptitle("Figure 4: AI Suite-Generation Records (Tokens and Generation Time)", fontsize=13)
    plt.tight_layout()
    plt.savefig(FIG4_PATH, dpi=300)
    plt.close()
    print(f"✅ Generated: {FIG4_PATH}")

def main():
    rows = load_csv_data()
    print(f"📊 Processing {len(rows)} evaluation records from {CSV_PATH}...")
    
    generate_figure1_coverage(rows)
    generate_figure2_fdr(rows)
    generate_figure3_projects(rows)
    generate_figure4_ai_economics()
    print(f"\n🎉 All 4 publication figures generated successfully in {RESULTS_DIR}!")

if __name__ == "__main__":
    main()
