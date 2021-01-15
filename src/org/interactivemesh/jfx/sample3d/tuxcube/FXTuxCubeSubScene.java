
package org.interactivemesh.jfx.sample3d.tuxcube;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;

import javafx.collections.ObservableList;

import javafx.event.EventHandler;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;

import javafx.scene.shape.*;

import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

/**
 *
 */
final class FXTuxCubeSubScene {

    private int TAG = 0;

    enum VP {

        Select("Select"),//
        BOTTOM("底部"),
        CORNER("全局"),
        FRONT("正面"),
        TOP("顶部");

        VP(String listName) {
            this.listName = listName;
        }

        private String listName;

        String getListName() {
            return listName;
        }
    }

    private SubScene subScene = null;

    private Group viewingGroup = null;
    private Affine viewingRotate = new Affine();
    private Translate viewingTranslate = new Translate();
    private double startX = 0;
    private double startY = 0;

    private PerspectiveCamera perspectiveCamera = null;

    private AmbientLight ambLight = null;
    private PointLight pointLight = null;

    private Group tuxCubeRotGroup = null;
    private Group tuxCubeCenterGroup = null;

    private Rotate tuxCubeRotate = null;
    private Timeline tuxCubeRotTimeline = null;

    private RotateTransition[] tuxRotTransAll = null;

    private BoundingBox tuxCubeBinL = null;
    private double sceneDiameter = 0;

    private MeshView[] meshViews = null;

    private PhongMaterial eyesMat = null;
    private PhongMaterial feetMat = null;
    private PhongMaterial mouthMat = null;
    private PhongMaterial pupilsMat = null;

    private TriangleMesh bodyMesh = null;
    private TriangleMesh frontMesh = null;
    private TriangleMesh eyesMesh = null;
    private TriangleMesh feetMesh = null;
    private TriangleMesh mouthMesh = null;
    private TriangleMesh pupilsMesh = null;
    private TriangleMesh[] triangleMeshes = null;
    private PhongMaterial[] phongMaterials = null;
    private PhongMaterial phongMaterial = null;
    private Stage stage = null;
    private int tag = 0;

    FXTuxCubeSubScene(Stage stage, int tag) {
        this.stage = stage;
        this.tag = tag;
        show();
    }

    public void show() {
        if (loadTuxModel(stage)) {
            createBaseScene();
            createSubScene(800, 800, SceneAntialiasing.BALANCED);
        }
    }

