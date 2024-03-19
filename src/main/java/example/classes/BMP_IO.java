package example.classes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;

import static javax.swing.SwingUtilities.updateComponentTreeUI;

public class BMP_IO extends JFrame implements MouseListener {
    int[][] map;
    JPanel center;
    File selectFile;
    int width = 200, height = 200;
    byte[] info_1, info_2;
    JScrollPane scrollPane;
    public BMP_IO() {
        setLayout(new BorderLayout());
        center = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(map != null) {
                    for(int i = 0; i < map.length; i++) {
                        for(int j = 0; j < map[i].length; j++) {
                            g.setColor(new Color(map[i][j]));
                            g.drawLine(j, height - i, j,height - i);
                        }
                    }
                }
                setSize(new Dimension(width, height));
            }
        };

        addMouseListener(this);
        center.addMouseListener(this);
        center.setBackground(Color.WHITE);
        center.setPreferredSize(new Dimension(400, 200));
        scrollPane = new JScrollPane(center);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");

        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter
                ("24-битное растровое изображение (* .bmp)", "bmp"));
        fileChooser.setCurrentDirectory(new File("\\Users\\koldun\\Desktop"));
        open.addActionListener(e -> {

            int choose = fileChooser.showOpenDialog(null);
            if (choose == JFileChooser.APPROVE_OPTION) {
                selectFile=fileChooser.getSelectedFile();
                readBMP();
            }
        });
        save.addActionListener(e -> {
            int choose=fileChooser.showSaveDialog(null);
            if(choose==JFileChooser.APPROVE_OPTION)
            {
                selectFile=fileChooser.getSelectedFile();
                writeBMP();
                System.out.println("save ");
            }
        });

        fileMenu.add(open);
        fileMenu.add(save);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
        add(scrollPane, BorderLayout.CENTER);
        setLocation(300, 150);
        setMinimumSize(new Dimension(200, 200));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
    private int byteToInt(byte[] b) {
        int t1 = (b[3] & 0xff) << 24;
        int t2 = (b[2] & 0xff) << 16;
        int t3 = (b[1]& 0xff)<<8;
        int t4 = b[0] & 0xff;
        return t1 + t2 + t3 + t4;
    }
    private byte[] intToByte(int a) {
        int size = 4;
        byte []t = new byte[size];
        t[0] = (byte) (( a & 0xff));
        t[1] = (byte) ((a & 0xff00) >> 8);
        t[2] = (byte) ((a & 0xff0000) >> 16);
        t[3] = (byte) ((a & 0xff000000) >> 24);
        return t;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        Point p = e.getPoint();
        Color c = new Color(map[height - p.y][p.x]);
        System.out.println("X: " + p.x + " Y: " + (height - p.y));
        System.out.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public void readBMP() {
        try {
            FileInputStream fileInputStream = new FileInputStream(selectFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] widthBytes = new byte[4];
            byte[] heightBytes = new byte[4];
            info_1 = new byte[18];
            bufferedInputStream.skip(18);
            bufferedInputStream.read(widthBytes);
            bufferedInputStream.read(heightBytes);
            width = byteToInt(widthBytes);
            height = byteToInt(heightBytes);
            System.out.println("Size in kb: " + selectFile.length() / 1024 + "kbytes");
            System.out.println("Dimensions: " + width + " " + height);
            map = new int[height][width];
            int skip = 4 - width * 3% 4;
            info_2 = new byte[28];
            bufferedInputStream.skip(28);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int blue = bufferedInputStream.read();
                    int green = bufferedInputStream.read();
                    int red = bufferedInputStream.read();
                    map[i][j] = new Color(red, green, blue).getRGB();
                }
                if (skip != 4) bufferedInputStream.skip(skip);
            }
            bufferedInputStream.close();
            fileInputStream.close();
            center.setPreferredSize(new Dimension(width, height));
            updateComponentTreeUI(center);
            repaint();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void writeBMP() {
        try {
           FileOutputStream fileOutputStream = new FileOutputStream(selectFile);
           BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
           bufferedOutputStream.write(info_1);
           bufferedOutputStream.write(intToByte(width));
           bufferedOutputStream.write(intToByte(height));
           bufferedOutputStream.write(info_2);
           for(int i = 0; i < height; i++) {
               for(int j = 0; j < width; j++) {
                   Color color = new Color(map[i][j]);
                   bufferedOutputStream.write(color.getBlue());
                   bufferedOutputStream.write(color.getGreen());
                   bufferedOutputStream.write(color.getRed());
               }
           }
           bufferedOutputStream.flush();
           fileOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
