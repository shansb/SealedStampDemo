package top.imshan.devtools;

import org.w3c.tidy.Tidy;

import java.io.*;

/**
 * 将html 转换成为严格的XHTML
 */
public class Html2Xhtml {

    /**
     * 转化类
     *
     * @param html  html文件输入路径(带文件名称)
     * @param xhtml xhtml文件输入路径(带文件名称)
     */
    private static String html2Xhtml(String html, String xhtml) {
 
        String path = null;
        try {
        	File xhtmlFile = new File(xhtml);
        	if(xhtmlFile.exists()){
        		xhtmlFile.delete();
        	}
            FileInputStream fin = new FileInputStream(html);
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            int data = -1;
            while ((data = fin.read()) != -1) {
                byteArrayOut.write(data);
            }
            fin.close();
            byte[] htmlFileData = byteArrayOut.toByteArray();

            byteArrayOut.close();

            ByteArrayInputStream tidyInput = new ByteArrayInputStream(
                    htmlFileData);
            ByteArrayOutputStream tidyOut = new ByteArrayOutputStream();
            Tidy tidy = new Tidy();
            tidy.setInputEncoding("GB2312");
            tidy.setOutputEncoding("UTF-8");
            tidy.setShowWarnings(false);
            tidy.setIndentContent(true);
            tidy.setSmartIndent(true);
            tidy.setIndentAttributes(false);
            tidy.setMakeClean(true);
            tidy.setQuiet(true);
            tidy.setWord2000(true);
            tidy.setXHTML(true);
            tidy.setErrout(new PrintWriter(System.out));

            tidy.parse(tidyInput, tidyOut);
            tidyInput.close();
            FileOutputStream out = new FileOutputStream(xhtml);
			tidyOut.writeTo(out);
            tidyOut.flush();
            tidyOut.close();
            out.close();
            path = xhtml;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }
    public static void main(String[] args) {
		String fromPath="C:\\Users\\183546\\Downloads\\100100119000001陈圆圆\\";
		String dest = "C:\\MyWork\\DBank\\mweb\\com.csii.mcm.web.p2p\\META-INF\\config\\ftl\\";
		// for循环批量转换
		for (int i = 0; i < 7; i++) {
			if (i>0 && i<3) {
				continue;
			}
			html2Xhtml(fromPath+"zxrzptfwxy1."+i+".html",dest+"zxrzptfwxy1."+i+".xhtml");			
			html2Xhtml(fromPath+"zxtzptfwxy1."+i+".html",dest+"zxtzptfwxy1."+i+".xhtml");			
		}
	}
}
