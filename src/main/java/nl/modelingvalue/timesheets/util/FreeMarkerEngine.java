package nl.modelingvalue.timesheets.util;

import java.io.*;
import java.nio.charset.*;
import java.time.*;

import de.micromata.jira.rest.core.util.*;
import freemarker.template.*;

public class FreeMarkerEngine {
    public static final LocalDateTime NOW           = LocalDateTime.now();
    public static final Version       VERSION       = new Version("2.3.31");
    private final       Configuration configuration = new Configuration(VERSION);

    public FreeMarkerEngine() {
        configuration.setClassForTemplateLoading(getClass(), "/");
        configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(VERSION).build());
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String process(String templateName, Object model) {
        try {
            Template     template     = configuration.getTemplate(templateName);
            StringWriter stringWriter = new StringWriter();
            template.process(model, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new Wrapper(e);
        }
    }
}