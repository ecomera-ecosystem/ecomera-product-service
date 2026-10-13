# Product Import CSV Format

## Overview
The product import feature allows administrators to bulk import or update products via CSV file upload. The import process is asynchronous and processes products in chunks with validation and error handling.

## CSV Schema

### Required Columns
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `sku` | String (max 64) | Required, unique | Product lookup key (create/update) |
| `title` | String (max 255) | Required | Product title |
| `price` | Decimal(10,2) | Required, >= 0.01 | Price in USD |
| `stock` | Integer | Required, >= 0 | Available stock quantity |
| `category_id` | UUID | Required, must exist | Foreign key to category |

### Optional Columns
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `description` | Text | Optional | Product description |
| `color` | String (max 50) | Optional | Product color |
| `size` | String (max 20) | Optional | Product size |
| `image_url` | String (max 255) | Optional | Product image URL |

### Rejected Columns (Not Imported)
The following columns will be rejected if present in the CSV:
- `id`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`
- `rating`

## Business Rules

| Rule | Behavior |
|------|----------|
| **New SKU** | Creates a new product |
| **Existing SKU** | Updates existing product (mutable fields only) |
| **Missing SKU in CSV** | Product is NOT deleted (no delete behavior) |
| **Duplicate SKU in CSV** | Row rejected, error logged |
| **Invalid `category_id`** | Row rejected, error logged |
| **Invalid price/stock format** | Row rejected, error logged |
| **Negative stock** | Row rejected |
| **File-level errors** | Entire import fails before any writes |
| **Row-level errors** | Valid rows continue, invalid rows rejected |

## Failure Behavior

| Error Type | Behavior |
|------------|----------|
| **File-level** (malformed CSV, missing headers, encoding) | Import fails before any database writes |
| **Row-level** (invalid data, unknown category, duplicate SKU) | Row rejected, valid rows continue, import completes with `COMPLETED_WITH_ERRORS` status |

## Example: Valid CSV

```csv
sku,title,description,price,stock,category_id,color,size,image_url
SKU-001,MacBook Pro M3,Apple M3 chip 14-inch,1999.99,25,a1000000-0000-0000-0000-000000000001,Space Gray,,https://example.com/macbook.jpg
SKU-002,iPhone 15 Pro,Apple iPhone 15 Pro,1299.99,50,a1000000-0000-0000-0000-000000000001,Natural Titanium,,https://example.com/iphone.jpg
SKU-003,Classic Leather Jacket,Genuine leather jacket,249.99,20,a1000000-0000-0000-0000-000000000002,Brown,M,https://example.com/jacket.jpg
```

## Example: Invalid Rows (Will Be Rejected)

```csv
sku,title,description,price,stock,category_id,color,size
SKU-BAD-001,Bad Price,Invalid price,not-a-price,10,a1000000-0000-0000-0000-000000000001,Red,M
SKU-BAD-002,Bad Stock,Negative stock,29.99,-5,a1000000-0000-0000-0000-000000000001,Blue,L
SKU-BAD-003,Unknown Category,Unknown category,29.99,10,ffffffff-ffff-ffff-ffff-ffffffffffff,Red,M
```

## Category Reference

Use existing category IDs from the database. Current seed categories:

| Category | UUID |
|----------|------|
| Electronics | `a1000000-0000-0000-0000-000000000001` |
| Clothing | `a1000000-0000-0000-0000-000000000002` |
| Jewelry & Accessories | `a1000000-0000-0000-0000-000000000003` |
| Home & Living | `a1000000-0000-0000-0000-000000000004` |
| Sports & Outdoors | `a1000000-0000-0000-0000-000000000005` |
| Beauty & Personal Care | `a1000000-0000-0000-0000-000000000006` |
| Books & Stationery | `a1000000-0000-0000-0000-000000000007` |
| Toys & Games | `a1000000-0000-0000-0000-000000000008` |