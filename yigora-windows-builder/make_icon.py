from PIL import Image, ImageDraw
im=Image.new("RGBA",(256,256),"white");d=ImageDraw.Draw(im)
# Monochrome geometric Y inspired by the approved black Yigora monogram
d.polygon([(37,49),(87,49),(148,118),(148,220),(111,220),(111,133)],fill="black")
d.polygon([(125,118),(164,49),(219,49),(171,108),(148,120)],fill="black")
im.save("Yigora_1.0.0.ico",sizes=[(16,16),(32,32),(48,48),(64,64),(128,128),(256,256)])
