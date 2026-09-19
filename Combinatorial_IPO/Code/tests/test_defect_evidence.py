from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from analyzer.defect_evidence import select_callable_evidence
from analyzer.java_parser import parse_java_file


class DefectEvidenceTests(unittest.TestCase):
    def _parse(self, root: Path, name: str, source: str):
        path = root / name
        path.write_text(source, encoding="utf-8")
        return parse_java_file(str(path))

    def test_diff_selects_only_enclosing_callable(self) -> None:
        buggy = """package example;
public class Sample {
  public static int changed(int a, int b) { return a; }
  public static int untouched(int a, int b) { return b; }
}
"""
        fixed = buggy.replace("return a", "return Math.min(a, b)")
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            evidence = select_callable_evidence(
                self._parse(root, "Fixed.java", fixed),
                fixed,
                self._parse(root, "Buggy.java", buggy),
                buggy,
            )
        self.assertIn("changed(int,int)", evidence)
        self.assertNotIn("untouched(int,int)", evidence)

    def test_ambiguous_trigger_overloads_are_marked(self) -> None:
        source = """package example;
public class Sample {
  public static int parse(int a, int b) { return a; }
  public static int parse(long a, long b) { return 0; }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            metadata = self._parse(root, "Sample.java", source)
            evidence = select_callable_evidence(
                metadata, source, trigger_test_sources=("Sample.parse(1, 2);",)
            )
        self.assertEqual(
            {"AMBIGUOUS"},
            {
                item["confidence"]
                for records in evidence.values()
                for item in records
                if item["kind"] == "TRIGGER_TEST_CALL"
            },
        )


if __name__ == "__main__":
    unittest.main()
