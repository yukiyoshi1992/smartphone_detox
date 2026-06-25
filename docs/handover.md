# handover.md

セッション間の引き継ぎ用ドキュメント。新しいセッションを始めるときは、まずこのファイルと `CLAUDE.md` を読むこと。

## 2026-06-25（初回セッション）

### このセッションでやったこと
- ユーザーから定常指示（CLAUDE.md/docs/handover.md運用、Discord優先確認、git commit&push運用）を受領。これは前回PJ（`Shogiban-kaiseki-appli`）と同じテンプレート文。
- Discord相互通信確認を試みた：
  - Botトークンは設定済み、アクセスポリシーは`pairing`、許可済みユーザー1名（snowflake `795820938221453314`）登録済み。
  - 前回PJのCLAUDE.mdに記載の既知対処（discord.js等の依存モジュールの事前import）を実施したが、**今回のセッションではDiscordツール（reply/fetch_messages等）が読み込まれなかった**。
  - 前回PJの対処法どおり、ユーザーに「このターミナルセッションを終了して新しいセッションを開始してください」と依頼済み。**→ 次セッション開始時、再度Discord確認から始めること。**
- プロジェクト内容を確認：
  - `01 初回開発/00 企画/課題.txt`、`01 初回開発/01 要件/めも.txt` に要件が記載されていることを確認（ユーザーが整備中だった）。
  - 内容：自宅にいるときに指定サイト・アプリをブロックするスマホデトックスアプリ。Wi-Fi/GPSで在宅判定、ON/OFF即切替。前回PJ（Shogiban-kaiseki-appli）を環境の参考にする。
- git環境構築：
  - このフォルダはgit未初期化だったため `git init`。
  - NASの所有権不一致（dubious ownership）エラーが出たため `git config --global --add safe.directory` で例外追加（解決済み、グローバル設定への追加なので次回以降は不要）。
  - リモートをユーザー指定の `https://github.com/yukiyoshi1992/smartphone_detox.git`（空リポジトリ確認済み）に設定。
  - `.gitignore`、`CLAUDE.md`、`docs/handover.md` を新規作成。

- git init + 初期ドキュメントのコミットをpush済み（リモート空リポジトリへの初回push、`main`ブランチ）。
- ユーザーに要件の未確定事項を確認：
  - **対象プラットフォーム → 「Androidのみ」で確定**
  - **配布スコープ → 「自分自身の個人利用のみ」で確定**
  - 「ブラウザによらずブロックする範囲」「ブロック解除の摩擦設計」の2点を質問したところ、**ユーザーから「MVPのメリデメを整理しないと回答が出せない」とストップがかかった**。先にこちらでメリデメ比較を整理してから再度確認する必要がある。
- ユーザーから「チャットをDiscordに移したいので、一度セッションを切る」との指示。このセッションはここで終了予定。

### 次にやること（次セッション最優先）
1. **Discord確認**：新セッション開始時、まずToolSearchでDiscordツールの有無を確認し、CLAUDE.md記載の手順を実施。今回のセッションでは依存ウォームアップ後もDiscordツールが読み込まれなかった点に注意（前回PJの既知の制約、新セッションなら直る可能性あり）。
2. **「ブラウザによらずブロックする範囲」のMVPメリデメ比較を整理する**（ユーザーへの提示待ち、現状未着手）：
   - 案A：VPN/DNSベースで最初から全ブラウザ・全アプリ共通ブロック（実装コスト高め、保守性は高い、最初から要件を完全に満たす）
   - 案B：まず主要ブラウザ（Chrome等）のみAccessibilityServiceでURL監視→ブロックし、MVPを早く出してから拡張（実装は軽いが、対象外ブラウザ/アプリは素通りする抜け道が残る）
   - 軸の例：実装コスト、保守性、抜け道の有無、開発速度（MVP到達までの時間）
3. 比較を提示した上で、ユーザーに「ブラウザによらずブロックする範囲」と「ブロック解除の摩擦設計」を再確認する。
4. 上記が決まったら、前回PJのアーキテクチャ検討の進め方（`02 要件定義/アーキテクチャ検討.md`的なドキュメントを作る）を参考に、技術方針を決めてユーザーに提示・確認を取る。

### 参考
- 前回PJ：`\\YukiYoshiNAS\Shogiban-kaiseki-appli`（将棋盤解析アプリ。Android(Kotlin+Compose+CameraX+Retrofit) + FastAPIサーバ。環境・Discord運用の参考元）
- リモートリポジトリ：`https://github.com/yukiyoshi1992/smartphone_detox.git`（push済み）

