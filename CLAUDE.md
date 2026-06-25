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

## ディレクトリ構成（2026-06-25確定）

今回の開発はあくまで「初回開発」スコープのため、企画・要件・設計・開発の作業はすべて
`01 初回開発/` 配下に置く（トップレベルに並列のフォルダを作らない）。
- `01 初回開発/00 企画/`：課題・企画メモ
- `01 初回開発/01 要件/`：要件メモ・要件関連の検討資料（MVPメリデメ比較表もここ）
- `01 初回開発/02 設計・開発/`：今後の設計・実装作業はここに置く（ユーザー指定）
- `01 初回開発/03 UAT/`：UATシナリオ（`UATシナリオ.md`）。実装が進むごとにケースを追加し、実施結果を更新する。

## 開発環境・git運用（2026-06-25、Android実装着手時に確定）

前回PJ（Shogiban-kaiseki-appli）と同じ理由——**NASの共有フォルダ上で直接Gradleビルドすると不安定になる**（`Permission denied`等が頻発した既知の問題）——により、Android Studioでのビルド・実行は**ローカルディスクのクローン**から行う。

- ローカルクローン：`C:\Users\yukiy\dev\smartphone_detox`（GitHubリモートから`git clone`済み）
- Androidプロジェクトの作成場所：そのクローン内の`01 初回開発/02 設計・開発/app`（リポジトリルートではなくサブフォルダに置く——前回PJの`03 設計・開発/01 Androidアプリ開発/`と同じ考え方）
- **2つの作業ツリーが存在する**：Claude（このNASパス上で直接編集）と、ユーザー（ローカルクローンでAndroid Studio操作）。GitHub remote経由で同期する：
  - Claudeが作業する前：必要に応じてユーザー側の最新コミットをpullしているか確認（通常はClaudeが先行して作業することが多い）。
  - ユーザーがAndroid Studio側で作業する前：**必ず`git pull`**してClaude側の変更を取り込む。
  - ユーザーがコミットしたら`git push`し、Claudeは次の作業前にNASパス側で`git pull`して取り込む。
- **2026-06-25実績：ローカルクローンでも日本語パスのエラーが発生し、対処済み。** Android StudioでGradle Sync時に`Your project path contains non-ASCII characters`エラーが発生（プロジェクトパス`01 初回開発/02 設計・開発/app`に日本語フォルダ名が含まれるため）。ローカルクローンに切り替えても**NAS特有ではなく日本語パス自体が原因**なので発生する。対処：`app/gradle.properties`に`android.overridePathCheck=true`と`org.gradle.vfs.watch=false`を追加して解消済み（前回PJと同じ対処、commit済み）。今後似たエラーが出た場合はこの2行が入っているか確認すること。

## What this project is

スマホデトックスを実現するスマホアプリ。自宅にいるとき（Wi-Fi/GPSでおおよそ判定）に、指定したサイト・アプリ（例：YouTubeアプリ、YouTubeサイト）へのアクセスをブロックする。アプリ上でON/OFFを即切替できる（一時的に必要になった場合に対応）。

開発の進め方・環境は前回PJ `\\YukiYoshiNAS\Shogiban-kaiseki-appli`（将棋盤解析アプリ、Android native + FastAPI）を参考にする。開発のやり取りは前回PJ同様、基本的にDiscordで実施。

## Why this project exists

課題（`01 初回開発/00 企画/課題.txt`より）：
- スマホの使い過ぎを抑止したい
- ショート動画など生産性が低いものへのアクセスを制限したい

## Requirements

**確定版の要件定義は `01 初回開発/01 要件/要件定義.md` を参照すること**（Mermaidのアーキテクチャ図つき、最新の正本）。生の検討過程・経緯は`01 初回開発/01 要件/めも.txt`、ブロック方式の比較検討は`01 初回開発/01 要件/MVPメリデメ比較_ブロック方式.md`、技術方針の詳細は`01 初回開発/02 設計・開発/アーキテクチャ検討.md`参照。

要件定義.mdの要旨（2026-06-25時点で全項目確定）：
- Androidのみ／個人利用・サイドロード前提／サーバーなし単一アプリ構成
- 在宅時のサイト・アプリブロック：在宅判定はWi-Fi SSID検知のみ（GPSは将来拡張候補）。サイトはBrave/ChromeをAccessibilityServiceでURL検知、アプリ（YouTube等）はフォアグラウンド検知。LINEはアプリ単位ブロックのみ（URL制御なし）
- ON/OFF切替は即時（摩擦なし）
- 時間帯指定ルール：在宅判定とAND条件で組み合わせ可能、曜日/平日/休日祝日に対応、祝日データは公開APIから自動取得
- 通知：時間帯でマナーモード（バイブ有効）に自動切替

## 未確定・要確認事項（設計着手前に解消が必要、優先度低）

- 対象デバイス：開発者本人の端末1台を想定か、複数端末対応が必要か（個人利用前提なので優先度は低いが未確認）。

## Current status (2026-06-25)

