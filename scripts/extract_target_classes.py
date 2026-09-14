#!/usr/bin/env python3
"""
Defects4J Target Class & Metadata Extractor
Designed for ProjectSQA Team (Member 4 - Infra Lead)

Purpose:
  Extracts the exact target Java class(es) under test (`classes.modified`)
  and ground-truth defect info for any of the 854 bugs in Defects4J,
  saving them to `target_benchmark/<Project>_<BugID>b/` so Member 1 (IPO),
  Member 2 (MIO), and Member 3 (Claude/Gemini) can generate tests immediately.

Usage:
  # 1. ดูข้อมูลคลาสเป้าหมายของบั๊กใดๆ โดยไม่ต้อง checkout:
  python scripts/extract_target_classes.py --info --project Lang --bug 2

  # 2. สกัดซอร์สโค้ด .java ของบั๊กเฉพาะตัว (เช่น Lang-2):
  python scripts/extract_target_classes.py --project Lang --bug 2

  # 3. สกัดซอร์สโค้ด .java ของทุกบั๊กในโปรเจกต์ (เช่น Csv):
  python scripts/extract_target_classes.py --project Csv

  # 4. สกัดซอร์สโค้ดของทั้ง 17 โปรเจกต์ (All 854 Active Bugs):
  python scripts/extract_target_classes.py --all
"""

import os
import sys
import json
import shutil
import argparse
import subprocess
from pathlib import Path

# ป้องกัน UnicodeEncodeError บน Windows terminal
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent
CATALOG_PATH = PROJECT_ROOT / "target_benchmark" / "all_bugs_catalog.json"
TARGET_BENCHMARK_DIR = PROJECT_ROOT / "target_benchmark"
CONTAINER_NAME = "defects4j_sqa"

def is_inside_container() -> bool:
    """ตรวจสอบว่ารันอยู่ภายใน Docker Container หรือไม่"""
    return shutil.which("defects4j") is not None or os.path.exists("/.dockerenv")

