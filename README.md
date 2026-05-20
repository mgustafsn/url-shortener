# URL Shortener

Serverless URL shortener built with Java 21 and AWS. Paste a long URL and get a short URL back.

## Architecture

- **AWS Lambda**
- **API Gateway**
- **DynamoDB**

### Shorten a URL
```bash
curl -X POST https://your-api-url/Prod/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/some/long/url"}'
```

Response:
```json
{
  "shortUrl": "https://your-api-url/Prod/aB3xQ7",
  "code": "aB3xQ7",
  "originalUrl": "https://example.com/some/long/url",
  "expiresAt": "never"
}
```

### Optional fields
```json
{
  "url": "https://example.com",
  "customSlug": "my-link",
  "expiresInDays": 30
}
```

### Redirect
```bash
curl -L https://your-api-url/Prod/aB3xQ7
```
## Tech Stack

- Java 21
- AWS Lambda
- AWS API Gateway
- AWS DynamoDB
- AWS SAM
- Maven


## Deploy

Prerequisites: Java 21, Maven, AWS CLI, AWS SAM CLI

```bash
mvn package -DskipTests
sam deploy --guided
```

## Author

Marcus Gustafsson
