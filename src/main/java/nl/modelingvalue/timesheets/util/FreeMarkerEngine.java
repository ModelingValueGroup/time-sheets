package nl.modelingvalue.timesheets.util;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import de.micromata.jira.rest.core.util.Wrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

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