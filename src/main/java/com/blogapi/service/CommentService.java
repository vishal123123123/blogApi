package com.blogapi.service;

import com.blogapi.payloads.CommentDto;

public interface CommentService {
	
	public CommentDto createComment(CommentDto commentDto,Integer postId,Integer userId);
	
	public void deleteComment(Integer commentId);

}
