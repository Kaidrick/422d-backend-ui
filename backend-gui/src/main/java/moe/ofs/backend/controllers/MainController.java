package moe.ofs.backend.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import moe.ofs.backend.ControlPanelApplication;
import moe.ofs.backend.Plugin;
import moe.ofs.backend.PluginClassLoader;
import moe.ofs.backend.domain.PlayerInfo;
import moe.ofs.backend.gui.PlayerListCellFactory;
import moe.ofs.backend.gui.PluginListCell;
import moe.ofs.backend.handlers.BackgroundTaskRestartObservable;
import moe.ofs.backend.handlers.PlayerEnterServerObservable;
import moe.ofs.backend.handlers.PlayerLeaveServerObservable;
import moe.ofs.backend.request.RequestHandler;
import moe.ofs.backend.util.AirdromeDataCollector;
import org.controlsfx.control.StatusBar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController implements Initializable, PropertyChangeListener {

    private ResourceBundle bundle;

    @FXML private StatusBar statusBar_Connection;
    @FXML private ListView<String> listViewAddons;
    @FXML private ListView<String> listViewConnectedPlayer;

    @FXML private Label labelDebugInfo1;
    @FXML private Label labelDebugInfo2;

    @FXML public void setDebugLabelTextOne(String info) {
        labelDebugInfo1.setText(info);
    }
    @FXML public void setDebugLabelTextTwo(String info) {
        labelDebugInfo2.setText(info);
    }

    @FXML public void testStart(ActionEvent actionEvent) {
        AirdromeDataCollector.collect();
    }

    // listen to property change of "started" in BackgroundTask
    @FXML public void connectionStatusBarTextSwitch(boolean trouble) {
        if(!trouble) {
            Platform.runLater(() -> statusBar_Connection.setText("Connected"));
        } else {
            Platform.runLater(() -> statusBar_Connection.setText("Waiting for connection..."));
        }
    }

    // should be populated on application start, and there is no need to modify the list after start
    @FXML public void populateLoadedPluginListView() {
        Set<Plugin> pluginSet = new HashSet<>(PluginClassLoader.loadedPluginSet);
        List<String> pluginNameList = pluginSet.stream().map(Plugin::getName).collect(Collectors.toList());

        ObservableList<String> list = FXCollections.observableArrayList(pluginNameList);

        listViewAddons.setItems(list);
        listViewAddons.setCellFactory(param -> new PluginListCell());

        System.out.println("populateLoadedPluginListView");
    }

    // registered to PlayerEnterServerObservable
    @FXML public void addPlayerToListView(PlayerInfo playerInfo) {
//        if(playerInfo.getId() != 1)
        Platform.runLater(() -> listViewConnectedPlayer.getItems().addAll(playerInfo.getName()));
    }

    @FXML public void removePlayerFromListView(PlayerInfo playerInfo) {
//        if(playerInfo.getId() != 1)
        Platform.runLater(() -> listViewConnectedPlayer.getItems().remove(playerInfo.getName()));
    }

    @FXML public void removeAllPlayerFromListView() {
        listViewConnectedPlayer.getItems().forEach(item ->
                Platform.runLater(() -> listViewConnectedPlayer.getItems().remove(item)));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;


        RequestHandler.getInstance().addPropertyChangeListener(this);

//        anchorPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        PlayerEnterServerObservable playerEnterServerObservable = this::addPlayerToListView;
        playerEnterServerObservable.register();

        PlayerLeaveServerObservable playerLeaveServerObservable = this::removePlayerFromListView;
        playerLeaveServerObservable.register();

        BackgroundTaskRestartObservable backgroundTaskRestartObservable = this::removeAllPlayerFromListView;
        backgroundTaskRestartObservable.register();

        PlayerListCellFactory factory =
                ControlPanelApplication.applicationContext.getBean(PlayerListCellFactory.class);

        listViewConnectedPlayer.setCellFactory(lv -> factory.listView(lv).getObject());

        populateLoadedPluginListView();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if(propertyChangeEvent.getPropertyName().equals("trouble")) {
            connectionStatusBarTextSwitch((boolean) propertyChangeEvent.getNewValue());
        }
    }
}
