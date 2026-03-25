# Chức năng cào dữ liệu đã thêm

## 1) Cào danh mục
- Vào `/categories`
- Dán URL nguồn, ví dụ `https://www.thegioididong.com`
- Bấm **Cào danh mục**
- Hệ thống sẽ thử import các nhóm phổ biến như điện thoại, laptop, tai nghe, đồng hồ, sạc, chuột, bàn phím...

## 2) Cào hàng loạt sản phẩm
- Vào `/products`
- Dán link trang danh mục, ví dụ:
  - `https://www.thegioididong.com/dtdd`
  - `https://www.thegioididong.com/tai-nghe`
- Chọn danh mục đích trong DB
- Chọn số lượng muốn cào
- Bấm **Cào & import**

## Ghi chú
- Code dùng `jsoup`, vì vậy cần Maven tải dependency lần đầu.
- Nếu IDE vẫn chưa nhận dependency, hãy bấm **Reload Maven**.
- Nếu website nguồn đổi HTML, selector có thể cần chỉnh thêm.
