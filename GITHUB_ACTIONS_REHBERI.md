# Crossed - Otomatik Sürüm Yayınlama Rehberi

Bu proje, yeni bir versiyon etiketi oluşturduğunuzda uygulamanın otomatik olarak derlenmesini ve bir **Debug APK** olarak GitHub üzerinden size sunulmasını sağlayacak bir sisteme sahiptir. 

Tüm bu işlemleri bilgisayara ihtiyaç duymadan, telefonunuzdan GitHub'a girerek yapabilirsiniz. Bu aşamada karmaşık imza (keystore) ayarlarına veya gizli anahtar (secrets) girmeye ihtiyacınız yoktur; GitHub varsayılan debug imzasıyla uygulamanızı test için otomatik olarak hazırlar.

## 1. Yeni Bir Sürüm (Release) Nasıl Yayınlanır?

Yeni bir sürüm yayınlamak çok kolaydır:

1. Telefonunuzun tarayıcısından GitHub'a girin ve deponuzun sayfasına gidin.
2. Sağ kısımdaki (veya alt/üst menüdeki) **Releases** bölümüne tıklayın ve **Draft a new release** (Yeni bir sürüm taslağı oluştur) butonuna basın.
3. **Choose a tag** (Bir etiket seçin) kutusuna dokunun ve yeni bir versiyon yazın. **Önemli:** Sürüm adı mutlaka `v` harfi ile başlamalıdır. Örneğin: `v1.0`, `v1.1`, `v2.0` gibi.
4. Yazdıktan sonra "Create new tag: v1.0 on publish" (Yayınlarken v1.0 etiketini oluştur) seçeneğini seçin.
5. Sürüme bir başlık ve kısa bir açıklama ekleyin (Örn: "İlk test sürümü eklendi").
6. Aşağıdaki yeşil **Publish release** (Sürümü yayınla) butonuna basın.

## 2. APK'yı İndirme

Publish release butonuna bastıktan sonra:
1. Deponuzun üst menüsünden **Actions** sekmesine gidin.
2. "Build and Release APK" adlı görevin çalıştığını göreceksiniz. Sarı dönen ikon işlemin devam ettiğini gösterir (yaklaşık 2-3 dakika sürebilir).
3. İşlem başarıyla bittiğinde (yeşil tik olduğunda), **Releases** sayfasına geri dönün.
4. Sürümünüzün (örneğin v1.0) hemen altında `app-debug.apk` dosyası belirecektir.
5. Telefondan bu dosyaya tıklayarak indirebilir ve hemen cihazınıza kurup test edebilirsiniz.

## 3. Hata Durumunda (Loglara Bakma)

Eğer Actions sekmesinde derleme işlemi kırmızı bir "X" işareti (başarısız) verirse:
1. Başarısız olan göreve tıklayın.
2. Açılan sayfada sol taraftaki menüden **build** (derleme) sekmesine basın.
3. Sağ tarafta çıkan log ekranında kırmızı hata mesajlarını (genellikle en alttadır) kopyalayabilir ve nerede sorun olduğunu anlamak için yapay zeka asistanınıza (bana) gönderebilirsiniz.

*(Not: İleride uygulamanızı resmi olarak (Release) imzalamak isterseniz, bu konfigürasyona imza anahtarı (keystore) adımları kolayca eklenebilir. Şimdilik hızlıca test etmeniz için debug modundadır.)*
