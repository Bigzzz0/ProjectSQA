import json
from pathlib import Path

inv_path = Path("/workspace/Combinatorial_IPO/Results/inventory.json")
ver_path = Path("/workspace/Combinatorial_IPO/Results/verified_suites_manifest.json")

with open(inv_path) as f:
    inv = json.load(f)
with open(ver_path) as f:
    ver = json.load(f)

verified_keys = {(r["project"], r["bug_id"], r["target_class"]) for r in ver["records"]}
ready = [r for r in inv["records"] if r.get("status") == "AUTO_READY"]

unverified = [r for r in ready if (r["project"], r["bug_id"], r["target_class"]) not in verified_keys]
print(f"Total ready: {len(ready)}, Verified: {len(verified_keys)}, Unverified: {len(unverified)}")

cache_dir = Path("/workspace/Combinatorial_IPO/Results/cache/all-854-modified-classes")
categories = {}
details = []

for r in unverified:
    proj = r["project"]
    bid = r["bug_id"]
    cls = r["target_class"]
    ident = f"{proj}_{bid}b {cls}"
    rec_path = cache_dir / f"{proj}_{bid}b" / Path(*cls.split(".")) / "class_record.json"
    if rec_path.is_file():
        cr = json.loads(rec_path.read_text())
        err = cr.get("error", "")
        if "ComparisonFailure" in err or "AssertionError" in err or "expected:<[" in err or "actual.getClass()" in err:
            cat = "ASSERTION_MISMATCH_OR_HASH"
        elif "cannot find symbol" in err or "cannot be applied to given types" in err or "incompatible types" in err:
            cat = "COMPILATION_OR_SIGNATURE_DIFF"
        elif "timed out" in err.lower() or "timeout" in err.lower():
            cat = "TIMEOUT"
        elif "ClassNotFoundException" in err or "NoClassDefFoundError" in err:
            cat = "CLASSPATH_OR_CLASSNOTFOUND"
        elif "All" in err and "failed oracle collection" in err:
            cat = "ORACLE_COLLECTION_ALL_FAILED"
        else:
            cat = "OTHER: " + err.split("\n")[0][:60]
        categories[cat] = categories.get(cat, 0) + 1
        details.append((ident, cat, err.split("\n")[0][:100]))
    else:
        categories["NO_CACHE_RECORD"] = categories.get("NO_CACHE_RECORD", 0) + 1
        details.append((ident, "NO_CACHE_RECORD", "No class_record.json found"))

print("\nCategories breakdown:")
for cat, count in sorted(categories.items(), key=lambda x: -x[1]):
    print(f"  [{count:2d}] {cat}")

print("\nDetails:")
for ident, cat, err in sorted(details):
    print(f"{ident:<60} | {cat:<30} | {err}")
