package com.example.mdpandroid.New;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.mdpandroid.R;

import java.util.ArrayList;
import java.util.List;

public class MazeView2 extends View {

    private static final int NUM_COLUMNS = 15;  // Range of X-axis
    private static final int NUM_ROWS = 20;     // Range of Y-axis
    private static final float WALL_THICKNESS = 4;
    private MazeView2.Cell[][] cells;
    private int[] exploredGrid, obstacleGrid;
    private int cellWidth, cellHeight;
    private float cellSize;
    List<Integer> numberIDX = new ArrayList<>(); // x-coordinate of identified image
    List<Integer> numberIDY = new ArrayList<>(); // y-coordinate of identified image
    List<String> numberID = new ArrayList<>(); // numberid of identified image
    List<Integer> robotX = new ArrayList<>(), robotY = new ArrayList<>();

    private Paint blackPaint, greenPaint, yellowPaint, bluePaint, lightBluePaint, whitePaint,
            redPaint, greyPaint, cyanPaint;
    private Paint goalPaint, startPaint, mapPaint, robotPaint, waypointPaint;
    private final String DEFAULTAL = "AR,AN,"; // Sending to Arudino
    private final String DEFAULTAR = "AR,AN,";
    //  private final String DEFAULTFASTEST = "GO";

    MapFragment mapFragment = (MapFragment) MapFragment.getInstance();

    // waypoint
    private int[] waypoint = {1, 1};
    // robot starting coordinates
    private int[] robotFront = {1, 2}; // x,y

    public int[] robotCenter = {1, 1}; // x,y
    int angle = 0;

    // store obstacles (x,y)
    ArrayList<String> obsArray = new ArrayList<String>();

    @SuppressLint("ResourceAsColor")
    public MazeView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        blackPaint = new Paint();
        yellowPaint = new Paint();
        whitePaint = new Paint();
        redPaint = new Paint();
        bluePaint = new Paint();
        lightBluePaint = new Paint();
        cyanPaint = new Paint();
        greyPaint = new Paint();
        greenPaint = new Paint();

        goalPaint = new Paint();
        startPaint = new Paint();
        mapPaint = new Paint();
        robotPaint = new Paint();
        waypointPaint = new Paint();

        // setting color for all the color variables
        bluePaint.setColor(Color.BLUE);
        lightBluePaint.setColor(Color.LTGRAY);
        yellowPaint.setColor(Color.YELLOW); // camera
        blackPaint.setColor(Color.BLACK); // obstacle and walls
        whitePaint.setColor(Color.WHITE); // robot body
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        cyanPaint.setColor(Color.CYAN);
        greyPaint.setColor(Color.GRAY);
        blackPaint.setStrokeWidth(WALL_THICKNESS);

        goalPaint.setColor(Color.rgb(142, 226, 195));
        startPaint.setColor(Color.rgb(142, 175, 226));
        mapPaint.setColor(Color.LTGRAY);
        robotPaint.setColor(Color.DKGRAY);
        waypointPaint.setColor(Color.rgb(94, 94, 94));

        createMaze();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // calculate the cellsize based on the canvas
        cellWidth = getWidth() / NUM_COLUMNS;
        cellHeight = getHeight() / NUM_ROWS;

        if (cellWidth > cellHeight) {
            cellWidth = cellHeight;
        } else {
            cellHeight = cellWidth;
        }
        cellSize = cellHeight;

        this.setLayoutParams(new RelativeLayout.LayoutParams(cellWidth * NUM_COLUMNS, cellHeight * NUM_ROWS));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawBitmap( mBitmap, 10, 10, null);
        canvas.drawColor(Color.WHITE);

        // Normal grids
        for (int i = 0; i <= 14; i++)
            for (int j = 0; j <= 19; j++)
                canvas.drawRect(i * cellWidth, (NUM_ROWS - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (NUM_ROWS - j) * cellHeight, mapPaint);

//        for (int x = 1; x <= NUM_COLUMNS; x++) {
//            if (x > 9)
//                canvas.drawText(Integer.toString(x - 1), cells[x][20].startX + (cellSize / 5),
//                        cells[x][20].startY + (cellSize / 3), blackPaint);
//            else
//                canvas.drawText(Integer.toString(x - 1), cells[x][20].startX + (cellSize / 3),
//                        cells[x][20].startY + (cellSize / 3), blackPaint);
//        }
//        for (int y = 0; y < NUM_ROWS; y++) {
//            if ((20 - y) > 9)
//                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 2),
//                        cells[0][y].startY + (cellSize / 1.5f), blackPaint);
//            else
//                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 1.5f),
//                        cells[0][y].startY + (cellSize / 1.5f), blackPaint);
//        }

        int inc = 0;
        int inc2 = 0;
        // Obstacles and explored
        for (int y = 0; y < NUM_ROWS; y++) {
            for (int x = 0; x < NUM_COLUMNS; x++) {
                // when explored then draw obstacle if any
                if (exploredGrid != null && exploredGrid[inc] == 1) {
                    canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, cyanPaint);
                    if (obstacleGrid != null && obstacleGrid[inc2] == 1) {
                        canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                                (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, blackPaint);
                    }
                    inc2++;
                }
                inc++;
            }
        }

        // Numberid drawings on obstacle
        if (numberID != null && numberIDY != null && numberIDX != null) {
            for (int i = 0; i < numberIDX.size(); i++) {
                if (Integer.parseInt(numberID.get(i)) < 10 && Integer.parseInt(numberID.get(i)) > 0)
                    canvas.drawText(numberID.get(i), (numberIDX.get(i) - 1) * cellWidth + 9, (NUM_ROWS - numberIDY.get(i) + 1) * cellHeight - 7, whitePaint);
                else if (Integer.parseInt(numberID.get(i)) > 9 && Integer.parseInt(numberID.get(i)) < 16)
                    canvas.drawText(numberID.get(i), (numberIDX.get(i) - 1) * cellWidth + 6, (NUM_ROWS - numberIDY.get(i) + 1) * cellHeight - 7, whitePaint);
            }
        }

