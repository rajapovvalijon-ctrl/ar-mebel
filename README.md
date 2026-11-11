# Mebel AR (IKEA Place'ga o'xshash)

Android + ARCore + SceneView. Mebelni tanlang, polga qarating, ekranga bosing — mebel xonangizda paydo bo'ladi.

## APK ni GitHub Actions bilan yig'ish
1. Bu papkani GitHub'dagi yangi repoga yuklang (`main` branch).
2. Repo > Actions > "APK yig'ish" avtomatik ishga tushadi (yoki "Run workflow").
3. Tugagach, run sahifasi pastidagi **Artifacts > MebelAR-debug-apk** dan `app-debug.apk` ni yuklab oling.
4. Telefonga o'rnating (ARCore qo'llab-quvvatlaydigan Android 7+ kerak; Google Play Services for AR avtomatik so'raladi).

## Yangi mebel qo'shish
`.glb` faylni `app/src/main/assets/models/` ga tashlang va `MainActivity.kt` dagi `catalog` ro'yxatiga qator qo'shing.