## 2026-06-25（2回目セッション、Discord移行後）

### このセッションでやったこと
- セッション開始時、CLAUDE.md/handover.mdを読んで前回引き継ぎ内容を確認。
- ユーザーから「Discordで会話したい」との指示を受け、`discord:configure`スキルで状態確認（トークン設定済み、`pairing`ポリシー、許可済みユーザー1名）。
- ユーザーから直接Discordでメッセージ（「聞こえる？」）が届き、`mcp__plugin_discord_discord__reply`で応答 → **接続成功を確認**。
- **重要な発見**：前セッションは`bun install`+`import`のwarmupコマンドを実行してもDiscordツールが読み込まれず、ユーザーに新セッション開始を依頼した。**今回のセッションではwarmupコマンドを一切実行せずに、最初のToolSearch呼び出しで即座にDiscordツールが読み込まれた**。つまり実際に効いていたのは「新しいターミナルセッションを開始する」こと自体であり、bun warmupは必須ではなかった可能性が高い。CLAUDE.mdの「最優先」セクションをこの実績に基づき更新済み（warmupは見つからない場合の保険に位置づけを変更）。
- ユーザーから定常指示を再確認：作業1つ終わるごとにCLAUDE.md/handover.md更新→git commit&push、セッションが切れても継続できるようにする運用。

- MVPメリデメ比較表を作成（`01 初回開発/01 要件/MVPメリデメ比較_ブロック方式.md`）：案A（VPN/DNSベース全ブロック）と案B（主要ブラウザのみAccessibilityService）を実装コスト・保守性・抜け道の有無・開発速度・副作用/UX・アプリブロックへの貢献の軸で比較。要旨：アプリブロック自体は案A/B共通で別途フォアグラウンド監視が必要（比較に影響しない）。論点は「サイトブロックの抜け道のなさ」と「MVP到達速度」のトレードオフ——案Aは堅牢だが実装重い、案Bは最速だが対応ブラウザ以外は素通り。
- 比較表をDiscordで提示。ユーザーから「以前使っていた『AppBlock』はVPN不要でどのブラウザでもブロックできていた」との情報提供を受け、**案C：AccessibilityServiceで主要ブラウザを複数まとめてカバー**（案Bの発展形）を比較表に追記。AccessibilityServiceがAndroid側都合（バッテリー最適化等）でOFFにされる既知の弱点も記録。
- **ユーザーが案Cで確定**。対象アプリ（初期スコープ）は**Brave、Chrome、LINE**（LINEはアプリ内ブラウザのURL読み取りが対象）。CLAUDE.mdの「確定した前提」に反映、`01 初回開発/01 要件/めも.txt`（ユーザーの元メモ）にも追記済み（ユーザーから明示的に依頼あり：「要件定義のメモは更新しておいてください」）。

- ユーザーから「設計・開発は`01 初回開発/02 設計・開発`配下で」との指示。CLAUDE.mdに反映済み。
- ユーザーから追加要求2件：
  1. **時間帯指定での利用抑制**：ご飯中・寝る前等の特定時間帯も抑制対象にしたい。時間帯では在宅時より広いアプリ・サイト（LINE、メール等含む）を制限したい。時間帯は複数設定可能にしたい。
  2. **通知の時間帯コントロール**：時間帯によって通知（マナーモード等）を自動切替したい（仕事時間・寝る時間に自動でマナーモード、のイメージ）。
  CLAUDE.md・めも.txtに反映済み。在宅判定との関係、通知制御の具体的方式（マナーモード vs Android標準DND）など詳細未確認——次回確認予定。
- 「ブロック解除の摩擦設計」の質問をDiscordで送ったところ、ユーザーから「メッセージが流れてしまったので質問の詳細を再度教えてほしい」との返信あり。A（即時切替）/B（摩擦あり：理由入力・遅延・確認ダイアログ等）の選択肢を改めて説明して再送。
- **ユーザー回答、要件確定**：
  - ブロック解除の摩擦設計：**A（即時切替）で確定**。摩擦は入れない。
  - 追加要求①：在宅判定とAND条件で組み合わせ可能にする（例：在宅時かつ22時以降）。時間帯指定は曜日・平日/休日・祝日など複数の指定方法に対応できるとよい、との追加詳細あり。
  - 追加要求②：通知制御は**マナーモードのみでOK**（Android標準DND/おやすみモードは不要）。ただしバイブレーションは動作させる（サイレントではない）。
  - CLAUDE.md・めも.txtに反映済み。**要件定義はほぼ確定**、残りは優先度の低い「対象デバイス（1台/複数台）」のみ。

