package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.GenericBundleActivator;
import com.polarion.alm.ui.server.forms.extensions.IFormExtension;

import java.util.Map;

@SuppressWarnings("unused")
public class ExtensionBundleActivator extends GenericBundleActivator {

    @Override
    protected Map<String, IFormExtension> getExtensions() {
        return Map.of(CucumberIntegrationFormExtension.ID, new CucumberIntegrationFormExtension());
    }

}
