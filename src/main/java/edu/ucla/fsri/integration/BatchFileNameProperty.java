/**
 * 
 */
package edu.ucla.fsri.integration;

/**
 * @author kthotti
 *
 */
import java.util.HashMap;
import java.util.Map;

public class BatchFileNameProperty extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;
    private static final String FILE_NAME_DELIMETER = "_";
    private static final Map<String, Integer> NAME_INDEXES = new HashMap<String, Integer>();
    
    static {
        NAME_INDEXES.put("CampusName", 0);
        NAME_INDEXES.put("CEMLIID", 1);
        NAME_INDEXES.put("interfaceid", 1);
        NAME_INDEXES.put("ShortDesc", 2);
        NAME_INDEXES.put("RunDateTime", 3);
    }

    public static BatchFileNameProperty valueof(String filename) {
        BatchFileNameProperty property = new BatchFileNameProperty();
        if (filename != null && !filename.isEmpty()) {
            String[] values;
            int index = filename.indexOf('.');
            if (index > 0) {
                values = filename.substring(0, index).split(FILE_NAME_DELIMETER);
            } else {
                values = filename.split(FILE_NAME_DELIMETER);
            }
            for (Map.Entry<String, Integer> entry : NAME_INDEXES.entrySet()) {
                if (entry.getValue()< values.length) {
                    property.put(entry.getKey(), values[entry.getValue()]);
                }
            }
            if (index > 0) {
            	property.put("CorrelationID", filename.substring(0, index));
                property.put("InterfaceFileName", filename);
            }	
            else
            	property.put("CorrelationID", filename);
        }
        return property;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object object) {
        if (object == null || !(object instanceof Map)) {
            return false;
        }
        try {
            Map<String, String> that = ((Map<String, String>) object);
            for (String key : NAME_INDEXES.keySet()) {
                if (!this.get(key).equals(that.get(key))) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
