package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JSON2PDFProcessor extends AbstractProcessor {

    public static final PropertyDescriptor TITLE = new PropertyDescriptor.Builder()
            .name("PDF Title")
            .displayName("PDF Title")
            .description("Title for the generated PDF document")
            .addValidator(StandardValidators.NON_EMPTY_EL_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .required(true)
            .defaultValue("JSON to PDF Conversion")
            .build();

    public static final PropertyDescriptor FONT_SIZE = new PropertyDescriptor.Builder()
            .name("Font Size")
            .displayName("Font Size")
            .description("Font size for the PDF content")
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .required(true)
            .defaultValue("12")
            .build();

    public static final PropertyDescriptor INCLUDE_KEYS = new PropertyDescriptor.Builder()
            .name("Include Keys")
            .displayName("Include Keys")
            .description("Whether to include JSON keys in the PDF output")
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .required(true)
            .defaultValue("true")
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("Successfully converted JSON to PDF")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("Failed to convert JSON to PDF")
            .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;
    private ObjectMapper mapper;

    @Override
    public void init(final ProcessorInitializationContext processContext) {
        mapper = new ObjectMapper();
        List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(TITLE);
        properties.add(FONT_SIZE);
        properties.add(INCLUDE_KEYS);
        this.properties = Collections.unmodifiableList(properties);

        Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) throws ProcessException {
        FlowFile flowFile = processSession.get();
        if (flowFile == null) {
            return;
        }

        String title = processContext.getProperty(TITLE).evaluateAttributeExpressions(flowFile).getValue();
        int fontSize = processContext.getProperty(FONT_SIZE).evaluateAttributeExpressions(flowFile).asInteger();
        boolean includeKeys = processContext.getProperty(INCLUDE_KEYS).evaluateAttributeExpressions(flowFile).asBoolean();

        try {
            processSession.write(flowFile, new StreamCallback() {
                @Override
                public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
                    // Read the input JSON content
                    String jsonContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    
                    // Parse JSON
                    JsonNode jsonNode = mapper.readTree(jsonContent);
                    
                    // Create PDF document
                    try (PDDocument document = new PDDocument()) {
                        PDPage page = new PDPage();
                        document.addPage(page);
                        
                        // Convert JSON to formatted text
                        String formattedText = formatJsonToText(jsonNode, includeKeys, 0);
                        String[] lines = formattedText.split("\n");
                        
                        PDPageContentStream contentStream = new PDPageContentStream(document, page);
                        try {
                            // Set font
                            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
                            
                            // Add title
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, 750);
                            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), fontSize + 2);
                            contentStream.showText(title);
                            contentStream.endText();
                            
                            // Add content
                            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
                            float yPosition = 700;
                            float lineHeight = fontSize + 4;
                            
                            for (String line : lines) {
                                if (yPosition < 50) {
                                    // Add new page if needed
                                    contentStream.close();
                                    PDPage newPage = new PDPage();
                                    document.addPage(newPage);
                                    contentStream = new PDPageContentStream(document, newPage);
                                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
                                    yPosition = 750;
                                }
                                
                                contentStream.beginText();
                                contentStream.newLineAtOffset(50, yPosition);
                                contentStream.showText(line);
                                contentStream.endText();
                                
                                yPosition -= lineHeight;
                            }
                        } finally {
                            contentStream.close();
                        }
                        
                        // Write PDF to output stream
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        document.save(baos);
                        outputStream.write(baos.toByteArray());
                    }
                }
            });

            // Update FlowFile attributes
            flowFile = processSession.putAttribute(flowFile, "mime.type", "application/pdf");
            flowFile = processSession.putAttribute(flowFile, "filename", 
                flowFile.getAttribute("filename").replaceAll("\\.(json|txt)$", ".pdf"));
            
            processSession.transfer(flowFile, SUCCESS);
            
        } catch (Exception ex) {
            getLogger().error("Error while converting JSON to PDF", ex);
            processSession.transfer(flowFile, FAILURE);
        }
    }

    private String formatJsonToText(JsonNode node, boolean includeKeys, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(indentLevel);
        
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (includeKeys) {
                    sb.append(indent).append(field.getKey()).append(": ");
                }
                
                if (field.getValue().isObject() || field.getValue().isArray()) {
                    sb.append("\n");
                    sb.append(formatJsonToText(field.getValue(), includeKeys, indentLevel + 1));
                } else {
                    sb.append(field.getValue().asText()).append("\n");
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                if (includeKeys) {
                    sb.append(indent).append("[").append(i).append("]: ");
                }
                
                if (node.get(i).isObject() || node.get(i).isArray()) {
                    sb.append("\n");
                    sb.append(formatJsonToText(node.get(i), includeKeys, indentLevel + 1));
                } else {
                    sb.append(node.get(i).asText()).append("\n");
                }
            }
        } else {
            sb.append(node.asText()).append("\n");
        }
        
        return sb.toString();
    }
}
