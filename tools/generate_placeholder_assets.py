#!/usr/bin/env python3
# =============================================================================
#  THE LAST REMNANT - Generator Aset Sementara (Placeholder)
#  Bagian   : Aset pixel art & audio (sementara)
#  PJ       : Carlos (aset final) - script ini dibuat agar tim tidak menunggu aset
#  Minggu   : W2 (sesuai mitigasi risiko "gunakan aset sementara")
#
#  Cara pakai (dari root repo):
#      python3 tools/generate_placeholder_assets.py
#  Butuh: Python 3 + Pillow (pip install pillow)
#
#  Semua file hasil ada di folder assets/. Carlos cukup MENIMPA file PNG/WAV
#  dengan nama & ukuran grid yang sama, kode Java tidak perlu diubah.
# =============================================================================
import json
import math
import os
import random
import struct
import wave

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "assets")
random.seed(17)


def out(*parts):
    path = os.path.join(ROOT, *parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    return path


# -----------------------------------------------------------------------------
# Palet warna: sore hari di Depok Selatan (hangat, sedikit pudar)
# -----------------------------------------------------------------------------
OUTLINE = (34, 28, 41, 255)
GRASS = [(86, 140, 72), (98, 154, 78), (72, 122, 64)]
ASPHALT = [(70, 72, 84), (78, 80, 92), (62, 64, 76)]
LANE = (226, 212, 168)
SIDEWALK = [(176, 166, 150), (158, 148, 134), (190, 180, 162)]
PAVING = [(150, 146, 140), (136, 132, 128), (166, 162, 154)]
WALL = [(226, 210, 176), (200, 182, 150), (238, 226, 198)]
ROOF = [(178, 84, 60), (150, 66, 50), (204, 108, 74)]
WOOD = [(122, 80, 54), (98, 62, 42), (146, 100, 68)]
GLASS = [(92, 132, 166), (130, 170, 196)]
WATER = [(58, 88, 98), (74, 108, 116)]
LEAF = [(56, 108, 68), (74, 134, 78), (44, 88, 58), (96, 156, 90)]
TRUNK = [(100, 70, 50), (78, 54, 40)]
IRON = [(64, 64, 76), (90, 90, 104)]
AWNING = [(196, 72, 62), (238, 228, 208)]
DIRT = [(150, 116, 82), (132, 100, 70)]


def rgba(c, a=255):
    return (c[0], c[1], c[2], a)


def px(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), rgba(c) if len(c) == 3 else c)


def rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, c)


def noise_fill(img, ox, oy, w, h, colors, weights=None):
    for y in range(h):
        for x in range(w):
            px(img, ox + x, oy + y, random.choices(colors, weights=weights)[0])


def auto_outline(img, color=OUTLINE):
    """Beri garis tepi 1px di sekeliling piksel tidak transparan."""
    src = img.copy()
    for y in range(img.height):
        for x in range(img.width):
            if src.getpixel((x, y))[3] != 0:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < img.width and 0 <= ny < img.height and src.getpixel((nx, ny))[3] != 0 \
                        and src.getpixel((nx, ny))[:3] != color[:3]:
                    img.putpixel((x, y), color)
                    break


# =============================================================================
# 1. TILESET 16x16 (margin 1, spacing 2, tepi di-extrude agar tidak "bleeding")
# =============================================================================
T = 16
COLS = 8
tile_painters = []


def tile(fn):
    tile_painters.append(fn)
    return fn


@tile
def t_grass(t):
    noise_fill(t, 0, 0, T, T, GRASS, [6, 2, 2])


@tile
def t_grass_tuft(t):
    t_grass(t)
    for (x, y) in ((4, 9), (10, 4), (12, 12)):
        px(t, x, y, LEAF[3]); px(t, x - 1, y + 1, LEAF[3]); px(t, x + 1, y + 1, LEAF[3])


@tile
def t_grass_flowers(t):
    t_grass(t)
    for (x, y, c) in ((3, 4, (236, 210, 90)), (11, 7, (232, 120, 120)), (6, 12, (240, 240, 230)), (13, 13, (236, 210, 90))):
        px(t, x, y, c); px(t, x, y + 1, LEAF[2])


@tile
def t_dirt(t):
    noise_fill(t, 0, 0, T, T, DIRT, [5, 2])


@tile
def t_asphalt(t):
    noise_fill(t, 0, 0, T, T, ASPHALT, [6, 2, 2])


@tile
def t_asphalt_lane(t):
    t_asphalt(t)
    rect(t, 2, 7, 11, 8, LANE)


@tile
def t_sidewalk(t):
    noise_fill(t, 0, 0, T, T, SIDEWALK, [6, 2, 2])
    rect(t, 0, 0, 15, 0, SIDEWALK[1]); rect(t, 0, 0, 0, 15, SIDEWALK[1])
    rect(t, 0, 8, 15, 8, SIDEWALK[1]); rect(t, 8, 0, 8, 15, SIDEWALK[1])


@tile
def t_curb(t):
    t_sidewalk(t)
    rect(t, 0, 12, 15, 13, (210, 204, 190)); rect(t, 0, 14, 15, 15, (60, 56, 64))


@tile
def t_wall(t):
    noise_fill(t, 0, 0, T, T, WALL, [8, 1, 1])
    rect(t, 0, 15, 15, 15, WALL[1])


@tile
def t_wall_window(t):
    t_wall(t)
    rect(t, 3, 3, 12, 11, (240, 234, 220))
    rect(t, 4, 4, 11, 10, GLASS[0]); rect(t, 4, 4, 6, 6, GLASS[1])
    rect(t, 7, 4, 8, 10, (240, 234, 220)); rect(t, 2, 12, 13, 12, WOOD[1])


@tile
def t_door(t):
    t_wall(t)
    rect(t, 3, 2, 12, 15, WOOD[1]); rect(t, 4, 3, 11, 15, WOOD[0])
    rect(t, 5, 4, 10, 8, WOOD[2]); px(t, 10, 10, (230, 200, 90))


