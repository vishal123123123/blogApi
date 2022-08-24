package com.blogapi.payloads;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
public class PostDto {

	private Integer postId;
	private String title;
	private String content;
	private String imageName;

	private String addedDate;

	private CategoryDto category;
	private String categoryTitle;

	private UserDto user;
	private String userName;
	
	private Set<CommentDto> comments=new HashSet<>();

}
