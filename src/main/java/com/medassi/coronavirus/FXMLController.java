package com.medassi.coronavirus;

import com.medassi.coronavirus.models.Data;
import com.medassi.coronavirus.models.Model;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;

public class FXMLController implements Initializable {

    @FXML
    private VBox vBoxWorld;
    @FXML
    private VBox vBoxFR;
    @FXML
    private VBox vBoxDept;
    @FXML
    private ListView<String> lvDept;

    private ProgressDialog pd;
    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
    private Model model;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initModel();
    }

    private void toolTipper(XYChart.Series<String, Integer> serie, String text) {
        serie.getData().forEach((d) -> {
            Tooltip.install(d.getNode(), new Tooltip(d.getXValue() + " : " + d.getYValue() + text));
        });
    }

    private void initModel() {
        Task<Model> task = new Task<Model>() {
            @Override
            protected Model call() throws Exception {
                model = new Model();
                pd.close();
                return model;
            }
        };
        pd = new ProgressDialog(task);
        pd.getDialogPane().getStylesheets().clear();
        pd.getDialogPane().getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        pd.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/virus.png"))));
        Stage st = (Stage) pd.getDialogPane().getScene().getWindow();
        st.getIcons().add(new Image(getClass().getResourceAsStream("/images/virus.png")));
        pd.setTitle("Initialisation Covid-19");
        pd.setHeaderText("Evolution des cas Covid-19");
        pd.setContentText("Récupération des données en ligne");
        new Thread(task).start();
        pd.showAndWait();
        loadData();
    }

    private void loadData() {
        lvDept.setItems(FXCollections.observableArrayList(model.getDepts()));
        lvDept.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        loadPane(model.getlWorld(), vBoxWorld, "Monde");
        loadPane(model.getlFrance(), vBoxFR, "France");
    }

    @FXML
    private void onMouseClickedLvDept(MouseEvent event) {
        ObservableList<String> selectedItems = lvDept.getSelectionModel().getSelectedItems();
        /* if (selectedItems.size() == 1 || selectedItems.size() == 0) {
            lcDept.getData().clear();
        }
         */
        ArrayList<ArrayList<Data>> llData = new ArrayList<>();
        String univers = "";
        for (String s : selectedItems) {
            llData.add(model.getHmByDep().get(s));
            univers += s + ";";
        }
        loadPaneDept(llData, univers);
    }

    private void loadPane(ArrayList<Data> lData, Pane pane, String univers) {
        XYChart.Series<String, Integer> seriesCasConfirmes = new XYChart.Series<>();
        seriesCasConfirmes.setName("Conf. " + univers);
        XYChart.Series<String, Integer> seriesDeces = new XYChart.Series<>();
        seriesDeces.setName("Décés " + univers);
        XYChart.Series<String, Integer> seriesGueris = new XYChart.Series<>();
        seriesGueris.setName("Guérisons " + univers);
        Collections.sort(lData, new Comparator<Data>() {
            @Override
            public int compare(Data o1, Data o2) {
                Date d1 = o1.getDate();
                Date d2 = o2.getDate();
                return d1.compareTo(d2);
            }
        });
        System.out.println("");
        ObservableList<String> categories = FXCollections.observableArrayList();
        for (Data d : lData) {
            categories.add(sdf.format(d.getDate()));
            if (d.getCasConfirmes() != -1) {
                seriesCasConfirmes.getData().add(new XYChart.Data<>(sdf.format(d.getDate()), d.getCasConfirmes()));
            }
            if (d.getDeces() != -1) {
                seriesDeces.getData().add(new XYChart.Data<>(sdf.format(d.getDate()), d.getDeces()));
            }
            if (d.getGueris() != -1) {
                seriesGueris.getData().add(new XYChart.Data<>(sdf.format(d.getDate()), d.getGueris()));
            }
        }
        CategoryAxis xAxis = new CategoryAxis(categories);
        xAxis.setLabel("Cas - " + univers);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Date");
        LineChart linechart = new LineChart(xAxis, yAxis);
        linechart.getData().add(seriesDeces);
        linechart.getData().add(seriesCasConfirmes);
        linechart.getData().add(seriesGueris);
        linechart.getYAxis().setAutoRanging(true);
        linechart.setTitle(univers);
        toolTipper(seriesDeces, " décés " + univers);
        toolTipper(seriesCasConfirmes, " cas confimés " + univers);
        toolTipper(seriesGueris, " guérisons " + univers);
        pane.getChildren().clear();
        pane.getChildren().add(linechart);
    }

    private void loadPaneDept(ArrayList<ArrayList<Data>> llData, String univers) {
        ObservableList<String> categories = FXCollections.observableArrayList();
        ArrayList<XYChart.Series<String, Integer>> seriesCasConfirmes = new ArrayList<>();
        ArrayList<XYChart.Series<String, Integer>> seriesDeces = new ArrayList<>();
        ArrayList<XYChart.Series<String, Integer>> seriesGueris = new ArrayList<>();
        for (ArrayList<Data> lData : llData) {
            XYChart.Series<String, Integer> serieCasConfirmes = new XYChart.Series<>();
            XYChart.Series<String, Integer> serieDeces = new XYChart.Series<>();
            XYChart.Series<String, Integer> serieGueris = new XYChart.Series<>();

            Collections.sort(lData, (Data o1, Data o2) -> {
                Date d1 = o1.getDate();
                Date d2 = o2.getDate();
                return d1.compareTo(d2);
            });
            System.out.println("");

            for (Data d : lData) {
                if (!categories.contains(sdf.format(d.getDate()))) {
                    categories.add(sdf.format(d.getDate()));
                }
                if (d.getCasConfirmes() != -1) {
                    serieCasConfirmes.getData().add(new XYChart.Data<>(sdf.format(d.getDate()), d.getCasConfirmes()));
                }
                if (d.getDeces() != -1) {
                    serieDeces.getData().add(new XYChart.Data<>(sdf.format(d.getDate()), d.getDeces()));
                }
                if (d.getGueris() != -1) {
                    serieGueris.getData().add(new XYChart.Data<>(sdf.format(d.getDate()), d.getGueris()));
                }
                univers = d.getNom();
            }
            serieCasConfirmes.setName("Conf. " + univers);
            serieDeces.setName("Décés " + univers);
            serieGueris.setName("Guérisons " + univers);
            seriesCasConfirmes.add(serieCasConfirmes);
            seriesDeces.add(serieDeces);
            seriesGueris.add(serieGueris);
        }
        CategoryAxis xAxis = new CategoryAxis(categories);
        NumberAxis yAxis = new NumberAxis();
        LineChart linechart = new LineChart(xAxis, yAxis);
        linechart.setTitle(univers);
        linechart.getData().addAll(seriesDeces);
        linechart.getData().addAll(seriesCasConfirmes);
        linechart.getData().addAll(seriesGueris);
        linechart.getYAxis().setAutoRanging(true);
        for (XYChart.Series<String, Integer> s : seriesCasConfirmes) {
            toolTipper(s, " cas confimés " + univers);
        }
        for (XYChart.Series<String, Integer> s : seriesDeces) {
            toolTipper(s, " décés " + univers);
        }
        for (XYChart.Series<String, Integer> s : seriesGueris) {
            toolTipper(s, " guérisons " + univers);
        }
        vBoxDept.getChildren().clear();
        vBoxDept.getChildren().add(linechart);
    }
}
