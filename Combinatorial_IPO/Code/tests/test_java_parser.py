"""Tests for lightweight Java source parsing used by the IPO pipeline."""

from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from analyzer.java_parser import method_signature, parse_java_file


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

    def test_parses_constructor_and_instance_visibility(self) -> None:
        source = """package example;
public class Sample {
    Sample(int left, int right) {}
    protected int add(int left, int right) { return left + right; }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            source_file = Path(temporary_directory) / "Sample.java"
            source_file.write_text(source, encoding="utf-8")
            metadata = parse_java_file(str(source_file))

            self.assertEqual("class", metadata["type_kind"])
            self.assertEqual("Sample(int,int)", method_signature(metadata["constructors"][0]))
            self.assertEqual("protected", metadata["methods"][0]["visibility"])
            self.assertFalse(metadata["methods"][0]["static"])
            self.assertEqual(3, metadata["constructors"][0]["start_line"])
            self.assertEqual(4, metadata["methods"][0]["start_line"])
            self.assertGreaterEqual(metadata["methods"][0]["end_line"], 4)

    def test_ignores_method_like_statements_inside_method_bodies(self) -> None:
        source = """package example;
public class Sample {
    public boolean valid(Object value) {
        if (value == null) { return false; }
        throw new IllegalArgumentException("bad");
    }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            source_file = Path(temporary_directory) / "Sample.java"
            source_file.write_text(source, encoding="utf-8")
            metadata = parse_java_file(str(source_file))

            self.assertEqual(["valid"], [method["name"] for method in metadata["methods"]])

    def test_records_constructor_declaration_even_when_signature_is_unsupported(self) -> None:
        source = """package example;
public class Sample {
    Sample(
        @Named(value = "seed") int seed
    ) {}
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            source_file = Path(temporary_directory) / "Sample.java"
            source_file.write_text(source, encoding="utf-8")
            metadata = parse_java_file(str(source_file))

            self.assertEqual([], metadata["constructors"])
            self.assertEqual(1, metadata["declared_constructor_count"])

    def test_marks_abstract_top_level_class(self) -> None:
        source = """package example;
public abstract class Sample {
    protected Sample(String value) {}
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            source_file = Path(temporary_directory) / "Sample.java"
            source_file.write_text(source, encoding="utf-8")
            metadata = parse_java_file(str(source_file))

            self.assertTrue(metadata["abstract"])
