#!/usr/bin/env python3
# =============================================================================
#  THE LAST REMNANT - Generator Peta Stage 1 + Aset Senjata (sementara)
#  Bagian   : Core gameplay & world (peta kota, ruang rahasia) + combat (sprite senjata)
#  PJ       : Iqbal Aulia Rachman
#  Minggu   : W3
#
#  Cara pakai (dari root repo):
#      python3 tools/generate_stage1_assets.py
#  Butuh: Python 3 + Pillow. Memakai ulang palet & tile dari
#  generate_placeholder_assets.py (milik Carlos) supaya gaya pixel art sama.
#
#  Hasil:
#    assets/maps/kepompong_tiles.png   tileset kota hancur + interior (16x16)
#    assets/maps/kota_kepompong.tmx    peta Stage 1 (buka & edit di Tiled)
#    assets/sprites/weapon_*.png, bullet.png, dummy*.png, secret_shelf*.png, rubble_gate.png
#    assets/sfx/slash.wav, shot.wav, reload.wav, pickup.wav, empty.wav
#    docs/preview_kota_kepompong.png   pratinjau peta tanpa menjalankan game
#
#  Peta boleh diedit langsung di Tiled setelah dibuat. Jalankan ulang script ini
#  HANYA jika ingin mengulang dari awal (hasil edit Tiled akan tertimpa).
# =============================================================================
import colorsys
import math
import os
import random
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import generate_placeholder_assets as base  # noqa: E402  (palet + tile prolog)

from generate_placeholder_assets import (  # noqa: E402
    OUTLINE, WOOD, IRON, T, COLS, MARGIN, SPACING,
    px, rect, noise_fill, auto_outline, out, tone, write_wav,
)

random.seed(2026)

# =============================================================================
# 1. TILESET "kepompong": tile prolog versi hancur + tile baru
# =============================================================================
DEAD = [(98, 104, 86), (86, 92, 78), (110, 112, 92)]
ASH = [(92, 90, 96), (80, 78, 86), (104, 100, 104)]
ROOT = [(96, 58, 92), (124, 76, 116), (70, 44, 72)]
POD = [(150, 132, 160), (128, 110, 140), (176, 160, 184)]
POD_GLOW = (120, 236, 224)
FLOOR_WOOD = [(112, 82, 62), (98, 70, 54), (124, 92, 70)]
FLOOR_DARK = [(70, 62, 66), (60, 54, 58)]
WALL_IN = [(150, 136, 120), (130, 118, 104), (164, 150, 132)]
VOID = (14, 12, 20)


def grade_ruin(img):
    """Ubah tile prolog (kota ilusi) jadi versi 'Depok yang hancur': pudar & gelap."""
    res = img.copy()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            s *= 0.38
            l = l * 0.78 + 0.02
            r2, g2, b2 = colorsys.hls_to_rgb(h, l, s)
            # sedikit hijau-ungu: debu, lumut, dan sisa akar Kalpataru
            res.putpixel((x, y), (int(r2 * 255 * 0.96), int(g2 * 255), int(b2 * 255 * 1.04), a))
    return res


new_painters = []


def ntile(fn):
    new_painters.append(fn)
    return fn


@ntile
def t_dead_ground(t):
    noise_fill(t, 0, 0, T, T, DEAD, [5, 3, 2])


@ntile
def t_ash(t):
    noise_fill(t, 0, 0, T, T, ASH, [5, 3, 2])
    for _ in range(3):
        px(t, random.randrange(T), random.randrange(T), (130, 126, 128))


@ntile
def t_crack(t):
    t_ash(t)
    x, y = 2, 3
    for _ in range(14):
        px(t, x, y, (44, 42, 50))
        x = min(15, x + random.choice((0, 1, 1)))
        y = min(15, y + random.choice((0, 1)))


@ntile
def t_asphalt_crack(t):
    noise_fill(t, 0, 0, T, T, [(64, 66, 74), (56, 58, 66), (72, 72, 80)], [5, 3, 2])
    x, y = 0, random.randrange(4, 12)
    while x < T:
        px(t, x, y, (32, 32, 40)); px(t, x, y + 1, (84, 84, 92))
        x += 1
        y = max(1, min(14, y + random.choice((-1, 0, 0, 1))))


@ntile
def t_rubble(t):
    t_ash(t)
    for _ in range(5):
        cx, cy = random.randrange(2, 14), random.randrange(2, 14)
        c = random.choice([(168, 154, 134), (140, 128, 112), (120, 72, 60)])
        rect(t, cx - 1, cy - 1, cx + 1, cy, c)
        px(t, cx - 1, cy + 1, OUTLINE[:3])


@ntile
def t_rubble_big(t):
    # tumpukan puing: tidak bisa dilewati
    t_ash(t)
    d = ImageDraw.Draw(t)
    d.polygon([(1, 15), (4, 5), (9, 2), (14, 7), (15, 15)], fill=(132, 120, 106, 255))
    d.polygon([(4, 15), (7, 8), (12, 10), (13, 15)], fill=(110, 98, 88, 255))
    d.line([(9, 2), (14, 7)], fill=(170, 158, 138, 255))
    rect(t, 3, 11, 5, 12, (120, 70, 58)); rect(t, 10, 5, 11, 6, (120, 70, 58))
    d.line([(6, 3), (6, 0)], fill=IRON[1] + (255,))


@ntile
def t_wall_broken(t):
    t.paste(grade_ruin(_orig("wall")), (0, 0))
    d = ImageDraw.Draw(t)
    d.polygon([(5, 0), (12, 0), (10, 6), (6, 9), (3, 4)], fill=VOID + (255,))
    d.line([(10, 6), (14, 12)], fill=(60, 52, 50, 255))
    d.line([(3, 4), (1, 9)], fill=(60, 52, 50, 255))


