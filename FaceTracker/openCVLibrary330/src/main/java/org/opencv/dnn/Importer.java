
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.dnn;

import org.opencv.core.Algorithm;

// C++: class Importer
//javadoc: Importer
public class Importer extends Algorithm {

    protected Importer(long addr) { super(addr); }


    //
    // C++:  void populateNet(Net net)
    //

    //javadoc: Importer::populateNet(net)
    public  void populateNet(Net net)
    {
        
        populateNet_0(nativeObj, net.nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  void populateNet(Net net)
    private static native void populateNet_0(long nativeObj, long net_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
