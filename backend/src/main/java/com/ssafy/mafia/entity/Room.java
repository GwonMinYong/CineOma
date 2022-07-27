package com.ssafy.mafia.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Room {
	@Id @GeneratedValue
	private int no;
	private int size;
	private String subject;
	@ManyToOne
	@JoinColumn(name = "id")
	private User user;
	private String password;
}