- ユーザーから「技術方針の検討に進めてOK」との承認を受け、`01 初回開発/02 設計・開発/アーキテクチャ検討.md`を作成（commit&push予定）。内容：
  - サーバーなし単一Androidアプリ構成
  - 在宅判定：Wi-Fi（自宅SSID）OR ジオフェンス（GeofencingClient、半径100〜150m）
  - ルールエンジン：時間帯×在宅条件のAND、曜日/平日/休日祝日対応（祝日は静的JSON、年1回更新運用）、ルールごとに対象アプリ・サイトセットを別々に持てる
  - ブロック実行：単一AccessibilityServiceでフォアグラウンドアプリ検知＋Brave/Chromeのアドレスバー読み取り。**LINEはURL読み取りが困難な見込みのため、時間帯ルール該当時はアプリ全体をブロック対象にする扱いとした**（要件の「より広いアプリ」要求に合致する設計判断、ユーザー未確認）。
  - 通知：`AudioManager.setRingerMode(RINGER_MODE_VIBRATE)`、`ACCESS_NOTIFICATION_POLICY`権限が必要
  - バックグラウンド安定性：WorkManager/AlarmManagerでのルール評価、フォアグラウンドサービス化、バッテリー最適化除外
  - 必要権限一覧、ユーザー確認ポイント4点（サーバーなし構成でよいか／在宅判定OR条件でよいか／LINEの扱いでよいか／祝日データ運用でよいか）をドキュメント末尾に記載
- Discordで提示し、4点についてユーザーから回答：
  1. サーバーなし構成 → **確定**。
  2. 在宅判定（Wi-Fi/GPS）→「メリデメ整理して」と要望あり。Wi-Fi/GPSの比較表をドキュメントに追加（バッテリー消費・屋内精度・検知できないケース・権限・実装コストの軸）。**Wi-FiのみでMVPを作る方針を提案 → ユーザー承認、確定**。GPSは将来拡張候補として残すが今回は実装しない。
  3. LINEの扱い→当初「URL読み取りが困難なのでアプリ全体ブロック」と提案したところ、ユーザーから「在宅中の特定サイトブロックなら、LINEで見ることだけブロックしたい」という別の意図が示されたが、複雑になるためユーザー自身が撤回——**「LINEはサイト制御不要、アプリ単位の制御のみで確定」**。
  4. 祝日データ→「自動がいい、自動はハードルが高いか」との質問。公開の祝日API（`holidays-jp`系の静的JSON等）を定期取得してローカルキャッシュ＋フォールバックを持つ方式で実現可能と回答、**自動更新方式で確定**（自社サーバー不要の方針と矛盾しない、読み取り専用の公開データを叩くだけ）。
  - ドキュメント・CLAUDE.mdに反映、commit&push済み。
- ユーザーから在宅判定（Wi-FiのみMVP）方針への最終承認あり。**アーキテクチャ検討は全項目確定。**
- ユーザーから「このまま実装に進めてOK」との回答とあわせて、**「`01 初回開発/01 要件/`配下に、確定した要件をまとめた`要件定義.md`を作成し、Mermaidでアーキテクチャ（全体構成）図も入れてほしい」**との依頼あり。
  - `01 初回開発/01 要件/要件定義.md`を新規作成：背景・前提・機能要件（在宅ブロック／ON-OFF／時間帯ルール／通知制御）・非機能要件・Mermaid全体構成図（Androidアプリ内のUI/ルールエンジン/在宅判定/AccessibilityService/通知コントローラー/祝日クライアントの関係と、外部の公開祝日APIへの接続を図示）をまとめた。これが**要件の正本（最新確定版）**。
  - CLAUDE.mdのRequirements欄をこのファイルへのポインタに簡略化（めも.txtの内容をそのまま転記する形から、正本を参照する形に変更）。
  - commit&push予定。

