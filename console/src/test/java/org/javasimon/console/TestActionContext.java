package org.javasimon.console;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.*;
/**
 * Action context for unit testing
 * @author gquintana
 */
public class TestActionContext extends ActionContext {
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private ByteArrayOutputStream byteArrayOutputStream;
    private Map<String,String> parameters=new HashMap<String, String>();
    private String contentType;
    public TestActionContext(String path) {
        super(mock(HttpServletRequest.class), mock(HttpServletResponse.class), path);
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }
    private void initWriter() {
        if (stringWriter==null) {
            stringWriter=new StringWriter();
        }
        if (printWriter==null) {
            printWriter=new PrintWriter(stringWriter);
        }
    }
    @Override
    public PrintWriter getWriter() throws IOException {
        initWriter();
        return printWriter;
    }
    private void initOutputStream() {
        if (byteArrayOutputStream==null) {
            byteArrayOutputStream=new ByteArrayOutputStream();
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        initOutputStream();
        return byteArrayOutputStream;
    }
    
    @Override
    public String toString() {
        return stringWriter.toString();
    }
    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }
    @Override
    public void setContentType(String contentType) {
        this.contentType=contentType;
    }
    public String getContentType() {
        return this.contentType;
    }
}
