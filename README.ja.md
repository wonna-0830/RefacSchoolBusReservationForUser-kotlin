# 🚌 母校のスクールバス予約システム（利用者用アプリ・リファクタリング版）

- RefacSchoolBusReservationForUser-kotlin  

- 2024年 大邱カトリック大学 キャップストーンデザイン 🥉奨励賞受賞作品（スクールバス予約システム）の Kotlin リファクタリング版です。  
- 従来の Java バージョンの構造を改善し、ユーザー体験（UX）とインターフェース（UI）を向上させました。  
- ****デモ動画を見る**** [📽️](https://youtube.com/shorts/W_HfrclE_xM?feature=share)  
- **📄 [🇰🇷 한국어版READMEはこちら](./README.md)**  

---

## 主な機能

### 1. ログイン [Login.kt](app/src/main/java/com/example/refac_userbus/Login.kt) / 会員登録画面 [Register.kt](app/src/main/java/com/example/refac_userbus/Register.kt)
- [ログイン／登録ページ](https://github.com/wonna-0830/login)
<a href="https://github.com/wonna-0830/login">
  <img src="images/light_login.jpg" width="180">
  <img src="images/dark_login.jpg" width="180">
  <img src="images/light_register.jpg" width="180">
  <img src="images/dart_register.jpg" width="180">
</a>

- Firebase Authentication によるメール／パスワードログイン  
- 自動ログイン機能（CheckBox 利用） => sharedPreference に保存  
- 登録完了後、Firebase にユーザー情報を保存  
- メール形式でない、または入力欄が未入力の場合はログイン無効  
- メール形式でない、または入力欄未入力／パスワード8文字未満／名前が未入力の場合は登録無効  

### 2. 路線選択画面 [RouteChoose.kt](app/src/main/java/com/example/refac_userbus/RouteChoose.kt)
- [レイアウトページ](https://github.com/wonna-0830/routechoose)
<a href="https://github.com/wonna-0830/login">
  <img src="images/light_routechoose.jpg" width="200">
  <img src="images/dark_routechoose.jpg" width="200">
</a>

- 4つの登校バス路線＋1つの下校バスボタンを提供  
- ボタンをクリックすると、選択された路線に応じて TimePlace ページの構成要素が変化（TextView／Spinner／地図など）  
- ユーザー名は TextView にて表示  

### 3. 時間および停留所選択画面 [TimePlace.kt](app/src/main/java/com/example/refac_userbus/TimePlace.kt)
- レイアウト [ページリンク](https://github.com/wonna-0830/timeplace)
<a href="https://github.com/wonna-0830/login">
  <img src="images/light_timeplace.jpg" width="200">
  <img src="images/dark_timeplace.jpg" width="200">
</a>

- 選択された路線に応じて Spinner／画像ビュー／テキストビューが動的に更新  
- 現在時刻以降の予約可能時間のみ表示  
- 時間または停留所のいずれかが未選択の場合、予約ボタンは無効  
- ローディング中は ProgressBar 表示  
- 予約完了後、Firebase Firestore に保存  
- 登校／下校それぞれ1日1回の予約制限  

### 4. 予約確認画面 [SelectBusList.kt](app/src/main/java/com/example/refac_userbus/SelectBusList.kt)
- レイアウト [ページリンク](https://github.com/wonna-0830/selectbuslist)
<a href="https://github.com/wonna-0830/login">
  <img src="images/light_selectbuslist.jpg" width="200">
  <img src="images/dark_selectbuslist.jpg" width="200">
</a>

- Firebase からユーザーの予約データをリスト形式で表示  
- 予約がない場合は案内メッセージを表示  
- 各予約項目には削除ボタンあり（バス出発後は無効化）  
- 削除時は Firebase 上でもリアルタイムに削除反映  
- 予約完了時、バス出発10分前にアラーム通知機能も追加済 [(確認画像)](images/user_alert.jpg)

### 5. 共通機能（FAB／設定など）
- [FAB（Floating Action Button）](images/light_fab.jpg) を使用して以下機能にアクセス可能：
  - 大学ホームページのスクールバス案内へリンク  
  - 予約確認画面へショートカット  
  - ログアウト機能  
  - ダークモード切り替え（端末のテーマに連動）  
- 端末の戻るボタンを2回押すとアプリ終了  

---

## 技術スタック

| 区分 | 使用技術 |
|------|----------|
| 言語 | Kotlin |
| IDE  | Android Studio |
| DB   | Firebase Firestore |
| 認証 | Firebase Authentication |
| アーキテクチャ | MVVM（部分導入） |
| その他 | RecyclerView、Spinner、ViewBindingなど |

---

## 🔄 リファクタリングのポイント

- **Java → Kotlin への完全移行** により、可読性と保守性を向上  
- **XML 構造の改善**
  - 路線ごとに分かれていた XML を、**共通レイアウト構造に統合**  
- **UX改善**
  - **1日1回の予約制限**で重複を防止  
  - Spinner では現在時刻以降のみ表示するよう **動的フィルタリング**  
  - よく使う機能（予約確認、ログアウトなど）は **FABボタン** で即アクセス可能  
- **UI改善**
  - ダークモードに自動対応（端末テーマに連動）  
  - ローディング状態の可視化（ProgressBar）  
- **構造的改善および拡張性確保**
  - Firebase Authentication によるユーザー別データ管理  
  - Firestore に構造化された予約データ保存 ⇒ 検索・並び替えなど拡張が容易

---

> ✨ このアプリは「生徒の快適な通学サポート」を目指して設計された、  
> 実用性・直感性・安全性を重視したスクールバス予約アプリです。

