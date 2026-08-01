# Crossed - Otomatik Sürüm Yayınlama Rehberi

Bu proje, yeni bir versiyon etiketi oluşturduğunuzda uygulamanın otomatik olarak derlenmesini ve bir APK olarak GitHub üzerinden size sunulmasını sağlayacak bir sisteme sahiptir. 

Tüm bu işlemleri bilgisayara ihtiyaç duymadan, telefonunuzdan GitHub'a girerek yapabilirsiniz.

## 1. İlk Kurulum: İmza Anahtarlarını (Secrets) Ekleme

Uygulamanızın cihazlara yüklenebilmesi için dijital olarak imzalanması gerekir. Bunun için güvenlik anahtarlarınızı projenin Ayarlar (Settings) bölümüne eklemeliyiz.

1. Telefonunuzun tarayıcısından GitHub'a girin ve deponuzun sayfasına gidin.
2. Üst menüden **Settings** (Ayarlar) sekmesine dokunun.
3. Sol menüyü aşağı kaydırın, **Secrets and variables** > **Actions** seçeneğine tıklayın.
4. Çıkan sayfada **New repository secret** (Yeni depo gizli değişkeni) yeşil butonuna basın.
5. Aşağıdaki dört gizli değişkeni tek tek eklemeniz gerekiyor:

   * **İsim:** `SIGNING_KEY`
     * **Değer:** Android için oluşturduğunuz ".jks" veya ".keystore" dosyasının Base64 ile kodlanmış hali. *(İnternetteki "Keystore to Base64" araçlarını kullanabilirsiniz. Eğer test için yayınlıyorsanız ve bu adım zor gelirse 3. başlığa göz atın.)*
   * **İsim:** `KEY_ALIAS`
     * **Değer:** Anahtarınızın takma adı (Örn: `key0` veya `upload`)
   * **İsim:** `KEY_STORE_PASSWORD`
     * **Değer:** Keystore için belirlediğiniz şifre.
   * **İsim:** `KEY_PASSWORD`
     * **Değer:** Anahtar (alias) için belirlediğiniz şifre.

## 2. Yeni Bir Sürüm (Release) Nasıl Yayınlanır?

Ayarlarınızı yaptıktan sonra, yeni bir sürüm yayınlamak çok kolaydır:

1. Deponuzun ana sayfasına gidin.
2. Sağ kısımdaki (veya üst menüdeki) **Releases** bölümüne tıklayın ve **Draft a new release** (Yeni bir sürüm taslağı oluştur) butonuna basın.
3. **Choose a tag** (Bir etiket seçin) kutusuna dokunun ve yeni bir versiyon yazın. **Önemli:** Sürüm adı mutlaka `v` harfi ile başlamalıdır. Örneğin: `v1.0`, `v1.1`, `v2.0` gibi.
4. Yazdıktan sonra "Create new tag: v1.0 on publish" (Yayınlarken v1.0 etiketini oluştur) seçeneğini seçin.
5. Sürüme bir başlık ve kısa bir açıklama ekleyin (Örn: "İlk sürüm eklendi").
6. Aşağıdaki yeşil **Publish release** (Sürümü yayınla) butonuna basın.

## 3. APK'yı İndirme

Publish release butonuna bastıktan sonra:
1. Deponuzun üst menüsünden **Actions** sekmesine gidin.
2. "Build and Release APK" adlı görevin çalıştığını göreceksiniz. Sarı dönen ikon işlemin devam ettiğini gösterir (yaklaşık 3-5 dakika sürebilir).
3. İşlem başarıyla bittiğinde (yeşil tik olduğunda), **Releases** sayfasına geri dönün.
4. Sürümünüzün (örneğin v1.0) hemen altında `app-release.apk` dosyası belirecektir.
5. Telefondan bu dosyaya tıklayarak indirebilir ve hemen cihazınıza kurabilirsiniz.

## 4. Hata Durumunda (Loglara Bakma)

Eğer Actions sekmesinde derleme işlemi kırmızı bir "X" işareti (başarısız) verirse:
1. Başarısız olan göreve tıklayın.
2. Açılan sayfada sol taraftaki menüden **build** (derleme) sekmesine basın.
3. Sağ tarafta çıkan log ekranında kırmızı hata mesajlarını (genellikle en alttadır) kopyalayabilir ve nerede sorun olduğunu anlamak için yapay zeka asistanınıza (bana) gönderebilirsiniz.

*(Not: İmza (Keystore) dosyası oluşturmak karmaşık gelirse veya sadece test etmek isterseniz, `.github/workflows/release.yml` dosyasındaki "APK'yı İmzala" (Sign APK) adımını silip yönergelerde yazan yorumları takip ederek imzasız (veya debug) APK da üretebilirsiniz.)*
