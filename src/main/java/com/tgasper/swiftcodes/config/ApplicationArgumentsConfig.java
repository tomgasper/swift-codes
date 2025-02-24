package com.tgasper.swiftcodes.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class ApplicationArgumentsConfig {
    private final ApplicationArguments args;

    public ApplicationArgumentsConfig(ApplicationArguments args) {
        this.args = args;
    }

    public boolean isImportMode() {
        return args.containsOption("import");
    }

    public boolean isServerMode() {
        return args.containsOption("server");
    }

    public String getImportPath() {
        if (args.containsOption("import")) {
            String[] values = args.getOptionValues("import").toArray(new String[0]);
            return values.length > 0 ? values[0] : null;
        }
        return null;
    }
}