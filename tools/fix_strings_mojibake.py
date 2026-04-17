"""Fix UTF-8 mojibake in strings.xml (Latin-1 misread of UTF-8 bytes)."""
import re
from pathlib import Path

# UTF-8 for punctuation mis-decoded as Windows-1252/Latin-1 (contains € U+20AC → blocks latin-1 encode)
UTF8_GARBAGE_TRIPLETS = (
    ("â€¦", "\u2026"),  # … HORIZONTAL ELLIPSIS
    ("â€”", "\u2014"),  # — EM DASH
    ("â€“", "\u2013"),  # – EN DASH
    ("â€™", "\u2019"),  # ’ RIGHT SINGLE QUOTATION MARK
    ("â€œ", "\u201c"),  # “
    ("â€", "\u201d"),  # ”
)


def preprocess_garbage(s: str) -> str:
    for bad, good in UTF8_GARBAGE_TRIPLETS:
        s = s.replace(bad, good)
    return s


# ASCII-only placeholders so .encode("latin-1") succeeds on the rest of mojibake bytes.
_MASK = (
    ("\u2026", "<<<ELL>>>"),
    ("\u2014", "<<<EM>>>"),
    ("\u2013", "<<<EN>>>"),
    ("\u2019", "<<<RSQ>>>"),
    ("\u201c", "<<<LDQ>>>"),
    ("\u201d", "<<<RDQ>>>"),
)


def _mask(s: str) -> str:
    for uni, tok in _MASK:
        s = s.replace(uni, tok)
    return s


def _unmask(s: str) -> str:
    for uni, tok in _MASK:
        s = s.replace(tok, uni)
    return s


def fix_inner(inner: str) -> str:
    inner = preprocess_garbage(inner)
    inner = _mask(inner)
    try:
        fixed = inner.encode("latin-1").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        fixed = inner
    return _unmask(fixed)


def process_file(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    # Corrupted merge: literal `r`n between tags
    text = text.replace(
        "</string>`r`n    <string name=\"common_loading\">",
        "</string>\n    <string name=\"common_loading\">",
    )
    lines = text.splitlines(keepends=True)
    out = []
    pattern = re.compile(r"^(\s*<string name=\"[^\"]+\">)(.*)(</string>)\s*$")

    for line in lines:
        m = pattern.match(line.rstrip("\r\n"))
        if not m:
            out.append(line)
            continue
        prefix, inner, closing = m.group(1), m.group(2), m.group(3)
        inner_fixed = fix_inner(inner)
        nl = "\n" if line.endswith("\n") else ""
        if line.endswith("\r\n"):
            nl = "\r\n"
        elif line.endswith("\n"):
            nl = "\n"
        out.append(prefix + inner_fixed + closing + nl)

    path.write_text("".join(out), encoding="utf-8")


def main() -> None:
    base = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
    for rel in ("values-fr/strings.xml", "values/strings.xml"):
        process_file(base / rel)
    print("OK:", base)


if __name__ == "__main__":
    main()
