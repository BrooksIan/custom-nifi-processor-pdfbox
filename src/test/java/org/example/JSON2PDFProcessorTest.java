package org.example;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class JSON2PDFProcessorTest {

    private TestRunner testRunner;

    @BeforeEach
    public void init() {
        testRunner = TestRunners.newTestRunner(org.example.JSON2PDFProcessor.class);
    }

    @Test
    public void testProcessorWithValidJSON() {
        // Arrange
        String jsonContent = "{\"name\":\"John Doe\",\"age\":30,\"email\":\"john@example.com\"}";
        
        testRunner.setProperty("PDF Title", "Test Document");
        testRunner.setProperty("Font Size", "12");
        testRunner.setProperty("Include Keys", "true");
        
        testRunner.enqueue(jsonContent.getBytes(StandardCharsets.UTF_8));
        
        // Act
        testRunner.run(1);
        
        // Assert
        testRunner.assertAllFlowFilesTransferred("SUCCESS", 1);
        
        List<MockFlowFile> successFiles = testRunner.getFlowFilesForRelationship("SUCCESS");
        MockFlowFile result = successFiles.get(0);
        
        // Verify the output is a PDF (should start with PDF header)
        byte[] content = result.toByteArray();
        String contentStr = new String(content, 0, Math.min(10, content.length));
        assert contentStr.startsWith("%PDF");
        
        // Verify MIME type is set correctly
        assert "application/pdf".equals(result.getAttribute("mime.type"));
    }

    @Test
    public void testProcessorWithInvalidJSON() {
        // Arrange
        String invalidJson = "invalid json content";
        
        testRunner.setProperty("PDF Title", "Test Document");
        testRunner.setProperty("Font Size", "12");
        testRunner.setProperty("Include Keys", "true");
        
        testRunner.enqueue(invalidJson.getBytes(StandardCharsets.UTF_8));
        
        // Act
        testRunner.run(1);
        
        // Assert
        testRunner.assertAllFlowFilesTransferred("FAILURE", 1);
    }
}
