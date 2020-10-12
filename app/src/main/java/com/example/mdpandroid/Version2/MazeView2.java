package com.example.mdpandroid.Version2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.mdpandroid.R;

import java.util.ArrayList;
import java.util.List;

public class MazeView2 extends View {

    private static final int NUM_COLUMNS = 15;  // Range of X-axis (if start from 1)
    private static final int NUM_ROWS = 20;     // Range of Y-axis
    private static final float WALL_THICKNESS = 4;
    private MazeView2.Cell[][] cells;
    private int[] exploredGrid, obstacleGrid;
    private int cellWidth, cellHeight;
    private float cellSize;
    List<Integer> numberIDX = new ArrayList<>(), imageIDX = new ArrayList<>(); // x-coordinate of identified image
    List<Integer> numberIDY = new ArrayList<>(), imageIDY = new ArrayList<>(); // y-coordinate of identified image
    List<String> numberID = new ArrayList<>();
    List<Integer> imageID = new ArrayList<>(); // numberid of identified image
    List<Integer> robotX = new ArrayList<>(), robotY = new ArrayList<>();

    int[] images = new int[]{R.drawable.up_arrow_1, R.drawable.down_arrow_2, R.drawable.right_arrow_3,
            R.drawable.left_arrow_4, R.drawable.go_5, R.drawable.six_6, R.drawable.seven_7,
            R.drawable.eight_8, R.drawable.nine_9, R.drawable.zero_10, R.drawable.alphabet_v_11,
            R.drawable.alphabet_w_12, R.drawable.alphabet_x_13, R.drawable.alphabet_y_14, R.drawable.alphabet_z_15};

    private Paint blackPaint, whitePaint;
    private Paint goalPaint, startPaint, mapPaint, robotPaint, waypointPaint, exploredPaint, fastestPaint;
    private final String DEFAULTAR = "AR,AN,"; // Sending to Arudino

    MapFragment mapFragment = MapFragment.getInstance();

    private int[] waypoint = {1, 1}; // waypoint
    // robot starting coordinates
    private int[] robotFront = {1, 2};
    public int[] robotCenter = {1, 1};
    int angle = 270;

    public boolean showImageRecognise = false;

    // store obstacles (x,y)
    ArrayList<String> obsArray = new ArrayList<>();

    @SuppressLint("ResourceAsColor")
    public MazeView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        blackPaint = new Paint();
        whitePaint = new Paint();

        goalPaint = new Paint();
        startPaint = new Paint();
        mapPaint = new Paint();
        robotPaint = new Paint();
        waypointPaint = new Paint();
        exploredPaint = new Paint();
        fastestPaint = new Paint();

        blackPaint.setColor(Color.BLACK); // obstacle and walls
        blackPaint.setStrokeWidth(WALL_THICKNESS);
        whitePaint.setColor(Color.WHITE); // robot body

        goalPaint.setColor(Color.rgb(142, 226, 195));
        startPaint.setColor(Color.rgb(142, 220, 226));
        mapPaint.setColor(Color.LTGRAY);
        robotPaint.setColor(Color.DKGRAY);
        waypointPaint.setColor(Color.rgb(192, 131, 245)); // 94 94 94
        exploredPaint.setColor(Color.rgb(142, 175, 226));
        fastestPaint.setColor(Color.rgb(204, 129, 129));

        createMaze();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // calculate the cellsize based on the canvas
        cellWidth = getWidth() / NUM_COLUMNS;
        cellHeight = getHeight() / NUM_ROWS;

        if (cellWidth > cellHeight)
            cellWidth = cellHeight;
        else
            cellHeight = cellWidth;
        cellSize = cellHeight;

