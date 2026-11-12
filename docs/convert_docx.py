
from pathlib import Path
from docx import Document

# Thu muc chua file Python
BASE_DIR = Path(__file__).resolve().parent

# File Word dau vao
INPUT_FILE = BASE_DIR / "Chuc_Nang_He_Thong_NLU_ECOFARM.docx"

# File Markdown dau ra
OUTPUT_DIR = BASE_DIR / "chuc-nang"
OUTPUT_FILE = OUTPUT_DIR / "Chuc_Nang_He_Thong_NLU_ECOFARM.md"


def convert_docx_to_markdown():
    if not INPUT_FILE.exists():
        raise FileNotFoundError(
            f"Khong tim thay file DOCX: {INPUT_FILE}"
        )

    document = Document(str(INPUT_FILE))
    markdown_parts = []

    # Doc cac doan van
    for paragraph in document.paragraphs:
        text = paragraph.text.strip()

        if not text:
            continue

        style = paragraph.style.name if paragraph.style else ""

        if style.startswith("Heading"):
            try:
                level = int(style.split()[-1])
            except ValueError:
                level = 2

            level = max(1, min(level, 6))
            markdown_parts.append("#" * level + " " + text)

        elif "List" in style:
            markdown_parts.append("- " + text)

        else:
            markdown_parts.append(text)

    # Doc cac bang
    for table in document.tables:
        if not table.rows:
            continue

        rows = []

        for row in table.rows:
            cells = [
                cell.text.strip().replace("|", r"\|").replace("\n", "<br>")
                for cell in row.cells
            ]

            rows.append("| " + " | ".join(cells) + " |")

        column_count = len(table.rows[0].cells)

        separator = "| " + " | ".join(
            ["---"] * column_count
        ) + " |"

        if rows:
            markdown_parts.append(rows[0])
            markdown_parts.append(separator)
            markdown_parts.extend(rows[1:])

    # Tao thu muc dau ra
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # Ghi file voi encoding UTF-8
    with open(OUTPUT_FILE, "w", encoding="utf-8", newline="\n") as file:
        file.write("\n\n".join(markdown_parts))

    print("CHUYEN DOI THANH CONG!")
    print(f"File dau vao: {INPUT_FILE}")
    print(f"File dau ra: {OUTPUT_FILE}")
    print(f"So doan van: {len(document.paragraphs)}")
    print(f"So bang: {len(document.tables)}")


if __name__ == "__main__":
    try:
        convert_docx_to_markdown()
    except Exception as error:
        print(f"LOI: {error}")