def is_docker_running() -> bool:
    """ตรวจสอบว่า Docker Daemon บน Host กำลังทำงานอยู่หรือไม่"""
    try:
        res = subprocess.run(["docker", "ps"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=5)
        return res.returncode == 0
    except Exception:
        return False

def load_catalog():
    """โหลดข้อมูลบั๊กทั้ง 854 ตัวจาก all_bugs_catalog.json แล้วทำเป็น dictionary"""
    if not CATALOG_PATH.exists():
        print(f"❌ ไม่พบไฟล์ Catalog ที่: {CATALOG_PATH}")
        print("💡 ให้รัน: python scripts/batch_extract_all_bugs.py เพื่อสร้าง Catalog ก่อน")
        sys.exit(1)
    with open(CATALOG_PATH, "r", encoding="utf-8") as f:
        raw = json.load(f)
    
    # ถ้าเป็น list ให้แปลงเป็น dict keyed by "Project-BugID"
    if isinstance(raw, list):
        catalog = {}
        for item in raw:
            key = f"{item['project']}-{item['bug_id']}"
            # map target_classes to modified_classes for compatibility
            if "target_classes" in item and "modified_classes" not in item:
                item["modified_classes"] = item["target_classes"]
            catalog[key] = item
        return catalog
    return raw

def show_bug_info(project: str, bug_id: int):
    """แสดงข้อมูล Target Classes และ Triggering Tests ของบั๊กที่ระบุ"""
    catalog = load_catalog()
    key = f"{project}-{bug_id}"
    bug_data = catalog.get(key)
    if not bug_data:
        print(f"❌ ไม่พบบั๊ก {key} ในแค็ตตาล็อก 854 บั๊ก")
        return
    
    print("=" * 65)
    print(f"📌 ข้อมูลบั๊ก: {key} ({project} Bug #{bug_id})")
    print("=" * 65)
    print(f"  • Root Package: {bug_data.get('root_package', 'N/A')}")
    print(f"  • Modified Classes Under Test ({len(bug_data.get('modified_classes', []))} คลาส):")
    for cls in bug_data.get("modified_classes", []):
        simple = cls.split(".")[-1]
        print(f"      - {cls}  -->  (ไฟล์: {simple}.java)")
    print(f"  • Trigger Tests (บั๊กนี้ทำให้เทสต่อไปนี้พัง):")
    for t in bug_data.get("trigger_tests", []):
        print(f"      - {t}")
    dest_dir = TARGET_BENCHMARK_DIR / f"{project}_{bug_id}b"
    print(f"  • ตำแหน่งจัดเก็บในโปรเจกต์: {dest_dir}")
    if dest_dir.exists():
        files = [f.name for f in dest_dir.glob("*.java")]
        print(f"    Status: ✅ สกัดซอร์สโค้ดแล้ว ({', '.join(files) if files else 'ยังไม่มี .java'})")
    else:
        print(f"    Status: ⏳ ยังไม่ได้สกัดซอร์สโค้ด (สั่งรันสกัดได้ด้วย --project {project} --bug {bug_id})")
    print("=" * 65)

def extract_single_bug(project: str, bug_id: int, force: bool = False) -> bool:
    """สกัด .java source files และ metadata ของบั๊กเป้าหมาย"""
    dest_dir = TARGET_BENCHMARK_DIR / f"{project}_{bug_id}b"
    
    if dest_dir.exists() and not force:
        java_files = list(dest_dir.glob("*.java"))
        if java_files:
            print(f"   ⏩ {project}-{bug_id}b สกัดไว้แล้วที่ {dest_dir.name} ({len(java_files)} java files). ข้าม...")
            return True

    dest_dir.mkdir(parents=True, exist_ok=True)
    catalog = load_catalog()
    bug_info = catalog.get(f"{project}-{bug_id}", {})
    modified_classes = bug_info.get("modified_classes", [])

    work_dir = f"/tmp/{project}_{bug_id}_extract"

    if is_inside_container():
        # กำลังรันอยู่ในคอนเทนเนอร์ Defects4J
        cmd = f"""
        rm -rf "{work_dir}" && defects4j checkout -p "{project}" -v "{bug_id}b" -w "{work_dir}" 2>&1
        defects4j info -p "{project}" -b "{bug_id}" > "{dest_dir}/defects4j_info.txt" 2>/dev/null || true
        SRC_DIR=$(cd "{work_dir}" && defects4j export -p dir.src.classes 2>/dev/null || echo "")
        """
        res = subprocess.run(["bash", "-c", cmd], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if res.returncode != 0 and not (dest_dir / "defects4j_info.txt").exists():
            print(f"   ❌ ล้มเหลวในการ Checkout {project}-{bug_id}b ใน container: {res.stderr}")
            return False

        # คัดลอก .java files
        copied = 0
        for cls in modified_classes:
            cls_rel = cls.replace(".", "/") + ".java"
            src_file = Path(work_dir) / cls_rel
            if not src_file.exists():
                simple_name = cls.split(".")[-1] + ".java"
                matches = list(Path(work_dir).rglob(simple_name))
                matches = [m for m in matches if "/test/" not in m.as_posix().lower()]
                if matches:
                    src_file = matches[0]

            if src_file.exists():
                shutil.copy2(src_file, dest_dir / src_file.name)
                copied += 1
                print(f"   -> คัดลอก {src_file.name} เข้าสู่ {dest_dir.name}/")

        # บันทึก metadata.json
        with open(dest_dir / "metadata.json", "w", encoding="utf-8") as f:
            json.dump(bug_info, f, indent=2, ensure_ascii=False)

        shutil.rmtree(work_dir, ignore_errors=True)
        return copied > 0

    else:
        # กำลังรันบน Host (Windows / Mac / Linux)
        if not is_docker_running():
            print(f"   ❌ Docker Daemon ไม่ได้ทำงานอยู่!")
            print(f"   💡 กรุณาเปิด Docker Desktop แล้วรัน: docker start {CONTAINER_NAME}")
            return False

        # เรียกผ่าน docker exec
        container_dest = f"/workspace/target_benchmark/{project}_{bug_id}b"
        docker_cmd = f"""
        rm -rf "{work_dir}" && defects4j checkout -p "{project}" -v "{bug_id}b" -w "{work_dir}"
        defects4j info -p "{project}" -b "{bug_id}" > "{container_dest}/defects4j_info.txt" 2>/dev/null || true
        SRC_DIR=$(cd "{work_dir}" && defects4j export -p dir.src.classes 2>/dev/null || echo "")
        """
        for cls in modified_classes:
            simple_name = cls.split(".")[-1] + ".java"
            cls_path = cls.replace(".", "/") + ".java"
            docker_cmd += f"""
            if [ -f "{work_dir}/$SRC_DIR/{cls_path}" ]; then
                cp "{work_dir}/$SRC_DIR/{cls_path}" "{container_dest}/{simple_name}"
            else
                FOUND=$(find "{work_dir}" -name "{simple_name}" | grep -v "/test/" | head -n 1)
                [ -n "$FOUND" ] && cp "$FOUND" "{container_dest}/{simple_name}"
            fi
            """
        docker_cmd += f'rm -rf "{work_dir}"'

        exec_cmd = ["docker", "exec", CONTAINER_NAME, "bash", "-c", docker_cmd]
        res = subprocess.run(exec_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=60)
        
        # บันทึก metadata.json บน Host
        with open(dest_dir / "metadata.json", "w", encoding="utf-8") as f:
            json.dump(bug_info, f, indent=2, ensure_ascii=False)

        java_files = list(dest_dir.glob("*.java"))
        if java_files:
            print(f"   ✅ สกัด {project}-{bug_id}b สำเร็จ: {[f.name for f in java_files]}")
            return True
        else:
            print(f"   ⚠️ ไม่พบคลาส .java สำหรับ {project}-{bug_id}b (ตรวจสอบ Defects4J)")
            return False

def list_project_bugs(project: str = None):
    """แสดงรายการ Active Bugs และสถานะว่าสกัดไฟล์แล้วหรือยัง"""
    catalog = load_catalog()
    if project:
        bugs = [v for k, v in catalog.items() if v["project"].lower() == project.lower()]
        if not bugs:
            print(f"❌ ไม่พบโปรเจกต์ {project} ในระบบ")
            return
        bugs = sorted(bugs, key=lambda x: x["bug_id"])
        print("=" * 70)
        print(f"📋 รายการ Active Bugs ของโปรเจกต์ {bugs[0]['project']} (ทั้งหมด {len(bugs)} บั๊ก):")
        print("=" * 70)
        for b in bugs:
            dest_dir = TARGET_BENCHMARK_DIR / f"{b['project']}_{b['bug_id']}b"
            has_java = dest_dir.exists() and len(list(dest_dir.glob("*.java"))) > 0
            icon = "✅ สกัดแล้ว" if has_java else "⏳ ยังไม่ได้สกัด"
            classes = b.get("simple_names") or [c.split(".")[-1] for c in b.get("modified_classes", [])]
            classes_str = ", ".join(classes)
            print(f"  Bug #{b['bug_id']:<3} | {icon:<15} | Classes: {classes_str}")
        print("=" * 70)
    else:
        # สรุปทุกโปรเจกต์
        print("=" * 70)
        print(f"📋 ภาพรวมทั้ง 17 โปรเจกต์ใน Defects4J (ทั้งหมด {len(catalog)} Active Bugs):")
        print("=" * 70)
        projects = {}
        for b in catalog.values():
            p = b["project"]
            projects.setdefault(p, []).append(b)
        for p, b_list in sorted(projects.items()):
            extracted_count = sum(1 for b in b_list if (TARGET_BENCHMARK_DIR / f"{p}_{b['bug_id']}b").exists() and len(list((TARGET_BENCHMARK_DIR / f"{p}_{b['bug_id']}b").glob("*.java"))) > 0)
            print(f"  • {p:<16} : {len(b_list):>3} bugs | สกัดแล้ว {extracted_count:>3}/{len(b_list)} bugs")
        print("=" * 70)

def main():
    parser = argparse.ArgumentParser(description="Defects4J Target Class & Metadata Extractor for ProjectSQA")
    parser.add_argument("--project", type=str, help="ชื่อโปรเจกต์ เช่น Lang, Math, Chart, Csv")
    parser.add_argument("--bug", type=int, help="รหัสบั๊ก เช่น 1, 2, 25")
    parser.add_argument("--all", action="store_true", help="สกัดทั้ง 854 บั๊กใน Defects4J")
    parser.add_argument("--info", action="store_true", help="แสดงข้อมูลคลาสเป้าหมายโดยไม่ต้อง checkout")
    parser.add_argument("--list", action="store_true", help="ดูรายการบั๊กและสถานะการสกัด")
    parser.add_argument("--force", action="store_true", help="บังคับสกัดใหม่แม้เคยมีไฟล์อยู่แล้ว")
    args = parser.parse_args()

    catalog = load_catalog()

    if args.list:
        list_project_bugs(args.project)
        return

    if args.info:
        if not args.project or not args.bug:
            print("❌ ต้องระบุทั้ง --project และ --bug เช่น: python scripts/extract_target_classes.py --info --project Lang --bug 2")
            sys.exit(1)
        show_bug_info(args.project, args.bug)
        return

    if args.project and args.bug:
        print(f"🚀 กำลังสกัด Target Class สำหรับ {args.project}-{args.bug}b...")
        extract_single_bug(args.project, args.bug, force=args.force)
        return

    if args.project and not args.bug:
        bugs = [v for k, v in catalog.items() if v["project"].lower() == args.project.lower()]
        if not bugs:
            print(f"❌ ไม่พบโปรเจกต์ {args.project} ในระบบ")
            sys.exit(1)
        print(f"🚀 กำลังสกัด Target Classes ทั้งหมดของโปรเจกต์ {args.project} (จำนวน {len(bugs)} บั๊ก)...")
        success = 0
        for b in sorted(bugs, key=lambda x: x["bug_id"]):
            if extract_single_bug(b["project"], b["bug_id"], force=args.force):
                success += 1
        print(f"\n🎉 สกัดโปรเจกต์ {args.project} เสร็จสิ้น: สำเร็จ {success}/{len(bugs)} บั๊ก")
        return

    if args.all:
        print(f"🚀 กำลังสกัด Target Classes ทั้ง 854 บั๊กใน Defects4J...")
        total = len(catalog)
        success = 0
        for i, (k, b) in enumerate(catalog.items(), 1):
            print(f"[{i}/{total}] {k}...")
            if extract_single_bug(b["project"], b["bug_id"], force=args.force):
                success += 1
        print(f"\n🎉 สกัดเสร็จสิ้นทั้งหมด: สำเร็จ {success}/{total} บั๊ก!")
        return

    parser.print_help()
    print("\n💡 คำสั่งตัวอย่างที่ใช้บ่อย:")
    print("  1. ดูข้อมูลคลาสที่ต้องแก้: python scripts/extract_target_classes.py --info --project Lang --bug 2")
    print("  2. สกัดคลาสเป้าหมาย Lang-2: python scripts/extract_target_classes.py --project Lang --bug 2")
    print("  3. สกัดคลาสทั้งโปรเจกต์ Csv: python scripts/extract_target_classes.py --project Csv")

if __name__ == "__main__":
    main()
