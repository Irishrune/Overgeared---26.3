#!/usr/bin/env python3
"""Source-only overview of known 26.3 custom recipe blockers.

These are textual counts, not a compiler-error report and not a correctness proof.
"""
from pathlib import Path
import json
import re
import re
root=Path(__file__).resolve().parents[1]
recipes=root/'common/src/main/java/net/stirdrem/overgeared/recipe'
files=list(recipes.glob('*.java'))
patterns={
    'legacy_empty_ingredient':r'(?<![A-Za-z])Ingredient\.EMPTY\b',
    'legacy_item_stack_codec':r'\bItemStack\.CODEC\b',
    'legacy_item_stack_stream_codec':r'\bItemStack\.STREAM_CODEC\b',
    'optional_item_stack_stream_codec':r'\bItemStack\.OPTIONAL_STREAM_CODEC\b',
    'recipe_holder_usage_not_a_failure':r'\bRecipeHolder\s*<',
    'legacy_recipe_manager_get_all':r'\bgetAllRecipesFor\s*\(',
}
report={}
for key,p in patterns.items():
    found=[]
    for path in files:
        for number,line in enumerate(path.read_text().splitlines(),1):
            if re.search(p,line):
                found.append({'path':path.relative_to(root).as_posix(),'line':number})
    report[key]={'occurrences':len(found),'locations':found}
if __name__=='__main__':
    print(json.dumps(report,indent=2))