@ntile
def t_roof_collapsed(t):
    t.paste(grade_ruin(_orig("roof")), (0, 0))
    d = ImageDraw.Draw(t)
    d.polygon([(0, 6), (7, 3), (16, 9), (16, 16), (0, 16)], fill=VOID + (255,))
    for x in range(1, 15, 3):
        d.line([(x, 8), (x + 2, 15)], fill=WOOD[1] + (255,))


def _pod(t, part, opened=False):
    """Kepompong 2x2 tile. part: tl tr bl br."""
    big = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    noise_fill(big, 0, 0, 32, 32, DEAD, [5, 3, 2])
    d = ImageDraw.Draw(big)
    for i in range(4):  # akar
        a = i * math.pi / 2 + 0.6
        d.line([(16, 18), (16 + math.cos(a) * 16, 18 + math.sin(a) * 15)], fill=ROOT[0] + (255,), width=2)
    d.ellipse([5, 2, 27, 30], fill=OUTLINE)
    d.ellipse([6, 3, 26, 29], fill=POD[0] + (255,))
    d.ellipse([9, 5, 18, 16], fill=POD[2] + (255,))
    for y in range(6, 28, 4):
        d.arc([6, y - 4, 26, y + 6], 20, 160, fill=POD[1] + (255,))
    if opened:
        d.polygon([(10, 6), (22, 6), (24, 18), (16, 22), (8, 18)], fill=(40, 30, 46, 255))
        d.line([(10, 6), (8, 18)], fill=POD[2] + (255,)); d.line([(22, 6), (24, 18)], fill=POD[2] + (255,))
        for k in range(4):
            px(big, 12 + k * 3, 19 + (k % 2), POD_GLOW)
    else:
        px(big, 16, 14, POD_GLOW); px(big, 15, 15, POD_GLOW); px(big, 17, 15, POD_GLOW)
    ox = 0 if part[1] == "l" else 16
    oy = 0 if part[0] == "t" else 16
    t.paste(big.crop((ox, oy, ox + 16, oy + 16)), (0, 0))


@ntile
def t_pod_tl(t): _pod(t, "tl")
@ntile
def t_pod_tr(t): _pod(t, "tr")
@ntile
def t_pod_bl(t): _pod(t, "bl")
@ntile
def t_pod_br(t): _pod(t, "br")
@ntile
def t_podo_tl(t): _pod(t, "tl", True)
@ntile
def t_podo_tr(t): _pod(t, "tr", True)
@ntile
def t_podo_bl(t): _pod(t, "bl", True)
@ntile
def t_podo_br(t): _pod(t, "br", True)


@ntile
def t_root(t):
    t_dead_ground(t)
    x, y = 0, random.randrange(5, 11)
    while x < T:
        px(t, x, y, ROOT[0]); px(t, x, y + 1, ROOT[2])
        x += 1
        y = max(2, min(13, y + random.choice((-1, 0, 1))))


@ntile
def t_root_glow(t):
    t_root(t)
    for _ in range(3):
        px(t, random.randrange(2, 14), random.randrange(2, 14), POD_GLOW)


@ntile
def t_dead_trunk(t):
    t_dead_ground(t)
    rect(t, 6, 0, 9, 15, (78, 64, 58)); rect(t, 6, 0, 6, 15, (60, 50, 46))
    d = ImageDraw.Draw(t)
    d.line([(9, 4), (14, 0)], fill=(78, 64, 58, 255), width=2)
    d.line([(6, 7), (1, 2)], fill=(78, 64, 58, 255), width=2)


@ntile
def t_car_l(t):
    t_asphalt_crack(t)
    rect(t, 1, 4, 15, 13, (112, 60, 56)); rect(t, 1, 4, 15, 5, (138, 80, 72))
    rect(t, 4, 6, 10, 9, (60, 70, 84)); rect(t, 2, 12, 5, 15, (30, 28, 34))
    rect(t, 1, 4, 1, 13, OUTLINE[:3])


@ntile
def t_car_r(t):
    t_asphalt_crack(t)
    rect(t, 0, 4, 13, 13, (112, 60, 56)); rect(t, 0, 4, 13, 5, (138, 80, 72))
    rect(t, 4, 6, 9, 9, (60, 70, 84)); rect(t, 9, 12, 12, 15, (30, 28, 34))
    rect(t, 13, 4, 13, 13, OUTLINE[:3])


@ntile
def t_post_roof(t):
    # atap pos ronda (seng berkarat)
    noise_fill(t, 0, 0, T, T, [(118, 104, 92), (100, 88, 80)], [3, 2])
    for x in range(0, 16, 3):
        rect(t, x, 0, x, 15, (82, 72, 66))
    rect(t, 0, 14, 15, 15, (60, 52, 48))


@ntile
def t_post_wall(t):
    noise_fill(t, 0, 0, T, T, WOOD, [3, 2, 2])
    rect(t, 0, 0, 15, 1, (70, 50, 40)); rect(t, 3, 4, 12, 9, (40, 32, 34))
    rect(t, 3, 4, 12, 4, (90, 70, 56))


# ---- interior rumah No. 17 + ruang rahasia ----
@ntile
def t_void(t):
    rect(t, 0, 0, 15, 15, VOID)


@ntile
def t_floor_wood(t):
    noise_fill(t, 0, 0, T, T, FLOOR_WOOD, [4, 3, 2])
    for y in (3, 7, 11, 15):
        rect(t, 0, y, 15, y, (82, 60, 46))
    px(t, random.randrange(T), random.choice((1, 5, 9, 13)), (70, 50, 40))


