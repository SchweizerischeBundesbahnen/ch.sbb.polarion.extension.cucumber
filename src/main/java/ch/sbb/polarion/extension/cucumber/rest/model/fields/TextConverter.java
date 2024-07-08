package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.core.util.types.Text;
import com.polarion.subterra.base.data.model.ICustomField;

public class TextConverter implements FieldValueConverter {

    private final boolean wrapText;

    public TextConverter(boolean wrapText) {
        this.wrapText = wrapText;
    }

    @Override
    public Object convert(ITrackerService trackerService, ITestRun testRun, ICustomField fieldPrototype, Object source) {
        String content = source == null ? "" : String.valueOf(source);
        return wrapText ? Text.plain(content) : content;
    }
}
