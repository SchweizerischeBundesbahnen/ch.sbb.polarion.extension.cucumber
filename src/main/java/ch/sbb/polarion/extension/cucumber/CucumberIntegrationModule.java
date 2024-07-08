package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.GenericModule;
import com.polarion.alm.ui.server.forms.extensions.FormExtensionContribution;

public class CucumberIntegrationModule extends GenericModule {

    @Override
    protected FormExtensionContribution getFormExtensionContribution() {
        return new FormExtensionContribution(CucumberIntegrationFormExtension.class, CucumberIntegrationFormExtension.ID);
    }
}
