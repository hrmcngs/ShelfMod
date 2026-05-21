# ShelfMod — Build & Test-play Guide

3 ローダー(Forge 1.20.1 / NeoForge 1.21.4 / Fabric 1.21.4)の JAR ビルドと dev 環境でのテストプレイ方法。

---

## 背景: 香港の竹棚と養生ネット

このモッドは香港の建設現場で使われる伝統的な「**竹棚 (Bamboo Scaffolding)**」と「**養生ネット (Safety Mesh)**」を Minecraft で再現することを目的にしています。

### 竹の使い分け (Hong Kong Building Authority 指針)
| ブロック | 香港名 | 直径 | 役割 |
|---|---|---|---|
| `bamboo_scaffold` | **毛竹 (Mao Jue)** | ≥75mm 厚 ≥10mm | 主柱・荷重支持の垂直スタンダード |
| `kao_bamboo_scaffold` | **篙竹 (Kao Jue)** | ≥40mm | 横材 (ledger)・中間補強・水平材 |
| `bamboo_brace` | **斜撑** | Kao 級 | 45° 筋交い (NE-SW / NW-SE) |
| `iron_scaffold` | (金属足場) | — | 政府推奨の現代代替材 |

実際の HK 仕様は 3〜5年生・3ヶ月以上乾燥した竹を使い、**双列足場(double-row)** で内列 200mm + 外列 1000mm を **transom(横担)** で繋ぎ、外列スタンダードは最大 1.3m 間隔、ledger 垂直間隔 600〜750mm。**ナイロン樹脂のストラップ**で 6回巻いて捻じ込む結束(金属ボルトなし)。

### 養生ネット
養生ネットは **GB 5725-2009 / BS 5867-2:2008 / NFPA 701:2019** のいずれかに適合する防燃 (Flame-retardant) が現在の香港法規では必須。緑が標準色、黒/オレンジも使われる。本モッドの 6 色 × 防燃/可燃の区分はこの法規をそのまま反映しています。

### 2025年 王福苑(Wang Fuk Court)火災
2025年11月26日、香港大埔区の **宏福苑** 集合住宅団地で外壁修繕中に大規模火災発生。**168名死亡**(1948年以来の最悪)。竹足場・養生ネット・窓の発泡断熱パネルが組み合わさり 8棟中7棟が延焼。検体 20 のうち 7 が防燃テスト不合格。香港政府は事件後、**「竹 → 金属足場への段階的廃止」を加速**。

このモッドで **可燃ネット (`*_flammable`)** を **竹足場** に貼ると現実と同じく延焼するように作ってあります。コンプライアントな組み合わせは:
- 防燃ネット (`*_safety_mesh`, デフォルト名) + 金属足場 (`iron_scaffold`)
- または 防燃ネット + 竹足場

歴史と教訓を意識した素材選びができるブロックパレットになっています。

---

## TL;DR

```bash
./build.sh             # 全ローダーをビルド → dist/<loader>-shelfmod-<ver>.jar
./run.sh forge         # Forge dev クライアントを起動
```

Windows なら `.ps1` 版が同じ位置にあります。

---

## 必要環境

| ローダー | JDK | gradle | OS |
|---|---|---|---|
| forge-1.20.1 | **17** | 8.1.1 (wrapper) | mac / Linux / Windows |
| neoforge-1.21.4 | **21** | 8.10 (wrapper) | mac / Linux / Windows |
| fabric-1.21.4 | **21** | 8.12 (wrapper) | mac / Linux / Windows |

- macOS は `brew install openjdk@17 openjdk@21`
- Windows は `winget install EclipseAdoptium.Temurin.17.JDK` / `Temurin.21.JDK`
- gradle は wrapper 同梱なので**インストール不要**
- 環境変数で明示したい場合は `JAVA17_HOME` / `JAVA21_HOME` をセット(無くても自動検出します)

---

## ファイル構成

```
ShelfMod/
├── build.sh       build.ps1       ビルド本体 (mac/Linux / Windows)
├── run.sh         run.ps1         テストプレイ起動 (runClient / runServer)
├── BUILD.md                       ← このファイル
├── dist/                          ビルド成果物の出力先
├── forge-1.20.1/
├── neoforge-1.21.4/
└── fabric-1.21.4/
```

---

## ビルド (`build.sh` / `build.ps1`)

### 基本

```bash
# macOS / Linux
./build.sh                          # 全ローダーをビルド
./build.sh forge                    # Forge のみ
./build.sh forge fabric             # 2つ並べて指定
./build.sh clean                    # 全 clean + dist/ 削除
```

```powershell
# Windows
.\build.ps1                         # 全ローダーをビルド
.\build.ps1 forge                   # Forge のみ
.\build.ps1 forge fabric            # 2つ並べて指定
.\build.ps1 clean                   # 全 clean + dist/ 削除
```

### フラグ

| `build.sh` | `build.ps1` | 役割 |
|---|---|---|
| `-o`, `--offline` | `-Offline`, `-o` | gradle に `--offline` を渡す(初回ビルド後はネット不要) |
| `-v`, `--verbose` | `-VerboseOutput`, `-v` | 進捗バーを使わず gradle の生ログを流す |
| `-c`, `--clean` | `-Clean`, `-c` | ビルド前に `clean` を実行 |
| `-h`, `--help` | — | ヘルプ表示 |

### 進捗バー

デフォルトでは 1 行更新型の進捗バーが出ます。

```
[forge]    [#############---------------]  41% :reobfJar
[neoforge] [##############--------------]  47% [NFRT] decompile
[fabric]   [#####################-------]  73% :remapJar
```

