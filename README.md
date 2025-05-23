# 🛒 ElectroMart - Electronics eCommerce Website

**ElectroMart** is a full-featured electronics eCommerce website developed as a final year academic project. The platform enables users to browse and purchase electronics, while admins can manage products, categories, users, and orders through a dedicated dashboard.

---

## 🔧 Tech Stack

- **Frontend**: HTML, CSS, JavaScript, Bootstrap  
- **Backend**: Spring Boot, Spring Data JPA, Spring Security
- **Database**: MySQL

---

## 👥 User Roles

### 👤 Guest
- Browse products by category
- View product details

### 👤 Registered User
- Register and log in
- View and update profile
- Add products to cart
- Place and cancel orders
- View order history

### 🛠️ Admin
- Manage product categories
- Add, edit, and delete products
- View and manage registered users
- View and update order status
- Access admin dashboard

---

## 📦 Key Features

- ✅ Category-wise product listing  
- ✅ Secure user registration and login  
- ✅ Shopping cart and checkout system  
- ✅ Order tracking and cancellation  
- ✅ Admin panel for managing products, users, and orders  
- ✅ Responsive UI using Bootstrap

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/santos021/electromart.git
cd electromart
```
### 2. Set Up MySQL Database
- Open MySQL and create a new database named electromart
- Update your Spring Boot application.properties with DB credentials
```bash
spring.datasource.url=jdbc:mysql://localhost:3306/electromart
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
```
### 3. Run the backend server
- Open the project in an IDE like IntelliJ or Eclipse
- Run the main Spring Boot application file
  
### 4.📄 License
- This project is created for educational purposes as part of a final year submission. Feel free to use or modify for learning.
  
### 🙌 Author
**Santos Kumar Biswal** <br>
Email-santosbiswal543@gmail.com
