
package org.interactivemesh.jfx.sample3d.tuxcube;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.animation.AnimationTimer;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;

import javafx.scene.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;

import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.util.Callback;

import javafx.util.Pair;
import org.interactivemesh.jfx.sample3d.tuxcube.FXTuxCubeSubScene.VP;

import javax.imageio.ImageIO;


/**
 *
 */
public final class FXTuxCube extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private SubScene subScene = null;

    private long lastTime = 0L;
    private int frameCounter = 0;
    private int elapsedFrames = 100;

    private AnimationTimer fpsTimer = null;
    private HUDLabel fpsTitleLabel = null;
    private HUDLabel fpsLabel = null;
//    private HUDLabel mpfTitleLabel = null;
//    private HUDLabel mpfLabel = null;

    private boolean isUpdateFPS = false;
    private boolean isTuxRotating = false;
    private boolean isCubeRotating = false;
    private boolean isMouseDragged = false;

    private DrawMode drawMode = DrawMode.LINE;

    private Background blackBG = null;
    private Background blueBG = null;
    private Background greenBG = null;

    private int gap = 0;
    private int border = 0;

    private Font titleFont = null;
    private Font textFont = null;
    private Font cellFont = null;

    private NumberFormat numFormat = null;

    private FXTuxCubeSubScene tuxCubeSubScene;
    private Color color = Color.rgb(255, 255, 255);

    private ThreadPoolExecutor executor = null;


    private HUDLabel isConnectingLabel = null;
    private HUDLabel isOpeningLabel = null;

    public FXTuxCubeSubScene getTuxCubeSubScene() {
        return tuxCubeSubScene;
    }

    @Override
    public void start(Stage stage) {

        final Rectangle2D screenRect = Screen.getPrimary().getBounds();
        final double screenWidth = screenRect.getWidth();
        final double screenHeight = screenRect.getHeight();

        /**
         * 根据屏幕的长宽设置字体 gap border（未验证是什么）
         */
        // screenHeight > 1200      (1440/27', 1600/30')
        int cellFontSize = 16;
        int textFontSize = 20;
        int titleFontSize = 38;
        gap = 6;
        border = 50;

        // 900 < screenHeight  <= 1080/23'
        if (screenHeight <= 1080) {
            cellFontSize = 14;
            textFontSize = 16;
            titleFontSize = 30;
            gap = 4;
            border = 30;
        }
        // 1080 < screenHeight <= 1200/24'
        else if (screenHeight <= 1200) {
            cellFontSize = 14;
            textFontSize = 18;
            titleFontSize = 34;
            gap = 5;
            border = 40;
        }


        /**
         * 字体设置
         */
        final String fontFamily = "Dialog";

        titleFont = Font.font(fontFamily, FontWeight.NORMAL, titleFontSize);
        textFont = Font.font(fontFamily, FontWeight.NORMAL, textFontSize);
        cellFont = Font.font(fontFamily, FontWeight.NORMAL, cellFontSize);

        numFormat = NumberFormat.getIntegerInstance();
        numFormat.setGroupingUsed(true);

        //
        // 3D subscene
        //
        tuxCubeSubScene = new FXTuxCubeSubScene(stage, 0);

        subScene = tuxCubeSubScene.getSubScene();
        if (subScene == null) {
            exit();
        }

        //
        // Title
        //

        final HUDLabel titleLeftLabel = new HUDLabel("三维图形浏览器", titleFont);
        final HUDLabel titleRightLabel = new HUDLabel("Java 3D", titleFont);

        //
        // 3D scene details and performance
        //

        // FPS - frames per second

        fpsTitleLabel = new HUDLabel("F P S");
        fpsTitleLabel.setTooltip(new Tooltip("frames per second"));
        fpsLabel = new HUDLabel("0");
        fpsLabel.setTooltip(new Tooltip("frames per second"));

        // MPF - milliseconds per frame

//        mpfTitleLabel = new HUDLabel("M P F");
//        mpfTitleLabel.setTooltip(new Tooltip("milliseconds per frame"));
//        mpfLabel = new HUDLabel("0");
//        mpfLabel.setTooltip(new Tooltip("milliseconds per frame"));

        // Tuxes/Shape3Ds/triangles

//        final HUDLabel tuxesLabel = new HUDLabel("Tuxes");
//        tuxesLabel.setTooltip(new Tooltip("number of Tux models and RotateTransitions"));
        final HUDLabel shape3dLabel = new HUDLabel("Shape3Ds");
        shape3dLabel.setTooltip(new Tooltip("number of Shape3D nodes"));
        final HUDLabel triangleLabel = new HUDLabel("Triangles");
        triangleLabel.setTooltip(new Tooltip("number of triangles"));

        // Set initial output values for autosizing
//        final HUDLabel numTuxesLabel = new HUDLabel(numFormat.format(27));
//        numTuxesLabel.setTooltip(new Tooltip("number of Tux models and RotateTransitions"));
//        final HUDLabel numShape3dLabel = new HUDLabel(numFormat.format(162));
//        numShape3dLabel.setTooltip(new Tooltip("number of Shape3D nodes"));
//        final HUDLabel numTriaLabel = new HUDLabel(numFormat.format(371088));
//        numTriaLabel.setTooltip(new Tooltip("number of triangles"));

        // Size of FXCanvas3D

        final HUDLabel heightLabel = new HUDLabel("Height");
        heightLabel.setTooltip(new Tooltip("height of 3D SubScene"));
        final HUDLabel pixHeightLabel = new HUDLabel("0");
        pixHeightLabel.setTooltip(new Tooltip("height of 3D SubScene"));
        final HUDLabel widthLabel = new HUDLabel("Width");
        widthLabel.setTooltip(new Tooltip("width of 3D SubScene"));
        final HUDLabel pixWidthLabel = new HUDLabel("0");
        pixWidthLabel.setTooltip(new Tooltip("width of 3D SubScene"));

        // Collect all outputs 

        final Rectangle gap1 = new Rectangle(gap, gap, Color.TRANSPARENT);
        final Rectangle gap2 = new Rectangle(gap, gap, Color.TRANSPARENT);

        final GridPane outputPane = new GridPane();
        outputPane.setHgap(10);
        outputPane.setVgap(0);
        outputPane.setGridLinesVisible(false);

        outputPane.add(fpsLabel, 0, 0);
        outputPane.add(fpsTitleLabel, 1, 0);
//        outputPane.add(mpfLabel, 0, 1);
//        outputPane.add(mpfTitleLabel, 1, 1);
        outputPane.add(gap1, 0, 2);
//        outputPane.add(numTuxesLabel, 0, 3);
//        outputPane.add(tuxesLabel, 1, 3);
//        outputPane.add(numShape3dLabel, 0, 4);
//        outputPane.add(shape3dLabel, 1, 4);
//        outputPane.add(numTriaLabel, 0, 5);
//        outputPane.add(triangleLabel, 1, 5);
        outputPane.add(gap2, 0, 6);
        outputPane.add(pixWidthLabel, 0, 7);
        outputPane.add(widthLabel, 1, 7);
        outputPane.add(pixHeightLabel, 0, 8);
        outputPane.add(heightLabel, 1, 8);

        final ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setHalignment(HPos.RIGHT);

        final ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setHalignment(HPos.LEFT);

        outputPane.getColumnConstraints().addAll(leftColumn, rightColumn);

        //
        // Controls
        //

        // Cube size

        //final HUDLabel numTitleLabel = new HUDLabel("Cube");
        final HUDLabel numTitleLabel = new HUDLabel("数量（立方）");
        numTitleLabel.setTooltip(new Tooltip("width x height x depth"));
        numTitleLabel.setVisible(false);
        final ObservableList<Number> nums = FXCollections.<Number>observableArrayList(1, 2, 3, 4, 5, 6);
        // final ObservableList<Number> nums = FXCollections.<Number>observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        /**
         * 复选框
         */
        final ComboBox<Number> cubeCombo = new ComboBox<>();
        cubeCombo.setTooltip(new Tooltip("width x height x depth"));
        cubeCombo.setItems(nums);
        cubeCombo.setVisibleRowCount(12);
        //cubeCombo.getSelectionModel().select(2);
        cubeCombo.getSelectionModel().select(0);
        cubeCombo.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> {
                    int num = (Integer) new_val;
                    int num3 = num * num * num;

                    tuxCubeSubScene.createTuxCubeOfDim(num, isTuxRotating, drawMode, color);
                    tuxCubeSubScene.setVantagePoint(VP.FRONT);

//                    numTuxesLabel.setText(numFormat.format(num3));
//                    numShape3dLabel.setText(numFormat.format(num3 * 6));
//                    numTriaLabel.setText(numFormat.format(num3 * 13744));
                }
        );
        cubeCombo.setButtonCell(new ListCell<Number>() {
            {
                this.setFont(cellFont);
            }

            @Override
            protected void updateItem(Number item, boolean empty) {
                // calling super here is very important - don't skip this!
                super.updateItem(item, empty);
                if (item != null) {
                    this.setText(Integer.toString((Integer) item));
                }
            }
        });

        cubeCombo.setCellFactory(
                new Callback<ListView<Number>, ListCell<Number>>() {
                    @Override
                    public ListCell<Number> call(ListView<Number> p) {
                        return new ListCell<Number>() {
                            {
                                this.setFont(cellFont);
                            }

                            @Override
                            protected void updateItem(Number item, boolean empty) {
                                // calling super here is very important - don't skip this!
                                super.updateItem(item, empty);
                                if (item != null) {
                                    this.setText(Integer.toString((Integer) item));
                                }
                            }
                        };
                    }
                }
        );
        cubeCombo.setVisible(false);

        // Viewpoints   

        final HUDLabel vpTitleLabel = new HUDLabel("观察角度");
        // final HUDLabel vpTitleLabel = new HUDLabel("Viewpoint");
        vpTitleLabel.setTooltip(new Tooltip("select viewpoint"));

        // Prompt text workaround !!!!!!!!!!!!!!!!!!
        final ComboBox<VP> vpCombo = new ComboBox<VP>();
        vpCombo.setTooltip(new Tooltip("select viewpoint"));
        vpCombo.getItems().addAll(VP.BOTTOM, VP.CORNER, VP.FRONT, VP.TOP); // DO NOT add VP.Select !!
        // Pre-select the prompt text item
        vpCombo.setValue(VP.Select);
        vpCombo.valueProperty().addListener(new ChangeListener<VP>() {
            @Override
            public void changed(ObservableValue<? extends VP> ov, VP old_val, VP new_val) {
                // Ignore the prompt text item VP.Select
                if (new_val != null && new_val != VP.Select) {
                    tuxCubeSubScene.setVantagePoint(new_val);
                    // Select the prompt text item
                    vpCombo.setValue(VP.Select);
                }
            }
        });
        vpCombo.setButtonCell(new ListCell<VP>() {
            {
                this.setFont(cellFont);
            }

            @Override
            protected void updateItem(VP item, boolean empty) {
                // calling super here is very important - don't skip this!
                super.updateItem(item, empty);
                if (item != null) {
                    this.setText(item.getListName());
                }
            }
        });

        vpCombo.setCellFactory(
                new Callback<ListView<VP>, ListCell<VP>>() {
                    @Override
                    public ListCell<VP> call(ListView<VP> p) {
                        return new ListCell<VP>() {
                            {
                                this.setFont(cellFont);
                            }

                            @Override
                            protected void updateItem(VP item, boolean empty) {
                                // calling super here is very important - don't skip this!
                                super.updateItem(item, empty);
                                if (item != null) {
                                    this.setText(item.getListName());
                                }
                            }
                        };
                    }
                });

        // Tux rotation

        final HUDLabel selectFile = new HUDLabel("选择文件");
        final Button selectButton = new Button();
        selectButton.setText("打开文件");

        final Slider rotationSlider = new Slider(20, 80, 50);


        // 自动转动
        final HUDLabel tuxRotTitlelabel = new HUDLabel("转动");
        //final HUDLabel tuxRotTitlelabel = new HUDLabel("Tux");
        tuxRotTitlelabel.setTooltip(new Tooltip("start/pause rotation of Tuxes"));

        final CheckBox tuxRotCheck = new CheckBox();
        tuxRotCheck.setTooltip(new Tooltip("start/pause rotation of Tuxes"));
        tuxRotCheck.setStyle("-fx-label-padding: 0");
        tuxRotCheck.setGraphicTextGap(0);
        tuxRotCheck.setFont(textFont); // determines size of graphic !?
        tuxRotCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                isTuxRotating = !isTuxRotating;
                tuxCubeSubScene.playPauseTuxRotation(isTuxRotating);
                if (!isTuxRotating) {
                    tuxCubeSubScene.stopTuxRotation();
                    rotationSlider.setValue(50);
                    tuxCubeSubScene.setRotationSpeed(50f);
                }
                checkFPS();