- `> Task :foo` の出現で 1 step 進む
- NeoForge の重い NFRT 内部処理(`Started working on decompile` など)も拾う
- 未実行タスク(`UP-TO-DATE` / `NO-SOURCE` / `SKIPPED`)はカウントしない
- 最後の `BUILD SUCCESSFUL` で 100% に揃える
- エラー時は進捗バー終了後にエラー全文を表示

### オフラインビルド

```bash
./build.sh --offline             # mac/Linux
.\build.ps1 -Offline             # Windows
```

- **初回ビルドはオンライン必須**(Forge MDK / NeoForge MDK / Fabric Loom + Yarn を DL)
- 一度通れば `~/.gradle/caches/` と各プロジェクトの `.gradle/` にキャッシュされ、それ以降は `--offline` でネット遮断状態でもビルド可能
- キャッシュは `./build.sh clean` で破壊されないので安心

### ビルド成果物

`dist/` 配下に `<loader>-shelfmod-<version>.jar` 形式で集約されます。

```
dist/
├── forge-shelfmod-0.1.0.jar
├── neoforge-shelfmod-0.1.0.jar
└── fabric-shelfmod-0.1.0.jar
```

ファイル名は `gradle.properties` の `mod_version` に追従。

---

## テストプレイ (`run.sh` / `run.ps1`)

dev 用 Minecraft クライアント(またはサーバ)を、現在のソースをロードした状態で起動します。

```bash
./run.sh forge                      # Forge dev クライアント
./run.sh forge server               # Forge dedicated server
./run.sh neoforge                   # NeoForge dev クライアント
./run.sh fabric                     # Fabric dev クライアント
./run.sh forge --offline            # オフラインで Forge クライアント
./run.sh fabric server -o -v        # サーバ・オフライン・詳細ログ
```

```powershell
.\run.ps1 forge                     # Forge クライアント
.\run.ps1 neoforge server           # NeoForge server
.\run.ps1 forge -Offline            # オフラインで Forge クライアント
.\run.ps1 fabric server -o -v       # サーバ・オフライン・詳細ログ
```

### フラグ

| `run.sh` | `run.ps1` | 役割 |
|---|---|---|
| `-o`, `--offline` | `-Offline`, `-o` | gradle に `--offline` を渡してネット遮断で起動 |
| `-v`, `--verbose` | `-VerboseOutput`, `-v` | gradle に `--info` を渡して詳細ログ |
| `-h`, `--help` | — | ヘルプ表示 |

- 初回起動はアセット DL + ソース展開で数分かかります(クライアントは Mojang アセット、Forge/NeoForge は decompile 済み MC ソース)
- 2回目以降は各プロジェクトの `run/` ディレクトリにワールドや設定が残るので高速
- **オフライン起動は最低1回のオンライン起動が成功している必要があります**(初回 DL したアセットを `<loader>/run/assets/`、依存を `~/.gradle/caches/` から読むため)
- 起動後は通常通り Minecraft が立ち上がり、`shelfmod` Mod が読み込まれた状態でプレイ可能
- 終了は MC を閉じるだけ

---

## JDK 検出ロジック(全 OS 共通)

`build.sh` / `run.sh` / `*.ps1` は以下の順で JDK を探します:

1. **環境変数** `JAVA17_HOME` / `JAVA21_HOME`
2. **macOS の登録 JDK**: `/usr/libexec/java_home -F -v <ver>`(`-F`=strict なので 21 を要求すれば 17 にフォールバックしない)
3. **macOS Homebrew keg-only**: `/opt/homebrew/opt/openjdk@<ver>/libexec/openjdk.jdk/Contents/Home`
4. **Linux**: `/usr/lib/jvm/java-<ver>-openjdk*`
5. **Windows**: `C:\Program Files\{Eclipse Adoptium,Microsoft,Java,BellSoft,Zulu}\jdk-<ver>*`

見つからないローダーは `SKIP` 表示で残りを続行します。

---

## トラブルシュート

### `JDK 21 not found`

macOS: `brew install openjdk@21`
Windows: `winget install EclipseAdoptium.Temurin.21.JDK`
Linux (Debian/Ubuntu): `sudo apt install openjdk-21-jdk`

または `export JAVA21_HOME=/path/to/jdk21`(Windows PowerShell は `$env:JAVA21_HOME=...`)

### NeoForge build で `Could not find net.neoforged:neoforge:21.4.x`

指定バージョンが NeoForge maven に存在しない。`neoforge-1.21.4/gradle.properties` の `neo_version` を <https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml> の最新版に更新。

### Fabric build で `Plugin ... requires at least Gradle X`

`fabric-1.21.4/gradle/wrapper/gradle-wrapper.properties` の `distributionUrl` を要求版に上げる(例: `gradle-8.12-bin.zip`)。

### `Yarn mappings` 関連の Could not find symbol

Fabric では Yarn マッピング名が Mojang/Forge と違うことがある(`ScaffoldingBlockItem` → `ScaffoldingItem` 等)。ビルドエラーのクラス名を [Linkie](https://linkie.shedaniel.dev/mappings) などで対応する Yarn 名に置換。

### 進捗バーが文字化けする / `\r` で行が増えていく

`build.sh --verbose` で gradle 生ログに切替可。Windows の旧 cmd は ANSI 非対応。Windows Terminal か PowerShell 7 推奨。

### `git push` が `non-fast-forward` で拒否される

初回 push 時にリモートが GitHub の "Initialize this repository" 経由で先にコミットを持っている場合:

```bash
git pull --rebase --allow-unrelated-histories origin main
# .gitignore でコンフリクトしたら両方マージして
git add .gitignore && git rebase --continue
git push -u origin main
```
