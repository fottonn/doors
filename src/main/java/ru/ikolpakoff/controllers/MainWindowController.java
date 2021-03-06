package ru.ikolpakoff.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.BigDecimalField;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import ru.ikolpakoff.base.HibernateUtil;
import ru.ikolpakoff.logic.*;
import ru.ikolpakoff.logic.dao.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;


public class MainWindowController implements Initializable {


    private Stage rootStage;
    public HibernateUtil hibernateUtil;

    public Stage getRootStage() {
        return rootStage;
    }

    public void setRootStage(Stage rootStage) {
        this.rootStage = rootStage;
    }

    @FXML
    private ComboBox<ProtectionDevice> protectionDeviceComboBox;

    @FXML
    private ComboBox<CurrentMeter> currentMeterComboBox;
    @FXML
    private ComboBox<CameraType> cameraTypeComboBox;
    @FXML
    private CustomTextField searchField;
    @FXML
    private MenuItem menuClose;
    @FXML
    private MenuItem menuAdd;
    @FXML
    private GridPane componentsGridPain;
    @FXML
    private Button findButton;
    @FXML
    private Button addButton;
    @FXML
    private TableView<Door> doorTable;
    @FXML
    private TableColumn<Door, String> doorColumn;
    @FXML
    private Label doorCountLabel;
    @FXML
    private CheckBox currentMeterCheckBox;
    @FXML
    private CheckBox protectionDeviceCheckBox;

    public ComboBox<CurrentMeter> getCurrentMeterComboBox() {
        return currentMeterComboBox;
    }

    public ComboBox<ProtectionDevice> getProtectionDeviceComboBox() {
        return protectionDeviceComboBox;
    }

    public ComboBox<CameraType> getCameraTypeComboBox() {
        return cameraTypeComboBox;
    }

    public GridPane getComponentsGridPain() {
        return componentsGridPain;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setupClearButtonField(searchField);
        hibernateUtil = HibernateUtil.getHibernateUtil();
        new CameraTypeDAO().fill(this);
        new ProtectionDeviceDAO().fill(this);
        new CurrentMeterDAO().fill(this);
        new ComponentDAO().fill(this);
        doorColumnInit();
        fillTableColumn(new DoorDAO().getAllDoors());

    }


    //method add clear button into CustomTextField
    private void setupClearButtonField(CustomTextField customTextField) {
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, customTextField, customTextField.rightProperty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this method provide close action from menu
    public void closeAction(ActionEvent actionEvent) {
        if (HibernateUtil.sessionFactory != null) {
            HibernateUtil.sessionFactoryClose();
        }
        rootStage.close();
    }

    //this method provide opening window for add elements from menu
    public void addAction(ActionEvent actionEvent) {
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("/fxmls/ComponentsAddWindow.fxml"));
            root = loader.load();
        } catch (IOException e) {
            System.out.println("ComponentsAddWindow.fxml not found");
            e.printStackTrace();
        }

        ((ComponentsAddWindowController) loader.getController()).setMainWindowController(this);

        Stage componentsAddWindowStage = new Stage();
        componentsAddWindowStage.initModality(Modality.APPLICATION_MODAL);
        componentsAddWindowStage.setTitle("Добавить");

