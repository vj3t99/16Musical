package com.musical16.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.musical16.Entity.ProductEntity;
import com.musical16.Entity.RateEntity;
import com.musical16.Entity.RateReplyEntity;
import com.musical16.Entity.UserEntity;
import com.musical16.converter.RateConverter;
import com.musical16.converter.RateReplyConverter;
import com.musical16.dto.rate.RateDTO;
import com.musical16.dto.rate.RateReplyDTO;
import com.musical16.dto.request.InputRate;
import com.musical16.dto.request.InputRateReply;
import com.musical16.dto.response.ResponseDTO;
import com.musical16.repository.ProductRepository;
import com.musical16.repository.RateReplyRepository;
import com.musical16.repository.RateRepository;
import com.musical16.repository.UserRepository;
import com.musical16.service.IHelpService;
import com.musical16.service.IRateService;

@Service
public class RateService implements IRateService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RateRepository rateRepository;
	
	@Autowired
	private IHelpService helpService;
	
	@Autowired
	private RateConverter rateConverter;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private RateReplyRepository rateReplyRepository;
	
	@Autowired
	private RateReplyConverter rateReplyConverter;
	
	@Override
	public List<RateDTO> findRateUser(HttpServletRequest req) {
		List<RateDTO> result = new ArrayList<>();
		UserEntity user = userRepository.findByUserName(helpService.getName(req));
		for(RateEntity each : rateRepository.findByUserAndFlag(user, false)) {
			RateDTO rateDTO = rateConverter.toDTO(each);
			result.add(rateDTO);
		}
		return result;
	}

	@Override
	public ResponseEntity<?> save(InputRate input, HttpServletRequest req) {
		ResponseDTO<RateDTO> result = new ResponseDTO<>();
		UserEntity user = userRepository.findByUserName(helpService.getName(req));
		RateEntity old = rateRepository.findByIdAndUser(input.getId(), user);
		if(old!=null) {
			if(!old.getFlag()) {
				old = rateConverter.toEntity(input,old);
				old.setCreatedBy(user.getUserName());
				old.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				rateRepository.save(old);
				
				//Tinh diem trung binh cho san pham
				RateUpdate(old.getProduct());
				
				result.setObject(rateConverter.toDTO(old));
				result.setMessage("????nh gi?? th??nh c??ng !");
				return ResponseEntity.ok(result);
			}else {
				result.setMessage("M?? ????nh gi?? ???? ???????c s??? d???ng");
				return ResponseEntity.badRequest().body(result); 
			}
		}else {
			result.setMessage("Kh??ng t??m th???y ????nh gi?? !");
			return ResponseEntity.badRequest().body(result);
		}
	}
	


	@Override
	public ResponseEntity<?> delete(Long id) {
		ResponseDTO<RateDTO> result = new ResponseDTO<>();
		RateEntity rate = rateRepository.findOne(id);
		if(rate!=null) {
			rateRepository.delete(rate);
			//Cap nhat point rate cho product
			RateUpdate(rate.getProduct());
			result.setMessage("X??a ????nh gi?? th??nh c??ng");
			result.setObject(rateConverter.toDTO(rate));
			return ResponseEntity.ok(result);
		}else {
			result.setMessage("????nh gi?? kh??ng t???n t???i");
			return ResponseEntity.badRequest().body(result);
		}
	}

	@Override
	public ResponseEntity<?> saveReply(InputRateReply input, HttpServletRequest req) {
		ResponseDTO<RateReplyDTO> result = new ResponseDTO<>();
		RateEntity rate = rateRepository.findOne(input.getRateId());
		UserEntity user = userRepository.findByUserName(helpService.getName(req));
		if(rate!=null) {
			if(input.getId()!=null) {
				RateReplyEntity oldReply = rateReplyRepository.findByIdAndUser(input.getId(), user);
				if(oldReply!=null) {
					RateReplyEntity reply = rateReplyConverter.toEntity(oldReply, input);
					reply.setModifiedBy(user.getUserName());
					reply.setModifiedDate(new Timestamp(System.currentTimeMillis()));
					rateReplyRepository.save(reply);
					result.setMessage("C???p nh???t th??nh c??ng ph???n h???i ????nh gi?? !");
					result.setObject(rateReplyConverter.toDTO(reply));
					return ResponseEntity.ok(result);
					
				}else {
					result.setMessage("M?? ph???n h???i ????nh gi?? kh??ng t???n t???i ho???c kh??ng th??? thao t??c !");
					return ResponseEntity.badRequest().body(result);
				}
			}else {
				RateReplyEntity reply = rateReplyConverter.toEntity(input);
				reply.setRate(rate);
				reply.setUser(user);
				reply.setCreatedBy(user.getUserName());
				reply.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				rateReplyRepository.save(reply);
				result.setMessage("Th??m ph???n h???i ????nh gi?? th??nh c??ng !");
				result.setObject(rateReplyConverter.toDTO(reply));
				return ResponseEntity.ok(result);
			}
		}else {
			result.setMessage("????nh gi?? kh??ng t???n t???i !");
			return ResponseEntity.badRequest().body(result);
		}
	}

	@Override
	public ResponseEntity<?> deleteReply(Long id, HttpServletRequest req) {
		ResponseDTO<RateReplyDTO> result = new ResponseDTO<>();
		UserEntity user = userRepository.findByUserName(helpService.getName(req));
		RateReplyEntity rateReply = rateReplyRepository.findByIdAndUser(id, user);
		if(rateReply!=null) {
			rateReplyRepository.delete(rateReply);
			result.setMessage("X??a ph???n h???i ????nh gi?? th??nh c??ng");
			result.setObject(rateReplyConverter.toDTO(rateReply));
			return ResponseEntity.ok(result);
		}else {
			result.setMessage("M?? ph???n h???i ????nh gi?? kh??ng t???n t???i ho???c b???n kh??ng th??? x??a ph???n h???i n??y !");
			return ResponseEntity.badRequest().body(result);
		}
	}
	
	public void RateUpdate(ProductEntity product) {
		Integer point = 0;
		for(RateEntity each : rateRepository.findByProductAndFlag(product, true)) {
			point += each.getPoint();
		}
		product.setRate_point((double) point/rateRepository.findByProductAndFlag(product, true).size());
		productRepository.save(product);
	}

	@Override
	public List<RateDTO> showAllRate() {
		List<RateDTO> result = new ArrayList<>();
		Order order = new Order(Direction.DESC, "createdDate");
		Sort sort = new Sort(order);
		for(RateEntity each : rateRepository.findAll(sort)) {
			RateDTO rateDTO = rateConverter.toDTO(each);
			result.add(rateDTO);
		}
		return result;
	}

}
