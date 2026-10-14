# Launch Product Import

## Endpoint
```
POST /api/v1/admin/imports
```

## Authentication
Requires admin role. Include `X-User-Id` header with the admin user identifier.

## Request

### Headers
| Header | Required | Description |
|--------|----------|-------------|
| `X-User-Id` | Yes | Admin user identifier (passed via API Gateway) |
| `Content-Type` | Yes | `multipart/form-data` |

### Request Body
Multipart form data with a single file part:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | File | Yes | CSV file to import |

### Example Request (cURL)
```bash
curl -X POST http://localhost:8082/api/v1/admin/imports \
  -H "X-User-Id: admin" \
  -F "file=@products.csv"
```

## Response

### Success (202 Accepted)
```json
{
  "importId": "550e8400-e29b-41d4-a716-446655440000",
  "statusUrl": "/api/v1/admin/imports/550e8400-e29b-41d4-a716-446655440000",
  "launchedAt": "2026-10-10T15:30:00"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `importId` | UUID | Unique identifier for the import job |
| `statusUrl` | String | URL to poll for import status |
| `launchedAt` | ISO 8601 datetime | When the import was launched |

### Error Responses

| Status | Condition |
|--------|-----------|
| 400 | File is empty, not CSV, or invalid content type |
| 401 | Missing or invalid authentication |
| 403 | Insufficient permissions (not admin) |
| 500 | Internal server error |

## Behavior

1. **File Validation**: Validates file is not empty and is CSV format
2. **Checksum**: Calculates SHA-256 checksum of file for integrity
3. **Import Record**: Creates `product_import` record with `PENDING` status
3. **Async Launch**: Returns immediately (202), job runs asynchronously
4. **Status**: Job status updates to `RUNNING` after launch

## CSV Requirements
- Must include header row
- Must follow [CSV Format Specification](./csv-format.md)
- Maximum file size: 10MB (configurable)
- UTF-8 encoding required

## Example Usage (JavaScript/Fetch)

```javascript
const formData = new FormData();
formData.append('file', csvFile);

const response = await fetch('http://localhost:8082/api/v1/admin/imports', {
  method: 'POST',
  headers: {
    'X-User-Id': 'admin-user-id'
  },
  body: formData
});

if (response.ok) {
  const result = await response.json();
  console.log('Import launched:', result.importId);
  // Poll status at result.statusUrl
}
```