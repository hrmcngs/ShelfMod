# HK Bamboo Scaffolding — 香港竹棚 & 養生ネット

![Bamboo scaffolding with green safety mesh in a forest](https://raw.githubusercontent.com/hrmcngs/ShelfMod/main/images/scaffold-overview.png)

> 🇬🇧 [English](#english) · 🇯🇵 [日本語](#日本語) · 🇨🇳 [简体中文](#简体中文)

| Loader | MC version |
|---|---|
| **Forge** | 1.20.1 |
| **NeoForge** | 1.21.4 |
| **Fabric** | 1.21.4 |

---

## English

A Minecraft mod recreating Hong Kong's iconic **bamboo scaffolding (竹棚)** and **construction safety mesh (養生ネット / 安全網)**. Each block holds **up to 4 bamboo poles per axis (12 in total)**, so you can build realistic scaffolds, bridges and tower structures.

### Features

- 🎋 **Up to 12 poles per block** (4 each on X / Y / Z)
- 🪜 Modern **iron scaffolding** alternative
- 🧱 The **diagonal bamboo brace** acts as a 3-block-range stabilizer — drop one in a gap to bridge two pole supports
- 🟩 Safety mesh: **6 colors × 4 fire-resistance variants**
- 🔥 Flammable mesh + bamboo will actually spread fire (historical accuracy)
- 💧 Fully waterloggable

### Controls — please read this!

Placing these blocks has a learning curve.

| Action | Result |
|---|---|
| **Plain right-click** | Places a new pole in the empty space next to the clicked face |
| **Shift + right-click an existing pole block** | Adds another pole on the **axis of the clicked face** |
| Repeat shift + right-click | Count cycles 1 → 2 → 3 → 4 → **back to 1** |

**Which face you click determines which axis grows:**

| Face clicked | Axis | Effect |
|---|---|---|
| **Top / Bottom** | Y (vertical) | Adds one more **vertical** standard pole in this block |
| **North / South** | Z (N–S) | Adds one more horizontal pole running **north–south** |
| **East / West** | X (E–W) | Adds one more horizontal pole running **east–west** |

So: shift + right-click **the face that points the direction you want a new pole to grow**.

### Example sequence

1. Right-click ground → 1 vertical pole (`count_y = 1`)
2. Shift + right-click its **top face** → second vertical pole in the same block (`count_y = 2`)
3. Repeat twice more → `count_y = 4` (max)
4. Shift + right-click again → loops back to `count_y = 1`
5. Shift + right-click the **south face** → adds a horizontal Z-axis pole (`count_z = 1`)
6. Result: one block containing a vertical pole AND a horizontal pole together

![Multi-axis scaffolding closeup](https://raw.githubusercontent.com/hrmcngs/ShelfMod/main/images/scaffold-closeup.png)

### Blocks

**Scaffold poles**

| Block | HK name | Diameter | Role |
|---|---|---|---|
| `bamboo_scaffold` | **Mao Jue (毛竹)** | ≥75mm | Load-bearing vertical standard |
| `kao_bamboo_scaffold` | **Kao Jue (篙竹)** | ≥40mm | Horizontal ledger / cross-brace |
| `iron_scaffold` | (metal) | — | Modern replacement |

**Brace**

| Block | HK name | Description |
|---|---|---|
| `bamboo_brace` | **斜撑** | 45° diagonal brace; NE-SW / NW-SE auto-picked by player facing |

![Bamboo brace closeup](https://raw.githubusercontent.com/hrmcngs/ShelfMod/main/images/brace-detail.png)

**Brace stabilizer effect**

The bamboo brace has no collision, but it acts as a long-range anchor:

- A **pole stays up if a brace is within 3 cardinal blocks**
- A **brace stays up if a pole is within 3 cardinal blocks**
- Together: drop **one** brace in the middle of a gap to bridge up to **6 blocks** between two anchored poles

**Safety mesh** — 6 colors (green / black / white / blue / silver / gray), 4 variants each:

| Suffix | Flame-resistant | Burns | Use |
|---|---|---|---|
| `*_safety_mesh` (default) | ✅ | ❌ | Standard. Safe even on bamboo |
| `*_safety_mesh_flammable` | ❌ | ✅ | **Burns** — historical recreation |
| `*_safety_mesh_heatproof` | ✅✅ | ❌ | Reinforced fire resistance |
| `*_safety_mesh_flammable_heatproof` | ❌ | ✅ | Flammable but heat-tolerant |

### Crafting

> Full recipes: `data/shelfmod/recipe/*.json`

- **bamboo_scaffold**: bamboo × 6 + string × 1
- **kao_bamboo_scaffold**: bamboo × 3 + string × 1
- **bamboo_brace**: kao bamboo × 2 (vertical)
- **iron_scaffold**: iron ingot × 6
- **safety mesh**: wool × 1 + dye → 4 panels

### Background

Inspired by Hong Kong's traditional bamboo scaffolding craft and informed by the **November 2025 Wang Fuk Court (宏福苑) fire**, where flammable safety mesh combined with bamboo scaffolding contributed to the deadliest residential fire in HK since 1948 (168 victims). The `_flammable` mesh variants reproduce this physical behaviour, so the choice of material carries real in-game consequences.

### Compliant pairings

| Scaffold | Mesh | Result |
|---|---|---|
| Bamboo | Default (flame-resistant) | ✅ Recommended |
| **Iron** | Default (flame-resistant) | ✅✅ Matches latest HK government guidance |
| Bamboo | Flammable mesh | ⚠️ Will burn (intentional recreation) |

### Credits & License

- Author: [hrmcngs](https://github.com/hrmcngs)
- HK Building Authority reference data
- Feedback welcome → [GitHub Issues](https://github.com/hrmcngs/ShelfMod/issues)
- **MIT License** — see [LICENSE](LICENSE)

---

## 日本語

香港の建設現場でおなじみの「**竹棚 (Bamboo Scaffolding)**」と「**養生ネット (Safety Mesh)**」を Minecraft に持ち込むモッドです。
1 ブロックに **各軸 4 本ずつ最大 12 本** の竹を詰め込めるので、リアルな足場・橋桁・棚田の組まれた竹組みを表現できます。

### 主な特徴

- 🎋 **1 ブロックに最大 12 本** の竹(X/Y/Z 各軸 4 本ずつ)
- 🪜 **金属足場(Iron Scaffold)** も同じ操作で組める
- 🧱 **斜めの筋交い(Bamboo Brace)** が半径3ブロックの「橋桁スタビライザー」として働く
- 🟩 **養生ネット 6 色 × 4 種** (通常/可燃/防燃/可燃+防燃)
- 🔥 **可燃ネット + 竹棚** は現実と同じく **延焼する**
- 💧 **水中設置 (Waterlogged) 対応**

### 操作方法 ─ ここが大事!

足場ブロックの操作は**慣れるまでクセが強い**ので、しっかり読んでおいてください。

| アクション | 結果 |
|---|---|
| **普通の右クリック** | 隣のマスに 1 本目の竹を新規設置 |
| **シフト + 右クリック** | クリックしたブロックの**その面の方向に竹を 1 本追加** |
| シフト + 右クリックを連打 | 1 → 2 → 3 → 4 → **1 にループ** |

**クリックする面で、どの軸方向に竹が追加されるかが決まります:**

| クリックした面 | 増える軸 | 見た目の効果 |
|---|---|---|
| **上面 / 下面** | Y軸 (縦) | 縦方向の竹(スタンダード)が 1 本増える |
| **北面 / 南面** | Z軸 (南北) | 南北方向に走る横棒(レジャー)が 1 本増える |
| **東面 / 西面** | X軸 (東西) | 東西方向に走る横棒が 1 本増える |

つまり「**生やしたい方向の面**」を **シフト + 右クリック** すれば OK。

### 例

1. 地面にシフトなしで右クリック → 縦の竹が 1 本立つ (`count_y = 1`)
2. その竹の **上面** をシフト + 右クリック → 2 本目の縦竹が同じ 1 ブロック内に追加 (`count_y = 2`)
3. さらに 2 回 → `count_y = 4`(最大)
4. もう一回シフト + 右クリック → ループして `count_y = 1` に戻る
5. その竹の **南面** をシフト + 右クリック → Z 軸方向の横竹が 1 本増える (`count_z = 1`)
6. ⇒ 結果: 1 ブロックの中に縦竹 1 本 + 横竹 1 本 が共存

### 同梱ブロック

**足場 (Pole 系)**

| ブロック | HK 名 | 直径 | 役割 |
|---|---|---|---|
| `bamboo_scaffold` | **毛竹 (Mao Jue)** | ≥75mm | 主柱・荷重支持の縦スタンダード |
| `kao_bamboo_scaffold` | **篙竹 (Kao Jue)** | ≥40mm | 横材(ledger)・中間補強 |
| `iron_scaffold` | (金属足場) | — | 現代代替材、香港政府推奨 |

**筋交い (Brace)**

| ブロック | HK 名 | 説明 |
|---|---|---|
| `bamboo_brace` | **斜撑** | 45° の筋交い。NE-SW / NW-SE はプレイヤーの向きで自動切替 |

**Brace のスタビライザー効果**

竹筋交いには **当たり判定がない代わりに、長距離アンカー** の機能が付いています。

- **Pole は半径 3 ブロック以内に Brace があれば落下しない**
- **Brace は半径 3 ブロック以内に Pole があれば落下しない**
- → 両者の握手範囲を組み合わせると、最大 **6 ブロック離れた 2 本の足場の間に 1 個 Brace** を置くだけで橋桁が組める

**養生ネット (Safety Mesh)** — 6 色 (緑・黒・白・青・灰・銀)、それぞれに **4 種類** のバリエーション:

| サフィックス | 防燃 | 可燃 | 用途 |
|---|---|---|---|
| `*_safety_mesh` (デフォルト) | ✅ | ❌ | 標準。竹棚に貼っても延焼しない |
| `*_safety_mesh_flammable` | ❌ | ✅ | **延焼する** ─ 史実再現用 |
| `*_safety_mesh_heatproof` | ✅✅ | ❌ | 耐火強化 |
| `*_safety_mesh_flammable_heatproof` | ❌ | ✅ | 可燃なのに耐火、矛盾系オブジェクト |

### クラフト

- **毛竹**: 竹 × 6 + 紐 × 1
- **篙竹**: 竹 × 3 + 紐 × 1
- **竹筋交い**: 篙竹 × 2(縦配置)
- **金属足場**: 鉄インゴット × 6
- **養生ネット**: ウール 1 + 染料 1 → 4 個

### 背景 ─ 2025 年 宏福苑火災

香港の建設文化と、**2025 年 11 月 26 日の宏福苑(Wang Fuk Court)火災** の教訓を Minecraft の中で形にしたい、というのが出発点です。香港大埔区の集合住宅で外壁修繕中に大規模火災発生、**168 名死亡**(1948 年以来最悪)。検体 20 のうち 7 が防燃テスト不合格でした。

このモッドで **`*_safety_mesh_flammable`** ネットを竹棚に貼って火を点けると、現実と同じく延焼します。歴史と教訓を意識した素材選びができるブロックパレットです。

### コンプライアントな組み合わせ

| 足場 | ネット | 結果 |
|---|---|---|
| 竹棚 | 防燃ネット(デフォルト) | ✅ 推奨 |
| **金属足場** | 防燃ネット | ✅✅ 政府推奨の最新基準 |
| 竹棚 | 可燃ネット | ⚠️ 延焼します(意図的な再現) |

### クレジット / ライセンス

- 開発: [hrmcngs](https://github.com/hrmcngs)
- フィードバック → [GitHub Issues](https://github.com/hrmcngs/ShelfMod/issues)
- **MIT License**

---

## 简体中文

一个重现香港**竹棚**(竹脚手架)与**安全网**(养生网)的 Minecraft 模组。每个方块最多容纳 **每个轴 4 根、共 12 根**竹子,可搭建出真实感的脚手架、桥梁与高塔结构。

### 主要特色

- 🎋 单方块最多 **12 根**竹子(X / Y / Z 三轴各 4 根)
- 🪜 同样操作支持**现代金属脚手架**
- 🧱 **竹斜撑**作为 3 格半径的"稳定锚" —— 放在两根立柱之间即可搭桥
- 🟩 安全网 **6 色 × 4 种阻燃属性**
- 🔥 易燃网 + 竹棚会真的延烧(还原现实)
- 💧 水中安装(waterlogged)支持

### 操作方法 ─ 这部分一定要看!

| 操作 | 结果 |
|---|---|
| **普通右键** | 在点击面相邻的空格新建一根竹子 |
| **Shift + 右键已有竹方块** | 在**点击面所在的轴方向**追加一根竹子 |
| 重复 Shift + 右键 | 数量循环 1 → 2 → 3 → 4 → **回到 1** |

**点击的面决定增加的方向:**

| 点击面 | 轴 | 效果 |
|---|---|---|
| **上面 / 下面** | Y 轴(垂直) | 块内**垂直立柱** +1 |
| **北面 / 南面** | Z 轴(南北) | 块内**南北横杆** +1 |
| **东面 / 西面** | X 轴(东西) | 块内**东西横杆** +1 |

也就是说,"想让竹子从哪个方向长出来,就 shift + 右键那个面"。

### 操作示例

1. 右键地面 → 1 根立柱 (`count_y = 1`)
2. Shift + 右键它的**上面** → 同一方块内变成 2 根立柱 (`count_y = 2`)
3. 再来 2 次 → `count_y = 4`(满)
4. 再 Shift + 右键 → 循环回 `count_y = 1`
5. Shift + 右键**南面** → 同一方块内增加一根 Z 轴横杆 (`count_z = 1`)
6. 结果:1 个方块内立柱和横杆共存

### 方块清单

**脚手架立柱**

| 方块 | 香港名 | 直径 | 用途 |
|---|---|---|---|
| `bamboo_scaffold` | **毛竹** | ≥75mm | 承重主柱 |
| `kao_bamboo_scaffold` | **篙竹** | ≥40mm | 横材 / 中间补强 |
| `iron_scaffold` | (金属) | — | 现代替代材 |

**斜撑**

| 方块 | 香港名 | 说明 |
|---|---|---|
| `bamboo_brace` | **斜撑** | 45° 斜撑;NE-SW / NW-SE 由玩家朝向自动选择 |

**斜撑的稳定效果**

竹斜撑没有碰撞体积,但作为**长距离锚点**工作:

- **立柱在 3 格(直线方向)内有斜撑时不会掉落**
- **斜撑在 3 格(直线方向)内有立柱时不会掉落**
- → 双方握手:在两根立柱之间放 **1 个斜撑**,即可在最多 **6 格空隙**上搭桥

**安全网** — 6 色(绿 / 黑 / 白 / 蓝 / 银 / 灰),每色 4 种变体:

| 后缀 | 阻燃 | 易燃 | 用途 |
|---|---|---|---|
| `*_safety_mesh`(默认) | ✅ | ❌ | 标准。装在竹棚上也不会延烧 |
| `*_safety_mesh_flammable` | ❌ | ✅ | **延烧** ─ 还原现实事故 |
| `*_safety_mesh_heatproof` | ✅✅ | ❌ | 加强阻燃 |
| `*_safety_mesh_flammable_heatproof` | ❌ | ✅ | 易燃但耐热 |

### 合成

- **毛竹**: 竹 × 6 + 线 × 1
- **篙竹**: 竹 × 3 + 线 × 1
- **竹斜撑**: 篙竹 × 2(垂直摆放)
- **金属脚手架**: 铁锭 × 6
- **安全网**: 羊毛 × 1 + 染料 × 1 → 4 块

### 背景 ─ 2025 年 宏福苑火灾

灵感源于香港传统竹棚工艺,也参考了 **2025 年 11 月 26 日大埔区宏福苑火灾**:易燃安全网与竹棚组合导致 **168 人遇难**,是自 1948 年以来最严重的住宅火灾,送检的 20 个样本中有 7 个未通过阻燃测试。

本模组的 `_flammable` 变体如实重现这一物理特性,材料选择因此具有真实意义。

### 推荐搭配

| 脚手架 | 安全网 | 结果 |
|---|---|---|
| 竹棚 | 默认(阻燃) | ✅ 推荐 |
| **金属** | 默认(阻燃) | ✅✅ 最新港府指南标准 |
| 竹棚 | 易燃网 | ⚠️ 会延烧(刻意还原) |

### 致谢 / 协议

- 作者: [hrmcngs](https://github.com/hrmcngs)
- 反馈 → [GitHub Issues](https://github.com/hrmcngs/ShelfMod/issues)
- **MIT 协议**
