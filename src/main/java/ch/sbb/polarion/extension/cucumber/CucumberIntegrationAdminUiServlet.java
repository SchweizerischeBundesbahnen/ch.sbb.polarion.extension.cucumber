package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class CucumberIntegrationAdminUiServlet extends GenericUiServlet {

    @Serial
    private static final long serialVersionUID = 6687845260037475974L;

    public CucumberIntegrationAdminUiServlet() {
        super("cucumber-admin");
    }
}
