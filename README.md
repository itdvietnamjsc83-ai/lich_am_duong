# Lịch Âm Dương – ứng dụng Android

Nhà phát triển: **Công ty cổ phần Công nghệ ITD Việt Nam**
Điện thoại / Zalo: 0988095867 · Email: vanthu.itdvn@gmail.com · Website: itdvietnam.com.vn
Địa chỉ: Số 1 Ngõ 30 đường Lý Sơn, Hà Nội

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
