"""Yigora 1.0.0 portable Windows launcher. No local Python installation needed."""
import sys, os, pathlib, runpy, threading, webbrowser, traceback

def main():
    folder=pathlib.Path(sys.executable if getattr(sys,'frozen',False) else __file__).resolve().parent
    script=folder/"server.py"
    if not script.is_file():
        import ctypes
        ctypes.windll.user32.MessageBoxW(0,"server.py not found beside Yigora.exe. Extract the full ZIP package first.","Yigora 1.0.0",0x10)
        return 1
    logdir=pathlib.Path(os.environ.get("LOCALAPPDATA",str(folder)))/"Yigora"/"logs"
    logdir.mkdir(parents=True,exist_ok=True)
    output=open(logdir/"Yigora.log","a",encoding="utf-8",buffering=1)
    sys.stdout=output
    sys.stderr=output
    os.environ["YIGORA_ASSET_DIR"]=str(folder)
    os.environ["YIGORA_DATA_DIR"]=str(pathlib.Path(os.environ.get("LOCALAPPDATA",str(folder)))/"Yigora"/"data")
    if str(folder) not in sys.path:sys.path.insert(0,str(folder))
    port=os.environ.get("YIGORA_PORT","8765")
    def browse():
        try:webbrowser.open("http://127.0.0.1:"+port)
        except Exception:pass
    threading.Timer(2.5,browse).start()
    try:runpy.run_path(str(script),run_name="__main__")
    except Exception:
        traceback.print_exc()
        try:
            import ctypes
            ctypes.windll.user32.MessageBoxW(0,"Yigora could not start. See the log in AppData\\Local\\Yigora\\logs\\Yigora.log.","Yigora error",0x10)
        except Exception:pass
        return 2
    return 0

if __name__=="__main__":sys.exit(main())
