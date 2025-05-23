# 🛒 Price Comparator API

A RESTful backend application for comparing prices, optimizing shopping baskets, tracking discounts, and monitoring trends across multiple retail stores.

> Built with **Java 21**, **Spring Boot 3**, **Maven**, and **OpenAPI (Swagger UI)**

---

## 🚀 How to Run

1. Clone this repository
2. Open in your IDE and run `PriceComparatorApplication.java`
3. Access Swagger UI at:  
   👉 `http://localhost:8080/swagger-ui.html`

---

## 📂 Project Structure

```plaintext
src/
├── controller/
├── model/
├── dto/
├── repository/
├── service/
├── util/
└── resources/data/csv/
```

---

## 📊 API Overview

### 🔍 Products

| Method | Endpoint                             | Description                              |
|--------|--------------------------------------|------------------------------------------|
| `GET`  | `/api/products/all`                  | List all products                        |
| `GET`  | `/api/products/store/{store}`        | Products from a specific store           |
| `GET`  | `/api/products/by-category`          | Filter products by category and date     |
| `GET`  | `/api/products/under-price`          | Filter products by max price             |
| `GET`  | `/api/products/search`               | Search by product name fragment          |
| `GET`  | `/api/products/brands`               | List unique brands                       |
| `GET`  | `/api/products/by-brand`             | Products from a specific brand on a date |
| `GET`  | `/api/products/compare`              | Compare two products by unit price       |
| `GET`  | `/api/products/sorted-by-unit-price` | Sort products by price per unit          |
| `GET`  | `/api/products/no-discount`          | Products without any discount on a date  |
| `GET`  | `/api/products/cheapest-by-name`     | Find cheapest product by name/date       |
| `GET`  | `/api/products/price-history`        | Get historical prices of a product       |
| `GET`  | `/api/products/substitutes`          | Suggest alternative products             |
| `GET`  | `/api/products/multi-store`          | In which stores a product is available   |
| `GET`  | `/api/products/from-csv`             | Load products from a sample CSV          |
| `GET`  | `/api/products/sample`               | Return a hardcoded test product          |

---

### 💸 Discounts

| Method | Endpoint                                     | Description                          |
|--------|----------------------------------------------|--------------------------------------|
| `GET`  | `/api/discounts/all`                         | All loaded discounts                 |
| `GET`  | `/api/discounts/store/{store}`               | Discounts from a store               |
| `GET`  | `/api/discounts/from-csv`                    | Load discounts from a CSV            |
| `GET`  | `/api/discounts/best`                        | Top 10 current discounts             |
| `GET`  | `/api/discounts/new?date=YYYY-MM-DD`         | Discounts that start on a given date |
| `GET`  | `/api/discounts/above`                       | Discounts greater than a percentage  |
| `GET`  | `/api/discounts/expiring`                    | Discounts that expire on a given day |
| `GET`  | `/api/discounts/top?limit=5&date=YYYY-MM-DD` | Top N discounts (percentage)         |

---

### 🧾 Basket & Optimization

| Method | Endpoint                   | Description                                  |
|--------|----------------------------|----------------------------------------------|
| `POST` | `/api/basket/optimize`     | Pick cheapest source for each item           |
| `POST` | `/api/basket/invoice`      | Detailed receipt with savings                |
| `POST` | `/api/basket/by-budget`    | Select best products under a budget          |
| `POST` | `/api/basket/compare-days` | Compare total basket cost on different dates |

---

### 📊 Statistics

| Method | Endpoint                          | Description                        |
|--------|-----------------------------------|------------------------------------|
| `GET`  | `/api/stats/category-price-trend` | Price trend per category/store     |
| `GET`  | `/api/stats/store-daily-index`    | Average daily price index by store |

---

### 🔔 Price Alerts

| Method | Endpoint                                | Description                              |
|--------|-----------------------------------------|------------------------------------------|
| `POST` | `/api/alerts`                           | Save a new alert (product + price limit) |
| `GET`  | `/api/alerts/triggered?date=YYYY-MM-DD` | See which alerts were triggered          |

---

## 📥 Sample JSON: Optimize Basket

### ✅ Request

**POST** `/api/basket/optimize`

Request body:
```json
{
  "productNames": ["lapte zuzu", "ulei floarea-soarelui"],
  "date": "2025-05-08"
}
```
Response body:
```json
{
"items": [
  {
  "productName": "lapte zuzu",
  "store": "lidl",
  "finalPrice": 7.20
  }
],
"totalPrice": 15.70,
"suggestions": []
}
```

## ⚙️ Test it in Swagger UI
Visit [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


## 🛠️ Technologies Used

- Java 21
- Spring Boot 3.x
- Spring Web / Validation
- OpenCSV
- SpringDoc OpenAPI (Swagger)
- Maven

## 📄 Sample CSVs

Place your CSVs in:

```plaintext
src/main/resources/data/csv/
```

Example file names:

- `lidl_2025-05-08.csv`
- `lidl_discounts_2025-05-08.csv`


## 👤 Author

This project was developed as part of the **Accesa Java Internship 2025** challenge.
