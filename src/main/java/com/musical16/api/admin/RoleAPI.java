package com.musical16.api.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musical16.Entity.RoleEntity;
import com.musical16.Entity.UserEntity;
import com.musical16.repository.RoleRepository;
import com.musical16.repository.UserRepository;

@RestController
public class RoleAPI {
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired UserRepository userRepository;

	@Autowired
    private BCryptPasswordEncoder bcryptEncoder;
	
	@GetMapping("/roles")
	public void CreateRoles() {
		RoleEntity role1 = new RoleEntity();
		role1.setName("ADMIN");
		role1.setCode("ADMIN");
		roleRepository.save(role1);
		RoleEntity role2 = new RoleEntity();
		role2.setName("MANAGER");
		role2.setCode("MANAGER");
		roleRepository.save(role2);
		RoleEntity role3 = new RoleEntity();
		role3.setName("USER");
		role3.setCode("USER");
		roleRepository.save(role3);
		
		UserEntity user = new UserEntity();
		user.setUserName("admin");
		user.setEmail("admin@musical.com");
		user.setPassword(bcryptEncoder.encode("admin"));
		user.setFullName("admin");
		user.setStatus(1);
		List<RoleEntity> roles = new ArrayList<>();
		roles.add(role1);
		user.setRoles(roles);
		userRepository.save(user);
	}
}
