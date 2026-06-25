# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

## ⚠️ 最優先・セッション開始時にまず実行すること

このプロジェクトはユーザーとDiscord経由でも連絡を取り合う運用（詳細は `docs/handover.md` 参照）。前回PJ（`\\YukiYoshiNAS\Shogiban-kaiseki-appli`）で、Discordツール（`reply`/`fetch_messages`等、`plugin:discord:discord`提供）が読み込まれない事象が繰り返し発生し、原因と対処法が確定済み。**毎セッション開始時、本格的な作業に入る前に必ず実行**すること（再調査は不要）。

**2026-06-25確定：効いているのは「新しいターミナルセッションで始める」こと自体であり、bun warmupコマンドは必須ではなかった。** 前セッションで warmup を実行してもDiscordツールが読み込まれず、ユーザーにターミナル再起動を依頼した。その後ユーザーが新セッションを開始したところ、**warmupコマンドを一切実行せずに、最初のToolSearch呼び出しで`mcp__plugin_discord_discord__reply`が即座に読み込まれ、`reply`が初回から正常動作した**（このセッションで実証）。つまり再現性のある対処法は「新セッションを開始する」ことそのものであり、bun warmupは保険として残すが必須ではない。

**実行手順（2026-06-25版）：**
1. ToolSearchで`select:mcp__plugin_discord_discord__reply`等のDiscordツールが読み込まれているか確認する。
2. 見つかれば、warmupコマンドは不要。そのままDiscordでの会話を進めてよい（2026-06-25に実証済みのパス）。
3. 見つからない場合のみ、保険として次をBashで実行してから手順1を再試行する（依存モジュールの初回アクセス遅延対策、MCP起動タイムアウト30秒対策）：
   ```
   cd ~/.claude/plugins/cache/claude-plugins-official/discord/*/ && bun install --no-summary
   bun -e "await import('discord.js'); await import('@modelcontextprotocol/sdk/server/index.js');"
   ```
4. それでも見つからなければ、**`/reload-plugins`では直らない**——ユーザーに「このターミナルセッションを終了して新しいセッションを開始してください」と伝える。新セッションで手順1から再実行する（2026-06-25実績：この「新セッション開始」だけで解決した）。

詳細な調査経緯は前回PJの `\\YukiYoshiNAS\Shogiban-kaiseki-appli\CLAUDE.md` 冒頭を参照（Windows Defenderのリアルタイムスキャンが初回ファイルアクセス時に介入している可能性が最有力、未確定。ただし2026-06-25の実績はwarmupなしで成功しているため、初回アクセス遅延説そのものが今回のケースの主因だったかは不確か）。

## What this project is

スマホデトックスを実現するスマホアプリ。自宅にいるとき（Wi-Fi/GPSでおおよそ判定）に、指定したサイト・アプリ（例：YouTubeアプリ、YouTubeサイト）へのアクセスをブロックする。アプリ上でON/OFFを即切替できる（一時的に必要になった場合に対応）。

開発の進め方・環境は前回PJ `\\YukiYoshiNAS\Shogiban-kaiseki-appli`（将棋盤解析アプリ、Android native + FastAPI）を参考にする。開発のやり取りは前回PJ同様、基本的にDiscordで実施。

## Why this project exists

課題（`01 初回開発/00 企画/課題.txt`より）：
- スマホの使い過ぎを抑止したい
- ショート動画など生産性が低いものへのアクセスを制限したい

## Requirements（`01 初回開発/01 要件/めも.txt`より、2026-06-25時点）

[概要]
- スマホデトックスを実現するスマホアプリを開発
- 環境は前回PJ（Shogiban-kaiseki-appli）を参考
- 開発のやり取りはDiscordで実施（前回PJ同様）

[具体要件]
- 自宅にいるときはスマホ指定のサイト・アプリが見れないようにする
  - 在宅判定はWi-Fiまたは GPS（おおよそでいい、厳密な判定は不要）
  - 対象サイト・アプリはアプリ上で指定・管理できる（例：YouTubeアプリ、YouTubeサイトを制限）
  - 対象サイトはブラウザによらずアクセス不可にできるとよい（must要件ではなく「よい」表現——優先度は要確認）
- アプリでON/OFFを切り替えれば、ブロックする/しないをすぐに切り替えられる（一時的に必要になることがあるため）

## 確定した前提（2026-06-25）

- **対象プラットフォーム：Androidのみ**（iOSは対象外。Screen Time APIの制約を回避できる）
- **配布スコープ：開発者本人の個人利用のみ**（ストア配布は想定しない。サイドロード前提で実装手段の幅が広い）
- **サイトブロックの実現方式：案C確定**（AccessibilityServiceで主要ブラウザを複数まとめてカバー、VPN不要。詳細比較は`02 要件定義/MVPメリデメ比較_ブロック方式.md`）。
  - **対象アプリ（初期スコープ）：Brave、Chrome、LINE**（LINEはアプリ内ブラウザのURL読み取りが対象。3つともAccessibilityServiceでUIツリーを読んでURL判定する対象として技術的には同列に扱える）。
  - 既知の弱点：AccessibilityServiceはAndroid側のバッテリー最適化・未使用アプリの権限自動剥奪等でOSにOFFにされることがある（仕組み自体の制約、案不問）。緩和策（OFFを検知して再有効化を促す通知等）の実装が必要。

## 未確定・要確認事項（設計着手前に解消が必要）

- **ブロック解除の摩擦設計**：ON/OFF切替を即時にするか、衝動的な解除を抑止する遅延・確認ステップを入れるか。未回答——次に確認すべき項目。
- 対象デバイス：開発者本人の端末1台を想定か、複数端末対応が必要か（個人利用前提なので優先度は低いが未確認）。

## Current status (2026-06-25)

- 要件定義の初期段階。プラットフォーム・配布スコープ・サイトブロック実現方式（案C、対象Brave/Chrome/LINE）は確定。残り1点（解除の摩擦設計）が未回答。
- git: 前セッションで初期化、remote設定・初回push済み（`https://github.com/yukiyoshi1992/smartphone_detox.git`）。
- **コミュニケーション手段をDiscordに移行完了**：新セッションでDiscord接続に成功（上記「最優先」セクション参照）。以降のやり取りはDiscord経由（chat_id `1517480345874731078`）。
- 設計・実装はまだ未着手。
