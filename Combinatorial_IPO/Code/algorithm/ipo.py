"""Native deterministic In-Parameter-Order generation for 2-way coverage.

The implementation follows the IPO framework: construct the Cartesian product
of the first two factors, then add each remaining factor using horizontal growth
followed by vertical growth.  Mandatory seed rows are an explicit engineering
augmentation applied after IPO generation; they are not attributed to IPO's
covering-array reduction.
"""

from __future__ import annotations

import itertools
from dataclasses import dataclass
from typing import Dict, List, Mapping, Optional, Sequence, Set, Tuple


@dataclass(frozen=True, order=True)
class NewFactorPair:
    """One pair between a previous factor and the factor being added."""

    previous_factor: str
    previous_value: str
    new_value: str


def _validate_domains(factor_domains: Mapping[str, Sequence[str]]) -> None:
    if not factor_domains:
        raise ValueError("At least one factor domain is required")

    for factor, values in factor_domains.items():
        if not isinstance(factor, str) or not factor:
            raise ValueError("Factor names must be non-empty strings")
        if not values:
            raise ValueError("Domain for {!r} must not be empty".format(factor))
        if any(not isinstance(value, str) for value in values):
            raise ValueError("Domain values for {!r} must be strings".format(factor))
        if len(set(values)) != len(values):
            raise ValueError(
                "Domain for {!r} contains duplicate values".format(factor)
            )


def initial_construction(
    factor_domains: Mapping[str, Sequence[str]],
    valid_combinations: Optional[Sequence[Mapping[str, str]]] = None,
) -> List[Dict[str, str]]:
    """Construct the complete test set for the first two factors."""
    _validate_domains(factor_domains)
    factors = list(factor_domains)
    if len(factors) < 2:
        raise ValueError("Initial IPO construction requires at least two factors")

    first_factor, second_factor = factors[:2]
    rows = [
        {first_factor: first_value, second_factor: second_value}
        for first_value, second_value in itertools.product(
            factor_domains[first_factor],
            factor_domains[second_factor],
        )
    ]
    if valid_combinations is None:
        return rows
    valid_prefixes = {
        (row[first_factor], row[second_factor]) for row in valid_combinations
    }
    return [
        row
        for row in rows
        if (row[first_factor], row[second_factor]) in valid_prefixes
    ]


def _normalize_valid_combinations(
    factor_domains: Mapping[str, Sequence[str]],
    valid_combinations: Optional[Sequence[Mapping[str, str]]],
) -> Optional[List[Dict[str, str]]]:
    if valid_combinations is None:
        return None
    factors = list(factor_domains)
    expected_keys = set(factors)
    normalized: List[Dict[str, str]] = []
    seen = set()
    for row_number, source in enumerate(valid_combinations, start=1):
        if set(source) != expected_keys:
            raise ValueError(
                "Valid combination {} must contain exactly factors {}".format(
                    row_number, factors
                )
            )
        row = {factor: source[factor] for factor in factors}
        for factor, value in row.items():
            if value not in factor_domains[factor]:
                raise ValueError(
                    "Valid combination {} contains value {!r} outside domain {!r}".format(
                        row_number, value, factor
                    )
                )
        key = tuple(row[factor] for factor in factors)
        if key not in seen:
            normalized.append(row)
            seen.add(key)
    if not normalized:
        raise ValueError("Constraints leave no valid combinations")
    return normalized


def _can_extend(
    partial: Mapping[str, str],
    valid_combinations: Optional[Sequence[Mapping[str, str]]],
) -> bool:
    return valid_combinations is None or any(
        all(candidate.get(factor) == value for factor, value in partial.items())
        for candidate in valid_combinations
    )


