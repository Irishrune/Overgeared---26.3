#!/usr/bin/env python3
"""Restore omitted upstream generated data from the checked-in generator definitions.

This reads the limited, declarative builder expressions in the original NeoForge
providers. It never executes Java or Python from those files. Unknown expressions
fail closed so a provider change cannot silently omit a recipe or gameplay flag.
"""
from pathlib import Path
import argparse, ast, json, re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT/'common/src/main/java/net/stirdrem/overgeared'
PROVIDERS = ROOT/'neoforge/src/main/java/net/stirdrem/overgeared/datagen'
OUT = ROOT/'common/src/main/resources/data'

def clean(s):
    return re.sub(r'/\*.*?\*/|//[^\n]*', '', s, flags=re.S)

def split(s, separator=','):
    result=[]; start=0; depth=0; quote=None; escape=False
    for i,ch in enumerate(s):
        if quote:
            if escape: escape=False
            elif ch=='\\': escape=True
            elif ch==quote: quote=None
        elif ch in '\"\'': quote=ch
        elif ch in '([{': depth+=1
        elif ch in ')]}': depth-=1
        elif ch==separator and depth==0:
            result.append(s[start:i].strip()); start=i+1
    result.append(s[start:].strip())
    return [v for v in result if v]

def calls(s):
    result=[]; s=s.strip()
    while s:
        m=re.match(r'\.?([\w.]+)\(',s)
        if not m: raise ValueError('Unknown builder expression: '+s[:150])
        start=m.end(); depth=1; quote=None
        for end in range(start,len(s)):
            ch=s[end]
            if quote:
                if ch==quote and s[end-1]!='\\': quote=None
            elif ch in '\"\'': quote=ch
            elif ch=='(': depth+=1
            elif ch==')':
                depth-=1
                if not depth: break
        result.append((m[1],split(s[start:end])))
        s=s[end+1:].strip()
    return result

ITEMS={}
for kind,file in [('ModItems','item/ModItems.java'),('ModBlocks','block/ModBlocks.java')]:
    source=(JAVA/file).read_text(encoding='utf-8')
    for name,id in re.findall(r'\b([A-Z][A-Z_]+)\s*=\s*(?:registerItem|registerSimpleItem|registerBlock)\(\s*"([a-z_0-9]+)"',source):
        ITEMS[kind+'.'+name]='overgeared:'+id
TAGS={}
source=(JAVA/'util/ModTags.java').read_text()
for kind,section in [('Items',source.split('class Items')[1].split('class Blocks')[0]),('Blocks',source.split('class Blocks')[1])]:
    for name,id in re.findall(r'\b([A-Z_]+)\s*=\s*tag\("([\w/]+)"',section): TAGS['ModTags.'+kind+'.'+name]='overgeared:'+id

def value(s):
    s=s.strip().replace('.get()','').replace('.asItem()','')
    if s in ITEMS: return ITEMS[s]
    if s in TAGS: return '#'+TAGS[s]
    if s=='Ingredient.EMPTY': return None
    if s=='pWriter': return None
    if s=='null': return None
    if s in ('true','false'): return s=='true'
    if s=='OvergearedMod.MOD_ID': return 'overgeared'
    parts=split(s,'+')
    if len(parts)>1: return ''.join(str(value(p)) for p in parts)
    if re.fullmatch(r'-?\d+(\.\d+)?[fF]?',s): return float(s.rstrip('fF')) if '.' in s else int(s)
    if s.startswith(('"',"'")): return ast.literal_eval(s)
    if s.startswith(('Items.','Blocks.')): return 'minecraft:'+s.split('.')[1].lower()
    if s.startswith(('RecipeCategory.','ForgingBookCategory.','AnvilTier.','ForgingQuality.')): return s.split('.')[1].lower()
    if s.startswith('ItemTags.') and '(' not in s: return '#minecraft:'+s.split('.')[1].lower()
    if s.startswith('BlockTags.') and '(' not in s:
        name=s.split('.')[1].lower().replace('mineable_with_','mineable/')
        return '#minecraft:'+name
    if s.startswith('Tags.Items.'): return '#c:'+s.split('.')[-1].lower()
    for method,args in calls(s):
        vals=[value(a) for a in args]
        if method == 'Ingredient.of': return vals[0] if len(vals)==1 else vals
        if method in ('ItemTags.create','BlockTags.create'): return '#'+vals[0].replace('forge:', 'c:', 1)
        if method=='ResourceLocation.fromNamespaceAndPath': return ':'.join(vals)
        if method in ('ResourceLocation.parse','ResourceLocation.tryParse'): return vals[0]
        if method=='OvergearedMod.loc': return 'overgeared:'+vals[0]
        if method in ('has','getHasName','getItemName'): return vals[0]
        if method=='ToolType.PICKAXE.getId': return 'pickaxe'
        if method.startswith('ToolType.') and method.endswith('.getId'): return method.split('.')[1].lower()
        if method=='ResourceLocation.tryBuild': return ':'.join(vals)
        raise ValueError('Unknown value '+s)
    raise ValueError('Unknown value '+s)

