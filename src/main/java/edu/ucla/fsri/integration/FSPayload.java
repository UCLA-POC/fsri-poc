/**
 * 
 */
package edu.ucla.fsri.integration;

/**
 * @author kthotti
 *
 */
public class FSPayload {
	
	private String value;

	public FSPayload(String value) {
        this.value = value;
    }
 
    public String getValue() {
        return value;
    }
 
    public void setValue(String value) {
        this.value = value;
    }
     
    public String toString() {
        return value;
    }
     
    public FSPayload deepClone() {
        FSPayload FSPayload = new FSPayload(value);
        return FSPayload;
    }
}
