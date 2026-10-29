"""Render the exact approved black Yigora monogram as a Windows .ico."""
from PIL import Image,ImageDraw
import re
path_data="M365,12 295,12 294,13 291,13 290,14 285,15 277,19 266,28 266,29 251,44 251,45 218,79 218,81 217,82 217,94 219,98 239,121 239,122 248,132 248,133 252,137 252,138 254,139 365,24Z M15,12 163,181 168,196 168,288 172,298 183,307 234,307 234,179 227,160 113,28 105,21 91,14 82,12Z"
im=Image.new("RGBA",(512,512),"white")
d=ImageDraw.Draw(im)
for block in re.findall(r"M([^Z]+)Z",path_data):
    coords=[(int(x),int(y)) for x,y in re.findall(r"([0-9]+),([0-9]+)",block)]
    points=[(int((512-410)/2+x*410/366),int((512-345)/2+y*345/308)) for x,y in coords]
    d.polygon(points,fill="black")
im.save("Yigora_1.0.0.ico",sizes=[(16,16),(32,32),(48,48),(64,64),(128,128),(256,256)])
