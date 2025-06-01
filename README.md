# NumArt - Matematik Öğrenme Uygulaması

## Proje Hakkında

NumArt, çocukların matematik becerilerini geliştirmek için tasarlanmış eğlenceli bir mobil uygulamadır. Uygulama, farklı zorluk seviyelerinde matematik oyunları sunarak öğrenmeyi eğlenceli hale getirmeyi amaçlamaktadır.

## Özellikler

- Kullanıcı hesap yönetimi (Kayıt olma, giriş yapma, şifre sıfırlama)
- Çoklu dil desteği
- Farklı zorluk seviyelerinde oyunlar:
  - Eşleştirme Oyunu
  - Keşfetme Oyunu
  - Zombi Oyunu
- Ayarlar menüsü
- İlerleme takibi

## Teknik Detaylar

- **Platform:** Android
- **Programlama Dili:** Kotlin
- **Mimari:** MVC (Model-View-Controller)
- **Minimum SDK:** Android 5.0 (API Level 21)

## Proje Yapısı

```
app/src/main/java/com/achelmas/numart/
├── Activities/
│   ├── MainActivity.kt
│   ├── LoginActivity.kt
│   ├── SignupActivity.kt
│   ├── GameActivity.kt
│   ├── MatchGameActivity.kt
│   ├── DiscoverGameActivity.kt
│   ├── ZombieActivity.kt
│   └── SettingsActivity.kt
├── Adapters/
│   ├── AdapterOfDiscover.kt
│   └── AdapterOfMatchLvl1.kt
├── Models/
│   └── ModelOfDiscover.kt
├── Utils/
│   └── LanguageManager.kt
└── Level MVCs/
    ├── easyLevelMVC/
    ├── mediumLevelMVC/
    └── hardLevelMVC/
```

## Oyun Modları

### 1. Eşleştirme Oyunu

- Matematiksel ifadeleri doğru sonuçlarla eşleştirme
- Farklı zorluk seviyeleri
- Puan sistemi

### 2. Keşfetme Oyunu

Keşfetme Oyunu, artırılmış gerçeklik (AR) teknolojisi kullanılarak geliştirilmiş, tarihi ve önemli yerleri keşfetmeye yönelik interaktif bir öğrenme deneyimidir. Oyunda:

- **AR Teknolojisi:**

  - Gerçek dünya üzerinde 3D tarihi yapı ve mekan modelleri görüntülenir
  - Kullanıcı etkileşimli 3D nesneler
  - Dokunmatik kontroller

- **Oyun Mekanikleri:**

  - Tarihi yerler ve önemli binalar hakkında bilgi edinme
  - Her yer için özel hazırlanmış sorular
  - Yaş gruplarına göre özelleştirilmiş içerik
  - İlerleme sistemi
  - Puan kazanma

- **Özellikler:**

  - Çoklu dil desteği (Türkçe/İngilizce)
  - Sesli okuma özelliği (Text-to-Speech)
  - Ses açma/kapama kontrolü
  - Konfeti animasyonları (başarı kutlaması)
  - Yenileme ve ilerleme butonları
  - 3D model animasyonları

- **Eğitim Hedefleri:**

  - Tarihi ve kültürel miras hakkında bilgi edinme
  - Önemli yapıları ve mekanları tanıma
  - Görsel-mekansal algıyı güçlendirme
  - Dil becerilerini geliştirme (çoklu dil desteği)
  - Kültürel farkındalık oluşturma

- **Öğrenme Deneyimi:**

  - İnteraktif 3D modeller ile etkileşim
  - Tarihi bilgileri eğlenceli şekilde öğrenme
  - Anlık geri bildirimler
  - Eğlenceli ve motive edici arayüz
  - Adım adım ilerleme sistemi
  - Başarı kutlamaları

- **İçerik Özellikleri:**
  - Yaş gruplarına göre özelleştirilmiş içerik (10 yaş altı ve üstü)
  - Her yer için detaylı bilgi kartları
  - İnteraktif sorular ve cevaplar
  - Görsel ve sesli anlatımlar
  - Kültürel ve tarihi bağlamda öğrenme

### 3. Zombi Oyunu

