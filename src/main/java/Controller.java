//@TODO 文件列表的自定义排序
//@TODO 支持修改文件夹名称，不仅仅是文件

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import java.util.ResourceBundle;

/**
 * @author IITII
 */
public class Controller implements Initializable {
    /**
     * pre: 替换前的列表数组
     * aft: 替换后的列表数组
     */
    public static ArrayList<FileInfo> pre, aft;
    /**
     * preTableView: 替换前的列表
     * afterTableView: 替换后的列表
     */
    public TableView<FileInfo> preTableView, afterTableView;
    /**
     * 主面板
     */
    public GridPane root;
    /**
     * dirPathTF, preStrTF, filterTF, afterStrTF
     * 目录框，替换前文字，筛选文字，替换后文字
     */
    public TextField dirPathTF, preStrTF, filterTF, afterStrTF;
    public Path DIR_PATH;
    /**
     * 添加监听事件，当输入框内值发生改变的时候，重新运行主函数
     */
    private final ChangeListener<Boolean> focusListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            main();
        }
    };
    /**
     * selectDirBtn, replaceBtn, openDir
     * 选择目录按钮，替换按钮，打开目录按钮
     */
    public JFXButton selectDirBtn, replaceBtn, openDir;

    /**
     * 通过序列化和反序列化实现数组 deep copy
     *
     * @param src 目标数组
     * @param <T> 泛型
     * @return deep copy 后的数组
     */
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

    /**
     * 默认初始化方法，在构造方法之后运行
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dirPathTF.focusedProperty().addListener(focusListener);
        preStrTF.focusedProperty().addListener(focusListener);
        filterTF.focusedProperty().addListener(focusListener);
        afterStrTF.focusedProperty().addListener(focusListener);
    }

    /**
     * 在主面板创建一个通知
     *
     * @param msg 消息主体
     */
    private void notification(String msg) {
        JFXSnackbar bar = new JFXSnackbar(root);
//        bar.setLayoutX(400);
//        bar.setLayoutY(700);
        bar.enqueue(new JFXSnackbar.SnackbarEvent(new Label(msg)));
    }

    private ArrayList<FileInfo> getFileInfos(ArrayList<String> preStrTFValue, ArrayList<FileInfo> arrayList) {
        if (preStrTFValue.size() != 0) {
            ArrayList<FileInfo> arrayList1 = new ArrayList<>();
            arrayList.forEach(fileInfo -> preStrTFValue.forEach(fileInfo1 -> {
                if (fileInfo.fileName.contains(fileInfo1)) {
                    //arrayList.remove(fileInfo);
                    arrayList1.add(fileInfo);
                }
            }));
            arrayList = arrayList1;
        }
        return arrayList;
    }

    private ArrayList<FileInfo> fileInfo(String path) {
        try {
            ArrayList<FileInfo> arrayList = new ArrayList<>();
            File file = new File(path);
            if (!file.exists()) {
                notification("目录不存在！！！");
                return null;
            }
            File[] files = file.listFiles();
            if (files == null || "".equals(file.toString())) {
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

    /**
     * 主函数
     */
    private void main() {
        String dirPathTFValue = dirPathTF.getText();
        ArrayList<String> preStrTFValue = textFiledToArray(preStrTF, ":");
        ArrayList<String> filterTFValue = textFiledToArray(filterTF, ":");
        ArrayList<String> afterStrTFValue = textFiledToArray(afterStrTF, ":");
        DIR_PATH = Paths.get(dirPathTFValue);
        if ("".equals(dirPathTFValue)) {
            notification("目录不能为空");
            return;
        }
        if (preStrTFValue.size() != afterStrTFValue.size()) {
            notification("请保证欲处理的文字处理前后一一对应！！！");
            return;
        }
        ArrayList<FileInfo> arrayList = fileInfo(dirPathTFValue);
        arrayList = getFileInfos(filterTFValue, arrayList);
        if (arrayList == null) {
            return;
        }
        Collections.sort(arrayList);
        // 清除列表里面的内容
        preTableView.getItems().clear();
        preTableView.getColumns().clear();
        //深度拷贝
        pre = deepCopy(arrayList);
        //pre = (ArrayList<FileInfo>) arrayList.stream().map(FileInfo::new).collect(Collectors.toList());
        listTableView(preTableView, arrayList);
        if (preStrTFValue.size() != 0 || afterStrTFValue.size() != 0) {
            ArrayList<FileInfo> arrayList1 = deepCopy(arrayList);
            if (arrayList1 == null) {
                return;
            }
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

    /**
     * 生成 TableView 图表
     *
     * @param tableView 需要填充数据的 TableView 控件
     * @param arrayList 内容数组
     */
    private void listTableView(TableView<FileInfo> tableView, ArrayList<FileInfo> arrayList) {
        //TableView<FileInfo> tableView = new TableView<FileInfo>();
        TableColumn<FileInfo, String> column = new TableColumn<>("文件名");
        TableColumn<FileInfo, String> column1 = new TableColumn<>("大小");
        TableColumn<FileInfo, String> column2 = new TableColumn<>("修改日期");
        column.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        column1.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        column2.setCellValueFactory(new PropertyValueFactory<>("modifyDate"));
        tableView.getColumns().addAll(column, column1, column2);
        tableView.getItems().addAll(arrayList);
    }

    private ArrayList<String> textFiledToArray(TextField textField, String spilt) {
        ArrayList<String> arrayList = new ArrayList<>();
        String[] strings = textField.getText().split(spilt);
        for (String temp : strings) {
            if (!"".equals(temp)) {
                arrayList.add(temp);
            }
        }
        return arrayList;
    }

    /**
     * 重命名文件
     *
     * @param path        文件路径
     * @param oldFileName 旧文件名
     * @param newFileName 新文件名
     */
    private void renameFile(Path path, String oldFileName, String newFileName) {
        try {
            if (oldFileName.equals(newFileName)) {
                return;
            }
            File oldFile = new File(path.toString() + File.separator + oldFileName);
            File newFile = new File(path.toString() + File.separator + newFileName);
            oldFile.renameTo(newFile);
        } catch (Exception e) {
            notification("无效路径！！！");
            e.printStackTrace();
        }
    }

    /**
     * 文件遍历重命名
     */
    @FXML
    private void rename() {
        if (pre != null && aft != null && pre.size() != 0 && aft.size() != 0) {
            for (int i = 0; i < pre.size(); i++) {
                renameFile(DIR_PATH, pre.get(i).fileName, aft.get(i).fileName);
            }
        }
    }

    /**
     * 目录选择按钮
     */
    @FXML
    private void fileChooser() {
        selectDirBtn.setOnAction(arg0 -> {
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
        });
    }

    /**
     * 打开文件浏览器，选择目录
     */
    @FXML
    private void openDir() {
        try {
            if (!"".equals(DIR_PATH.toString().replace(" ", ""))) {
                Desktop.getDesktop().open(new File(DIR_PATH.toString()));
            } else {
                notification("目录为空！！！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