- **要件定義・技術方針ともに全項目確定**。`01 初回開発/01 要件/要件定義.md`（確定版まとめ＋Mermaidアーキテクチャ図）を作成済み。
- **実装フェーズ進行中**：Androidプロジェクト作成済み（`01 初回開発/02 設計・開発/app`、パッケージ名`com.yukiyoshi.smphdetox`、`minSdk=34`/`compileSdk・targetSdk=37`）。日本語パスのGradleエラー・compileSdk不足エラーは解消済み（上記「開発環境・git運用」参照）。実機での起動確認済み。
- **実装順序①完了・実機検証済み**：`BlockAccessibilityService`（`block`パッケージ）を実装。フォアグラウンドアプリ検知でブロック対象（現状YouTubeのみハードコード）ならホーム画面に戻す。実機でYouTubeアプリを開くと即座にホームに戻ることを確認済み。
- **実装順序②完了・実機検証済み**：`home`パッケージにWi-Fi在宅判定を実装（`HomeWifiSettings`で複数SSID設定の保存、`HomeWifiStatus`で現在のWi-Fi接続SSIDとの一致判定、機種依存対策として`WifiManager.connectionInfo`へのフォールバックあり）。MainActivityに設定UI・「今接続中のWi-Fiを登録」ボタン・権限リクエスト・状態確認ボタンを追加。実機で在宅/非在宅の判定が正しく動作することを確認済み。
- **実装順序③実装済み**：`rule`パッケージにルールエンジンを実装（`TimeRule`：曜日・開始終了時刻・在宅条件を持つルール、`RuleEngine`：日付をまたぐ時間帯にも対応した有効判定、`RuleSettings`：SharedPreferencesへの保存、`RuleSection`：ルール追加・削除・有効確認のUI、時刻入力はMaterial3 TimePicker）。
- **進め方変更（2026-06-25、ユーザー指示）**：1機能実装→即実機確認、を繰り返す進め方から、**④⑤⑥を都度のユーザー確認を挟まずまとめて実装し、最後にまとめてモンキーテストする**進め方に変更。UI整理（Wi-Fi/GPS設定画面と管理画面の分割等）もモンキーテスト後にまとめて対応する。
- **実装順序④完了**：`BlockAccessibilityService`にBrave/Chromeのアドレスバー検知を追加（`com.android.chrome:id/url_bar`等のリソースIDを読み取り、ブロック対象ドメインに該当すればホームに戻す）。ブロック対象アプリ・サイトはハードコードを廃止し、`BlockSettings`（SharedPreferences）＋`BlockSettingsSection`（UI、インストール済みアプリから選択して追加）で管理するように変更。
- **実装順序⑤完了**：`notification`パッケージに通知マナーモード自動切替を実装（`RingerModeController`：`ACCESS_NOTIFICATION_POLICY`権限チェック＋リンガーモード切替、`NotificationRuleSection`：通知専用の時間帯ルールUI、ブロック用ルールと同じ`TimeRuleForm`/`RuleEngine`を再利用し`RuleSettings`の保存先のみ分離）。
- **実装順序⑥完了**：`holiday`パッケージに祝日API連携を実装（`HolidayRepository`：holidays-jp公開APIから祝日一覧を取得しSharedPreferencesにキャッシュ、取得失敗時は既存キャッシュを使い続ける）。`TimeRule.includeHolidays`を追加し、ONにすると曜日指定に関わらず祝日ならルールが適用される。ブロック用・通知用の両ルール画面表示時に`refreshIfStale()`で自動更新。
- **①～⑥すべて実装済み**。モンキーテスト前にユーザーから「どう使えばいいかわからないので画面を分けてほしい」との要望があり、**Navigation Composeを導入して3画面構成に再編**：
  - TOP画面（`ui/TopScreen.kt`）：設定・ルール管理への遷移ボタン、ブロック機能全体ON/OFFスイッチ（`block/AppMasterSettings.kt`、BlockAccessibilityServiceがOFF中は何もしない）、在宅状況・アクセシビリティ許可状況（`block/AccessibilityStatus.kt`）の表示
  - 設定画面（`ui/SettingsScreen.kt`）：アクセシビリティ設定、Wi-Fi登録・在宅確認（旧MainActivity直書きの内容を移動）
  - ルール管理画面（`ui/RuleManagementScreen.kt`）：タブで「ブロック」（時間帯ルール＋ブロック対象アプリ・サイト）／「通知」（通知ルール）を切替。各ルール一覧に有効/無効の`Switch`を追加（`TimeRule.enabled`、削除せず一時停止できる）
  - `ui/AppNavHost.kt`でルーティング、`MainActivity.kt`はNavHostを呼ぶだけに簡素化
- **実機での一括ビルド・モンキーテストはこれから**（UATシナリオの該当ケースは追加済みだが「結果：未実施」が多数残っている状態）。
- git: 前セッションで初期化、remote設定・初回push済み（`https://github.com/yukiyoshi1992/smartphone_detox.git`）。
- **コミュニケーション手段をDiscordに移行完了**：新セッションでDiscord接続に成功（上記「最優先」セクション参照）。以降のやり取りはDiscord経由（chat_id `1517480345874731078`）。
