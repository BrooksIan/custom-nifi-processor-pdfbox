# Custom NiFi Processor - JSON2PDF

A custom Apache NiFi processor that converts JSON input to formatted PDF documents using Apache PDFBox. Perfect for generating reports, invoices, or any document from structured data.

## Features

- **JSON to PDF Conversion**: Converts JSON input to formatted PDF documents
- **Configurable Formatting**: Customizable PDF title, font size, and key inclusion
- **Smart Layout**: Handles nested JSON objects and arrays with proper indentation
- **Multi-page Support**: Automatically creates new pages when content exceeds limits
- **Error Handling**: Robust error handling with proper relationship routing

## Processor Properties

| Property | Display Name | Description | Required | Default |
|----------|--------------|-------------|----------|---------|
| `PDF Title` | PDF Title | Title for the generated PDF document (supports Expression Language) | Yes | `JSON to PDF Conversion` |
| `Font Size` | Font Size | Font size for the PDF content | Yes | `12` |
| `Include Keys` | Include Keys | Whether to include JSON keys in the PDF output | Yes | `true` |

## Relationships

| Name | Description |
|------|-------------|
| `SUCCESS` | FlowFiles that were successfully converted to PDF |
| `FAILURE` | FlowFiles that failed conversion due to processing errors |

## Usage Examples

### Example 1: Basic JSON to PDF Conversion
**Input JSON:**
```json
{
  "name": "John Doe",
  "age": 30,
  "email": "john.doe@example.com",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY"
  }
}
```

**Configuration:**
- PDF Title: `User Profile Report`
- Font Size: `12`
- Include Keys: `true`

**Output:** A PDF document with formatted JSON content including keys and values.

### Example 2: Values-Only PDF
**Input JSON:**
```json
{
  "product": "Laptop",
  "price": 999.99,
  "inStock": true
}
```

**Configuration:**
- PDF Title: `Product Information`
- Font Size: `14`
- Include Keys: `false`

**Output:** A PDF document showing only the values without keys.

## Building the Processor

### Prerequisites
- Java 11 or higher (compatible with NiFi 1.28)
- Maven 3.6 or higher

### Build Instructions

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd custom-nifi-processor-pdfbox
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

3. **The JAR file will be created at:**
   ```
   target/pdfbox-processor-1.0.jar
   ```

## Installation in NiFi

1. **Copy the JAR file** to your NiFi `lib` directory:
   ```bash
   cp target/pdfbox-processor-1.0.jar $NIFI_HOME/lib/
   ```

2. **Restart NiFi** to load the new processor

3. **Find the processor** in the NiFi UI under the "org.example" group

## Development

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── example/
│   │           ├── SalutationProcessorV4.java
│   │           └── validators/
│   │               └── CustomValidators.java
│   └── resources/
└── test/
    └── java/
        └── org/
            └── example/
```

### Key Components

- **SalutationProcessorV4**: Main processor class extending `AbstractProcessor`
- **CustomValidators**: Custom validation logic for the BeforeOrAfter property
- **Property Descriptors**: Define processor configuration properties
- **Relationships**: Define success and failure paths for FlowFiles

### Dependencies

The processor uses the following key dependencies:
- Apache NiFi API (1.28.0)
- Jackson for JSON processing (2.15.2)
- Apache Commons IO (2.11.0)
- PDFBox (3.0.5) - included for future PDF processing capabilities

## Error Handling

The processor handles various error scenarios:

1. **Invalid BeforeOrAfter value**: FlowFile routed to FAILURE relationship
2. **JSON parsing errors**: Falls back to plain text processing
3. **Missing name field**: Uses "Unknown" as default name
4. **Processing exceptions**: FlowFile routed to FAILURE relationship with error logging

## Expression Language Support

The processor supports NiFi Expression Language for:
- **Salutation property**: Can reference FlowFile attributes
- **Name Field property**: Can reference FlowFile attributes

Example Expression Language usage:
- `${salutation.text}` - Use FlowFile attribute for salutation
- `${name.field}` - Use FlowFile attribute for field name

## Testing

The processor includes basic unit tests. To run tests:

```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the NiFi documentation
2. Review the processor logs in NiFi
3. Create an issue in this repository

## Version History

- **v1.0**: Initial release with basic salutation functionality
  - JSON and plain text processing
  - Configurable salutation position
  - Custom validation
  - Error handling and logging
