# 🛒 Web TGDD - Đồ án môn Lập trình J2EE

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-orange?logo=openjdk"/>
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?logo=springboot"/>
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white"/>
  <img alt="Maven" src="https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white"/>
  <img alt="Thymeleaf" src="https://img.shields.io/badge/Thymeleaf-Template-005F0F?logo=thymeleaf"/>
</p>

Ứng dụng web mô phỏng hệ thống bán hàng **Thế Giới Di Động** được xây dựng bằng **Spring Boot (Java 17)** theo kiến trúc MVC, phục vụ môn **Lập trình J2EE**.

---

## 🎯 1) Mục tiêu dự án
- Thực hành phát triển ứng dụng web J2EE theo quy trình đầy đủ.
- Áp dụng mô hình nhiều lớp: **Controller - Service - Repository - Database**.
- Triển khai các chức năng chính của hệ thống thương mại điện tử:
  - 📦 Quản lý danh mục và sản phẩm
  - 🛍️ Giỏ hàng, đặt hàng, theo dõi đơn
  - 🔐 Đăng ký/đăng nhập, phân quyền người dùng
  - 💳 Tích hợp thanh toán MoMo (sandbox)
  - ✉️ Gửi OTP qua email
  - 🕸️ Cào dữ liệu sản phẩm/danh mục từ website nguồn

---

## 🧰 2) Công nghệ sử dụng
- ☕ **Java 17**
- 🍃 **Spring Boot 4**
- 🌐 **Spring MVC + Thymeleaf**
- 🗄️ **Spring Data JPA (Hibernate)**
- 🛡️ **Spring Security**
- 🐬 **MySQL**
- 🔧 **Maven**
- 🧲 **Jsoup** (cào dữ liệu)

---

## 🗂️ 3) Cấu trúc dự án
```text
src/main/java/.../ngonguyentiendat
├── config/         # Cấu hình Security, Jackson, Web, Mail, MoMo...
├── controller/     # Controller giao diện web + REST API
├── dto/            # DTO cho form, API, checkout, loyalty...
├── model/          # Entity JPA: Product, Category, Order, AppUser...
├── repository/     # Interface truy cập dữ liệu
├── security/       # Bảo mật, UserDetailsService...
└── service/        # Xử lý nghiệp vụ

src/main/resources
├── templates/      # Giao diện Thymeleaf
├── static/         # Ảnh, css/js, uploads
└── application.properties
```

---

## ✨ 4) Chức năng chính
### 👤 Người dùng
- 🏠 Xem trang chủ, danh sách sản phẩm, chi tiết sản phẩm
- ➕ Thêm sản phẩm vào giỏ hàng
- ✅ Thanh toán/đặt đơn
- 🔑 Đăng ký, đăng nhập
- ✉️ Nhận OTP email cho các luồng xác thực
- 🎁 Tích điểm và đổi điểm loyalty

### 👨‍💼 Quản trị viên
- 🗃️ Quản lý danh mục
- 📱 Quản lý sản phẩm
- 📋 Quản lý đơn hàng
- 🧪 Theo dõi và test API nội bộ phục vụ demo

### 🚀 Chức năng mở rộng
- 🕷️ Cào dữ liệu danh mục/sản phẩm bằng Jsoup để import nhanh dữ liệu mẫu
- 💳 Tích hợp cổng thanh toán **MoMo sandbox**

---

## 🖥️ 5) Yêu cầu môi trường
- JDK 17+
- Maven 3.9+
- MySQL 8+
- IDE khuyến nghị: IntelliJ IDEA hoặc VS Code (Java Extension Pack)

---

## ⚙️ 6) Hướng dẫn chạy dự án
### Bước 1: Clone project
```bash
git clone <repo-url>
cd Web_TGDD-Spring_Boot/ngonguyentiendat
```

### Bước 2: Tạo database MySQL
```sql
CREATE DATABASE WebTGDD CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 3: Cấu hình `application.properties`
Cập nhật theo môi trường máy:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.mail.*` (SMTP Gmail)
- `momo.*` (nếu dùng thanh toán MoMo)

> 🔒 Khuyến nghị: chuyển thông tin nhạy cảm (DB password, mail password, secret key) sang **biến môi trường** hoặc file private không commit lên Git.

### Bước 4: Chạy ứng dụng
```bash
./mvnw spring-boot:run
```
Hoặc trên Windows:
```bash
mvnw.cmd spring-boot:run
```

### Bước 5: Truy cập
- 🌍 Trang web: `http://localhost:8080`
- 🔗 Một số trang chức năng:
  - `http://localhost:8080/products`
  - `http://localhost:8080/categories`
  - `http://localhost:8080/admin`

---

## 👥 7) Tài khoản mặc định (seed)
Dự án có cấu hình tài khoản mặc định trong `application.properties`:
- Admin
- Manager
- User

Bạn có thể đổi email/mật khẩu mặc định trước khi chạy lần đầu.

---

## 🌱 8) Hướng phát triển thêm
- 🔎 Bổ sung lọc/tìm kiếm nâng cao
- ⚡ Tích hợp Redis cho session/cart
- ✅ Viết test cho service/controller
- 🐳 Triển khai Docker + CI/CD
- 🧩 Tách cấu hình theo môi trường `dev/staging/prod`

---

## 👨‍🎓 9) Tác giả
- Sinh viên thực hiện: **Ngô Nguyễn Tiến Đạt**
- Môn học: **Lập trình J2EE**