def stack(id,count=1): return {'id':id,'count':count}

def generate():
    out={}
    def put(kind,id,data):
        ns,path=id.split(':',1); key=OUT/ns/kind/(path+'.json')
        if key in out and out[key]!=data: raise ValueError('Conflicting generated file '+str(key))
        out[key]=data
    # Silver comes from optional companion mods; an empty common tag keeps the
    # heating recipes valid when those mods are absent, without adding a new ore.
    put('tags/item', 'c:ingots/silver', {'replace':False, 'values':[]})
    # Item tags and block tags are direct declarative chains, including empty extension tags.
    for kind,provider in [('item','ModItemTagGenerator.java'),('block','ModBlockTagGenerator.java')]:
        body=clean((PROVIDERS/provider).read_text()).split('protected void addTags')[1]
        body=body[body.index('{')+1:body.rfind('}')].rsplit('}',1)[0]
        for stmt in split(body,';'):
            chain=calls(stmt); name=value(chain[0][1][0])[1:]; entries=[]; removed=[]
            for method,args in chain[1:]:
                if method in ('add','addTag'): entries.extend(value(a) for a in args)
                elif method=='remove': removed.extend(value(a) for a in args)
                else: raise ValueError(method)
            # Fabric has no NeoForge tag subtraction. Express the two upstream
            # exceptions against the 26.3 vanilla tier definitions explicitly.
            if removed:
                if name == 'overgeared:incorrect_for_copper_tool':
                    entries=['#minecraft:needs_iron_tool', '#minecraft:needs_diamond_tool', '#overgeared:needs_steel_tool']
                elif name == 'overgeared:incorrect_for_steel_tool':
                    entries=['minecraft:netherite_block', 'minecraft:respawn_anchor', 'minecraft:ancient_debris']
                else: raise ValueError('Unsupported tag subtraction: '+name)
            put('tags/'+kind,name,{'replace':False,'values':entries})
    # Block drops retain the original stone-anvil -> cobblestone behavior.
    for name,id in ITEMS.items():
        if not name.startswith('ModBlocks.'): continue
        drop='minecraft:cobblestone' if name=='ModBlocks.STONE_SMITHING_ANVIL' else id
        put('loot_table',id.replace(':',':blocks/'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':drop}], 'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    text=clean((PROVIDERS/'ModRecipeProvider.java').read_text())
    lists={name:[value(v) for v in split(body)] for name,body in re.findall(r'List<ItemLike>\s+(\w+)\s*=\s*List.of\((.*?)\);',text,re.S)}
    body=text.split('protected void buildRecipes(RecipeOutput pWriter)')[1]
    body=body[body.index('{')+1:body.rfind('}')].rsplit('}',1)[0]
    count=0
    for stmt in split(body,';'):
        if stmt.startswith(('CompoundTag nbtTag','nbtTag.putString')): continue
        chain=calls(stmt); factory,raw=chain[0]
        if factory.startswith('ore'):
            method=factory.removeprefix('ore').lower(); args=[value(a) if a not in lists else lists[a] for a in raw]
            _,inputs,category,result,xp,time,group=args
            for ingredient in inputs:
                data={'type':'minecraft:'+method,'ingredient':ingredient,'result':stack(result),'experience':xp,'cookingtime':time}
                if group: data['group']=group
                put('recipe','overgeared:'+result.split(':')[1]+'_from_'+method+'_'+ingredient.split(':')[1],data)
            continue
        a=[value(x) for x in raw]; criteria={}; suffix=''; data={}; count+=1
        if factory in ('ShapedRecipeBuilder.shaped','ShapelessRecipeBuilder.shapeless','OvergearedShapelessRecipeBuilder.shapeless','ShapedForgingRecipeBuilder.shaped'):
            forging=factory.startswith('ShapedForging'); shaped='shaped' in factory
            category,result=a[:2]
            data={'type':('overgeared:forging' if forging else ('overgeared:' if factory.startswith('Overgeared') else 'minecraft:')+('crafting_shaped' if shaped else 'crafting_shapeless')),
                  'category':category if forging else ('equipment' if category in ('tools','combat') else category),
                  'result':stack(result,a[2] if len(a)>2 and not forging else (a[2] if len(a)==4 else 1))}
            if shaped: data.update(pattern=[],key={})
            else: data['ingredients']=[]
            if forging: data.update(hammering=a[-1],tier='iron',need_quenching=not result.endswith(('_helmet','_chestplate','_leggings','_boots')))
        elif factory=='SmithingTransformRecipeBuilder.smithing':
            result=a[4]; data={'type':'minecraft:smithing_transform','template':a[0],'base':a[1],'addition':a[2],'result':stack(result)}
        elif factory=='FletchingRecipeBuilder.fletching':
            result=a[3]; data={'type':'overgeared:fletching','result':stack(result,a[4])}
            data.update({k:v for k,v in zip(('tip','shaft','feather'),a[:3]) if v is not None})
        elif factory in ('CastingRecipeBuilder.casting','ToolCastSmeltingRecipeBuilder.cast','ToolCastBlastingRecipeBuilder.cast'):
            result,xp,time=a
            typ={'CastingRecipeBuilder.casting':'casting','ToolCastSmeltingRecipeBuilder.cast':'cast_smelting','ToolCastBlastingRecipeBuilder.cast':'cast_blasting'}[factory]
            suffix='_from_'+('casting_furnace' if typ=='casting' else typ)
            data={'type':'overgeared:'+typ,'result':stack(result),'input':{},'experience':xp,'cookingtime':time,'need_polishing':typ!='casting'}
        else: raise ValueError(factory)
        id=result
        for method,raw in chain[1:]:
            a=[value(v) for v in raw]
            if method=='pattern': data['pattern'].append(a[0])
            elif method=='define':
                ingredient=a[1]
                if data['type']=='overgeared:forging':
                    ingredient={'ingredient':ingredient,'requires_heated':a[2] if len(a)>2 else False,'transfer_nbt':a[3] if len(a)>3 else False}
                data['key'][a[0]]=ingredient
            elif method=='requires': data['ingredients'].extend([a[0]]*(a[1] if len(a)>1 else 1))
            elif method in ('unlockedBy','unlocks'): criteria[str(a[0]).replace(':','_')]={'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':a[1]}]}}
            elif method=='save':
                if len(a)>1: id=a[1]
            elif method=='material': data['input'][a[0]]=a[1]
            elif method=='setBlueprint': data.setdefault('blueprint',[]).append(a[0])
            elif method=='failedResult': data['result_failed']=stack(a[0],a[1] if len(a)>1 else 1)
            elif method in ('withTippedResult','withLingeringResult'): data['result_tipped' if method=='withTippedResult' else 'result_lingering']=stack(a[0],data['result']['count'])
            else:
                mapping={'tier':'tier','setNeedQuenching':'need_quenching','setQuality':'has_quality','needsMinigame':'needs_minigame','setPolishing':'has_polishing','qualityDifficulty':'quality_difficulty','group':'group','toolType':'tool_type','needsPolishing':'need_polishing'}
                if method not in mapping: raise ValueError(method)
                data[mapping[method]]=a[0]
        id+=suffix
        put('recipe',id,data)
        if criteria:
            criteria['has_the_recipe']={'trigger':'minecraft:recipe_unlocked','conditions':{'recipes':[id]}}
            put('advancement',id.replace(':',':recipes/'),{'parent':'minecraft:recipes/root','criteria':criteria,'requirements':[list(criteria)],'rewards':{'recipes':[id]}})
    if count<150: raise ValueError('Provider unexpectedly incomplete: '+str(count))
    return out

if __name__=='__main__':
    args=argparse.ArgumentParser(); args.add_argument('--check',action='store_true'); args=args.parse_args()
    changed=[]
    for path,data in generate().items():
        text=json.dumps(data,indent=2)+'\n'
        if not path.exists() or path.read_text(encoding='utf-8')!=text:
            changed.append(str(path.relative_to(ROOT)))
            if not args.check: path.parent.mkdir(parents=True,exist_ok=True); path.write_text(text,encoding='utf-8')
    print(f'{len(generate())} gameplay definitions; {len(changed)} '+('outdated' if args.check else 'updated'))
    if args.check and changed: raise SystemExit('\n'.join(changed))
