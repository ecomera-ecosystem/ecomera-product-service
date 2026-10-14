# Batch Import Documentation

## Overview
This section documents the product CSV import feature, including API endpoints, CSV format specification, and usage examples.

## Documentation Index

| Document | Description |
|----------|-------------|
| [CSV Format](./csv-format.md) | Column definitions, types, constraints, and examples |
| [Launch Import](./import-launch.md) | How to upload CSV and launch import job |
| [Check Status](./import-status.md) | Polling import status, pagination, error download |

## Quick Start

1. **Prepare CSV** - Follow [CSV Format](./csv-format.md)
2. **Launch Import** - POST to `/api/v1/admin/imports` with CSV file
3. **Poll Status** - GET `/api/v1/admin/imports/{importId}` until `COMPLETED` or `COMPLETED_WITH_ERRORS`
4. **Download Errors** - If `COMPLETED_WITH_ERRORS`, GET `/api/v1/admin/imports/{id}/errors`

## Example Flow

```bash
# 1. Launch import
curl -X POST http://localhost:8082/api/v1/admin/imports \
  -H "X-User-Id: admin" \
  -F "file=@products.csv"

# Response: {"importId": "uuid", "statusUrl": "/api/v1/admin/imports/uuid"}

# 2. Poll status
curl -H "X-User-Id: admin" http://localhost:8082/api/v1/admin/imports/{importId}

# 3. If errors, download report
curl -H "X-User-Id: admin" http://localhost:8082/api/v1/admin/imports/{importId}/errors
```

## Example Files

See [examples/](./examples/) for sample CSV files:
- `valid-products.csv` - 5 valid products
- `update-existing.csv` - Mix of updates and new products
- `invalid-price.csv` - Invalid price formats
- `invalid-stock.csv` - Invalid stock values
- `unknown-category.csv` - Non-existent category IDs
- `duplicate-sku-in-csv.csv` - Duplicate SKUs in same file