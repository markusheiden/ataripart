package de.heiden.ataripart;

import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;

import java.util.Properties;

/**
 * Version provider.
 */
public class VersionProvicer implements IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/ataripart-version.properties"));
        return new String[] { properties.getProperty("version") };
    }
}
