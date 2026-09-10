"""Tests for lightweight Java source parsing used by the IPO pipeline."""

from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from analyzer.java_parser import parse_java_file


class JavaParserTests(unittest.TestCase):
    def test_package_private_top_level_class_is_supported(self) -> None:
        source = """package example;

final class PackagePrivateSample {
    public static int min(int left, int right) {
        return Math.min(left, right);
    }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            source_file = Path(temporary_directory) / "PackagePrivateSample.java"
            source_file.write_text(source, encoding="utf-8")

            metadata = parse_java_file(str(source_file))

            self.assertEqual("PackagePrivateSample", metadata["class"])
            self.assertEqual("min", metadata["methods"][0]["name"])
