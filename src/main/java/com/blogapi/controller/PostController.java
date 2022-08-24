package com.blogapi.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blogapi.config.AppConstants;
import com.blogapi.config.ResponseModel;
import com.blogapi.payloads.ApiResponse;
import com.blogapi.payloads.PostDto;
import com.blogapi.payloads.PostResponse;
import com.blogapi.service.FileService;
import com.blogapi.service.PostService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class PostController {

	@Autowired
	private PostService postService;

	@Autowired
	private FileService fileService;

	@Value("${project.image}")
	private String path;

	// create post
	@PostMapping("/user/{userId}/category/{categoryId}/posts")
	public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto, @PathVariable Integer userId,
			@PathVariable Integer categoryId) {

		PostDto createPost = this.postService.createPost(postDto, userId, categoryId);

		return new ResponseEntity<PostDto>(createPost, HttpStatus.CREATED);

	}

	// get post by User
	@GetMapping("/user/{userId}/posts")
	public ResponseEntity<List<PostDto>> getPostByUser(@PathVariable Integer userId) {

		List<PostDto> posts = this.postService.getPostByUser(userId);

		return new ResponseEntity<List<PostDto>>(posts, HttpStatus.OK);
	}

	// get posts by Category
	@GetMapping("/category/{categoryId}/posts")
	public ResponseEntity<List<PostDto>> getPostByCategory(@PathVariable Integer categoryId) {

		List<PostDto> posts = this.postService.getPostByCategory(categoryId);

		return new ResponseEntity<List<PostDto>>(posts, HttpStatus.OK);
	}

	// GET all posts
	@GetMapping("/posts")
	public ResponseEntity<PostResponse> getAllPosts(
			@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
			@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir) {

		PostResponse postResponse = postService.getAllPost(pageNumber, pageSize, sortBy, sortDir);
		return new ResponseEntity<PostResponse>(postResponse, HttpStatus.OK);
	}

	// get post by id
	@GetMapping("/posts/{postId}")
	public ResponseEntity<PostDto> getPostById(@PathVariable Integer postId) {

		PostDto post = postService.getPostById(postId);
		return new ResponseEntity<PostDto>(post, HttpStatus.OK);
	}

	// delete Post
	@DeleteMapping("/posts/{postId}")
	public ApiResponse deletePost(@PathVariable Integer postId) {
		postService.deletePost(postId);
		return new ApiResponse("post Successfuly deleted !!!", true);
	}

	// Update Post
	@PutMapping("/posts/{postId}")
	public ResponseEntity<PostDto> updatePost(@RequestBody PostDto postDto, @PathVariable Integer postId) {
		PostDto updatePost = this.postService.updatePost(postDto, postId);
		return new ResponseEntity<PostDto>(updatePost, HttpStatus.OK);
	}

	// search
	@GetMapping("/posts/search/{keywords}")
	public ResponseEntity<List<PostDto>> searchPostByTitle(@PathVariable("keywords") String keywors) {

		List<PostDto> result = this.postService.searchPost(keywors);
		return new ResponseEntity<List<PostDto>>(result, HttpStatus.OK);

	}

	// post image upload
	@PostMapping("/posts/image/upload/{postId}")
	public ResponseEntity<PostDto> uploadPostImage(@RequestParam("image") MultipartFile image,
			@PathVariable Integer postId) throws IOException {
		PostDto postDto = this.postService.getPostById(postId);
		String fileName = this.fileService.uploadImage(path, image);

		postDto.setImageName(fileName);
		PostDto updatePost = this.postService.updatePost(postDto, postId);

		return new ResponseEntity<PostDto>(updatePost, HttpStatus.OK);
	}

	// method for serve image
	@GetMapping(value = "/post/iamge/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public void downloadImage(@PathVariable("imageName") String imageName, HttpServletResponse response)
			throws IOException {
		InputStream resource = this.fileService.getResource(path, imageName);
		((ServletResponse) resource).setContentType(MediaType.IMAGE_JPEG_VALUE);
		StreamUtils.copy(resource, response.getOutputStream());

	}
	@PostMapping("/import/excel")
	public ResponseEntity<?> uploadFile(
			@RequestParam("file") MultipartFile file) throws Exception {
		return new ResponseEntity<>(postService.excelToPost(file), HttpStatus.OK);
	}
	
	@GetMapping("/generate/report")
	public void generateReport(HttpServletResponse response,
			@RequestParam(value = "formatType") String formatType) throws IOException, JRException {

		ResponseModel rs = postService.getAllPostReport();

		if (rs.getData() != null) {

			JasperReport jasperReport = JasperCompileManager
					.compileReport(getClass().getResourceAsStream("/jrxml/postReport.jrxml"));

			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource((Collection<?>) rs.getData());
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("creadtedBy", "Capital Projects User report ");
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

			final OutputStream outputStream = response.getOutputStream();

			if (formatType.equals("pdf")) {
				response.setContentType("application/x-pdf");
				response.setHeader("Content-disposition", "inline; filename=App_report_en.pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

			} else if (formatType.equals("html")) {
				response.setContentType("application/x-html");
				response.setHeader("Content-disposition", "inline; filename=App_report_en.html");
				JasperExportManager.exportReportToHtmlFile(jasperPrint, outputStream.toString());

			} else if (formatType.equals("excel")) {
				JRXlsxExporter exporter = new JRXlsxExporter();
				SimpleXlsxReportConfiguration reportConfigXLS = new SimpleXlsxReportConfiguration();
				reportConfigXLS.setSheetNames(new String[] { "sheet1" });
				exporter.setConfiguration(reportConfigXLS);
				exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
				response.setHeader("Content-Disposition", "attachment;filename=jasperReport.xlsx");
				response.setContentType("application/octet-stream");
				exporter.exportReport();

			} else if (formatType.equals("csv")) {

				JRCsvExporter csvExporter = new JRCsvExporter();
				csvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				csvExporter.setExporterOutput(new SimpleWriterExporterOutput(response.getOutputStream()));

				response.setHeader("Content-Disposition", "attachment;filename=jasperReport.csv");
				response.setContentType("application/octet-stream");
				csvExporter.exportReport();

			} else {
				response.setContentType("application/x-pdf");
				response.setHeader("Content-disposition", "inline; filename=App_report_en.pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
			}

		}

	}

}
