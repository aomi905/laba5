package bsu.rfact.java.laba5;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {
    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private int selectedMarker = -1;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double[][] viewport = new double[2][2];
    private ArrayList<double[][]> undoHistory = new ArrayList(5);
    private double scaleX;
    private double scaleY;
    private BasicStroke axisStroke;
    private BasicStroke gridStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    private Font axisFont;
    private Font labelsFont;
    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
    private boolean scaleMode = false;
    private boolean changeMode = false;
    private double[] originalPoint = new double[2];
    private java.awt.geom.Rectangle2D.Double selectionRect = new java.awt.geom.Rectangle2D.Double();
    private boolean showAxis = true,
            showMarkers = true,
            showGrid = true,
            showRotate = false;
    private BasicStroke graphicsStroke;

    public GraphicsDisplay() {
        setBackground(Color.PINK);
        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{20, 5, 5, 5, 10, 5, 5, 5},
                0.0f);
        axisStroke = new BasicStroke(4.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f,
                null, 0.0f);
        markerStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f,
                null, 0.0f);
        gridStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f,
                null, 0.0f);
        this.selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F);
        this.axisFont = new Font("Serif", 1, 36);
        this.labelsFont = new Font("Serif", 0, 16);
        formatter.setMaximumFractionDigits(5);
        this.addMouseListener(new GraphicsDisplay.MouseHandler());
        this.addMouseMotionListener(new GraphicsDisplay.MouseMotionHandler());
    }

    public void displayGraphics(ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());
        Iterator var3 = graphicsData.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData.add(newPoint);
        }

        this.minX = ((Double[])graphicsData.get(0))[0];
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[])graphicsData.get(0))[1];
        this.maxY = this.minY;

        for(int i = 1; i < graphicsData.size(); ++i) {
            if (((Double[])graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1];
            }

            if (((Double[])graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1];
            }
        }

        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        if (this.graphicsData != null && this.graphicsData.size() != 0) {
            Graphics2D canvas = (Graphics2D)g;
            if(showRotate)
                rotate(canvas);
            if (showGrid)
                this.paintGrid(canvas);
            if (showAxis) {
                this.paintAxis(canvas);
                this.paintLabels(canvas);
            }
            this.paintGraphics(canvas);
            if (showMarkers)
                this.paintMarkers(canvas);
            this.paintSelection(canvas);
        }
    }

    private void paintSelection(Graphics2D canvas) {
        if (this.scaleMode) {
            canvas.setStroke(this.selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(this.selectionRect);
        }
    }

    private void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(this.graphicsStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        Iterator var5 = this.graphicsData.iterator();

        while(var5.hasNext()) {
            Double[] point = (Double[])var5.next();
            if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                if (currentX != null && currentY != null) {
                    canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0], point[1])));
                }

                currentX = point[0];
                currentY = point[1];
            }
        }

    }

    private void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        Iterator var5 = this.graphicsData.iterator();

        while (var5.hasNext()) {
            Double[] point = (Double[]) var5.next();
            Integer countableX = point[0].intValue();
            Integer countableY = point[1].intValue();
            if (countableX % 2 == 0 && countableY % 2 == 0)
                canvas.setColor(Color.BLUE);
            else
                canvas.setColor(Color.RED);

            Point2D.Double center = translateXYtoPoint(point[0], point[1]);

            canvas.draw(new Line2D.Double(shiftPoint(center, 10, 0),
                    shiftPoint(center, -10, 0)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 0, 10),
                    shiftPoint(center, 0, -10)));

            canvas.draw(new Line2D.Double(shiftPoint(center, 10, 5),
                    shiftPoint(center, 10, -5)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -10, 5),
                    shiftPoint(center, -10, -5)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 5, 10),
                    shiftPoint(center, -5, 10)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 5, -10),
                    shiftPoint(center, -5, -10)));


        }
    }

    private void paintLabels(Graphics2D canvas) {
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        double labelYPos;
        if (this.viewport[1][1] < 0.0D && this.viewport[0][1] > 0.0D) {
            labelYPos = 0.0D;
        } else {
            labelYPos = this.viewport[1][1];
        }

        double labelXPos;
        if (this.viewport[0][0] < 0.0D && this.viewport[1][0] > 0.0D) {
            labelXPos = 0.0D;
        } else {
            labelXPos = this.viewport[0][0];
        }

        double pos = this.viewport[0][0];

        double step;
        java.awt.geom.Point2D.Double point;
        String label;
        Rectangle2D bounds;
        for(step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
            point = this.translateXYtoPoint(pos, labelYPos);
            label = formatter.format(pos);
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }

        pos = this.viewport[1][1];

        for(step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
            point = this.translateXYtoPoint(labelXPos, pos);
            label = formatter.format(pos);
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }

        if (this.selectedMarker >= 0) {
            point = this.translateXYtoPoint(((Double[])this.graphicsData.get(this.selectedMarker))[0], ((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            label = "X=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }

    }

    private void paintGrid(Graphics2D canvas) {
        canvas.setStroke(this.gridStroke);
        double pos = this.viewport[0][0];

        double step;
        for(step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(pos, this.viewport[0][1]), this.translateXYtoPoint(pos, this.viewport[1][1])));
        }

        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[1][1])));
        pos = this.viewport[1][1];

        for(step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], pos), this.translateXYtoPoint(this.viewport[1][0], pos)));
        }

        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1])));
    }

    private void paintAxis(Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        Rectangle2D bounds;
        java.awt.geom.Point2D.Double labelPos;
        if (this.viewport[0][0] <= 0.0D && this.viewport[1][0] >= 0.0D) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(0.0D, this.viewport[0][1]), this.translateXYtoPoint(0.0D, this.viewport[1][1])));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(-(this.viewport[1][0] - this.viewport[0][0]) * 0.0025D, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015D), this.translateXYtoPoint(0.0D, this.viewport[0][1])));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint((this.viewport[1][0] - this.viewport[0][0]) * 0.0025D, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015D), this.translateXYtoPoint(0.0D, this.viewport[0][1])));
            bounds = this.axisFont.getStringBounds("y", context);
            labelPos = this.translateXYtoPoint(0.0D, this.viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10.0F, (float)(labelPos.y + bounds.getHeight() / 2.0D));
        }

        if (this.viewport[1][1] <= 0.0D && this.viewport[0][1] >= 0.0D) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], 0.0D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01D, (this.viewport[0][1] - this.viewport[1][1]) * 0.005D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01D, -(this.viewport[0][1] - this.viewport[1][1]) * 0.005D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));
            bounds = this.axisFont.getStringBounds("x", context);
            labelPos = this.translateXYtoPoint(this.viewport[1][0], 0.0D);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0D), (float)(labelPos.y - bounds.getHeight() / 2.0D));
        }

    }

    protected void rotate(Graphics2D canvas){
        AffineTransform transform = AffineTransform.getRotateInstance(
                -Math.PI / 2,
                getSize().getWidth() / 2,
                getSize().getHeight() / 2);
        transform.concatenate(new AffineTransform(
                getSize().getHeight() / getSize().getWidth(),0.0, 0.0,
                getSize().getWidth() / getSize().getHeight(),
                (getSize().getWidth() - getSize().getHeight()) / 2,
                (getSize().getHeight() - getSize().getWidth()) / 2));
        canvas.setTransform(transform);
    }


    protected java.awt.geom.Point2D.Double translateXYtoPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];
        double deltaY = this.viewport[0][1] - y;
        return new java.awt.geom.Point2D.Double(deltaX * this.scaleX, deltaY * this.scaleY);
    }

    protected double[] translatePointToXY(int x, int y) {
        return new double[]{this.viewport[0][0] + (double)x / this.scaleX, this.viewport[0][1] - (double)y / this.scaleY};
    }

    protected Point2D.Double shiftPoint(Point2D.Double src,
                                        double deltaX, double deltaY){
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }


    protected int findSelectedPoint(int x, int y) {
        if (this.graphicsData == null) {
            return -1;
        } else {
            int pos = 0;

            for(Iterator var5 = this.graphicsData.iterator(); var5.hasNext(); ++pos) {
                Double[] point = (Double[])var5.next();
                java.awt.geom.Point2D.Double screenPoint = this.translateXYtoPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0D) {
                    return pos;
                }
            }

            return -1;
        }
    }

    public ArrayList<Double[]> getGraphicsData() {
        return graphicsData;
    }

    public void reset() {
        this.displayGraphics(this.originalData);
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    public void setShowRotate(boolean antiClockRotate) {
        this.showRotate = antiClockRotate;
        repaint();
    }

    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                if (GraphicsDisplay.this.undoHistory.size() > 0) {
                    GraphicsDisplay.this.viewport = (double[][])GraphicsDisplay.this.undoHistory.get(GraphicsDisplay.this.undoHistory.size() - 1);
                    GraphicsDisplay.this.undoHistory.remove(GraphicsDisplay.this.undoHistory.size() - 1);
                } else {
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX, GraphicsDisplay.this.maxY, GraphicsDisplay.this.maxX, GraphicsDisplay.this.minY);
                }

                GraphicsDisplay.this.repaint();
            }

        }

        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
                GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                if (GraphicsDisplay.this.selectedMarker >= 0) {
                    GraphicsDisplay.this.changeMode = true;
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
                } else {
                    GraphicsDisplay.this.scaleMode = true;
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
                    GraphicsDisplay.this.selectionRect.setFrame((double)ev.getX(), (double)ev.getY(), 1.0D, 1.0D);
                }

            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
                if (GraphicsDisplay.this.changeMode) {
                    GraphicsDisplay.this.changeMode = false;
                } else {
                    GraphicsDisplay.this.scaleMode = false;
                    double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                    GraphicsDisplay.this.undoHistory.add(GraphicsDisplay.this.viewport);
                    GraphicsDisplay.this.viewport = new double[2][2];
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);
                    GraphicsDisplay.this.repaint();
                }

            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
            GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            } else {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            }

            GraphicsDisplay.this.repaint();
        }

        public void mouseDragged(MouseEvent ev) {
            if (GraphicsDisplay.this.changeMode) {
                double[] currentPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] + (currentPoint[1] - ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1]);
                if (newY > GraphicsDisplay.this.viewport[0][1]) {
                    newY = GraphicsDisplay.this.viewport[0][1];
                }

                if (newY < GraphicsDisplay.this.viewport[1][1]) {
                    newY = GraphicsDisplay.this.viewport[1][1];
                }

                ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] = newY;
                GraphicsDisplay.this.repaint();
            } else {
                double width = (double)ev.getX() - GraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0D) {
                    width = 5.0D;
                }

                double height = (double)ev.getY() - GraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0D) {
                    height = 5.0D;
                }

                GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(), GraphicsDisplay.this.selectionRect.getY(), width, height);
                GraphicsDisplay.this.repaint();
            }

        }
    }
}