//                DirectoryChooser directoryChooser=new DirectoryChooser();
//                File file = directoryChooser.showDialog(stage);
//                String path = file.getPath();//选择的文件夹路径

            }
        });

        // Cube rotation

        final HUDLabel rotationLabel = new HUDLabel("    转动方向速度控制   ");
        //final HUDLabel rotationLabel = new HUDLabel("    <  Cube Rotation  >    ");
        rotationLabel.setTooltip(new Tooltip("direction & speed of rotation"));


        rotationSlider.setBlockIncrement(0.6);
        rotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (isTuxRotating) {
                    isCubeRotating = (newValue.floatValue() < 49f || newValue.floatValue() > 51f);
                    tuxCubeSubScene.setRotationSpeed(((Double) newValue).floatValue());
                    checkFPS();
                } else {
                    tuxCubeSubScene.setRotationSpeed(50f);
                }
            }
        });
        rotationSlider.setTooltip(new Tooltip("direction & speed of rotation"));

        final HUDLabel selectColor = new HUDLabel("选择颜色");
        final ColorPicker selectColorPicker = new ColorPicker();
        selectColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                color = selectColorPicker.getValue();
                tuxCubeSubScene.setColor(drawMode, color);
                tuxRotCheck.setSelected(false);
            }
        });
        // Collect all controls

        final HUDLabel screenShot = new HUDLabel("截图保存");
        final Button screenBtn = new Button("点击保存");


        /**
         * 实例化controlPane 放置控件
         */
        final GridPane controlPane = new GridPane();
        controlPane.setHgap(10);
        controlPane.setVgap(4);
        controlPane.setGridLinesVisible(true);

