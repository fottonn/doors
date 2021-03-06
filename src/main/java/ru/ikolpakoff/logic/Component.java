package ru.ikolpakoff.logic;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import jfxtras.labs.scene.control.BigDecimalField;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "COMPONENT")
public class Component {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "COMPONENT_NAME")
    private String name;

    @Transient
    private Label label;
    @Transient
    private CheckBox checkBox;
    @Transient
    private BigDecimalField bigDecimalField;

    public Component() {
    }

    public Component(String name) {

        this.name = name;

        label = new Label(name);
        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setValignment(label, VPos.CENTER);
        GridPane.setMargin(label, new Insets(5));

        checkBox = new CheckBox();
        checkBox.setDisable(true);
        checkBox.setMnemonicParsing(false);
        GridPane.setHalignment(checkBox, HPos.CENTER);
        GridPane.setValignment(checkBox, VPos.CENTER);

        bigDecimalField = new BigDecimalField();
        bigDecimalField.getStylesheets().add("css/MyBigDecimalField.css");
        bigDecimalField.setDisable(true);
        bigDecimalField.setText("0");
        bigDecimalField.setMinValue(new BigDecimal(0));
        bigDecimalField.setPrefWidth(150);
        GridPane.setHalignment(bigDecimalField, HPos.CENTER);
        GridPane.setValignment(bigDecimalField, VPos.CENTER);
        GridPane.setMargin(bigDecimalField, new Insets(5));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Label getLabel() {
        return label;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public BigDecimalField getBigDecimalField() {
        return bigDecimalField;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public void setBigDecimalField(BigDecimalField bigDecimalField) {
        this.bigDecimalField = bigDecimalField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return name.equalsIgnoreCase(component.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
