package top.imshan.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.IOException;

/**
 * pdf页面事件处理:一个超级简单的页码处理器 根据itextpdf官方文档进行修改
 * 这里实现了添加页码的功能
 * @author shansb
 *
 */
public class PdfFooterPager extends PdfPageEventHelper {
	/**
	 * 指定字体
	 */
	private Font font;


	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		try {
			font = new Font(BaseFont.createFont(
					"simhei.ttf", BaseFont.IDENTITY_H,
					BaseFont.NOT_EMBEDDED), 10);
		} catch (DocumentException de) {
			de.printStackTrace();
			throw new ExceptionConverter(de);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ExceptionConverter(ioe);
		}
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		/*
		 * 1.创建一个只有1列的table 2.该列居中显示页数 3.获得pdf图层并写入table
		 */
		PdfPTable table = new PdfPTable(1);
		try {
			// table属性
			table.setWidths(new int[] { 24 });
			table.setTotalWidth(writer.getPageSize().getWidth() * 0.9f);
			table.getDefaultCell().setFixedHeight(20);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			// 单元格属性
			PdfPCell cell = new PdfPCell(new Phrase(String.format("%d",
					writer.getPageNumber()), font));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			// 写入pdf
			PdfContentByte canvas = writer.getDirectContent();
			canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
			table.writeSelectedRows(0, -1, 36, 30, canvas);
			canvas.endMarkedContentSequence();
		} catch (DocumentException de) {
			de.printStackTrace();
			throw new ExceptionConverter(de);
		}
	}

}