def _growth_context(
    factor_domains: Mapping[str, Sequence[str]],
    new_factor: str,
) -> Tuple[List[str], Set[NewFactorPair]]:
    _validate_domains(factor_domains)
    factors = list(factor_domains)
    if new_factor not in factor_domains:
        raise ValueError("Unknown new factor: {!r}".format(new_factor))

    new_index = factors.index(new_factor)
    if new_index < 2:
        raise ValueError("Growth requires at least two previous factors")
    previous_factors = factors[:new_index]
    required = {
        NewFactorPair(previous_factor, previous_value, new_value)
        for previous_factor in previous_factors
        for previous_value in factor_domains[previous_factor]
        for new_value in factor_domains[new_factor]
    }
    return previous_factors, required


def _validate_existing_rows(
    rows: Sequence[Mapping[str, str]],
    factor_domains: Mapping[str, Sequence[str]],
    expected_factors: Sequence[str],
) -> None:
    expected_keys = set(expected_factors)
    for row_number, row in enumerate(rows, start=1):
        if set(row) != expected_keys:
            raise ValueError(
                "IPO row {} must contain exactly factors {}".format(
                    row_number, list(expected_factors)
                )
            )
        for factor in expected_factors:
            if row[factor] not in factor_domains[factor]:
                raise ValueError(
                    "IPO row {} contains value {!r} outside domain {!r}".format(
                        row_number, row[factor], factor
                    )
                )


def horizontal_growth(
    rows: Sequence[Mapping[str, str]],
    factor_domains: Mapping[str, Sequence[str]],
    new_factor: str,
    valid_combinations: Optional[Sequence[Mapping[str, str]]] = None,
) -> Tuple[List[Dict[str, str]], Set[NewFactorPair]]:
    """Extend each row with the value covering the most missing new pairs."""
    previous_factors, uncovered = _growth_context(factor_domains, new_factor)
    if valid_combinations is not None:
        uncovered = {
            pair
            for pair in uncovered
            if any(
                row[pair.previous_factor] == pair.previous_value
                and row[new_factor] == pair.new_value
                for row in valid_combinations
            )
        }
    _validate_existing_rows(rows, factor_domains, previous_factors)
    extended_rows: List[Dict[str, str]] = []

    for source_row in rows:
        best_value = factor_domains[new_factor][0]
        best_score = -1
        for candidate in factor_domains[new_factor]:
            proposed = dict(source_row)
            proposed[new_factor] = candidate
            if not _can_extend(proposed, valid_combinations):
                continue
            score = sum(
                NewFactorPair(
                    previous_factor,
                    source_row[previous_factor],
                    candidate,
                )
                in uncovered
                for previous_factor in previous_factors
            )
            if score > best_score:
                best_value = candidate
                best_score = score

        if best_score < 0:
            raise ValueError("A valid IPO row cannot be extended by {!r}".format(new_factor))
        extended_row = dict(source_row)
        extended_row[new_factor] = best_value
        extended_rows.append(extended_row)
        for previous_factor in previous_factors:
            uncovered.discard(
                NewFactorPair(
                    previous_factor,
                    source_row[previous_factor],
                    best_value,
                )
            )

    return extended_rows, uncovered


