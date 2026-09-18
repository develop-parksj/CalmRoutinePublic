# CalmRoutine (カームルーティン)

Native Android (Kotlin/Jetpack Compose) で構築された、日々のルーティン管理とメンタルウェルネスをサポートするアプリケーションです。AIコーチによるパーソナライズされたフィードバック、感情トラッキング、およびクラウドデータ同期機能を提供します。

## 🚀 プロジェクトの概要
このプロジェクトは、マインドフルな生活を支援するための実用的なツールの提供に加え、Jetpack Composeを用いたモダンなUI開発、AI（GPT）との連携、およびFirebaseを活用した堅牢なクラウドインフラの構築を目的として開発されました。

### 主な機能
- **ルーティン管理**: 習慣化を助ける直感的なルーティン作成・管理機能。
- **感情トラッキング**: 日々の気分を絵文字とメモで記録し、視覚的な履歴を確認。
- **AIコーチング**: OpenAI GPT APIを活用し、ユーザーの状況に合わせた励ましやアドバイスをリアルタイムで提供。
- **クラウド同期とバックアップ**: Firebase Firestoreを活用し、データの安全なバックアップと複数デバイス間での同期を実現。
- **多言語対応**: 日本語、韓国語、英語を含む10言語（日・韓・英・独・西・仏・印・葡・露・中）に完全対応。
- **プレミアム機能**: Google Play Billingを通じた定期購読により、広告なしの体験や高度なAI機能を提供。

## 🛠 技術スタック
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **State Management**: StateFlow, Flow, ViewModel
- **Backend**: 
  - **Firebase**: Authentication, Firestore, Remote Config, Analytics, Crashlytics
  - **AWS**: Lambda / API Gateway (OpenAI GPT Integration Proxy)
- **Local DB**: SQLite (SQLiteOpenHelper Implementation)
- **Libraries**: Retrofit2, OkHttp3, kotlinx-serialization, WorkManager, Coil, Play Billing Library, AdMob

## 💡 技術的な特徴と挑戦
1. **AI連携エンジンの構築**: セキュリティと拡張性を考慮し、AWS LambdaをプロキシとしてOpenAI APIと安全に通信するアーキテクチャを実装。
2. **モダンなUI開発**: Jetpack Composeを活用した宣言的なUI設計により、保守性が高く柔軟なアニメーションやレイアウトを実現。
3. **バックグラウンド処理**: WorkManagerを使用し、AIコーチによる通知生成やリマインダーなどの定期タスクを、システムリソースを最適化しつつ実行。
4. **柔軟なローカライズ**: 多言語リソースの管理を徹底し、各言語圏のユーザーに最適化された自然なUI/UXを提供。

## 📦 セットアップと実行
> [!IMPORTANT]
> セキュリティ上の理由から、`google-services.json` および APIエンドポイント情報はリポジトリに含まれていません。各自でFirebaseプロジェクトを作成し、配置する必要があります。

1. `./gradlew assembleDebug`
2. Android Studio でプロジェクトを開き、実行