//        controlPane.add(numTitleLabel, 0, 0);
//        controlPane.add(cubeCombo, 0, 1);
        controlPane.add(selectFile, 0, 0);
        controlPane.add(selectButton, 0, 1);

        controlPane.add(vpTitleLabel, 1, 0);
        controlPane.add(vpCombo, 1, 1);

        controlPane.add(tuxRotTitlelabel, 2, 0);
        controlPane.add(tuxRotCheck, 2, 1);

        controlPane.add(rotationLabel, 3, 0);
        controlPane.add(rotationSlider, 3, 1);

        controlPane.add(selectColor, 4, 0);
        controlPane.add(selectColorPicker, 4, 1);

        controlPane.add(screenShot, 5, 0);
        controlPane.add(screenBtn, 5, 1);

        /**
         * edition by czx
         * add socket viewing-sync feature
         */

        isOpeningLabel = new HUDLabel("未开放");
        isConnectingLabel = new HUDLabel("未连接");
        Button openConnectButton = new Button("开放");
        Button connectButton = new Button("连接");

        FXTuxCube fxTuxCube = this;

        connectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // IP Port String Pair and Name String consisted pair
                Dialog<Pair<Pair<String, String>, String>> dialog = new Dialog<>();
                dialog.setTitle("连接主机");
                dialog.setHeaderText("请输入你想要连接的主机和端口号,并且输入你的名字");

                // 设定两个btn的type
                ButtonType startConnBtnType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(startConnBtnType, ButtonType.CANCEL);

                // 初始化 gridPane
                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);

                // 三个需要输入的东西的TextField 以及他们旁边放的label
                TextField ip = new TextField();
                ip.setPromptText("输入你想连接的主机IP");
                TextField port = new TextField();
                port.setPromptText("输入你想连接的主机的端口号");
                TextField name = new TextField();
                name.setPromptText("输入你的名字");

                gridPane.add(new Label("Host IP:"), 0, 0);
                gridPane.add(new Label("Host port:"), 0, 1);
                gridPane.add(new Label("Name:"), 0, 2);
                gridPane.add(ip, 1, 0);
                gridPane.add(port, 1, 1);
                gridPane.add(name, 1, 2);

                // Enable/Disable login button depending on whether infos was entered
                Node connButton = dialog.getDialogPane().lookupButton(startConnBtnType);
                connButton.setDisable(true);
                name.textProperty().addListener((observable, oldValue, newValue) -> {
                    String ipStr = ip.textProperty().get();
                    String portStr = port.textProperty().get();
                    // 用trim去掉空格,防止只有空格的情况,其他的不做判断了..
                    if (!(ipStr.trim().isEmpty() || portStr.trim().isEmpty())) {
                        connButton.setDisable(newValue.trim().isEmpty());
                    } else
                        connButton.setDisable(true);
                });

                dialog.getDialogPane().setContent(gridPane);

                // Request focus on the ip field by default.
                Platform.runLater(() -> ip.requestFocus());

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == startConnBtnType) {
                        return new Pair<>(new Pair<>(ip.getText(), port.getText()), name.getText());
                    }
                    return null;
                });


                Optional<Pair<Pair<String, String>, String>> result = dialog.showAndWait();
                result.ifPresent(ipPortAndNamePair -> {
                    Pair<String, String> ipPortPair = ipPortAndNamePair.getKey();
                    System.out.println("ip=" + ipPortPair.getKey() + ", port=" + ipPortPair.getValue() + ", name=" + ipPortAndNamePair.getValue());
                });
                Pair<String, String> ipPortPair = result.get().getKey();
                String ipStr = ipPortPair.getKey();
                String portStr = ipPortPair.getValue();
                String nameStr = result.get().getValue();

                // TODO socket part
                executor = ThreadPoolConfig.getThreadPool();
                executor.execute(() -> {
                        try {
                            Socket socket = new Socket(ipStr, new Integer(portStr));
                            Thread thread = new Thread(new ClientThread(fxTuxCube, socket, nameStr));
                            thread.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                });
                isConnectingLabel.setText("已连接");
                /*try {

                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                /*--------------------------------*/


            }
        });
        openConnectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.setTitle("开放主机");
                dialog.setHeaderText("请输入你想开放的端口号以及你的名字");

                ButtonType openBtnType = new ButtonType("开放", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(openBtnType, ButtonType.CANCEL);

                // 初始化grid
                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);

                TextField port = new TextField();
                TextField name = new TextField();
                port.setPromptText("请输入你想开放的端口");
                name.setPromptText("请输入你的名字");

                gridPane.add(new Label("Host port:"), 0, 0);
                gridPane.add(port, 1, 0);
                gridPane.add(new Label("Name:"), 0, 1);
                gridPane.add(name, 1, 1);

                Node openButton = dialog.getDialogPane().lookupButton(openBtnType);
                openButton.setDisable(true);
                name.textProperty().addListener(((observable, oldValue, newValue) -> {
                    String portStr = port.textProperty().get();
                    if (!portStr.trim().isEmpty())
                        openButton.setDisable(newValue.trim().isEmpty());
                    else
                        openButton.setDisable(true);
                }));

                dialog.getDialogPane().setContent(gridPane);

                // request focus on the port field by default
                Platform.runLater(() -> port.requestFocus());

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == openBtnType) {
                        return new Pair<>(port.getText(), name.getText());
                    }
                    return null;
                });

                Optional<Pair<String, String>> result = dialog.showAndWait();

                result.ifPresent(portNamePair -> {
                    System.out.println("port=" + portNamePair.getKey() + ", name=" + portNamePair.getValue());
                });
                String portStr = result.get().getKey();
                String nameStr = result.get().getValue();

                // TODO socket part
                executor = ThreadPoolConfig.getThreadPool();
                executor.execute(new Runnable() {

                    Socket socket = null; // 和本线程相关的socket

                    ServerSocket serverSocket = null;
                    @Override
                    public void run() {
                        try {
                            serverSocket = new ServerSocket(new Integer(portStr));
                            while (true) {
                                socket = serverSocket.accept();
                                Thread thread = new Thread(new ServerThread(fxTuxCube, socket));
                                thread.start();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                isOpeningLabel.setText("已开放");
                /*
                new Thread() {

                    @Override
                    public void run() {

                    }

                }.start();*/

                /*-------------------------------*/
            }
        });


        controlPane.add(isConnectingLabel, 6, 0);
        controlPane.add(connectButton, 6, 1);
        controlPane.add(isOpeningLabel, 7, 0);
        controlPane.add(openConnectButton, 7, 1);


        /*--------------------------------------------------------------------*/
        for (int i = 0; i < 4; i++) {
            final ColumnConstraints cC = new ColumnConstraints();
            cC.setHalignment(HPos.CENTER);
            controlPane.getColumnConstraints().add(cC);
        }

        final RowConstraints topRow = new RowConstraints();
        topRow.setValignment(VPos.CENTER);

        final RowConstraints botRow = new RowConstraints();
        botRow.setValignment(VPos.CENTER);

        controlPane.getRowConstraints().addAll(topRow, botRow);


        final EventHandler onMouseEnteredHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                controlPane.setOpacity(1.0);
            }
        };
        final EventHandler onMouseExitedHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                controlPane.setOpacity(0.5);
            }
        };

        controlPane.setOpacity(0.5);
        controlPane.setOnMouseEntered(onMouseEnteredHandler);
        controlPane.setOnMouseExited(onMouseExitedHandler);

        // Layout

        // best smooth resizing if 3D-subScene is re-sized in Scene's ChangeListener
        // and HUDs in layeredPane.layoutChildren()

        // best background painting and mouse event handling
        // if subScene.setFill(Color.TRANSPARENT)
        // and layeredPane.setBackground(...). Don't use Scene.setFill.


        final double size = Math.min(screenWidth * 0.8, screenHeight * 0.8);
        subScene.setWidth(size);
        subScene.setHeight(size);

        final Group rootGroup = new Group();
        final Scene scene = new Scene(rootGroup, size, size, true);
        final ChangeListener sceneBoundsListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldXY, Object newXY) {
                subScene.setWidth(scene.getWidth());
                subScene.setHeight(scene.getHeight());
            }
        };
        scene.widthProperty().addListener(sceneBoundsListener);
        scene.heightProperty().addListener(sceneBoundsListener);


        final Pane layeredPane = new Pane() {
            @Override
            protected void layoutChildren() {

                double width = scene.getWidth();
                double height = scene.getHeight();

                titleLeftLabel.autosize();
                titleLeftLabel.relocate(border, border);

                titleRightLabel.autosize();
                titleRightLabel.relocate(width - titleRightLabel.getWidth() - border, border);

                controlPane.autosize();
                controlPane.relocate(border, height - controlPane.getHeight() - border);

                outputPane.autosize();
                outputPane.relocate(width - border - outputPane.getWidth(),
                        height - outputPane.getHeight() - border);

                pixWidthLabel.setText(numFormat.format((int) width));
                pixHeightLabel.setText(numFormat.format((int) height));
            }
        };
        layeredPane.getChildren().addAll(subScene, titleLeftLabel, titleRightLabel, controlPane, outputPane);


        // Backgrounds

        final Stop[] stopsRG = new Stop[]{new Stop(0.0, Color.LIGHTGRAY),
                new Stop(0.2, Color.BLACK),
                new Stop(1.0, Color.BLACK)};
        final RadialGradient rg = new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE, stopsRG);
        blackBG = new Background(new BackgroundFill(rg, null, null));

        final Stop[] stopsLG = new Stop[]{new Stop(0.0, Color.rgb(0, 73, 255)),
                new Stop(0.7, Color.rgb(127, 164, 255)),
                new Stop(1.0, Color.rgb(0, 73, 255))};
        final LinearGradient lg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stopsLG);
        blueBG = new Background(new BackgroundFill(lg, null, null));

        greenBG = new Background(new BackgroundFill(Color.TURQUOISE, null, null));

        layeredPane.setBackground(blueBG); // initial background

        rootGroup.getChildren().add(layeredPane);

        selectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tuxCubeSubScene = new FXTuxCubeSubScene(stage, 1);
                SubScene subScene1 = tuxCubeSubScene.getSubScene();
                subScene1.setWidth(scene.getWidth());
                subScene1.setHeight(scene.getHeight());
                scene.widthProperty().removeListener(sceneBoundsListener);
                scene.heightProperty().removeListener(sceneBoundsListener);

                scene.widthProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldXY, Object newXY) {
                        subScene1.setWidth(scene.getWidth());
                        subScene1.setHeight(scene.getHeight());
                    }
                });
                scene.heightProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldXY, Object newXY) {
                        subScene1.setWidth(scene.getWidth());
                        subScene1.setHeight(scene.getHeight());
                    }
                });