- ユーザーから「設計・実装に進んでください、Android Studio起動します。プロジェクト作成回り・git連携周りの手順を案内して」との依頼。
  - 前回PJと同じ理由（NAS共有フォルダ上のGradleビルドが不安定）で、`C:\Users\yukiy\dev\smartphone_detox`にGitHubリモートからclone済み（Bashで実施）。
  - Android StudioでのNew Project手順（保存先：クローン内`01 初回開発/02 設計・開発/app`、パッケージ名候補`com.yukiyoshi.smphdetox`、Kotlin、Minimum SDKは端末のバージョンに合わせる）をDiscordで案内。
  - git運用ルールも案内：**ユーザーは作業前に必ず`git pull`**（Claude側のNASパス編集を取り込むため）、コミットしたら`push`してClaudeが次にpullする、という双方向の同期運用。NAS特有のビルドエラーが出た場合の対処（`gradle.properties`の`org.gradle.vfs.watch=false`等）も伝達済み。
  - CLAUDE.mdに「開発環境・git運用」セクションを新設して反映、commit&push済み。
  - ユーザーがAndroid Studioで「New Project」を実施、空のCompose Empty Activityプロジェクトが`01 初回開発/02 設計・開発/app`配下に作成された（パッケージ名`com.yukiyoshi.smphdetox`）。
  - ユーザーから「ゾウさんマークを押したらエラー出たよ」とのDiscordメッセージ＋エラーログ添付（`message.txt`、Gradle Sync時のエラー）。
  - **エラー内容**：`Your project path contains non-ASCII characters` — プロジェクトパス`01 初回開発/02 設計・開発/app`に日本語フォルダ名が含まれるため、Android Gradle Plugin（9.2.1）のASCII限定パスチェックに引っかかった。**ローカルクローンに切り替えても、NAS特有ではなくパス内の日本語自体が原因なので発生することが判明**（CLAUDE.mdの過去の記述を修正）。
  - **対処**：`01 初回開発/02 設計・開発/app/gradle.properties`に`android.overridePathCheck=true`（パスチェック無効化）と`org.gradle.vfs.watch=false`（前回PJの既知対処）を追加。ローカルクローン側（`C:\Users\yukiy\dev\smartphone_detox`）で編集・commit・push、その後NASパス側で`git pull`して同期。
  - CLAUDE.mdに実績を反映、commit&push済み。
  - ユーザーが再度Gradle Syncを実行 → **Sync自体は通った**。次にビルド（`assembleDebug`）でエラー：`checkDebugAarMetadata`が失敗、`androidx.core-ktx:1.19.0`等3つの依存ライブラリが「compileSdk 37以上が必要」と要求。プロジェクトは`compileSdk 36`（36.1）だったため不一致。
  - **対処**：`app/build.gradle.kts`の`compileSdk`を37に、`targetSdk`を36→37に変更（`minSdk=34`はそのまま）。ローカルクローン側で編集・commit・push、NASパス側でpullして同期済み。
  - ユーザーが再ビルド・実機実行 → **成功**。「Hello Android!!」画面が実機に表示されることを確認。最小構成のセットアップはこれで完了。
- ユーザーから「進めて」の承認を受け、**実装順序①（AccessibilityServiceでのフォアグラウンドアプリ検知＋ホーム遷移）を実装**：
  - `block/BlockAccessibilityService.kt`新規作成：`TYPE_WINDOW_STATE_CHANGED`イベントでフォアグラウンドのパッケージ名を検知し、ブロック対象（現状`com.google.android.youtube`のみハードコード、後でルールエンジンから渡す想定）に該当すれば`performGlobalAction(GLOBAL_ACTION_HOME)`でホームに戻す。
  - `res/xml/accessibility_service_config.xml`新規作成（サービス設定）、`strings.xml`にサービス説明文を追加。
  - `AndroidManifest.xml`にサービスを登録（`BIND_ACCESSIBILITY_SERVICE`権限、`accessibilityservice`アクションのintent-filter）。
  - `MainActivity.kt`を更新：「アクセシビリティ設定を開く」ボタンを追加（`Settings.ACTION_ACCESSIBILITY_SETTINGS`）——AccessibilityServiceはユーザーが手動で設定画面から有効化する必要があるため。
  - NASパス側で作業・commit&push済み。
  - ユーザーがビルド時にエラー報告：`accessibility_service_config.xml`の`android:accessibilityFlags`属性値`retrieveInteractiveWindows`が不正（正しいフラグ名は`flagRetrieveInteractiveWindows`、Claudeの記述ミス）。修正してcommit&push済み。
  - ユーザーが再度`git pull`・ビルド・実機確認 → **成功**。YouTubeアプリを開くと即座にホーム画面に戻ることを確認。**実装順序①完了。**