    private boolean loadTuxModel(Stage stage) {

        //
        // 3D model importer
        //

        final ObjModelImporter objImp = new ObjModelImporter();
        try {

            if (tag != 0) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择模型文件");
                File file = fileChooser.showOpenDialog(stage);
                try {
                    final URL modelUrl = file.toURL();
                    objImp.read(modelUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                tag = 100;
                final URL modelUrl = this.getClass().getResource("resources/TuxModel-NoTongue.obj");
                objImp.read(modelUrl);
            }

        } catch (ImportException e) {
            e.printStackTrace();
            return false;
        }

        final Map<String, MeshView> namedMeshViews = objImp.getNamedMeshViews();
        final Map<String, PhongMaterial> namedPhgMats = objImp.getNamedMaterials();

        objImp.close();

        //
        // Re-used PhongMaterials and TriangleMeshes
        //

        /**
         * 颜色
         */

//        mouthMat = namedPhgMats.get("TuxMouthAppear");
//        feetMat = namedPhgMats.get("TuxFeetAppear");
//        eyesMat = namedPhgMats.get("TuxEyesAppear");
//        pupilsMat = namedPhgMats.get("TuxPupilsAppear");
//
//
//        eyesMesh = (TriangleMesh)namedMeshViews.get("TuxEyes").getMesh();


        triangleMeshes = new TriangleMesh[namedMeshViews.size()];

        int i = 0;

        for (String key :
                namedMeshViews.keySet()) {
            triangleMeshes[i++] = ((TriangleMesh) namedMeshViews.get(key).getMesh());
        }

        phongMaterials = new PhongMaterial[namedPhgMats.size()];

        int j = 0;

        for (String key :
                namedPhgMats.keySet()) {
            phongMaterials[j++] = namedPhgMats.get(key);
        }


//        feetMesh = (TriangleMesh)namedMeshViews.get("TuxFeet").getMesh();
//
//
//        mouthMesh = (TriangleMesh)namedMeshViews.get("TuxMouth").getMesh();
//
//
//        pupilsMesh = (TriangleMesh)namedMeshViews.get("TuxPupils").getMesh();
//
//        bodyMesh = (TriangleMesh)namedMeshViews.get("TuxBody").getMesh();
//
//        frontMesh = (TriangleMesh)namedMeshViews.get("TuxFront").getMesh();

        return true;
    }

    private void createBaseScene() {

        //
        // Viewing : Camera & Light
        //

        // SubScene's camera
        perspectiveCamera = new PerspectiveCamera(true);
        perspectiveCamera.setVerticalFieldOfView(false);
        perspectiveCamera.setFarClip(250);
        perspectiveCamera.setNearClip(0.1);
        perspectiveCamera.setFieldOfView(44);

        // SubScene's lights
        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(-20000);

        ambLight = new AmbientLight(Color.color(0.3, 0.3, 0.3));

        // Viewing group: camera and headlight
        viewingGroup = new Group(perspectiveCamera, pointLight);
        viewingGroup.getTransforms().setAll(viewingRotate, viewingTranslate);

        //
        // Group hierarchy of the cube
        //

        // Centers the entire cube at (0,0,0)
        tuxCubeCenterGroup = new Group();

        // Cube rotation target
        tuxCubeRotGroup = new Group(tuxCubeCenterGroup);

        tuxCubeRotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);

        final KeyValue kv0 = new KeyValue(tuxCubeRotate.angleProperty(), 0, Interpolator.LINEAR);
        final KeyValue kv1 = new KeyValue(tuxCubeRotate.angleProperty(), 360, Interpolator.LINEAR);
        final KeyFrame kf0 = new KeyFrame(Duration.millis(0), kv0);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(50000), kv1); // min speed, max duration

        tuxCubeRotTimeline = new Timeline();
        tuxCubeRotTimeline.setCycleCount(Timeline.INDEFINITE);
        tuxCubeRotTimeline.getKeyFrames().setAll(kf0, kf1);

        tuxCubeRotGroup.getTransforms().setAll(tuxCubeRotate);
    }

    private void createSubScene(final double width, final double height, final SceneAntialiasing sceneAA) {

        //
        // SubScene & Root 
        //
        final Group subSceneRoot = new Group();

        subScene = new SubScene(subSceneRoot, width, height, true, sceneAA);

        // otherwise subScene doesn't receive mouse events                  TODO bug ??   
        subScene.setFill(Color.TRANSPARENT);

        // Perspective camera
        subScene.setCamera(perspectiveCamera);

        // Add all to SubScene
        subSceneRoot.getChildren().addAll(tuxCubeRotGroup, viewingGroup, ambLight);

        // Navigator on SubScene

        final Rotate viewingRotX = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
        final Rotate viewingRotY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);

        subScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
//System.out.println("OnMouseDragged " + event);
                if (event.getButton() == MouseButton.PRIMARY) {
                    viewingRotX.setAngle((startY - event.getSceneY()) / 10);
                    viewingRotY.setAngle((event.getSceneX() - startX) / 10);
                    viewingRotate.append(viewingRotX.createConcatenation(viewingRotY));
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    viewingTranslate.setX(viewingTranslate.getX() + (startX - event.getSceneX()) / 100);
                    viewingTranslate.setY(viewingTranslate.getY() + (startY - event.getSceneY()) / 100);
                } else if (event.getButton() == MouseButton.MIDDLE) {
                    viewingTranslate.setZ(viewingTranslate.getZ() + (event.getSceneY() - startY) / 40);
                }

                startX = event.getSceneX();
                startY = event.getSceneY();
            }
        });
        subScene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                //System.out.println("OnScroll event.getDeltaY() = " + event.getDeltaY());
                viewingTranslate.setZ(viewingTranslate.getZ() - event.getDeltaY() / 40);
                //System.out.println("OnScroll viewingTransZ.getZ = " + viewingTransZ.getZ());
            }
        });
        subScene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                startX = event.getSceneX();
                startY = event.getSceneY();
