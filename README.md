# JSON2PDF NiFi Processor

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://openjdk.java.net/)
[![NiFi](https://img.shields.io/badge/NiFi-1.28-green.svg)](https://nifi.apache.org/)
[![PDFBox](https://img.shields.io/badge/PDFBox-3.0.5-red.svg)](https://pdfbox.apache.org/)

A custom Apache NiFi processor that converts JSON input to formatted PDF documents using Apache PDFBox. Perfect for generating reports, invoices, or any document from structured data in your NiFi data flows.

## Table of Contents

- [Features](#features)
- [Processor Properties](#processor-properties)
- [Relationships](#relationships)
- [Usage Examples](#usage-examples)
- [NiFi Flow Template](#nifi-flow-template)
- [Installation](#installation)
- [Building the Processor](#building-the-processor)
- [Docker Deployment](#docker-deployment)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

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

## NiFi Flow Template

This project includes a ready-to-use NiFi flow template (`JSON2PDFExample.xml`) that demonstrates the JSON2PDF processor in action. The template includes:

### Template Components:
- **GenerateFlowFile**: Generates sample JSON data for testing
- **AttributesToJSON**: Converts FlowFile attributes to JSON format
- **JSON2PDFProcessor**: Converts JSON to PDF (our custom processor)
- **UpdateAttribute**: Updates filename to include `.pdf` extension
- **PutFile**: Saves the generated PDF to `/tmp/pdfs/` directory
- **LogAttribute**: Logs processor attributes for debugging

### How to Use the Template:
1. **Import the Template**: In NiFi, go to Templates → Upload Template → Select `JSON2PDFExample.xml`
2. **Add Template to Canvas**: Drag the template from the Templates panel onto your canvas
3. **Configure Processors**: Adjust the JSON2PDFProcessor properties as needed
4. **Start the Flow**: Start all processors to begin processing
5. **Check Output**: Generated PDFs will be saved to `/tmp/pdfs/` directory

### Template Flow:
```
GenerateFlowFile → AttributesToJSON → JSON2PDFProcessor → UpdateAttribute → PutFile
                                                      ↓
                                                 LogAttribute
```

This template provides a complete end-to-end example of how to use the JSON2PDF processor in a real NiFi data flow.

## Installation

### Quick Start with Docker

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/json2pdf-nifi-processor.git
   cd json2pdf-nifi-processor
   ```

2. **Build the processor:**
   ```bash
   mvn clean package
   ```

3. **Start NiFi with the processor:**
   ```bash
   docker-compose up -d
   ```

4. **Access NiFi:**
   - Open http://localhost:8080/nifi/
   - Login: admin / ctsBtRBKHRAx69EqUghvvgEvjnaLjFEB
   - Look for "JSON2PDFProcessor" in the "org.example" processor group

### Manual Installation

1. Build the NAR file using Maven
2. Copy the generated `json2pdf-processor-1.0.nar` file to your NiFi's `lib` directory
3. Restart NiFi
4. The processor will be available in the processor palette

## Building the Processor

### Prerequisites
- Java 11 or higher (compatible with NiFi 1.28)
- Maven 3.6 or higher

### Build Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/json2pdf-nifi-processor.git
   cd json2pdf-nifi-processor
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

3. **The NAR file will be created at:**
   ```
   target/json2pdf-processor-1.0.nar
   ```

## Docker Deployment

This project includes a complete Docker Compose setup for easy deployment and testing.

### Docker Compose Configuration

The `docker-compose.yml` file includes:
- **NiFi 1.28.0** with the JSON2PDF processor pre-loaded
- **Persistent volumes** for NiFi data repositories
- **Health checks** to ensure NiFi starts properly
- **Port mapping** for web interface access

### Environment Variables

- `NIFI_WEB_HTTP_PORT=8080` - NiFi web interface port
- `NIFI_SENSITIVE_PROPS_KEY=changeme123456789` - Encryption key for NiFi
- `NIFI_SINGLE_USER_CREDENTIALS_USERNAME=admin` - Default username
- `NIFI_SINGLE_USER_CREDENTIALS_PASSWORD=ctsBtRBKHRAx69EqUghvvgEvjnaLjFEB` - Default password

### Running with Docker

```bash
# Start NiFi with the processor
docker-compose up -d

# Check logs
docker-compose logs -f nifi

# Stop NiFi
docker-compose down
```

## Testing

The project includes comprehensive unit tests using JUnit 5 and NiFi Mock framework.

### Running Tests

```bash
mvn test
```

### Test Coverage

- **Valid JSON Processing**: Tests successful PDF generation from valid JSON
- **Invalid JSON Handling**: Tests error handling for malformed JSON
- **Property Validation**: Tests all processor properties
- **Relationship Routing**: Tests SUCCESS and FAILURE relationship routing

## Development

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── org/example/
│   │       └── JSON2PDFProcessor.java
│   └── resources/
│       └── META-INF/
│           └── services/
│               └── org.apache.nifi.processor.Processor
└── test/
    └── java/
        └── org/example/
            └── JSON2PDFProcessorTest.java
```

### Key Components

- **JSON2PDFProcessor**: Main processor class extending `AbstractProcessor`
- **PDF Generation**: Uses Apache PDFBox for robust PDF creation
- **JSON Processing**: Handles complex nested JSON structures
- **Property Descriptors**: Define processor configuration properties
- **Relationships**: Define success and failure paths for FlowFiles

### Dependencies

The processor uses the following key dependencies:
- **Apache NiFi API 1.28.0**: Core NiFi processor framework
- **PDFBox 3.0.5**: PDF generation and manipulation
- **Jackson 2.15.2**: JSON processing
- **Apache Commons IO 2.11.0**: I/O utilities
- **JUnit 5**: Unit testing framework
- **NiFi Mock 1.28.0**: NiFi testing utilities

## Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** and add tests
4. **Run the test suite:**
   ```bash
   mvn test
   ```
5. **Commit your changes:**
   ```bash
   git commit -m "Add your feature description"
   ```
6. **Push to your branch:**
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create a Pull Request**

### Development Guidelines

- Follow Java coding standards
- Add unit tests for new functionality
- Update documentation as needed
- Ensure all tests pass before submitting PR

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: Report bugs and request features via [GitHub Issues](https://github.com/yourusername/json2pdf-nifi-processor/issues)
- **Discussions**: Join the conversation in [GitHub Discussions](https://github.com/yourusername/json2pdf-nifi-processor/discussions)
- **Documentation**: Check the [Wiki](https://github.com/yourusername/json2pdf-nifi-processor/wiki) for additional documentation

## Acknowledgments

- [Apache NiFi](https://nifi.apache.org/) - Data flow processing framework
- [Apache PDFBox](https://pdfbox.apache.org/) - PDF generation library
- [Jackson](https://github.com/FasterXML/jackson) - JSON processing library
