package com.dataform.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

@Controller
@RequestMapping
public class UserController {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");

	// private Logger log = LogManager.getLogger(getClass());

	@Value("${username}")
	private String username;

	@Value("${password}")
	private String password;

	@RequestMapping(value = "index")
	public String index() {
		return "index";
	}

	@RequestMapping(value = "login")
	public String login(Model model, String username, String password) {
		if (this.username.equals(username) && this.password.equals(password)) {
			return "redirect:showdata";
		}
		model.addAttribute("error", true);
		return "index";
	}

	@RequestMapping(value = "showdata")
	public String showdata(String datetime, Model model) {
		if (datetime == null) {
			datetime = sdf.format(new Date());
		}
		model.addAttribute("datetime", datetime);
		List<String[]> datas = new ArrayList<>();
		try {
			Workbook workbook = Workbook.getWorkbook(new File("upload/" + datetime + ".xls"));
			Sheet sheet = workbook.getSheet("门店上传数据 (2)");
			for (int i = 5; i < sheet.getRows(); i++) {
				List<String> arr = new ArrayList<>();
				Cell[] row = sheet.getRow(i);
				for (Cell c : row) {
					arr.add(getCellContentIgnoreException(c));
				}
				datas.add(arr.toArray(new String[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("datas", datas);
		return "showdata";
	}

	private static String getCellContentIgnoreException(Cell cell) {
		try {
			return cell.getContents().trim();
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	@RequestMapping(value = "/download")
	public void download(HttpServletRequest request, HttpServletResponse response, String datetime, Model model)
			throws Exception {
		try {
			// 打开本地文件流
			InputStream inputStream = new FileInputStream("upload/" + datetime + ".xls");
			// 设置响应头和客户端保存文件名
			response.setCharacterEncoding("UTF-8");
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment;fileName=" + datetime + ".xls");
			// 激活下载操作
			OutputStream os = response.getOutputStream();

			// 循环写入输出流
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				os.write(b, 0, length);
			}

			// 这里主要关闭。
			os.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "uploadData", method = RequestMethod.POST)
	@ResponseBody
	public String uploadData(MultipartFile file, String datetime) {
		if (!file.isEmpty()) {
			try {
				// 这里只是简单例子，文件直接输出到项目路径下。
				// 实际项目中，文件需要输出到指定位置，需要在增加代码处理。
				// 还有关于文件格式限制、文件大小限制，详见：中配置。
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(new File("upload/" + datetime + ".xlsx")));
				out.write(file.getBytes());
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return "上传失败," + e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				return "上传失败," + e.getMessage();
			}
			return "上传成功";
		} else {
			return "上传失败，因为文件是空的.";
		}
	}
}