        componentsAddWindowStage.setScene(new Scene(root, 415, 120));
        componentsAddWindowStage.setResizable(false);
        componentsAddWindowStage.show();
    }

    //this method provide undisabling checkboxes
    public void undisablingCheckBoxes() {
        ObservableList<Node> children = componentsGridPain.getChildren();
        if (cameraTypeComboBox.getValue() != null) {
            addButton.setDisable(false);
            findButton.setDisable(false);
            for (Node node : children) {
                if (node instanceof CheckBox) {
                    node.setDisable(false);
                }
            }
        } else {
            addButton.setDisable(true);
            findButton.setDisable(true);
            for (Node node : children) {
                if (node instanceof CheckBox) {
                    if (((CheckBox) node).isSelected()) {
                        ((CheckBox) node).setSelected(false);
                        Event.fireEvent(node, new ActionEvent());
                    }
                    node.setDisable(true);
                }
            }
        }
    }

    //this method provide undisabling or disabling ComboBoxes or BigDecimalFields of elements
    public void checkBoxFire(ActionEvent actionEvent) {
        CheckBox cb = (CheckBox) actionEvent.getSource();
        int rIndex = GridPane.getRowIndex(cb);
        for (Node node : componentsGridPain.getChildren()) {
            if (node instanceof ComboBox || node instanceof BigDecimalField) {
                if ((GridPane.getRowIndex(node) != null ? GridPane.getRowIndex(node) : 0) == rIndex) {
                    if (cb.isSelected()) {
                        if (node instanceof BigDecimalField) {
                            ((BigDecimalField) node).setNumber(new BigDecimal(1));
                            ((BigDecimalField) node).setMinValue(new BigDecimal(1));
                        } else {
                            ComboBox c = (ComboBox) node;
                            c.setValue(c.getItems().get(0));
                        }
                        node.setDisable(false);
                    } else {
                        if (node instanceof ComboBox) ((ComboBox) node).setValue(null);
                        else {
                            ((BigDecimalField) node).setMinValue(new BigDecimal(0));
                            ((BigDecimalField) node).setNumber(new BigDecimal(0));
                        }
                        node.setDisable(true);
                    }
                }
            }
        }

    }

    public void doorAdding() {
        Door door = doorByFields();

        List<Door> doorsByHash = new DoorDAO().getDoorByHash(door.getHash());

        if (doorsByHash == null || doorsByHash.isEmpty()) {
            new DoorDAO(door).save();
        } else {
            for (Door d : doorsByHash) {
                if (d.getCameraType().equals(door.getCameraType())) {
                    if ((d.getProtectionDevice() == null && door.getProtectionDevice() == null) || d.getProtectionDevice().equals(door.getProtectionDevice())) {
                        if ((d.getCurrentMeter() == null && door.getCurrentMeter() == null) || d.getCurrentMeter().equals(door.getCurrentMeter())) {
                            if (d.getComponents().equals(door.getComponents())) {
                                new Alert(Alert.AlertType.WARNING, "Добавляемая дверь уже содержится в базе", ButtonType.OK).showAndWait();
                                return;
                            }
                        }
                    }
                }
            }
            new DoorDAO(door).save();
        }


    }

    private Door doorByFields() {
        Door door;

        int doorNumber;
        CameraType cameraType;
        ProtectionDevice protectionDevice;
        CurrentMeter currentMeter;
        Map<Component, Integer> components;

        Component component;
        CheckBox checkBox;
        Label label = null;
        BigDecimalField bigDecimalField = null;

        cameraType = cameraTypeComboBox.getValue();
        protectionDevice = protectionDeviceComboBox.getValue();
        currentMeter = currentMeterComboBox.getValue();
        doorNumber = new DoorDAO().getLastDoorNumber(cameraType) + 1;
        components = new HashMap<>();

        door = new Door(doorNumber, cameraType, protectionDevice, currentMeter);

        ObservableList<Node> componentsGridPainChildrens = componentsGridPain.getChildren();
        String componentName = null;
        Integer componentCount = null;
        for (Node children : componentsGridPainChildrens) {
            if (children instanceof CheckBox && ((CheckBox) children).isSelected() &&
                    (GridPane.getRowIndex(children) != null ? GridPane.getRowIndex(children) : 0) >= 3) {
                checkBox = (CheckBox) children;
                for (Node node : componentsGridPainChildrens) {
                    if (GridPane.getRowIndex(children) == GridPane.getRowIndex(node)) {
                        if (node instanceof Label) {
                            label = (Label) node;
                            componentName = label.getText();
                        } else if (node instanceof BigDecimalField) {
                            bigDecimalField = (BigDecimalField) node;
                            componentCount = bigDecimalField.getNumber().intValue();
                        }
                    }
                }

                component = new DoorDAO().getComponent(componentName);
                component.setCheckBox(checkBox);
                component.setLabel(label);
                component.setBigDecimalField(bigDecimalField);
                components.put(component, componentCount);
            }
        }

        door.setComponents(components);
        door.setHash();

        return door;
    }

    private void fieldsByDoor(Door door) {

        ObservableList<Node> componentsGridPainChildrens = componentsGridPain.getChildren();
        Set<Map.Entry<Component, Integer>> components = door.getComponents().entrySet();

        for (Node node : componentsGridPainChildrens) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                checkBox.setSelected(false);
                Event.fireEvent(checkBox, new ActionEvent());
            }
        }


        cameraTypeComboBox.setValue(door.getCameraType());

        if (door.getProtectionDevice() != null) {
            protectionDeviceCheckBox.setSelected(true);
            Event.fireEvent(protectionDeviceCheckBox, new ActionEvent());
            protectionDeviceComboBox.setValue(door.getProtectionDevice());
        }

        if (door.getCurrentMeter() != null) {
            currentMeterCheckBox.setSelected(true);
            Event.fireEvent(currentMeterCheckBox, new ActionEvent());
            currentMeterComboBox.setValue(door.getCurrentMeter());
        }


        for (Map.Entry<Component, Integer> entry : components) {
            for (Node node : componentsGridPainChildrens) {
                if (node != null && node instanceof Label && ((Label) node).getText().equals(entry.getKey().getName())) {
                    for (Node node1 : componentsGridPainChildrens) {
                        if (node1 != null && GridPane.getRowIndex(node1) == GridPane.getRowIndex(node) && node1 instanceof BigDecimalField) {
                            for (Node cb : componentsGridPainChildrens) {
                                if (cb != null && cb instanceof CheckBox && GridPane.getRowIndex(cb) == GridPane.getRowIndex(node)) {
                                    ((CheckBox) cb).setSelected(true);
                                    Event.fireEvent(cb, new ActionEvent());
                                    break;
                                }
                            }
                            ((BigDecimalField) node1).setNumber(new BigDecimal(entry.getValue()));
                            break;
                        }
                    }
                    break;
                }
            }
        }

    }

    public void doorFinding() {

        Door door = doorByFields();

        List<Door> doors = new DoorDAO().getDoorByHash(door.getHash());

        if (doors == null || doors.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Дверь не найдена", ButtonType.OK).showAndWait();
        } else {
            for (Door d : doors) {
                if (d.getCameraType().equals(door.getCameraType())) {
                    if ((d.getProtectionDevice() == null && door.getProtectionDevice() == null) || d.getProtectionDevice().equals(door.getProtectionDevice())) {
                        if ((d.getCurrentMeter() == null && door.getCurrentMeter() == null) || d.getCurrentMeter().equals(door.getCurrentMeter())) {
                            if (d.getComponents().equals(door.getComponents())) {
                                d.setDoorDesignation();
                                ButtonType copyButtonType = new ButtonType("Скопировать и закрыть");
                                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                        d.getDoorDesignation(), copyButtonType);
                                alert.setHeaderText("Дверь найдена!");
                                Button btn = (Button) alert.getDialogPane().lookupButton(copyButtonType);
                                btn.setOnAction(event -> {
                                    Clipboard clbd = Clipboard.getSystemClipboard();
                                    ClipboardContent clipboardContent = new ClipboardContent();
                                    clipboardContent.putString(alert.getContentText());
                                    clbd.setContent(clipboardContent);
                                });
                                alert.showAndWait();
                                fillTableColumn(d);
                                return;
                            }
                        }
                    }
                }
            }
            new Alert(Alert.AlertType.WARNING, "Дверь не найдена", ButtonType.OK).showAndWait();

        }

    }

    public void fillTableColumn(Door door) {
        ObservableList<Door> list = FXCollections.observableArrayList();
        list.add(door);
        doorTable.setItems(list);
        doorCountLabel.setText("1");
    }

    public void fillTableColumn(List<Door> doors) {
        ObservableList<Door> list = FXCollections.observableArrayList();
        list.addAll(doors);
        doorTable.setItems(list);
        doorCountLabel.setText(String.valueOf(list.size()));
    }

    public void doorColumnInit() {

        doorColumn.setCellFactory(param -> {
            TableCell<Door, String> cell = new TableCell<Door, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                    }
                }

            };


            ContextMenu contextMenu = new ContextMenu();
            MenuItem editMenuItem = new MenuItem("Изменить");
            contextMenu.getItems().add(editMenuItem);
            cell.setContextMenu(contextMenu);

            cell.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {
                        fieldsByDoor((Door) cell.getTableRow().getItem());
                    }
                }
            });

            return cell;
        });

        doorColumn.setCellValueFactory(new PropertyValueFactory<Door, String>("doorDesignation"));
    }
}