Zombi Oyunu, artırılmış gerçeklik (AR) teknolojisi kullanılarak geliştirilmiş eğlenceli bir matematik oyunudur. Oyunda:

- **AR Teknolojisi:** Gerçek dünya üzerinde 3D zombi modelleri görüntülenir
- **Oyun Mekanikleri:**
  - Oyuncuya 3 can hakkı verilir
  - Zombiler rastgele konumlarda belirir ve oyuncuya doğru ilerler
  - Her zombiye tıklandığında 10 puan kazanılır
  - Zombiler oyuncuya ulaşırsa can kaybedilir
  - 3 can kaybedildiğinde oyun biter
- **Özellikler:**
  - 3D zombi modelleri ve animasyonları
  - Gerçek zamanlı hareket sistemi
  - Puan ve can takip sistemi
  - Başlangıç menüsü
  - Oyun sonu bildirimleri
- **Eğitim Hedefleri:**
  - Hızlı düşünme ve refleks geliştirme
  - Görsel-mekansal algıyı güçlendirme
  - Dikkat ve odaklanma becerilerini artırma
  - El-göz koordinasyonunu geliştirme

## Kurulum

1. Projeyi klonlayın
2. Android Studio'da açın
3. Gradle senkronizasyonunu tamamlayın
4. Uygulamayı çalıştırın

## Geliştirme

- Android Studio Arctic Fox veya üzeri
- Kotlin 1.5.0 veya üzeri
- Gradle 7.0 veya üzeri

## Katkıda Bulunma

1. Fork'layın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

## İletişim

Proje Sahibi: [İsim Soyisim]
E-posta: [E-posta Adresi]

<div align="center">
      <h1> 
            <img src="./app/src/main/res/drawable/logo.png" width="165px">
            <br/>
           Arithmatika
            <br/> 
      </h1>
</div>

<div align="center">
      <h3> 
            🌟 Mujahid Alabdullah - 200541605 🌟
          <br/>
      </h3>
<h4>Porje Dökümanı : <a href= "https://docs.google.com/document/d/1ubI3OFqzkjR_8TAjsisP0jeOMxRGo8e3/edit?usp=sharing&ouid=116800964171632565370&rtpof=true&sd=true">PROJE DOC (SWOT , MARS , THS , UMLS ve SAD dahil )</a></h4>
      <h4>Porje Dökümanı : <a href= "https://mujahid0abdullah.github.io/ARGameView/">PROJE Landing page</a></h4>
</div>

   <br/> 
            <img style="display: inline-block;" src="https://mujahid0abdullah.github.io/ARGameView/assets/img/7.jpg" width="265px">
           <img style="display: inline-block;" src="https://mujahid0abdullah.github.io/ARGameView/assets/img/20.jpg" width="265px">

   <br/>

# 🧠 Arithmatika

_Eğitici Artırılmış Gerçeklik (AR) Tabanlı Mobil Oyun_

## ✨ Proje Hakkında

**Arithmatika**, 5–12 yaş arası çocuklar için geliştirilen bir artırılmış gerçeklik (AR) tabanlı mobil eğitim oyunudur. Oyun, matematiksel işlem becerilerini ve dikkat yeteneklerini geliştirmeyi hedeflemektedir. Proje, THY'nin isteği doğrultusunda uçuş sırasında kullanılmak üzere özel olarak tasarlanmıştır.

> **Slogan:** _"Gökyüzünde Matematik, Yanı Başında Eğlence!"_

---

## 📦 Kurulum ve Çalıştırma (Adım Adım)

> Bu adımlar, uygulamayı cihazınıza kurmanız ve çalıştırmanız içindir.

### 1. Projeyi Klonlayın

```bash
git clone https://github.com/kullaniciadi/arithmatika.git
cd arithmatika
```

### 2. Android Studio ile Açıp Çalıştırabilirsiniz

## Android Studio > Open Project > arithmatika

## 📦 APK Dosyası