//                tuxCubeSubScene.show();
                layeredPane.getChildren().removeAll(FXTuxCube.this.subScene, titleLeftLabel, titleRightLabel, controlPane, outputPane);
                FXTuxCube.this.subScene = subScene1;
                rootGroup.getChildren().remove(layeredPane);
                layeredPane.getChildren().addAll(subScene1, titleLeftLabel, titleRightLabel, controlPane, outputPane);
                layeredPane.setBackground(blueBG);
                rootGroup.getChildren().add(layeredPane);


                tuxCubeSubScene.createTuxCubeOfDim(1, isTuxRotating, drawMode, color);
                tuxCubeSubScene.setVantagePoint(VP.CORNER);
            }
        });
        //
        // ContextMenu
        //
        screenBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("保存图片");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
                //String path = "f:\\" + System.currentTimeMillis() + ".png";
                File file = chooser.showSaveDialog(stage);
                //WritableImage image = tuxCubeSubScene.getSubScene().snapshot(new SnapshotParameters(), null);
                WritableImage image = layeredPane.snapshot(new SnapshotParameters(), null);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    showAlertDialog("保存成功!  " + file.getPath().toString());
                } catch (IOException ex) {
                    showAlertDialog("保存失败:" + ex.getMessage());
                }
            }
        });