        if (mapFragment.fastest) {
            if (robotX != null && robotY != null) {
                for (int i = 0; i < robotX.size(); i++) {
                    canvas.drawRect(robotX.get(i) * cellWidth, (NUM_ROWS - 1 - robotY.get(i)) * cellHeight,
                            (robotX.get(i) + 1) * cellWidth, (NUM_ROWS - robotY.get(i)) * cellHeight, redPaint);
                }
            }
        }

        // Grid drawing lines
        for (int c = 0; c < NUM_COLUMNS + 1; c++) {
            canvas.drawLine(c * cellWidth, 0, c * cellWidth, NUM_ROWS * cellHeight, whitePaint);
        }
        for (int r = 0; r < NUM_ROWS + 1; r++) {
            canvas.drawLine(0, r * cellHeight, NUM_COLUMNS * cellWidth, r * cellHeight, whitePaint);
        }

        drawStartZone(canvas);
        drawGoalZone(canvas);
        displayWaypoint(canvas);
        displayRobot(canvas);

    }

    private void drawStartZone(Canvas canvas) { // startZone
        for (int i = 0; i <= 2; i++)
            for (int j = 0; j <= 2; j++)
                canvas.drawRect(i * cellWidth, (NUM_ROWS - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (NUM_ROWS - j) * cellHeight, startPaint);
    }

    private void drawGoalZone(Canvas canvas) { // goalZone
        for (int i = 12; i <= 14; i++)
            for (int j = 17; j <= 19; j++)
                canvas.drawRect(i * cellWidth, (NUM_ROWS - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (NUM_ROWS - j) * cellHeight, goalPaint);
    }

    private void displayRobot(Canvas canvas) { // displaying robot position when user taps
        if (robotCenter[0] >= 0) {
            canvas.drawCircle(robotCenter[0] * cellWidth + cellWidth / 2,
                    (NUM_ROWS - robotCenter[1]) * cellHeight - cellHeight / 2, 1.3f * cellWidth, robotPaint);
        }
        if (robotFront[0] >= 0) {
            canvas.drawCircle(robotFront[0] * cellWidth + cellWidth / 2,
                    (NUM_ROWS - robotFront[1]) * cellHeight - cellHeight / 2, 0.3f * cellWidth, whitePaint);
        }
    }

    private void displayWaypoint(Canvas canvas) { // display the waypoint when user taps
        if (waypoint[0] >= 0) {
            canvas.drawRect(waypoint[0] * cellWidth, (NUM_ROWS - 1 - waypoint[1]) * cellHeight,
                    (waypoint[0] + 1) * cellWidth, (NUM_ROWS - waypoint[1]) * cellHeight, waypointPaint);
        }
    }

    private class Cell {
        boolean topWall = true, leftWall = true, bottomWall = true, rightWall = true;
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
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                message = "R";  //right = 2
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                message = "L";   //left =1
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
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
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                message = "L";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 270:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
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
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                message = "L";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 180:
                updateRobotCoords(robotCenter[0], robotCenter[1], 270);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 0);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
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
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 270:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                message = "L";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            case 90:
                updateRobotCoords(robotCenter[0], robotCenter[1], 180);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
                break;
            default:
                updateRobotCoords(robotCenter[0], robotCenter[1], 90);
                message = "R";
                mapFragment.sendCtrlToBtAct(DEFAULTAL + message);
        }
    }

    //robot moves forward
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
        }
    }

    //robot turns right
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
        }
    }

    //robot turns left
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
        }
    }


    // method to change the coordinates and direction of the robot
    public void updateRobotCoords(int col, int row, int direction) {
        robotCenter[0] = col;   // X coord
        robotCenter[1] = row;   // Y coord
        angle = direction;
        //limiting the plot grid for robot
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
        if (mapFragment.autoUpdate)
            invalidate();
    }


    //method to update explored and obstacle grids on the maze
    public void updateMaze(int[] exploredGrid, int[] obstacleGrid) {
        this.exploredGrid = exploredGrid;
        this.obstacleGrid = obstacleGrid;
        if (mapFragment.autoUpdate)
            invalidate();
    }

    //method to update explored images on the maze
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
        if (mapFragment.autoUpdate)
            invalidate();
    }

    //Enable users to select specific grids by touching for waypoint and robot coordinates
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        // either plot waypoint or robot center
        if (!mapFragment.getEnablePlotRobotPosition()) {
            int x = (int) (event.getX() / cellWidth);
            int y = NUM_ROWS - 1 - (int) (event.getY() / cellHeight);

            if (x == waypoint[0] && y == waypoint[1]) {
                waypoint[0] = -1;
                waypoint[1] = -1;
            } else {
                waypoint[0] = x; // when user touch and assign new waypoint value
                waypoint[1] = y;
            }
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

            mapFragment.setRobotTextView(robotCenter);
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

    public int[] getRobotFront() {
        return robotFront;
    }

    //The methods below are all for clearing array lists that stores additional maze elements and revert the maze back to its original state
    public void clearExploredGrid() {
        exploredGrid = null;
        if (mapFragment.autoUpdate)
            invalidate();
    }

    public void clearObstacleGrid() {
        obstacleGrid = null;
        if (mapFragment.autoUpdate)
            invalidate();
    }

    public void clearNumID() {
        numberID = null;
        numberIDX = null;
        numberIDY = null;
        robotX.clear();
        robotY.clear();
        if (mapFragment.autoUpdate)
            invalidate();
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
