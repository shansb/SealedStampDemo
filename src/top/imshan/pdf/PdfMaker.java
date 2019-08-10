package top.imshan.pdf;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import top.imshan.utils.ImageHelper;
import top.imshan.utils.PdfFooterPager;
import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PDF生成器
 * @author shansb
 */
public class PdfMaker {
    /**
     * 编码集
     */
    private static final String CHARSET = "UTF-8";

    /**
     * 生产pdf时需要的字体
     */
    private static final String FONT = "/simhei.ttf";
    /**
     * freemarker模板路径
     */
    private static final String PATH_PREFIX = "/test/";
    /**
     * 印章uid颜色
     */
    public static final Color MARK_CONTENT_COLOR = Color.RED;
    /**
     * 107px相当于A4PDF的38mm物理距离，缩放真实大小比例 = 38/210（A4宽）*595（pdf 像素宽度）
     */
    public static final float REAL_PIXEL = 107.7f;

    /**
     * 盖骑缝章，设置页码
     * @param infilePath  原PDF路径
     * @param outFilePath 输出PDF路径
     * @param stampId     章图片id（水印内容）
     * @throws IOException
     * @throws DocumentException
     */
    public void createPageSealPdf(String infilePath, String outFilePath, String stampId)
            throws IOException, DocumentException {
        PdfReader reader = null;
        FileOutputStream fileOutputStream = null;
        PdfStamper stamp = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //印章序号字体为红色
        BufferedImage stampImage = ImageHelper.waterPress(MARK_CONTENT_COLOR, stampId);
        try {
            reader = new PdfReader(infilePath);// 选择需要印章的pdf
            fileOutputStream = new FileOutputStream(outFilePath);
            stamp = new PdfStamper(reader, fileOutputStream);// 加完印章后的pdf
            Rectangle pageSize = reader.getPageSize(1);// 获得第一页
            int nums = reader.getNumberOfPages();
            Image[] nImage = ImageHelper.subImages(stampImage, nums);// 生成骑缝章切割图片
            float width = pageSize.getWidth();
            float height = pageSize.getHeight();


            for (int n = 1; n <= nums; n++) {
                PdfContentByte over = stamp.getOverContent(n);// 设置在第几页打印印章
                Image img = nImage[n - 1];// 选择图片

                float percent = REAL_PIXEL /img.getHeight();
                // 大于1页才有骑缝章
                if (nums > 1) {
                    //印章缩放到107px
                    img.scalePercent(percent * 100f);
                    //计算位置时同样要考虑缩放
                    float sealX = width - img.getWidth() * percent;
                    float sealY = height / 2 - img.getHeight() * percent / 2;
                    img.setAbsolutePosition(sealX,sealY);
                    over.addImage(img);
                }
                //最后一页在文章末尾追加印章
                if(n == nums){
                    ImageIO.write(stampImage, "png", out);
                    Image fullStamp = Image.getInstance(out.toByteArray());
                    // 默认盖在页面右下2/3处
                    float fixedX = 0.7f*width-fullStamp.getWidth()*percent/2;
                    float fixedY = 0.3f*height-fullStamp.getHeight()*percent / 2;
                    //获取动态高度
                    List<Float> textRectangles = getKeyWord(nums, reader,"电子印章代码");
                    if (!textRectangles.isEmpty()) {
                        fixedY = (float) (textRectangles.get(0).getMaxY());
                        fixedX = (float) textRectangles.get(0).getMinX();
                        // 页面高度减去页边距，有文本内容的最大Y轴值
                        float contentMaxY = height-72f;
                        //文本高度减去图片高度就是图片能存放的最大Y轴值
                        float imageMaxY = contentMaxY - fullStamp.getHeight()*percent;
                        fixedY = fixedY > imageMaxY ? imageMaxY : fixedY;
                    }
                    fullStamp.scalePercent(percent*100f);
                    fullStamp.setAbsolutePosition(fixedX, fixedY);
                    over.addImage(fullStamp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stamp != null) {
                stamp.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (reader != null) {
                reader.close();
            }
            out.close();
        }
    }

    /**
     * 获取指定页面的关键词坐标
     * @param pageNum 指定页面
     * @param reader pdfReader
     * @param keyWord 关键字
     * @return 坐标列表
     */
    private List<Float> getKeyWord(Integer pageNum, PdfReader reader, final String keyWord) {
        final List<Float> result = new ArrayList<Float>(1);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        try {
            parser.processContent(pageNum, new RenderListener() {

                @Override
                public void renderText(TextRenderInfo info) {
                    //文字处理
                    //获取的内容很奇怪，连续的单词也可能被分开
                    String text = info.getText();
                    if(null != text && text.contains(keyWord)){
                        result.add(info.getBaseline().getBoundingRectange());
                    }
                }

                @Override
                public void renderImage(ImageRenderInfo arg0) {
                    // 图像处理
                }

                @Override
                public void endTextBlock() {
                    //do nothing
                }

                @Override
                public void beginTextBlock() {
                    //do nothing
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * freemarker渲染html
     * @param data 动态填充内容
     * @param htmlTmp 模板
     * @return 内容string
     */
    public String freeMarkerRender(Map<String, Object> data, String htmlTmp) {
        // 创建配置实例
        Configuration configuration = new Configuration();
        // 设置编码
        configuration.setDefaultEncoding(CHARSET);
        // 模板文件
        configuration.setClassForTemplateLoading(PdfMaker.class,
                PATH_PREFIX);
        Writer out = new StringWriter();
        try {
            // 获取模板,并设置编码方式
            Template template = configuration.getTemplate(htmlTmp);
            // 合并数据模型与模板
            template.process(data, out); // 将合并后的数据和模板写入到流中，这里使用的字符流
            out.flush();
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 创建pdf，指定了页面大小和页边距
     * @param content 字符内容
     * @param dest 目标路径
     * @param isRotation 是否A4横版
     * @throws IOException
     */
    public void createPdf(String content, String dest, boolean isRotation) throws IOException {
        Document document = null;
        PdfWriter writer = null;
        FileOutputStream fileOutputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            // step 1
            //横版时直接输入大小而不用PageSize.A4.rotate()
            //是因为使用rotate会导致读取pdf高宽的时候发生对换，加盖骑缝章错误。
            if (isRotation) {
                document = new Document(new RectangleReadOnly(842F, 595F));
            } else {
                document = new Document(PageSize.A4);
            }
            document.setMargins(90f, 90f, 72f, 72f);//A4默认页边距
            // step 2
            fileOutputStream = new FileOutputStream(dest);
            writer = PdfWriter.getInstance(document, fileOutputStream);
            //增加页码
            writer.setPageEvent(new PdfFooterPager());
            // step 3
            document.open();
            // step 4 设置字体
            XMLWorkerFontProvider fontImp =
                    new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
            fontImp.register(FONT);

            byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
            XMLWorkerHelper.getInstance()
                    .parseXHtml(writer, document, byteArrayInputStream, null,
                            Charset.forName(CHARSET), fontImp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // step 5
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
            if (document != null) {
                document.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    /**
     * 创建横版A4 pdf并添加骑缝章
     * @param dataMap 动态内容
     * @param templateName 模板
     * @param dest 目标路径+目标文件名
     * @param isRotation 是否横版A4
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdfWithStamp(Map<String, Object> dataMap, String templateName,String dest, boolean isRotation) throws IOException, DocumentException{

        //动态生成文本内容
        String content = freeMarkerRender(dataMap, templateName);
        if (null == content) {
            System.out.print("pdf content is null!");
            return;
        }
        // 无章pdf：临时文件
        String destTemp = dest + "temp";
        // 创建无章pdf
        createPdf(content, destTemp, isRotation);
        // 加盖骑缝章
        createPageSealPdf(destTemp, dest, (String) dataMap.get("Uid"));
        // 删除无章pdf
        File tempFile = new File(destTemp);
        if(tempFile.exists()){
            tempFile.delete();
        }
        System.out.println("文件地址："+ dest);
    }
}
