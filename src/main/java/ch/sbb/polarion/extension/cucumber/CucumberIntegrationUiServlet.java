package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class CucumberIntegrationUiServlet extends GenericUiServlet {

    @Serial
    private static final long serialVersionUID = 7394283682969105251L;

    public CucumberIntegrationUiServlet() {
        super("cucumber");
    }
}
