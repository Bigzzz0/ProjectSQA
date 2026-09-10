"""Create a reproducible experiment manifest from approved target keys."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
PROJECT_ROOT = IPO_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from runner.catalog import load_catalog  # noqa: E402
from runner.experiment import build_experiment_manifest  # noqa: E402
from scenario.spec import load_target_plan  # noqa: E402


def create_experiment(
    experiment_id: str,
    target_keys: list[str],
    catalog_path: Path,
    scenario_root: Path,
) -> dict:
    """Require approved scenarios before producing the locked manifest."""
    catalog_by_key = {
        target.target_key: target for target in load_catalog(catalog_path)
    }
    for key in target_keys:
        if key in catalog_by_key:
            load_target_plan(
                scenario_root / "{}.json".format(key), catalog_by_key[key]
            )
    return build_experiment_manifest(
        experiment_id, target_keys, catalog_path, scenario_root
    )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Create a locked IPO experiment manifest"
    )
    parser.add_argument("--experiment-id", required=True)
    parser.add_argument("--target", action="append", required=True)
    parser.add_argument(
        "--catalog",
        type=Path,
        default=PROJECT_ROOT / "target_benchmark" / "catalog_17_projects.json",
    )
    parser.add_argument(
        "--scenarios",
        type=Path,
        default=IPO_ROOT / "Configuration" / "targets",
    )
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    manifest = create_experiment(
        args.experiment_id, args.target, args.catalog, args.scenarios
    )
    if args.output.exists():
        raise FileExistsError(
            "Experiment manifest already exists; choose a new output path: {}".format(
                args.output
            )
        )
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps(manifest, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(manifest, indent=2))


if __name__ == "__main__":
    main()
