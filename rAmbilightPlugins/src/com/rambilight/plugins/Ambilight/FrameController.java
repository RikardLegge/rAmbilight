package com.rambilight.plugins.Ambilight;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/*
 * Original source
 * http://www.coderanch.com/t/415944/GUI/java/user-ve-undecorated-window-resizable
 * 
 * Only edited to suite my needs. Full credit to the original author
 */

/**
 * Frame controller for selecting the area of the ambilight screen capture
 */
public class FrameController extends JFrame implements MouseMotionListener, MouseListener {

    private static final long serialVersionUID = 1L;
    private Point start_drag;
    private Point start_loc;
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    private int   minWidth;
    private int   minHeight;
    private Point initialLocation;
    int       cursorArea = 10;
    Rectangle screen     = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private int DIFF_MIN_WIDTH  = 30;
    private int DIFF_MIN_HEIGHT = 30;

    private int border_TMP;
    private int border;

    private JTextField textField;
    private String     textFieldOldInput;
    private JPanel     viewContainer;

    private CallbackHandle callbackHandle;

    public FrameController(Point initialLocation, Dimension initialDimension, int border, CallbackHandle callbackHandle) {

        this.initialLocation = initialLocation;
        this.callbackHandle = callbackHandle;
        minWidth = 400;
        minHeight = 400;
        this.border = border;
        border_TMP = border;
        Init(initialDimension);
    }

    private void Init(Dimension initialDimension) {
        addMouseMotionListener(this);
        addMouseListener(this);
        setTitle("Change capture rectangle");
        try {
            setIconImage(new ImageIcon(FrameController.class.getResource("Application_Icon.png")).getImage());
        } catch (Exception e) {
        }
        this.setSize(initialDimension.width, initialDimension.height);

        minWidth -= DIFF_MIN_WIDTH;
        minHeight -= DIFF_MIN_HEIGHT;

        setLocation(initialLocation);
        setUndecorated(true);
        setOpacity(.80f);
        setAlwaysOnTop(true);

        CreateContent();
    }

