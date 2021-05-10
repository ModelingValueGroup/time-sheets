package nl.modelingvalue.timesheets.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;

public class UrlBuilder {
    private final StringBuilder b = new StringBuilder();

    public UrlBuilder(String root) {
        b.append(root);
    }

    public UrlBuilder append(String name, String value) {
        if (!b.toString().contains("?")) {
            b.append("?");
        } else {
            b.append("&");
        }
        b.append(name).append("=").append(URLEncoder.encode(value, UTF_8));
        return this;
    }

    @Override
    public String toString() {
        return b.toString();
    }
}
