package com.dataform.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

@Controller
@RequestMapping
public class UserController {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM");

	// private Logger log = LogManager.getLogger(getClass());

	@Value("${user}")
	private String user;

	@Value("${password}")
	private String password;

	@RequestMapping(value = "index")
	public String index(HttpSession session) {
		session.removeAttribute("adminLogin");
		return "index";
	}

	@RequestMapping(value = "toLogin")
	public String toLogin(HttpSession session) {
		session.removeAttribute("dataLogin");
		return "login";
	}

	// 后台登录
	@RequestMapping(value = "login")
	public String login(Model model, String username, String password, HttpSession session) {
		if (this.user.equals(username) && this.password.equals(password)) {
			session.setAttribute("adminLogin", "true");
			return "redirect:toUpload";
		}
		model.addAttribute("error", true);
		return "index";
	}

	@RequestMapping(value = "toUpload")
	public String toUpload() {
		return "upload";
	}

	// 前台登录
	@RequestMapping(value = "showDataLogin")
	public String showDataLogin(Model model, String username, String password, HttpSession session) {
		if (this.user.equals(username) && this.password.equals(password)) {
			session.setAttribute("dataLogin", "true");
			return "redirect:showdata";
		}
		model.addAttribute("error", true);
		return "login";
	}

	@RequestMapping(value = "showdata")
	public String showdata(String datetime, Model model, HttpSession session) {
		if (session.getAttribute("dataLogin") == null) {
			return "login";
		}
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
	public void download(HttpServletRequest request, HttpServletResponse response, String datetime, Model model,
			HttpSession session) throws Exception {
		if (session.getAttribute("dataLogin") == null) {
			response.getOutputStream().write("请重新登录".getBytes());
			return;
		}
		try {
			// 打开本地文件流
			InputStream inputStream = new FileInputStream("upload/" + datetime + ".xls");
			String fileName = sdf2.format(sdf.parse(datetime));
			// 设置响应头和客户端保存文件名
			response.setCharacterEncoding("UTF-8");
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment;fileName=" + fileName + ".xls");
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
	public String uploadData(MultipartFile file, String datetime, HttpSession session, Model model) {
		if (session.getAttribute("adminLogin") == null) {
			return "index";
		}
		if (!file.isEmpty()) {
			try {
				// 这里只是简单例子，文件直接输出到项目路径下。
				// 实际项目中，文件需要输出到指定位置，需要在增加代码处理。
				// 还有关于文件格式限制、文件大小限制，详见：中配置。
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(new File("upload/" + datetime + ".xls")));
				out.write(file.getBytes());
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("error", e.getMessage());
			}
		}
		return "upload";
	}
}
