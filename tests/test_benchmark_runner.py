import csv
import json
import os
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import scripts.run_benchmark as runner


class BenchmarkRunnerTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.work_base = self.root / "work"
        self.results_dir = self.root / "results"
        self.suite_dir = self.root / "suites"
        self.work_base.mkdir()
        self.results_dir.mkdir()
        self.suite_dir.mkdir()
        self.classes = ["org.example.Target", "org.example.Helper"]
        self.suites = [self._write_suite("TargetGeminiTest.java"), self._write_suite("HelperGeminiTest.java")]
        self.case = {
            "failures_buggy": 1,
            "failures_fixed": 0,
            "compile_error": False,
            "timeout": False,
            "missing_summary": False,
        }
        self.current_classes = self.classes
        self.current_suites = self.suites
        self.checkout_calls = []

        def checkout(project, bug_id, is_buggy, work_dir):
            self.checkout_calls.append((project, bug_id, is_buggy))
            os.makedirs(work_dir, exist_ok=True)
            return True

        def run_cmd(command, timeout=None):
            stage = command[1]
            work_dir = command[command.index("-w") + 1]
            if stage == "coverage":
                if self.case["timeout"]:
                    return 1, "", "command timed out"
                if self.case["compile_error"]:
                    return 1, "", "cannot compile generated tests"
                if self.case["missing_summary"]:
                    return 0, "coverage completed without a summary", ""
                Path(work_dir, "summary.csv").write_text(
                    "LinesTotal,LinesCovered,ConditionsTotal,ConditionsCovered\n10,7,4,2\n",
                    encoding="utf-8",
                )
                return 0, "coverage complete", ""

            is_buggy = work_dir.endswith("b")
            count = self.case["failures_buggy" if is_buggy else "failures_fixed"]
            failure_file = Path(work_dir, "failing_tests")
            lines = "".join(f"--- org.example.TargetGeminiTest::test_{i}\n" for i in range(count))
            failure_file.write_text(lines, encoding="utf-8")
            return (1 if count else 0), "tests complete", ""

        self.patches = [
            patch.object(runner, "WORK_BASE", str(self.work_base)),
            patch.object(runner, "RESULTS_DIR", str(self.results_dir)),
            patch.object(runner, "load_catalog_targets", return_value={("Demo", 1): self.classes}),
            patch.object(runner, "find_test_files_for_target", side_effect=self.find_suites),
            patch.object(runner.d4j_meta, "checkout_project", side_effect=checkout),
            patch.object(runner.d4j_meta, "get_modified_classes", return_value=self.classes),
            patch.object(runner.d4j_meta, "run_cmd", side_effect=run_cmd),
        ]
        for item in self.patches:
            item.start()
        self.csv_path = str(self.results_dir / "benchmark.csv")

    def tearDown(self):
        for item in reversed(self.patches):
            item.stop()
        self.temp.cleanup()

    def _write_suite(self, filename):
        path = self.suite_dir / filename
        path.write_text("package org.example;\npublic class Test {}\n", encoding="utf-8")
        return str(path)

    def find_suites(self, technique, project, bug_id, classes):
        return self.current_suites

    def evaluate(self):
        return runner.evaluate_technique_on_bug("Demo", 1, "gemini", self.csv_path)

    def latest_csv_row(self):
        with open(self.csv_path, newline="", encoding="utf-8") as stream:
            return next(csv.DictReader(stream))

    def test_bug_detected_requires_buggy_failure_and_fixed_pass(self):
        result = self.evaluate()
        self.assertEqual(result["fault_detected"], "BUG_DETECTED")
        self.assertEqual(result["buggy_failures"], ["org.example.TargetGeminiTest::test_0"])
        self.assertEqual(result["fixed_failures"], [])

    def test_not_detected_when_both_versions_pass(self):
        self.case.update(failures_buggy=0, failures_fixed=0)
        self.assertEqual(self.evaluate()["fault_detected"], "NOT_DETECTED")

    def test_failure_on_both_versions_is_flaky_or_regression(self):
        self.case.update(failures_buggy=1, failures_fixed=1)
        self.assertEqual(self.evaluate()["fault_detected"], "FLAKY_OR_REGRESSION")

    def test_fixed_only_failure_is_regression_not_not_detected(self):
        self.case.update(failures_buggy=0, failures_fixed=1)
        self.assertEqual(self.evaluate()["fault_detected"], "FLAKY_OR_REGRESSION")

    def test_compile_error_has_no_measured_coverage(self):
        self.case["compile_error"] = True
        result = self.evaluate()
        self.assertEqual(result["status"], "COMPILE_ERROR")
        self.assertEqual(self.latest_csv_row()["Line_Coverage_%"], "")

    def test_missing_coverage_summary_is_not_reported_as_zero(self):
        self.case["missing_summary"] = True
        result = self.evaluate()
        row = self.latest_csv_row()
        self.assertEqual(result["status"], "DONE")
        self.assertIsNone(result["line_cov"])
        self.assertIsNone(result["branch_cov"])
        self.assertEqual(row["Line_Coverage_%"], "")
        self.assertEqual(row["Branch_Coverage_%"], "")
        self.assertIn("summary.csv is missing", row["Error_Detail"])
        self.assertEqual(row["Fault_Detection_Status"], "BUG_DETECTED")

    def test_timeout_is_recorded(self):
        self.case["timeout"] = True
        self.assertEqual(self.evaluate()["status"], "TIMEOUT")

    def test_no_suite_is_recorded_without_checkout(self):
        self.current_suites = []
        result = self.evaluate()
        self.assertEqual(result["status"], "NO_SUITE")
        self.assertEqual(self.checkout_calls, [])

    def test_resume_rechecks_no_suite_if_a_suite_is_added(self):
        self.current_suites = []
        result = self.evaluate()
        self.assertTrue(runner.progress_result_is_current(result, "Demo", 1, "gemini"))
        self.current_suites = self.suites
        self.assertFalse(runner.progress_result_is_current(result, "Demo", 1, "gemini"))

    def test_suite_with_wrong_target_package_is_rejected(self):
        wrong_package = self.suite_dir / "TargetGeminiTest.java"
        wrong_package.write_text("package org.other;\npublic class TargetGeminiTest {}\n", encoding="utf-8")
        self.assertFalse(runner.test_file_matches_targets(str(wrong_package), ["org.example.Target"]))

    def test_multiclass_suite_is_one_bug_result_and_keeps_all_targets(self):
        result = self.evaluate()
        row = self.latest_csv_row()
        self.assertEqual(row["Target_Classes"], ";".join(self.classes))
        self.assertEqual(row["Test_Files"], "TargetGeminiTest.java;HelperGeminiTest.java")
        self.assertEqual(result["status"], "DONE")

    def test_csv_result_upsert_does_not_duplicate_key(self):
        row = ["Demo", 1, "Gemini 3.8 Flash", ";".join(self.classes), "suite.java", 75, 50,
               "NOT_DETECTED", 0, "DONE", "2026-09-25 12:00:00"]
        runner.append_csv_result(self.csv_path, row, "hash-1", "run-1", 2.0)
        row[7] = "BUG_DETECTED"
        runner.append_csv_result(self.csv_path, row, "hash-1", "run-2", 3.0)
        with open(self.csv_path, newline="", encoding="utf-8") as stream:
            rows = list(csv.DictReader(stream))
        self.assertEqual(len(rows), 1)
        self.assertEqual(rows[0]["Run_ID"], "run-2")
        self.assertEqual(rows[0]["Fault_Detection_Status"], "BUG_DETECTED")


if __name__ == "__main__":
    unittest.main()
