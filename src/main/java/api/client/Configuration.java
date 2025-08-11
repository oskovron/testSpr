package api.client;

import java.util.Map;
import java.util.Objects;

public class Configuration {
    private String servicePath;
    private String contentType;
    private Map<String, String> headers;

    public Configuration(String servicePath, String contentType) {
        this.servicePath = servicePath;
        this.contentType = contentType;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Configuration)) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(getServicePath(), that.getServicePath()) &&
                Objects.equals(getContentType(), that.getContentType()) &&
                Objects.equals(getHeaders(), that.getHeaders());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServicePath(), getContentType(), getHeaders());
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "servicePath='" + servicePath + '\'' +
                ", contentType='" + contentType + '\'' +
                ", headers=" + headers +
                '}';
    }
}