@ntile
def t_floor_dark(t):
    noise_fill(t, 0, 0, T, T, FLOOR_DARK, [3, 2])
    for x in (0, 8):
        rect(t, x, 0, x, 15, (48, 44, 48))
    for y in (0, 8):
        rect(t, 0, y, 15, y, (48, 44, 48))


@ntile
def t_wall_in(t):
    # dinding dalam (permukaan), dilihat dari depan
    noise_fill(t, 0, 0, T, T, WALL_IN, [4, 2, 2])
    rect(t, 0, 13, 15, 15, (96, 84, 76))
    if random.random() < 0.5:
        x = random.randrange(3, 12)
        for k in range(6):
            px(t, x + (k % 2), 2 + k, (100, 90, 82))


@ntile
def t_wall_in_top(t):
    # bagian atas dinding (gelap) - batas ruangan
    rect(t, 0, 0, 15, 15, (46, 40, 50)); rect(t, 0, 13, 15, 15, (70, 62, 70))


@ntile
def t_wall_in_photo(t):
    t_wall_in(t)
    rect(t, 5, 3, 10, 9, (60, 44, 36)); rect(t, 6, 4, 9, 8, (150, 128, 100)); rect(t, 7, 5, 8, 6, (230, 180, 140))


@ntile
def t_door_in(t):
    t_floor_wood(t)
    rect(t, 0, 12, 15, 15, (70, 52, 40))


@ntile
def t_rug(t):
    t_floor_wood(t)
    rect(t, 0, 1, 15, 14, (110, 58, 64)); rect(t, 0, 3, 15, 3, (196, 160, 110)); rect(t, 0, 12, 15, 12, (196, 160, 110))
    for x in range(1, 16, 4):
        px(t, x, 7, (196, 160, 110)); px(t, x + 1, 8, (196, 160, 110))


@ntile
def t_sofa(t):
    t_floor_wood(t)
    rect(t, 0, 3, 15, 13, (84, 96, 110)); rect(t, 0, 3, 15, 6, (100, 114, 128)); rect(t, 0, 13, 15, 13, OUTLINE[:3])


@ntile
def t_table(t):
    t_floor_wood(t)
    rect(t, 1, 3, 14, 10, WOOD[2]); rect(t, 1, 3, 14, 3, (170, 124, 86)); rect(t, 2, 11, 3, 14, WOOD[1]); rect(t, 12, 11, 13, 14, WOOD[1])
    rect(t, 5, 5, 8, 7, (230, 226, 214))


@ntile
def t_cabinet_top(t):
    t_wall_in(t)
    rect(t, 1, 2, 14, 15, WOOD[0]); rect(t, 1, 2, 14, 3, WOOD[2]); rect(t, 7, 4, 8, 15, WOOD[1])


@ntile
def t_cabinet_bottom(t):
    t_floor_wood(t)
    rect(t, 1, 0, 14, 13, WOOD[0]); rect(t, 7, 0, 8, 13, WOOD[1]); px(t, 6, 6, (230, 200, 120)); px(t, 9, 6, (230, 200, 120))
    rect(t, 1, 13, 14, 13, OUTLINE[:3])


@ntile
def t_shelf(t):
    t_wall_in(t)
    rect(t, 1, 1, 14, 15, WOOD[1])
    for y in (5, 10):
        rect(t, 1, y, 14, y, WOOD[0])
    for x in range(2, 14, 2):
        rect(t, x, 2, x, 4, random.choice([(150, 60, 60), (60, 100, 140), (200, 180, 120), (80, 120, 80)]))
        rect(t, x, 7, x, 9, random.choice([(150, 60, 60), (60, 100, 140), (200, 180, 120)]))


@ntile
def t_tv(t):
    t_wall_in(t)
    rect(t, 2, 3, 13, 13, (40, 40, 48)); rect(t, 3, 4, 12, 11, (60, 120, 124))
    for y in range(4, 12, 2):
        rect(t, 3, y, 12, y, (80, 150, 150))
    rect(t, 6, 14, 9, 15, (40, 40, 48))


@ntile
def t_desk(t):
    t_floor_dark(t)
    rect(t, 0, 2, 15, 11, (86, 70, 62)); rect(t, 0, 2, 15, 3, (110, 92, 80))
    rect(t, 2, 4, 6, 8, (230, 224, 200)); rect(t, 8, 5, 12, 9, (220, 214, 190)); px(t, 3, 5, (60, 60, 70)); px(t, 9, 6, (60, 60, 70))
    rect(t, 1, 12, 2, 15, (60, 50, 46)); rect(t, 13, 12, 14, 15, (60, 50, 46))


@ntile
def t_cable(t):
    t_floor_dark(t)
    x, y = 0, 9
    while x < T:
        px(t, x, y, (30, 30, 36)); px(t, x, y - 1, (90, 90, 100))
        x += 1
        y = max(4, min(12, y + random.choice((-1, 0, 1))))


@ntile
def t_boxes(t):
    t_floor_dark(t)
    rect(t, 1, 4, 9, 14, (160, 118, 70)); rect(t, 1, 4, 9, 5, (186, 140, 88)); rect(t, 5, 4, 5, 14, (130, 94, 56))
    rect(t, 8, 1, 14, 8, (150, 110, 66)); rect(t, 8, 1, 14, 2, (176, 132, 82))


@ntile
def t_wall_in_dark(t):
    # dinding ruang rahasia (lebih gelap, ada coretan "EXIT")
    noise_fill(t, 0, 0, T, T, [(96, 90, 100), (84, 78, 88)], [3, 2])
    rect(t, 0, 13, 15, 15, (60, 56, 66))


