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

package com.ericsson.oss.services.flowautomation.test.fwk.script;

import java.io.File;
import java.io.IOException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Runs scripts
 */
public class ScriptRunner {

    public Object runFlowScript(final DelegateExecution execution, final String scriptPath) {
        final Binding binding = new Binding();

        // Only support Groovy for now. Will add other script languages Camunda PE supports later
        if (!scriptPath.endsWith(".groovy")) {
            throw new IllegalArgumentException("ScriptRunner cannot run " + scriptPath + ". Only following support: .groovy");
        }

        // Add execution to binding
        binding.setVariable("execution", execution);

        // Add all variables in execution to binding
        for (final String variableName : execution.getVariableNames()) {
            binding.setVariable(variableName, execution.getVariable(variableName));
        }

        final GroovyShell shell = new GroovyShell(binding);

        String fullScriptPath = System.getProperty("user.dir").replace('\\', '/') + "/src/main/resources/" + scriptPath;
        File f = new File(fullScriptPath);
        if(!f.isFile()) {
            fullScriptPath = System.getProperty("user.dir").replace('\\', '/') + "/target/test-classes/" + scriptPath;
        }

        try {
            return shell.evaluate(new File(fullScriptPath));
        } catch (CompilationFailedException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("ScriptRunner failed to compile " + scriptPath, e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("ScriptRunner encountered IOException accessing " + scriptPath, e);
        }

        // ? need to add variables to execution which were modified outside execution ?
        // hopefully not - could be tricky
    }

}
