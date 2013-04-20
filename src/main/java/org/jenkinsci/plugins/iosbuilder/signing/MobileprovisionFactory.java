package org.jenkinsci.plugins.iosbuilder.signing;

import sun.misc.BASE64Decoder;

public class MobileprovisionFactory {
    public static Mobileprovision newInstance(byte[] data) {
        try {
            return new Mobileprovision(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Mobileprovision newInstance(String encodedData) {
        try {
            return newInstance(new BASE64Decoder().decodeBuffer(encodedData));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