@ntile
def t_wall_in_note(t):
    t_wall_in_dark(t)
    rect(t, 3, 2, 8, 8, (226, 220, 196)); rect(t, 9, 4, 13, 10, (210, 204, 180))
    d = ImageDraw.Draw(t)
    d.line([(4, 4), (7, 4)], fill=(60, 60, 70, 255)); d.line([(4, 6), (6, 6)], fill=(60, 60, 70, 255))
    d.line([(8, 5), (10, 7)], fill=(170, 40, 40, 255))


_orig_cache = {}


def _orig(name):
    """Tile prolog asli (sebelum dipudarkan)."""
    if name not in _orig_cache:
        t = Image.new("RGBA", (T, T), (0, 0, 0, 0))
        base.tile_painters[base.TID[name]](t)
        _orig_cache[name] = t
    return _orig_cache[name]


# urutan tile = urutan di tileset: semua tile prolog (dipudarkan) lalu tile baru
OLD_NAMES = list(base.TILE_NAMES)
NEW_NAMES = [f.__name__[2:] for f in new_painters]
NAMES = OLD_NAMES + NEW_NAMES
TID = {n: i for i, n in enumerate(NAMES)}
SOLID = set(base.SOLID) | {
    "rubble_big", "wall_broken", "roof_collapsed", "pod_tl", "pod_tr", "pod_bl", "pod_br",
    "podo_tl", "podo_tr", "dead_trunk", "car_l", "car_r", "post_roof", "post_wall",
    "void", "wall_in", "wall_in_top", "wall_in_photo", "sofa", "table", "cabinet_top", "cabinet_bottom",
    "shelf", "tv", "desk", "boxes", "wall_in_dark", "wall_in_note",
}


