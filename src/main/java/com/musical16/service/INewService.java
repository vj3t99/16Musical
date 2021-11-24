package com.musical16.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.musical16.dto.news.NewDTO;
import com.musical16.dto.response.MessageDTO;

public interface INewService {

	List<NewDTO> findAll();

	MessageDTO save(NewDTO newDTO, HttpServletRequest req);

	MessageDTO delete(Long id);

	NewDTO findOne(Long id);

	MessageDTO uploadImage(MultipartFile file, Long id, HttpServletRequest req);

}
