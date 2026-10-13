# Check Import Status

## Endpoint
```
GET /api/v1/admin/imports/{importId}
```

## Authentication
Requires admin role. Include `X-User-Id` header with the admin user identifier.

## Response

### Success (200 OK)
```json
{
  "importId": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "products.csv",
  "status": "COMPLETED",
  "totalRows": 100,
  "createdCount": 85,
  "updatedCount": 10,
  "rejectedCount": 5,
  "errorMessage": null,
  "startedAt": "2026-10-10T15:30:00",
  "completedAt": "2026-10-10T15:30:45"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `importId` | UUID | Unique import identifier |
| `filename` | String | Original CSV filename |
| `status` | Enum | `PENDING`, `RUNNING`, `COMPLETED`, `COMPLETED_WITH_ERRORS`, `FAILED` |
| `totalRows` | Integer | Total rows in CSV (excluding header) |
| `createdCount` | Integer | New products created |
| `updatedCount` | Integer | Existing products updated |
| `rejectedCount` | Integer | Rows rejected due to errors |
| `errorMessage` | String | Error message if status is FAILED |
| `startedAt` | ISO 8601 | When import started processing |
| `completedAt` | ISO 8601 | When import finished (null if running) |

### Status Values

| Status | Description |
|--------|-------------|
| `PENDING` | Import record created, job not yet started |
| `RUNNING` | Job is currently processing |
| `COMPLETED` | All rows processed successfully |
| `COMPLETED_WITH_ERRORS` | Some rows rejected, but valid rows processed |
| `FAILED` | Job failed (file-level error) |

### Not Found (404)
```json
{
  "timestamp": "2026-10-10T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Import not found",
  "path": "/api/v1/admin/imports/invalid-id"
}
```

### Forbidden (403)
Returned if user lacks admin role.

## Polling Strategy

```javascript
async function pollImportStatus(importId, intervalMs = 2000) {
  while (true) {
    const response = await fetch(`/api/v1/admin/imports/${importId}`, {
      headers: { 'X-User-Id': 'admin' }
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get status: ${response.status}`);
    }
    
    const data = await response.json();
    
    if (data.status === 'COMPLETED' || data.status === 'COMPLETED_WITH_ERRORS' || data.status === 'FAILED') {
      return data;
    }
    
    await new Promise(resolve => setTimeout(resolve, 2000));
  }
}
```

## List All Imports (Paginated)

### Endpoint
```
GET /api/v1/admin/imports?page=0&size=20
```

### Response (200 OK)
```json
{
  "content": [
    {
      "importId": "550e8400-e29b-41d4-a716-446655440000",
      "filename": "products.csv",
      "status": "COMPLETED",
      "totalRows": 100,
      "createdCount": 85,
      "updatedCount": 10,
      "rejectedCount": 5,
      "startedAt": "2026-10-10T15:30:00",
      "completedAt": "2026-10-10T15:30:45"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": false, "unsorted": true, "empty": true }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 20,
  "number": 0,
  "sort": { "sorted": false, "unsorted": true, "empty": true },
  "numberOfElements": 1,
  "empty": false
}
```

### Query Parameters
| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | 0 | Page number (0-based) |
| `size` | 20 | Page size (max 100) |

## Error Download

### Endpoint
```
GET /api/v1/admin/imports/{importId}/errors
```

### Response (200 OK)
CSV file download with headers:
```
line_number,sku,title,description,price,stock,category_id,color,size,error_message
12,SKU-BAD-001,Bad Price,Invalid price,not-a-price,10,a1000000-0000-0000-0000-000000000001,Red,M,"Invalid price format: not-a-price"
```

Only available when import status is `COMPLETED_WITH_ERRORS` or `FAILED`.