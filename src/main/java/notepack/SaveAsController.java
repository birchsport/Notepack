package notepack;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import notepack.app.domain.Note;
import notepack.app.domain.NoteStorageItem;

/**
 * FXML Controller class
 *
 */
public class SaveAsController implements Initializable {

    @FXML
    private AnchorPane tabBackground;
    @FXML
    private TreeView<NoteTreeViewItem> notepadStructure;
    @FXML
    private TextField noteName;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private SaveAsCallback clbk;

    private Note note;
    @FXML
    private Label parentDirectory;

    private String directoryName;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                noteName.requestFocus();
            }
        });

    }

    public void setNote(Note note) {
        this.note = note;

        directoryName = note.getStorage().getBasePath();
        if (!directoryName.endsWith(File.separator)) {
            directoryName += File.separator;
        }
        parentDirectory.setText(directoryName);

        refreshTreeView();

        notepadStructure.setCellFactory((p) -> {
            return new NoteTreeCell();
        });

        notepadStructure.setOnMouseClicked((t) -> {

            TreeItem<NoteTreeViewItem> it = notepadStructure.getSelectionModel().getSelectedItem();

            if (it.getValue().getNoteStorageItem().isLeaf()) {
                noteName.setText(it.getValue().getNoteStorageItem().getName());
                directoryName = it.getValue().getNoteStorageItem().getDirectory();
            } else {
                directoryName = it.getValue().getNoteStorageItem().getPath() + File.separator;
            }

            parentDirectory.setText(directoryName);
        });

    }

    public void setSaveAsCallback(SaveAsCallback clbk) {
        this.clbk = clbk;
    }

    @FXML
    private void onSaveBtn(ActionEvent event) {

        String name = noteName.getText();
        if (name.length() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Name could not be empty");
            alert.showAndWait();
            return;
        }

        clbk.save(directoryName + name);

        closeWindow();
    }

    @FXML
    private void onCancelBtn(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void refreshTreeView() {
        NoteStorageItem items = note.getNotepad().getStorage().getItemsInStorage();

        NoteTreeViewItem rootItem = new NoteTreeViewItem(note.getNotepad().getName());
        TreeItem root = new TreeItem(rootItem);

        root = addChildren(root, items);
        root.setExpanded(true);

        notepadStructure.setRoot(root);
        notepadStructure.setShowRoot(false);

        noteName.requestFocus();
    }

    private TreeItem addChildren(TreeItem parent, NoteStorageItem items) {

        for (NoteStorageItem it : items.get()) {

            if (it.isLeaf()) {

                Note cnote = new Note(it.getPath(), note.getNotepad(), it.getName());
                NoteTreeViewItem noteTreeViewItem = new NoteTreeViewItem(cnote, it);
                TreeItem<NoteTreeViewItem> n = new TreeItem<>(noteTreeViewItem);

                parent.getChildren().add(n);

            } else {

                NoteTreeViewItem noteTreeViewItem = new NoteTreeViewItem(it);
                TreeItem<NoteTreeViewItem> n = new TreeItem<>(noteTreeViewItem);
                parent.getChildren().add(n);

                n = addChildren(n, it);
            }
        }

        return parent;
    }

}
