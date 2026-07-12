# Baemin Mini Backend

Backend MVP cho hệ thống đặt món Baemin Mini. Giai đoạn hiện tại hoàn thành phần Người 1 trong SRS: Core/Auth/DB.

## 1. Công nghệ sử dụng

- Java 21
- Spring Boot 4.1.0
- Spring WebMVC
- Spring Data JPA
- Spring Security
- JWT bằng JJWT
- MySQL 8+
- Flyway migration
- Swagger/OpenAPI bằng Springdoc
- Lombok

## 2. Cấu trúc chính

```text
src/main/java/com/baemin_mini
├── common              # ApiResponse và xử lý lỗi chung
├── common/exception    # Exception custom + GlobalExceptionHandler
├── config              # Security, JWT properties, OpenAPI
├── controller          # REST API controllers
├── domain              # BaseEntity, AuditableEntity
├── domain/entity       # Entity JPA
├── domain/enums        # Enum dùng trong domain
├── dto                 # Request/response DTO
├── repository          # Spring Data repositories
├── security            # JWT service/filter
├── service             # Service interfaces
└── service/impl        # Service implementations
```

## 3. Database

Database mặc định khi chạy dev:

```text
baemin_mini
```

Cấu hình mặc định nằm ở:

```text
src/main/resources/application-dev.yaml
```

Mặc định app dùng:

```text
DB_URL=jdbc:mysql://localhost:3306/baemin_mini?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=<cần tự set theo máy local>
```

Trên máy đang dùng ServBay/MySQL local, password là:

```powershell
$env:DB_PASSWORD='ServBay.dev'
```

## 4. Chạy project local

Windows PowerShell:

```powershell
cd D:\yoot\project-yoedu\Project\Baemin-Mini\Baemin_Mini
$env:DB_PASSWORD='ServBay.dev'
.\mvnw.cmd spring-boot:run
```

Nếu MySQL của bạn dùng password khác:

```powershell
$env:DB_PASSWORD='your_mysql_password'
```

Nếu muốn đổi database URL hoặc user:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/baemin_mini?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='your_mysql_password'
```

## 5. Flyway migration

Migration hiện có:

```text
V1__create_core_schema.sql
V2__seed_core_data.sql
```

Khi app start, Flyway tự tạo schema core và seed dữ liệu demo.

Các bảng core hiện tại:

- users
- roles
- user_roles
- refresh_tokens
- user_addresses

## 6. Tài khoản demo

Mật khẩu chung:

```text
123456
```

Tài khoản:

```text
admin / 123456       role ADMIN
customer01 / 123456  role CUSTOMER
merchant01 / 123456  role RESTAURANT
shipper01 / 123456   role SHIPPER
```

## 7. Swagger/OpenAPI

Sau khi chạy app, mở:

```text
http://localhost:8080/swagger-ui.html
```

Lấy access token bằng endpoint login, sau đó bấm nút Authorize trong Swagger và dán access token vào ô Bearer JWT.

Chỉ dán token, không cần gõ chữ `Bearer`.

## 8. Token flow

### Access token

Dùng để gọi API private:

```text
Authorization: Bearer <accessToken>
```

Ví dụ:

```text
GET /api/v1/users/me
GET /api/v1/users/addresses
```

### Refresh token

Chỉ dùng để lấy access token mới:

```text
POST /api/v1/auth/refresh
```

Refresh token được lưu trong database ở dạng hash, có `jti`, `expires_at`, `revoked_at`, và được rotate khi refresh.

## 9. API hiện có

Auth:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

User:

```text
GET /api/v1/users/me
```

Address book:

```text
GET    /api/v1/users/addresses
POST   /api/v1/users/addresses
PUT    /api/v1/users/addresses/{id}
DELETE /api/v1/users/addresses/{id}
```

Address API chỉ cho role CUSTOMER gọi bằng `@PreAuthorize("hasRole('CUSTOMER')")`.

## 10. Quy tắc địa chỉ mặc định

Hệ thống cho phép user không có địa chỉ mặc định.

- Nếu tạo/cập nhật địa chỉ với `isDefault = true`, các địa chỉ khác của user sẽ tự chuyển về `false`.
- Nếu tạo/cập nhật với `isDefault = false` hoặc không truyền `isDefault`, địa chỉ đó không phải default.
- Nếu xóa địa chỉ default, hệ thống không tự chọn default mới. User có thể tự set default lại bằng API update.

## 11. Test nhanh bằng PowerShell

Login:

```powershell
$baseUrl = 'http://localhost:8080'
$loginBody = @{ username = 'customer01'; password = '123456' } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/v1/auth/login" -ContentType 'application/json' -Body $loginBody
$accessToken = $login.data.accessToken
$headers = @{ Authorization = "Bearer $accessToken" }
```

Get current user:

```powershell
Invoke-RestMethod -Method Get -Uri "$baseUrl/api/v1/users/me" -Headers $headers
```

Get addresses:

```powershell
Invoke-RestMethod -Method Get -Uri "$baseUrl/api/v1/users/addresses" -Headers $headers
```

Create address:

```powershell
$body = @{
  title = 'Nhà riêng'
  receiverName = 'Nguyễn Văn Khách'
  receiverPhone = '0901000001'
  addressLine = '123 Nguyễn Trãi, Quận 1, TP.HCM'
  latitude = 10.77210900
  longitude = 106.69827800
  isDefault = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$baseUrl/api/v1/users/addresses" -Headers $headers -ContentType 'application/json; charset=utf-8' -Body $body
```

## 12. Ghi chú cho người làm tiếp

Người 2/3 có thể dùng auth hiện tại để bảo vệ các API order, restaurant, shipper, admin bằng `@PreAuthorize`.

Ví dụ:

```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('RESTAURANT')")
@PreAuthorize("hasRole('SHIPPER')")
```