def vertical_growth(
    rows: Sequence[Mapping[str, str]],
    factor_domains: Mapping[str, Sequence[str]],
    new_factor: str,
    uncovered_pairs: Set[NewFactorPair],
    valid_combinations: Optional[Sequence[Mapping[str, str]]] = None,
) -> List[Dict[str, str]]:
    """Add the minimum rows needed by IPO_V for each new-factor value."""
    previous_factors, possible_pairs = _growth_context(
        factor_domains, new_factor
    )
    all_factors = previous_factors + [new_factor]
    _validate_existing_rows(rows, factor_domains, all_factors)
    if not uncovered_pairs <= possible_pairs:
        raise ValueError("Uncovered pairs contain interactions outside the domains")

    completed_rows = [dict(row) for row in rows]
    if valid_combinations is not None:
        remaining = set(uncovered_pairs)
        prefix_keys = set()
        candidates: List[Dict[str, str]] = []
        for valid_row in valid_combinations:
            prefix = {factor: valid_row[factor] for factor in all_factors}
            key = tuple(prefix[factor] for factor in all_factors)
            if key not in prefix_keys:
                candidates.append(prefix)
                prefix_keys.add(key)
        while remaining:
            best_row = None
            best_covered: Set[NewFactorPair] = set()
            for candidate in candidates:
                covered = {
                    pair
                    for pair in remaining
                    if candidate[pair.previous_factor] == pair.previous_value
                    and candidate[new_factor] == pair.new_value
                }
                if len(covered) > len(best_covered):
                    best_row = candidate
                    best_covered = covered
            if best_row is None or not best_covered:
                raise ValueError("Constraint-aware vertical growth cannot cover valid pairs")
            completed_rows.append(dict(best_row))
            remaining -= best_covered
        return completed_rows
    for new_value in factor_domains[new_factor]:
        missing_by_factor = {
            previous_factor: [
                previous_value
                for previous_value in factor_domains[previous_factor]
                if NewFactorPair(
                    previous_factor,
                    previous_value,
                    new_value,
                )
                in uncovered_pairs
            ]
            for previous_factor in previous_factors
        }
        new_row_count = max(
            (len(values) for values in missing_by_factor.values()),
            default=0,
        )
        for row_index in range(new_row_count):
            added_row: Dict[str, str] = {}
            for previous_factor in previous_factors:
                missing_values = missing_by_factor[previous_factor]
                added_row[previous_factor] = (
                    missing_values[row_index]
                    if row_index < len(missing_values)
                    else factor_domains[previous_factor][0]
                )
            added_row[new_factor] = new_value
            completed_rows.append(added_row)

    return completed_rows


def _append_mandatory_seeds(
    rows: List[Dict[str, str]],
    factor_domains: Mapping[str, Sequence[str]],
    seed_combinations: Sequence[Mapping[str, str]],
) -> None:
    factors = list(factor_domains)
    expected_keys = set(factors)
    existing = {tuple(row[factor] for factor in factors) for row in rows}

    for seed_number, seed in enumerate(seed_combinations, start=1):
        if set(seed) != expected_keys:
            raise ValueError(
                "Seed row {} must contain exactly factors {}".format(
                    seed_number, factors
                )
            )
        for factor in factors:
            if seed[factor] not in factor_domains[factor]:
                raise ValueError(
                    "Seed row {} contains value {!r} outside domain {!r}".format(
                        seed_number, seed[factor], factor
                    )
                )
        key = tuple(seed[factor] for factor in factors)
        if key not in existing:
            rows.append({factor: seed[factor] for factor in factors})
            existing.add(key)


def generate_pairwise(
    factor_domains: Mapping[str, Sequence[str]],
    seed_combinations: Sequence[Mapping[str, str]] = (),
    valid_combinations: Optional[Sequence[Mapping[str, str]]] = None,
) -> List[Dict[str, str]]:
    """Generate a deterministic 2-way covering array using native IPO."""
    _validate_domains(factor_domains)
    factors = list(factor_domains)
    valid_rows = _normalize_valid_combinations(factor_domains, valid_combinations)

    if len(factors) == 1:
        only_factor = factors[0]
        rows = [
            {only_factor: value} for value in factor_domains[only_factor]
        ]
    else:
        rows = initial_construction(factor_domains, valid_rows)
        for new_factor in factors[2:]:
            rows, uncovered = horizontal_growth(
                rows,
                factor_domains,
                new_factor,
                valid_rows,
            )
            rows = vertical_growth(
                rows,
                factor_domains,
                new_factor,
                uncovered,
                valid_rows,
            )

    if valid_rows is not None:
        rows = [row for row in rows if _can_extend(row, valid_rows)]
    _append_mandatory_seeds(rows, factor_domains, seed_combinations)
    if valid_rows is not None:
        valid_keys = {
            tuple(row[factor] for factor in factors) for row in valid_rows
        }
        for seed in seed_combinations:
            if tuple(seed[factor] for factor in factors) not in valid_keys:
                raise ValueError("Mandatory seed violates scenario constraints")
    return rows
