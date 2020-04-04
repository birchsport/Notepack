/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and loadContent the template in the editor.
 */
package notepack.app.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import notepack.app.domain.Note;
import notepack.app.domain.NoteStorage;
import notepack.app.domain.NoteStorageConfiguration;
import notepack.app.domain.NoteTreeItem;

/**
 *
 * @author lg
 */
public class Filesystem implements NoteStorage {

    private NoteStorageConfiguration nsc;

    private ArrayList<NoteTreeItem> items = new ArrayList<>();

    private int deep = 0;

    public Filesystem() {
        nsc = new NoteStorageConfiguration();
        nsc.set("directory", System.getProperty("java.io.tmpdir"));
    }

    public Filesystem(String path) {
        nsc = new NoteStorageConfiguration();
        nsc.set("directory", path);
    }

    public Filesystem(NoteStorageConfiguration nsc) {
        this.nsc = nsc;
    }

    private String getBasePath() {
        return nsc.get("directory");
    }

    @Override
    public String loadContent(String path) {

        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public NoteTreeItem list() {

        File f = new File(getBasePath());

        NoteTreeItem it = new NoteTreeItem(getBasePath(), f.getName());

        it = addItems(it, getBasePath(), 0);

        return it;
    }

    private NoteTreeItem addItems(NoteTreeItem parent, String startPath, int deep) {
        if (deep > 5) {
            return parent;
        }

        ArrayList<String> supportedExtensions = new ArrayList<>();
        supportedExtensions.add("txt");
        supportedExtensions.add("ini");
        supportedExtensions.add("json");
        supportedExtensions.add("xml");
        supportedExtensions.add("md");
        supportedExtensions.add("csv");
        supportedExtensions.add("yaml");
        supportedExtensions.add("log");

        File f = new File(startPath);
        for (String p : f.list()) {
            if (p.substring(0, 1).equals(".")) {
                continue;
            }

            File ff = new File(startPath + File.separator + p);

            String extension = "";

            int i = p.lastIndexOf('.');
            int pos = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));

            if (i > pos) {
                extension = p.substring(i + 1);
            }

            if (supportedExtensions.contains(extension)) {

                parent.add(new NoteTreeItem(startPath + File.separator + p, p));

            } else if (ff.isDirectory()) {

                NoteTreeItem newDirectoryParent = new NoteTreeItem(startPath + File.separator + p, p);
//                parent.add(newDirectoryParent);

                newDirectoryParent = addItems(newDirectoryParent, startPath + File.separator + p, deep + 1);
                if (newDirectoryParent.get().size() > 0) {
                    parent.add(newDirectoryParent);
                }
            }
        }

        return parent;
    }

    @Override
    public void saveContent(String content, String path) {

        try {
            Files.write(Paths.get(getBasePath() + File.separator + path), content.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Filesystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setConfiguration(NoteStorageConfiguration nsc) {
        this.nsc = nsc;
    }

    @Override
    public NoteStorageConfiguration getConfiguration() {
        return nsc;
    }

}
