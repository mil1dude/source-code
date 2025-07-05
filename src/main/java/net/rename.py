import os
import re

def find_public_class_name(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            # Suche nach: public class ClassName
            match = re.search(r'public\s+class\s+([A-Za-z0-9_]+)', line)
            if match:
                return match.group(1)
    return None

def rename_files(root_dir):
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith('.java'):
                filepath = os.path.join(dirpath, filename)
                class_name = find_public_class_name(filepath)
                if class_name and filename != f"{class_name}.java":
                    new_path = os.path.join(dirpath, f"{class_name}.java")
                    if not os.path.exists(new_path):
                        print(f"Renaming: {filepath} -> {new_path}")
                        os.rename(filepath, new_path)
                    else:
                        print(f"Skipped (target exists): {new_path}")

if __name__ == "__main__":
    # Passe den Pfad ggf. an!
    rename_files(".")