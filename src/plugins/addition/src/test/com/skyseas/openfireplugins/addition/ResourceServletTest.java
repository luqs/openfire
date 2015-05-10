package com.skyseas.openfireplugins.addition;

import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class ResourceServletTest extends TestCase {
    @Mocked HttpServletRequest request;
    @Mocked HttpServletResponse response;
    @Mocked ResourceStorage storage;
    @Mocked Resource resource;
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    ResourceServlet servlet;
    @Override
    public void setUp() {
        servlet = new ResourceServlet(storage);
        new NonStrictExpectations(ServletFileUpload.class) {
            {
                ServletFileUpload.isMultipartContent(request);
                result = true;
            }
        };
    }

    public void testDoPost() throws Exception {

        // Arrange
        final String resourceName = "resource_name";
        new NonStrictExpectations(servlet) {
            {
                servlet.parseUploadFile(request, "content");
                result = inputStream;
                times = 1;

                storage.store(inputStream);
                result = resourceName;
                times = 1;

                inputStream.close();
                times = 1;

                servlet.finish(response, HttpServletResponse.SC_OK, resourceName);
                times = 1;

            }
        };

        // Act
        servlet.doPost(request, response);
    }

    public void testDoPost_When_Store_Fail() throws Exception {
        // Arrange
        new NonStrictExpectations(servlet) {
            {
                servlet.parseUploadFile(request, "content");
                result = inputStream;
                times = 1;

                storage.store(inputStream);
                result = new IOException();
                times = 1;

                inputStream.close();
                times = 1;

                servlet.finish(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "io exception");
                times = 1;

            }
        };

        // Act
        servlet.doPost(request, response);
    }

    public void testDoPost_When_Content_Is_Empty() throws Exception {
        // Arrange
        new NonStrictExpectations(servlet) {
            {
                servlet.parseUploadFile(request, "content");
                result = null;
                times = 1;

                inputStream.close();
                times = 1;

                servlet.finish(response, HttpServletResponse.SC_BAD_REQUEST, "have no content");
                times = 1;


            }
        };

        // Act
        servlet.doPost(request, response);
    }


    public void testDoGet() throws Exception {
        // Arrange
        final String resourceName = "myresource";
        new NonStrictExpectations(servlet) {
            {
                request.getParameter(ResourceServlet.RESOURCE_NAME);
                result = resourceName;

                storage.load(resourceName);
                result = resource;
                times = 1;

                servlet.writeTo(resource, response);
                times = 1;
            }
        };

        // Act
        servlet.doGet(request, response);
    }

    public void testDoGet_When_Resource_Not_Found() throws Exception {
        // Arrange
        final String resourceName = "myresource";
        new NonStrictExpectations(servlet) {
            {
                request.getParameter(ResourceServlet.RESOURCE_NAME);
                result = resourceName;

                storage.load(resourceName);
                result = null;
                times = 1;

                servlet.finish(response, HttpServletResponse.SC_NOT_FOUND, "resource not found.");
                times = 1;
            }
        };

        // Act
        servlet.doGet(request, response);
    }

    public void testDoGet_When_Resource_Write_Fail() throws Exception {
        // Arrange
        final String resourceName = "myresource";
        new NonStrictExpectations(servlet) {
            {
                request.getParameter(ResourceServlet.RESOURCE_NAME);
                result = resourceName;

                storage.load(resourceName);
                result = resource;
                times = 1;

                servlet.writeTo(resource, response);
                result = new IOException();
                times = 1;

                servlet.finish(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "read resource fail.");
                times = 1;

            }
        };

        // Act
        servlet.doGet(request, response);
    }

    public void testWriteTo() throws Exception {
        // Arrange
        final byte[] buffer = "helloworld".getBytes();
        final ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final MockResource resource = new MockResource("myfile", buffer.length, input);
        new NonStrictExpectations() {
            {
                response.getOutputStream();
                result = new MockServletOutputStream(output);
            }
        };

        // Act
        servlet.writeTo(resource, response);

        // Assert
        new Verifications() {
            {
                response.setContentLength((int)resource.getSize());
                times = 1;

                response.setContentType("application/octet-stream");
                times = 1;

                response.setStatus(HttpServletResponse.SC_OK);
                times = 1;

                input.close();
                times = 1;
            }
        };

        assertEquals(new String(buffer), new String(output.toByteArray()));
    }

    public class MockResource implements Resource {
        private final String name;
        private final int size;
        private final InputStream stream;

        public MockResource(String name, int size, InputStream stream) {
            this.name = name;
            this.size = size;
            this.stream = stream;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public InputStream open() {
            return stream;
        }
    }

    public class MockServletOutputStream extends ServletOutputStream {
        private final OutputStream stream;

        public MockServletOutputStream(OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public void write(int b) throws IOException {
            stream.write(b);
        }
    }

}