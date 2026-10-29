# -*- coding: utf-8 -*-
"""Yigora 1.2.0 standalone Windows setup. Actual financial database is preserved."""
import hashlib, io, os, pathlib, shutil, subprocess, sys, zipfile, ctypes, tempfile, traceback

BEGIN=b"\x00YIGORA_INSTALL_PAYLOAD_v1_2_0_START\x00"
END=b"\x00YIGORA_INSTALL_PAYLOAD_v1_2_0_END\x00"
APP="Yigora"
VERSION="1.2.0"
IDYES=6

def notify(message,title="Yigora 1.2.0",flag=0x40):
    return ctypes.windll.user32.MessageBoxW(0,str(message),title,flag)

def payload():
    blob=pathlib.Path(sys.executable).read_bytes()
    pos=blob.find(BEGIN)
    if pos < 0: raise ValueError("Quraşdırma məlumatları EXE faylında yoxdur.")
    start=pos+len(BEGIN)
    length=int.from_bytes(blob[start:start+8],"little")
    if length<500 or length>250_000_000:raise ValueError("Quraşdırma paketi zədələnib.")
    digest=blob[start+8:start+40]
    data=blob[start+40:start+40+length]
    if len(data)!=length or hashlib.sha256(data).digest()!=digest:raise ValueError("Fayl bütövlüyü yoxlamasından keçmədi.")
    if blob[start+40+length:start+40+length+len(END)]!=END:raise ValueError("Paket başlığı zədələnib.")
    return data

def shortcuts(target):
    try:
        import win32com.client
        shell=win32com.client.Dispatch("WScript.Shell")
        desktop=pathlib.Path(os.environ["USERPROFILE"])/"Desktop"
        menu=pathlib.Path(os.environ["APPDATA"])/"Microsoft"/"Windows"/"Start Menu"/"Programs"/"Yigora"
        menu.mkdir(parents=True,exist_ok=True)
        for dest in [desktop/"Yigora.lnk",menu/"Yigora.lnk"]:
            dest.parent.mkdir(parents=True,exist_ok=True)
            link=shell.CreateShortcut(str(dest))
            link.TargetPath=str(target/"Yigora.exe")
            link.WorkingDirectory=str(target)
            link.IconLocation=str(target/"assets"/"yigora.ico")
            link.Description="Yigora 1.2.0 — mağazaların icarə uçotu"
            link.Save()
    except Exception as ex:
        # A shortcut is optional; a functioning installation must not be lost.
        (target/"shortcut_error.log").write_text(str(ex),encoding="utf-8")

def install(data):
    root=pathlib.Path(os.environ.get("LOCALAPPDATA",pathlib.Path.home()))/"Yigora"
    root.mkdir(parents=True,exist_ok=True)
    data_dir=root/"data"
    data_dir.mkdir(parents=True,exist_ok=True)
    # Preserve the previous financial database and all phone permissions, sessions, password.
    source_prefix="Windows/"
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        if z.testzip() is not None:raise ValueError("ZIP məlumatları zədələnib.")
        required={"Yigora.exe","server.py","excel_bridge.py","web/index.html","assets/yigora.ico"}
        seen=set()
        for info in z.infolist():
            if info.is_dir():continue
            name=info.filename.replace("\\","/")
            if not name.startswith(source_prefix):continue
            rel=pathlib.PurePosixPath(name[len(source_prefix):])
            if rel.is_absolute() or ".." in rel.parts or not rel.parts:raise ValueError("Qeyri-qanuni ZIP yolu")
            relpath=str(rel).replace("/",os.sep)
            # Only expected runtime files. Nothing is executed from outside the package.
            if rel.parts[0] not in ("web","assets","legacy","data") and str(rel) not in (
                    "Yigora.exe","server.py","excel_bridge.py","import_excel.py"):
                continue
            if str(rel) in required:seen.add(str(rel))
            target=root/relpath
            target.parent.mkdir(parents=True,exist_ok=True)
            if rel.parts[0]=="data" and target.exists():continue
            # Do not replace a running EXE in place. Atomic replace on successful extraction.
            with z.open(info) as inp:
                tmp=target.with_name(target.name+".yigora-new")
                with open(tmp,"wb") as out:shutil.copyfileobj(inp,out)
            os.replace(tmp,target)
        if seen!=required:raise ValueError("Paketdə vacib proqram faylları çatışmır: "+str(required-seen))
    # Enable local network clients only. Never open or forward the router's internet port.
    server=root/"server.py"
    source=server.read_text(encoding="utf-8")
    header="os.environ.setdefault('YIGORA_ALLOW_LAN','1')"
    if header not in source:
        # os is imported in server.py
        source=source.replace("BASE=pathlib.Path(",header+"\nBASE=pathlib.Path(",1)
        server.write_text(source,encoding="utf-8")
    shortcuts(root)
    return root

def main():
    if "--selftest" in sys.argv:
        try:
            payload()
            return 0
        except Exception:
            return 2
    ok=notify("Yigora 1.2.0 kompüterinizə quraşdırılacaq.\n\n"
              "• Python, VPN və bulud xidməti tələb olunmur.\n"
              "• Köhnə icarə və ödəniş məlumatları saxlanılır.\n"
              "• Masaüstündə Yigora işarəsi yaranır.\n\n"
              "Quraşdırmaya başlayaq?",
              "Yigora 1.2.0 — Quraşdırma",0x24)
    if ok!=IDYES:return 0
    try:
        target=install(payload())
        notify("Yigora 1.2.0 quraşdırıldı.\n\nMasaüstündə Yigora işarəsi yaradıldı.\n"
               "Telefonun eyni Wi-Fi şəbəkəsində olması kifayətdir.",
               "Yigora — Quraşdırma tamamlandı")
        if (target/"Yigora.exe").is_file():
            subprocess.Popen([str(target/"Yigora.exe")],cwd=str(target),creationflags=0x08000000)
        return 0
    except Exception as e:
        notify("Quraşdırmada xəta:\n"+str(e), "Yigora — Xəta",0x10)
        return 1
if __name__=="__main__":sys.exit(main())
