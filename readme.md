# Membuat Rest Service
Tutorial ini merupakan lanjutan dari tutorial [Spring Hibernate](https://github.com/bippo/spring-hibernate). Pada tutorial ini, aplikasi utama yang kita gunakan untuk menguji Service akan kita gantikan dengan Rest Service.

```
├── pom.xml
├── readme.md
└── src
    └── main
        ├── java
        │   └── bippotraining
        │       ├── Application.java
        │       ├── HibernateXMLConf.java
        │       ├── controller
        │       │   └── EmployeeController.java
        │       ├── dao
        │       │   ├── EmployeeDAO.java
        │       │   └── EmployeeDAOImpl.java
        │       ├── model
        │       │   └── Employee.java
        │       └── service
        │           ├── EmployeeService.java
        │           └── EmployeeServiceImpl.java
        └── resources
            ├── application.properties
            └── hibernate5Configuration.xml
```
## 1. Pengenalan HTTP
Untuk versi lengkap tentang HTTP basic, silakan lihat referensi ini [https://learn.onemonth.com/understanding-http-basics/](https://learn.onemonth.com/understanding-http-basics/).

HTTP adalah protokol (cara berkomunikasi) antara web server dan browser. Pada HTTP terdapat proses request dan response. Berikut adalah contoh ketika kita browser kita melakukan request ke https://www.google.com.
```
GET /memperluas-pasar-dengan-kemudahan-bertransaksi-melalui-web/?versionId=1&final=true HTTP/1.1
Host: blog.bippo.co.id
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3
```
Beberapa hal yang perlu kita perhatikan dari request di atas:
1. **GET** umumnya digunakan browser untuk menerima dokumen, gambar atau resource lain. Setelah GET adalah halaman yang di-request, dalam hal ini adalah `/memperluas-pasar-dengan-kemudahan-bertransaksi-melalui-web/`, semua setelah tanda **?** (`?versionId=1&final=true`) adalah parameter dalam bentuk pasangan key/value yang dipisahkan tanda **=**. Pada contoh di atas, terdapat pasangan `versionId=1` dan `final=true` Kemudian yang terakhir adalah versi protokol, dalam hal ini 1.1.
2. **Host** adalah server tempat kita request
3. **Accept** menandakan jenis resource yang dapat diterima oleh browser.

Parameter setelah GET (resource yang ingin kita ambil) dicatat di dalam history browser, untuk itu jangan pernah mengirimkan informasi sensitif (misalkan password) di dalam parameter GET (misal `?userId=john&password=secret``.

### POST
Selain GET, terdapat jenis request POST yang secara protokol sama dengan request GET tetap parameter diletakkan di request body, sehingga tidak tercatat di dalam history browser. Pada contoh berikut `Content-Type: application/x-www-form-urlencoded` menandakan bahwa parameter yang dikirimkan ke server dalam bentuk pasangan key/value yang dipisahkan dengan tanda `=` dan antar parameter dipisahkan dengan tanda `&`. Pada aplikasi kita nanti, kita akan lebih banyak menggunakan `Content-Type: application/json`.
```
POST /login HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Content-Length: 27

userId=john&password=secret
```
Untuk method yang lain, seperti PUT bekerja mirip dengan HTTP POST.

### HTTP Response
```
$ telnet www.w3.org 80
Trying 128.30.52.100...
Connected to www.w3.org.
Escape character is '^]'.
GET / HTTP/1.0

HTTP/1.1 200 OK
Date: Thu, 27 Jun 2019 01:19:53 GMT
Content-Location: Home.html
Vary: negotiate,accept,Accept-Encoding,upgrade-insecure-requests
TCN: choice
Last-Modified: Wed, 26 Jun 2019 21:15:10 GMT
ETag: "9a8e-58c408a525380;89-3f26bd17a2f00"
Accept-Ranges: bytes
Content-Length: 39566
Cache-Control: max-age=600
Expires: Thu, 27 Jun 2019 01:29:53 GMT
Connection: close
Content-Type: text/html; charset=utf-8

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<!-- Generated from data/head-home.php, ../../smarty/{head.tpl} -->
<head>
<title>World Wide Web Consortium (W3C)</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="Help" href="/Help/" />
<link rel="stylesheet" href="/2008/site/css/minimum" type="text/css" media="all" />
<style type="text/css" media="print, screen and (min-width: 481px)">
/*<![CDATA[*/
@import url("/2008/site/css/advanced");
....
```
Pada contoh di atas, kita melakukan request ke server www.w3.org/ port 80 (port untuk http, untuk https port 443) dengan menggunakan telnet (`telnet www.w3.org 80`). Setelah itu kita memasukkan `GET / HTTP/1.0` yang menandakan kita membutuhkan resource `/`. Pada bagian setelah `GET / HTTP/1.0` adalah response yang diberikan oleh web server.
Hal yang perlu diperhatikan dari reponse tersebut adalah:
1. `HTTP/1.1 200 OK` berarti server mengembalikan _status code_ **200 OK**,
2. `Content-Type: text/html; charset=utf-8` jenis konten yang dikembalikan oleh server.
3. Bagian `<!DOCTYPE html PUBLIC ...` adalah content body yang dikembalikan oleh server.

Berikut adalah kemungkinan status yang dikembalikan server:
> 1XX: Informational
> 2XX: Success
> 3XX: Redirection
> 4XX: Client Error
> 5XX: Server Error

## 2. Konfigurasi file maven pom.xml
File pom.xml kita berisi library yang dibutuhkan untuk project ini, yaitu: spring-boot-starter-web, hibernate, spring-boot-starter-data-jpa, driver postgresql dan tomcat-dbcp.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>bippo-training</groupId>
    <artifactId>boot-rest</artifactId>
    <version>1.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.4.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.4.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>9.4-1200-jdbc41</version>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>tomcat-dbcp</artifactId>
            <version>9.0.21</version>
        </dependency>
    </dependencies>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

```

## 3. Konfigurasi Aplikasi
Pada application.properties, beri komentar pada konfigurasi yang ada.
```properties
#spring.main.web-application-type=NONE
```

## 4. Controller
Kelas Controller berfungsi menyediakan layanan rest service. Kelas Controller yang kita buat yaitu `bippotraining.controller.EmployeeController` dengan isi sebagai berikut:
```java
package bippotraining.controller;

import bippotraining.model.Employee;
import bippotraining.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping(path = "/", produces = "application/json")
    public List<Employee> getAllEmployee() {
        return employeeService.getEmployee();
    }

    @PostMapping(path = "/", consumes = "application/json", produces = "application/json")
    public void addEmployee(@RequestBody Employee employee) {
        employeeService.addEmployee(employee);
    }

    @PutMapping(path = "/", consumes = "application/json", produces = "application/json")
    public void updateEmployee(@RequestBody Employee employee) {
        employeeService.updateEemployee(employee);
    }

    @DeleteMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public void deleteEmployee(@PathVariable("id") String id) {
        employeeService.removeEmployee(id);
    }

}
```

### 1 @RestController
Anotasi ini menandakan bahwa kelas kita adalah Controller

### 2. @RequestMapping("/employee")
Anotasi ini menandakan semua method di dalam kelas kita dapat diakses dari path dengan awalan `/employee`

### 3. @GetMapping(path =  "/", produces =  "application/json")
Anotasi ini menandakan rest kita dapat diakses dengan http method `GET` dan alamat `/employee/` (gabungan dari `@RequestMapping("/employee")` dan `@GetMapping(path =  "/", ...)`.
Method yang menggunakan anotasi ini:
```java
    @GetMapping(path = "/", produces = "application/json")
    public List<Employee> getAllEmployee() {
        return employeeService.getEmployee();
    }
```
Method tersebut akan menghasilkan List of Employee yang akan dikonversi ke dalam format JSON.

### 4. @PostMapping(path = "/", consumes = "application/json", produces = "application/json")
Anotasi ini menandakan bahwa rest service tersebut dapat diakses dengan method POST menerima parameter dalam bentuk JSON dan mengembalikan juga dalam bentuk JSON.

### 5. @DeleteMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
Anotasi ini menandakan bahwa rest service tersebut dapat diakses dengan method DELETE. Perhatikan parameter `/{id}` pada path yang menandakan bahwa URL tersebut dapat diakses dengan memasukkan id dari employee, misalkan kita ingin menghapus employee dengan id 01 maka kita dapat request: `DELETE /01 HTTP/1.1`.

## 5. Aplikasi Utama
Aplikasi utama kita lebih sederhana dibandingkan dengan aplikasi sebelumnya:
```java
package bippotraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude= HibernateJpaAutoConfiguration.class)
// https://stackoverflow.com/questions/38627491/spring-4-hibernate-5-org-springframework-orm-jpa-entitymanagerholder-cannot/38637273#38637273
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

}

```

## 6. Pengujian
Proses pengujian kita akan menggunakan curl. Parameter dari curl yang kita gunakan:
1. **-v** untuk menampilkan HTTP request dan response header
2. **-H** untuk mengirim HTTP Header
3. **-d** untuk mengirim data/parameter

sehingga command curl kita sebagai berikut:
`$curl -v [GET|POST|PUT|DELETE] -H 'header' -d 'data' host/resource`

### 1. POST /employee/
Kita akan mengirimkan data object Employee dengan property:
```
employeeId:05,
employeeName:John Doe 03,
employeeEmail:john@playground.com,
employeeAddress:San Jose
```
Sesuai dengan spesifikasi rest, kita maka kita perlu mengubah data tersebut menjadi object JSON, maka representasi JSON menjadi:
```javascript
{
  "employeeId": "05",
  "employeeName": "John Doe 03",
  "employeeEmail": "john@playground.com",
  "employeeAddress": "San Jose"
}
```

```bash
$ curl -v POST -H "Content-Type: application/json" -d '{"employeeId":"05","employeeName":"John Doe 03","employeeEmail":"john@playground.com","employeeAddress":"San Jose"}' localhost:8080/employee/
* Rebuilt URL to: POST/
* Could not resolve host: POST
* Closing connection 0
curl: (6) Could not resolve host: POST
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#1)
> POST /employee/ HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 115
>
* upload completely sent off: 115 out of 115 bytes
< HTTP/1.1 200
< Content-Length: 0
< Date: Thu, 27 Jun 2019 02:08:29 GMT
<
* Connection #1 to host localhost left intact
```

Proses POST data sukses ditandakan dengan status code 200 pada bagian `< HTTP/1.1 200 `.

### 2. GET /employee/
Untuk melihat bahwa proses POST kita berhasil, maka kita akses `GET /employee/` dari curl.
```bash
$ curl -v http://localhost:8080/employee/
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /employee/ HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Thu, 27 Jun 2019 01:54:59 GMT
<
* Connection #0 to host localhost left intact
[{"employeeId":"03","employeeName":"John Doe 03","employeeEmail":"john@playground.com","employeeAddress":"San Jose"}]
```

Response yang dihasilkan dapat dilihat pada bagian terakhir, yaitu representasi JSON dari List of Employee.
```JSON
[
  {
    "employeeId": "03",
    "employeeName": "John Doe 03",
    "employeeEmail": "john@playground.com",
    "employeeAddress": "San Jose"
  },
  {
    "employeeId": "01",
    "employeeName": "John Doe 03",
    "employeeEmail": "john@playground.com",
    "employeeAddress": "San Jose"
  }
]
```