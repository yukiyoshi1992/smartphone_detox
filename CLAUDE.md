# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

## ⚠️ 最優先・セッション開始時にまず実行すること

このプロジェクトはユーザーとDiscord経由でも連絡を取り合う運用（詳細は `docs/handover.md` 参照）。前回PJ（`\\YukiYoshiNAS\Shogiban-kaiseki-appli`）で、Discordツール（`reply`/`fetch_messages`等、`plugin:discord:discord`提供）が読み込まれない事象が繰り返し発生し、原因と対処法が確定済み。**毎セッション開始時、本格的な作業に入る前に必ず実行**すること（再調査は不要）。

1. ToolSearchで`reply`/`fetch_messages`等のDiscordツールが読み込まれているか確認する。
2. **読み込まれているかどうかに関わらず**、毎回必ずBashで次を実行する（依存モジュールの初回アクセス遅延を先払いし、MCP起動タイムアウト(30秒)を回避するため）：
   ```
   cd ~/.claude/plugins/cache/claude-plugins-official/discord/*/ && bun install --no-summary
   bun -e "await import('discord.js'); await import('@modelcontextprotocol/sdk/server/index.js');"
   ```
3. 手順1でツールが見つかっていれば完了。
4. 見つからなければ、ウォームアップ後でも読み込まれないことがある（2026-06-25実績：ウォームアップしても今回のセッションでは読み込まれず）。**`/reload-plugins`では直らない**——ユーザーに「このターミナルセッションを終了して新しいセッションを開始してください」と伝える。新セッションで手順1〜2を再実行する。

詳細な調査経緯は前回PJの `\\YukiYoshiNAS\Shogiban-kaiseki-appli\CLAUDE.md` 冒頭を参照（Windows Defenderのリアルタイムスキャンが初回ファイルアクセス時に介入している可能性が最有力、未確定）。

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

## 未確定・要確認事項（設計着手前に解消が必要）

- **「ブラウザによらずアクセス不可」の実現範囲**：VPN/DNSベースで最初から全ブラウザ・全アプリ共通でブロックするか、まず主要ブラウザ（Chromeなど）のみAccessibilityServiceで対応して後で拡張するか。ユーザーより「MVPのメリデメを整理しないと判断できない」とのコメントあり——**次の作業はこのメリデメ比較表を作ること**（実装コスト・保守性・即効性などの軸で比較）。
- **ブロック解除の摩擦設計**：ON/OFF切替を即時にするか、衝動的な解除を抑止する遅延・確認ステップを入れるか。上と同様、未回答。
- 対象デバイス：開発者本人の端末1台を想定か、複数端末対応が必要か（個人利用前提なので優先度は低いが未確認）。

## Current status (2026-06-25)

- 要件定義の初期段階。プラットフォーム・配布スコープは確定。残り2点（サイト/アプリブロックの実現範囲、解除の摩擦設計）はMVPのメリット・デメリット比較が先に必要とのことで保留中。
- git: 本セッションで初期化、remote設定・初回push済み（`https://github.com/yukiyoshi1992/smartphone_detox.git`）。
- **コミュニケーション手段をDiscordに移行予定**：ユーザーが現在のターミナルセッションを終了し、以降はDiscord経由でやり取りする意向。次セッションは新しいターミナル起動後、まずDiscordツールの読み込み確認（上記「最優先」セクション）から始めること。
- 設計・実装はまだ未着手。
