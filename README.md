# Lịch Âm Dương – ứng dụng Android

Nhà phát triển: **Công ty cổ phần Công nghệ ITD Việt Nam**
Điện thoại / Zalo: 0988095867 · Email: vanthu.itdvn@gmail.com · Website: itdvietnam.com.vn
Địa chỉ: Số 1 Ngõ 30 đường Lý Sơn, Hà Nội

## Tải và dùng ngay

- **Android:** tải [LichAmDuong.apk](https://github.com/itdvietnamjsc83-ai/lich_am_duong/releases/latest/download/LichAmDuong.apk) về điện thoại, mở file để cài. Nếu máy hỏi, cho phép cài ứng dụng từ nguồn này.
- **iPhone / iPad:**
  1. Mở <https://itdvietnamjsc83-ai.github.io/lich_am_duong/> bằng **Safari**.
  2. Bấm nút **Chia sẻ**, chọn **Thêm vào MH chính**.
  3. Ứng dụng có biểu tượng riêng trên màn hình chính và dùng được khi không có mạng.
  4. Muốn được báo nhắc lịch đúng giờ: bấm **Thêm vào ứng dụng Lịch**. Các lần nhắc sẽ chuyển sang ứng dụng Lịch của iPhone, và Lịch sẽ báo đúng giờ.

Cả hai bản đều được GitHub Actions tự build và tự cập nhật mỗi khi mã nguồn thay đổi:
- `.github/workflows/android.yml` build file APK và đăng lên mục Releases.
- `.github/workflows/pages.yml` đưa thư mục `web/` lên GitHub Pages.

## Chức năng

- **Lịch tháng**: số dương lịch lớn, số âm lịch bên dưới. Ngày được tô màu như sau:
  - Chủ nhật, mùng 1 và rằm có màu đỏ.
  - Ngày lễ có chấm vàng.
  - Vuốt ngang để chuyển tháng.
- **Chi tiết ngày**:
  - Ngày âm lịch, tháng đủ hay thiếu.
  - Can chi của ngày, tháng, năm.
  - Tiết khí, pha trăng, ngày lễ, giờ hoàng đạo.
- **Đổi ngày** dương → âm và âm → dương:
  - Có xử lý tháng nhuận.
  - Báo lỗi khi chọn ngày 30 của tháng thiếu.
- **Nhắc lịch theo âm lịch hoặc dương lịch**:
  - Lặp *một lần*, *hằng tháng* (ví dụ mùng 1 và rằm) hoặc *hằng năm* (ví dụ ngày giỗ).
  - Chọn báo đúng ngày hoặc trước 1, 3, 7 ngày, và chọn giờ báo.
  - Xem trước 4 lần nhắc kế tiếp ngay khi đang tạo.
  - Hai quy tắc riêng cho âm lịch: tháng thiếu nhắc vào ngày 29; năm có tháng nhuận chỉ nhắc ở tháng chính.
  - Tự đặt lại lịch báo sau khi khởi động máy, đổi giờ hoặc cập nhật ứng dụng.
- **Mốc báo chọn tự do**: thông báo đặt trước 0–30 ngày, vào giờ và phút bất kỳ. Chạm vào giờ để mở mặt đồng hồ.
- **Đổ chuông như báo thức** (tuỳ chọn riêng cho từng nhắc lịch, bản Android):
  - Chuông có **giờ riêng, chọn tự do**: trước 0–30 ngày, giờ và phút bất kỳ, độc lập với giờ thông báo. Ví dụ: thông báo 19:45 trước 3 ngày, đổ chuông 05:30 sáng đúng ngày giỗ.
  - Đến giờ, điện thoại đổ chuông và rung liên tục, hiện màn hình chuông kể cả khi đang khoá.
  - Có hai nút **Tắt chuông** và **Báo lại**. Thời gian báo lại chọn được: 5, 10, 15 hoặc 30 phút.
  - Không ai tắt thì chuông tự tắt sau 5 phút và để lại thông báo "Đã lỡ chuông".
  - Chuông kêu theo âm lượng báo thức của máy, nên vẫn kêu khi máy để im lặng. Có nút **Nghe thử chuông**.
  - Nếu giờ chuông trùng giờ thông báo, ứng dụng chỉ đổ chuông, không hiện thêm thông báo.
  - Trên iPhone, Apple không cho ứng dụng web đổ chuông như ứng dụng Đồng hồ. Khi thêm vào ứng dụng Lịch, giờ chuông được đưa vào thành một lần báo riêng.
- **Lặp hằng ngày** (*Nhắc lịch › Lặp hằng ngày*): dùng cho việc làm nhiều lần mỗi ngày, như uống thuốc, cho con ăn hay đổ nước thải.
  - Mỗi việc có nhiều giờ trong ngày. Có bốn cách nhập nhanh:
    - gõ nhiều giờ một lúc, ví dụ `7h 11h30 19h`;
    - chọn nhanh "2 lần", "3 lần" mỗi ngày;
    - tạo theo chu kỳ, ví dụ "mỗi 3 giờ từ 6:00 đến 21:00";
    - chạm vào giờ để chọn trên mặt đồng hồ.
  - Chọn được hằng ngày hoặc chỉ một số thứ, và có thể đặt ngày kết thúc, ví dụ uống thuốc trong 10 ngày.
  - Cách báo: đổ chuông như báo thức (có báo lại) hoặc chỉ thông báo. Có thể ghi chú, hiện ra khi báo, ví dụ "uống sau ăn, 1 viên".
  - Danh sách **Hôm nay** liệt kê mọi lần báo theo giờ, có ô tích "đã xong" để biết đã uống thuốc, đã cho ăn hay chưa.
  - Bản Android có nút **Thêm vào Đồng hồ của máy**, để tạo báo thức tương ứng trong ứng dụng Đồng hồ (không bắt buộc).
  - Bản iPhone có nút **Thêm vào ứng dụng Lịch**, tạo các lịch lặp hằng tuần trong ứng dụng Lịch, có âm báo.
  - Dùng chung file sao lưu với lịch nhắc, nên chuyển sang máy khác được.
- **Dải thương hiệu ITD Việt Nam** luôn hiện ở cuối mọi màn hình. Chạm vào để xem thông tin liên hệ.
- **Sao lưu & chuyển máy**: vào *Cài đặt › Sao lưu & chuyển máy*.
  - **Xuất file sao lưu**: tạo file `.json` chứa toàn bộ nhắc lịch. Lưu file vào Google Drive, iCloud, Zalo hoặc máy tính.
  - **Nhập từ file**: trên máy mới, mở file đó. Nhắc lịch được thêm vào danh sách hiện có, nhắc trùng tự bỏ qua.
  - Bản Android và bản iPhone dùng chung định dạng file, nên chuyển qua lại giữa hai loại máy được.
- **Giao diện co giãn theo độ rộng màn hình**:
  - Điện thoại thanh và màn hình ngoài Z Fold: một ngăn, thanh điều hướng ở dưới.
  - Z Fold mở ra: hai ngăn, đường chia nằm đúng giữa màn hình, trùng nếp gập.
    - Ngăn trái lịch tháng, ngăn phải chi tiết ngày.
    - Ngăn trái đổi ngày, ngăn phải danh sách nhắc lịch.
    - Ngăn trái danh sách nhắc lịch, ngăn phải trình soạn nhắc.
  - Máy tính bảng hoặc Z Fold xoay ngang: thanh điều hướng dọc bên trái và hai ngăn.
- **Giao diện Sáng / Tối / Theo máy**. Giao diện tối dùng nền đen tuyền `#000000` để tiết kiệm pin trên màn hình AMOLED.
- **Cỡ chữ**: Vừa / Lớn / Rất lớn. Mặc định là Lớn, cho dễ đọc.
- **Hoạt động hoàn toàn offline**: không cần mạng, không thu thập dữ liệu, nhắc lịch lưu trên máy.

## Mở và chạy

1. Cài **Android Studio** (bản Ladybug 2024.2 trở lên).
2. Chọn *File › Open*, chọn thư mục `LichAmDuong`. Lần mở đầu tiên, Android Studio tự tải Gradle 8.10.2 và các thư viện, cần có mạng.
3. Cắm điện thoại (bật *Gỡ lỗi USB*) hoặc tạo máy ảo, rồi bấm **Run ▶**.
   - Muốn thử bố cục Z Fold, tạo máy ảo loại *7.6" Fold-in with outer display*.

### Tạo bản cài đặt APK / AAB

- **Bản thử:** chọn *Build › Build App Bundle(s) / APK(s) › Build APK(s)*.
- **Bản phát hành (đưa lên Google Play):** chọn *Build › Generate Signed App Bundle / APK*, sau đó tạo hoặc chọn tệp khoá ký.
  - Lưu tệp khoá ký cẩn thận. Mất khoá thì không cập nhật được ứng dụng trên Google Play.

### Chạy kiểm thử lõi âm lịch

```
./gradlew test
```

Bộ kiểm thử nằm ở `app/src/test/.../LunarCalendarTest.kt`. Nội dung kiểm tra:
- Ngày Tết các năm 2024, 2025, 2026.
- Tháng nhuận của các năm 2020, 2025, 2026, 2028.
- Đổi qua lại âm ↔ dương, trải từ năm 2000 đến 2060.
- Can chi, giờ hoàng đạo, tiết khí.
- Cách tính lần nhắc kế tiếp.

## Bản web cho iPhone (thư mục `web/`)

- Viết bằng HTML/CSS/JavaScript thuần, không phụ thuộc thư viện ngoài.
- Có `manifest.webmanifest` và `sw.js`, nên cài được lên màn hình chính và chạy offline.
- `lunar.js` là bản JavaScript của lõi âm lịch Kotlin, cho kết quả giống hệt bản Android.
- Bố cục tự đổi theo màn hình:
  - Màn hình hẹp hơn 700px: một ngăn.
  - iPad, hoặc điện thoại xoay ngang: hai ngăn.
- Có giao diện tối nền đen tuyền cho màn hình OLED.
- **Nhắc lịch:** dữ liệu lưu trên máy (localStorage). Trên iOS, trang web không tự báo được khi đang đóng, nên ứng dụng xuất file lịch `.ics`:
  - Nhắc lịch âm: tạo sẵn các lần nhắc đã quy sang dương lịch, gồm 20 năm tới (lặp hằng năm) hoặc 24 tháng tới (lặp hằng tháng).
  - Mỗi lần nhắc có báo trước theo cài đặt.
  - Thêm file này vào ứng dụng Lịch của iPhone thì Lịch sẽ báo đúng giờ.

## Cấu trúc mã nguồn

```
app/src/main/java/vn/itdvietnam/lichamduong/
├── lunar/            Lõi lịch âm (thuật toán Hồ Ngọc Đức, múi giờ +7)
│   ├── LunarCalendar.kt   đổi âm ↔ dương, tháng nhuận, độ dài tháng
│   ├── CanChi.kt          can chi, giờ hoàng đạo, 24 tiết khí, pha trăng
│   ├── Holidays.kt        ngày lễ dương lịch và âm lịch
│   └── DayInfo.kt         gom toàn bộ thông tin một ngày
├── reminder/         Nhắc lịch
│   ├── Reminder.kt        mô hình dữ liệu
│   ├── Recurrence.kt      tính các lần nhắc kế tiếp (âm / dương, lặp)
│   ├── ReminderRepository.kt  lưu trên máy
│   ├── ReminderScheduler.kt   đặt báo bằng AlarmManager
│   └── ReminderReceivers.kt   hiện thông báo, đặt lại sau khởi động
├── settings/         Cài đặt giao diện, cỡ chữ
└── ui/               Giao diện Jetpack Compose
    ├── LichAppUi.kt       khung co giãn: 1 ngăn / 2 ngăn / thanh dọc
    ├── theme/Theme.kt     bảng màu sáng và tối AMOLED
    └── screens/           Tháng, Ngày, Đổi ngày, Nhắc lịch, Tạo nhắc, Cài đặt
```

## Thông số kỹ thuật

- Kotlin 2.0.21, Jetpack Compose (Material 3), Android Gradle Plugin 8.7.3.
- minSdk 26 (Android 8.0), targetSdk 35 (Android 15).
- Quyền sử dụng:
  - `POST_NOTIFICATIONS` để hiện thông báo nhắc lịch.
  - `RECEIVE_BOOT_COMPLETED` để đặt lại lịch báo sau khi khởi động máy.
  - `USE_EXACT_ALARM` để báo đúng giờ. Google Play cho phép ứng dụng lịch dùng quyền này. Khi điền mục *Khai báo quyền* trên Play Console, cần ghi rõ ứng dụng là lịch có chức năng nhắc sự kiện.
