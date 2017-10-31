package org.medal.clear.flow.agent;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    public static void premain(String args, Instrumentation inst) {
        LOG.info("FA00001197: Agent started.");

        Map<String, String> properties = readInitString(args);
        
        Set<String> packagesToInstrument = readNamespaces(properties.get(PARAM_NAMESPACES));
        LOG.trace("FA00006462 Packages to be instrumented {}", packagesToInstrument);
        
        inst.addTransformer(new Transformer(packagesToInstrument));
    }

    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }

    private static final String PARAM_SEPARATOR = ",";
    private static final String PARAM_KEYVALUE_SEPARATOR = "=";
    private static final String PARAM_MULTIVALUE_SEPARATOR = ";";
    private static final String PARAM_NAMESPACES = "namespaces";

    static Map<String, String> readInitString(String initString) {

        Map<String, String> properties = new HashMap<>();

        if (initString != null) {
            // i.e. host=localhost,port=1020,users=guest;admin;manager
            for (String initProperty : initString.split(PARAM_SEPARATOR)) {
                String[] keyValue = initProperty.split(PARAM_KEYVALUE_SEPARATOR, 2);
                if (keyValue.length != 2) {
                    continue;
                }
                properties.put(keyValue[0], keyValue[1]);
            }
        }

        return properties;
    }

    private static Set<String> readNamespaces(String namespaces) {

        if (isBlank(namespaces)) {
            return Collections.emptySet();
        }

        String[] ns = namespaces.split(PARAM_MULTIVALUE_SEPARATOR);
        return Arrays.stream(ns)
                .filter(StringUtils::isNotBlank)
                .map(it -> it.replaceAll("\\.", "/")) // normalize it to the internal view
                .collect(Collectors.toCollection(TreeSet<String>::new));

    }

}
