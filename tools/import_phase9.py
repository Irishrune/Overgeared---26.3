#!/usr/bin/env python3
"""Rebuild the exact in-progress Overgeared Fabric 26.3 Phase 9 source.

This intentionally imports from a pinned upstream commit plus the archived
Phase 9 diff. It never treats the unmodified upstream branch as the port.
"""

import argparse
import base64
import bz2
import hashlib
import io
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
from urllib.request import Request, urlopen
from zipfile import ZipFile

UPSTREAM_COMMIT = "c898a0ce89b55a311ddf1b89079808a73f3604a1"
UPSTREAM_URL = f"https://codeload.github.com/phuccom000/Overgeared/zip/{UPSTREAM_COMMIT}"
PATCH_SHA256 = "37f550084b077e2d091a81666a7bd3c6053996848557265f3833cff72aa24001"


def source_from_zip(data: bytes, destination: Path) -> Path:
    with ZipFile(io.BytesIO(data)) as archive:
        members = archive.namelist()
        roots = {name.split("/", 1)[0] for name in members if name}
        if len(roots) != 1:
            raise RuntimeError(f"Expected a single root in upstream ZIP, found: {roots}")
        archive.extractall(destination)
    return destination / next(iter(roots))


def import_source(repo: Path, source_zip: Path | None) -> int:
    patch_file = repo / "port-source/phase9.patch.bz2.base64"
    if not patch_file.is_file():
        raise RuntimeError(f"Missing patch payload: {patch_file}")
    payload = patch_file.read_text(encoding="ascii").strip()
    patch = bz2.decompress(base64.b64decode(payload, validate=True))
    digest = hashlib.sha256(patch).hexdigest()
    if digest != PATCH_SHA256:
        raise RuntimeError(f"Phase 9 patch SHA256 mismatch: {digest}")

    if (repo / "build.gradle").exists():
        raise RuntimeError("Source build.gradle already exists; refusing a destructive re-import")

    if source_zip:
        source_bytes = source_zip.read_bytes()
    else:
        req = Request(UPSTREAM_URL, headers={"User-Agent": "Overgeared-26.3-port-import/1.0"})
        with urlopen(req, timeout=90) as resp:
            source_bytes = resp.read()

    with tempfile.TemporaryDirectory(prefix="overgeared-phase9-") as tmp:
        source_root = source_from_zip(source_bytes, Path(tmp))
        upstream_properties = (source_root / "gradle.properties").read_text(encoding="utf-8")
        if "minecraft_version=1.21.1" not in upstream_properties:
            raise RuntimeError("Unexpected upstream source; Minecraft version anchor does not match")
        for flag in ("--check", ""):
            cmd = ["git", "apply", "--binary", "--whitespace=nowarn"]
            if flag:
                cmd.append(flag)
            result = subprocess.run(cmd, cwd=source_root, input=patch,
                                    stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            if result.returncode:
                raise RuntimeError(f"git apply {flag} failed:\n{result.stderr.decode(errors='replace')}")

        count = 0
        for src in source_root.rglob("*"):
            if not src.is_file():
                continue
            relative = src.relative_to(source_root)
            if ".git" in relative.parts or "__pycache__" in relative.parts or src.suffix == ".pyc":
                continue
            dest = repo / relative
            if dest.exists() and dest.read_bytes() != src.read_bytes():
                raise RuntimeError(f"Refusing to overwrite existing file with different content: {relative}")
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dest)
            count += 1

    if count < 700:
        raise RuntimeError(f"Suspiciously small import: {count} files")
    print(f"Imported {count} Phase 9 source files at pinned upstream {UPSTREAM_COMMIT}")
    return count


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--source-zip", type=Path, default=None,
                        help="Optional offline upstream ZIP (otherwise downloads pinned commit)")
    args = parser.parse_args()
    import_source(args.repo.resolve(), args.source_zip.resolve() if args.source_zip else None)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"Phase 9 import FAILED: {exc}", file=sys.stderr)
        raise SystemExit(1)