//        screenBtn.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                String path = "f:\\" + System.currentTimeMillis() + ".png";
//                File file = new File(path);
//                //WritableImage image = tuxCubeSubScene.getSubScene().snapshot(new SnapshotParameters(), null);
//                WritableImage image = layeredPane.snapshot(new SnapshotParameters(), null);
//                try {
//                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
//                    showAlertDialog("保存成功!  " + "  图片已保存至: " + path);
//                } catch (IOException ex) {
//                    showAlertDialog("保存失败:" + ex.getMessage());
//                }
//            }
//        });
        // Projection
        //final Menu menuProjection = new Menu("Projection");
        final Menu menuProjection = new Menu("投影");
        final ToggleGroup toggleProjectionGroup = new ToggleGroup();

        final RadioMenuItem itemParallel = new RadioMenuItem("Parallel");
        itemParallel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                tuxCubeSubScene.setProjectionMode("Parallel");
            }
        });
        itemParallel.setToggleGroup(toggleProjectionGroup);
        itemParallel.setDisable(true); // Not implemented yet

        final RadioMenuItem itemPerspective = new RadioMenuItem("Perspective");
        itemPerspective.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                tuxCubeSubScene.setProjectionMode("Perspective");
            }
        });
        itemPerspective.setSelected(true);
        itemPerspective.setToggleGroup(toggleProjectionGroup);

        menuProjection.getItems().addAll(itemParallel, itemPerspective);

        // Polygon mode
        //final Menu menuPolygon = new Menu("Polygon mode");
        final Menu menuPolygon = new Menu("切换填充模式");
        final ToggleGroup togglePolygonGroup = new ToggleGroup();

        final EventHandler polyModeHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (drawMode == DrawMode.FILL) {
                    drawMode = DrawMode.LINE;
                } else {
                    drawMode = DrawMode.FILL;
                }
                tuxCubeSubScene.setDrawMode(drawMode);
            }
        };

        //final RadioMenuItem itemPolyFill = new RadioMenuItem("Fill");
        final RadioMenuItem itemPolyFill = new RadioMenuItem("纯色填充");
        itemPolyFill.setToggleGroup(togglePolygonGroup);
