# 🛒 Price Comparator API (Java, Spring Boot)

Aplicatie backend pentru compararea prețurilor între supermarketuri. Proiect realizat în cadrul challenge-ului Accesa Java Internship 2025.

---

## ✅ Tehnologii folosite

- Java 21
- Spring Boot 3
- Maven
- OpenCSV
- Lombok

---

## 🚀 Funcționalități implementate

### 🔎 Produse & Reduceri

- ✅ Citire produse și reduceri din fișiere CSV (Lidl, Kaufland, Profi)
- ✅ Încărcare automată în memorie la pornirea aplicației
- ✅ Returnare produse și reduceri per magazin și per zi

### 📉 Reduceri

- ✅ Reduceri active (`/api/discounts/best`)
- ✅ Reduceri noi (start = azi) (`/api/discounts/new?date=...`)

### 🧠 Comparator

- ✅ Căutare cel mai ieftin produs (cu reducere) după nume + dată (`/api/products/cheapest-by-name`)
- ✅ Istoric de prețuri pentru un produs (`/api/products/price-history?name=...`)
- ✅ Recomandări substituibile (același tip/unitate, preț/unit comparabil) (`/api/products/substitutes`)

### 🛒 Coș optimizat

- ✅ Endpoint: `/api/products/basket/optimize`
- ✅ Primește: listă produse + dată
- ✅ Returnează:
    - Magazin optim pentru fiecare produs
    - Preț final total
    - Sugestii mai ieftine: produse echivalente din alt magazin cu economie estimată

Exemplu request:

```json
POST /api/products/basket/optimize
{
  "productNames": ["lapte zuzu", "ulei floarea-soarelui"],
  "date": "2025-05-08"
}
