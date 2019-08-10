package top.imshan.pdf;

import com.itextpdf.text.DocumentException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PdfMakerTest {
    public static void main(String[] args){
        Map<String, Object> dynamicData = new HashMap<String, Object>();
        PdfMakerTest test = new PdfMakerTest();
        test.createStampPdf(dynamicData,"testTemplate.html","resources/test","demo.pdf",true);

    }

    /**
     * 创建横版pdf并加盖骑缝章
     * @param dataMap 内容
     * @param template 模板
     * @param filePath 路径
     * @param name 文件名
     * @param isRotated 是否A4横版
     */
    private void createStampPdf(Map<String, Object> dataMap, String template,
                                String filePath, String name, boolean isRotated) {
        dataMap.put("Uid", generateUid());
        String dest = filePath + File.separator + name;
        PdfMaker marker = new PdfMaker();
        try {
            marker.createPdfWithStamp(dataMap, template, dest, isRotated);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private String generateUid() {
        //序号使用自定义字库，只支持数字和大写字母
        return "ADDER23";
    }
}
