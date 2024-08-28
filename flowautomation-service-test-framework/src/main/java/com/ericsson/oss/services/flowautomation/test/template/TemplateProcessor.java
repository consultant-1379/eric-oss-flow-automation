/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.test.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.Version;

/**
 * A basic template processor. Provides a method which takes a freemarker template and a JSON 'data file' with properties to be used
 * to expand the template, and returns the expanded template.
 */
public class TemplateProcessor {
    public TemplateProcessor() {
        //nedded for spnarqube rules
    }

    /**
     * Takes a freemarker template and a JSON 'data file' with properties to be used to expand the template, and returns the expanded template.
     * @param templateFilePath    Freemarker template file path. By default it locates file in classpath. Prefix with file:// to locate a file outside classpath.
     * @param templateDataFilePath  Template data file path. Content must be JSON. By default it locates file in classpath. Prefix with file:// to locate a file outside classpath.
     * @return Expanded template.
     * @throws IOException
     * @throws TemplateException
     * @throws URISyntaxException
     */
    public String processTemplate(final String templateFilePath, final String templateDataFilePath) throws IOException, TemplateException {
        String templatedString = null;
        String classsPath = "classpath:";
        String templateFilePathToUse = templateFilePath;
        
        final Configuration freemarker = new Configuration(new Version("2.3.23"));
        if (templateFilePath.startsWith(classsPath) || templateFilePath.indexOf(':') == -1) {
            freemarker.setClassForTemplateLoading(TemplateProcessor.class, "/");
            if (templateFilePath.startsWith(classsPath)) {
                templateFilePathToUse = templateFilePath.substring(templateFilePath.indexOf(':')+1);
            }
        }
        else if (templateFilePath.startsWith("file:")) {
            String dirName = templateFilePath.substring("file://".length());
            dirName = dirName.substring(0, dirName.lastIndexOf('/'));
            freemarker.setTemplateLoader(new FileTemplateLoader(new File(dirName)));
            templateFilePathToUse = templateFilePath.substring(templateFilePath.lastIndexOf('/')+1);
        }
        
        freemarker.setDefaultEncoding("UTF-8");
        freemarker.setObjectWrapper(new MyObjectWrapper());

        final Template template = freemarker.getTemplate(templateFilePathToUse);
        final Map<String, Object> templateData = getTemplateDataFromFile(templateDataFilePath);

        try (final StringWriter out = new StringWriter()) {
            template.process(templateData, out);            // Do the templating
            templatedString = out.getBuffer().toString();
            out.flush();
        }
        
        return templatedString;
    }

    private Map<String, Object> getTemplateDataFromFile(final String filePath) throws IOException {
        final String contents = getFileAsString(filePath);
        return new ObjectMapper().readValue(contents, new TypeReference<Map<String,Object>>(){});
    }
    
    private String getFileAsString(final String filePath) throws IOException {
        String result = null;
        String classsPath = "classpath:";

        if (filePath.startsWith(classsPath) || filePath.indexOf(':') == -1) {
            String filePathToUse = filePath;
            
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            if (filePath.startsWith(classsPath)) {
                filePathToUse = filePath.substring(filePath.indexOf(':')+1);
            }
            try (InputStream is = classLoader.getResourceAsStream(filePathToUse)) {
                if (is == null) return null;
                try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                     BufferedReader reader = new BufferedReader(isr)) {
                    result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }
        }
        else if (filePath.startsWith("file:")) {
            final Path path = Paths.get(URI.create(filePath));
            final Stream<String> lines = Files.lines(path);
            result = lines.collect(Collectors.joining("\n"));
            lines.close();
        }
        else {
            throw new IllegalArgumentException("Invalid schema for file: " + filePath);
        }
        
        return result;
    }

    static class MyObjectWrapper implements ObjectWrapper {
        /**
         * @deprecated
         * @param obj
         * @return SimpleHash((Map)obj)
         * @deprecated
         */
        @Deprecated
        @Override
        public TemplateModel wrap(final Object obj) {
            return new SimpleHash((Map)obj);
        }
    }
}
