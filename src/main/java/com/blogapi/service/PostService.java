package com.blogapi.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.blogapi.config.ResponseModel;
import com.blogapi.entity.Post;
import com.blogapi.payloads.PostDto;
import com.blogapi.payloads.PostResponse;

public interface PostService {
	
	//create Post
	public PostDto createPost(PostDto postDto,Integer userId,Integer categoryId);
	
	//update post
	public PostDto updatePost(PostDto postDto,Integer postId);
	
	//delete post
	public void deletePost(Integer postId);
	
	//get All posts
	public PostResponse getAllPost(Integer pageNumber,Integer pageSize,String sortBy,String sortDir);
	
	//get post by id
	public PostDto getPostById(Integer postId);
	
	//get post by user
	public List<PostDto> getPostByUser(Integer userId);
	
	//get post by category
	public List<PostDto> getPostByCategory(Integer categoryId);

	//search post
	List<PostDto> searchPost(String keyword);

	public Object excelToPost(MultipartFile file);

	public ResponseModel getAllPostReport();
}