📥 [arithmatika.apk](https://mega.nz/file/TihwXZxI#Ab-_pGJW-Z8VZlsWzx11uVeOk-tvWlAOh1A-zEBitSc)

Uygulama Android 8.0+ destekli cihazlarda test edilmiştir.

---

## 📷 Uygulama Videosu

🎥 [Match Oyunun Demo Videosu İzle](https://youtube.com/shorts/dm-a3USfmCQ?feature=share)

🎥 [Math Oyunun Demo Videosu İzle](https://youtube.com/shorts/VfHXMOfZC1w?feature=share)

---

## 📱 Temel Özellikler

- 🎮 **Kişiselleştirilebilir Kaşif Avatarı**
- 🌍 **3D Dünya Haritası Üzerinde Etkileşimli Uçuş Rotası**
- 🧩 **Mini Oyunlar ve Matematik Görevleri (Kolay–Zor)**
- 🧠 **Dikkat Geliştirici Aktiviteler**
- 🗣️ **Dil Öğrenme ve Kültürel Keşif Görevleri**
- 🛡️ **Güvenli Oyun Deneyimi (Ebeveyn Kontrolü, Göz Dinlendirme, Oturma Pozisyonu)**

---

## 🎮 Oyun Modülleri

### ➕ Matematik Görevleri

Çocuklar, verilen sayılarla hedef sonuçlara ulaşmaya çalışır. Kolaydan zora doğru seviye ilerlemesi vardır.

### 🧠 Dikkat Mini Oyunları

AR nesneleriyle şekil eşleştirme, hafıza ve desen tanıma gibi aktiviteler içerir.

### 🌍 Keşif ve Dil Eğitimi

Her ülke için kültürel bilgiler, selamlaşmalar ve sayılar öğretilir. Avatarlar yöresel kıyafetler giyebilir.

---

## 🏗️ Teknolojik Altyapı

| Bileşen          | Teknoloji                             |
| ---------------- | ------------------------------------- |
| Platform         | Android                               |
| Programlama Dili | Kotlin                                |
| AR Teknolojisi   | [SceneView AR](https://sceneview.dev) |
| 3D Modelleme     | TinkerCAD , Blender                   |
| Backend          | Firebase                              |
| Tasarım Araçları | Figma                                 |
| Sürüm Kontrol    | Git + GitHub                          |
| Proje Yönetimi   | DevOps + trello                       |

---

## 👨‍👩‍👧‍👦 Hedef Kullanıcılar

- 5–12 yaş arası çocuklar
- Havayolu şirketleri (uçuş içi eğlence için)
- Okullar ve eğitim kurumları

---

## 📈 Proje Durumu

> 🚧 **Alfa Sürümünde**  
> ✅ Prototip Tamamlandı  
> 🧪 Test Süreci Devam Ediyor  
> 🧩 Yeni içerikler geliştiriliyor

---

---

## 🗓️ Haftalık Çalışma Takvimi

| Hafta | Tarih Aralığı         | Yapılan Çalışmalar                                     |
| ----- | --------------------- | ------------------------------------------------------ |
| 1     | 15 - 22 Mart 2025     | Proje planlaması, teknoloji seçimi, gereksinim analizi |
| 2     | 23 - 29 Mart 2025     | UI/UX tasarımları, sistem mimarisi belirleme           |
| 3     | 30 Mar - 5 Nisan 2025 | 3D model tasarımları, AR sahne testi                   |
| 4     | 6 - 12 Nisan 2025     | Matematik görev modülü geliştirme                      |
| 5     | 13 - 25 Nisan 2025    | Dikkat oyunu modülü geliştirme, Firebase bağlantısı    |
| 6     | 25 - 29 Nisan 2025    | Kullanıcı arayüzleri, avatar oluşturma ekranları       |
| 7     | 27 Nisan - 3 Mayıs    | Görev entegrasyonu ve seviye sistemi geliştirme        |
| 8     | 4 - 10 Mayıs          | Test planı uygulama: birim testler, kullanıcı testleri |
| 9     | 11 - 17 Mayıs         | Hataların düzeltilmesi, video hazırlıkları             |
| 10    | 18 - 20 Mayıs         | APK üretimi, son kontrol, README ve teslim işlemleri   |

---

## 📬 İletişim

Geliştirici: [Mujahıd ALABDULLAH]  
📧 E-posta: 200541605@firat.edu.tr
🔗 [LinkedIn / Kişisel Web Sitesi]

---

<br/>
