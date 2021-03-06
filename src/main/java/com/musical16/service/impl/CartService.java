package com.musical16.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.musical16.Entity.CartDetailEntity;
import com.musical16.Entity.CartEntity;
import com.musical16.Entity.ProductEntity;
import com.musical16.Entity.UserEntity;
import com.musical16.converter.CartDetailConverter;
import com.musical16.dto.cart.CartDTO;
import com.musical16.dto.cart.CartDetailDTO;
import com.musical16.dto.response.MessageDTO;
import com.musical16.repository.CartDetailRepository;
import com.musical16.repository.CartRepository;
import com.musical16.repository.ProductRepository;
import com.musical16.repository.UserRepository;
import com.musical16.service.ICartService;
import com.musical16.service.IHelpService;

@Service
public class CartService implements ICartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartDetailRepository cartDetailRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private IHelpService helpService;

	@Autowired
	private CartDetailConverter cartDetailConverter;

	@Override
	public CartDTO findAll(HttpServletRequest req) {
		CartDTO cartDTO = new CartDTO();
		try {
			UserEntity user = userRepository.findByUserName(helpService.getName(req));
			CartEntity cart = cartRepository.findByUser(user);
			updateCart(cart);
			List<CartDetailDTO> cartDetailDTO = new ArrayList<>();
			if (cart != null) {
				for (CartDetailEntity each : cartDetailRepository.findByCart(cart)) {
					cartDetailDTO.add(cartDetailConverter.toDTO(each));
				}
			}
			cartDTO.setId(cart.getId());
			cartDTO.setTotalPrice(cart.getTotalPrice());
			cartDTO.setTotalQuantity(cart.getTotalQuantity());
			cartDTO.setCartDetail(cartDetailDTO);
		} catch (NullPointerException e) {

		}
		return cartDTO;
	}

	@Override
	public MessageDTO save(CartDetailDTO cartDetailDTO, HttpServletRequest req) {
		MessageDTO message = new MessageDTO();
		try {
			UserEntity user = userRepository.findByUserName(helpService.getName(req));
			CartEntity cart = cartRepository.findByUser(user);
			CartDetailEntity cartDetail = new CartDetailEntity();		
			ProductEntity product = productRepository.findOne(cartDetailDTO.getProductId());
			
			//N???u kh??ng c?? id chi ti???t gi??? h??ng th?? th??m m???i
			if (cartDetailDTO.getId() == null) {
				List<CartDetailEntity> listCartDetail = cartDetailRepository.findByCartAndProduct(cart, product);
				//N???u s???n ph???m ???? t???n t???i trong gi??? h??ng th?? c???ng th??m s??? l?????ng
				if (listCartDetail.size()==1) {
					for(CartDetailEntity each : listCartDetail)
						cartDetail = each;
					cartDetail.setQuantity(cartDetailDTO.getQuantity()+cartDetail.getQuantity());
					//Ki???m tra s??? l?????ng c???a gi??? h??ng v???i s??? l?????ng c???a kho h??ng ??ang c??
					if (cartDetail.getQuantity()+cartDetailDTO.getQuantity() <= product.getQuantity()) {
						cartDetail.setPrice(product.getPrice() * cartDetail.getQuantity());
						cartDetail.setModifiedBy(helpService.getName(req));
						cartDetail.setModifiedDate(new Timestamp(System.currentTimeMillis()));
						cartDetailRepository.save(cartDetail);
						message.setMessage("Th??m s??? l?????ng v??o gi??? h??ng th??nh c??ng");
						updateCart(cart);
					} else {
						message.setMessage("Shop ch??ng t??i kh??ng c?? ????? s??? l?????ng ????? cung c???p");
					}
				// N???u s???n ph???m kh??ng t???n t???i trong gi??? h??ng th?? t???o 1 s???n ph???m m???i trong gi??? h??ng
				} else {
					cartDetail.setCart(cart);
					cartDetail.setProduct(product);
					cartDetail.setQuantity(cartDetailDTO.getQuantity());
					//Ki???m tra s??? l?????ng c???a gi??? h??ng v???i s??? l?????ng c???a kho h??ng ??ang c??
					if (cartDetailDTO.getQuantity() <= product.getQuantity()) {
						cartDetail.setPrice(product.getPrice() * cartDetail.getQuantity());
						cartDetail.setCreatedBy(helpService.getName(req));
						cartDetail.setCreatedDate(new Timestamp(System.currentTimeMillis()));
						cartDetailRepository.save(cartDetail);
						message.setMessage("Th??m s???n ph???m v??o gi??? h??ng th??nh c??ng");
						updateCart(cart);
					} else {
						message.setMessage("Shop ch??ng t??i kh??ng c?? ????? s??? l?????ng ????? cung c???p");
					}
				}
			//N???u c?? id th?? s??? c???p nh???t s??? l?????ng s???n ph???m
			} else {
				cartDetail = cartDetailRepository.findOne(cartDetailDTO.getId());		
				cartDetail.setQuantity(cartDetailDTO.getQuantity());
				//Ki???m tra s??? l?????ng c???a gi??? h??ng v???i s??? l?????ng c???a kho h??ng ??ang c??
				if (cartDetailDTO.getQuantity() <= cartDetail.getProduct().getQuantity()) {
					cartDetail.setPrice(cartDetail.getProduct().getPrice() * cartDetail.getQuantity());
					cartDetail.setModifiedBy(helpService.getName(req));
					cartDetail.setModifiedDate(new Timestamp(System.currentTimeMillis()));
					cartDetailRepository.save(cartDetail);
					message.setMessage("C???p nh???t s??? l?????ng s???n ph???m th??nh c??ng");
					updateCart(cart);
				} else {
					message.setMessage("Shop ch??ng t??i kh??ng c?? ????? h??ng ????? cung c???p");
				}

			}
		} catch (NullPointerException e) {
			message.setMessage("S???n ph???m kh??ng t???n t???i ho???c gi??? h??ng c???a b???n ch??a ???????c t???o");
		}
		return message;
	}
	
	private void updateCart(CartEntity cart) {
		Integer Quantity = 0;
		Double Price = 0.0;
		if(cart!=null) {
			for(CartDetailEntity each : cart.getCartDetail()) {
				Quantity += each.getQuantity();
				Price += each.getPrice();		
			}	
		}
		cart.setTotalQuantity(Quantity);
		cart.setTotalPrice(Price);
		cartRepository.save(cart);
		
	}

	@Override
	public MessageDTO delete(Long id, HttpServletRequest req) {
		MessageDTO message = new MessageDTO();
		CartEntity cart = cartRepository.findByUser(userRepository.findByUserName(helpService.getName(req)));
		if(cart!=null) {
			List<CartDetailEntity> list = cartDetailRepository.findByCartAndProduct(cart, productRepository.findOne(id));
			if(!list.isEmpty()) {
				for(CartDetailEntity each : list) {
					cartDetailRepository.delete(each);
				}
				message.setMessage("X??a s???n ph???m th??nh c??ng kh???i gi??? h??ng");
			}else {
				message.setMessage("S???n ph???m n??y kh??ng c?? trong gi??? h??ng");
			}
			updateCart(cart);
		}else {
			message.setMessage("Gi??? h??ng kh??ng t???n t???i");
		}
		return message;
	}

}
