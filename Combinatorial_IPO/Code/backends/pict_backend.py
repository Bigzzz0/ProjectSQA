"""Microsoft PICT adapter for reference pairwise generation.

IPO/IPOG is the theoretical algorithm studied by the project.  Microsoft PICT
is used here only as a separate empirical baseline and verification aid; this
module does not claim that PICT itself implements IPO.
"""

from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path
from typing import Dict, List, Mapping, Optional, Sequence, Tuple


class PictGenerationError(RuntimeError):
    """Raised when PICT cannot produce a valid covering array."""


def _validate_domains(param_domains: Mapping[str, Sequence[str]]) -> None:
    if not param_domains:
        raise ValueError("At least one parameter domain is required")

    for name, values in param_domains.items():
        if not name or any(character in name for character in ":,\t\r\n"):
            raise ValueError("Invalid PICT parameter name: {!r}".format(name))
        if not values:
            raise ValueError("Domain for {!r} must not be empty".format(name))
        if len(set(values)) != len(values):
            raise ValueError("Domain for {!r} contains duplicate values".format(name))


def build_pict_model(
    param_domains: Mapping[str, Sequence[str]],
) -> Tuple[str, Dict[str, Dict[str, str]]]:
    """Encode Java expressions as safe PICT tokens and return their lookup."""
    _validate_domains(param_domains)
    token_lookup: Dict[str, Dict[str, str]] = {}
    lines: List[str] = []

    for parameter_index, (name, values) in enumerate(param_domains.items()):
        value_lookup = {
            "P{:03d}V{:03d}".format(parameter_index, value_index): value
            for value_index, value in enumerate(values)
        }
        token_lookup[name] = value_lookup
        lines.append("{}: {}".format(name, ", ".join(value_lookup)))

    return "\n".join(lines) + "\n", token_lookup


def parse_pict_output(
    output: str,
    token_lookup: Mapping[str, Mapping[str, str]],
) -> List[Dict[str, str]]:
    """Decode tab-separated PICT output into Java-expression combinations."""
    lines = [line.rstrip("\r") for line in output.splitlines() if line.strip()]
    if len(lines) < 2:
        raise PictGenerationError("PICT output contains no combinations")

    headers = lines[0].split("\t")
    if headers != list(token_lookup):
        raise PictGenerationError(
            "PICT headers do not match model parameters: {!r}".format(headers)
        )

    combinations: List[Dict[str, str]] = []
    for row_number, line in enumerate(lines[1:], start=1):
        tokens = line.split("\t")
        if len(tokens) != len(headers):
            raise PictGenerationError("Malformed PICT row {}".format(row_number))

        combination: Dict[str, str] = {}
        for name, token in zip(headers, tokens):
            try:
                combination[name] = token_lookup[name][token]
            except KeyError as exc:
                raise PictGenerationError(
                    "Unknown token {!r} for parameter {!r}".format(token, name)
                ) from exc
        combinations.append(combination)

    return combinations


def build_pict_seed(
    seed_combinations: Sequence[Mapping[str, str]],
    token_lookup: Mapping[str, Mapping[str, str]],
) -> str:
    """Encode complete, concrete seed rows in PICT's tab-separated format."""
    if not seed_combinations:
        return ""

    headers = list(token_lookup)
    reverse_lookup = {
        name: {value: token for token, value in values.items()}
        for name, values in token_lookup.items()
    }
    lines = ["\t".join(headers)]
    for row_number, combination in enumerate(seed_combinations, start=1):
        if set(combination) != set(headers):
            raise ValueError(
                "Seed row {} must contain exactly {}".format(row_number, headers)
            )
        encoded: List[str] = []
        for name in headers:
            value = combination[name]
            try:
                encoded.append(reverse_lookup[name][value])
            except KeyError as exc:
                raise ValueError(
                    "Seed value {!r} is not in domain {!r}".format(value, name)
                ) from exc
        lines.append("\t".join(encoded))
    return "\n".join(lines) + "\n"


def generate_pairwise(
    param_domains: Mapping[str, Sequence[str]],
    pict_executable: str = "pict",
    timeout_seconds: int = 60,
    seed_combinations: Sequence[Mapping[str, str]] = (),
) -> List[Dict[str, str]]:
    """Generate pairwise combinations with PICT and return Java expressions."""
    model_text, token_lookup = build_pict_model(param_domains)

    # With one factor there are no pairs; covering every value is sufficient.
    if len(param_domains) == 1:
        name, values = next(iter(param_domains.items()))
        return [{name: value} for value in values]

    model_path: Optional[Path] = None
    seed_path: Optional[Path] = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", encoding="utf-8", delete=False
        ) as model_file:
            model_file.write(model_text)
            model_path = Path(model_file.name)

        command = [pict_executable, str(model_path)]
        seed_text = build_pict_seed(seed_combinations, token_lookup)
        if seed_text:
            with tempfile.NamedTemporaryFile(
                mode="w", suffix=".txt", encoding="utf-8", delete=False
            ) as seed_file:
                seed_file.write(seed_text)
                seed_path = Path(seed_file.name)
            command.append("/e:{}".format(seed_path))

        completed = subprocess.run(
            command,
            capture_output=True,
            text=True,
            timeout=timeout_seconds,
            check=False,
        )
        if completed.returncode != 0:
            message = completed.stderr.strip() or completed.stdout.strip()
            raise PictGenerationError(
                "PICT exited with code {}: {}".format(completed.returncode, message)
            )
        return parse_pict_output(completed.stdout, token_lookup)
    except FileNotFoundError as exc:
        raise PictGenerationError(
            "PICT executable was not found: {!r}".format(pict_executable)
        ) from exc
    except subprocess.TimeoutExpired as exc:
        raise PictGenerationError(
            "PICT exceeded the {} second timeout".format(timeout_seconds)
        ) from exc
    finally:
        if model_path is not None:
            model_path.unlink(missing_ok=True)
        if seed_path is not None:
            seed_path.unlink(missing_ok=True)