- ユーザーから「進めて」の承認を受け、**実装順序②（Wi-Fi在宅判定）を実装**：
  - `home/HomeWifiSettings.kt`新規作成：自宅Wi-FiのSSIDをSharedPreferencesに保存・読込。
  - `home/HomeWifiStatus.kt`新規作成：`ConnectivityManager`/`NetworkCapabilities`から現在接続中のWi-FiのSSIDを取得し（`WifiInfo`経由）、登録した自宅SSIDと一致するか判定する`isHomeWifiConnected()`。
  - `AndroidManifest.xml`に`ACCESS_WIFI_STATE`・`ACCESS_FINE_LOCATION`権限を追加（SSID取得には位置情報権限が必要なAndroidの仕様）。
  - `MainActivity.kt`を更新：自宅SSID入力欄＋保存ボタン、位置情報権限のランタイムリクエストボタン、「在宅状態を確認」ボタンと状態表示テキストを追加。
  - NASパス側で作業・commit&push済み。
  - ユーザーから「自宅のWi-FiはAとGで二つ登録したい」との要望。複数SSID対応に変更：`HomeWifiSettings`を単一SSIDから`Set<String>`に変更、`MainActivity`に追加・削除UIを実装。commit&push済み。
  - ユーザーが`git pull`・ビルド後、「在宅状態を確認」ボタンを押すとアプリがクラッシュ。ユーザーがlogcatファイル（`01 初回開発/02 設計・開発/test/logcat.txt`）を共有してくれた。
  - **クラッシュ原因**：`java.lang.SecurityException: ConnectivityService: ... does not have android.permission.ACCESS_NETWORK_STATE.` — `ConnectivityManager.getActiveNetwork()`の呼び出しに必要な`ACCESS_NETWORK_STATE`権限をManifestへ宣言し忘れていた（Claudeの実装漏れ）。
  - **対処**：`AndroidManifest.xml`に`ACCESS_NETWORK_STATE`権限を追加。commit&push済み。
  - ユーザーが再度確認 → クラッシュはしなくなったが、**実際に自宅Wi-Fi（`YandY-A`）接続中にも関わらず「非在宅」と判定される事象**を報告。共有された`logcat2.txt`は古いクラッシュのスタックトレースが残っていた可能性があり（logcatは履歴が溜まる）、決定的な手がかりではなかった。**原因未確定**——以下3点をユーザーに確認依頼中：(1) アプリに登録したSSID文字列が実際のSSID（`YandY-A`等）と完全一致しているか（短縮形「A」等を登録していないか）、(2) 位置情報権限を許可済みか、(3) 端末の位置情報（GPS）システム設定がONになっているか。**回答待ち。**
  - ユーザーから「対応の隙間時間にUATシナリオを作っておいて」との依頼。`01 初回開発/03 UAT/UATシナリオ.md`を新規作成：カテゴリA（アクセシビリティ権限・アプリブロック）〜F（ON/OFF切替）まで要件定義に基づくテストケース一覧を作成。実装済み機能（UAT-A2のYouTubeブロック）はOK、調査中のUAT-B3（在宅判定）はNGとして記録、未実装機能は「実装待ち」として先行登録（実装が進むたびに追記・更新する運用）。
  - ユーザーから3点回答：SSIDは完全一致で入力済み／位置情報権限は「アプリ起動中のみ許可」で許可済み／位置情報システム設定はON。いずれも問題なし→**コード側の不具合と判断**。
  - **対処（2点）**：(1) `HomeWifiStatus.kt`の`currentWifiSsid()`を、`NetworkCapabilities`経由のWifiInfoが機種によってSSIDを返さないケースに対応するため、`WifiManager.connectionInfo`（非推奨だが実績のあるAPI）へのフォールバックを追加。(2) 手入力でのSSIDタイプミスを防ぐため、MainActivityに「今接続中のWi-Fiを登録」ボタンを追加（現在接続中のSSIDをそのまま登録、タイプ不要）。commit&push済み。
  - ユーザーから「GPSを常に許可が選べない、問題ないか」との質問。**「アプリ起動中のみ許可」で今は十分**（手動確認ボタンの用途のため）と回答。「常に許可」は今後ルールエンジンで自動バックグラウンド判定を実装する段階で`ACCESS_BACKGROUND_LOCATION`を別途リクエストする必要があると説明（今回は対応不要）。
  - ユーザーが再ビルド・「今接続中のWi-Fiを登録」→「在宅状態を確認」を実施 → **「在宅中」と正しく表示され成功**。UAT-B3をOKに更新。Wi-Fiを切った状態でも「非在宅」に正しく変わることも確認（UAT-B4 OK、自動更新ではなくボタン押下時点の状態を見る仕様であることも確認）。**実装順序②（Wi-Fi在宅判定）完了。**
