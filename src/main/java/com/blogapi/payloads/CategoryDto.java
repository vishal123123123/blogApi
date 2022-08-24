package com.blogapi.payloads;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class CategoryDto {
	
	private  Integer categoryId;
	
	@NotEmpty
	@Size(min=4,message = "min size of category Title is 4")
	private String categoryTitle;
	
	@NotEmpty
	@Size(min=10,message = "min size of category Title is 10")
	private String categoryDescription;

}
