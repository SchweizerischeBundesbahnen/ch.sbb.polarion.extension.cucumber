package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.subterra.base.data.model.ICustomField;

public interface FieldValueConverter {

    Object convert(ITrackerService trackerService, ITestRun testRun, ICustomField fieldPrototype, Object source);

}
