package com.ssafy.mafia.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mafia.service.JwtServiceImpl;
import com.ssafy.mafia.entity.User;
import com.ssafy.mafia.service.UserService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/user")
public class UserController {
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	@Autowired
	private JwtServiceImpl jwtService;

	@Autowired
	private UserService userService;

	@ApiOperation(value = "�α���", notes = "Access-token�� �α��� ��� �޼����� ��ȯ�Ѵ�.", response = Map.class)
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
			@RequestBody @ApiParam(value = "�α��� �� �ʿ��� ȸ������(���̵�, ��й�ȣ).", required = true) User user) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		try {
			User loginUser = userService.login(user);
			if (loginUser != null) {
				String token = jwtService.create("id", loginUser.getId(), "access-token");// key, data, subject
				logger.debug("�α��� ��ū���� : {}", token);
				resultMap.put("access-token", token);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.ACCEPTED;
			}
		} catch (Exception e) {
			logger.error("�α��� ���� : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@ApiOperation(value = "ȸ������", notes = "ȸ�� ������ ���� Token�� ��ȯ�Ѵ�.", response = Map.class)
	@GetMapping("/info/{userid}")
	public ResponseEntity<Map<String, Object>> getInfo(
			@PathVariable("userid") @ApiParam(value = "������ ȸ���� ���̵�.", required = true) String userid,
			HttpServletRequest request) {
//		logger.debug("userid : {} ", userid);
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		if (jwtService.isUsable(request.getHeader("access-token"))) {
			logger.info("��� ������ ��ū!!!");
			try {
//				�α��� ����� ����.
				User user = userService.userInfo(userid);
				resultMap.put("userInfo", user);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} catch (Exception e) {
				logger.error("������ȸ ���� : {}", e);
				resultMap.put("message", e.getMessage());
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			logger.error("��� �Ұ��� ��ū!!!");
			resultMap.put("message", FAIL);
			status = HttpStatus.ACCEPTED;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PostMapping
	public ResponseEntity<String> signup(@RequestBody User user, HttpSession session, HttpServletResponse response)
			throws Exception {
		int found = userService.idCheck(user.getId());
		if (found == 0) {
			userService.registerUser(user);
			return new ResponseEntity<String>(SUCCESS, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "ȸ���������", notes = "�Ѹ��� ȸ�� ������ ��´�. ������ �ش� member��ü ��ȯ. ���н�  no_content ��ȯ", response = User.class)
	@GetMapping(value = "/{userid}")
	public ResponseEntity<User> userInfo(@PathVariable("userid") String userid) throws Exception {
		User user = userService.userInfo(userid);
		System.out.println(user.toString());
		if (user != null)
			return new ResponseEntity<User>(user, HttpStatus.OK);
		else
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "ȸ�� ����", notes = "�ش� ȸ���� ������ ��´�. ������ member��ü ��ȯ", response = User.class)
	@PutMapping
	public ResponseEntity<User> modifyUser(@RequestBody User user) throws Exception {
		System.out.println(user.toString());

		userService.updateUser(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	// ��������
	@ApiOperation(value = "ȸ������", notes = "�ش�ȸ���� ������Ų��.", response = String.class)
	@DeleteMapping(value = "/{userid}")
	public ResponseEntity<String> removeUser(@PathVariable("userid") String userid) throws Exception {
		userService.deleteUser(userid);
		return new ResponseEntity<String>(SUCCESS, HttpStatus.OK);
	}

}
