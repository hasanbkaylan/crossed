# Crossed 📍

Android İçin Çevrimdışı, Gizlilik Odaklı ve P2P Konum Kesişimi Tespit Uygulaması

---

## 📌 Proje Hakkında

Crossed, internet bağlantısına veya merkezi bir sunucuya ihtiyaç duymadan, arkadaşlarınızla geçmişte aynı zamanda ve aynı yerde bulunup bulunmadığınızı tespit etmenizi sağlayan açık kaynaklı bir Android uygulamasıdır.

Geleneksel uygulamaların aksine Crossed, konum verilerinizi asla harici bir sunucuya göndermez. Tüm veriler yalnızca kendi cihazınızda işlenir ve saklanır.

---

## ✨ Öne Çıkan Özellikler

- 🔒 %100 Yerel Depolama ve Gizlilik: Analitik takibi, reklam veya bulut eşitlemesi yoktur. İnternet erişimi gerektirmez.
- 🔐 Kriptografik Özetleme (Hashing): Fotoğraflarınızdan alınan enlem, boylam ve zaman verileri geri döndürülemez hash formatına çevrilir. Ham konum verisi doğrudan paylaşılmaz.
- 🤝 P2P (Peer-to-Peer) Bağlantı: Merkezi bir sunucu olmadan, yakındaki cihazlarla doğrudan yerel ağ üzerinden iletişim kurarak eşleşme kontrolü yapar.
- 🎯 Özelleştirilebilir Eşleşme Yarıçapı: Kesişim hassasiyetini 50m, 100m, 200m veya 500m olarak ayarlayabilirsiniz.
- 📸 Yerel EXIF Fotoğraf Taraması: Galerinizdeki konum etiketi içeren fotoğrafları yerel olarak tarayarak konum veritabanınızı oluşturur.
- 🌐 Dil Desteği: Türkçe ve İngilizce dil seçenekleri mevcuttur.
- 🗑️ Tam Veri Kontrolü: Cihazınızda taranmış olan tüm konum verilerini tek tıkla tamamen silebilirsiniz.

---

## 🛡️ Gizlilik ve Güvenlik İlkeleri

1. Çevrimdışı Çalışma: Uygulamanın internet erişim yetkisi yoktur. Verileriniz cihaz dışına çıkamaz.
2. Özetlenmiş Veri Karşılaştırması: Eşleşme sırasında ham koordinatlar yerine yalnızca şifrelenmiş özetler (hash) karşılaştırılır.
3. Açık Rıza Esası: Arka planda gizli tarama yapılmaz. Eşleşme kontrolü yalnızca siz ve karşınızdaki kişi "Yakındakileri Bul" butonuna bastığında başlar.

---

## 🚀 Kullanım Adımları

1. Gereksinimler: Android 8.0 veya üzeri işletim sistemi.
2. Veritabanı Oluşturma: Uygulamayı açın ve "Yeni Fotoğraflar İçin Tara" butonuna basarak yerel konum verilerinizi indeksleyin.
3. Ayarlar: "Ayarlar ve Veriler" ekranından görünen adınızı ve eşleşme yarıçapını (ör. 500 metre) seçin.
4. Eşleşme: Yanınızdaki arkadaşınızla aynı anda "Yakındakileri Bul" butonuna basın.
5. Sonuçlar: Geçmişte aynı yerde bulunduysanız, kesişen tarih, saat ve konum bilgileri "Yollar Kesişti!" ekranında listelenecektir.

---

## 🛠️ Teknik Detaylar ve Lisans

- Platform: Android
- Mimari: Offline-First / Peer-to-Peer (P2P)
- Geliştirici: Hasan Berat Kaylan
- Lisans: GNU General Public License v3.0 (GPLv3) — Özgür ve Açık Kaynak Kodlu Yazılım