        this.setLayoutParams(new RelativeLayout.LayoutParams(cellWidth * NUM_COLUMNS, cellHeight * NUM_ROWS));
        invalidate();
    }

    public void changeImageRecogniseSettings(boolean showImage) {
        showImageRecognise = showImage;
        if (mapFragment.autoUpdate)
            invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        drawGrids(canvas);
        drawExploredObstacles(canvas);
        if (showImageRecognise)
            displayImageIdentified(canvas);
        else
            displayNumberIdentified(canvas);
        drawStartZone(canvas);
        drawGoalZone(canvas);
        displayWaypoint(canvas);
        displayFastestPath(canvas);
        drawGridLines(canvas);
        displayRobot(canvas);

    }

    // Normal grids
    private void drawGrids(Canvas canvas) {
        for (int i = 0; i <= NUM_COLUMNS - 1; i++)
            for (int j = 0; j <= NUM_ROWS - 1; j++)
                canvas.drawRect(i * cellWidth, (NUM_ROWS - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (NUM_ROWS - j) * cellHeight, mapPaint);
    }

    // startZone
    private void drawStartZone(Canvas canvas) {
        for (int i = 0; i <= 2; i++)
            for (int j = 0; j <= 2; j++)
                canvas.drawRect(i * cellWidth, (NUM_ROWS - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (NUM_ROWS - j) * cellHeight, startPaint);
    }

    // goalZone
    private void drawGoalZone(Canvas canvas) {
        for (int i = 12; i <= NUM_COLUMNS - 1; i++)
            for (int j = 17; j <= NUM_ROWS - 1; j++)
                canvas.drawRect(i * cellWidth, (NUM_ROWS - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (NUM_ROWS - j) * cellHeight, goalPaint);
    }

    // Grid drawing lines
    private void drawGridLines(Canvas canvas) {
        for (int c = 0; c < NUM_COLUMNS + 1; c++)
            canvas.drawLine(c * cellWidth, 0, c * cellWidth, NUM_ROWS * cellHeight, whitePaint);
        for (int r = 0; r < NUM_ROWS + 1; r++)
            canvas.drawLine(0, r * cellHeight, NUM_COLUMNS * cellWidth, r * cellHeight, whitePaint);
    }

    // displaying robot position when user taps
    private void displayRobot(Canvas canvas) {
        if (robotCenter[0] >= 0) {
            canvas.drawCircle(robotCenter[0] * cellWidth + cellWidth / 2,
                    (NUM_ROWS - robotCenter[1]) * cellHeight - cellHeight / 2, 1.3f * cellWidth, robotPaint);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot_head);
            Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, cellWidth * 3, cellHeight * 3, false);
            Matrix m = new Matrix();
            m.setRotate(angle, resizeBitmap.getWidth(), resizeBitmap.getHeight());
            switch (angle) {
                case 0:
                    m.postTranslate((robotCenter[0] - 1) * cellWidth, (NUM_ROWS - robotCenter[1] - 2) * cellHeight);
                    break;
                case 90:
                    m.postTranslate((robotCenter[0] - 4) * cellWidth, (NUM_ROWS - robotCenter[1] - 2) * cellHeight);
                    break;
                case 180:
                    m.postTranslate((robotCenter[0] - 4) * cellWidth, (NUM_ROWS - robotCenter[1] - 5) * cellHeight);
                    break;
                case 270:
                    m.postTranslate((robotCenter[0] - 1) * cellWidth, (NUM_ROWS - robotCenter[1] - 5) * cellHeight);
                    break;
                default:
                    m.postTranslate((robotCenter[0] - 1) * cellWidth, (NUM_ROWS - robotCenter[1] - 2) * cellHeight);
                    break;
            }
            canvas.drawBitmap(resizeBitmap, m, whitePaint);
        }
    }

    // display the waypoint when user taps
    private void displayWaypoint(Canvas canvas) {
        if (waypoint[0] >= 0) {
            canvas.drawRect(waypoint[0] * cellWidth, (NUM_ROWS - 1 - waypoint[1]) * cellHeight,
                    (waypoint[0] + 1) * cellWidth, (NUM_ROWS - waypoint[1]) * cellHeight, waypointPaint);
        }
    }

    // Obstacles and explored
    private void drawExploredObstacles(Canvas canvas) {
        int gridExplored = 0, obsIndex = 0;
        for (int y = 0; y < NUM_ROWS; y++)
            for (int x = 0; x < NUM_COLUMNS; x++) {
                // when explored then draw obstacle if any
                if (exploredGrid != null && exploredGrid[gridExplored] == 1) {
                    int left = x * cellWidth, top = (NUM_ROWS - 1 - y) * cellHeight,
                            right = (x + 1) * cellWidth, bottom = (NUM_ROWS - y) * cellHeight;
                    canvas.drawRect(left, top, right, bottom, exploredPaint);
                    if (obstacleGrid != null && obstacleGrid[obsIndex] == 1)
                        canvas.drawRect(left, top, right, bottom, blackPaint);
                    obsIndex++;
                }
                gridExplored++;
            }
    }

    private void displayFastestPath(Canvas canvas) {
//        if (mapFragment.fastest)
        if (robotX != null && robotY != null)
            if (robotX != null && robotY != null)
                for (int i = 0; i < robotX.size(); i++)
                    canvas.drawRect(robotX.get(i) * cellWidth, (NUM_ROWS - 1 - robotY.get(i)) * cellHeight,
                            (robotX.get(i) + 1) * cellWidth, (NUM_ROWS - robotY.get(i)) * cellHeight, fastestPaint);
    }

    // Numberid drawings on obstacle
    private void displayNumberIdentified(Canvas canvas) {
        if (numberID != null && numberIDY != null && numberIDX != null) {
            for (int i = 0; i < numberIDX.size(); i++) {
                int y = (NUM_ROWS - numberIDY.get(i)) * cellHeight - 9;
                if (Integer.parseInt(numberID.get(i)) < 10 && Integer.parseInt(numberID.get(i)) > 0)
                    canvas.drawText(numberID.get(i), (numberIDX.get(i)) * cellWidth + 13, y, whitePaint);
                else if (Integer.parseInt(numberID.get(i)) > 9 && Integer.parseInt(numberID.get(i)) < 16)
                    canvas.drawText(numberID.get(i), (numberIDX.get(i)) * cellWidth + 9, y, whitePaint);
            }
        }
    }

    private void displayImageIdentified(Canvas canvas) {
        if (imageID != null && imageIDY != null && imageIDX != null) {
            for (int i = 0; i < imageIDX.size(); i++) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), images[imageID.get(i) - 1]);
                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, cellWidth, cellHeight, false);
                int x = (imageIDX.get(i)) * cellWidth + 1, y = (NUM_ROWS - imageIDY.get(i) - 1) * cellHeight;
                canvas.drawBitmap(resizeBitmap, x, y, whitePaint);
            }
        }
    }

    private class Cell {
        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    // generate cell objects
    public void createMaze() {
        cells = new MazeView2.Cell[NUM_COLUMNS + 1][NUM_ROWS + 1];
        for (int x = 0; x < NUM_COLUMNS; x++)
            for (int y = 0; y < NUM_ROWS; y++)
                cells[x][y] = new MazeView2.Cell(x, y);
    }

    // Robot move towards the left wall
    public void moveLeft() {
        String message;
        switch (angle) {
            case 270:
                if (robotCenter[0] == 1) break;
                updateRobotCoords(robotCenter[0] - 1, robotCenter[1], 270);
                message = "F";  //forward = 0
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                message = "R";  //right = 2
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                message = "L";   //left =1
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
        }
    }

    // Robot move towards the right wall
    public void moveRight() {
        String message;
        switch (angle) {
            case 90:
                if (robotCenter[0] == 13) break;
                updateRobotCoords(robotCenter[0] + 1, robotCenter[1], 90);
                message = "F";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                message = "L";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 270:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
        }
    }

    // Robot move towards the top wall
    public void moveUp() {
        String message;
        switch (angle) {
            case 0:
                if (robotCenter[1] == 18) break;
                updateRobotCoords(robotCenter[0], robotCenter[1] + 1, 0);
                message = "F";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                message = "L";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
        }
    }

    // Robot move towards the bottom wall
    public void moveDown() {
        String message;
        switch (angle) {
            case 180:
                if (robotCenter[1] == 1) break;
                updateRobotCoords(robotCenter[0], robotCenter[1] - 1, 180);
                message = "F";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 270:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                message = "L";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                message = "R";
                mapFragment.sendToBTActivity(DEFAULTAR + message);
                break;
        }
    }

    // robot moves forward
    public void moveForward() {
        switch (angle) {
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1] - 1, 180);
                break;
            case 270:
                updateRobotCoords(robotCenter[0] - 1, robotCenter[1], 270);
                break;
            case 90:
                updateRobotCoords(robotCenter[0] + 1, robotCenter[1], 90);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1] + 1, 0);
                break;
        }
    }

    // robot turns right
    public void turnRight() {
        switch (angle) {
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                break;
            case 270:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                break;
        }
    }

    // robot turns left
    public void turnLeft() {
        switch (angle) {
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                break;
            case 270:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                break;
        }
    }


    // method to change the coordinates and direction of the robot
    public void updateRobotCoords(int col, int row, int direction) {
        robotCenter[0] = col;   // X coord
        robotCenter[1] = row;   // Y coord
        angle = direction;

        // limiting the plot grid for robot
        if (robotCenter[0] == 0)
            robotCenter[0] = 1;
        else if (robotCenter[0] == NUM_COLUMNS - 1)
            robotCenter[0] = 13;
        else if (robotCenter[1] == 0)
            robotCenter[1] = 1;
        else if (robotCenter[1] == NUM_ROWS - 1)
            robotCenter[1] = 18;

        switch (direction) {
            case 0: // ^
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] + 1;
                break;
            case 90: // >
                robotFront[0] = robotCenter[0] + 1;
                robotFront[1] = robotCenter[1];
                break;
            case 180: // v
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] - 1;
                break;
            case 270: // <
                robotFront[0] = robotCenter[0] - 1;
                robotFront[1] = robotCenter[1];
                break;
        }
        if (mapFragment.autoUpdate) invalidate();
    }


    //method to update explored and obstacle grids on the maze
    public void updateMaze(int[] exploredGrid, int[] obstacleGrid) {
        this.exploredGrid = exploredGrid;
        this.obstacleGrid = obstacleGrid;
        if (mapFragment.autoUpdate) invalidate();
    }

    // method to update explored images on the maze
    public void updateNumberID(int x, int y, String ID) {

        if (numberIDY == null) {
            numberIDX = new ArrayList<>();
            numberIDY = new ArrayList<>();
            numberID = new ArrayList<>();
        }
        for (int i = 0; i < this.numberIDY.size(); i++) {
            if (x == this.numberIDX.get(i) && y == this.numberIDY.get(i)) {
                this.numberIDX.remove(i);
                this.numberIDY.remove(i);
                this.numberID.remove(i);
            }
        }

        this.numberIDX.add(x);
        this.numberIDY.add(y);
        this.numberID.add(ID);
        if (mapFragment.autoUpdate) invalidate();
    }

    // method to update explored images on the maze
    public void updateImageID(int x, int y, int ID) {
        if (imageIDY == null) {
            imageIDX = new ArrayList<>();
            imageIDY = new ArrayList<>();
            imageID = new ArrayList<>();
        }
        for (int i = 0; i < this.imageIDY.size(); i++) {
            if (x == this.imageIDX.get(i) && y == this.imageIDY.get(i)) {
                this.imageIDX.remove(i);
                this.imageIDY.remove(i);
                this.imageID.remove(i);
            }
        }

        this.imageIDX.add(x);
        this.imageIDY.add(y);
        this.imageID.add(ID);
        if (mapFragment.autoUpdate) invalidate();
    }

    // Enable users to select specific grids by touching for waypoint and robot coordinates
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        // either plot waypoint or robot center
        if (!mapFragment.getEnablePlotRobotPosition()) {
            int x = (int) (event.getX() / cellWidth);
            int y = NUM_ROWS - 1 - (int) (event.getY() / cellHeight);

            waypoint[0] = (x == waypoint[0] && y == waypoint[1]) ? -1 : x;
            waypoint[1] = (x == waypoint[0] && y == waypoint[1]) ? -1 : y;

            invalidate(); // call onDraw method again

            // handlers user when user touch the maze when mazeFragment not initialised
            mapFragment.setWaypointTextView(waypoint);

        } else if (mapFragment.getEnablePlotRobotPosition()) {
            int x = (int) (event.getX() / cellWidth);
            int y = NUM_ROWS - 1 - (int) (event.getY() / cellHeight);

            if (x == NUM_COLUMNS - 1)
                x = NUM_COLUMNS - 2;
            else if (y == NUM_ROWS - 1)
                y = NUM_ROWS - 2;

            if (x == robotCenter[0] && y == robotCenter[1])
                updateRobotCoords(-1, -1, angle);
            else
                updateRobotCoords(x, y, angle);

            mapFragment.setRobotPosition(robotCenter, angle);
            invalidate();
        }
        return true;
    }

    public int[] getWaypoint() {
        return waypoint;
    }

    public int[] getRobotCenter() {
        return robotCenter;
    }

    public int getRobotAngle() {
        return angle;
    }

    //The methods below are all for clearing array lists that stores additional maze elements and revert the maze back to its original state
    public void clearExploredGrid() {
        exploredGrid = null;
        if (mapFragment.autoUpdate) invalidate();
    }

    public void clearObstacleGrid() {
        obstacleGrid = null;
        if (mapFragment.autoUpdate) invalidate();
    }

    public void clearNumID() {
        numberID = null;
        numberIDX = null;
        numberIDY = null;
        imageID = null;
        imageIDX = null;
        imageIDY = null;
        robotX.clear();
        robotY.clear();
        if (mapFragment.autoUpdate) invalidate();
    }

    public ArrayList<String> getObsArray() {
        return obsArray;
    }

    public void setObsArray(int x, int y) {
        this.obsArray.add("" + x + "," + y);
    }

    public void clearObsArray() {
        obsArray.clear();
    }

}
