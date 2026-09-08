#!/usr/bin/env python3
"""
Defects4J Metadata & CLI Wrapper Module for ProjectSQA
Designed by Member 4 (Infrastructure & Data Analysis Lead)

Provides dynamic, non-hardcoded access to Defects4J commands, project lists,
bug IDs, modified classes, classpaths, and Cobertura coverage parsing.
"""

import os
import sys
import subprocess
import xml.etree.ElementTree as ET
from typing import List, Dict, Optional, Tuple

def run_cmd(cmd: List[str], cwd: Optional[str] = None, timeout: int = 120) -> Tuple[int, str, str]:
    """Execute a system command and return (exit_code, stdout, stderr)."""
    try:
        proc = subprocess.run(
            cmd,
            cwd=cwd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=timeout,
            encoding='utf-8',
            errors='replace'
        )
        return proc.returncode, proc.stdout.strip(), proc.stderr.strip()
    except subprocess.TimeoutExpired:
        return -1, "", "Command timed out"
    except Exception as e:
        return -1, "", str(e)

def is_d4j_available() -> bool:
    """Check if defects4j CLI is available in the environment."""
    code, out, _ = run_cmd(["defects4j", "pids"], timeout=15)
    return code == 0 and len(out) > 0

def get_all_projects() -> List[str]:
    """
    Retrieve list of all active projects in Defects4J dynamically via 'defects4j pids'.
    Returns list of project IDs, e.g., ['Chart', 'Cli', 'Closure', ..., 'Time'].
    """
    code, out, err = run_cmd(["defects4j", "pids"], timeout=30)
    if code != 0:
        # Fallback standard list if command fails
        return [
            "Chart", "Cli", "Closure", "Codec", "Collections",
            "Compress", "Csv", "Gson", "JacksonCore", "JacksonDatabind",
            "JacksonXml", "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"
        ]
    projects = [line.strip() for line in out.splitlines() if line.strip()]
    return projects

def get_active_bugs(project: str) -> List[int]:
    """
    Retrieve all active bug IDs for a given project dynamically via 'defects4j bids -p <project>'.
    """
    code, out, err = run_cmd(["defects4j", "bids", "-p", project], timeout=30)
    if code != 0:
        return []
    bugs = []
    for token in out.replace('\n', ' ').split():
        token = token.strip()
        if token.isdigit():
            bugs.append(int(token))
    return sorted(bugs)

def checkout_project(project: str, bug_id: int, is_buggy: bool, work_dir: str) -> bool:
    """
    Checkout a project version from Defects4J.
    is_buggy=True -> '<bug_id>b', is_buggy=False -> '<bug_id>f'
    """
    version = f"{bug_id}b" if is_buggy else f"{bug_id}f"
    if os.path.exists(work_dir) and os.path.isdir(work_dir):
        # Already checked out
        return True
    
    os.makedirs(os.path.dirname(os.path.abspath(work_dir)), exist_ok=True)
    code, out, err = run_cmd(["defects4j", "checkout", "-p", project, "-v", version, "-w", work_dir], timeout=180)
    return code == 0

def compile_project(work_dir: str) -> bool:
    """Compile the checked-out project."""
    code, out, err = run_cmd(["defects4j", "compile", "-w", work_dir], timeout=180)
    return code == 0

def export_metadata(work_dir: str, property_name: str) -> str:
    """
    Export metadata from Defects4J working directory.
    Properties: dir.src.classes, dir.src.tests, cp.compile, cp.test, classes.modified, tests.trigger
    """
    code, out, err = run_cmd(["defects4j", "export", "-p", property_name, "-w", work_dir], timeout=60)
    if code == 0:
        return out.strip()
    return ""

def get_modified_classes(work_dir: str) -> List[str]:
    """Get list of modified classes (the faulty classes) for this bug."""
    out = export_metadata(work_dir, "classes.modified")
    if not out:
        return []
    return [line.strip() for line in out.splitlines() if line.strip()]

def get_trigger_tests(work_dir: str) -> List[str]:
    """Get list of trigger tests (ground truth failing tests) for this bug."""
    out = export_metadata(work_dir, "tests.trigger")
    if not out:
        return []
    return [line.strip() for line in out.splitlines() if line.strip()]

def parse_cobertura_coverage(coverage_xml_path: str, target_classes: Optional[List[str]] = None) -> Dict[str, float]:
    """
    Parse Cobertura coverage.xml.
    If target_classes is provided, compute line and branch coverage specifically for those classes.
    Otherwise, return project-wide coverage.
    Returns: {'line_coverage': float, 'branch_coverage': float} (in percent, 0.0 to 100.0)
    """
    if not os.path.exists(coverage_xml_path):
        return {"line_coverage": 0.0, "branch_coverage": 0.0}
    
    try:
        tree = ET.parse(coverage_xml_path)
        root = tree.getroot()
        
        if not target_classes:
            # Overall project coverage
            line_rate = float(root.attrib.get("line-rate", 0.0)) * 100.0
            branch_rate = float(root.attrib.get("branch-rate", 0.0)) * 100.0
            return {
                "line_coverage": round(line_rate, 2),
                "branch_coverage": round(branch_rate, 2)
            }
        
        # Target specific classes
        total_lines = 0
        covered_lines = 0
        total_branches = 0
        covered_branches = 0
        
        target_set = set(target_classes)
        
        for cls in root.iter("class"):
            cls_name = cls.attrib.get("name", "")
            if cls_name in target_set or any(cls_name.endswith(tc) for tc in target_set):
                for line in cls.iter("line"):
                    total_lines += 1
                    if int(line.attrib.get("hits", 0)) > 0:
                        covered_lines += 1
                    
                    # Branch condition coverage
                    if line.attrib.get("branch") == "true":
                        cond_coverage = line.attrib.get("condition-coverage", "")
                        # Format: "50% (1/2)"
                        if "(" in cond_coverage and "/" in cond_coverage:
                            nums = cond_coverage.split("(")[1].split(")")[0].split("/")
                            c_br = int(nums[0])
                            t_br = int(nums[1])
                            covered_branches += c_br
                            total_branches += t_br
        
        line_cov = (covered_lines / total_lines * 100.0) if total_lines > 0 else 0.0
        branch_cov = (covered_branches / total_branches * 100.0) if total_branches > 0 else 0.0
        
        return {
            "line_coverage": round(line_cov, 2),
            "branch_coverage": round(branch_cov, 2),
            "total_lines": total_lines,
            "covered_lines": covered_lines,
            "total_branches": total_branches,
            "covered_branches": covered_branches
        }
    except Exception as e:
        print(f"Warning: Failed to parse {coverage_xml_path}: {e}")
        return {"line_coverage": 0.0, "branch_coverage": 0.0}

if __name__ == "__main__":
    print("Testing Defects4J Metadata Module...")
    print("Defects4J available:", is_d4j_available())
    projects = get_all_projects()
    print(f"Found {len(projects)} projects: {', '.join(projects[:5])}...")
    if "Lang" in projects:
        lang_bugs = get_active_bugs("Lang")
        print(f"Lang has {len(lang_bugs)} active bugs. First 5: {lang_bugs[:5]}")