//        itemPolyFill.setSelected(true);//控制 填充
        itemPolyFill.setOnAction(polyModeHandler);

//        final RadioMenuItem itemPolyLine = new RadioMenuItem("Line");
        final RadioMenuItem itemPolyLine = new RadioMenuItem("线条填充");
        itemPolyLine.setToggleGroup(togglePolygonGroup);
        itemPolyLine.setSelected(true);
        itemPolyLine.setOnAction(polyModeHandler);

        menuPolygon.getItems().addAll(itemPolyFill, itemPolyLine);

        // Background
        //final Menu menuBackground = new Menu("Background");
        final Menu menuBackground = new Menu("更换背景色");
        final ToggleGroup toggleBackgroundGroup = new ToggleGroup();

        //final RadioMenuItem itemBlackBG = new RadioMenuItem("Black RadialGradient");
        final RadioMenuItem itemBlackBG = new RadioMenuItem(" 黑色 ");
        itemBlackBG.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                layeredPane.setBackground(blackBG);
            }
        });
        itemBlackBG.setToggleGroup(toggleBackgroundGroup);

        //final RadioMenuItem itemBlueBG = new RadioMenuItem("Blue LinearGradient");
        final RadioMenuItem itemBlueBG = new RadioMenuItem(" 蓝色 ");
        itemBlueBG.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                layeredPane.setBackground(blueBG);
            }
        });
        itemBlueBG.setToggleGroup(toggleBackgroundGroup);
        itemBlueBG.setSelected(true);

        //final RadioMenuItem itemGreenBG = new RadioMenuItem("Turquois Color");
        final RadioMenuItem itemGreenBG = new RadioMenuItem(" 青色 ");
        itemGreenBG.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                layeredPane.setBackground(greenBG);
            }
        });
        itemGreenBG.setToggleGroup(toggleBackgroundGroup);

        menuBackground.getItems().addAll(itemBlackBG, itemBlueBG, itemGreenBG);

        // Reset cube rotation
        //final MenuItem itemStopCubeRot = new MenuItem("Reset cube rotation");
        final MenuItem itemStopCubeRot = new MenuItem("转动复位");
        itemStopCubeRot.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                tuxCubeSubScene.stopCubeRotation();
                rotationSlider.setValue(50);
            }
        });

        // Reset tux rotation
        //final MenuItem itemStopTuxRot = new MenuItem("Reset Tux rotation");
        final MenuItem itemStopTuxRot = new MenuItem("自转控制复位");
        itemStopTuxRot.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (isTuxRotating) {
                    tuxRotCheck.setSelected(false);
                }
                tuxCubeSubScene.stopTuxRotation();
            }
        });

