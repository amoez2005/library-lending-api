package io.github.amoez2005.library.acceptance;

import static io.cucumber.junit.platform.engine.Constants.*;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.github.amoez2005.library.acceptance")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty,html:target/cucumber/report.html,json:target/cucumber/report.json")
public class AcceptanceTest {}