//System.out.println("OnMousePressed = " + event);
            }
        });
    }

    public ViewAttribute getVA() {
        double mxx = viewingRotate.getMxx();
        double mxy = viewingRotate.getMxy();
        double mxz = viewingRotate.getMxz();
        double myx = viewingRotate.getMyx();
        double myy = viewingRotate.getMyy();
        double myz = viewingRotate.getMyz();
        double mzx = viewingRotate.getMzx();
        double mzy = viewingRotate.getMzy();
        double mzz = viewingRotate.getMzz();
        double xt = viewingRotate.getTx();
        double yt = viewingRotate.getTy();
        double zt = viewingRotate.getTz();
        return new ViewAttribute(viewingTranslate.getX(), viewingTranslate.getY(), viewingTranslate.getZ(),
                mxx, mxy, mxz, xt,
                myx, myy, myz, yt,
                mzx, mzy, mzz, zt);
    }

    public void applyViewAttribute(ViewAttribute va) {
//            @Override
//            public void handle(MouseEvent event) {
////System.out.println("OnMouseDragged " + event);
//                if (event.getButton() == MouseButton.PRIMARY) {
//                    viewingRotX.setAngle((startY - event.getSceneY()) / 10);
//                    viewingRotY.setAngle((event.getSceneX() - startX) / 10);
//                    viewingRotate.append(viewingRotX.createConcatenation(viewingRotY));
//                } else if (event.getButton() == MouseButton.SECONDARY) {
//                    viewingTranslate.setX(viewingTranslate.getX() + (startX - event.getSceneX()) / 100);
//                    viewingTranslate.setY(viewingTranslate.getY() + (startY - event.getSceneY()) / 100);
//                } else if (event.getButton() == MouseButton.MIDDLE) {
//                    viewingTranslate.setZ(viewingTranslate.getZ() + (event.getSceneY() - startY) / 40);
//                }
//
//                startX = event.getSceneX();
//                startY = event.getSceneY();
//            }
//        final Rotate viewingRotX = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
//        final Rotate viewingRotY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
//
//
//        viewingRotX.setAngle(va.viewingTranslate.ge);

//        viewingRotate.append(viewingRotX.createConcatenation(viewingRotY));
//        startX = va.startX;
//        startY = va.startY;
//        System.out.println(va);

        viewingRotate.setMxx(va.xx);
        viewingRotate.setMxy(va.xy);
        viewingRotate.setMxz(va.xz);
        viewingRotate.setMyx(va.yx);
        viewingRotate.setMyy(va.yy);
        viewingRotate.setMyz(va.yz);
        viewingRotate.setMzx(va.zx);
        viewingRotate.setMzy(va.zy);
        viewingRotate.setMzz(va.zz);
        viewingRotate.setTx(va.xt);
        viewingRotate.setTy(va.yt);
        viewingRotate.setTz(va.zt);

        viewingTranslate.setX(va.tX);
        viewingTranslate.setY(va.tY);
        viewingTranslate.setZ(va.tZ);
    }

    SubScene getSubScene() {
        return subScene;
    }

    SubScene exchangeSubScene(final SceneAntialiasing sceneAA) {

        // Clear current SubScene
        ((Group) subScene.getRoot()).getChildren().clear();
        subScene.setCamera(null);
        subScene.setOnMouseDragged(null);
        subScene.setOnScroll(null);
        subScene.setOnMousePressed(null);

        // Create and return a new SubScene
        createSubScene(subScene.getWidth(), subScene.getHeight(), sceneAA);

        return subScene;
    }

    void setColor(DrawMode drawMode, Color color) {
        //phongMaterial = phongMaterials[((int) (Math.random() * phongMaterials.length))];
        if (drawMode == DrawMode.LINE) {
            createTuxCubeOfDim(1, false, DrawMode.LINE, color);
        } else {
            createTuxCubeOfDim(1, false, DrawMode.FILL, color);
        }


    }

    // 
    void createTuxCubeOfDim(int dim, boolean play, DrawMode drawMode, Color color) {

        // Tux : 13.744 triangles, 6 MeshViews

        // TuxBody :   3.856
        // TuxEyes :   1.056
        // TuxFeet :   4.640
        // TuxFront :    192
        // TuxMouth :  2.944
        // TuxPupils : 1.056

        //          #Tux             triangles  MeshViews
        // dim  1 :    1 * 13.744 =     13.744          6
        // dim  2 :    8 * 13.744 =    109.952         48
        // dim  3 :   27 * 13.744 =    371.088        162
        // dim  4 :   64 * 13.744 =    879.616        384
        // dim  5 :  125 * 13.744 =  1.718.000        750
        // dim  6 :  216 * 13.744 =  2.968.704      1.296
        // dim  7 :  343 * 13.744 =  4.714.192      2.058
        // dim  8 :  512 * 13.744 =  7.036.928      3.072
        // dim  9 :  729 * 13.744 = 10.019.376      4.374
        // dim 10 : 1000 * 13.744 = 13.744.000      6.000
        // dim 11 : 1331 * 13.744 = 18.293.264      7.986
        // dim 12 : 1728 * 13.744 = 23.749.632     10.368

        // Clear cube
        tuxCubeCenterGroup.getChildren().clear();

        // Center Tux for rotation
        final double transZ = -0.01396;

        final int xDist = 2;
        final int yDist = 2;
        final int zDist = 2;

        long delay = 4;
        final long delayIncr = 4;

        Group tuxCenterGroup = null;    // center Tux at (0,y,0)
        Group tuxRotationGroup = null;  // target for rotate transition
        Group tuxPositionGroup = null;  // position Tux within the cube

        // Appearances for body and front
        final PhongMaterial[] materials = createMaterials(dim, color);

        final int numTux = dim * dim * dim;
        final double maxTux = 10 * 10 * 10;

        int n = 0;
        int nx6 = 0;
//        int t = materials.length-1;
        int t = 0;

        float xTrans = 0;
        float yTrans = 0;
        float zTrans = 0;

        tuxRotTransAll = new RotateTransition[numTux];
        meshViews = new MeshView[numTux * triangleMeshes.length];

        final ObservableList<Node> children = tuxCubeCenterGroup.getChildren();

        for (int i = 0; i < dim; i++) {               // z axis
            for (int j = 0; j < dim; j++) {           // y axis
                for (int k = 0; k < dim; k++) {       // x axis

                    nx6 = n * triangleMeshes.length;

                    // group hierarchy from bottom to top
                    // 1.
                    tuxCenterGroup = new Group();
                    tuxCenterGroup.setTranslateZ(transZ);
                    for (int num = 0; num < triangleMeshes.length; num++) {
                        tuxCenterGroup.getChildren().add(createMeshView(triangleMeshes[t], materials[0], meshViews, nx6 + (t++)));
                    }


//                    tuxCenterGroup.getChildren().addAll(
//                        createMeshView(n, eyesMat, meshViews, nx6),
//                        createMeshView(feetMesh, feetMat, meshViews, nx6+1),
//                        createMeshView(mouthMesh, mouthMat, meshViews, nx6+2),
//                        createMeshView(pupilsMesh, pupilsMat, meshViews, nx6+3),
//                            createMeshView(triangleMeshes[t], materials[t], meshViews, nx6+(t++)),
//                            createMeshView(feetMesh, feetMat, meshViews, nx6+1),
//                            createMeshView(mouthMesh, mouthMat, meshViews, nx6+2),
//                            createMeshView(pupilsMesh, pupilsMat, meshViews, nx6+3),
//                            /**
//                             *  color
//                             */
////                        createMeshView(bodyMesh, materials[t], meshViews, nx6+4),
//                        createMeshView(bodyMesh, null, meshViews, nx6+4),
//                        createMeshView(frontMesh, null, meshViews, nx6+5));
                    //                      createMeshView(frontMesh, materials[t--], meshViews, nx6+5));
                    // 2.
                    tuxRotationGroup = new Group(tuxCenterGroup);

//System.out.println("tuxCenterGroup 0 = " + tuxCenterGroup.getBoundsInLocal());
// Tux BoundsInLocal
// minX:-0.81812, minY:-1.45735, minZ:-0.28825 
// maxX: 0.81812, maxY:-0.01011, maxZ: 0.31617                    
//System.out.println("tuxCenterGroup 1 =  " + tuxCenterGroup.getBoundsInParent());
// Tux BoundsInParent
// minX:-0.81812, minY:-1.45735, minZ:-0.30221
// maxX: 0.81812, maxY:-0.01011, maxZ: 0.30221

                    RotateTransition rotateTransition = new RotateTransition();
                    rotateTransition.setNode(tuxRotationGroup);
                    rotateTransition.setAxis(Rotate.Y_AXIS);
                    rotateTransition.setDelay(Duration.millis(delay));
                    rotateTransition.setDuration(Duration.millis(5000 - 2 * n * (maxTux / numTux)));
                    rotateTransition.setCycleCount(Timeline.INDEFINITE);
                    rotateTransition.setAutoReverse(false);
                    rotateTransition.setInterpolator(Interpolator.LINEAR);
                    rotateTransition.setByAngle(360);
                    tuxRotTransAll[n] = rotateTransition;

                    // 3.
                    tuxPositionGroup = new Group(tuxRotationGroup);
                    tuxPositionGroup.setTranslateX(xTrans);
                    tuxPositionGroup.setTranslateY(yTrans);
                    tuxPositionGroup.setTranslateZ(zTrans);
                    children.add(tuxPositionGroup);

                    xTrans += xDist;
                    delay = delayIncr * n;
                    n++;
                }                                   // x axis

                xTrans = 0;
                yTrans += yDist;
            }                                       // y axis

            yTrans = 0;
            zTrans += zDist;

            t = 0;
        }                                           // z axis

        tuxCubeBinL = (BoundingBox) tuxCubeCenterGroup.getBoundsInLocal();
        //System.out.println("tuxCubeBinL " + tuxCubeBinL);
        tuxCubeCenterGroup.setTranslateX(-(tuxCubeBinL.getMinX() + tuxCubeBinL.getMaxX()) / 2.0);
        tuxCubeCenterGroup.setTranslateY(-(tuxCubeBinL.getMinY() + tuxCubeBinL.getMaxY()) / 2.0);
        tuxCubeCenterGroup.setTranslateZ(-(tuxCubeBinL.getMinZ() + tuxCubeBinL.getMaxZ()) / 2.0);

        sceneDiameter = Math.sqrt(Math.pow(tuxCubeBinL.getWidth(), 2) + Math.pow(tuxCubeBinL.getHeight(), 2) + Math.pow(tuxCubeBinL.getDepth(), 2));

        playPauseTuxRotation(play);

        if (drawMode == DrawMode.LINE) {
            setDrawMode(drawMode);
        }
    }

    /**
     * 可以用这里的参数，参考来实现视角的定位（camera的位置和rotate角度）
     *
     * @param vp view point视角的枚举
     */
    void setVantagePoint(VP vp) {

        Transform rotate = null;

        final double distance = distToSceneCenter(sceneDiameter / 2);

        switch (vp) {
            case BOTTOM:
                rotate = new Rotate(90, Rotate.X_AXIS);
                break;
            case CORNER:
                Rotate rotateX = new Rotate(-45, Rotate.X_AXIS);
                Rotate rotateY = new Rotate(-45, new Point3D(0, 1, 1).normalize());
                rotate = rotateX.createConcatenation(rotateY);
                break;
            case FRONT:
                rotate = new Rotate();
                break;
            case TOP:
                rotate = new Rotate(-90, Rotate.X_AXIS);
                break;
        }

        viewingRotate.setToTransform(rotate);

        viewingTranslate.setX(0);
        viewingTranslate.setY(0);
        viewingTranslate.setZ(-distance);

    }

    void playPauseTuxRotation(boolean play) {
        if (play) {
            for (RotateTransition rot : tuxRotTransAll) {
                rot.play();
            }
        } else {
            for (RotateTransition rot : tuxRotTransAll) {
                rot.pause();
            }
        }
    }

    void stopTuxRotation() {
        for (RotateTransition rot : tuxRotTransAll) {
            rot.stop();
            rot.getNode().setRotate(0);
        }
    }

    void stopCubeRotation() {
        tuxCubeRotTimeline.stop();
        tuxCubeRotate.setAngle(0);
    }

    // range: [20 ... 50 ... 80]
    void setRotationSpeed(float speed) {
        if (speed < 49f) {
            tuxCubeRotTimeline.play();
            tuxCubeRotTimeline.setRate(1 * (49 - speed));
        } else if (speed > 51f) {
            tuxCubeRotTimeline.play();
            tuxCubeRotTimeline.setRate(-1 * (speed - 51)); // negative rate works only while Timeline is playing !!
        } else {
            tuxCubeRotTimeline.pause();
        }
    }

    void setDrawMode(DrawMode drawMode) {
        for (MeshView mv : meshViews) {
            mv.setDrawMode(drawMode);
        }
    }

    // TODO
    void setProjectionMode(String mode) {
        if (mode.equalsIgnoreCase("Parallel")) {

        } else {

        }
    }

    private MeshView createMeshView(Mesh mesh, Material material, MeshView[] meshViews, int index) {
        final MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        meshViews[index] = meshView;
        return meshView;
    }

    private PhongMaterial[] createMaterials(int dim, Color color) {

        final PhongMaterial[] materials = new PhongMaterial[dim * dim];

        int k = 0;
        int direction = 1;
        float hue = 0;

        final float step = 1.0f / (dim * dim);

//        for (int i = 0; i < dim; i++) {
//            for (int j = 0; j < dim; j++) {

//System.out.println("hue = " + hue);
        materials[k] = new PhongMaterial();
//                java.awt.Color hsbCol = new java.awt.Color(java.awt.Color.HSBtoRGB(hue, 0.85f, 0.7f));
//                materials[k].setDiffuseColor(Color.rgb(hsbCol.getRed(), hsbCol.getGreen(), hsbCol.getBlue()));
        //materials[k].setDiffuseColor(Color.hsb(hue, 0.85, 0.7));
        materials[k].setDiffuseColor(color);
//                materials[k].setSpecularPower(16f);

//                hue += step * direction;
//                k++;
//            }

//            direction *= (-1);
//            if (direction < 0) {
//                hue += step * (dim - 1);
//            } else {
//                hue += step * (dim + 1);
//            }

//            hue *= 360;
//        }

        return materials;
    }

    private double distToSceneCenter(double sceneRadius) {
        // Extra space
        final double borderFactor = 1.0;

        final double fov = perspectiveCamera.getFieldOfView();

        final double c3dWidth = subScene.getWidth();
        final double c3dHeight = subScene.getHeight();
        // Consider ratio of canvas' width and height
        double ratioFactor = 1.0;
        if (c3dWidth > c3dHeight) {
            ratioFactor = c3dWidth / c3dHeight;
        }
//System.out.println("sceneRadius       = " + sceneRadius);

        final double distToSceneCenter = borderFactor * ratioFactor * sceneRadius / Math.tan(Math.toRadians(fov / 2));
//System.out.println("distToSceneCenter = " + distToSceneCenter);
        return distToSceneCenter;
    }

}