    private void CreateContent() {
        viewContainer = (JPanel) getContentPane();
        viewContainer.setBackground(Color.LIGHT_GRAY);
        viewContainer.setBorder(new LineBorder(Color.RED, border));

        JPanel headerPanel = new JPanel(new CenterLayout());// new
        // FlowLayout(FlowLayout.CENTER,
        // 0, 2));
        headerPanel.setPreferredSize(new Dimension(getSize().width, getSize().height));
        headerPanel.addMouseListener(this);
        headerPanel.addMouseMotionListener(this);

        textField = new JTextField();
        textField.setText(getX() + ", " + getY() + " / " + getWidth() + "x" + getHeight());
        textField.setBorder(new LineBorder(Color.WHITE, 0));
        textField.setOpaque(false);
        // textField.setEditable(false);
        textField.setFont(new Font(textField.getFont().getFamily(), Font.PLAIN, 50));
        textField.addActionListener((e) -> {
            String[] parsed = textField.getText().split(",");
            int x = Integer.parseInt(parsed[0].trim());
            parsed = parsed[1].split("/");
            int y = Integer.parseInt(parsed[0].trim());
            setLocation(x, y);
            parsed = parsed[1].split("x");
            int w = Integer.parseInt(parsed[0].trim());
            int h = Integer.parseInt(parsed[1].trim());
            setSize(w, h);
        });
        headerPanel.add(textField, BorderLayout.CENTER);

        viewContainer.add(headerPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public void SetCallbackHandle(CallbackHandle callbackHandle) {
        this.callbackHandle = callbackHandle;
    }

    public static Point getScreenLocation(MouseEvent e, JFrame frame) {
        Point cursor = e.getPoint();
        Point view_location = frame.getLocationOnScreen();
        return new Point((int) (view_location.getX() + cursor.getX()), (int) (view_location.getY() + cursor.getY()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        moveOrFullResizeFrame(e);
        textField.setText(getX() + ", " + getY() + " / " + getWidth() + "x" + getHeight());
    }

    long mouseMovedLast = 0;

    public void mouseMoved(MouseEvent e) {
        if (System.currentTimeMillis() - mouseMovedLast < 5)
            return;
        mouseMovedLast = System.currentTimeMillis();

        int borderFix = e.getSource().getClass().equals(FrameController.class) ? 0 : border;
        int p = cursorArea; // padded

        Point cursorLocation = e.getPoint();
        int x = cursorLocation.x;
        int y = cursorLocation.y;
        int w = getWidth() - p - borderFix * 2;
        int h = getHeight() - p - borderFix * 2;

        boolean l = x < p;
        boolean r = x > w;
        boolean t = y < p;
        boolean b = y > h;

        if (l && t)
            setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        else if (r && t)
            setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        else if (l && b)
            setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        else if (r && b)
            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        else if (l)
            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        else if (t)
            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        else if (r)
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        else if (b)
            setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        else
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object sourceObject = e.getSource();
        if (sourceObject instanceof JPanel) {
            if (e.getClickCount() == 2) {
                if (getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) {
                    dispose();
                    callbackHandle.onWindowClose(getLocation(), getSize(), border);
                }
            }
        }
    }

    private void moveOrFullResizeFrame(MouseEvent e) {
        Object sourceObject = e.getSource();
        Point current = getScreenLocation(e, this);
        Point offset = new Point((int) current.getX() - (int) start_drag.getX(), (int) current.getY() - (int) start_drag.getY());

        if (sourceObject instanceof JPanel && getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) {
            int newX = (int) (start_loc.getX() + offset.getX());
            int newY = (int) (start_loc.getY() + offset.getY());

            setLocation(newX, newY);

        }
        else if (!getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) {
            int oldLocationX = (int) getLocation().getX();
            int oldLocationY = (int) getLocation().getY();
            int newLocationX = (int) (this.start_loc.getX() + offset.getX());
            int newLocationY = (int) (this.start_loc.getY() + offset.getY());
            boolean N_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            boolean NE_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
            boolean NW_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            boolean E_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            boolean W_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            boolean S_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            boolean SW_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            boolean setLocation = false;
            int newWidth = e.getX();
            int newHeight = e.getY();

            if (!e.getSource().getClass().equals(FrameController.class)) {
                if (N_Resize)
                    border_TMP = border + newLocationY - oldLocationY;
                else if (S_Resize)
                    border_TMP = border + oldLocationY - newLocationY;
                else if (E_Resize)
                    border_TMP = border + oldLocationX - newLocationX;
                else if (W_Resize)
                    border_TMP = border + newLocationX - oldLocationX;
                int max = (int) Math.min(getWidth() * .3f, getHeight() * .3f);
                if (border_TMP > max)
                    border_TMP = max;
                else if (border_TMP < 10)
                    border_TMP = 10;
                return;
            }

            if (NE_Resize) {
                newHeight = getHeight() - (newLocationY - oldLocationY);
                newLocationX = (int) getLocation().getX();
                setLocation = true;
            }
            else if (E_Resize)
                newHeight = getHeight();
            else if (S_Resize)
                newWidth = getWidth();
            else if (N_Resize) {
                newLocationX = (int) getLocation().getX();
                newWidth = getWidth();
                newHeight = getHeight() - (newLocationY - oldLocationY);
                setLocation = true;
            }
            else if (NW_Resize) {
                newWidth = getWidth() - (newLocationX - oldLocationX);
                newHeight = getHeight() - (newLocationY - oldLocationY);
                setLocation = true;
            }
            else if (NE_Resize) {
                newHeight = getHeight() - (newLocationY - oldLocationY);
                newLocationX = (int) getLocation().getX();
            }
            else if (SW_Resize) {
                newWidth = getWidth() - (newLocationX - oldLocationX);
                newLocationY = (int) getLocation().getY();
                setLocation = true;
            }
            if (W_Resize) {
                newWidth = getWidth() - (newLocationX - oldLocationX);
                newLocationY = (int) getLocation().getY();
                newHeight = getHeight();
                setLocation = true;
            }

            if (newWidth <= minWidth) {
                newLocationX = oldLocationX;
                newWidth = getWidth();
            }

            if (newHeight <= minHeight) {
                newLocationY = oldLocationY;
                newHeight = getHeight();
            }

            if (newWidth != getWidth() || newHeight != getHeight()) {
                this.setSize(newWidth, newHeight);

                if (setLocation)
                    this.setLocation(newLocationX, newLocationY);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.start_drag = getScreenLocation(e, this);
        this.start_loc = this.getLocation();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        border = border_TMP;
        viewContainer.setBorder(new LineBorder(Color.RED, border));
    }

    /**
     * Callback handle for when the frame controller overlay closes
     */
    public interface CallbackHandle {

        void onWindowClose(Point position, Dimension dimension, int border);
    }
}

/**
 * Layout manager for centering of it's content
 */
class CenterLayout implements LayoutManager, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container target) {
        return target.getPreferredSize();
    }

    public Dimension minimumLayoutSize(Container target) {
        return target.getMinimumSize();
    }

    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            Dimension size = target.getSize();
            int w = size.width - (insets.left + insets.right);
            int h = size.height - (insets.top + insets.bottom);
            int count = target.getComponentCount();

            for (int i = 0; i < count; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    m.setBounds((w - d.width) / 2, (h - d.height) / 2, d.width, d.height);
                    break;
                }
            }
        }
    }

}