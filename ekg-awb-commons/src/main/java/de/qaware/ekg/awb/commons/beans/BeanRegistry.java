package de.qaware.ekg.awb.commons.beans;


public class BeanRegistry {

    private static BeanRegistry registry = new BeanRegistry();

    public static BeanRegistry getInstance() {
        return registry;
    }

}
