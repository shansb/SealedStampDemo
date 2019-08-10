# 动态生成骑缝章Demo

## 功能
1. 支持动态生成水印印章（d：38mm）
1. 根据页码动态加盖骑缝章
1. 根据模板关键字定位文末加盖完整印章
1. 自定义页面大小和页边距(`PdfMaker.createPdf()`中定义）
1. 实现添加pdf页码

## 说明
- 测试主入口：`PdfMakerTest`
- `jtidy.jar`只是用来规范html标签，非产品所需三方库。
- 印章素材推荐大于330*330
- simhei.ttf为完整黑体字库
- tinysun.ttf为自制只带数字和大小字母的宋体字库，用来添加印章水印。
- 印章水印位置需要自己重新调整。

## 开发流程
1. 制作xhtml模板：
  - 全新制作：自己写xhtml，可参考`resources/test/testTemplate.html`.
  - word转码: word手动另存为html，通过`Html2Xhtml`进行标签规范，然后手动检查。
2. 填入freemarker动态内容占位符（如有需要）
3. 按照`PdfMakerTest.createStampPdf()`调用即可。