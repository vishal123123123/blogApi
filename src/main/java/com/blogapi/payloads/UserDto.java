package com.blogapi.payloads;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserDto {
	
	private int id;
	@NotEmpty
	@Size(min=4,message="UserName should be 4 character ! !")
	private String name;
	@Email(message="Email address not valid ! !")
	private String email;
	@NotEmpty
	@Size(min=3,max=10,message="Password should be min 3 chars and max 10 chars !!")
	private String password;
	@NotEmpty
	private String about;

	private Set<CommentDto> comments=new HashSet<>();
}
