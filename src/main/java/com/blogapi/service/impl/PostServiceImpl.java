package com.blogapi.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.blogapi.config.CommonServiceHttpMessage;
import com.blogapi.config.CommonStatus;
import com.blogapi.config.DateTimeConverter;
import com.blogapi.config.ReportServiceHttpMessage;
import com.blogapi.config.ResponseModel;
import com.blogapi.config.ResponseStatus;
import com.blogapi.entity.Category;
import com.blogapi.entity.Post;
import com.blogapi.entity.User;
import com.blogapi.exceptions.ResourceNotFoundException;
import com.blogapi.payloads.PostDto;
import com.blogapi.payloads.PostResponse;
import com.blogapi.repositories.CategoryRepo;
import com.blogapi.repositories.PostRepo;
import com.blogapi.repositories.UserRepo;
import com.blogapi.service.PostService;

@Service
public class PostServiceImpl implements PostService {

	private static final String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String TYPE_XLS = "application/vnd.ms-excel";
	 private static final String ID = "SrNo";
	 private static final String TITLE = "Title";
	 private static final String CONTENT = "Content";
	 private static final String IMAGENAME = "ImageName";
	 private static final String ADDEDDATE = "AddedDate";
	 private static final String USERNAME = "UserName";
	 private static final String CATEGORYTITLE = "CategoryTitle";
	 

	@Autowired
	private PostRepo postRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CategoryRepo categoryRepo;
	
	@Autowired
    private DateTimeConverter dateTimeConverter;

	@Override
	public PostDto createPost(PostDto postDto, Integer userId, Integer categoryId) {

		User user = this.userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId));
		Category category = this.categoryRepo.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Category Id", categoryId));

		Post post = this.modelMapper.map(postDto, Post.class);

		post.setImageName("default.png");
		post.setAddedDate(postDto.getAddedDate()!= null ? dateTimeConverter.stringToDateTime(postDto.getAddedDate()) : null);
		post.setUser(user);
		post.setCategory(category);

		Post newPost = this.postRepo.save(post);

		return this.modelMapper.map(newPost, PostDto.class);
	}

	@Override
	public PostDto updatePost(PostDto postDto, Integer postId) {

		Post post = postRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));

		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setImageName(postDto.getImageName());

		Post updatedPost = postRepo.save(post);

		return modelMapper.map(updatedPost, PostDto.class);
	}

	@Override
	public void deletePost(Integer postId) {
		Post post = postRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
		this.postRepo.delete(post);

	}

	@Override
	public PostResponse getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

		Sort sort = (sortDir.equalsIgnoreCase("asc")) ? sort = Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();

