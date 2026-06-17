#!/usr/bin/env python3
"""Run the Java benchmark and write results to presentation/material/benchmark.csv."""

import subprocess, csv, sys
from pathlib import Path

CSV_PATH = Path("presentation/material/benchmark.csv")


def run_benchmark():
    proc = subprocess.run(
        ["mvn", "-q", "compile", "exec:java",
         "-Dexec.mainClass=com.group1.interwasm.Benchmark",
         "-Dexec.args=all"],
        text=True, capture_output=True
    )
    # Print the table so the user can see the live results
    print(proc.stdout, end="")
    if proc.returncode != 0:
        print(proc.stderr, file=sys.stderr)
        sys.exit(proc.returncode)
    return proc.stdout


def parse_table(output):
    rows = []
    for line in output.splitlines():
        if '│' not in line:
            continue
        parts = [p.strip() for p in line.split('│')]
        if len(parts) < 8:
            continue
        func = parts[0]
        if not func:
            continue
        try:
            v1  = float(parts[1])
            v2  = float(parts[2])
            v3  = float(parts[3])
            v8  = float(parts[4])
            r12 = float(parts[5].replace('×', ''))
            r23 = float(parts[6].replace('×', ''))
            r38 = float(parts[7].replace('×', ''))
            rows.append([func, v1, v2, v3, v8, r12, r23, r38])
        except ValueError:
            continue  # header or separator line
    return rows


output = run_benchmark()
rows = parse_table(output)

if not rows:
    print("ERROR: could not parse any data rows from benchmark output.", file=sys.stderr)
    sys.exit(1)

with open(CSV_PATH, 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['function', 'v1_us', 'v2_us', 'v3_us', 'v8_us',
                     'v1_to_v2', 'v2_to_v3', 'v3_to_v8'])
    writer.writerows(rows)

print(f"\nWrote {len(rows)} rows → {CSV_PATH}")
