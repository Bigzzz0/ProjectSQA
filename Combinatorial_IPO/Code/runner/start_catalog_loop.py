"""User-triggered, zero-argument entry point for the real IPO catalog loop."""

from __future__ import annotations

import shutil
import subprocess
from pathlib import Path
from typing import List, Optional


CONTAINER_NAME = "sqa-defects4j"
WINDOWS_DOCKER = Path(
    r"C:\Users\User\AppData\Local\Programs\DockerDesktop\resources\bin\docker.exe"
)
READINESS_COMMAND = (
    "cd /workspace/Combinatorial_IPO && "
    "python3 Code/runner/readiness_check.py"
)
LOOP_COMMAND = (
    "cd /workspace/Combinatorial_IPO && "
    "python3 Code/runner/scenario_catalog.py "
    "--catalog /workspace/target_benchmark/catalog_17_projects.json "
    "--scenarios /workspace/Combinatorial_IPO/Configuration/targets "
    "--output-root /workspace/Combinatorial_IPO"
)


def resolve_docker() -> str:
    """Find Docker Desktop without requiring the user to type a command."""
    discovered = shutil.which("docker") or shutil.which("docker.exe")
    if discovered:
        return discovered
    if WINDOWS_DOCKER.is_file():
        return str(WINDOWS_DOCKER)
    raise RuntimeError("Docker was not found. Start Docker Desktop and try again.")


def docker_arguments(command: str) -> List[str]:
    return ["exec", CONTAINER_NAME, "sh", "-lc", command]


def run_catalog_loop(docker_executable: Optional[str] = None) -> None:
    """Require a green readiness check, then start the real Round 2 run."""
    docker = docker_executable or resolve_docker()
    print("Checking IPO readiness (this does not start the loop)...", flush=True)
    readiness = subprocess.run(
        [docker] + docker_arguments(READINESS_COMMAND), check=False
    )
    if readiness.returncode != 0:
        raise RuntimeError(
            "Readiness check failed. The catalog loop was not started."
        )

    print("Readiness passed. Starting the IPO catalog loop in Result_Round2...", flush=True)
    loop = subprocess.run([docker] + docker_arguments(LOOP_COMMAND), check=False)
    if loop.returncode != 0:
        raise RuntimeError(
            "The catalog loop stopped with an error. Check "
            "Combinatorial_IPO/Result_Round2/catalog_loop_state.json."
        )
    print("IPO catalog loop completed.", flush=True)


if __name__ == "__main__":
    run_catalog_loop()