- ユーザーから「進めて」の承認を受け、**実装順序③（ルールエンジン：時間帯×在宅条件のAND評価）を実装**：
  - `rule/TimeRule.kt`新規作成：曜日集合・開始/終了時刻・在宅条件を持つルールのデータクラス。SharedPreferencesのStringSetに保存するため`encode()`/`decode()`で文字列シリアライズ（外部ライブラリ追加を避けるための簡易実装）。
  - `rule/RuleEngine.kt`新規作成：`isRuleActive()`で日付をまたぐ時間帯（例：22:00-06:00）にも対応した有効判定、`activeRules()`で複数ルールから現在有効なものを抽出。
  - `rule/RuleSettings.kt`新規作成：ルール一覧の保存・追加・削除（`HomeWifiSettings`と同じパターン）。
  - `rule/RuleSection.kt`新規作成：ルール名・開始/終了時刻（HH:mm手入力）・曜日チェックボックス・在宅条件チェックボックスを持つ追加フォーム、登録済みルール一覧と削除ボタン、「現在有効なルールを確認」ボタンを持つComposable。
  - `MainActivity.kt`に`RuleSection()`を組み込み、画面が縦に伸びたため`Column`に`verticalScroll`を追加。
  - 祝日（休日・祝日のみ指定）の実際の判定は未実装——曜日集合での指定のみ対応、祝日API連携は実装順序⑥で対応予定。
  - NASパス側で作業・commit&push済み。
  - ユーザーが`git pull`・ビルドして確認 → **動作は問題なし**だが、(1) 開始/終了時刻のテキスト入力欄が縦長にレイアウト崩れする、(2) HH:mm手入力が打ちにくいので時計UIで選択できるようにしてほしい、との指摘。
  - **対処**：`RuleSection.kt`の時刻入力をテキスト手入力からMaterial3の`TimePicker`（ダイアログ表示、`rememberTimePickerState`使用）に変更。開始・終了の2つのボタンを`Row`+`Arrangement.spacedBy`で横並びにしてレイアウト崩れも解消。commit&push済み。**ユーザー側の再ビルド・確認待ち。**

### 次にやること（次セッション/次タスク最優先）
1. ユーザーにローカルクローンで`git pull`してもらい、再ビルド・実機確認を依頼する。**ここから継続。** TimePickerでの時刻選択がしやすくなったか、レイアウト崩れが直っているか確認してもらう。
2. 動作確認できたら、ルール本来の動作確認（時間帯・曜日・在宅条件の組み合わせ通りに有効/無効が切り替わるか、日付をまたぐ時間帯も含めて）も行う。UAT-D1〜D3を更新。
3. その後④Brave/ChromeのURL検知→⑤通知マナーモード切替→⑥祝日API連携、の順に小さく動くものを積み上げていく（前回PJのパターンを踏襲）。実装するたびにUATシナリオ（`01 初回開発/03 UAT/UATシナリオ.md`）に該当ケースを追加・実施すること。
4. Claudeの作業はNASパス（`\\YukiYoshiNAS\...\smartphone_detox`）上で行い、ユーザーの作業はローカルクローン（`C:\Users\yukiy\dev\smartphone_detox`）上で行う前提を継続。作業開始前に両者とも`git pull`を忘れないこと。
5. **新しいAndroid権限やAPIを使う際は、Manifestへの宣言漏れ・機種依存のAPI挙動差がないか実装時に一度チェックリスト的に確認すること**（実装順序①のXML属性ミス、②の権限宣言漏れ・機種依存のSSID取得不具合、と続けて実機検証まで進んでようやく発覚した教訓）。
6. **テキスト手入力よりも、選択式UI（TimePicker、ドロップダウン等）を最初から優先する**——時刻入力で手入力を選んだ結果ユーザーから「打ちにくい」と指摘され手戻りになった。今後同様の入力項目（日付・時刻・定型選択肢など）はテキスト入力より先に選択式UIを検討すること。

### 参考
- Discordのchat_id：`1517480345874731078`（ユーザーのDiscord user_id: `795820938221453314`、username: `yoshi19920305`）。返信時は`mcp__plugin_discord_discord__reply`に`chat_id`を渡す。
