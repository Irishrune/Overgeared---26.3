"""Approved 0.3 progression layered onto the historical generated resources."""

def add_progression(out, root):
    def put(kind, name, value):
        out[root/'overgeared'/kind/(name+'.json')] = value
    # Armor now uses the same optional-blueprint cap as tools.
    for path, recipe in list(out.items()):
        if recipe.get('type') == 'overgeared:forging' and recipe.get('category') == 'armors':
            item = recipe['result']['id'].split(':')[1]
            for slot in ('helmet', 'chestplate', 'leggings', 'boots'):
                if item.endswith('_'+slot): recipe['blueprint'] = [slot]
    put('blueprint_tooltypes', 'armor', {'tooltypes':['helmet','chestplate','leggings','boots']})
    # Replace the old duplication-only template recipe with a survival entry point.
    put('recipe', 'diamond_upgrade_smithing_template', {
        'type':'minecraft:crafting_shaped', 'category':'misc', 'pattern':['DDD','DBD',' S '],
        'key':{'D':'minecraft:diamond','B':'overgeared:empty_blueprint','S':'overgeared:steel_ingot'},
        'result':{'id':'overgeared:diamond_upgrade_smithing_template','count':1}})
    traits = {t:[] for t in ('balanced','heavy','sturdy','vitality')}
    for material in ('stone','copper','iron','golden','steel'):
        ns = 'overgeared' if material in ('copper','steel') else 'minecraft'
        equipment = []
        for tool in ('sword','axe','pickaxe','shovel','hoe'):
            equipment.append((f'{ns}:{material}_{tool}',tool))
            part = 'sword_blade' if tool == 'sword' else tool+'_head'
            equipment.append((f'overgeared:{material}_{part}',tool))
        if material != 'stone':
            equipment += [(f'{ns}:{material}_{slot}',slot) for slot in ('helmet','chestplate','leggings','boots')]
        for item, kind in equipment:
            traits['sturdy'].append(item)
            if kind in ('sword','axe'):
                traits['balanced'].append(item); traits['heavy'].append(item)
            if kind == 'chestplate': traits['vitality'].append(item)
    # One equipment item + the treatment ingredients, in the displayed anvil layout.
    materials = {'balanced':'minecraft:copper_ingot','heavy':'minecraft:iron_ingot',
                 'sturdy':'minecraft:iron_nugget','vitality':'minecraft:golden_apple'}
    for trait, items in traits.items():
        put('tags/item', 'treatable/'+trait, {'replace':False,'values':items})
        for item in items:
            name = 'treatment/'+trait+'/'+item.split(':')[1]
            recipe = {'type':'overgeared:forging','category':'misc','treatment':trait,
                      'pattern':['EM',' M',' M'] if trait == 'sturdy' else ['EM'],
                      'key':{'E':{'ingredient':item,'transfer_nbt':True},'M':{'ingredient':materials[trait]}},
                      'result':{'id':item,'count':1},'hammering':4,'tier':'iron','has_quality':False,
                      'needs_minigame':True,'has_polishing':False,'need_quenching':False,'quality_difficulty':'expert'}
            put('recipe',name,recipe)
            put('advancement','recipes/'+name,{'parent':'minecraft:recipes/root',
                'criteria':{'has_equipment':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item}]}}},
                'requirements':[['has_equipment']], 'rewards':{'recipes':['overgeared:'+name]}})
