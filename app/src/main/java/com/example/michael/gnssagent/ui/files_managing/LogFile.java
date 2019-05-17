package com.example.michael.gnssagent.ui.files_managing;

import java.io.File;

public class LogFile {

    private File file;
    private String path;
    private String name;

    public LogFile(File file) {
        this.file = file;
        this.path = file.getPath();
        this.name = file.getName();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }
}
