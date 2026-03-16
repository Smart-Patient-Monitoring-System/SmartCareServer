import os
import glob
import re

files = glob.glob(r'c:\Users\supul\healthcare-system\mainservice\src\main\java\com\example\mainservice\controller\**\*.java', recursive=True)

for file in files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove @CrossOrigin(...) or @CrossOrigin
    new_content = re.sub(r'@CrossOrigin\s*(?:\([^)]*\))?', '', content)
    
    # Also remove some commented ones that might look ugly
    new_content = re.sub(r'//\s*@CrossOrigin\s*(?:\([^)]*\))?', '', new_content)

    if new_content != content:
        with open(file, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {os.path.basename(file)}")
