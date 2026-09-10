"""Tests for the user-triggered Python catalog-loop entry point."""

from __future__ import annotations

import unittest
from unittest.mock import Mock, patch

from runner.start_catalog_loop import (
    LOOP_COMMAND,
    READINESS_COMMAND,
    run_catalog_loop,
)


class StartCatalogLoopTests(unittest.TestCase):
    def test_green_readiness_starts_round2_catalog_loop(self) -> None:
        completed = Mock(returncode=0)
        with patch(
            "runner.start_catalog_loop.subprocess.run", return_value=completed
        ) as run, patch("builtins.print"):
            run_catalog_loop("docker")

        self.assertEqual(2, run.call_count)
        readiness_args = run.call_args_list[0].args[0]
        loop_args = run.call_args_list[1].args[0]
        self.assertIn(READINESS_COMMAND, readiness_args)
        self.assertIn(LOOP_COMMAND, loop_args)
        self.assertIn("scenario_catalog.py", LOOP_COMMAND)
        self.assertIn("--scenarios /workspace/Combinatorial_IPO/Configuration/targets", LOOP_COMMAND)
        self.assertNotIn("--no-verify", LOOP_COMMAND)

    def test_failed_readiness_never_starts_loop(self) -> None:
        failed = Mock(returncode=1)
        with patch(
            "runner.start_catalog_loop.subprocess.run", return_value=failed
        ) as run, patch("builtins.print"):
            with self.assertRaisesRegex(RuntimeError, "was not started"):
                run_catalog_loop("docker")

        self.assertEqual(1, run.call_count)


if __name__ == "__main__":
    unittest.main()