def build_tileset():
    tiles = []
    random.seed(17)                      # tile prolog dilukis dengan seed yang sama seperti versi asli
    for name in OLD_NAMES:
        t = Image.new("RGBA", (T, T), (0, 0, 0, 0))
        base.tile_painters[base.TID[name]](t)
        tiles.append(grade_ruin(t))
    random.seed(2026)
    for fn in new_painters:
        t = Image.new("RGBA", (T, T), (0, 0, 0, 0))
        fn(t)
        tiles.append(t)
    rows = math.ceil(len(tiles) / COLS)
    w = MARGIN * 2 + COLS * T + (COLS - 1) * SPACING
    h = MARGIN * 2 + rows * T + (rows - 1) * SPACING
    sheet = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for i, t in enumerate(tiles):
        x = MARGIN + (i % COLS) * (T + SPACING)
        y = MARGIN + (i // COLS) * (T + SPACING)
        sheet.paste(t, (x, y))
        for k in range(T):  # extrude 1px (anti garis antar tile)
            sheet.putpixel((x - 1, y + k), t.getpixel((0, k)))
            sheet.putpixel((x + T, y + k), t.getpixel((T - 1, k)))
            sheet.putpixel((x + k, y - 1), t.getpixel((k, 0)))
            sheet.putpixel((x + k, y + T), t.getpixel((k, T - 1)))
    sheet.save(out("maps", "kepompong_tiles.png"))
    return sheet, w, h


# =============================================================================
# 2. SPRITE SENJATA, PELURU, BONEKA LATIHAN, RAK RAHASIA
# =============================================================================
def build_sprites():
    k = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(k)
    d.polygon([(4, 12), (12, 4), (13, 5), (6, 12)], fill=(214, 220, 228, 255))
    d.line([(5, 12), (12, 5)], fill=(250, 252, 255, 255))
    rect(k, 2, 12, 4, 14, (96, 60, 44)); px(k, 5, 11, (60, 60, 70)); px(k, 4, 10, (60, 60, 70))
    auto_outline(k)
    k.save(out("sprites", "weapon_knife.png"))

    p = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rect(p, 2, 5, 13, 8, (70, 72, 84)); rect(p, 2, 5, 13, 5, (110, 112, 126))
    rect(p, 3, 9, 6, 13, (58, 44, 40)); rect(p, 7, 9, 8, 10, (70, 72, 84)); px(p, 13, 6, (30, 30, 36))
    auto_outline(p)
    p.save(out("sprites", "weapon_pistol.png"))

    b = Image.new("RGBA", (4, 4), (0, 0, 0, 0))
    rect(b, 1, 0, 2, 3, (255, 236, 160)); rect(b, 0, 1, 3, 2, (255, 236, 160)); rect(b, 1, 1, 2, 2, (255, 255, 240))
    b.save(out("sprites", "bullet.png"))

    # boneka latihan (16x24): kepala bulat tanpa wajah, jahitan, tiang kayu
    def doll(broken):
        s = Image.new("RGBA", (16, 24), (0, 0, 0, 0))
        rect(s, 7, 14, 8, 22, WOOD[1])
        rect(s, 4, 22, 11, 23, WOOD[0])
        if broken:
            rect(s, 3, 19, 12, 21, (196, 186, 170)); px(s, 6, 20, (120, 40, 40)); px(s, 9, 19, (120, 40, 40))
            rect(s, 10, 17, 13, 19, (210, 200, 186))
        else:
            rect(s, 4, 9, 11, 16, (196, 186, 170)); rect(s, 2, 10, 13, 11, (196, 186, 170))
            rect(s, 7, 9, 8, 16, (150, 60, 60))
            ImageDraw.Draw(s).ellipse([4, 1, 11, 8], fill=(214, 204, 188, 255))
            px(s, 6, 4, (40, 30, 40)); px(s, 9, 4, (40, 30, 40))
            for x in range(5, 11, 2):
                px(s, x, 7, (120, 40, 40))
        auto_outline(s)
        return s

    doll(False).save(out("sprites", "dummy.png"))
    doll(True).save(out("sprites", "dummy_broken.png"))

    # rak buku penutup ruang rahasia (2x2 tile) + posisi setelah digeser
    def shelf(opened):
        s = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        ox = 18 if opened else 0
        w = 14 if opened else 32
        rect(s, ox, 0, ox + w - 1, 31, WOOD[1]); rect(s, ox, 0, ox + w - 1, 1, WOOD[2])
        for y in (9, 19, 29):
            rect(s, ox, y, ox + w - 1, y + 1, WOOD[0])
        for row, y0 in enumerate((2, 12, 22)):
            for x in range(ox + 2, ox + w - 2, 3):
                c = random.choice([(150, 60, 60), (60, 100, 140), (200, 180, 120), (80, 120, 80), (120, 90, 140)])
                rect(s, x, y0 + random.randrange(0, 2), x + 1, y0 + 6, c)
        if not opened:
            px(s, 30, 15, POD_GLOW)  # celah kecil bercahaya: petunjuk ada sesuatu di baliknya
        return s

    shelf(False).save(out("sprites", "secret_shelf.png"))
    shelf(True).save(out("sprites", "secret_shelf_open.png"))

    # gerbang puing di ujung gang (3x2 tile), terbuka setelah rekaman TV ditonton
    g = Image.new("RGBA", (48, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(g)
    d.polygon([(0, 31), (4, 12), (14, 4), (24, 10), (34, 2), (44, 12), (47, 31)], fill=(124, 112, 100, 255))
    d.polygon([(6, 31), (12, 16), (22, 20), (30, 12), (40, 22), (42, 31)], fill=(100, 90, 82, 255))
    for x in range(3, 46, 6):
        d.line([(x, 31), (x + 3, 20)], fill=IRON[1] + (255,))
    rect(g, 18, 22, 28, 25, (120, 70, 58))
    g.save(out("sprites", "rubble_gate.png"))


def build_sfx():
    write_wav("slash", [v * (1 - i / 2200) for i, v in enumerate(
        [random.uniform(-0.35, 0.35) for _ in range(2200)])])
    shot = [random.uniform(-0.6, 0.6) * math.exp(-i / 900) for i in range(4400)]
    write_wav("shot", [a + b for a, b in zip(shot, tone(140, 0.2, 0.3, "sine", 14) + [0] * 4400)])
    write_wav("reload", tone(900, 0.03, 0.2, "square", 40) + [0.0] * 3000 + tone(700, 0.04, 0.22, "square", 40))
    write_wav("pickup", tone(440, 0.05, 0.2, "tri", 18) + tone(660, 0.08, 0.2, "tri", 14))
    write_wav("empty", tone(1200, 0.02, 0.15, "square", 60))


# =============================================================================
# 3. PETA STAGE 1: KOTA KEPOMPONG (80 x 44 tile)
#    x 0-16  : Ladang Kepompong (Zentra bangun di Z-02)
#    x 17-49 : Depok Selatan yang hancur (jalan, gang Kenanga, pos ronda, No. 17)
#    x 50-65 : kosong (agar interior tidak terlihat dari luar)
#    x 66-79 : interior rumah No. 17 + ruang rahasia
# =============================================================================
MW, MH = 80, 44


def build_map(sheet, ts_w, ts_h):
    G = [[TID["void"]] * MW for _ in range(MH)]
    D = [[-1] * MW for _ in range(MH)]
    O = [[-1] * MW for _ in range(MH)]

    def g(x, y, n): G[y][x] = TID[n]
    def d(x, y, n): D[y][x] = TID[n]
    def o(x, y, n): O[y][x] = TID[n]
    def clear(x, y): D[y][x] = -1

    # ---------- dasar luar ----------
    # petak rumput mati berkelompok (blok 5x5), sisanya abu & retakan
    patch = {(bx, by): random.random() < 0.3 for bx in range(0, 12) for by in range(0, 10)}
    for y in range(MH):
        for x in range(50):
            r = random.random()
            if x < 17:
                g(x, y, "root" if r < 0.12 else "root_glow" if r < 0.15 else "dead_ground")
            elif patch[(x // 5, y // 5)] and r < 0.85:
                g(x, y, "dead_ground" if r < 0.8 else "crack")
            else:
                g(x, y, "crack" if r < 0.07 else "rubble" if r < 0.12 else "ash")

    # batas luar: puing di tepi peta & tepi kanan area kota
    for x in range(50):
        d(x, 0, "rubble_big"); d(x, MH - 1, "rubble_big")
    for y in range(MH):
        d(0, y, "rubble_big"); d(49, y, "rubble_big")

    def dead_tree(x, y):
        d(x, y, "dead_trunk")

    # ---------- Ladang Kepompong ----------
    def pod(x, y, opened=False):
        pre = "podo_" if opened else "pod_"
        d(x, y, pre + "tl"); d(x + 1, y, pre + "tr"); d(x, y + 1, pre + "bl"); d(x + 1, y + 1, pre + "br")

    pods = []
    for row, y in enumerate(range(3, 40, 5)):
        for col, x in enumerate(range(2, 15, 4)):
            if (x, y) in ((6, 28),):
                continue
            if random.random() < 0.85:
                pods.append((x + (row % 2), y))
    for (x, y) in pods:
        pod(x, y)
    pod(6, 28, opened=True)          # Z-02: kepompong Zentra, sudah terbuka
    for y in range(1, MH - 1):        # pagar besi rusak antara ladang dan kota
        if y not in (10, 11, 12, 13):
            d(17, y, "fence" if random.random() < 0.8 else "rubble_big")
    for (x, y) in ((15, 7), (3, 19), (14, 33), (2, 40)):
        dead_tree(x, y)

    # ---------- deretan rumah utara (versi hancur dari prolog) ----------
    def ruined_house(x0, w, door, collapsed_cols=()):
        for x in range(x0, x0 + w):
            if x - x0 in collapsed_cols:
                d(x, 2, "roof_collapsed"); d(x, 3, "roof_collapsed"); d(x, 4, "rubble_big")
                d(x, 5, "wall_broken"); d(x, 6, "rubble_big")
            else:
                d(x, 2, "roof_ridge"); d(x, 3, "roof"); d(x, 4, "roof_edge")
                d(x, 5, "wall_window" if (x - x0) % 3 == 1 else "wall"); d(x, 6, "wall")
        d(x0 + door, 6, "door")

    ruined_house(19, 8, 3, collapsed_cols=(5, 6))
    ruined_house(29, 7, 2, collapsed_cols=(0,))
    # warung Bu Sari: runtuh total
    for x in range(38, 45):
        d(x, 2, "roof_collapsed" if x % 2 else "roof_ridge")
        d(x, 3, "roof_collapsed"); d(x, 4, "rubble_big"); d(x, 5, "awning" if x in (40, 41) else "rubble_big")
        d(x, 6, "counter" if x in (39, 40, 41, 42) else "rubble_big")
    for x in range(45, 49):
        for y in range(2, 7):
            d(x, y, "rubble_big")
    for x in range(18, 49):
        g(x, 1, "ash")
        d(x, 1, "fence") if random.random() < 0.6 else d(x, 1, "rubble_big")

    # ---------- jalan raya (baris 7-13) ----------
    for x in range(1, 49):
        g(x, 7, "sidewalk"); g(x, 8, "curb")
        g(x, 9, "asphalt_crack" if random.random() < 0.4 else "asphalt")
        g(x, 10, "asphalt_lane" if x % 2 == 0 and random.random() < 0.7 else "asphalt_crack")
        g(x, 11, "asphalt_crack" if random.random() < 0.4 else "asphalt")
        g(x, 12, "gutter" if x >= 18 else "dead_ground"); g(x, 13, "sidewalk" if x >= 18 else "dead_ground")
    for x in range(1, 17):        # jalan masuk ke ladang kepompong (tanah)
        for y in (7, 8, 9, 10, 11, 12, 13):
            g(x, y, "dead_ground" if random.random() < 0.7 else "root")
    for x in range(1, 17):
        for y in (10, 11, 12):
            D[y][x] = -1          # jalur bebas dari ladang ke jalan
    d(22, 7, "lamp_post"); o(22, 6, "lamp_top")
    d(36, 7, "rubble_big"); d(26, 7, "crate"); d(27, 7, "rubble_big")
    d(24, 10, "car_l"); d(25, 10, "car_r")      # mobil terguling di tengah jalan
    d(41, 9, "rubble_big"); d(42, 9, "rubble_big"); d(31, 12, "rubble_big")
    for x in (33, 34, 35):                       # mulut gang: penutup selokan
        g(x, 12, "slab")

    # ---------- Gang Kenanga (x 33-35) ke selatan ----------
    for y in range(13, MH - 1):
        for x in (33, 34, 35):
            g(x, y, "paving" if random.random() < 0.75 else "crack")
    for y in range(14, MH - 4):
        if y not in (19, 20, 26, 27):
            d(32, y, "fence" if random.random() < 0.85 else "rubble_big")
            d(36, y, "fence" if random.random() < 0.85 else "rubble_big")

    # ---------- taman & pos ronda (barat gang) ----------
    for (x, y) in ((20, 16), (27, 15), (19, 24), (29, 30), (22, 36), (28, 39)):
        dead_tree(x, y)
    for x in range(23, 27):                      # pos ronda 4x2
        d(x, 18, "post_roof"); d(x, 19, "post_wall")
    for x in range(22, 28):
        g(x, 20, "paving"); g(x, 21, "paving")
    d(21, 28, "rubble_big"); d(22, 28, "rubble_big"); d(26, 33, "bench_l"); d(27, 33, "bench_r")
    for x in range(18, 32):
        d(x, MH - 5, "fence") if random.random() < 0.7 else d(x, MH - 5, "rubble_big")

    # ---------- rumah No. 17 (timur gang) ----------
    for x in range(39, 48):
        d(x, 21, "roof_ridge"); d(x, 22, "roof"); d(x, 23, "roof_edge")
        d(x, 24, "wall"); d(x, 25, "wall")
    d(40, 24, "wall_window"); d(46, 24, "wall_window")
    d(43, 25, "door"); d(44, 24, "wall_number")
    d(41, 21, "roof_collapsed"); d(46, 22, "roof_collapsed")
    for x in range(37, 49):
        g(x, 26, "sidewalk"); g(x, 27, "sidewalk")
    for x in range(37, 39):          # jalan setapak dari gang ke halaman No. 17
        for y in (26, 27):
            g(x, y, "paving")
    d(41, 26, "pot"); d(47, 26, "pot")
    for x in range(37, 49):
        d(x, 29, "fence") if x != 42 else None
    for x in range(37, 49):
        d(x, 20, "fence") if random.random() < 0.7 else d(x, 20, "rubble_big")
    dead_tree(38, 31); dead_tree(46, 34); d(40, 36, "rubble_big"); d(44, 38, "rubble_big")

    # ujung selatan gang: gerbang puing (entity Barrier) lalu jalan keluar ke Perpustakaan
    for x in (33, 34, 35):
        g(x, MH - 2, "paving")
    for y in range(MH - 4, MH - 1):   # dinding puing kiri-kanan: satu-satunya jalan lewat gerbang
        d(32, y, "rubble_big"); d(36, y, "rubble_big")

    # ---------- interior rumah No. 17 (x 66-79) ----------
    for y in range(MH):
        for x in range(50, MW):
            g(x, y, "void")

    # ruang rahasia: x 68-77, y 3-9
    for x in range(68, 78):
        d(x, 3, "wall_in_top")
        d(x, 4, "wall_in_dark")
    for x in range(69, 77):
        for y in range(5, 9):
            g(x, y, "floor_dark")
    for y in range(4, 10):
        d(68, y, "wall_in_top"); d(77, y, "wall_in_top")
    for x in range(68, 78):
        if x not in (72, 73):
            d(x, 9, "wall_in_top")
    d(70, 4, "wall_in_note"); d(71, 4, "wall_in_note")
    d(73, 4, "tv")                     # TV rekaman pria bertopeng
    d(69, 5, "desk"); d(70, 5, "desk")  # meja catatan Kalpataru & Proyek EXIT
    d(76, 5, "boxes"); d(76, 6, "boxes")
    g(72, 6, "cable"); g(73, 7, "cable"); g(74, 6, "cable"); g(75, 7, "cable")

    # lorong rahasia di balik rak (x 72-73, y 9-11)
    for y in (9, 10, 11):
        for x in (72, 73):
            g(x, y, "floor_dark")
            clear(x, y)

    # ruang tengah: x 66-79, y 10-19
    for x in range(66, 80):
        if x not in (72, 73):
            d(x, 10, "wall_in_top"); d(x, 11, "wall_in")
    d(69, 11, "wall_in_photo")
    for x in range(67, 79):
        for y in range(12, 19):
            g(x, y, "floor_wood")
    for y in range(10, 20):
        d(66, y, "wall_in_top"); d(79, y, "wall_in_top")
    for x in range(66, 80):
        if x != 72:
            d(x, 19, "wall_in_top")
    g(72, 19, "door_in")
    d(67, 11, "cabinet_top"); d(67, 12, "cabinet_bottom")   # lemari: tempat sembunyi
    d(75, 11, "shelf"); d(76, 11, "shelf")
    d(77, 12, "table")                                        # meja kecil di samping radio
    for x in range(71, 76):
        for y in range(14, 17):
            g(x, y, "rug")
    d(68, 15, "sofa"); d(68, 16, "sofa")
    d(69, 17, "table")
    d(78, 17, "boxes")

    # ---------- collision dari tile solid ----------
    solid_ids = {TID[n] for n in SOLID}
    rects = []
    for y in range(MH):
        x = 0
        while x < MW:
            if D[y][x] in solid_ids or (D[y][x] < 0 and G[y][x] == TID["void"]):
                x0 = x
                while x < MW and (D[y][x] in solid_ids or (D[y][x] < 0 and G[y][x] == TID["void"])):
                    x += 1
                rects.append((x0, y, x - x0, 1))
            else:
                x += 1
    merged = []
    for r in sorted(rects, key=lambda r: (r[0], r[2], r[1])):
        if merged and merged[-1][0] == r[0] and merged[-1][2] == r[2] and merged[-1][1] + merged[-1][3] == r[1]:
            m = merged[-1]
            merged[-1] = (m[0], m[1], m[2], m[3] + 1)
        else:
            merged.append(r)

    # ---------- objek (spawn & penanda) ----------
    oid = [1]

    def obj(name, x, y, w=T, h=T, props=None):
        i = oid[0]; oid[0] += 1
        s = f'  <object id="{i}" name="{name}" x="{x}" y="{y}" width="{w}" height="{h}">'
        if props:
            s += "\n   <properties>\n" + "".join(
                f'    <property name="{k}" value="{v}"/>\n' for k, v in props.items()) + "   </properties>\n  "
        return s + "</object>\n"

    def at(tx, ty, dy=0):
        return tx * T, ty * T + dy

    E = ""
    # Zentra bangun di depan Z-02
    E += obj("player", *at(6, 30, 2), props={"kind": "player", "facing": "down"})
    E += obj("cp_kepompong", *at(6, 30), props={"kind": "checkpoint", "id": "stage1_awal"})
    # boneka latihan: sementara pengganti Boneka (Enemy milik Insan)
    E += obj("boneka_latihan_1", *at(9, 31, 4), props={"kind": "dummy", "hp": "3"})
    E += obj("boneka_latihan_2", *at(4, 33, 4), props={"kind": "dummy", "hp": "3"})
    # titik spawn musuh Boneka (dibaca modul Enemy, Insan)
    for i, (tx, ty) in enumerate(((12, 22), (21, 11), (30, 10), (40, 11), (28, 25), (34, 33))):
        E += obj(f"boneka_{i + 1}", *at(tx, ty, 4), props={"kind": "enemy", "enemyId": "boneka"})
    # pistol berisi 6 peluru di pos ronda
    E += obj("pistol_pos_ronda", *at(25, 20, 8), props={"kind": "weapon", "weaponId": "pistol", "ammo": "6", "reserve": "0"})
    E += obj("catatan_pos_ronda", *at(23, 20), 32, 16,
             props={"kind": "inspect", "dialogueId": "s1_pos_ronda", "label": "Periksa pos ronda"})
    # tempat sembunyi saat dikejar
    E += obj("sembunyi_mobil", *at(24, 11), 32, 16, props={"kind": "hiding"})
    E += obj("sembunyi_lemari", *at(67, 13), 16, 16, props={"kind": "hiding"})
    # pintu rumah No. 17  <->  ruang tengah
    E += obj("pintu_no17", *at(43, 26), 16, 12,
             props={"kind": "warp", "target": "no17_dalam", "label": "Masuk rumah No. 17", "facing": "up"})
    E += obj("no17_luar", *at(43, 27, 4), props={"kind": "marker", "facing": "down"})
    E += obj("pintu_keluar_no17", *at(72, 18, 8), 16, 8,
             props={"kind": "warp", "target": "no17_luar", "label": "Keluar rumah", "facing": "down"})
    E += obj("no17_dalam", *at(72, 17, 4), props={"kind": "marker", "facing": "up"})
    E += obj("cp_no17", *at(72, 17), props={"kind": "checkpoint", "id": "stage1_no17"})
    # radio Pak Darto: dipindai -> flag scanned_radio -> rak bergeser (ruang rahasia terbuka)
    E += obj("radio_darto", *at(76, 12, 6), props={"kind": "memory", "memoryId": "radio"})
    E += obj("rak_rahasia", 72 * T, 10 * T, 32, 32,
             props={"kind": "barrier", "openFlag": "scanned_radio", "sprite": "secret_shelf",
                    "openSprite": "secret_shelf_open"})
    E += obj("foto_dinding", *at(69, 12), 16, 8,
             props={"kind": "inspect", "dialogueId": "s1_foto_dinding", "label": "Lihat foto di dinding"})
    # isi ruang rahasia
    E += obj("tv_rekaman", *at(73, 5), 16, 16,
             props={"kind": "inspect", "dialogueId": "s1_tv_rekaman", "label": "Nyalakan TV",
                    "setFlag": "tv_watched"})
    E += obj("catatan_kalpataru", *at(69, 6), 32, 8,
             props={"kind": "inspect", "dialogueId": "s1_catatan_kalpataru", "label": "Baca catatan"})
    E += obj("papan_exit", *at(70, 5), 32, 8,
             props={"kind": "inspect", "dialogueId": "s1_papan_exit", "label": "Lihat coretan di dinding"})
    # ujung gang: tertutup puing sampai rekaman TV ditonton -> jalan ke Perpustakaan Arsip
    E += obj("gerbang_puing", 33 * T - 8, (MH - 4) * T, 48, 32,
             props={"kind": "barrier", "openFlag": "tv_watched", "sprite": "rubble_gate"})
    E += obj("ke_perpustakaan", *at(34, MH - 2), 16, 16,
             props={"kind": "exit", "nextMap": "maps/perpustakaan_arsip.tmx", "label": "Menuju Perpustakaan Arsip"})

    def csv(layer):
        return ",\n".join(",".join(str(v + 1 if v >= 0 else 0) for v in layer[y]) for y in range(MH))

    coll = "".join(f'  <object id="{0}" name="solid" x="{x * T}" y="{y * T}" width="{w * T}" height="{h * T}"/>\n'
                   for (x, y, w, h) in merged)
    # id unik untuk collision (setelah id entity)
    lines, nid = [], oid[0]
    for line in coll.splitlines(keepends=True):
        lines.append(line.replace('id="0"', f'id="{nid}"', 1)); nid += 1
    coll = "".join(lines)

    tmx = f'''<?xml version="1.0" encoding="UTF-8"?>
<!-- Stage 1: Kota Kepompong (dibangkitkan tools/generate_stage1_assets.py, PJ Iqbal). Buka & edit di Tiled. -->
<map version="1.10" tiledversion="1.10.2" orientation="orthogonal" renderorder="right-down" width="{MW}" height="{MH}" tilewidth="{T}" tileheight="{T}" infinite="0" nextlayerid="6" nextobjectid="{nid}">
 <properties>
  <property name="displayName" value="Kota Kepompong"/>
  <property name="stage" value="1"/>
 </properties>
 <tileset firstgid="1" name="kepompong" tilewidth="{T}" tileheight="{T}" spacing="{SPACING}" margin="{MARGIN}" tilecount="{len(NAMES)}" columns="{COLS}">
  <image source="kepompong_tiles.png" width="{ts_w}" height="{ts_h}"/>
 </tileset>
 <layer id="1" name="ground" width="{MW}" height="{MH}">
  <data encoding="csv">
{csv(G)}
</data>
 </layer>
 <layer id="2" name="detail" width="{MW}" height="{MH}">
  <data encoding="csv">
{csv(D)}
</data>
 </layer>
 <layer id="3" name="overhead" width="{MW}" height="{MH}">
  <data encoding="csv">
{csv(O)}
</data>
 </layer>
 <objectgroup id="4" name="collision" visible="0">
{coll} </objectgroup>
 <objectgroup id="5" name="entities">
{E} </objectgroup>
</map>
'''
    with open(out("maps", "kota_kepompong.tmx"), "w") as fh:
        fh.write(tmx)

    # pratinjau PNG
    prev = Image.new("RGBA", (MW * T, MH * T))

    def tile_img(i):
        x = MARGIN + (i % COLS) * (T + SPACING)
        y = MARGIN + (i // COLS) * (T + SPACING)
        return sheet.crop((x, y, x + T, y + T))

    cache = {}
    for layer in (G, D, O):
        for y in range(MH):
            for x in range(MW):
                v = layer[y][x]
                if v >= 0:
                    if v not in cache:
                        cache[v] = tile_img(v)
                    prev.alpha_composite(cache[v], (x * T, y * T))
    os.makedirs(os.path.join(base.ROOT, "..", "docs"), exist_ok=True)
    prev.save(os.path.join(base.ROOT, "..", "docs", "preview_kota_kepompong.png"))
    return len(merged), oid[0] - 1


def main():
    print("Membuat aset Stage 1 di", os.path.abspath(base.ROOT))
    sheet, w, h = build_tileset()
    print(f"  tileset kepompong : {len(NAMES)} tile ({len(NEW_NAMES)} baru)")
    random.seed(7)
    build_sprites(); print("  sprite senjata, peluru, boneka, rak rahasia : ok")
    build_sfx(); print("  sfx combat : ok")
    random.seed(44)
    n, e = build_map(sheet, w, h)
    print(f"  peta Stage 1 : {MW}x{MH} tile, {n} kotak collision, {e} objek")
    print("Selesai.")


if __name__ == "__main__":
    main()