//		if (sortDir.equalsIgnoreCase("asc")) {
//			sort = Sort.by(sortBy).ascending();
//		} else {
//			sort = Sort.by(sortBy).descending();
//		}

		Pageable p = PageRequest.of(pageNumber, pageSize, sort);

		Page<Post> pagePost = this.postRepo.findAll(p);

		List<Post> allPosts = pagePost.getContent();

		List<PostDto> postDtos = allPosts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
				.collect(Collectors.toList());

		PostResponse postResponse = new PostResponse();

		postResponse.setContent(postDtos);
		postResponse.setPageNumber(pagePost.getNumber());
		postResponse.setPageSize(pagePost.getSize());
		postResponse.setTotalElements(pagePost.getTotalElements());
		postResponse.setTotalPages(pagePost.getTotalPages());
		postResponse.setLastPage(pagePost.isLast());

		return postResponse;
	}

	@Override
	public PostDto getPostById(Integer postId) {
		Post post = this.postRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
		return this.modelMapper.map(post, PostDto.class);
	}

	@Override
	public List<PostDto> getPostByUser(Integer userId) {

		User user = this.userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "user is", userId));

		List<Post> posts = this.postRepo.findByUser(user);

		List<PostDto> postDtos = posts.stream().map((post) -> modelMapper.map(post, PostDto.class))
				.collect(Collectors.toList());
		return postDtos;
	}

	@Override
	public List<PostDto> getPostByCategory(Integer categoryId) {

		Category cat = this.categoryRepo.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));

		List<Post> posts = this.postRepo.findByCategory(cat);
		List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
				.collect(Collectors.toList());

		return postDtos;
	}

	@Override
	public List<PostDto> searchPost(String keyword) {

		List<Post> posts = this.postRepo.searchByTitle("%" + keyword + "%");

		List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
				.collect(Collectors.toList());

		return postDtos;
	}

	@Override
	public Object excelToPost(MultipartFile file) {
		ResponseModel rs = null;
		if (Boolean.TRUE.equals(hasExcelFormat(file))) {
			
			try {
				InputStream is = file.getInputStream();
				Workbook workbook = WorkbookFactory.create(is);
				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rows = sheet.iterator();
				List<Post> posts = new ArrayList<Post>();
				int rowNumber = 0;
				while (rows.hasNext()) {
					Row currentRow = rows.next();
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					Iterator<Cell> cellsInRow = currentRow.iterator();
					Post post = new Post();
					int cellIdx = 0;
					while (cellsInRow.hasNext()) {

						DataFormatter formatter = new DataFormatter();
						Cell currentCell = cellsInRow.next();
						switch (cellIdx) {

						case 0:
							if (currentCell != null) {
								String title = formatter.formatCellValue(currentCell);
								if (title != null) {
									post.setTitle(title);
								} else {
									return ResponseStatus.create(
											"Column:" + TITLE + ",Row:" + currentRow.getRowNum()
													+ ",Post title is null",
											null, HttpStatus.CREATED, HttpStatus.CREATED.value());
								}
							} else {
								return ResponseStatus.create(
										"Column:" + TITLE + ",Row:" + currentRow.getRowNum()
												+ ",post title is null",
										null, HttpStatus.CREATED, HttpStatus.CREATED.value());
							}
							break;
						case 1:
							if (currentCell != null) {
								String content = formatter.formatCellValue(currentCell);
								if (content != null) {
									post.setContent(content);
								} else {
									return ResponseStatus.create(
											"Column:" + CONTENT + ",Row:" + currentRow.getRowNum()
													+ ",post Content is null",
											null, HttpStatus.CREATED, HttpStatus.CREATED.value());
								}
							} else {
								return ResponseStatus.create(
										"Column:" + CONTENT + ",Row:" + currentRow.getRowNum()
												+ ",post Content is null",
										null, HttpStatus.CREATED, HttpStatus.CREATED.value());
							}
							break;

						case 2:
							if (currentCell != null) {
								String imageName = formatter.formatCellValue(currentCell);
								if (imageName != null) {
									post.setImageName(imageName);
								} else {
									return ResponseStatus.create(
											"Column:" + IMAGENAME + ",Row:" + currentRow.getRowNum()
													+ ",post imageName is null",
											null, HttpStatus.CREATED, HttpStatus.CREATED.value());
								}
							} else {
								return ResponseStatus.create(
										"Column:" + IMAGENAME + ",Row:" + currentRow.getRowNum()
												+ ",post imageName is null",
										null, HttpStatus.CREATED, HttpStatus.CREATED.value());
							}
							break;

						case 3:
							 if (currentCell != null) {
                                 String userName = formatter.formatCellValue(currentCell);
                                 if (userName != null) {
                                    // Optional<Department> dept = departmentDao.findByCompanyIdAndDepCode(employee.getCompany().getId(), departmentCode);
                                	 Optional<User> us=userRepo.findByName(userName);
                                     if (us.isPresent()) {
                                         post.setUser(us.get());
                                     } else {
                                         return ResponseStatus.create(
                                                 "Column: " + USERNAME + " ,Row:" + currentRow.getRowNum()
                                                         + ", given User Name not found",
                                                 null, HttpStatus.CREATED, HttpStatus.CREATED.value());
                                     }
                                 } else {
                                     return ResponseStatus.create(
                                             "Column: " + USERNAME + " ,Row:" + currentRow.getRowNum()
                                                     + ", User Name is null",
                                             null, HttpStatus.CREATED, HttpStatus.CREATED.value());
                                 }
                             } else {
                                 return ResponseStatus.create(
                                         "Column: " + USERNAME + " ,Row:" + currentRow.getRowNum()
                                                 + ", User Name is null",
                                         null, HttpStatus.CREATED, HttpStatus.CREATED.value());
                             }
							break;

						case 4:
							 if (currentCell != null) {
                                 String categoryTitle = formatter.formatCellValue(currentCell);
                                 if (categoryTitle != null) {
                                   
                                	 Optional<Category> cat=categoryRepo.findByCategoryTitle(categoryTitle);
                                     if (cat.isPresent()) {
                                         post.setCategory(cat.get());
                                     } else {
                                         return ResponseStatus.create(
                                                 "Column: " + CATEGORYTITLE + " ,Row:" + currentRow.getRowNum()
                                                         + ", given Category Title not found",
                                                 null, HttpStatus.CREATED, HttpStatus.CREATED.value());
                                     }
                                 } else {
                                     return ResponseStatus.create(
                                             "Column: " + CATEGORYTITLE + " ,Row:" + currentRow.getRowNum()
                                                     + ", Category Title is null",
                                             null, HttpStatus.CREATED, HttpStatus.CREATED.value());
                                 }
                             } else {
                                 return ResponseStatus.create(
                                         "Column: " + CATEGORYTITLE + " ,Row:" + currentRow.getRowNum()
                                                 + ", Category Title is null",
                                         null, HttpStatus.CREATED, HttpStatus.CREATED.value());
                             }
							break;
						case 5:
							if (currentCell != null) {
								String date = formatter.formatCellValue(currentCell);
								if (date != null) {
									post.setAddedDate(dateTimeConverter.stringToDateTime(date));
								} else {
									return ResponseStatus.create(
											"Column:" + ADDEDDATE + ",Row:" + currentRow.getRowNum()
													+ ",Added date is null",
											null, HttpStatus.CREATED, HttpStatus.CREATED.value());
								}
							} else {
								return ResponseStatus.create(
										"Column:" + ADDEDDATE + ",Row:" + currentRow.getRowNum()
												+ ",Added date is null",
										null, HttpStatus.CREATED, HttpStatus.CREATED.value());
							}
							break;
						default:
							break;

						}
						cellIdx++;
					}
					if (post != null) {

						post.setStatus(CommonStatus.ACTIVE);

						rs = ResponseStatus.create(CommonServiceHttpMessage.POST_CREATED,
								posts.add(postRepo.save(post)), HttpStatus.OK, HttpStatus.OK.value());

					} else {
						rs = ResponseStatus.create(CommonServiceHttpMessage.POST_CREATING_FAILED, null,
								HttpStatus.CREATED, HttpStatus.CREATED.value());
					}
				}
				workbook.close();

			} catch (Exception e) {
				rs = ResponseStatus.create(e.getMessage(), null, HttpStatus.CREATED, HttpStatus.CREATED.value());
				throw new RuntimeException("Failed to parse Excel");

			}
		} else {
			rs = ResponseStatus.create("Please upload an excel file!", null, HttpStatus.CREATED,
					HttpStatus.CREATED.value());
		}
		return rs;
	}

	private Boolean hasExcelFormat(MultipartFile file) {
		return TYPE_XLSX.equals(file.getContentType()) || TYPE_XLS.equals(file.getContentType());
	}

	@Override
	public ResponseModel getAllPostReport() {
		List<PostDto> postDtoList=new ArrayList();
		List<Post> postList= postRepo.findAll();
		
		postList.forEach(p->{
			
			PostDto postDto = new PostDto();
			postDto.setTitle(p.getTitle()!=null?p.getTitle():"---");
			postDto.setContent(p.getContent()!=null?p.getContent():"---");
			
			postDto.setCategoryTitle(p.getCategory().getCategoryTitle()!=null?p.getCategory().getCategoryTitle():"---");
			postDto.setUserName(p.getUser().getName()!=null?p.getUser().getName():"---");
			
			postDto.setAddedDate(p.getAddedDate() != null ? dateTimeConverter.calendarToString(p.getAddedDate()) : "---");
	           
			postDtoList.add(postDto);
		});
		if (postDtoList.isEmpty()) {
            return ResponseStatus.create(ReportServiceHttpMessage.POSTS_NOT_FOUND, null, HttpStatus.CREATED, HttpStatus.CREATED.value());
        } else {
            return ResponseStatus.create(ReportServiceHttpMessage.POSTS_FOUND, postDtoList, HttpStatus.OK, HttpStatus.OK.value());
        }
	}

}
