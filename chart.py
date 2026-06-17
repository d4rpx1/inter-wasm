import pandas as pd
import matplotlib.pyplot as plt
from pathlib import Path

material_dir = Path("presentation/material")
df = pd.read_csv(material_dir / "benchmark.csv")

x = range(len(df))
width = 0.2

plt.figure(figsize=(10, 5))
plt.bar([i - 1.5 * width for i in x], df["v1_us"], width, label="v1")
plt.bar([i - 0.5 * width for i in x], df["v2_us"], width, label="v2")
plt.bar([i + 0.5 * width for i in x], df["v3_us"], width, label="v3")
plt.bar([i + 1.5 * width for i in x], df["v8_us"], width, label="V8 JIT")

plt.xticks(x, df["function"], rotation=20, ha="right")
plt.ylabel("µs / call (log scale)")
plt.yscale("log")
plt.title("WebAssembly Interpreter Benchmark\nIntel Core i5-1235U (12th Gen)", fontsize=11)
plt.legend()

for offsets, col in [
    ([-1.5 * width + i for i in x], df["v1_us"]),
    ([-0.5 * width + i for i in x], df["v2_us"]),
    ([+0.5 * width + i for i in x], df["v3_us"]),
    ([+1.5 * width + i for i in x], df["v8_us"]),
]:
    for xi, val in zip(offsets, col):
        label = f"{val:.3f}" if val < 1 else f"{val:.1f}"
        plt.text(xi, val * 1.15, label, ha="center", va="bottom", fontsize=6, rotation=90)

plt.tight_layout()
plt.savefig(material_dir / "benchmark_chart.png", dpi=200)
plt.close()