@tile
def t_roof(t):
    for y in range(T):
        for x in range(T):
            c = ROOF[0] if (y % 4) else ROOF[1]
            if (y % 4) == 1 and (x + (y // 4) * 2) % 4 == 0:
                c = ROOF[2]
            px(t, x, y, c)


@tile
def t_roof_edge(t):
    t_roof(t)
    rect(t, 0, 12, 15, 13, ROOF[1]); rect(t, 0, 14, 15, 15, (60, 40, 40))


@tile
def t_roof_ridge(t):
    t_roof(t)
    rect(t, 0, 0, 15, 2, ROOF[2]); rect(t, 0, 3, 15, 3, ROOF[1])


@tile
def t_gutter(t):
    rect(t, 0, 0, 15, 15, (120, 118, 112))
    rect(t, 0, 3, 15, 12, WATER[0])
    for x in range(0, 16, 5):
        px(t, x, 6, WATER[1]); px(t, x + 1, 6, WATER[1]); px(t, (x + 3) % 16, 10, WATER[1])


@tile
def t_fence(t):
    t_grass(t)
    rect(t, 0, 5, 15, 6, IRON[1]); rect(t, 0, 11, 15, 12, IRON[1])
    for x in (1, 5, 9, 13):
        rect(t, x, 2, x + 1, 14, IRON[0])


def leaf_blob(t, cx, cy, r):
    for y in range(T):
        for x in range(T):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                c = LEAF[0]
                if d > r - 2:
                    c = LEAF[2]
                if (x - cx) < -1 and (y - cy) < -1 and random.random() < 0.6:
                    c = LEAF[1]
                if random.random() < 0.08:
                    c = LEAF[3]
                px(t, x, y, c)


@tile
def t_tree_tl(t):
    leaf_blob(t, 16, 16, 15)


@tile
def t_tree_tr(t):
    leaf_blob(t, -1, 16, 15)


@tile
def t_tree_bl(t):
    leaf_blob(t, 16, -1, 15)


@tile
def t_tree_br(t):
    leaf_blob(t, -1, -1, 15)


@tile
def t_trunk(t):
    t_grass(t)
    rect(t, 6, 0, 9, 13, TRUNK[0]); rect(t, 6, 0, 6, 13, TRUNK[1])
    rect(t, 3, 13, 12, 14, (64, 104, 58))


@tile
def t_awning(t):
    for x in range(T):
        c = AWNING[(x // 4) % 2]
        rect(t, x, 0, x, 11, c)
    for x in range(0, 16, 4):
        rect(t, x, 12, x + 1, 13, AWNING[0])
    rect(t, 0, 0, 15, 0, (150, 50, 44))


@tile
def t_counter(t):
    t_wall(t)
    rect(t, 0, 6, 15, 15, WOOD[0]); rect(t, 0, 6, 15, 7, WOOD[2])
    for x, c in ((2, (230, 160, 70)), (7, (120, 190, 120)), (12, (236, 210, 90))):
        rect(t, x, 2, x + 2, 5, (220, 230, 236)); rect(t, x, 3, x + 2, 5, c)


@tile
def t_bench_l(t):
    noise_fill(t, 0, 0, T, T, SIDEWALK, [6, 2, 2])
    rect(t, 3, 6, 15, 9, WOOD[2]); rect(t, 3, 9, 15, 9, WOOD[1]); rect(t, 4, 10, 5, 13, IRON[0])


@tile
def t_bench_r(t):
    noise_fill(t, 0, 0, T, T, SIDEWALK, [6, 2, 2])
    rect(t, 0, 6, 12, 9, WOOD[2]); rect(t, 0, 9, 12, 9, WOOD[1]); rect(t, 10, 10, 11, 13, IRON[0])


@tile
def t_lamp_post(t):
    t_sidewalk(t)
    rect(t, 7, 0, 8, 13, IRON[0]); rect(t, 5, 13, 10, 14, IRON[0])


@tile
def t_lamp_top(t):
    rect(t, 7, 6, 8, 15, IRON[0]); rect(t, 4, 3, 11, 5, IRON[1])
    rect(t, 5, 6, 10, 7, (250, 226, 150))


@tile
def t_bush(t):
    t_grass(t)
    leaf_blob(t, 8, 9, 6)


@tile
def t_wall_number(t):
    t_wall(t)
    rect(t, 4, 5, 11, 10, (40, 70, 110)); rect(t, 5, 6, 10, 9, (60, 100, 150))
    # angka "17" kecil
    w = (240, 240, 240)
    rect(t, 6, 6, 6, 9, w)                                   # angka 1
    rect(t, 8, 6, 10, 6, w); px(t, 10, 7, w); px(t, 9, 8, w); px(t, 9, 9, w)  # angka 7


@tile
def t_paving(t):
    for y in range(T):
        for x in range(T):
            c = PAVING[0] if ((x // 4 + y // 4) % 2) else PAVING[2]
            if x % 4 == 0 or y % 4 == 0:
                c = PAVING[1]
            px(t, x, y, c)


@tile
def t_wall_shadow(t):
    t_grass(t)
    for y in range(4):
        for x in range(T):
            r, g, b, a = t.getpixel((x, y))
            px(t, x, y, (int(r * .7), int(g * .7), int(b * .75)))


@tile
def t_pot(t):
    t_sidewalk(t)
    rect(t, 5, 9, 10, 14, (176, 96, 64)); rect(t, 5, 9, 10, 9, (200, 120, 80))
    leaf_blob_small = [(7, 6), (8, 5), (6, 7), (9, 7), (8, 7), (7, 8), (8, 8), (10, 6), (5, 6)]
    for (x, y) in leaf_blob_small:
        px(t, x, y, LEAF[1])


@tile
def t_gate(t):
    t_paving(t)
    rect(t, 0, 3, 15, 4, IRON[1])
    for x in range(1, 16, 3):
        rect(t, x, 3, x, 14, IRON[0])


@tile
def t_crate(t):
    t_sidewalk(t)
    rect(t, 2, 4, 13, 14, WOOD[2]); rect(t, 2, 4, 13, 4, WOOD[1]); rect(t, 2, 9, 13, 9, WOOD[1])
    rect(t, 2, 4, 2, 14, WOOD[1]); rect(t, 13, 4, 13, 14, WOOD[1])


@tile
def t_slab(t):
    # penutup selokan (jembatan kecil di mulut gang)
    noise_fill(t, 0, 0, T, T, [(168, 164, 156), (150, 146, 140)], [5, 2])
    rect(t, 0, 0, 15, 0, (110, 108, 104)); rect(t, 0, 15, 15, 15, (110, 108, 104))


TILE_NAMES = [f.__name__[2:] for f in tile_painters]
TID = {name: i for i, name in enumerate(TILE_NAMES)}
SOLID = {"wall", "wall_window", "door", "roof", "roof_edge", "roof_ridge", "gutter", "fence", "trunk",
         "counter", "lamp_post", "bush", "wall_number", "pot", "crate", "bench_l", "bench_r"}

MARGIN, SPACING = 1, 2


def build_tileset():
    rows = math.ceil(len(tile_painters) / COLS)
    w = MARGIN * 2 + COLS * T + (COLS - 1) * SPACING
    h = MARGIN * 2 + rows * T + (rows - 1) * SPACING
    sheet = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for i, fn in enumerate(tile_painters):
        t = Image.new("RGBA", (T, T), (0, 0, 0, 0))
        fn(t)
        x = MARGIN + (i % COLS) * (T + SPACING)
        y = MARGIN + (i // COLS) * (T + SPACING)
        sheet.paste(t, (x, y))
        # extrude 1px ke segala arah (anti garis tipis antar tile)
        for k in range(T):
            sheet.putpixel((x - 1, y + k), t.getpixel((0, k)))
            sheet.putpixel((x + T, y + k), t.getpixel((T - 1, k)))
            sheet.putpixel((x + k, y - 1), t.getpixel((k, 0)))
            sheet.putpixel((x + k, y + T), t.getpixel((k, T - 1)))
    sheet.save(out("maps", "town_tiles.png"))
    return w, h


# =============================================================================
# 2. KARAKTER 16x24 (4 arah x 4 frame) - baris: bawah, kiri, kanan, atas
# =============================================================================
CW, CH = 16, 24
SKIN = [(232, 180, 140), (206, 150, 112)]


def draw_char(img, ox, oy, d, f, look):
    layer = Image.new("RGBA", (CW, CH), (0, 0, 0, 0))
    bob = -1 if f in (1, 3) else 0
    # --- kaki ---
    leg = look["pants"]
    lx_off = {0: (0, 0), 1: (-1, 1), 2: (0, 0), 3: (1, -1)}[f]
    if d in ("left", "right"):
        a, b = {0: (0, 0), 1: (-2, 2), 2: (0, 0), 3: (2, -2)}[f]
        rect(layer, 6 + a, 17, 7 + a, 21, leg); rect(layer, 8 + b, 17, 9 + b, 21, leg)
        rect(layer, 6 + a, 22, 8 + a, 22, look["shoes"]); rect(layer, 8 + b, 22, 10 + b, 22, look["shoes"])
    else:
        rect(layer, 5, 17, 7, 21 + lx_off[0], leg); rect(layer, 8, 17, 10, 21 + lx_off[1], leg)
        rect(layer, 5, 22 + lx_off[0], 7, 22 + lx_off[0], look["shoes"])
        rect(layer, 8, 22 + lx_off[1], 10, 22 + lx_off[1], look["shoes"])
    if look.get("skirt"):
        rect(layer, 4, 15 + bob, 11, 21, look["skirt"]); rect(layer, 4, 21, 11, 21, tuple(int(c * .8) for c in look["skirt"]))
    # --- badan ---
    top = 10 + bob
    rect(layer, 4, top, 11, 17 + bob, look["shirt"])
    rect(layer, 4, top, 11, top, look.get("collar", look["shirt"]))
    if look.get("stripe"):
        rect(layer, 4, top + 4, 11, top + 4, look["stripe"])
    # lengan (ayun saat jalan)
    sw = {0: 0, 1: 1, 2: 0, 3: -1}[f]
    if d in ("down", "up"):
        rect(layer, 3, top + 1 + sw, 3, top + 6 + sw, look["shirt"]); px(layer, 3, top + 7 + sw, SKIN[0])
        rect(layer, 12, top + 1 - sw, 12, top + 6 - sw, look["shirt"]); px(layer, 12, top + 7 - sw, SKIN[0])
    else:
        ax = 7 + (sw * 2 if d == "right" else -sw * 2)
        rect(layer, ax, top + 1, ax + 1, top + 6, tuple(int(c * .85) for c in look["shirt"]))
        px(layer, ax, top + 7, SKIN[0])
    if look.get("backpack") and d != "down":
        if d == "up":
            rect(layer, 4, top + 1, 11, top + 7, look["backpack"]); rect(layer, 4, top + 1, 11, top + 1, (240, 200, 90))
        else:
            bx = 3 if d == "right" else 10
            rect(layer, bx, top, bx + 2, top + 7, look["backpack"])
    # --- kepala ---
    hy = 2 + bob
    rect(layer, 4, hy, 11, hy + 7, SKIN[0])
    rect(layer, 4, hy + 7, 11, hy + 7, SKIN[1])
    hair = look["hair"]
    if look.get("hijab"):
        hj = look["hijab"]
        rect(layer, 3, hy - 1, 12, hy + 9, hj)
        if d == "down":
            rect(layer, 5, hy + 2, 10, hy + 7, SKIN[0])
        elif d == "left":
            rect(layer, 4, hy + 2, 8, hy + 7, SKIN[0])
        elif d == "right":
            rect(layer, 7, hy + 2, 11, hy + 7, SKIN[0])
        rect(layer, 3, hy + 9, 12, hy + 10, tuple(int(c * .85) for c in hj))
    else:
        if d == "up":
            rect(layer, 4, hy, 11, hy + 6, hair)
        elif d == "down":
            rect(layer, 4, hy, 11, hy + 1, hair); px(layer, 4, hy + 2, hair); px(layer, 11, hy + 2, hair)
        elif d == "left":
            rect(layer, 4, hy, 11, hy + 1, hair); rect(layer, 9, hy, 11, hy + 5, hair)
        else:
            rect(layer, 4, hy, 11, hy + 1, hair); rect(layer, 4, hy, 6, hy + 5, hair)
        if look.get("cap"):
            cap = look["cap"]
            rect(layer, 4, hy - 1, 11, hy + 1, cap)
            if d == "down":
                rect(layer, 4, hy + 2, 11, hy + 2, tuple(int(c * .7) for c in cap))
            elif d == "left":
                rect(layer, 2, hy + 1, 5, hy + 2, tuple(int(c * .7) for c in cap))
            elif d == "right":
                rect(layer, 10, hy + 1, 13, hy + 2, tuple(int(c * .7) for c in cap))
    eye = (40, 34, 48)
    if d == "down":
        rect(layer, 6, hy + 4, 6, hy + 5, eye); rect(layer, 9, hy + 4, 9, hy + 5, eye)
        if look.get("glasses"):
            rect(layer, 5, hy + 3, 10, hy + 3, (60, 60, 70)); px(layer, 5, hy + 4, (60, 60, 70)); px(layer, 10, hy + 4, (60, 60, 70))
        if look.get("mustache"):
            rect(layer, 6, hy + 6, 9, hy + 6, look["hair"])
    elif d == "left":
        rect(layer, 5, hy + 4, 5, hy + 5, eye)
        if look.get("glasses"):
            rect(layer, 4, hy + 3, 7, hy + 3, (60, 60, 70))
    elif d == "right":
        rect(layer, 10, hy + 4, 10, hy + 5, eye)
        if look.get("glasses"):
            rect(layer, 8, hy + 3, 11, hy + 3, (60, 60, 70))
    auto_outline(layer)
    img.alpha_composite(layer, (ox, oy))


LOOKS = {
    # Iwan - kurir: topi & jaket teal, ransel paket
    "iwan": dict(hair=(48, 36, 32), cap=(40, 132, 128), shirt=(46, 146, 140), collar=(30, 104, 100),
                 stripe=(240, 168, 64), pants=(52, 56, 74), shoes=(30, 30, 36), backpack=(196, 140, 72)),
    # Bu Sari - pemilik warung: kerudung marun, baju kunyit
    "sari": dict(hair=(40, 30, 30), hijab=(128, 46, 58), shirt=(214, 160, 64), pants=(96, 66, 50),
                 shoes=(70, 50, 40), skirt=(110, 78, 60)),
    # Pak Darto - pensiunan: rambut uban, kacamata, sarung kotak hijau
    "darto": dict(hair=(200, 200, 204), glasses=True, mustache=True, shirt=(150, 104, 70), collar=(120, 80, 54),
                  pants=(60, 104, 84), shoes=(70, 50, 40), skirt=(60, 104, 84)),
}


def build_characters():
    for name, look in LOOKS.items():
        sheet = Image.new("RGBA", (CW * 4, CH * 4), (0, 0, 0, 0))
        for row, d in enumerate(("down", "left", "right", "up")):
            for f in range(4):
                draw_char(sheet, f * CW, row * CH, d, f, look)
        sheet.save(out("sprites", f"{name}.png"))


# =============================================================================
# 3. OBJEK (16x16): benda memori, ikon perangkat, indikator, kilau
# =============================================================================
def sprite16(fn, name):
    s = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    fn(s)
    auto_outline(s)
    s.save(out("sprites", f"{name}.png"))


def obj_kite(s):
    pts = [(8, 1), (14, 7), (8, 13), (2, 7)]
    d = ImageDraw.Draw(s)
    d.polygon(pts, fill=(222, 84, 74, 255))
    d.polygon([(8, 1), (14, 7), (8, 7)], fill=(240, 200, 90, 255))
    d.polygon([(2, 7), (8, 13), (8, 7)], fill=(240, 200, 90, 255))
    d.line([(8, 1), (8, 13)], fill=(120, 60, 50, 255)); d.line([(2, 7), (14, 7)], fill=(120, 60, 50, 255))
    for i, (x, y) in enumerate(((9, 14), (10, 15))):
        px(s, x, y, (240, 240, 230))


def obj_photo(s):
    rect(s, 2, 4, 13, 13, (244, 238, 222))
    rect(s, 3, 5, 12, 11, (150, 128, 100))
    rect(s, 4, 7, 5, 11, (90, 70, 60)); rect(s, 4, 6, 5, 6, (232, 180, 140))
    rect(s, 9, 7, 10, 11, (128, 46, 58)); rect(s, 9, 6, 10, 6, (232, 180, 140))
    rect(s, 3, 5, 12, 5, (186, 160, 126))


def obj_radio(s):
    rect(s, 1, 5, 14, 14, (120, 82, 56)); rect(s, 1, 5, 14, 5, (150, 104, 70))
    rect(s, 3, 7, 8, 12, (60, 50, 44))
    for y in range(7, 13, 2):
        rect(s, 3, y, 8, y, (90, 76, 66))
    rect(s, 10, 7, 12, 9, (236, 214, 150)); px(s, 11, 11, (40, 40, 40)); px(s, 11, 12, (200, 200, 200))
    rect(s, 11, 1, 11, 4, (180, 180, 190))


def obj_device(s):
    # The Remnant: perangkat genggam dengan layar sian
    rect(s, 3, 1, 12, 14, (58, 62, 78)); rect(s, 3, 1, 12, 1, (90, 96, 116))
    rect(s, 5, 3, 10, 8, (60, 220, 210)); rect(s, 5, 3, 10, 3, (170, 250, 240))
    px(s, 7, 5, (20, 80, 90)); px(s, 8, 6, (20, 80, 90))
    px(s, 6, 11, (230, 90, 90)); px(s, 9, 11, (90, 210, 120))


def obj_parcel(s):
    rect(s, 2, 4, 13, 13, (196, 140, 72)); rect(s, 2, 4, 13, 5, (216, 164, 92))
    rect(s, 7, 4, 8, 13, (232, 216, 176)); rect(s, 4, 8, 6, 10, (240, 240, 236))


def build_misc():
    for fn, n in ((obj_kite, "obj_kite"), (obj_photo, "obj_photo"), (obj_radio, "obj_radio"),
                  (obj_device, "remnant_icon"), (obj_parcel, "obj_parcel")):
        sprite16(fn, n)
    # indikator interaksi (panah kecil 8x8)
    ind = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    for y, (a, b) in enumerate(((1, 6), (1, 6), (2, 5), (3, 4))):
        rect(ind, a, y + 1, b, y + 1, (250, 236, 170))
    auto_outline(ind)
    ind.save(out("sprites", "indicator.png"))
    # titik putih 1x1 (dipakai untuk partikel, overlay, garis UI)
    Image.new("RGBA", (1, 1), (255, 255, 255, 255)).save(out("ui", "pixel.png"))
    # bayangan elips di bawah karakter
    sh = Image.new("RGBA", (12, 4), (0, 0, 0, 0))
    ImageDraw.Draw(sh).ellipse([0, 0, 11, 3], fill=(20, 16, 28, 90))
    sh.save(out("sprites", "shadow.png"))
    # cahaya lembut (glow) untuk benda memori
    g = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    for y in range(32):
        for x in range(32):
            dist = math.hypot(x - 15.5, y - 15.5) / 16
            a = max(0.0, 1 - dist) ** 2
            g.putpixel((x, y), (255, 255, 255, int(a * 255)))
    g.save(out("sprites", "glow.png"))


# =============================================================================
# 4. UI: panel 9-patch, kursor, slot fragmen, vignette
# =============================================================================
def build_ui():
    p = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    rect(p, 1, 0, 22, 23, (22, 20, 34, 236)); rect(p, 0, 1, 23, 22, (22, 20, 34, 236))
    rect(p, 2, 1, 21, 1, (236, 222, 190)); rect(p, 2, 22, 21, 22, (236, 222, 190))
    rect(p, 1, 2, 1, 21, (236, 222, 190)); rect(p, 22, 2, 22, 21, (236, 222, 190))
    rect(p, 3, 3, 20, 3, (60, 54, 80)); rect(p, 3, 3, 3, 20, (60, 54, 80))
    p.save(out("ui", "panel.png"))

    tag = Image.new("RGBA", (12, 12), (0, 0, 0, 0))
    rect(tag, 1, 0, 10, 11, (236, 222, 190)); rect(tag, 0, 1, 11, 10, (236, 222, 190))
    rect(tag, 1, 10, 10, 10, (190, 172, 140))
    tag.save(out("ui", "tag.png"))

    cur = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    for x, (a, b) in enumerate(((1, 6), (2, 5), (3, 4))):
        rect(cur, x + 2, a, x + 2, b, (250, 210, 110))
    cur.save(out("ui", "cursor.png"))

    for name, fill in (("slot_empty", None), ("slot_full", (90, 230, 220))):
        s = Image.new("RGBA", (12, 12), (0, 0, 0, 0))
        d = ImageDraw.Draw(s)
        d.polygon([(6, 0), (11, 6), (6, 11), (1, 6)], fill=(236, 222, 190, 255))
        d.polygon([(6, 2), (9, 6), (6, 9), (3, 6)], fill=(fill or (40, 36, 56)) + (255,))
        s.save(out("ui", f"{name}.png"))

    v = Image.new("RGBA", (128, 72), (0, 0, 0, 0))
    for y in range(72):
        for x in range(128):
            dx, dy = (x - 63.5) / 64, (y - 35.5) / 36
            dist = math.sqrt(dx * dx + dy * dy)
            a = max(0.0, min(1.0, (dist - 0.55) / 0.6)) ** 1.6
            v.putpixel((x, y), (16, 10, 26, int(a * 200)))
    v.save(out("ui", "vignette.png"))


# =============================================================================
# 5. LATAR MENU UTAMA (480x270): langit senja, siluet kota, kabel listrik
# =============================================================================
def build_menu_bg():
    W, H = 480, 270
    sky = Image.new("RGBA", (W, H))
    top, mid, low = (34, 30, 66), (148, 74, 92), (242, 160, 96)
    bands = 18
    for y in range(H):
        t = y / (H * 0.8)
        t = min(1, t)
        c1, c2, tt = (top, mid, t * 2) if t < 0.5 else (mid, low, (t - 0.5) * 2)
        # dithering ala pixel art di batas pita
        bt = tt * bands
        frac = bt - int(bt)
        for x in range(W):
            k = int(bt) + (1 if ((x + y) % 2 == 0 and frac > 0.5) else 0)
            q = min(1, k / bands)
            c = tuple(int(c1[i] + (c2[i] - c1[i]) * q) for i in range(3))
            sky.putpixel((x, y), c + (255,))
    d = ImageDraw.Draw(sky)
    d.ellipse([330, 150, 372, 192], fill=(252, 214, 140, 255))
    d.ellipse([336, 156, 366, 186], fill=(255, 232, 170, 255))
    for _ in range(40):
        x, y = random.randrange(W), random.randrange(0, 90)
        px(sky, x, y, (230, 220, 255))
    sky.save(out("ui", "menu_sky.png"))

    far = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(far)
    col = (86, 54, 92, 255)
    x = 0
    while x < W:
        w = random.randint(14, 34)
        h = random.randint(40, 110)
        d.rectangle([x, H - 60 - h, x + w, H], fill=col)
        for wy in range(H - 56 - h, H - 70, 7):
            for wx in range(x + 3, x + w - 3, 5):
                if random.random() < 0.18:
                    d.rectangle([wx, wy, wx + 1, wy + 2], fill=(250, 210, 130, 255))
        x += w + random.randint(0, 6)
    # menara pemancar & kubah masjid
    d.rectangle([120, 60, 122, 160], fill=col); d.line([(110, 160), (121, 60), (132, 160)], fill=col, width=2)
    px(far, 121, 58, (255, 90, 90)); px(far, 121, 59, (255, 90, 90))
    d.ellipse([268, 132, 312, 176], fill=col); d.rectangle([268, 154, 312, 210], fill=col)
    d.rectangle([318, 110, 322, 210], fill=col); d.polygon([(316, 110), (320, 96), (324, 110)], fill=col)
    far.save(out("ui", "menu_far.png"))

    near = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(near)
    col = (40, 26, 50, 255)
    x = -10
    while x < W:
        w = random.randint(40, 70)
        base = H - random.randint(46, 70)
        d.rectangle([x, base, x + w, H], fill=col)
        d.polygon([(x - 4, base), (x + w // 2, base - random.randint(16, 26)), (x + w + 4, base)], fill=col)
        if random.random() < 0.7:
            wx = x + random.randint(8, max(9, w - 16))
            d.rectangle([wx, base + 10, wx + 7, base + 17], fill=(255, 196, 120, 255))
            d.line([(wx + 4, base + 10), (wx + 4, base + 17)], fill=col)
        x += w
    # tiang & kabel listrik
    for pxl in (60, 230, 400):
        d.rectangle([pxl, 120, pxl + 2, H], fill=col); d.rectangle([pxl - 8, 126, pxl + 10, 127], fill=col)
    for (x0, x1) in ((60, 230), (230, 400), (400, 560), (-110, 60)):
        for off in (0, 6):
            pts = []
            for i in range(31):
                t = i / 30
                xx = x0 + (x1 - x0) * t
                yy = 127 + off + math.sin(t * math.pi) * 14
                pts.append((xx, yy))
            d.line(pts, fill=col, width=1)
    near.save(out("ui", "menu_near.png"))


# =============================================================================
# 6. FONT BITMAP (format AngelCode .fnt, dibaca BitmapFont libGDX)
#    Dirender dari DejaVu Sans Bold tanpa anti-aliasing -> tampilan pixel.
# =============================================================================
FONT_CANDIDATES = ["/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "DejaVuSans-Bold.ttf"]


def build_font(size=13, name="font"):
    path = next((p for p in FONT_CANDIDATES if os.path.exists(p)), None)
    if path is None:
        print("  ! DejaVuSans-Bold.ttf tidak ditemukan, font dilewati (pakai font bawaan libGDX)")
        return
    ft = ImageFont.truetype(path, size)
    ascent, descent = ft.getmetrics()
    line_h = ascent + descent
    chars = [chr(c) for c in range(32, 127)]
    sheet = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(sheet)
    d.fontmode = "1"
    x, y = 1, 1
    lines = []
    for ch in chars:
        bbox = ft.getbbox(ch)
        adv = round(ft.getlength(ch))
        gw = max(0, bbox[2] - bbox[0])
        gh = max(0, bbox[3] - bbox[1])
        if ch == " ":
            lines.append(f"char id=32 x=0 y=0 width=0 height=0 xoffset=0 yoffset=0 xadvance={adv} page=0 chnl=15")
            continue
        if x + gw + 2 > 256:
            x, y = 1, y + line_h + 2
        d.text((x - bbox[0], y - bbox[1]), ch, font=ft, fill=(255, 255, 255, 255))
        lines.append(f"char id={ord(ch)} x={x} y={y} width={gw} height={gh} xoffset={bbox[0]} "
                     f"yoffset={bbox[1]} xadvance={adv} page=0 chnl=15")
        x += gw + 2
    sheet.save(out("fonts", f"{name}.png"))
    with open(out("fonts", f"{name}.fnt"), "w") as fh:
        fh.write(f'info face="DejaVu Sans Bold" size={size} bold=1 italic=0 charset="" unicode=1 '
                 f'stretchH=100 smooth=0 aa=1 padding=0,0,0,0 spacing=1,1\n')
        fh.write(f"common lineHeight={line_h + 2} base={ascent} scaleW=256 scaleH=256 pages=1 packed=0\n")
        fh.write(f'page id=0 file="{name}.png"\n')
        fh.write(f"chars count={len(lines)}\n")
        fh.write("\n".join(lines) + "\n")


# =============================================================================
# 7. SFX sederhana (WAV 22kHz mono, disintesis)
# =============================================================================
RATE = 22050


def write_wav(name, samples):
    with wave.open(out("sfx", f"{name}.wav"), "w") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s)) * 30000)) for s in samples))


def tone(freq, dur, vol=0.4, shape="square", decay=8.0, slide=0.0):
    n = int(RATE * dur)
    res, phase = [], 0.0
    for i in range(n):
        t = i / RATE
        f = freq + slide * t
        phase += f / RATE
        if shape == "square":
            v = 1 if (phase % 1) < 0.5 else -1
        elif shape == "tri":
            v = 4 * abs((phase % 1) - 0.5) - 1
        else:
            v = math.sin(2 * math.pi * phase)
        res.append(v * vol * math.exp(-decay * t))
    return res


def build_sfx():
    write_wav("blip", tone(660, 0.04, 0.18, "square", 30))
    write_wav("select", tone(520, 0.06, 0.25, "tri", 25) + tone(780, 0.05, 0.2, "tri", 25))
    write_wav("confirm", tone(523, 0.07, 0.25, "tri", 12) + tone(659, 0.07, 0.25, "tri", 12) + tone(988, 0.16, 0.25, "tri", 10))
    scan = []
    for i, f in enumerate((440, 554, 659, 880, 1108)):
        scan += tone(f, 0.07, 0.18, "sine", 6)
    scan += tone(1318, 0.5, 0.2, "sine", 4)
    write_wav("scan", scan)
    write_wav("journal", tone(300, 0.05, 0.2, "tri", 20, slide=1800) + tone(900, 0.08, 0.15, "sine", 20))


# =============================================================================
# 8. PETA AREA UJI (TMX) - 40 x 26 tile
#    Peta kota final (W3) dibuat Iqbal di Tiled; file ini hanya area uji W2.
# =============================================================================
MW, MH = 40, 26


def build_map(ts_w, ts_h):
    G = [[TID["grass"]] * MW for _ in range(MH)]      # layer ground
    D = [[-1] * MW for _ in range(MH)]                # layer detail (di atas ground, di bawah entity)
    O = [[-1] * MW for _ in range(MH)]                # layer overhead (di atas entity)

    def setg(x, y, name): G[y][x] = TID[name]
    def setd(x, y, name): D[y][x] = TID[name]
    def seto(x, y, name): O[y][x] = TID[name]

    for y in range(MH):
        for x in range(MW):
            r = random.random()
            G[y][x] = TID["grass_tuft"] if r < 0.12 else TID["grass_flowers"] if r < 0.16 else TID["grass"]

    # baris pohon di atas & bawah peta (batas)
    def tree(x, y):
        setd(x, y + 1, "trunk"); setd(x + 1, y + 1, "bush")
        seto(x, y - 1, "tree_tl"); seto(x + 1, y - 1, "tree_tr"); seto(x, y, "tree_bl"); seto(x + 1, y, "tree_br")

    for x in range(MW):
        setd(x, 0, "fence")
    # --- rumah-rumah di utara jalan (baris 1..6) ---
    def house(x0, w, window_cols, door_col, number=False):
        for x in range(x0, x0 + w):
            setd(x, 2, "roof_ridge"); setd(x, 3, "roof"); setd(x, 4, "roof_edge")
            setd(x, 5, "wall"); setd(x, 6, "wall")
        for c in window_cols:
            setd(x0 + c, 5, "wall_window")
        setd(x0 + door_col, 6, "door")
        if number:
            setd(x0 + door_col + 1, 5, "wall_number")

    for x in range(MW):
        for y in (1,):
            setg(x, y, "grass")
    house(1, 8, (1, 5), 3)
    house(11, 7, (1, 4), 3)
    # warung Bu Sari (x 21..28)
    for x in range(21, 29):
        setd(x, 2, "roof_ridge"); setd(x, 3, "roof"); setd(x, 4, "roof_edge"); setd(x, 5, "awning"); setd(x, 6, "counter")
    house(31, 8, (1, 6), 4)
    # trotoar utara + curb
    for x in range(MW):
        setg(x, 7, "sidewalk"); setg(x, 8, "curb")
    setd(10, 7, "lamp_post"); seto(10, 6, "lamp_top")
    setd(30, 7, "lamp_post"); seto(30, 6, "lamp_top")
    setd(20, 7, "crate"); setd(29, 7, "pot"); setd(19, 7, "pot")
    # jalan raya (baris 9..11)
    for x in range(MW):
        setg(x, 9, "asphalt"); setg(x, 10, "asphalt_lane" if x % 2 == 0 else "asphalt"); setg(x, 11, "asphalt")
    # selokan (baris 12) + penutup di mulut gang (x 17..19)
    for x in range(MW):
        setd(x, 12, "gutter")
    for x in (17, 18, 19):
        D[12][x] = -1; setg(x, 12, "slab")
    for x in (5, 6):
        D[12][x] = -1; setg(x, 12, "slab")
    # trotoar selatan
    for x in range(MW):
        setg(x, 13, "sidewalk")
    # --- Gang Kenanga (x 17..19) turun ke selatan ---
    for y in range(14, MH):
        for x in (17, 18, 19):
            setg(x, y, "paving")
    for y in range(14, MH - 2):
        setd(16, y, "fence"); setd(20, y, "fence")
    # bukaan pagar ke taman (kiri) & ke rumah No.17 (kanan)
    for y in (17, 18):
        D[y][16] = -1
    for y in (19, 20):
        D[y][20] = -1
    for x in range(21, 26):
        for y in (19, 20):
            setg(x, y, "paving")
    # --- taman kecil (kiri bawah) ---
    for y in range(15, 24):
        for x in range(2, 15):
            if random.random() < 0.25:
                setg(x, y, "grass_flowers")
    for x in range(3, 13):
        setg(x, 17, "dirt"); setg(x, 18, "dirt")
    for x in range(13, 16):
        setg(x, 17, "dirt"); setg(x, 18, "dirt")
    setg(7, 16, "dirt"); setg(8, 16, "dirt")
    setd(6, 15, "bench_l"); setd(7, 15, "bench_r")
    G[15][6] = TID["sidewalk"]; G[15][7] = TID["sidewalk"]
    tree(3, 14); tree(11, 14); tree(2, 21); tree(9, 21)
    setd(13, 21, "bush"); setd(14, 22, "bush"); setd(5, 20, "bush")
    for x in range(1, 16):
        setd(x, MH - 1, "fence")
    for y in range(14, MH):
        setd(0, y, "fence")
    # --- rumah Pak Darto, No. 17 (kanan bawah) ---
    for x in range(26, 35):
        setd(x, 14, "roof_ridge"); setd(x, 15, "roof"); setd(x, 16, "roof_edge"); setd(x, 17, "wall"); setd(x, 18, "wall")
    setd(27, 17, "wall_window"); setd(33, 17, "wall_window")
    setd(30, 18, "door"); setd(31, 17, "wall_number")
    for x in range(26, 35):
        setg(x, 19, "sidewalk"); setg(x, 20, "sidewalk")
    setd(28, 19, "pot"); setd(34, 19, "pot")
    for x in range(21, 39):
        setd(x, 22, "fence")
    for y in range(13, 22):
        setd(39, y, "fence")
    for x in range(21, 26):
        setd(x, 14, "bush") if x % 2 else None
    tree(36, 15)
    for x in range(21, 39):
        for y in range(23, MH):
            if random.random() < 0.3:
                setg(x, y, "grass_tuft")
    for x in range(20, MW):
        setd(x, MH - 1, "fence")
    tree(23, 24); tree(33, 24)
    # tepi kiri/kanan atas
    for y in range(1, 7):
        setd(0, y, "fence"); setd(39, y, "fence")

    # ---- collision: gabungkan tile solid sebaris jadi persegi panjang ----
    solid_ids = {TID[n] for n in SOLID}
    rects = []
    for y in range(MH):
        x = 0
        while x < MW:
            if D[y][x] in solid_ids:
                x0 = x
                while x < MW and D[y][x] in solid_ids:
                    x += 1
                rects.append((x0, y, x - x0, 1))
            else:
                x += 1
    # gabung vertikal persegi dengan x & lebar sama
    merged = []
    for r in sorted(rects, key=lambda r: (r[0], r[2], r[1])):
        if merged and merged[-1][0] == r[0] and merged[-1][2] == r[2] and merged[-1][1] + merged[-1][3] == r[1]:
            m = merged[-1]
            merged[-1] = (m[0], m[1], m[2], m[3] + 1)
        else:
            merged.append(r)

    def csv(layer):
        rows = []
        for y in range(MH):
            rows.append(",".join(str(v + 1 if v >= 0 else 0) for v in layer[y]))
        return ",\n".join(rows)

    tilecount = len(tile_painters)
    obj_id = [1]

    def obj(name, x, y, w=0, h=0, props=None, point=False):
        oid = obj_id[0]; obj_id[0] += 1
        s = f'  <object id="{oid}" name="{name}" x="{x}" y="{y}"'
        if not point:
            s += f' width="{w}" height="{h}"'
        s += ">"
        if props:
            s += "\n   <properties>\n" + "".join(
                f'    <property name="{k}" value="{v}"/>\n' for k, v in props.items()) + "   </properties>\n  "
        if point:
            s += "<point/>"
        s += "</object>\n"
        return s

    coll = "".join(obj("solid", x * T, y * T, w * T, h * T) for (x, y, w, h) in merged)
    # spawn & entity. Koordinat Tiled: y ke bawah (dibalik otomatis oleh TmxMapLoader)
    ents = ""
    ents += obj("player", 3 * T, 10 * T + 4, T, T, {"kind": "player"})
    ents += obj("sari", 24 * T, 7 * T, T, T, {"kind": "npc", "npcId": "sari", "facing": "down"})
    ents += obj("darto", 29 * T, 19 * T + 6, T, T, {"kind": "npc", "npcId": "darto", "facing": "down"})
    ents += obj("kite", 13 * T, 14 * T + 4, T, T, {"kind": "memory", "memoryId": "kite"})
    ents += obj("photo", 7 * T, 15 * T + 2, T, T, {"kind": "memory", "memoryId": "photo"})
    ents += obj("radio", 33 * T, 19 * T + 2, T, T, {"kind": "memory", "memoryId": "radio"})

    tmx = f'''<?xml version="1.0" encoding="UTF-8"?>
<!-- Area uji W2 (dibangkitkan tools/generate_placeholder_assets.py). Buka & edit di Tiled. -->
<map version="1.10" tiledversion="1.10.2" orientation="orthogonal" renderorder="right-down" width="{MW}" height="{MH}" tilewidth="{T}" tileheight="{T}" infinite="0" nextlayerid="6" nextobjectid="{obj_id[0] + 1}">
 <properties>
  <property name="displayName" value="Depok Selatan - Jl. Kenanga"/>
 </properties>
 <tileset firstgid="1" name="town" tilewidth="{T}" tileheight="{T}" spacing="{SPACING}" margin="{MARGIN}" tilecount="{tilecount}" columns="{COLS}">
  <image source="town_tiles.png" width="{ts_w}" height="{ts_h}"/>
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
{ents} </objectgroup>
</map>
'''
    with open(out("maps", "test_area.tmx"), "w") as fh:
        fh.write(tmx)

    # pratinjau PNG (untuk dicek tanpa menjalankan game)
    ts = Image.open(out("maps", "town_tiles.png"))
    prev = Image.new("RGBA", (MW * T, MH * T))

    def tile_img(i):
        x = MARGIN + (i % COLS) * (T + SPACING)
        y = MARGIN + (i // COLS) * (T + SPACING)
        return ts.crop((x, y, x + T, y + T))

    for layer in (G, D, O):
        for y in range(MH):
            for x in range(MW):
                if layer[y][x] >= 0:
                    prev.alpha_composite(tile_img(layer[y][x]), (x * T, y * T))
    os.makedirs(os.path.join(ROOT, "..", "docs"), exist_ok=True)
    prev.save(os.path.join(ROOT, "..", "docs", "preview_test_area.png"))
    return len(merged)


def main():
    print("Membuat aset sementara di", os.path.abspath(ROOT))
    w, h = build_tileset()
    print(f"  tileset      : {len(tile_painters)} tile")
    build_characters(); print("  karakter     : iwan, sari, darto")
    build_misc(); build_ui(); print("  objek & UI   : ok")
    build_menu_bg(); print("  latar menu   : ok")
    build_font(13, "font"); print("  font bitmap  : ok")
    build_sfx(); print("  sfx          : ok")
    n = build_map(w, h); print(f"  peta uji     : {MW}x{MH} tile, {n} kotak collision")
    print("Selesai.")


if __name__ == "__main__":
    main()
