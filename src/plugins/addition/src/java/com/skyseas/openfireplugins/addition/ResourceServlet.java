package com.skyseas.openfireplugins.addition;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Override;
import java.net.URLEncoder;
import java.util.List;

/**
 * 接收资源上传的servlet。
 * Created by zhangzhi on 2014/11/17.
 */
public class ResourceServlet extends HttpServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServlet.class);
    public static final String UPLOAD_CONTENT_NAME = "content";
    public static final String RESOURCE_NAME = "name";
    private final ResourceStorage resourceStorage;

    ResourceServlet(ResourceStorage resourceStorage) {
        if (resourceStorage == null) {
            throw new NullPointerException("resourceStorage");
        }
        this.resourceStorage = resourceStorage;
    }

    /**
     * GET 下载文件
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String resourceName = request.getParameter(RESOURCE_NAME);
        if (resourceName != null && resourceName.length() > 0) {
            try {
                Resource resource = resourceStorage.load(resourceName);
                if (resource != null) {
                    /* 从资源存储器中加载资源并写入到响应 */
                    writeTo(resource, response);
                } else {
                    finish(response, HttpServletResponse.SC_NOT_FOUND, "resource not found.");
                }
            } catch (Exception exp) {
                LOGGER.error(String.format("write to response fail. name:%s", resourceName), exp);
                response.reset();
                finish(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "read resource fail.");
            }
        }
    }

    /**
     * POST 上传文件
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (ServletFileUpload.isMultipartContent(request)) {

            InputStream uploadStream = parseUploadFile(request, UPLOAD_CONTENT_NAME);
            if (uploadStream != null) {
                try {
                    /* 将上传文件流存储到资源存储器，并输出资源名称 */
                    String resourceName = resourceStorage.store(uploadStream);
                    finish(response, HttpServletResponse.SC_OK, resourceName);
                } catch (IOException exp) {
                    LOGGER.error("store file fail.", exp);
                    finish(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "io exception");
                } finally {
                    uploadStream.close();
                }
            } else {
                finish(response, HttpServletResponse.SC_BAD_REQUEST, "have no content");
            }
        }
    }

    /**
     * 分析请求中的特定字段名的上传文件。
     *
     * @param request
     * @param fieldName
     * @return
     */
    protected InputStream parseUploadFile(HttpServletRequest request, String fieldName) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (fieldName.equalsIgnoreCase(item.getFieldName())) {
                    return item.getInputStream();
                }
            }
        } catch (Exception e) {
            LOGGER.error("parse upload file fail.", e);
        }
        return null;
    }


    /**
     * 完成当前请求，并设置响应状态信息。
     *
     * @param response
     * @param status
     * @param message
     * @throws java.io.IOException
     */
    protected void finish(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        if (message != null) {
            try {
                response.getWriter().write(message);
                response.getWriter().flush();
            } finally {
                response.getWriter().close();
            }
        }
    }

    /**
     * 将资源写入响应
     *
     * @param resource
     * @param response
     * @throws Exception
     */
    protected void writeTo(Resource resource, HttpServletResponse response) throws Exception {
        response.setContentLength((int)resource.getSize());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"",
                URLEncoder.encode(resource.getName(), "utf-8")));

        /* 打开资源并将其内容输出到响应缓冲区 */
        InputStream inStream = resource.open();
        try {
            Util.writeTo(inStream, response.getOutputStream());
        } finally {
            inStream.close();
        }
    }


}
