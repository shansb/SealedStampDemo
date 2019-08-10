package top.imshan.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * 图像处理
 *
 * @author shansb
 *
 */
public class ImageHelper {
    /**
     * 动态水印Y轴坐标（需要自己根据素材动态调整）
     */
    private static final int WATER_PRESS_Y = 100;
    /**
     * 自定义字体对象
     */
    private static Font font = loadFont("/tinysun.ttf",
            Font.PLAIN, 25);
    /**
     * 创建一个印章对象
     */
    private static Image srcImg = loadStamp("/stamp.png");

    /**
     * 加载自定义字体
     *
     * @param name
     *            字体路径
     * @param style
     *            风格
     * @param size
     *            大小
     */
    private static Font loadFont(String name, int style, int size) {
        InputStream fontStream = ImageHelper.class.getResourceAsStream(name);
        Font font;
        try {
            font = Font.createFont(style, fontStream);
            return font.deriveFont((float) size);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fontStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("加载字体失败，重新尝试加载宋体");
        return new Font("宋体", style, size);
    }

    /**
     * 读取印章
     *
     * @param path
     *            路径
     * @return 印章对象
     */
    private static Image loadStamp(String path) {
        InputStream imgStream = ImageHelper.class.getResourceAsStream(path);
        try {
            return ImageIO.read(imgStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("无法加载电子印章图像");
        } finally {
            try {
                imgStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 电子印章添加水印
     * 文字居中
     *
     * @param markContentColor
     *            水印文字的颜色
     * @param waterMarkContent
     *            水印的文字内容
     */
    public static BufferedImage waterPress(Color markContentColor,
                                           String waterMarkContent) {
        // 读取原图片信息
        // File srcImgFile = new File(srcImgPath);
        // Image srcImg = ImageIO.read(srcImgFile);
        // 宽、高
        int srcImgWidth = srcImg.getWidth(null);
        int srcImgHeight = srcImg.getHeight(null);
        // 加水印
        // 2、得到画笔对象
        BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufImg.createGraphics();
        // 背景透明
        bufImg = g.getDeviceConfiguration().createCompatibleImage(
                srcImg.getWidth(null), srcImg.getHeight(null),
                Transparency.TRANSLUCENT);
        g.dispose();
        g = bufImg.createGraphics();
        // 设置图片透明度
        g.setComposite(AlphaComposite
                .getInstance(AlphaComposite.SRC_OVER, 0.8f));
        // 4、把原图画到新画板上
        g.drawImage(
                srcImg.getScaledInstance(srcImg.getWidth(null),
                        srcImg.getHeight(null), java.awt.Image.SCALE_SMOOTH),
                0, 0, null);
        // 设置水印颜色
        g.setColor(markContentColor);
        g.setFont(font);
        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int fontLength = getWatermarkLength(waterMarkContent, g);

        // 相对与X的起始的位置
        int originX;
        // 相对与Y的起始的位置
        int originY;
        originY = WATER_PRESS_Y;
        // 实际文字行数是1，计算x的居中的起始位置
        originX = (srcImgWidth - fontLength) / 2;
        System.out.println("水印文字总长度:" + fontLength + ",图片宽度:" + srcImgWidth + ",字符个数:"
                + waterMarkContent.length());
        // 文字叠加,自动换行叠加
        int tempX = originX;
        int tempY = originY;
        // 最后叠加余下的文字
        g.drawString(waterMarkContent, tempX, tempY);
        g.dispose();
        return bufImg;
    }

    /**
     * 获取水印文字总长度
     *
     * @param waterMarkContent 水印的文字
     * @param g
     *            画笔对象
     * @return 水印文字总长度
     */
    private static int getWatermarkLength(String waterMarkContent, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(
                waterMarkContent.toCharArray(), 0, waterMarkContent.length());
    }

    /**
     * 尽可能均匀竖直切割图片
     * @param imgPath 路径
     * @param n 切割份数
     * @return 图像数组
     * @throws IOException
     */
    public static BufferedImage[] subImages(String imgPath, int n) throws IOException {
        BufferedImage[] nImage = new BufferedImage[n];
        BufferedImage img = ImageIO.read(new File(imgPath));
        int h = img.getHeight();
        int w = img.getWidth();

        int sw = w / n;
        for (int i = 0; i < n; i++) {
            BufferedImage subImg;
            if (i == n - 1) {// 最后剩余部分
                subImg = img.getSubimage(i * sw, 0, w - i * sw, h);
            } else {// 前n-1块均匀切
                subImg = img.getSubimage(i * sw, 0, sw, h);
            }

            nImage[i] = subImg;
        }
        return nImage;
    }

    /**
     * 切割图片
     * @param img 原始图片
     * @param n       切割份数
     * @return itextPdf的Image[]
     * @throws IOException
     */
    public static com.itextpdf.text.Image[] subImages(BufferedImage img, int n) throws IOException {
        com.itextpdf.text.Image[] nImage = new com.itextpdf.text.Image[n];
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            int h = img.getHeight();
            int w = img.getWidth();

            int sw = w / n;
            for (int i = 0; i < n; i++) {
                BufferedImage subImg;
                if (i == n - 1) {// 最后剩余部分
                    subImg = img.getSubimage(i * sw, 0, w - i * sw, h);
                } else {// 前n-1块均匀切
                    subImg = img.getSubimage(i * sw, 0, sw, h);
                }

                ImageIO.write(subImg, "png", out);
                nImage[i] = com.itextpdf.text.Image.getInstance(out.toByteArray());
                out.flush();
                out.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(out != null){
                out.close();
            }
        }
        return nImage;
    }
}


