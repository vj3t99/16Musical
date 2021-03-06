package com.musical16.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.musical16.dto.product.ImageDTO;
import com.musical16.dto.response.MessageDTO;

public interface IImageService {


	MessageDTO deleteFile(Long id);

	MessageDTO deleteAllFile(Long id);

	ImageDTO save(MultipartFile file, Long id, HttpServletRequest req);

	
}
