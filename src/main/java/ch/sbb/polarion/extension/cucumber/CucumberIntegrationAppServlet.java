package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class CucumberIntegrationAppServlet extends GenericUiServlet {

    @Serial
    private static final long serialVersionUID = 6687845260037475974L;

    public CucumberIntegrationAppServlet() {
        super("cucumber-app");
    }
}
