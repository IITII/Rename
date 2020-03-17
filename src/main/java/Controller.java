//@TODO 文件列表的自定义排序

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    public TableView<FileInfo> preTableView, afterTableView;
    public GridPane root;
    public TextField dirPathTF, preStrTF, filterTF, afterStrTF;
    public JFXButton selectDirBtn, replaceBtn, openDir;
    public Path DIR_PATH;
    public static ArrayList<FileInfo> pre, aft;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dirPathTF.focusedProperty().addListener(focusListener);
        preStrTF.focusedProperty().addListener(focusListener);
        filterTF.focusedProperty().addListener(focusListener);
        afterStrTF.focusedProperty().addListener(focusListener);
    }

    private void notification(String msg) {
        JFXSnackbar bar = new JFXSnackbar(root);
//        bar.setLayoutX(400);
//        bar.setLayoutY(700);
        bar.enqueue(new JFXSnackbar.SnackbarEvent(new Label(msg)));
    }

    private void main() {
        String dirPathTFValue = dirPathTF.getText();
        ArrayList<String> preStrTFValue = textFiledToArray(preStrTF, ":");
        ArrayList<String> filterTFValue = textFiledToArray(filterTF, ":");
        ArrayList<String> afterStrTFValue = textFiledToArray(afterStrTF, ":");
        DIR_PATH = Paths.get(dirPathTFValue);
        if (dirPathTFValue.equals("")) {
            notification("目录不能为空");
            return;
        }
        if (preStrTFValue.size() != afterStrTFValue.size()) {
            notification("请保证欲处理的文字处理前后一一对应！！！");
            return;
        }
        ArrayList<FileInfo> arrayList = fileInfo(dirPathTFValue);
        arrayList = getFileInfos(filterTFValue, arrayList);
        Collections.sort(arrayList);
        preTableView.getItems().clear();
        preTableView.getColumns().clear();
        pre = deepCopy(arrayList);
        //pre = (ArrayList<FileInfo>) arrayList.stream().map(FileInfo::new).collect(Collectors.toList());
        listTableView(preTableView, arrayList);
        if (preStrTFValue.size() != 0 || afterStrTFValue.size() != 0) {
            ArrayList<FileInfo> arrayList1 = deepCopy(arrayList);
            int arrayIndex = 0;
            for (FileInfo fileInfo : arrayList1) {
                for (int index = 0; index < preStrTFValue.size(); index++) {
                    if (fileInfo.fileName.contains(preStrTFValue.get(index))) {
                        fileInfo.fileName = fileInfo.fileName.replace(preStrTFValue.get(index), afterStrTFValue.get(index));
                        arrayList1.set(arrayIndex, fileInfo);
                    }
                }
                arrayIndex++;
            }
            Collections.sort(arrayList1);
            afterTableView.getItems().clear();
            afterTableView.getColumns().clear();
            aft = deepCopy(arrayList1);
            listTableView(afterTableView, arrayList1);
        }
    }

    public static <T> ArrayList<T> deepCopy(ArrayList<T> src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            @SuppressWarnings("unchecked")
            ArrayList<T> dest = (ArrayList<T>) in.readObject();
            return dest;
        } catch (Exception e) {
            System.out.println("Deep copy error...");
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<FileInfo> getFileInfos(ArrayList<String> preStrTFValue, ArrayList<FileInfo> arrayList) {
        if (preStrTFValue.size() != 0) {
            ArrayList<FileInfo> arrayList1 = new ArrayList<>();
            arrayList.forEach(fileInfo -> {
                preStrTFValue.forEach(fileInfo1 -> {
                    if (fileInfo.fileName.contains(fileInfo1)) {
                        //arrayList.remove(fileInfo);
                        arrayList1.add(fileInfo);
                    }
                });
            });
            arrayList = arrayList1;
        }
        return arrayList;
    }

    private ArrayList<FileInfo> fileInfo(String path) {
        try {
            ArrayList<FileInfo> arrayList = new ArrayList<FileInfo>();
            File file = new File(path);
            if (!file.exists()) {
                notification("目录不存在！！！");
                return null;
            }
            File[] files = file.listFiles();
            if (files == null || file.toString().equals("")) {
                notification("目录不存在！！！");
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            for (File singleFile : files) {
                if (singleFile.isFile()) {
                    arrayList.add(
                            new FileInfo(
                                    singleFile.getName(),
                                    fileSize(singleFile.length()),
                                    sdf.format(file.lastModified())
                            ));
                }
            }
            return arrayList;
        } catch (Exception e) {
            e.printStackTrace();
            notification("未知错误...");
            return null;
        }
    }

    private void listTableView(TableView<FileInfo> tableView, ArrayList<FileInfo> arrayList) {
        //TableView<FileInfo> tableView = new TableView<FileInfo>();
        TableColumn<FileInfo, String> column = new TableColumn<FileInfo, String>("文件名");
        TableColumn<FileInfo, String> column1 = new TableColumn<FileInfo, String>("大小");
        TableColumn<FileInfo, String> column2 = new TableColumn<FileInfo, String>("修改日期");
        column.setCellValueFactory(new PropertyValueFactory<FileInfo, String>("fileName"));
        column1.setCellValueFactory(new PropertyValueFactory<FileInfo, String>("fileSize"));
        column2.setCellValueFactory(new PropertyValueFactory<FileInfo, String>("modifyDate"));
        tableView.getColumns().addAll(column, column1, column2);
        tableView.getItems().addAll(arrayList);
    }

    private String fileSize(long size) {
        long unit = 1024;
        String[] mark = {" PB", " GB", " MB", " KB", " B"};
        return size / unit / unit + mark[2];
//        if (size / unit >= 1) {
//            size /= unit;
//            if (size / unit >= 1) {
//                size /= unit;
//                if (size / unit >= 1) {
//                    size /= unit;
//                    if (size / unit >= 1) {
//                        size /= unit;
//                        return size + mark[0];
//                    }
//                    return size + mark[1];
//                }
//                return size + mark[2];
//            }
//            return size + mark[3];
//        }
//        return size + mark[4];
    }

    private ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                main();
            }
        }
    };

    private ArrayList<String> textFiledToArray(TextField textField, String spilt) {
        ArrayList<String> arrayList = new ArrayList<>();
        String[] strings = textField.getText().split(spilt);
        for (String temp : strings) {
            if (!temp.equals("")) {
                arrayList.add(temp);
            }
        }
        return arrayList;
    }

    private void renameFile(Path path, String oldFileName, String newFileName) {
        try {
            if (oldFileName.equals(newFileName))
                return;
            File oldFile = new File(path.toString() + File.separator + oldFileName);
            File newFile = new File(path.toString() + File.separator + newFileName);
            oldFile.renameTo(newFile);
        } catch (Exception e) {
            notification("无效路径！！！");
            e.printStackTrace();
        }
    }

    @FXML
    private void rename() {
        if (pre != null && aft != null && pre.size() != 0 && aft.size() != 0) {
            for (int i = 0; i < pre.size(); i++) {
                renameFile(DIR_PATH, pre.get(i).fileName, aft.get(i).fileName);
            }
        }
    }

    @FXML
    private void fileChooser() {
        selectDirBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                selectDirBtn.setDisable(true);
                try {
                    DirectoryChooser fileChooser = new DirectoryChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    File file = fileChooser.showDialog(new Stage());
                    DIR_PATH = file.toPath();
                    dirPathTF.setText(DIR_PATH.toString());
                    main();
                    //System.out.println(file);
                    selectDirBtn.setDisable(false);
                } catch (Exception ignored) {
                }
                selectDirBtn.setDisable(false);
            }
        });
    }

    @FXML
    private void openDir() {
        try {
            if (!DIR_PATH.toString().replace(" ", "").equals("")) {
                Desktop.getDesktop().open(new File(DIR_PATH.toString()));
            } else {
                notification("目录为空！！！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
