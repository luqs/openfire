package com.skyseas.openfireplugins.addition;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class DefaultResourceStorageTest extends TestCase {

    private DefaultResourceStorage storage;

    @Override
    public void setUp() throws Exception{
        storage = new DefaultResourceStorage(new File(System.getProperty("user.dir") + "/res/").getAbsoluteFile());
    }

    public void testStore() throws Exception {
        // Arrange
        byte[] buffer = "hellworld 我是一个小毛笔".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        // Act
        String resource = storage.store(inputStream);

        // Assert
        assertNotNull(resource);
        Resource out = storage.load(resource);

        assertEquals(out.getName(), out.getName());
        assertEquals(out.getSize(), buffer.length);
        byte[] resourceBuffer = new byte[(int) out.getSize()];
        InputStream stream = out.open();

        try {
            assertEquals(resourceBuffer.length, stream.read(resourceBuffer));
        } finally {
            stream.close();
        }
        assertEquals(new String(buffer), new String(resourceBuffer));
    }
}