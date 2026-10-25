#!/usr/bin/env python3
"""Create a complete, offline 100-language text resource from the original 18 phrases.
Uses publicly accessible Google Translate web endpoint at build time; not an official API.
Stops the build on any unsupported/untranslated language: no silent English fallback.
"""
import json, re, time, urllib.parse, urllib.request, pathlib, random, sys

ROOT=pathlib.Path(__file__).resolve().parents[1]
DATA=ROOT/"app/src/main/assets/messages.json"
data=json.loads(DATA.read_text(encoding="utf-8"))
langs=data["languages"]
assert len(langs)==100 and len(set(langs))==100, "Need exactly 100 unique languages"
src_p=data["phrases"]["en"]
src_g=data["labels"]["en"]["g"]
src_t=data["labels"]["en"]["tap"]
assert len(src_p)==18 and len(src_g)==4 and len(src_t)==8
src=src_p+src_g+src_t

def translate_batch(target, items):
    if target=="en": return items[:]
    tagged="\n".join("⟦%03d⟧ %s"%(n,s) for n,s in enumerate(items))
    qs=urllib.parse.urlencode({"client":"gtx","sl":"en","tl":target,"dt":"t","q":tagged})
    req=urllib.request.Request("https://translate.googleapis.com/translate_a/single?"+qs, headers={"User-Agent":"Mozilla/5.0 (compatible; MoodWeatherTranslate/0.2)"})
    with urllib.request.urlopen(req,timeout=30) as r:
        obj=json.loads(r.read().decode("utf-8"))
    result="".join(p[0] or "" for p in obj[0] if p)
    blocks=re.split(r'⟦\s*(\d{3})\s*⟧',result)
    if len(blocks)!=1+len(items)*2:
        raise ValueError("translation markers lost, expected %d got %d; excerpt: %s"%(len(items), (len(blocks)-1)//2, result[:180]))
    translated={}
    for i in range(1,len(blocks),2):
        index=int(blocks[i]); v=blocks[i+1].strip()
        if index>=len(items) or index in translated or not v:
            raise ValueError("invalid translated item")
        translated[index]=v
    if len(translated)!=len(items):
        raise ValueError("missing translation item")
    return [translated[i] for i in range(len(items))]

def translate(target):
    # One 30-line translation request usually preserves markers. Fallback to
    # smaller blocks if the service changes line segmentation.
    try:
        return translate_batch(target,src)
    except Exception as e:
        print(f"  full-group fallback: {str(e)[:120]}",flush=True)
    outputs=[]
    for start in range(0,len(src),5):
        part=src[start:start+5]
        for attempt in range(3):
            try:
                outputs.extend(translate_batch(target,part)); break
            except Exception as e:
                if attempt==2: raise RuntimeError(f"{target} [{start}]: {e}")
                time.sleep(1+attempt*2)
    return outputs

for ix, code in enumerate(langs):
    if code in data["phrases"] and code in data["labels"]:
        continue
    for attempt in range(3):
        try:
            out=translate(code)
            # Copy AZ originals, and retain original 7 languages. Never inject
            # a new phrase, just translate each existing one.
            assert len(out)==30 and all(isinstance(v,str) and v.strip() for v in out)
            if sum(a.strip()==b.strip() for a,b in zip(out[:18],src_p))>10:
                raise ValueError("many outputs unchanged from English")
            data["phrases"][code]=out[:18]
            data["labels"][code]={"g":out[18:22],"tap":out[22:30]}
            print(f"[{ix+1}/100] {code} translated",flush=True)
            break
        except Exception as exc:
            print(f"[{ix+1}/100] {code} attempt {attempt+1}: {exc}",flush=True)
            if attempt==2: raise
            time.sleep((attempt+1)*3+random.random())

# Validate EVERY language before building an APK.
assert set(data["phrases"])==set(langs), "Some language translations are missing"
assert set(data["labels"])==set(langs), "Some language labels are missing"
for code in langs:
    assert len(data["phrases"][code])==18,code
    assert len(data["labels"][code]["g"])==4,code
    assert len(data["labels"][code]["tap"])==8,code
    assert all(v.strip() for v in data["phrases"][code]),code
assert data["phrases"]["az"]==json.loads(DATA.read_text(encoding="utf8"))["phrases"]["az"]
DATA.write_text(json.dumps(data,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("SUCCESS 100 languages × 30 texts = 3000 localized strings",flush=True)
