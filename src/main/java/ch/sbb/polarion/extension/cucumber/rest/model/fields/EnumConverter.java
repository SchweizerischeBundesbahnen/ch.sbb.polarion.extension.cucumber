package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.subterra.base.data.model.ICustomField;
import com.polarion.subterra.base.data.model.IListType;
import com.polarion.subterra.base.data.model.IType;

import java.util.List;
import java.util.Map;

public class EnumConverter implements FieldValueConverter {

    @Override
    @SuppressWarnings("unchecked")
    public Object convert(ITrackerService trackerService, ITestRun testRun, ICustomField fieldPrototype, Object source) {
        IType fieldType = fieldPrototype.getType();
        boolean array = fieldType instanceof IListType;

        //This particular converter just extracts string names or value from initial json.
        //These values will be converted to enum options on generic side.
        return array ? ((List<Map<String, String>>) source)
                .stream().map(m -> m.get("name")).toList() :
                ((Map<String, String>) source).get("value");
    }
}