//        final MenuItem itemChangeColor = new MenuItem("更改颜色");
//        itemChangeColor.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                tuxCubeSubScene.setColor(drawMode,color);
//                tuxRotCheck.setSelected(false);
//            }
//        });

        // Scene anti-aliasing mode
        final Menu menuSceneAA = new Menu("抗锯齿");
        final ToggleGroup toggleGroupSceneAA = new ToggleGroup();

        final RadioMenuItem itemBalancedAA = new RadioMenuItem("抗锯齿-开");
        itemBalancedAA.setToggleGroup(toggleGroupSceneAA);
        itemBalancedAA.setSelected(true);

        final RadioMenuItem itemDisabledAA = new RadioMenuItem("抗锯齿-关");
        itemDisabledAA.setToggleGroup(toggleGroupSceneAA);

        final EventHandler sceneAAHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                SceneAntialiasing sceneAA = SceneAntialiasing.DISABLED;
                if (toggleGroupSceneAA.getSelectedToggle() == itemBalancedAA) {
                    sceneAA = SceneAntialiasing.BALANCED;
                }

                // Exchange SubScene

                subScene = tuxCubeSubScene.exchangeSubScene(sceneAA);
                layeredPane.getChildren().set(0, subScene);
            }
        };
        itemBalancedAA.setOnAction(sceneAAHandler);
        itemDisabledAA.setOnAction(sceneAAHandler);

        menuSceneAA.getItems().addAll(itemBalancedAA, itemDisabledAA);

        // Exit

        final MenuItem itemExit = new MenuItem("退出应用");
        //final MenuItem itemExit = new MenuItem("Exit");
        itemExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                exit();
            }
        });

        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setHideOnEscape(true);
        contextMenu.getItems().addAll(menuBackground, menuPolygon,
                new SeparatorMenuItem(),
                itemStopTuxRot,
                menuSceneAA,
                new SeparatorMenuItem(),
                itemExit);

        layeredPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (contextMenu.isShowing()) {
                    contextMenu.hide();
                }
                if (e.getButton() == MouseButton.SECONDARY && !isMouseDragged) {
                    contextMenu.show(layeredPane, e.getScreenX() + 2, e.getScreenY() + 2);
                }

                isMouseDragged = false;
                checkFPS();
            }
        });
        layeredPane.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                isMouseDragged = true;

                checkFPS();
            }
        });

        //
        // Stage
        //

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                exit();
            }
        });
        stage.setScene(scene);
        // stage.setTitle("InteractiveMesh : FXTuxCube 0.7.1");
        stage.setTitle("  三维浏览器  ");

        stage.show();

        // Initial states

        lastTime = System.nanoTime();
        /**
         * 初始化
         */
        tuxCubeSubScene.createTuxCubeOfDim(1, isTuxRotating, drawMode, color);
        tuxCubeSubScene.setVantagePoint(VP.CORNER);

        //
        fpsTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (isUpdateFPS == false) {
                    return;
                }

                frameCounter++;

                if (frameCounter > elapsedFrames) {

                    final long currTime = System.nanoTime();
                    final double duration = ((double) (currTime - lastTime)) / frameCounter;
                    lastTime = currTime;

                    // frames per second
                    final int fps = (int) (1000000000d / duration + 0.5);
                    // milliseconds per frame
                    final int mpf = (int) (duration / 1000000d + 0.5);

                    frameCounter = 0;
                    elapsedFrames = (int) Math.max(1, (fps / 3f)); // update: 3 times per sec
                    elapsedFrames = Math.min(100, elapsedFrames);

                    fpsLabel.setText(Integer.toString(fps));
//                    mpfLabel.setText(Integer.toString(mpf));
                }
                /*
                float fpsf = PerformanceTracker.getSceneTracker(scene).getInstantFPS();
                System.out.println("fps     = " + fpsf + 
                                 "\npulses  = " + PerformanceTracker.getSceneTracker(scene).getInstantPulses() +
                                 "\nfps avg = " + PerformanceTracker.getSceneTracker(scene).getAverageFPS());
                */
            }
        };
    }

    private void checkFPS() {
        boolean isRendering = isTuxRotating || isCubeRotating || isMouseDragged;
        if (isUpdateFPS != isRendering) {
            isUpdateFPS = isRendering;
        } else {
            return;
        }

        if (isUpdateFPS == false) {
            fpsTimer.stop();
            fpsLabel.setText(Integer.toString(0));
//            mpfLabel.setText(Integer.toString(0));
        } else {
            fpsTimer.start();
        }
    }

    private void exit() {
        System.exit(0);
    }

    // HUD : head-up display
    private final class HUDLabel extends Label {
        private HUDLabel(String text) {
            this(text, textFont);
        }

        private HUDLabel(String text, Font font) {
            super(text);
            setFont(font);
            setTextFill(Color.WHITE);
        }
    }

    private void showAlertDialog(String txt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText("保存图片");
        alert.setContentText(txt);
        alert.showAndWait();
    }

}
