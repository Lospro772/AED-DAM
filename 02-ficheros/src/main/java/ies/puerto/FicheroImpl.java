package ies.puerto;

import java.io.File;

public class FicheroImpl implements IFichero {
    @Override
    public boolean existe(Path path) {
        if (path == null) {
            return false;
        }
        File file = new File(path.toFile().getAbsolutePath());
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
