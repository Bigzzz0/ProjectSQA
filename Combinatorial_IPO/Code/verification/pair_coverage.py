"""Verify 2-way coverage independently from any generation backend."""

from __future__ import annotations

import itertools
from dataclasses import dataclass
from typing import List, Mapping, Sequence, Set, Tuple


@dataclass(frozen=True, order=True)
class InteractionPair:
    """One value interaction between two ordered factors."""

    first_factor: str
    first_value: str
    second_factor: str
    second_value: str


@dataclass(frozen=True)
class PairCoverageReport:
    """A deterministic summary of expected and observed 2-way interactions."""

    expected_pair_count: int
    covered_pair_count: int
    missing_pairs: Tuple[InteractionPair, ...]

    @property
    def complete(self) -> bool:
        return not self.missing_pairs

    @property
    def coverage_percent(self) -> float:
        if self.expected_pair_count == 0:
            return 100.0
        return round(
            self.covered_pair_count / self.expected_pair_count * 100.0,
            4,
        )


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


def expected_pairs(
    factor_domains: Mapping[str, Sequence[str]],
) -> Set[InteractionPair]:
    """Return every required 2-way interaction for the supplied domains."""
    _validate_domains(factor_domains)
    factors = list(factor_domains)
    pairs: Set[InteractionPair] = set()

    for first_index, second_index in itertools.combinations(
        range(len(factors)), 2
    ):
        first_factor = factors[first_index]
        second_factor = factors[second_index]
        for first_value in factor_domains[first_factor]:
            for second_value in factor_domains[second_factor]:
                pairs.add(
                    InteractionPair(
                        first_factor,
                        first_value,
                        second_factor,
                        second_value,
                    )
                )
    return pairs


def _observed_pairs(
    factor_domains: Mapping[str, Sequence[str]],
    combinations: Sequence[Mapping[str, str]],
) -> Set[InteractionPair]:
    factors = list(factor_domains)
    expected_keys = set(factors)
    observed: Set[InteractionPair] = set()

    for row_number, combination in enumerate(combinations, start=1):
        if set(combination) != expected_keys:
            raise ValueError(
                "Combination row {} must contain exactly factors {}".format(
                    row_number, factors
                )
            )
        for factor in factors:
            value = combination[factor]
            if value not in factor_domains[factor]:
                raise ValueError(
                    "Combination row {} contains value {!r} outside domain {!r}".format(
                        row_number, value, factor
                    )
                )

        for first_index, second_index in itertools.combinations(
            range(len(factors)), 2
        ):
            first_factor = factors[first_index]
            second_factor = factors[second_index]
            observed.add(
                InteractionPair(
                    first_factor,
                    combination[first_factor],
                    second_factor,
                    combination[second_factor],
                )
            )
    return observed


def verify_pair_coverage(
    factor_domains: Mapping[str, Sequence[str]],
    combinations: Sequence[Mapping[str, str]],
) -> PairCoverageReport:
    """Validate rows and report their coverage of all required value pairs."""
    required = expected_pairs(factor_domains)
    observed = _observed_pairs(factor_domains, combinations)
    covered = required & observed
    missing = tuple(sorted(required - covered))
    return PairCoverageReport(
        expected_pair_count=len(required),
        covered_pair_count=len(covered),
        missing_pairs=missing,
    )
