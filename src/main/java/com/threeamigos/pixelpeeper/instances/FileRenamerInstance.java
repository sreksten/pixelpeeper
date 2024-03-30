package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.FileRenamerImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.FileRenamer;

public class FileRenamerInstance {

    private static final FileRenamer instance =
            new FileRenamerImpl(FileSelectorInstance.get(), NamePatternInstance.get(), MessageHandlerInstance.get());

    public static FileRenamer get() {
        return instance;
    }

    private FileRenamerInstance() {
    }
}
